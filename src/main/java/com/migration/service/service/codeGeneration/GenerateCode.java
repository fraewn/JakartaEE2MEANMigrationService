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
	}

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
			String packageName = node.get("package").asString();
			String functionality = this.getFunctionalitiesAssociatedWithModel(filename).get(0);
			this.generateModelCode(functionality, filename);

		}
	}

	public void generateModelCode(String functionality, String filename){
		HashMap<String, String> attributeTypes = new HashMap<>();
		String entityName = "";
		for(EntityModel entityModel : entityModels){
			if(this.transformJavaFilenameToPureEntity(entityModel.getName()).equals(this.transformJSFilenameToPureEntity(filename))){
				attributeTypes = entityModel.getAttributeTypes();
				entityName = entityModel.getName();
			}
		}
		String functionalityImport = "const " + functionality + " = require('" + functionality + "');\n\n";
		String schemaDefintion = this.transformJSFilenameToPureEntity(filename) + "Schema = " + functionality +
				".Schema({\n";
		String attributeDefintion = "";
		for (Map.Entry<String, String> entry : attributeTypes.entrySet()) {
			attributeDefintion = attributeDefintion + entry.getKey() + ": {type: " + entry.getValue() + ", required: true},\n";
		}
		// cut off last comma
		attributeDefintion = attributeDefintion.substring(0, attributeDefintion.length()-2) + "\n\n";
		String moduleExport =
				"module.exports = " + functionality + ".model('" + entityName.replace(".java", "") + "'," + this.transformJSFilenameToPureEntity(filename) + "Schema)";
		String code = functionalityImport + schemaDefintion + attributeDefintion + moduleExport;
		System.out.println(code);
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
