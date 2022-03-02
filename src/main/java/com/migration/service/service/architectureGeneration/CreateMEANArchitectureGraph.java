package com.migration.service.service.architectureGeneration;

import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledge;
import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
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
import java.util.Map;

@Component
public class CreateMEANArchitectureGraph {
	private Driver javaEEGraphDriver;
	private Session javaEEGraphSession;

	private Driver MEANGraphDriver;
	private Session MEANGraphSession;
	List<ModuleKnowledge> frontendEntityProcessingModuleKnowledge = new ArrayList<>();
	List<ModuleKnowledge> frontendFeatureModuleKnowledge = new ArrayList<>();
	List<ModuleKnowledge> backendFeatureModuleKnowledge = new ArrayList<>();
	HashMap<ModuleKnowledge,List<ModuleKnowledge>> backendFeatureAssociations = new HashMap<>();
	HashMap<String, List<String>> backendEntityProcessingModuleAssociations = new HashMap<>();
	private ModuleKnowledgeService moduleKnowledgeService;
	private NodeKnowledgeService nodeKnowledgeService;
	private OntologyKnowledgeService ontologyKnowledgeService;
	private EntityModelService entityModelService;

	HashMap<ModuleKnowledge, EntityModel> entitiesComponentsDic = new HashMap<>();


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
		this.addMissingComponentToModule("App", "BACKEND_APP", "Backend");
		this.addMissingComponentToModule("App", "FRONTEND_APP", "Frontend");
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
										if(nodeKnowledge.getCalculatedInterpretation().contains("Mail Feature") || nodeKnowledge.getCalculatedInterpretation().contains("Authentication Feature")){
											if(this.backendFeatureAssociations.get(moduleKnowledgeInstance)==null) {
												List<ModuleKnowledge> list = new ArrayList<>();
												list.add(checkedModuleKnowledgeInstance);
												this.backendFeatureAssociations.put(moduleKnowledgeInstance, list);
											}
											else {
												List<ModuleKnowledge> list = this.backendFeatureAssociations.get(moduleKnowledgeInstance);
												list.add(checkedModuleKnowledgeInstance);
												this.backendFeatureAssociations.put(moduleKnowledgeInstance, list);
											}
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
					this.backendFeatureModuleKnowledge.add(moduleKnowledgeInstance);
				}
				if(moduleKnowledgeInstance.getUsage().equals("Frontend Entity Processing")){
					this.frontendEntityProcessingModuleKnowledge.add(moduleKnowledgeInstance);
				}
				if(moduleKnowledgeInstance.getUsage().equals("Frontend Feature")){
					this.frontendFeatureModuleKnowledge.add(moduleKnowledgeInstance);

				}
			}
		}


		this.persistCallsToOtherModules(entityModels);
		System.out.println("++++++++ created backend entity processing architecture ++++++++");
		this.processBackendFeature();
		System.out.println("++++++++ created backend features architecture ++++++++");
		this.processBackendFeatureAssocations();
		this.processBackendEntityProcessingAssociations();
		this.processFrontendEntityProcessingFeature();
		System.out.println("++++++++ created frontend entity processing architecture ++++++++");
		this.processFrontendFeature();
		System.out.println("++++++++ created frontend feature architecture ++++++++");
		this.createStandardAngularModel();
		this.entityModelService.insertAll(entityModels);
		System.out.println("Architecture creation done");
	}

	public void processBackendEntityProcessingAssociations(){
		for (Map.Entry<String, List<String>> entry : this.backendEntityProcessingModuleAssociations.entrySet()) {
			for(String connectedController : entry.getValue()){
				String restControllerId = this.findRestControllerByBaseInGraph(connectedController).get(0);
				String connectControllerQuery = "Match(n:RestController) where n.id='" + entry.getKey() + "' Match" +
						"(m:RestController) where m.id='"+ restControllerId + "' MERGE(n)-[:USES]-(m)";
				this.runQueryOnMEANGraph(connectControllerQuery);
			}
		}
	}

	public void processBackendFeatureAssocations(){
		for (Map.Entry<ModuleKnowledge, List<ModuleKnowledge>> entry : this.backendFeatureAssociations.entrySet()) {
			List<ModuleKnowledge> featureModuleKnowledgeList = entry.getValue();
			for(ModuleKnowledge featureModuleKnowledge : featureModuleKnowledgeList) {
				for (String component : featureModuleKnowledge.getModuleCluster()) {
					NodeKnowledge nodeKnowledge = nodeKnowledgeService.findByName(component);
					if (nodeKnowledge.getCalculatedInterpretation().contains("Mail Feature")) {
						this.addEmailFeatureUsage(entry.getKey(), featureModuleKnowledge);
					} else if (nodeKnowledge.getCalculatedInterpretation().contains("Authentication Feature")) {
						this.addAuthFeatureUsage(entry.getKey(), featureModuleKnowledge);
					}
				}
			}
		}
	}

	public void processBackendFeature(){
		for(ModuleKnowledge moduleKnowledge : this.backendFeatureModuleKnowledge){
			this.processBackendFeature(moduleKnowledge);
		}
	}

	public void processFrontendFeature(){
		for(ModuleKnowledge moduleKnowledge : this.frontendFeatureModuleKnowledge){
			List<String> componentNames = new ArrayList<>();
			for (String component : moduleKnowledge.getModuleCluster()) {
				String moduleName = moduleKnowledge.getBase();
				String packageName = moduleKnowledge.getBase().replaceAll(" ", "");
				String componentName = this.removeCommonJavaEENamesFromClassName(component);
				NodeKnowledge nodeKnowledge = this.findNodeKnowledgeByClassName(component);

				for (String interpretation : nodeKnowledge.getCalculatedInterpretation()) {
					// e.g. LoginBean.java
					if (interpretation.equals("View Controller")) {
						// component
						// automatic: + html + css
						// automatic: oninit ondestroy
						String TsComponentName = componentName + ".component.ts";
						componentNames.add(TsComponentName);
						String HtmlComponentName = componentName + ".component.html";
						String CssComponentName = componentName + ".component.css";
						String tsQuery =
								"MERGE(n:" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent()
										+ " {id:'" + TsComponentName + "', component: '" + componentName + "', module: '" + moduleName +
										"', package:'" + packageName + "/component', location:'Frontend'})";
						String htmlQuery =
								"MERGE(n:" + "HTML" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent()
										+ " {id:'" + HtmlComponentName + "', component: '" + componentName + "', module: '" + moduleName + "', package:'" + packageName +
										"/component', location:'Frontend'})";
						String cssQuery =
								"MERGE(n:" + "CSS" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent()
										+ " {id:'" + CssComponentName + "',  component: '" + componentName + "', module: '" + moduleName + "', package:'" + packageName +
										"/component', location:'Frontend'})";
						this.runQueryOnMEANGraph(tsQuery);
						this.runQueryOnMEANGraph(htmlQuery);
						this.runQueryOnMEANGraph(cssQuery);
						this.connectFrontendNodesByComponent(componentName);
						if(moduleKnowledge.getUsedModules()!=null) {
							for (String usedModule : moduleKnowledge.getUsedModules()) {
								for(String compId : componentNames) {
									String query = "Match(n:Module) where n.id='" + this.findModuleByBaseInGraph(usedModule).get(0) + "' " +
											"Match(m:Component) where m.id='" + compId + "' Merge(m)" +
											"-[:LINKS_TO_COMPONENTS_WITHIN]-(n)";
									this.runQueryOnMEANGraph(query);
								}
							}
						}
					}
					// TODO check this
					if(interpretation.equals("Messaging Feature")){
						String serviceName = componentName + ".service.ts";
						String messageServiceQuery =
								"MERGE(n:Service {id:'" + serviceName + "', module:'" + moduleName + "', package: '" + packageName + "', " +
										"location:'Frontend', base:'" + moduleKnowledge.getBase() + "'})";
						this.runQueryOnMEANGraph(messageServiceQuery);
						if(moduleKnowledge.getUsedModules()!=null) {
							for (String usedModule : moduleKnowledge.getUsedModules()) {
								for(String compId : this.findComponentByBaseInGraph(usedModule)) {
									String query = "Match(n:Component) where n.id='" + compId +
											"' Match(m:Service) where m.id='" + serviceName + "' Merge(n)" +
											"-[:USES]-(m)";
									this.runQueryOnMEANGraph(query);
								}
							}
						}
					}
				}
			}


		}
	}

	public void createStandardAngularModel(){
		// component
		String tsQuery =
				"MERGE(n:Component {id:'app.component.ts', component: 'app', module: 'app', package:'/', location:'Frontend'})";
		String htmlQuery =
				"MERGE(n:HTMLComponent {id:'app.component.html', component: 'app', module: 'app', package:'/', location:'Frontend'})";
		String cssQuery =
				"MERGE(n:CSSComponent {id:'app.component.css',  component: 'app', module: 'app', package:'/', location:'Frontend'})";

		this.runQueryOnMEANGraph(tsQuery);
		this.runQueryOnMEANGraph(htmlQuery);
		this.runQueryOnMEANGraph(cssQuery);
		this.connectFrontendNodesByComponent("app");

		// module
		String moduleQuery = "MERGE(n:Module {id:'app.module.ts', module: 'app', package:'/', " +
				"location:'Frontend'})";
		this.runQueryOnMEANGraph(moduleQuery);
		// app module imports all other modules
		String mergeAllModuleWithoutDeclaration = "Match(n:Module) where n.id<>'app.module.ts' match(m:Module) where m.id='app.module.ts'" +
				" MERGE(m)" +
				"-[:IMPORTS]-(n)";
		this.runQueryOnMEANGraph(mergeAllModuleWithoutDeclaration);
		// app module declares all components that have not been declared by other modules
		for(String componentId : this.findLonelyComponentsInGraph()){
			String mergeModuleWithLonelyComponent = "Match(n:Component) where n.id='" + componentId + "' match(m:Module) where m" +
					".id='app.module.ts' Merge(m)-[:DECLARES]-(n)";
			this.runQueryOnMEANGraph(mergeModuleWithLonelyComponent);
		}
		this.runQueryOnMEANGraph("Match(n:App) where n.id='FRONTEND_APP' Match(m:Module) where m.id='app.module.ts' MERGE(n)-[:DEFINES]-" +
				"(m)");
	}

	public List<String> findLonelyComponentsInGraph(){
		String query = "MATCH (m:Component) WHERE NOT ()-[:DECLARES]-(m) RETURN m";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("m").asNode().get("id").asString());
		}
	}

	public List<String> findModuleByBaseInGraph(String base){
		String query = "Match(n:Module) where n.base='" + base + "' return n";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("n").asNode().get("id").asString());
		}
	}

	public List<String> findComponentByBaseInGraph(String base){
		String query = "Match(n:Component) where n.base='" + base + "' return n";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("n").asNode().get("id").asString());
		}
	}

	public List<String> findRestControllerByBaseInGraph(String base){
		String query = "Match(n:RestController) where n.module='" + base + "' return n";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("n").asNode().get("id").asString());
		}
	}

	public void matchEntitiesToModules(){
		for(ModuleKnowledge moduleKnowledge : this.frontendEntityProcessingModuleKnowledge) {
			// each frontend entity processing module should have one backend entity processing module associated, so this should not
			// happen
			// TODO check that before starting the process so the knowledge can change that and does not only find out after half the
			//  process is completed
			System.out.println(moduleKnowledge.getBase());
			if(moduleKnowledge.getUsedModules()!=null) {
				System.out.println(moduleKnowledge.getBase());
				// match the entity that is processed by the module
				for (String usedModule : moduleKnowledge.getUsedModules()) {
					// find entity that is processed
					for (String model : this.findModelByModuleInGraph(usedModule)) {
						for (EntityModel entityModel : this.entityModels) {
							if (this.removeJsFromClassName(model).equals(this.removeJavaFromClassNameAndChangeToLowerCase(entityModel.getName()))) {
								entitiesComponentsDic.put(moduleKnowledge, entityModel);
								break;
							}
						}
					}
				}
			}
		}
	}

	public List<String> findBackendEndpointOfModel(String entity){
		String query = "match(n:Route)-[]-(m:RestController)-[]-(k:Model) where k.id='" + entity + ".js'" + " return n";
		String queryWithMiddleware = "match(n:Route)-[]-(a:Middleware)-[]-(m:RestController)-[]-(k:Model) where k.id='" + entity +
				".js'" + " return n";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("n").asNode().get("id").asString()).size() == 0 ?
					session.run(queryWithMiddleware).list(result -> result.get("n").asNode().get("id").asString()) :
					session.run(query).list(result -> result.get("n").asNode().get("id").asString());
		}
	}

	public void processFrontendEntityProcessingFeature(){
		this.matchEntitiesToModules();
		List<String> entitiesAlreadyUsed = new ArrayList<>();
		boolean authModuleAlreadyCreated = false;
		for(ModuleKnowledge moduleKnowledge : this.frontendEntityProcessingModuleKnowledge) {
			String entity = this.removeJavaFromClassNameAndChangeToLowerCase(entitiesComponentsDic.get(moduleKnowledge).getName().toLowerCase());

			// create module with service, interface and subject
			String interfaceName = entity + ".model.ts";
			String moduleName = entity + ".module.ts";
			String serviceName = entity + ".service.ts";
			String subjectName = entity + "Subject";
			if(entitiesAlreadyUsed.contains(entity)==false) {
				String interfaceQuery =
						"MERGE(n:Interface {id:'" + interfaceName + "', module:'" + moduleName + "', package: '" + entity + "', location" +
								":'Frontend', base:'" + moduleKnowledge.getBase() + "'})";
				String moduleQuery = "MERGE(n:Module {id:'" + moduleName + "', module:'" + moduleName + "', package: '" + entity + "', location" +
						":'Frontend', base:'" + moduleKnowledge.getBase() + "'})";
				String serviceQuery = "MERGE(n:Service {id:'" + serviceName + "', module:'" + moduleName + "', package: '" + entity + "', location" +
						":'Frontend', base:'" + moduleKnowledge.getBase() + "'})";
				String subjectQuery = "MERGE(n:Subject {id:'" + subjectName + "', module:'" + moduleName + "', package: '" + entity + "'," +
						" location:'Frontend', base:'" + moduleKnowledge.getBase() + "'})";
				this.runQueryOnMEANGraph(interfaceQuery);
				this.runQueryOnMEANGraph(moduleQuery);
				this.runQueryOnMEANGraph(serviceQuery);
				this.runQueryOnMEANGraph(subjectQuery);
				entitiesAlreadyUsed.add(entity);
				if(this.findBackendEndpointOfModel(this.removeJavaFromClassName(entitiesComponentsDic.get(moduleKnowledge).getName())).size()>0) {
					String connectServiceToEndpointQuery =
							"Match(s:Service) where s.id='" + serviceName + "' Match(n:Route) where n.id='"
									+ this.findBackendEndpointOfModel(this.removeJavaFromClassName(entitiesComponentsDic.get(moduleKnowledge).getName())).get(0) + "' MERGE(s)-[:REST_CALL]-(n)";
					this.runQueryOnMEANGraph(connectServiceToEndpointQuery);
				}
			}
			// create components for the module
			for (String component : moduleKnowledge.getModuleCluster()) {
				String componentName = entity + "-" + this.removeCommonJavaEENamesFromClassName(component);
				NodeKnowledge nodeKnowledge = findNodeKnowledgeByClassName(component);

				boolean viewAlreadyCreated = false;
				for (String interpretation : nodeKnowledge.getCalculatedInterpretation()) {

					// e.g. LoginBean.java
					if (viewAlreadyCreated== false && (interpretation.equals("View Controller") || interpretation.equals("View"))) {
						viewAlreadyCreated = true;
						// component
						// automatic: + html + css
						// automatic: oninit ondestroy
						String entityName = entity + "-";
						String packageName = entity;
						String TsComponentName = entityName + this.removeCommonJavaEENamesFromClassName(nodeKnowledge.getName()) + ".component.ts";
						String HtmlComponentName = entityName + this.removeCommonJavaEENamesFromClassName(nodeKnowledge.getName()) + ".component.html";
						String CssComponentName = entityName + this.removeCommonJavaEENamesFromClassName(nodeKnowledge.getName()) + ".component.css";
						String tsQuery =
								"MERGE(n:" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent()
										+ " {id:'" + TsComponentName + "', component: '" + componentName + "', module: '" + moduleName +
										"', package:'" + packageName + "/component', location:'Frontend', base:'" + moduleKnowledge.getBase() + "'})" ;
						String htmlQuery =
								"MERGE(n:" + "HTML" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent()
										+ " {id:'" + HtmlComponentName + "', component: '" + componentName + "', module: '" + moduleName + "', package:'" + packageName +
										"/component', location:'Frontend', base:'" + moduleKnowledge.getBase() + "'})" ;
						String cssQuery =
								"MERGE(n:" + "CSS" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent()
										+ " {id:'" + CssComponentName + "',  component: '" + componentName + "', module: '" + moduleName + "', package:'" + packageName +
										"/component', location:'Frontend', base:'" + moduleKnowledge.getBase() + "'})" ;
						this.runQueryOnMEANGraph(tsQuery);
						this.runQueryOnMEANGraph(htmlQuery);
						this.runQueryOnMEANGraph(cssQuery);
						this.connectFrontendNodesByComponent(componentName);
					} else if (interpretation.equals("Authentication Management")) {
						if(authModuleAlreadyCreated==false) {
							// create Auth Module
							String authServiceQuery = "Merge(n:Service {id:'auth.service.ts', module: 'auth', package:'auth', " +
									"location:'Frontend'})";
							String authGuardQuery =
									"Merge(n:" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent()
											+ " {id:'auth.guard.ts', module: 'auth', package:'auth', location:'Frontend'})";
							String authGuardFunctionalityQuery =
									"Merge(n:Functionality {name:'" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getDefaultLibrary() + "', " +
											"module: 'auth', package:'auth', location:'Frontend'})";
							//String authModuleQuery =
									//"MERGE(n:Module {id:'auth.module.ts', module:'auth', package: 'auth', location" +
									//		":'Frontend'})";
							String authInterfaceQuery =
									"MERGE(n:Interface {id:'auth.model.ts', module:'auth', package: 'auth', location" +
											":'Frontend'})";
							String authSubjectQuery =
									"MERGE(n:Subject {id:'authSubject', module:'auth', package: 'auth', location" +
											":'Frontend'})";
							this.runQueryOnMEANGraph(authGuardQuery);
							this.runQueryOnMEANGraph(authServiceQuery);
							this.runQueryOnMEANGraph(authGuardFunctionalityQuery);
							// auth module is not necessary since login/logout belong to user module
							//this.runQueryOnMEANGraph(authModuleQuery);
							this.runQueryOnMEANGraph(authInterfaceQuery);
							this.runQueryOnMEANGraph(authSubjectQuery);
							// add relations
							String mergeAuthServiceWithModel = "match(n:Service) where n.module='auth' match(m:Interface) where " +
									"m.module='auth' merge(n)-[:USES]-(m)";
							String mergeAuthGuardAndFunc =
									"match(n:" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent() + ") " +
											"where n.module='auth' match(m:Functionality) where m.module='auth' merge(n)-[:IMPORTS]-(m)";
							String mergeAuthServiceWithSubject = "match(n:Service) where n.module='auth' match(m:Subject) where " +
									"m.module='auth' merge(n)-[:CREATES]-(m)";
							this.runQueryOnMEANGraph(mergeAuthServiceWithModel);
							this.runQueryOnMEANGraph(mergeAuthGuardAndFunc);
							this.runQueryOnMEANGraph(mergeAuthServiceWithSubject);
							authModuleAlreadyCreated = true;
						}
						this.runQueryOnMEANGraph("Match(n:Service) where n.module='" + moduleName + "' Match(m:" + ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent() + ") " +
								"MERGE (m)-[:INJECTS]-(n)");
						this.runQueryOnMEANGraph("Match(n:Component) where n.module='" + moduleName + "' Match(m:Service) where m" +
								".module='auth' MERGE (n)-[:USES]-(m)");
						this.runQueryOnMEANGraph("Match(n:Component) where n.module='" + moduleName + "' Match(m:Subject) where m" +
								".module='auth' MERGE (n)-[:SUBSCRIBES_TO]-(m)");
						this.runQueryOnMEANGraph("Match(n:Component) where n.module='" + moduleName + "' Match(m:Interface) where m" +
								".module='auth' MERGE (n)-[:USES]-(m)");
					}
				}
			}
			this.connectFrontendNodesByModule(moduleName);
		}
		this.connectFrontendNodesByModule("auth");
	}


	public void connectFrontendNodesByModule(String module){
		List<String> queries = new ArrayList<>();
		queries.add("Match(n:Module) where n.module='" + module + "' Match(m:Component) where m.module='"+ module + "' Merge(n)" +
				"-[:DECLARES]-(m)");
		queries.add("Match(n:Service) where n.module='" + module + "' Match(m:Interface) where m.module='"+ module + "' Merge(n)" +
				"-[:USES]-(m)");
		queries.add("Match(n:Component) where n.module='" + module + "' Match(m:Interface) where m.module='"+ module + "' Merge(n)" +
				"-[:USES]-(m)");
		queries.add("Match(n:Component) where n.module='" + module + "' Match(m:Service) where m.module='"+ module + "' Merge(n)" +
				"-[:USES]-(m)");
		queries.add("Match(n:Component) where n.module='" + module + "' Match(m:Subject) where m.module='"+ module + "' Merge(n)" +
				"-[:SUBSCRIBES_TO]-(m)");
		queries.add("Match(n:Service) where n.module='" + module + "' Match(m:Subject) where m.module='"+ module + "' Merge(n)" +
				"-[:CREATES]-(m)");
		queries.add("Match(n:RoutingModule) Match(m:Component) Merge(n)-[:MANAGES_ROUTE_TO]-(m)");
		queries.add("Match(n:Component) Merge(m:Functionality {name:'OnInit', location" +
						":'Frontend'}) MERGE(n)-[:IMPLEMENTS]-(m)");
		queries.add("Match(n:Component) Merge(m:Functionality {name:'OnDestroy', location" +
				":'Frontend'}) MERGE(n)-[:IMPLEMENTS]-(m)");
		queries.add("Match(n:App) where n.id='FRONTEND_APP' Match(m:Module) where m.id='app.module.ts' MERGE(n)-[:DEFINES]-(m)");
		for(String query : queries){
			this.runQueryOnMEANGraph(query);
		}
	}

	public void connectFrontendNodesByComponent(String component){
		List<String> queries = new ArrayList<>();
		queries.add("Match(n:Component) where n.component='" + component + "' Match(m:HTMLComponent) where m.component='"+ component + "'" +
				" " +
				"Merge" +
				"(n)" +
				"-[:HAS]-(m)");
		queries.add("Match(n:Component) where n.component='" + component + "' Match(m:CSSComponent) where m.component='"+ component + "' " +
				"Merge(n)" +
				"-[:HAS]-(m)");
		queries.add("Match(n:Component) where n.component='" + component + "' Match(m:Service) where m.component='"+ component + "' Merge" +
				"(n)" +
				"-[:USES]-(m)");
		for(String query : queries){
			this.runQueryOnMEANGraph(query);
		}
	}

	public String removeJavaFromClassNameAndChangeToLowerCase(String className){
		return className.toLowerCase().replace(".java", "");
	}

	public String removeJavaFromClassName(String className){
		return className.replace(".java", "");
	}


	public String removeJsFromClassName(String className){
		return className.toLowerCase().replace(".js", "");
	}

	public String removeCommonJavaEENamesFromClassName(String className){
		className = className.toLowerCase().replace("bean", "");
		className = className.toLowerCase().replace("view", "");
		className = className.toLowerCase().replace(".java", "");
		return className;
	}

	public List<String> findModelByModuleInGraph(String moduleId){
		String query = "Match(n:Model) where n.module='" + moduleId + "' return n";
		Driver driver = setUpNeo4jDriver("MEAN");
		try(Session session = setUpNeo4jSession(driver)) {
			return session.run(query).list(result -> result.get("n").asNode().get("id").asString());
		}
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
		String authComponent="Authentication Feature";
		String query = "MERGE(n:Middleware {id:'Auth.js', module:'" + moduleKnowledge.getBase() + "', location: 'Backend', package: " +
				"'middleware'})";
		String authFunctionalityQuery =
				"MERGE(f:Functionality {name:'" + ontologyKnowledgeService.findByJavaEEComponent(authComponent).getDefaultLibrary() + "'," +
						" location:'Backend', module:'" + moduleKnowledge.getBase() + "'})";
		String connectMiddlewareToFunctionalityQuery = "Match(n:Middleware) where n.id='Auth.js' Match(f:Functionality) where f.name='"
				+ ontologyKnowledgeService.findByJavaEEComponent(authComponent).getDefaultLibrary() + "' MERGE(n)-[:IMPORTS]-(f)";
		this.runQueryOnMEANGraph(query);
		this.runQueryOnMEANGraph(authFunctionalityQuery);
		this.runQueryOnMEANGraph(connectMiddlewareToFunctionalityQuery);
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
				for (String moduleComponent : scheduledFeatureModuleKnowledge.getModuleCluster()) {
					NodeKnowledge scheduledFeatureComponentKnowledge = this.findNodeKnowledgeByClassName(moduleComponent);
					// find all modules that are used by a scheduled module
					// e.g. a scheduled batch feature:
					if (scheduledFeatureComponentKnowledge.getCalculatedInterpretation().contains("Data Access Object")) {
						createSchedulerToControllerRelationQuery =
								"Match(n:" + ontologyKnowledgeService.findByJavaEEComponent(schedulingFeatureJavaEEComponent).getMEANComponent()
										+ " {id:'" + this.renameToJsComponent(componentKnowledge.getName()) + "'}) Match(m:RestController" +
										" {module:'" + scheduledFeatureModuleKnowledge.getBase() + "'}) MERGE (n)" +
										"-[:CALLS]-(m)";
						this.runQueryOnMEANGraph(createSchedulerToControllerRelationQuery);
						// a batch feature is not visible as its own class in the mean stack architecture model
						// instead it gets translated as a controller method (e.g. batch write xy)
						// therefore, find all entity processing modules that the batch feature module uses
						/*List<String> usedModules = scheduledFeatureModuleKnowledge.getUsedModules();
						for (String moduleCalledByBatchFeature : usedModules) {
							// add connection from the scheduler to the controller class, so it's visible in the modell
							// that there are scheduled controller operations


						}*/
						break;
					}
				}
			}
		}
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
		int restModelCount=0;
		int routeCount=0;
		int restRouteCount=0;
		int soapApiCount = 0;

		String transactionFeature="Transaction Feature";
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
				// automatically add rest route to soap route
				String restInterpretation="Service";
				String associatedRESTMEANComponent =
						ontologyKnowledgeService.findByJavaEEComponent(restInterpretation).getMEANComponent();
				query =
						query + " MERGE (k" + restRouteCount + ":" + associatedRESTMEANComponent + " {id:'" + moduleName +
								"', module:" +
								" '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
								"'routes'})";

				if (!(routeCount > 0)) {
					query =
							query + " MERGE (g:Functionality" + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
									"', location: '" + meanLocation + "', package: 'routes', name: '" + ontologyKnowledgeService.findByJavaEEComponent(restInterpretation).getDefaultLibrary() + "'})";
				}
				restRouteCount++;
			}
			// soap controller
			else if(nodeKnowledge.getCalculatedInterpretation().contains(wsdlEndpointJavaEEComponent)){
				String associatedMEANComponent =
						ontologyKnowledgeService.findByJavaEEComponent(wsdlEndpointJavaEEComponent).getMEANComponent();
				query =
						query + " MERGE (w:" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
								"'soap/controller'})";
				// automatically add rest controller to soap controller
				String restInterpretation="Data Access Object";
				String associatedRESTMEANComponent =
						ontologyKnowledgeService.findByJavaEEComponent(restInterpretation).getMEANComponent();
				query =
						query + " MERGE (m" + restModelCount + ":" + associatedRESTMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
								"'controller'})";
				restModelCount++;
			}
			else {
				for (String interpretation : nodeKnowledge.getCalculatedInterpretation()) {
					if(interpretation.equals(transactionFeature)){
						query =
								query + " MERGE (trans:Functionality" + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() +
										"', location: '" + meanLocation + "', name: '" + ontologyKnowledgeService.findByJavaEEComponent(transactionFeature).getDefaultLibrary() + "'})";
					}

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
						if(moduleKnowledge.getUsedModules()!=null) {
							for (String usedModule : moduleKnowledge.getUsedModules()) {
								ModuleKnowledge usedModuleKnowledge = moduleKnowledgeService.findModuleKnowledgeByBase(usedModule);
								for (String comp : usedModuleKnowledge.getModuleCluster()) {
									NodeKnowledge nodeKnowledgeInstance = this.findNodeKnowledgeByClassName(comp);
									if (nodeKnowledgeInstance.getCalculatedInterpretation().contains("Batch Feature")) {
										entityModels.remove(entityModel);
										entityModel.setBatchMethodNeeded(true);
										entityModelService.insertOne(entityModel);
										entityModels.add(entityModel);
										break;
									}
								}
							}
						}
					}
					// controller
					else if (interpretation.equals("Data Access Object")) {
						String associatedMEANComponent =
								ontologyKnowledgeService.findByJavaEEComponent(interpretation).getMEANComponent();
						query =
								query + " MERGE (m" + modelCount + ":" + associatedMEANComponent + " {id:'" + moduleName + "', module: '" + moduleKnowledge.getBase() + "', location: '" + meanLocation + "', package: " +
										"'controller'})";
						modelCount++;
						if(moduleKnowledge.getUsedModules()!=null){
							for(String usedModule : moduleKnowledge.getUsedModules()){
								if(this.findRestControllerByBaseInGraph(usedModule).size()!=0) {
									if(this.backendEntityProcessingModuleAssociations.get(moduleKnowledge)==null){
										List<String> list = new ArrayList<>();
										list.add(usedModule);
										this.backendEntityProcessingModuleAssociations.put(moduleName, list);
									}
									else {
										List<String> list = this.backendEntityProcessingModuleAssociations.get(moduleKnowledge);
										list.add(usedModule);
										this.backendEntityProcessingModuleAssociations.put(moduleName, list);
									}
								}
							}
						}

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
			for (String attribute : entityModel.getAttributes()) {

				if(entityModel.getAttributeIsRelatedOtherEntity().get(attribute)==true){
					// does not work with User in reports (attribute like) because type is Set<User>  Match(n:Model {id:'Report.js'}) Match
					// (m:Model {id:'Set<User>.js'}) MERGE (n)-[r:ManyToMany]-(m) SET r.name='liker'
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

	public String renameToTsComponent(String className){
		return className.replace(".java", "") + ".ts";
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

	public void addMissingComponentToModule(String component, String moduleId, String location){
		String query = "MERGE (n:" + component + " {id:'" + moduleId + "', location:'" + location + "'})";
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
		queries.add("MATCH (n:App) where n.location='Backend' MATCH (m:Route) where m.module='" + module + "' MERGE (n)" +
				"-[:DEFINES]->(m)");
		queries.add("MATCH (n:App) where n.location='Backend' MATCH (m:SoapRoute) where m.module='" + module + "' MERGE (n)" +
				"-[:DEFINES]->(m)");
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
		queries.add("MATCH (n:RestController) where n.module='" + module + "' MATCH (m:Functionality) where m.module='" + module  + "' " +
				"AND m.name='mongoose transactions' MERGE(n)-[:IMPORTS]->(m)");
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
