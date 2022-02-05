package com.migration.service.model.analysis.local.splittingStrategies.splittingByFunctionality;

import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledge;
import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.knowledgeCollection.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.ontologyKnowledge.OntologyKnowledgeService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.value.Uncoercible;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FunctionalitySplitting {
	int searchDepth = 2;
	List<String> filteredJavaEEComponents;
	List<String> allowedJavaEEComponents ;
	String centralJavaEEComponent;
	String substitutionalCentralJavaEEComponent;

	private OntologyKnowledgeService ontologyKnowledgeService;
	private NodeKnowledgeService nodeKnowledgeService;

	private final Driver driver;
	public FunctionalitySplitting(Driver driver, OntologyKnowledgeService ontologyKnowledgeService, NodeKnowledgeService nodeKnowledgeService){
		this.driver = driver;
		this.ontologyKnowledgeService = ontologyKnowledgeService;
		this.nodeKnowledgeService = nodeKnowledgeService;
		initWithDefaultSettings();
	}

	public void initWithDefaultSettings(){
		filteredJavaEEComponents = new ArrayList<>();
		filteredJavaEEComponents.add("Database Entity");
		filteredJavaEEComponents.add("Data Access Object");
	}
	public List<ModuleKnowledge> executeFunctionalitySplittingStrategy(){
		List<ModuleKnowledge> moduleKnowledgeList = new ArrayList<>();
		for(String functionality : findFunctionalities()){
			ModuleKnowledge moduleKnowledge = new ModuleKnowledge();
			List<String> moduleComponents = new ArrayList<>();
			List<String> neighbours = findFunctionalityNeighbours(functionality);


			for(String neighbour : neighbours){
				NodeKnowledge neighbourNodeKnowledge = nodeKnowledgeService.findByName(neighbour);
				try{
					String keyword = ontologyKnowledgeService.findByKnowledgeSource(functionality).getAssociatedKeyword();
					if(neighbourNodeKnowledge.getKeywords().contains(keyword)){
						moduleComponents.add(neighbour);
					}
				}
				catch(Exception e){}
				if(!neighbourNodeKnowledge.getCalculatedInterpretation().contains(filteredJavaEEComponents)){
					moduleComponents.add(neighbour);
				}
				if(neighbourNodeKnowledge.getLabel().contains("Interface")) {
					for (String implementingClass : searchClassesImplementingTheInterface(neighbour)) {
						moduleComponents.add(implementingClass);
					}
				}
				moduleKnowledge.setBase(functionality);
				moduleKnowledge.setModuleCluster(moduleComponents);
				moduleKnowledge.setSplittingStrategy("Functionality Based Splitting Strategy");
				moduleKnowledgeList.add(moduleKnowledge);
			}


		}
		return moduleKnowledgeList;
	}

	public List<String> findFunctionalities(){
		String query = "MATCH (n:Functionality) return n";
		try(Session session = driver.session()) {
			return session.run(query).list(result -> result.get("n").asNode().get("name").asString());
		}
	}

	public List<String> findFunctionalityNeighbours(String functionalityName){
		String query = "MATCH (n:Functionality {name: \'" + functionalityName + "\'}) " +
				"CALL apoc.neighbors.byhop(n, \'CALLS_METHOD|IMPLEMENTS|" +
				"EXTENDS|INJECTS|USES_FUNCTIONALITY\', 2) " +
				"YIELD nodes " +
				"UNWIND nodes as m " +
				"RETURN m";
		try(Session session = driver.session()) {
			List<String> neighbours = session.run(query).list(result -> result.get("m").asNode().get("name").asString());
			// keep size of neighbours between 5 and 15, if necessary adjust the search extent
			if(neighbours.size()<5){
				query = "MATCH (n:Entity {name: \'" + functionalityName + "\'}) " +
						"CALL apoc.neighbors.byhop(n, \'CALLS_METHOD|IMPLEMENTS|" +
						"EXTENDS|INJECTS|IS_ENTITY\', 3) " +
						"YIELD nodes " +
						"UNWIND nodes as m " +
						"RETURN m";
				return session.run(query).list(result -> result.get("m").asNode().get("name").asString());
			}
			else if(neighbours.size()>15){
				query = "MATCH (n:Entity {name: \'" + functionalityName + "\'}) " +
						"CALL apoc.neighbors.byhop(n, \'CALLS_METHOD|IMPLEMENTS|" +
						"EXTENDS|INJECTS|IS_ENTITY\', 1) " +
						"YIELD nodes " +
						"UNWIND nodes as m " +
						"RETURN m";
				return session.run(query).list(result -> result.get("m").asNode().get("name").asString());
			}
			return neighbours;
		}
	}

	public List<String> searchClassesImplementingTheInterface(String interfaceName){
		String query = "MATCH (n {name:'" + interfaceName + "'})<-[:IMPLEMENTS]-(c) return c";
		try(Session session = driver.session()) {
			List<String> classesImplementingTheInterface = new ArrayList<>();

			// TODO not working yet, neo4j uncoercible exception
			classesImplementingTheInterface.addAll(session.run(query).list(result -> {
				if(result!=null){
					return result.get("n").asNode().get("name").asString();
				}
				return null;
			}));
			return classesImplementingTheInterface;
		}
	}
}
