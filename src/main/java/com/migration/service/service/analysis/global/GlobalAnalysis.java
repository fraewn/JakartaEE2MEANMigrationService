package com.migration.service.service.analysis.global;

import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledge;
import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledge;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledgeService;
import com.migration.service.model.analysisKnowledge.semanticKnowledge.SemanticKnowledge;
import com.migration.service.model.analysisKnowledge.semanticKnowledge.SemanticKnowledgeService;
import org.neo4j.driver.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GlobalAnalysis {

	private final SemanticKnowledgeService semanticKnowledgeService;
	private final NodeKnowledgeService nodeKnowledgeService;
	private final OntologyKnowledgeService ontologyKnowledgeService;
	private final ModuleKnowledgeService moduleKnowledgeService;
	//private List<Pair<String, Value>> triangleCountResults;

	private String pageRankQuery = "CALL algo.pageRank.stream(null, null, " +
			"            {iterations:20, dampingFactor:0.85}) " +
			"YIELD nodeId, score " +
			"RETURN algo.asNode(nodeId).name AS name,score";
	private String betweennessCentralityQuery = "CALL algo.betweenness.stream(null,null) " +
			"YIELD nodeId, centrality " +
			"MATCH (n) WHERE id(n) = nodeId " +
			"RETURN n.name AS name, centrality as score";
	private String closenessCentralityQuery = "CALL algo.closeness.stream(null, null) " +
			"YIELD nodeId, centrality " +
			"RETURN algo.asNode(nodeId).name AS name, centrality as score";
	private String triangleCountQuery = "CALL algo.triangleCount.stream(null, null, {concurrency:8}) " +
			"YIELD nodeId, triangles " +
			"return algo.getNodeById(nodeId).name as name, triangles as score";
	private String triangleCoefficientQuery = "CALL algo.triangleCount.stream(null, null, {concurrency:8}) " +
			"YIELD nodeId, coefficient " +
			"return algo.getNodeById(nodeId).name as name, coefficient as score";
	private String louvainQuery = "CALL algo.louvain.stream('JavaImplementation', null, {})" +
			" YIELD nodeId, community" +
			" RETURN algo.getNodeById(nodeId).name as name, community" +
			" ORDER BY community DESC";

	private final Driver driver;
	public GlobalAnalysis(Driver driver, SemanticKnowledgeService semanticKnowledgeService,
						  NodeKnowledgeService nodeKnowledgeService, OntologyKnowledgeService ontologyKnowledgeService,
						  ModuleKnowledgeService moduleKnowledgeService) {
		this.driver = driver;
		this.semanticKnowledgeService = semanticKnowledgeService;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.ontologyKnowledgeService = ontologyKnowledgeService;
		this.moduleKnowledgeService = moduleKnowledgeService;
		this.setUp();
	}

	public void setUp(){
		//ontologyKnowledgeService.setUp();
	}

	public List<ModuleKnowledge> calculateLouvainClusters(){
		List<ModuleKnowledge> louvainModules = new ArrayList<>();
		int currentClusterNumber = -1;
		String currentBase = "";
		List<String> moduleCluster = new ArrayList<>();
		try(Session session = driver.session()){
			boolean beginningOfList = true;
			for(Record r : session.run(louvainQuery).list()){
				int community = r.get(1).asInt();
				String name = r.get(0).asString();
				if(beginningOfList) {
					currentClusterNumber = community;
					beginningOfList = false;
				}
				if(community==currentClusterNumber){
					moduleCluster.add(name);
					currentBase = "" + community;
				}
				else {
					currentClusterNumber = community;
					ModuleKnowledge moduleKnowledge = new ModuleKnowledge();
					moduleKnowledge.setSplittingStrategy("Louvain");
					moduleKnowledge.setBase(currentBase);
					moduleKnowledge.setModuleCluster(moduleCluster);
					louvainModules.add(moduleKnowledge);
					moduleCluster = new ArrayList<>();
					currentBase = "" + community;
					moduleCluster.add(name);
				}
				//System.out.println(r.get(0).asString());
				//System.out.println(r.get(1).asInt());
			}
			ModuleKnowledge moduleKnowledge = new ModuleKnowledge();
			moduleKnowledge.setSplittingStrategy("Louvain");
			moduleKnowledge.setBase(currentBase);
			moduleKnowledge.setModuleCluster(moduleCluster);
			louvainModules.add(moduleKnowledge);
		}
		return louvainModules;
	}

	public List<NodeKnowledge> calculateInterpretation(List<NodeKnowledge> nodeKnowledgeList){
			List<NodeKnowledge> nodeKnowledgeWithInterpretations = new ArrayList<>();
			for(NodeKnowledge nodeKnowledgeInstance : nodeKnowledgeList) {
				List<String> calculatedInterpretations = new ArrayList<>();
				List<String> labels = nodeKnowledgeInstance.getLabel();
				// node is not a java class
				if(!labels.contains("JavaImplementation") && !labels.contains("Interface")){
					for (String label : labels) {
						calculatedInterpretations.add(label);
					}
				}
				// node is a java class
				else {
					List<String> keywords = nodeKnowledgeInstance.getKeywords();
					if(labels.contains("Enum")){
						calculatedInterpretations.add("Enum");
					}
					if (nodeKnowledgeInstance.isClassIsEntity()) {
						calculatedInterpretations.add("Entity Implementation");
					}
					if (labels.contains("AbstractClass") || labels.contains("Interface")) {
						calculatedInterpretations.add("Abstract/Interface");
					}
					// automatic interpretation is only possible with one keyword
					if (keywords.size() == 1) {
						OntologyKnowledge ontologyKnowledge = ontologyKnowledgeService.findByAssociatedKeyword(keywords.get(0));
						if (ontologyKnowledge != null) {
							calculatedInterpretations.add(ontologyKnowledge.getJavaEEComponent());
						}
					}

					List<OntologyKnowledge> ontologyKnowledgeList = ontologyKnowledgeService.findAll();
					for (String functionality : nodeKnowledgeInstance.getFunctionalities()) {
						for (OntologyKnowledge ontologyKnowledge : ontologyKnowledgeList) {
							String knowledgeSource = ontologyKnowledge.getKnowledgeSource();
							if(!knowledgeSource.equals("Default Component")){
								if(!knowledgeSource.equals("") && functionality.contains(knowledgeSource)) {
									if (!calculatedInterpretations.contains(ontologyKnowledge.getJavaEEComponent())) {
										calculatedInterpretations.add(ontologyKnowledge.getJavaEEComponent());
									}
								}
							}
						}
					}
					if (nodeKnowledgeInstance.getBetweennessCentralityScore() > 10 && calculatedInterpretations.size()==0) {
						calculatedInterpretations.add("Cross Section");
					}
				}
				if(calculatedInterpretations.size()==0 || calculatedInterpretations.size() > 2){
					nodeKnowledgeInstance.setReviewNecessary(true);
				}
				else {
					nodeKnowledgeInstance.setReviewNecessary(false);
				}
				nodeKnowledgeInstance.setCalculatedInterpretation(calculatedInterpretations);
				nodeKnowledgeWithInterpretations.add(nodeKnowledgeInstance);
			}
		return nodeKnowledgeWithInterpretations;
	}

	public List<NodeKnowledge> executeGlobalAnalyses() {
		List<NodeKnowledge> nodeKnowledge = new ArrayList<>();
		List<String> nodeNames = getAllNodeNames();
		HashMap<String, List<String>> nodeLabels = getAllNodesWithLabels();
		List<String> classEntities = getEntityClasses();

		HashMap<String, Double> betweennessCentralityAnalysisResults = executeAnalysis(betweennessCentralityQuery);
		HashMap<String, Double> closenessCentralityAnalysisResults = executeAnalysis(closenessCentralityQuery);
		HashMap<String, Double> pageRankAnalysisResults = executeAnalysis(pageRankQuery);
		HashMap<String, Double> triangleCountAnalysisResults = executeAnalysis(triangleCountQuery);
		HashMap<String, Double> triangleCoefficientAnalysisResults = executeAnalysis(triangleCoefficientQuery);

		for(String nodeName : nodeNames){
			NodeKnowledge nodeKnowledgeInstance = new NodeKnowledge();
			nodeKnowledgeInstance.setName(nodeName);
			nodeKnowledgeInstance.setLabel(nodeLabels.get(nodeName));
			nodeKnowledgeInstance.setFunctionalities(this.getFunctionalitiesForANode(nodeName));

			// global Strategies
			nodeKnowledgeInstance.setTriangleScore(triangleCountAnalysisResults.get(nodeName));
			nodeKnowledgeInstance.setTriangleCoefficientScore(triangleCoefficientAnalysisResults.get(nodeName));
			nodeKnowledgeInstance.setBetweennessCentralityScore(betweennessCentralityAnalysisResults.get(nodeName));
			nodeKnowledgeInstance.setClosenessCentralityScore(closenessCentralityAnalysisResults.get(nodeName));
			nodeKnowledgeInstance.setPageRankScore(pageRankAnalysisResults.get(nodeName));
			nodeKnowledgeInstance.setClassIsEntity(false);
			for(String classEntity : classEntities){
				if (classEntity.equals(nodeName)) {
					nodeKnowledgeInstance.setClassIsEntity(true);
				}
			}
			if(nodeKnowledgeInstance.isClassIsEntity()){
				nodeKnowledgeInstance.setRepresentedEntity(getEntity(nodeName));
			}
			List<SemanticKnowledge> semanticKnowledgePerLayer = semanticKnowledgeService.getAllSemanticKnowledge();
			List<String> keywords = new ArrayList<>();
			List<String> associatedLayers = new ArrayList<>();
			for(SemanticKnowledge semanticKnowledgeLayer: semanticKnowledgePerLayer){
				for(String keyword: semanticKnowledgeLayer.getKeywords()){
					if(nodeKnowledgeInstance.getName().toLowerCase().contains(keyword.toLowerCase())){
						if(!keywords.contains(keyword)) {
							keywords.add(keyword);
						}
						if(!associatedLayers.contains(semanticKnowledgeLayer.getName())){
							associatedLayers.add(semanticKnowledgeLayer.getName());
						}
					}
				}
				nodeKnowledgeInstance.setKeywords(keywords);
				nodeKnowledgeInstance.setAssociatedLayers(associatedLayers);
			}
			nodeKnowledge.add(nodeKnowledgeInstance);
		}
		return calculateInterpretation(nodeKnowledge);
	}

	public List<String> getFunctionalitiesForANode(String nodeName){
		List<String> functionalities = new ArrayList<>();
		try(Session session = driver.session()){

			for(Record r : session.run("MATCH (n {name:'" + nodeName + "'}) return n.imports").list()){
				if(r.get(0).isNull()) {
					functionalities.add("");
				}
				else {
					for (Object o : r.get(0).asList()) {
						functionalities.add(o.toString());
					}
				}
			}
			return functionalities;
		}
	}

	public HashMap<String, List<String>> getAllNodesWithLabels(){
		try(Session session = driver.session()){
			HashMap<String, List<String>> nodeNamesAndTypes = new HashMap<>();
			for(Record r : session.run("MATCH (n) return n, labels(n)").list()){
				List<String> labels = new ArrayList<>();
				for(Object o : r.get("labels(n)").asList()){
					labels.add(String.valueOf(o));
				}
				// add node name as key and label as value to hash map
				nodeNamesAndTypes.put(r.get("n").asNode().get("name").asString(), labels);
			}
			return nodeNamesAndTypes;

		}
	}

	public List<String> getAllNodeNames(){
		try(Session session = driver.session()) {
			return session.run("MATCH (n) return n, labels(n)").list(result -> result.get("n").asNode().get("name").asString());
		}
	}

	public List<String> getEntityClasses(){
		String query = "MATCH (n:Layer {name: \"Persistence Layer\"})\n" +
				"CALL apoc.neighbors.byhop(n, \"IS_ENTITY|BELONGS_TO\", 2)\n" +
				"YIELD nodes\n" +
				"UNWIND nodes AS m\n" +
				"WITH m\n" +
				"WHERE 'Class' IN LABELS(m)\n" +
				"return m";
		try(Session session = driver.session()) {
			return session.run(query).list(result -> result.get("m").asNode().get("name").asString());
		}
	}

	public String getEntity(String className){
		String query = "MATCH (n:Class {name:'" + className + "'})-[r:IS_ENTITY]-(p) return p";
		try(Session session = driver.session()) {
			return session.run(query).next().get("p").asNode().get("name").asString();
		}
	}

	public HashMap<String, Double> executeAnalysis(String query){
		try (Session session = driver.session()) {
			Result result = session.run(query);

			HashMap<String, Double> results = new HashMap<>();
			for (Result it = result; it.hasNext(); ) {
				Record record = it.next();
				Map<String,Object> map = record.asMap();
				results.put((String) map.get("name"), Double.valueOf(map.get("score").toString()).doubleValue());

			}
			return results;
		}
	}
}
