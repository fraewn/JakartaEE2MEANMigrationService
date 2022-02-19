package com.migration.service.service.migration;

import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledge;
import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledge;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledgeService;

import com.migration.service.model.migrationKnowledge.entityMigration.EntityModel;
import com.migration.service.model.migrationKnowledge.entityMigration.EntityModelService;
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
	private EntityModelService entityModelService;

	List<EntityModel> entityModels = new ArrayList<>();

	private List<String> entities = new ArrayList<>();


	public CreateMEANArchitectureGraph(ModuleKnowledgeService moduleKnowledgeService,
									   NodeKnowledgeService nodeKnowledgeService,
									   OntologyKnowledgeService ontologyKnowledgeService,
									   EntityModelService entityModelService){
		this.moduleKnowledgeService = moduleKnowledgeService;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.ontologyKnowledgeService = ontologyKnowledgeService;
		this.entityModelService = entityModelService;

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
					// create backend entity processing module
					this.createEntityProcessingMEANModule(moduleKnowledgeInstance);
					// connect to modules used by it
					if(moduleKnowledgeInstance.getUsedModules()!=null) {
						for (String usedModule : moduleKnowledgeInstance.getUsedModules()) {
							for (ModuleKnowledge checkedModuleKnowledgeInstance : moduleKnowledgeList) {
								// find intelligence by module name
								if (usedModule.equals(checkedModuleKnowledgeInstance.getBase())) {
									for(String component : checkedModuleKnowledgeInstance.getModuleCluster()){
										NodeKnowledge nodeKnowledge = nodeKnowledgeService.findByName(component);
										if(nodeKnowledge.getCalculatedInterpretation().contains("Mail Feature")){
											this.addEmailFeatureUsage(moduleKnowledgeInstance, checkedModuleKnowledgeInstance);
											break;
										}
										else if(nodeKnowledge.getCalculatedInterpretation().contains("Authentication Feature")){
											this.addAuthFeatureUsage(moduleKnowledgeInstance, checkedModuleKnowledgeInstance);
											break;
										}
									}
									break;
								}
							}
						}
					}
				}
				if(moduleKnowledgeInstance.getUsage().equals(("Backend Feature"))){
					this.processBackendFeature(moduleKnowledgeInstance);
				}
			}
		}
		this.persistCallsToOtherModules(entityModels);
	}

	public void processBackendFeature(ModuleKnowledge backendFeatureKnowledge){
		String schedulingFeatureJavaEEComponent = "Scheduling Feature";
		String messagingFeatureJavaEEComponent="Messaging Feature";
		String applicationManagementJavaEEComponent="Application Management Feature";
		String emailJavaEEComponent="Mail Feature";
		String authComponent="Authentication Feature";
		for(String backendFeatureComponent : backendFeatureKnowledge.getModuleCluster()) {
			NodeKnowledge featureComponentKnowledge = this.findNodeKnowledgeByClassName(backendFeatureComponent);
			// module represents a scheduling feature
			if(featureComponentKnowledge.getCalculatedInterpretation().contains(schedulingFeatureJavaEEComponent)){
				this.addSchedulingFeatureToBackendModel(backendFeatureKnowledge);
			}
			else if(featureComponentKnowledge.getCalculatedInterpretation().contains(applicationManagementJavaEEComponent)){
				addApplicationManagementFeatureToBackendModel(backendFeatureKnowledge);
			}
			else if(featureComponentKnowledge.getCalculatedInterpretation().contains(messagingFeatureJavaEEComponent)){
				addMessagingFeatureToBackendModel(backendFeatureKnowledge);
			}
			else if(featureComponentKnowledge.getCalculatedInterpretation().contains(emailJavaEEComponent)){
				this.addEmailFeatureToBackendModel(backendFeatureKnowledge);
			}
			else if(featureComponentKnowledge.getCalculatedInterpretation().contains(authComponent)){
				this.addAuthFeatureToBackendModel(backendFeatureKnowledge);
			}
		}
	}

	public void addAuthFeatureToBackendModel(ModuleKnowledge moduleKnowledge){
		String query = "MERGE(n:Middleware {id:'Auth.js', module:'" + moduleKnowledge.getBase() + "', location: 'Backend', package: " +
				"'middleware'})";
		this.runQueryOnMEANGraph(query);
	}

	public void addAuthFeatureUsage(ModuleKnowledge usingModuleKnowledge, ModuleKnowledge authFeatureModuleKnowledge){
		String authComponent="Authentication Feature";
		String mergeQuery = "Match(n:Route) where n.module='" + usingModuleKnowledge.getBase() + "' MATCH(m:"
				+ ontologyKnowledgeService.findByJavaEEComponent(authComponent).getMEANComponent() + ") where m.module='" + authFeatureModuleKnowledge.getBase() +
				"' MERGE " +
				"(n)" +
				"-[:FORWARDS_REQUEST]-(m)";
		String deleteQuery = "Match(n:Route) where n.module='" + usingModuleKnowledge.getBase() + "' MATCH(m:RestController) where m" +
				".module='" + usingModuleKnowledge.getBase() + "' MATCH(n)-[r]-(m) delete r";
		String reMergeQuery =
				"Match(n:Middleware) where n.module='" + authFeatureModuleKnowledge.getBase() + "' MATCH(m:RestController) where m" +
						".module='" + usingModuleKnowledge.getBase() +
				"' MERGE (n)-[:FORWARDS_REQUEST]-(m)";
		System.out.println(mergeQuery);
		this.runQueryOnMEANGraph(mergeQuery);
		this.runQueryOnMEANGraph(deleteQuery);
		this.runQueryOnMEANGraph(reMergeQuery);
	}

	public void addEmailFeatureToBackendModel(ModuleKnowledge mailFeatureModuleKnowledge){
		String emailJavaEEComponent="Mail Feature";
			String query = "MERGE(n:" + ontologyKnowledgeService.findByJavaEEComponent(emailJavaEEComponent).getMEANComponent() + " {id: " +
					"'Mail.js', module:'" + mailFeatureModuleKnowledge.getBase() + "', location: 'Backend', package: 'mail'})";
			this.runQueryOnMEANGraph(query);
	}

	public void addEmailFeatureUsage(ModuleKnowledge usingModuleKnowledge, ModuleKnowledge emailFeatureModuleKnowledge){
		String emailJavaEEComponent="Mail Feature";
		String mergeQuery = "Match(n:RestController) where n.module='" + usingModuleKnowledge.getBase() + "' MATCH(m:"
				+ ontologyKnowledgeService.findByJavaEEComponent(emailJavaEEComponent).getMEANComponent() + ") where m.module='" + emailFeatureModuleKnowledge.getBase() +
				"' MERGE " +
				"(n)" +
				"-[:USES]-(m)";
		System.out.println(mergeQuery);
		this.runQueryOnMEANGraph(mergeQuery);
	}

	public void addApplicationManagementFeatureToBackendModel(ModuleKnowledge moduleKnowledge){
		// external module, so no connections to other modules given
		String applicationManagementJavaEEComponent="Application Management Feature";
		runQueryOnMEANGraph("MERGE(n:" + ontologyKnowledgeService.findByJavaEEComponent(applicationManagementJavaEEComponent).getMEANComponent() + " {id: '" +
				ontologyKnowledgeService.findByJavaEEComponent(applicationManagementJavaEEComponent).getDefaultLibrary() + "', module:'"
				+ moduleKnowledge.getBase() + "', location: 'Backend', package: ''})");
		connectNodesInBackend(moduleKnowledge.getBase());
	}

	public void addMessagingFeatureToBackendModel(ModuleKnowledge moduleKnowledge){
		// kafka
		String messagingFeatureJavaEEComponent="Messaging Feature";
		String query = "MERGE(n:" + ontologyKnowledgeService.findByJavaEEComponent(messagingFeatureJavaEEComponent).getMEANComponent() + " {id: " +
				"'Messenger.js', module: '" + moduleKnowledge.getBase() + "', location: 'Backend', package: 'messaging'})";
		String functionalityQuery = "MERGE(n:Functionality {name: '" +
				ontologyKnowledgeService.findByJavaEEComponent(messagingFeatureJavaEEComponent).getDefaultLibrary() + "', module: '"
				+ moduleKnowledge.getBase() + "', location: 'Backend', package: 'messaging'})";
		runQueryOnMEANGraph(query);
		runQueryOnMEANGraph(functionalityQuery);
		for(String component : moduleKnowledge.getModuleCluster()){
			NodeKnowledge nodeKnowledge = nodeKnowledgeService.findByName(component);
			if(nodeKnowledge.getCalculatedInterpretation().contains("Service")){
				String messagingEndpointQuery =
						"MERGE(n:" + ontologyKnowledgeService.findByJavaEEComponent("Service").getMEANComponent() + " {id: '" +
						 renameToJsComponent(nodeKnowledge.getName()) + "', module: '" + moduleKnowledge.getBase() + "', " +
								"location: 'Backend', package: 'messaging'})";

				runQueryOnMEANGraph(messagingEndpointQuery);
				connectNodesInBackend(moduleKnowledge.getBase());
				break;
			}
		}
	}

	public void addSchedulingFeatureToBackendModel(ModuleKnowledge moduleKnowledge){
		String schedulingFeatureJavaEEComponent = "Scheduling Feature";

		String createSchedulingFeatureQuery = "";
		String createSchedulerToControllerRelationQuery = " ";
		String createSchedulingFunctionalityQuery = "";
		// for each component in the module
		for(String component : moduleKnowledge.getModuleCluster()) {
			// get further knowledge
			NodeKnowledge componentKnowledge = this.findNodeKnowledgeByClassName(component);

			// query to add the scheduling feature to the mean stack architecture model
			createSchedulingFeatureQuery = "MERGE(schedulingFeature:" + ontologyKnowledgeService.findByJavaEEComponent(schedulingFeatureJavaEEComponent).getMEANComponent()
					+ " {id: '" + this.renameToJsComponent(componentKnowledge.getName()) + "', module:'" + moduleKnowledge.getBase() +
					"', location: 'Backend', package: 'scheduling'})";
			createSchedulingFunctionalityQuery =
					"MATCH(n:" + ontologyKnowledgeService.findByJavaEEComponent(schedulingFeatureJavaEEComponent).getMEANComponent() +
							") where n.module='" + moduleKnowledge.getBase() + "' MERGE(f:Functionality {name: '" + ontologyKnowledgeService.findByJavaEEComponent(schedulingFeatureJavaEEComponent).getDefaultLibrary()
							+ "', module:'" + moduleKnowledge.getBase() + "', location: 'Backend', package: 'scheduling'}) MERGE (n)-[:IMPORTS]-(f)";
			this.runQueryOnMEANGraph(createSchedulingFeatureQuery);
			this.runQueryOnMEANGraph(createSchedulingFunctionalityQuery);
			// now find out which features are scheduled
			for (String usedModule : moduleKnowledge.getUsedModules()) {
				// find the module of the scheduled feature
				ModuleKnowledge scheduledFeatureModuleKnowledge = moduleKnowledgeService.findModuleKnowledgeByBase(usedModule);
				// for each component in the scheduled feature
				for (String moduleComponent : moduleKnowledge.getModuleCluster()) {
					NodeKnowledge scheduledFeatureComponentKnowledge = this.findNodeKnowledgeByClassName(moduleComponent);
					// find all modules that are used by a scheduled module
					// e.g. a scheduled batch feature:
					if (scheduledFeatureComponentKnowledge.getCalculatedInterpretation().contains("Batch Feature")) {
						// a batch feature is not visible as its own class in the mean stack architecture model
						// instead it gets translated as a controller method (e.g. batch write xy)
						// therefore, find all entity processing modules that the batch feature module uses
						List<String> usedModules = scheduledFeatureModuleKnowledge.getUsedModules();
						for (String moduleCalledByBatchFeature : usedModules) {
							// add connection from the scheduler to the controller class, so it's visible in the modell
							// that there are scheduled controller operations
							createSchedulerToControllerRelationQuery =
									"Match(n:" + ontologyKnowledgeService.findByJavaEEComponent(schedulingFeatureJavaEEComponent).getMEANComponent()
											+ " {id:'" + this.renameToJsComponent(componentKnowledge.getName()) + "'}) Match(m:RestController {module:'" + moduleCalledByBatchFeature + "'}) MERGE (n)" +
											"-[:CALLS]-(m)";
							System.out.println(createSchedulerToControllerRelationQuery);
							this.runQueryOnMEANGraph(createSchedulerToControllerRelationQuery);
						}
						break;
					}
				}
			}
		}


	}

	public void setBatchAttributeInController(){
		// write to mongo
	}

	public void connectFeatureToEntityProcessingModule(String featureId, String entityProcessingModuleId){

	}

	public String getControllerClassForBackendEntityProcessingModule(String usedModule){
		String query = "Match(n:RestController {module:'" + usedModule + "'}) return n";
		List<String> results = new ArrayList<>();
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			session.run(query).list(result -> results.add(result.get("n").asNode().get("id").asString()));
		}
		catch(Exception e){
			System.out.println("did not work");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return results.get(0);
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


		String soapAPIJavaEEComponent="SOAP API";
		String wsdlEndpointJavaEEComponent="WSDL Endpoint";
		for(String component : moduleKnowledge.getModuleCluster()){
			NodeKnowledge nodeKnowledge = this.findNodeKnowledgeByClassName(component);
			moduleName = renameToJsComponent(component);
			// model
			// (additional) soap route
			if(nodeKnowledge.getCalculatedInterpretation().contains(soapAPIJavaEEComponent)){
				String associatedMEANComponent =
						ontologyKnowledgeService.findByJavaEEComponent(soapAPIJavaEEComponent).getMEANComponent();
				query =
						query + " MERGE (s" + soapApiCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
								"'soap/routes'})";
				if(!(soapApiCount>0)) {
					query =
							query + " MERGE (t:Functionality" + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
									"', location: '" + meanLocation + "', package: 'soap/route', name: '" + ontologyKnowledgeService.findByJavaEEComponent(soapAPIJavaEEComponent).getDefaultLibrary() + "'})";
				}
				soapApiCount++;
			}
			// soap controller
			else if(nodeKnowledge.getCalculatedInterpretation().contains(wsdlEndpointJavaEEComponent)){
				String associatedMEANComponent =
						ontologyKnowledgeService.findByJavaEEComponent(wsdlEndpointJavaEEComponent).getMEANComponent();
				query =
						query + " MERGE (w:" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
								"'soap/controller'})";
			}
			else {
				for (String interpretation : nodeKnowledge.getCalculatedInterpretation()) {

					if (interpretation.equals("Entity Implementation")) {
						String associatedMEANComponent =
								ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
						query =
								query + " MERGE (n" + entityCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
										"'model'})";

						if (!(entityCount > 0)) {
							query =
									query + " MERGE (f:Functionality {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
											"', location: '" + meanLocation + "', package: 'routes', name: 'mongoose'})";
						}
						entityCount++;
						// persist entity, attributes and relation to other entities
						EntityModel entityModel = this.createBackendModel(component);
						entityModels.add(entityModel);
						this.persistEntityModel(entityModel);
					}
					// controller
					else if (interpretation.equals("Data Access Object")) {
						String associatedMEANComponent =
								ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
						query =
								query + " MERGE (m" + modelCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
										"'controller'})";
						modelCount++;
					}
					// route
					else if (interpretation.equals("Service")) {
						String associatedMEANComponent =
								ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
						query =
								query + " MERGE (k" + routeCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
										"'routes'})";

						if (!(routeCount > 0)) {
							query =
									query + " MERGE (g:Functionality" + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
											"', location: '" + meanLocation + "', package: 'routes', name: '" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getDefaultLibrary() + "'})";
						}
						routeCount++;
					}
				}
			}
		}
		// persist raw architecture as graph
		this.runQueryOnMEANGraph(query);
		// connect nodes
		connectNodesInBackend(moduleKnowledge.getBase());

	}

	public void persistCallsToOtherModules(List<EntityModel> entityModels){
		for(EntityModel entityModel : entityModels) {
			System.out.println(entityModel.getName());
			for (String attribute : entityModel.getAttributes()) {

				if(entityModel.getAttributeIsRelatedOtherEntity().get(attribute)==true){
					// does not work with User in reports (attribute like) because type is Set<User>  Match(n:Model {id:'Report.js'}) Match
					// (m:Model {id:'Set<User>.js'}) MERGE (n)-[r:ManyToMany]-(m) SET r.name='liker'
					System.out.println("Match(n:Model {id:'" + this.renameToJsComponent(entityModel.getName()) + "'}) Match(m:Model {id:'" + entityModel.getAttributeTypes().get(attribute) +
							".js'}) MERGE (n)-[r:" + entityModel.getRelationTypes().get(attribute) + "]-(m) SET r.name='" + attribute +
							"'");
					this.runQueryOnMEANGraph("Match(n:Model {id:'" + this.renameToJsComponent(entityModel.getName()) + "'}) Match(m:Model {id:'" + entityModel.getAttributeTypes().get(attribute) +
							".js'}) MERGE (n)-[r:" + entityModel.getRelationTypes().get(attribute) + "]-(m) SET r.name='" + attribute +
							"'");
				}
			}
		}
	}


	// probably not used since attributes are persisted in mongo
	public void addAttributesToModels(EntityModel entityModel){
		String attributeList = "";
		for(String attribute : entityModel.getAttributes()){
			String type = entityModel.getAttributeTypes().get(attribute);
			attributeList = attributeList + attribute + ":" + type + ", ";
		}
		String query = "Match(n: {name: '" + entityModel.getName() + "'}) SET n.attributes = " + attributeList;
	}

	public String renameToJsComponent(String className){
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
		this.entityModelService.insertOne(entityModel);
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
		HashMap<String, String> attributeTypes = new HashMap<>();
		HashMap<String, String> relationTypes = new HashMap<>();
		EntityModel entityModel = new EntityModel();
		try {
			System.out.println(this.javaEEGraphSession.run(query).next());
			for(Record r : this.javaEEGraphSession.run(query).list()){
				//System.out.println(r.asMap());
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(r.asMap().get("fields").toString());
				//System.out.println(json.get("names").toString());
				String attribute = json.get("names").toString().replace("[", "").replace("]", "").replaceAll("\"", "");
				attributes.add(attribute);
				attributeTypes.put(attribute, json.get("type").toString());
				if(json.get("annotations").toString().contains("ManyToOne")){
					entityAssociation.put(attribute, true);
					relationTypes.put(attribute, "ManyToOne");
				}
				else if(json.get("annotations").toString().contains("ManyToMany")){
					entityAssociation.put(attribute, true);
					relationTypes.put(attribute, "ManyToMany");
				}
				else {
					entityAssociation.put(attribute, false);
				}
			}
			entityModel.setName(name);
			entityModel.setAttributes(attributes);
			entityModel.setAttributeTypes(attributeTypes);
			entityModel.setAttributeIsRelatedOtherEntity(entityAssociation);
			entityModel.setRelationTypes(relationTypes);

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
		String query = "MERGE (n:" + component + " {id:'" + moduleId + "', location:'Backend'})";
		this.runQueryOnMEANGraph(query);
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

	public boolean checkIfComponentForModuleExists(String component, String moduleId) {
		String query = "Match (n:" + component + ") where n.id='" + moduleId + "' return n";
		Driver driver = setUpNeo4jDriver("MEAN");
		try (Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("n").asNode()).size() > 0;
		}
	}

	public void connectNodesInBackend(String module){
		List<String> queries = new ArrayList<>();
		queries.add("MATCH (n:App) MATCH (m:Route) where m.module='" + module + "' MERGE (n)" +
				"-[:INCLUDES]->(m)");
		queries.add("MATCH (n:App) MATCH (m:SoapRoute) where m.module='" + module + "' MERGE (n)" +
				"-[:INCLUDES]->(m)");
		queries.add("MATCH (n:RestController) where n.module='" + module + "' MATCH (m:Model) where m.module='" + module  + "' MERGE " +
				"(n)-[:USES]->(m)");
		queries.add("MATCH (n:SoapController) where n.module='" + module + "' MATCH (m:Model) where m.module='" + module  + "' MERGE " +
				"(n)-[:USES]->(m)");
		queries.add("MATCH (n:Model) where n.module='" + module + "' MATCH (m:Functionality) where m.module='" + module  + "' AND m" +
				".name='mongoose' MERGE(n)-[:IMPORTS]->(m)");
		queries.add("MATCH (n:Route) where n.module='" + module + "' MATCH (m:RestController) where m.module='" + module  + "' MERGE " +
				"(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:SoapRoute) where n.module='" + module + "' MATCH (m:SoapController) where m.module='" + module + "' MERGE " +
				"(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:Route) where n.module='" + module + "' MATCH (m:Functionality) where m.module='" + module  + "' "
				+ "AND m.name='express.js' MERGE(n)-[:IMPORTS]->(m)");
		queries.add("MATCH (n:Route) where n.module='" + module + "' MATCH (m:Middleware) where m.module='" + module  + "' MERGE " +
				"(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:Route) where n.module='" + module + "' MATCH (m:Class) where m.module='" + module  + "' MERGE " +
				"(n)-[:USES]->(m)");
		queries.add("MATCH (n:SoapRoute) where n.module='" + module + "' MATCH (m:Functionality) where m.module='" + module
				+ "' AND m.name='soap' MERGE(n)-[:IMPORTS]->(m)");
		queries.add("MATCH (n:Middleware) where n.module='" + module + "' MATCH (m:RestController) where m.module='" + module  +
				"' MERGE(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:Middleware) where n.module='" + module + "' MATCH (m:SoapController) where m.module='" + module  +
				"' MERGE(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:RestController) where n.module='" + module + "' MATCH (m:Class) where m.module='" + module  +
				"' MERGE(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:SoapController) where n.module='" + module + "' MATCH (m:Class) where m.module='" + module  +
				"' MERGE(n)-[:FORWARDS_REQUEST]->(m)");
		queries.add("MATCH (n:Class) where n.module='" + module + "' MATCH (m:Functionality) where m.module='" + module  +
				"' MERGE(n)-[:USES]->(m)");
		queries.add("MATCH (n:External) where n.location='Backend' MATCH (m:App) where m.location='Backend' MERGE(n)-[:ASSOCIATED_WITH]->" +
				"(m)");
		//... execute
		for(String query : queries) {
			this.runQueryOnMEANGraph(query);
		}
	}

	public void connectControllerCallsToOtherEntities(){

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
