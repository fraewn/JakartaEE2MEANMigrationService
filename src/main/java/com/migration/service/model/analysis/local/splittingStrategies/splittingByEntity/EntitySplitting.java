package com.migration.service.model.analysis.local.splittingStrategies.splittingByEntity;

import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.knowledgeCollection.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.entitySplitting.EntitySplittingProfile;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.entitySplitting.EntitySplittingProfileService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntitySplitting {
	private final NodeKnowledgeService nodeKnowledgeService;
	private final EntitySplittingProfileService entitySplittingProfileService;
	private EntitySplittingProfile entitySplittingProfile;


	private final Driver driver;
	public EntitySplitting(Driver driver, NodeKnowledgeService nodeKnowledgeService,
						   EntitySplittingProfileService entitySplittingProfileService) {
		this.driver = driver;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.entitySplittingProfileService = entitySplittingProfileService;

	}
	public List<ModuleKnowledge> executeEntitySplitting(){
		List<ModuleKnowledge> moduleKnowledges = new ArrayList<>();
		this.entitySplittingProfile = entitySplittingProfileService.findAll().get(0);
		for(String entity : this.getEntities()){
			String centralJavaEEComponent = this.findCentralJavaEEComponentInEntityNeighbours(this.getEntityNeighbours(entity));
			List<String> centralComponentNeighbours = this.getCentralJavaEEComponentNeighbours(centralJavaEEComponent);
			centralComponentNeighbours.add(centralJavaEEComponent);
			List<String> filteredModule = this.filter(centralComponentNeighbours);
			ModuleKnowledge moduleKnowledge = new ModuleKnowledge();
			moduleKnowledge.setBase(entity);
			moduleKnowledge.setModuleCluster(filteredModule);
			moduleKnowledge.setSplittingStrategy("Entity Splitting Strategy");
			moduleKnowledges.add(moduleKnowledge);
		}
		return moduleKnowledges;
	}

	public List<String> filter(List<String> module){
		List<String> javaEEComponentsToBeFiltered = entitySplittingProfile.getFilteredJavaEEComponents();
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

	public List<String> getCentralJavaEEComponentNeighbours(String serviceName){
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

	public String findCentralJavaEEComponentInEntityNeighbours(List<String> entityNeighbours){
		for(String neighbour : entityNeighbours){
			for(String javaEEComponent : nodeKnowledgeService.findByName(neighbour).getCalculatedInterpretation()){
				if(entitySplittingProfile.getCentralJavaEEComponent().equals(javaEEComponent)){
					return neighbour;
				}
			}
		}

		// if the central javaEE component is not found by now, ...
		for(String neighbour : entityNeighbours){
			for(String javaEEComponent : nodeKnowledgeService.findByName(neighbour).getCalculatedInterpretation()){
				// use the substitute
				System.out.println(entitySplittingProfile.getSubstitutionalCentralJavaEEComponent());
				if(entitySplittingProfile.getSubstitutionalCentralJavaEEComponent().equals(javaEEComponent)){
					return neighbour;
				}
			}
		}

		// if the substitutional javaEE component could also not be found,...
		return "";
	}



}
