package com.migration.service.model.analysis.global;

import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledgeService;
import com.migration.service.model.knowledgeCollection.utilKnowledge.UtilKnowledge;
import com.migration.service.model.knowledgeCollection.utilKnowledge.UtilKnowledgeService;
import org.neo4j.driver.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GlobalAnalysis {

	private final SemanticKnowledgeService semanticKnowledgeService;
	private final UtilKnowledgeService utilKnowledgeService;
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

	private final Driver driver;
	public GlobalAnalysis(Driver driver, SemanticKnowledgeService semanticKnowledgeService, UtilKnowledgeService utilKnowledgeService) {
		this.driver = driver;
		this.semanticKnowledgeService = semanticKnowledgeService;
		this.utilKnowledgeService = utilKnowledgeService;
		this.setUp();
	}

	public void setUp(){
		// set allowed MEAN Module types (static)
		List<UtilKnowledge> utilKnowledgeList = new ArrayList<>();
		List<String> allowedMeanModuleTypes = new ArrayList<>();
		allowedMeanModuleTypes.add("cross section");
		allowedMeanModuleTypes.add("functional");
		allowedMeanModuleTypes.add("wrapper");
		UtilKnowledge utilKnowledge = new UtilKnowledge();
		utilKnowledge.setAllowedMeanModuleTypes(allowedMeanModuleTypes);
		utilKnowledgeList.add(utilKnowledge);
		utilKnowledgeService.insertAll(utilKnowledgeList);
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

			calculateInterpretation(nodeKnowledgeInstance);
			System.out.println(nodeKnowledgeInstance.getName() + " " + nodeKnowledgeInstance.getLabel() + " " + nodeKnowledgeInstance.getCalculatedInterpretation());

			nodeKnowledge.add(nodeKnowledgeInstance);
		}
		return nodeKnowledge;
	}


	public void calculateInterpretation(NodeKnowledge nodeKnowledgeInstance){
		if(nodeKnowledgeInstance.isClassIsEntity() || nodeKnowledgeInstance.containsLabel("Entity")){
			nodeKnowledgeInstance.setCalculatedInterpretation("Entity Class");
		}
		else if(nodeKnowledgeInstance.containsLabel("Entity")){
			nodeKnowledgeInstance.setCalculatedInterpretation("Entity");
		}
		else if(nodeKnowledgeInstance.containsLabel("AbstractClass")){
			nodeKnowledgeInstance.setCalculatedInterpretation("Abstract");
		}
		else if(nodeKnowledgeInstance.containsLabel("Interface")){
			nodeKnowledgeInstance.setCalculatedInterpretation("Interface");
		}
		else if(nodeKnowledgeInstance.containsLabel("Layer")){
			nodeKnowledgeInstance.setCalculatedInterpretation("Layer");
		}
		else if(nodeKnowledgeInstance.containsLabel("Resource")){
			nodeKnowledgeInstance.setCalculatedInterpretation("External Resource");
		}
		else{
			nodeKnowledgeInstance.setCalculatedInterpretation("");
		}
	}



	public List<NodeKnowledge> checkIfReviewIsNecessary(List<NodeKnowledge> nodeKnowledge){
		for(NodeKnowledge nodeKnowledgeInstance : nodeKnowledge){
			nodeKnowledgeInstance.setReviewNecessary(false);
			if(nodeKnowledgeInstance.getTriangleScore() > 10 || nodeKnowledgeInstance.getTriangleScore() >= 0.5
			|| nodeKnowledgeInstance.getBetweennessCentralityScore() >= 10 || nodeKnowledgeInstance.getClosenessCentralityScore() >= 0.38){
				if(!nodeKnowledgeInstance.containsLabel("AbstractClass") && nodeKnowledgeInstance.containsLabel("Interface")){
					nodeKnowledgeInstance.setReview("Abstract or Interface");
				}
				else if(nodeKnowledgeInstance.containsLabel("Entity")) {
					nodeKnowledgeInstance.setReview("Entity");
				}
				else if(nodeKnowledgeInstance.containsLabel("Layer")){
					nodeKnowledgeInstance.setReview("Layer");
				}
				else {
					nodeKnowledgeInstance.setReviewNecessary(true);
					nodeKnowledgeInstance.setReview("Review necessary");
				}
			}
		}

		return nodeKnowledge;
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

	/*public HashMap<String, TriangleCountResult> executeTriangleCount() {
		try (Session session = driver.session()) {
			Result result = session.run("CALL algo.triangleCount.stream(null, null, {concurrency:8}) " +
					"YIELD nodeId, triangles, coefficient " +
					"return algo.getNodeById(nodeId).name as name, triangles, coefficient");

			HashMap<String, TriangleCountResult> triangleResults = new HashMap<>();
			for (Result it = result; it.hasNext(); ) {
				Record record = it.next();
				Map<String,Object> map = record.asMap();
				triangleResults.put((String) map.get("name"), new TriangleCountResult(Double.valueOf(map.get("triangles").toString()).doubleValue(),
						Double.valueOf(map.get("coefficient").toString()).doubleValue()));
			}
			return triangleResults;
		}
	}*/


}
