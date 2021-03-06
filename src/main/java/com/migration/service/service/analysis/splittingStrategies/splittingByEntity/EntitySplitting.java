package com.migration.service.service.analysis.splittingStrategies.splittingByEntity;

import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.entitySplitting.EntitySplittingProfile;
import com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.entitySplitting.EntitySplittingProfileService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntitySplitting {
	private final NodeKnowledgeService nodeKnowledgeService;
	private final EntitySplittingProfileService entitySplittingProfileService;
	private final ModuleKnowledgeService moduleKnowledgeService;
	private EntitySplittingProfile entitySplittingProfile;


	private final Driver driver;
	public EntitySplitting(Driver driver, NodeKnowledgeService nodeKnowledgeService,
						   EntitySplittingProfileService entitySplittingProfileService, ModuleKnowledgeService moduleKnowledgeService) {
		this.driver = driver;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.entitySplittingProfileService = entitySplittingProfileService;
		this.moduleKnowledgeService = moduleKnowledgeService;

	}
	public void executeEntitySplitting(){
		List<ModuleKnowledge> moduleKnowledges = new ArrayList<>();
		this.entitySplittingProfile = entitySplittingProfileService.findAll().get(0);
		for(String entity : this.getEntities()){
			String centralJavaEEComponent = this.findCentralJakartaEEComponentInEntityNeighbours(this.getEntityNeighbours(entity));
			List<String> centralComponentNeighbours = this.getCentralJakartaEEComponentNeighbours(centralJavaEEComponent);
			centralComponentNeighbours.add(centralJavaEEComponent);
			List<String> filteredModule = this.filter(centralComponentNeighbours);
			ModuleKnowledge moduleKnowledge = new ModuleKnowledge();
			moduleKnowledge.setBase(entity);
			moduleKnowledge.setModuleCluster(filteredModule);
			moduleKnowledge.setSplittingStrategy("Entity Based Splitting Strategy");
			moduleKnowledges.add(moduleKnowledge);
		}
		moduleKnowledgeService.insertAll(moduleKnowledges);
	}

	public List<String> filter(List<String> module){
		List<String> javaEEComponentsToBeFiltered = entitySplittingProfile.getFilteredJakartaEEComponents();
		System.out.println(javaEEComponentsToBeFiltered);
		List<String> sortedOut = new ArrayList<>();
		for(String component : module){
			for(String javaEEComponent : nodeKnowledgeService.findByName(component).getCalculatedInterpretation()){
				for(String componentToBeFiltered : javaEEComponentsToBeFiltered){
					if(javaEEComponent.equals(componentToBeFiltered) || javaEEComponent.equals("Abstract/Interface")){
						sortedOut.add(component);
					}
				}
			}
		}
		System.out.println(sortedOut);
		module.removeAll(sortedOut);

		return module;
	}

	public List<String> getEntities(){
		String query = "MATCH (n:Entity) return n";
		try(Session session = driver.session()) {
			return session.run(query).list(result -> result.get("n").asNode().get("name").asString());
		}
	}
	public List<String> getEntityNeighbours(String entityName){
		String query = "MATCH (n:Entity {name: \'" + entityName + "\'}) " +
				"CALL apoc.neighbors.byhop(n, \'CALLS_METHOD|IMPLEMENTS|" +
				"EXTENDS|INJECTS|IS_ENTITY\', 3) " +
				"YIELD nodes " +
				"UNWIND nodes as m " +
				"RETURN m";
		try(Session session = driver.session()) {
			return session.run(query).list(result -> result.get("m").asNode().get("name").asString());
		}
	}

	public List<String> getCentralJakartaEEComponentNeighbours(String serviceName){
		int searchDepth = entitySplittingProfile.getSearchDepth();
		String query = "MATCH (n:JavaImplementation {name: \'" + serviceName + "\'}) " +
				"CALL apoc.neighbors.byhop(n, \'>CALLS_METHOD|>IMPLEMENTS|EXTENDS|INJECTS|IS_ENTITY\', " + searchDepth + ") " +
				"YIELD nodes " +
				"UNWIND nodes as m " +
				"RETURN m";
		try(Session session = driver.session()) {
			// test necessary
			return session.run(query).list(result -> result.get("m").asNode().get("name").asString());
		}
	}

	public String findCentralJakartaEEComponentInEntityNeighbours(List<String> entityNeighbours){
		for(String neighbour : entityNeighbours){
			for(String javaEEComponent : nodeKnowledgeService.findByName(neighbour).getCalculatedInterpretation()){
				if(entitySplittingProfile.getCentralJakartaEEComponent().equals(javaEEComponent)){
					return neighbour;
				}
			}
		}

		// if the central javaEE component is not found by now, ...
		for(String neighbour : entityNeighbours){
			for(String javaEEComponent : nodeKnowledgeService.findByName(neighbour).getCalculatedInterpretation()){
				// use the substitute
				System.out.println(entitySplittingProfile.getSubstitutionalCentralJakartaEEComponent());
				if(entitySplittingProfile.getSubstitutionalCentralJakartaEEComponent().equals(javaEEComponent)){
					return neighbour;
				}
			}
		}

		// if the substitutional javaEE component could also not be found,...
		return "";
	}



}
