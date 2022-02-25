package com.migration.service.service.analysis.local.splittingStrategies.splittingByFunctionality;

import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledge;
import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledgeService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FunctionalitySplitting {
	List<String> filteredJavaEEComponents;
	List<String> filteredFunctionalities;

	private final ModuleKnowledgeService moduleKnowledgeService;
	private OntologyKnowledgeService ontologyKnowledgeService;
	private NodeKnowledgeService nodeKnowledgeService;

	private final Driver driver;
	public FunctionalitySplitting(Driver driver, OntologyKnowledgeService ontologyKnowledgeService,
								  NodeKnowledgeService nodeKnowledgeService, ModuleKnowledgeService moduleKnowledgeService){
		this.driver = driver;
		this.ontologyKnowledgeService = ontologyKnowledgeService;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.moduleKnowledgeService = moduleKnowledgeService;
		initWithDefaultSettings();
	}

	public void initWithDefaultSettings(){
		filteredJavaEEComponents = new ArrayList<>();
		filteredJavaEEComponents.add("Database Entity");
		filteredJavaEEComponents.add("Data Access Object");
		filteredJavaEEComponents.add("Cross Section");

		filteredFunctionalities = new ArrayList<>();
		filteredFunctionalities.add("javax.inject");
		filteredFunctionalities.add("javax.ejb");
		filteredFunctionalities.add("javax.annotation");
	}
	public void executeFunctionalitySplittingStrategy(){
		List<ModuleKnowledge> moduleKnowledgeList = new ArrayList<>();
		for(String functionality : filterFunctionalities(findFunctionalities())){
			// skip javax.faces.view etc. only do javax.faces
			boolean skipLoop = false;
			if((functionality.split("\\.").length-1) > 1){
				int lastIndexOfDot = functionality.lastIndexOf(".");
				String possibleAncestorFunctionality = functionality.substring(0, lastIndexOfDot);

				for(ModuleKnowledge moduleKnowledge : moduleKnowledgeList){
					if(moduleKnowledge.getBase().contains(possibleAncestorFunctionality) || moduleKnowledge.getBase().contains(functionality)){
						skipLoop=true;
					}
				}
			}


			for(ModuleKnowledge moduleKnowledge : moduleKnowledgeList){
				if(moduleKnowledge.getBase().contains(functionality)){
					skipLoop=true;
				}
			}
			if(skipLoop == true){
				System.out.println("excluded: " + functionality);
				continue;
			}
			System.out.println("worked for: " + functionality);
			ModuleKnowledge moduleKnowledge = new ModuleKnowledge();
			List<String> moduleComponents = new ArrayList<>();
			List<String> neighbours = findFunctionalityNeighbours(functionality);

			for(String neighbour : neighbours){
				boolean containsFilteredComponent = false;
				NodeKnowledge neighbourNodeKnowledge = nodeKnowledgeService.findByName(neighbour);
				for(String filteredComponent : filteredJavaEEComponents){
					if(neighbourNodeKnowledge.getCalculatedInterpretation().contains(filteredComponent)){
						containsFilteredComponent = true;
					}
				}
				if(containsFilteredComponent == true){
					continue;
				}
				if(neighbourNodeKnowledge.containsLabel("Functionality") || neighbourNodeKnowledge.containsLabel(
						"InjectedExternal")){
					continue;
				}
				if(!moduleComponents.contains(neighbour)) moduleComponents.add(neighbour);
				// add because of keyword
					/*else if(neighbourNodeKnowledge.getKeywords().contains(keyword)){
						moduleComponents.add(neighbour);
					}*/
				// add

				// finally
				if(neighbourNodeKnowledge.getLabel().contains("Interface")) {
					for (String implementingClass : searchClassesImplementingTheInterface(neighbour)) {
						if(!moduleComponents.contains(implementingClass)) moduleComponents.add(implementingClass);
					}
				}
			}
			try {
				String keyword = ontologyKnowledgeService.findByKnowledgeSource(functionality).getAssociatedKeyword();
				for(String component : findByKeyword(keyword)){
					if(!moduleComponents.contains(component)) moduleComponents.add(component);
				}
			}
			catch (Exception e){}
			moduleComponents.add(functionality);
			moduleKnowledge.setModuleCluster(moduleComponents);
			moduleKnowledge.setBase(functionality);
			moduleKnowledge.setSplittingStrategy("Functionality Based Splitting Strategy");
			moduleKnowledgeList.add(moduleKnowledge);
		}
		moduleKnowledgeService.insertAll(moduleKnowledgeList);

	}

	public List<String> findFunctionalities(){
		String query = "MATCH (n:Functionality) return n";
		try(Session session = driver.session()) {
			return session.run(query).list(result -> result.get("n").asNode().get("name").asString());
		}
	}

	public List<String> findByKeyword(String keyword){
		List<String> nodesAssociatedByKeyword = new ArrayList<>();
		List<NodeKnowledge> nodeKnowledgeList = nodeKnowledgeService.findAll();
		for(NodeKnowledge nodeKnowledge : nodeKnowledgeList){
			if(nodeKnowledge.getKeywords().contains(keyword)){
				nodesAssociatedByKeyword.add(nodeKnowledge.getName());
			}
		}
		return nodesAssociatedByKeyword;
	}

	public List<String> filterFunctionalities(List<String> functionalties){
		functionalties.remove("javax.inject");
		functionalties.remove("javax.ejb");
		functionalties.remove("javax.annotation");
		return functionalties;
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
				query = "MATCH (n:Functionality {name: \'" + functionalityName + "\'}) " +
						"CALL apoc.neighbors.byhop(n, \'CALLS_METHOD|IMPLEMENTS|" +
						"EXTENDS|INJECTS|USES_FUNCTIONALITY\' , 3) " +
						"YIELD nodes " +
						"UNWIND nodes as m " +
						"RETURN m";
				neighbours = session.run(query).list(result -> result.get("m").asNode().get("name").asString());
			}
			else if(neighbours.size()>15){
				query = "MATCH (n:Functionality {name: \'" + functionalityName + "\'}) " +
						"CALL apoc.neighbors.byhop(n, \'CALLS_METHOD|IMPLEMENTS|" +
						"EXTENDS|INJECTS|USES_FUNCTIONALITY\' , 1) " +
						"YIELD nodes " +
						"UNWIND nodes as m " +
						"RETURN m";
				neighbours = session.run(query).list(result -> result.get("m").asNode().get("name").asString());
			}
			return neighbours;
		}
	}

	public List<String> searchClassesImplementingTheInterface(String interfaceName){
		String query = "MATCH (n {name:'" + interfaceName + "'})<-[:IMPLEMENTS]-(c) return c";
		try(Session session = driver.session()) {
			List<String> classesImplementingTheInterface = new ArrayList<>();
			classesImplementingTheInterface.addAll(session.run(query).list(result -> {
				if(result!=null){
					return result.get("c").asNode().get("name").asString();
				}
				return null;
			}));
			return classesImplementingTheInterface;
		}
	}
}
