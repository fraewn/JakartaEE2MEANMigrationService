package com.migration.service.service.codeGeneration;

import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledgeService;
import com.migration.service.model.migrationKnowledge.entityMigration.EntityModel;
import com.migration.service.model.migrationKnowledge.entityMigration.EntityModelService;
import com.migration.service.service.util.EnvironmentUtils;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GenerateCode {
	private ModuleKnowledgeService moduleKnowledgeService;
	private NodeKnowledgeService nodeKnowledgeService;
	private OntologyKnowledgeService ontologyKnowledgeService;
	private EntityModelService entityModelService;
	String modelPackage;
	String controllerPackage;
	List<EntityModel> entityModels = new ArrayList<>();

	String path = "";

	public GenerateCode(ModuleKnowledgeService moduleKnowledgeService,
	NodeKnowledgeService nodeKnowledgeService,
	OntologyKnowledgeService ontologyKnowledgeService,
	EntityModelService entityModelService) {
		this.moduleKnowledgeService = moduleKnowledgeService;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.ontologyKnowledgeService = ontologyKnowledgeService;
		this.entityModelService = entityModelService;
	}

	public void executeCodeGeneration(String path){
		entityModels = entityModelService.findAll();
		this.path = path.replaceAll("/", "\\\\");
		this.createBackendDirectory();
		this.createModels();
		this.createRestControllers();
	}

	// ######################### Rest Controllers ####################
	public List<Node> getAllRestControllers(){
		String query = "MATCH (m:RestController) return m";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("m").asNode());
		}
	}

	public List<String> getFunctionalitiesAssociatedWithRestController(String id){
		String query = "MATCH(m:RestController)-[]-(f:Functionality) where m.id='" + id + "' return m";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("m").asNode().get("name").asString());
		}
	}

	public List<String> getOtherModelsAssociatedWithRestController(String id, String module){
		String query = "MATCH(n:RestController)-[]-(m:Model) where n.id='" + id + "' and  m.module<>'" + module + "' return m";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("m").asNode().get("id").asString());
		}
	}

	public List<String> getComponentsAssociatedWithServiceThatUsesRoute(String id){
		String query = "match(rc:RestController)-[]-(r:Route)-[]-(s:Service)-[]-(m:Interface)-[]-(c:Component) where rc.id='" + id + "' " +
				"return c";
		String queryForControllersWithMiddleware = "match(rc:RestController)-[]-(k:Middleware)-[]-(r:Route)-[]-(s:Service)-[]-" +
				"(m:Interface)-[]-(c:Component) where rc.id='" + id + "' " +
				"return c";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {

			return session.run(query).list(result -> result.get("c").asNode().get("id").asString()).size()==0 ?
					session.run(queryForControllersWithMiddleware).list(result -> result.get("c").asNode().get("id").asString()) :
					session.run(query).list(result -> result.get("c").asNode().get("id").asString());
		}
	}

	public List<String> getModelByController(String id){
		String query = "match(rc:RestController)-[]-(m:Model) where rc.id='" + id + "' and rc.module=m.module " +
				"return m";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("m").asNode().get("id").asString());
		}
	}

	public void createRestControllers(){
		List<Node> controllerNodes = this.getAllRestControllers();
		for(Node node : controllerNodes){
			String filename = node.get("id").asString();
			String module = node.get("module").asString();
			boolean transaction = this.checkForTransaction(filename);
			String packageName = "\\" + node.get("package").asString() + "\\";
			System.out.println("/*++++++ Code generation for file: " + filename + " at location: " + this.path + packageName + " " +
					"+++++*/\n");
			this.generateRestControllerCode(filename, transaction, module);
		}
	}

	public boolean checkForTransaction(String id){
		String query = "match(n:RestController)-[r]-(m:Model) where n.id='" + id + "' return count(r)";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("count(r)").asInt()).get(0)==2 ? true : false;
		}
	}

	public String removeComponentPartsFromName(String name){
		return name.replace(".component.ts","");
	}

	public void generateRestControllerCode(String filename, boolean transaction, String module){
		List<String> functionalities = this.getFunctionalitiesAssociatedWithRestController(filename);
		List<String> otherAssociatedModels = this.getOtherModelsAssociatedWithRestController(filename, module);
		List<String> neededMethodsForComponent = this.getComponentsAssociatedWithServiceThatUsesRoute(filename);
		String importedFunctionalities = "";
		for(String func : functionalities){
			importedFunctionalities = importedFunctionalities + "const " + func.replace(" ", "") + " = require('" + func + "');\n";
		}
		String otherModelImport = "";
		importedFunctionalities = importedFunctionalities + "\n";
		for(String model : otherAssociatedModels){
			//String model = this.getModelByController(controller).get(0).;
			otherModelImport =
					otherModelImport + "const " + model.replace(".js", "") + " = require('../" + this.modelPackage + "/" + model.toLowerCase() + "');" +
							"\n";
		}
		String model = this.getModelByController(filename).get(0).replace(".js", "");
		String ownControllerImport = "const " +  model + " = require('../" + this.modelPackage + "/" + model.toLowerCase() + "');\n\n";

		String methods = "";
		for(String method : neededMethodsForComponent){
			methods = methods + "exports." + this.removeComponentPartsFromName(method.replace("-","")) + " = (req, res, next) => { \n// " +
					"TODO: Implement method  \n}\n";
		}
		// if transaction func -> dann bitte ein Todo mit mach transaction bitte entwickler
		String todo="";
		if(transaction){
			todo = "// TODO implement transactional logic \n\n";
		}
		String code = importedFunctionalities +  otherModelImport + ownControllerImport + todo + methods;

		System.out.println(code);
	}

	// ######################### Models ##############################
	public List<Node> getAllModels(){
		String query = "MATCH (m:Model) return m";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("m").asNode());
		}
	}

	public List<String> getFunctionalitiesAssociatedWithModel(String modelId){
		String query = "MATCH(m:Model)-[]-(f:Functionality) where m.id='" + modelId + "' return f";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("f").asNode().get("name").asString());
		}
	}

	public void createModels(){
		String directory = "\\";
		List<Node> modelNodes = this.getAllModels();
		for(Node node : modelNodes){
			String filename = node.get("id").asString();
			this.modelPackage = node.get("package").asString();
			String packageName = "\\" + node.get("package").asString() + "\\";
			String functionality = this.getFunctionalitiesAssociatedWithModel(filename).get(0);
			System.out.println("/*++++++ Code generation for file: " + filename + " at location: " + this.path + packageName + " " +
					"+++++*/\n");
			this.generateModelCode(functionality, filename);

		}
	}

	public void generateModelCode(String functionality, String filename){
		HashMap<String, String> attributeTypes = new HashMap<>();
		String entityName = "";
		for(EntityModel entityModel : entityModels){
			if(this.transformJavaFilenameToPureEntity(entityModel.getName())
					.equals(this.transformJSFilenameToPureEntity(filename))){
				attributeTypes = entityModel.getAttributeTypes();
				entityName = entityModel.getName();
			}
		}
		String functionalityImport = "const " + functionality + " = require('" + functionality + "');\n\n";
		String schemaDefintion = this.transformJSFilenameToPureEntity(filename) + "Schema = " + functionality +
				".Schema({\n";
		String attributeDefintion = "";
		for (Map.Entry<String, String> entry : attributeTypes.entrySet()) {
			attributeDefintion = attributeDefintion + entry.getKey()
					+ ": {type: " + entry.getValue() + ", required: true},\n";
		}
		// cut off last comma
		attributeDefintion = attributeDefintion.substring(0, attributeDefintion.length()-2)
				                 + "\n});\n\n";
		String moduleExport =
				"module.exports = " + functionality + ".model('"
						+ entityName.replace(".java", "")
						+ "', " + this.transformJSFilenameToPureEntity(filename) + "Schema)";
		String code = functionalityImport + schemaDefintion + attributeDefintion + moduleExport;
		System.out.println(code+"\n\n\n");
	}











	public void generateControllerCode(String filename){

	}

	public String transformJSFilenameToPureEntity(String filename){
		return filename.replace(".js", "").toLowerCase();
	}

	public String transformJavaFilenameToPureEntity(String filename){
		return filename.replace(".java", "").toLowerCase();
	}

	public void createBackendDirectory(){
		this.path = path + "\\mean\\backend";
	}












	public Driver setUpNeo4jDriver(String env){
		String url = "";
		String username = "";
		String password = "";
		try {
			EnvironmentUtils envUtils = new EnvironmentUtils();
			url = envUtils.getEnvironment(env, "ip", "port", "portType");
			username = envUtils.getCredential(env, "username");
			password = envUtils.getCredential(env, "password");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return GraphDatabase.driver(url, AuthTokens.basic(username, password));
	}

	public Session setUpNeo4jSession(Driver driver){
		return driver.session();
	}


}
