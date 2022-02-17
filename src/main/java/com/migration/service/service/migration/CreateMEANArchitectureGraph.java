package com.migration.service.service.migration;

import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledge;
import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledge;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledgeService;

import com.migration.service.model.migrationKnowledge.entityMigration.EntityModel;
import com.migration.service.service.util.EnvironmentUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.neo4j.driver.*;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class CreateMEANArchitectureGraph {
	private Driver javaEEGraphDriver;
	private Session javaEEGraphSession;

	private Driver MEANGraphDriver;
	private Session MEANGraphSession;

	private ModuleKnowledgeService moduleKnowledgeService;
	private NodeKnowledgeService nodeKnowledgeService;
	private OntologyKnowledgeService ontologyKnowledgeService;

	private List<String> entities = new ArrayList<>();


	public CreateMEANArchitectureGraph(ModuleKnowledgeService moduleKnowledgeService,
									   NodeKnowledgeService nodeKnowledgeService,
									   OntologyKnowledgeService ontologyKnowledgeService){
		this.moduleKnowledgeService = moduleKnowledgeService;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.ontologyKnowledgeService = ontologyKnowledgeService;

		this.entities = this.setUpEntities();
		javaEEGraphDriver = this.setUpNeo4jDriver("JavaEE");
		javaEEGraphSession = this.setUpNeo4jSession(this.javaEEGraphDriver);
		MEANGraphDriver = this.setUpNeo4jDriver("MEAN");
		MEANGraphSession = this.setUpNeo4jSession(this.MEANGraphDriver);
	}

	public List<String> setUpEntities(){
		for(NodeKnowledge nodeKnowledge : nodeKnowledgeService.findAll()){
			if(nodeKnowledge.containsLabel("Entity")){
				entities.add(nodeKnowledge.getName());
			}
		}
		return entities;
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

	public void createArchitecture(){
		boolean componentFound = false;
		this.addMissingComponentToModule("App", "BACKEND_APP");
		List<ModuleKnowledge> moduleKnowledgeList = moduleKnowledgeService.findAllFinalModules();
		for (ModuleKnowledge moduleKnowledgeInstance : moduleKnowledgeList) {
			// for each component in a moduleCluster
			if(moduleKnowledgeInstance.getUsage()!=null){
				if(moduleKnowledgeInstance.getUsage().equals("Backend Entity Processing")){
					this.createEntityProcessingMEANModule(moduleKnowledgeInstance);
				}
				if(moduleKnowledgeInstance.getUsage().equals(("Backend Feature"))){

				}
			}

			/*for (String component : moduleKnowledgeInstance.getModuleCluster()) {
				// find the node knowledge that corresponds to it
				for (NodeKnowledge nodeKnowledge : nodeKnowledgeService.findAll()) {
					if(componentFound==true){
						componentFound = false;
						break;
					}
					if (component.equals(nodeKnowledge.getName())) {
						componentFound = true;
						// find the interpretation/javaEEcomponent that this node is associated with
						for (String interpretation : nodeKnowledge.getCalculatedInterpretation()) {
							try {
								// look up what mean architecture component is associated with that javaEE component
								OntologyKnowledge ontologyKnowledge = ontologyKnowledgeService.findByJavaEEComponent(interpretation);
								String associatedMEANComponent = ontologyKnowledge.getMEANComponent();
								if (ontologyKnowledge.getMEANLocation().equals("Backend")) {
									createMEANComponentNode(associatedMEANComponent, moduleKnowledgeInstance.getBase(), component,
											ontologyKnowledge.getMEANLocation());
								}
								// create graph node for that
							}
							catch(NullPointerException nullpointer){
								System.out.println(nullpointer.getMessage());
							}
						}

					}
				}
			}*/
			/*String base = moduleKnowledgeInstance.getBase();
			if(checkIfComponentForModuleExists("Controller", base)){
				if(!checkIfComponentForModuleExists("Route", base)) this.addMissingComponentToModule("Route", base);
			}*/

		}
	}

	public void processBackendFeature(ModuleKnowledge moduleKnowledge){
		for(String component : moduleKnowledge.getModuleCluster()) {
			NodeKnowledge nodeKnowledge = this.findNodeKnowledgeByClassName(component);
			for(String interpretation : nodeKnowledge.getCalculatedInterpretation()){

			}
		}
	}

	public void createEntityProcessingMEANModule(ModuleKnowledge moduleKnowledge){
		ontologyKnowledgeService.setUp();
		String moduleName = "";
		String query = "";
		String meanLocation = "Backend";
		int entityCount = 0;
		int modelCount =0;
		int routeCount=0;
		int soapApiCount = 0;
		for(String component : moduleKnowledge.getModuleCluster()){
			NodeKnowledge nodeKnowledge = this.findNodeKnowledgeByClassName(component);
			for(String interpretation : nodeKnowledge.getCalculatedInterpretation()){
				if(interpretation.equals("Entity Implementation")){
					moduleName = getEntityProcessingModuleName(component);
					System.out.println(moduleName);
					String associatedMEANComponent =
							ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
					query =
							query + " MERGE (n" + entityCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
							"'controller'})";

					if(!(entityCount>0)) {
						query =
								query + " MERGE (f:Functionality {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
								"', location: '" + meanLocation + "', package: 'routes', name: 'mongoose'})";
					}
					entityCount++;
					// persist entity, attributes and relation to other entities
					this.persistEntityModel(this.createBackendModel(component));
				}
				if(interpretation.equals("Data Access Object")){
					String associatedMEANComponent =
							ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
					query =
							query + " MERGE (m" + modelCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
							"'model'})";
					modelCount++;
				}
				if(interpretation.equals("Service")){
					String associatedMEANComponent =
							ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
					query =
							query + " MERGE (k" + routeCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
							"'routes'})";

					if(!(routeCount>0)) {
						query =
								query + " MERGE (g:Functionality" + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
								"', location: '" + meanLocation + "', package: 'routes', name: '" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getDefaultLibrary() + "'})";
					}
					routeCount++;
				}
				if(interpretation.equals("SOAP API")){
					String associatedMEANComponent =
							ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
					query =
							query + " MERGE (s" + soapApiCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
							"'soap'})";
					if(!(soapApiCount>0)) {
						query =
								query + " MERGE (t:Functionality" + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
										"', location: '" + meanLocation + "', package: 'routes', name: '" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getDefaultLibrary() + "'})";
					}
					soapApiCount++;
				}
				if(interpretation.equals("WSDL Endpoint")){
					String associatedMEANComponent =
							ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
					query =
							query + " MERGE (w:" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
							"'soap'})";
					query =
							query + " MERGE (z:Functionality" + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
									"', location: '" + meanLocation + "', package: 'routes', name: '" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getDefaultLibrary() + "'})";
				}
			}
		}


		// persist raw architecture as graph
		this.runQueryOnMEANGraph(query);
		// connect nodes
		connectNodesInBackend(moduleKnowledge.getBase());
	}

	public void persistCallsToOtherModules(){

	}

	public String getEntityProcessingModuleName(String className){
		return className.replace(".java", "") + ".js";
	}

	public void runQueryOnJavaEEGraph(String query){
		try(Session session = this.javaEEGraphSession) {
			session.run(query);
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public void runQueryOnMEANGraph(String query){
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			session.run(query);
		}
		catch(Exception e){
			System.out.println("did not work");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public void persistEntityModel(EntityModel entityModel){
		// write to mongo
	}

	public NodeKnowledge findNodeKnowledgeByClassName(String component){
		for (NodeKnowledge nodeKnowledge : nodeKnowledgeService.findAll()) {
			if (component.equals(nodeKnowledge.getName())) {
				// find the interpretation/javaEEcomponent that this node is associated with
				return nodeKnowledge;
			}
		}
		return null;
	}

	public EntityModel createBackendModel(String name){
		// get java entity class from JavaEE Graph Model
		String query = "Match(n {name:'" + name + "'}) unwind n.fields as fields return fields";
		List<String> attributes = new ArrayList<>();
		HashMap<String, Boolean> entityAssociation = new HashMap<>();
		EntityModel entityModel = new EntityModel();
		System.out.println(query);
		try {
			System.out.println(this.javaEEGraphSession.run(query).next());
			for(Record r : this.javaEEGraphSession.run(query).list()){
				//System.out.println(r.asMap());
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(r.asMap().get("fields").toString());
				//System.out.println(json);
				//System.out.println(json.get("names").toString());
				attributes.add(json.get("names").toString().replace("[", "").replace("]", "").replaceAll("\"", ""));
				if(json.get("annotations").toString().contains("Many")){
					entityAssociation.put(json.get("names").toString().replace("[", "").replace("]", "").replaceAll("\"", ""), true);
				}
				else {
					entityAssociation.put(json.get("names").toString().replace("[", "").replace("]", "").replaceAll("\"", ""), false);
				}
			}
			entityModel.setName(name);
			entityModel.setAttributes(attributes);
			entityModel.setAttributeIsRelatedOtherEntity(entityAssociation);
			//System.out.println(entityAssociation);

			//List<Node> nodeList = this.javaEEGraphSession.run(query).list(result -> result.get("output").asNode());
			//System.out.println(nodeList.get(0).get("fields"));
			//System.out.println(nodeList.get(0).get("fields").get(0).get("names"));
			//nodeList.get(0).get("fields").asList(item -> item.get("names").asList(attributeName -> attributes.add(attributeName
			// .toString())));
			//System.out.println(attributes);
			//return session.run(query).list(result -> result.get("n").asNode());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return entityModel;
	}

	public void addMissingComponentToModule(String component, String moduleId){
		String query = "MERGE (n:" + component + " {id:'" + moduleId + "'})";
		this.runQueryOnMEANGraph(query);
		/*try(Session session = javaEEGraphDriver.session()) {
			session.run(query);
		}
		catch(Exception e){
			System.out.println("Exception during adding an additional component node " + component);
			e.printStackTrace();
			System.out.println(e.getMessage());
		}*/
	}

	// Route, Modell and Controller naming conventions represent the entity that is processed by them
	public String shortenClassNameToEntityBasedBackendClassName(String className){
		for(String entity : entities){
			if(className.contains(entity)){
				return entity + ".js";
			}
		}
		// class name is not entity based
		return className;
	}

	public boolean checkIfComponentForModuleExists(String component, String moduleId){
		String query = "Match (n:" + component + ") where n.id='" + moduleId + "' return n";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("n").asNode()).size() > 0;
		}
	}



	public void connectNodesInBackend(String module){
		List<String> queries = new ArrayList<>();
		queries.add("MATCH (n:App) MATCH (m:Route) where m.module='" + module + "' MERGE (n)" +
				"-[:DEFINES]->(m)");
		queries.add("MATCH (n:App) MATCH (m:SoapApi) where m.module='" + module + "' MERGE (n)" +
				"-[:DEFINES]->(m)");
		//queries.add("MATCH (n:App) where n.id='" + id + "' MATCH (m:Functionality) where m.id='" + id  + "' " +
		//"MERGE (n)-[:USES]->(m)");
		queries.add("MATCH (n:Controller) where n.module='" + module + "' MATCH (m:Model) where m.module='" + module  + "' MERGE " +
				"(n)-[:USES]->(m)");
		queries.add("MATCH (n:Route) where n.module='" + module + "' MATCH (m:Controller) where m.module='" + module  + "' MERGE " +
				"(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:Route) where n.module='" + module + "' MATCH (m:Functionality) where m.module='" + module  + "' "
				+ "AND m.name='express.js' MERGE(n)-[:IMPORTS]->(m)");
		queries.add("MATCH (n:Route) where n.module='" + module + "' MATCH (m:Middleware) where m.module='" + module  + "' MERGE " +
				"(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:Controller) where n.module='" + module + "' MATCH (m:Functionality) where m.module='" + module
				+ "' AND m.name='mongoose' MERGE(n)-[:IMPORTS]->(m)");
		queries.add("MATCH (n:Middleware) where n.module='" + module + "' MATCH (m:Controller) where m.module='" + module  +
				"' MERGE(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:Controller) where n.module='" + module + "' MATCH (m:Class) where m.module='" + module  +
				"' MERGE(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:SoapApi) where n.module='" + module + "' MATCH (m:Controller) where m.module='" + module  +
				"' MERGE(n)-[:FORWARDS_REQUEST]->(m)");
		//... execute
		for(String query : queries) {
			this.runQueryOnMEANGraph(query);
		}
	}



	// too generic
	public void createMEANComponentNode(String meanComponent, String moduleBase, String className, String meanLocation){
		// take name until second capital letter
		// ReportServiceImpl -> Report und dann setzt da die Mean component endung dran
		String query = "MERGE (n:" + meanComponent + " {name: '" + this.shortenClassNameToEntityBasedBackendClassName(className)
				+ "', id:'" + moduleBase + "', location: '" + meanLocation + "'})";
		System.out.println(query);
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			session.run(query);
		}
		catch(Exception e){
			System.out.println("Exception during backend node creation for: " + className);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}



}
