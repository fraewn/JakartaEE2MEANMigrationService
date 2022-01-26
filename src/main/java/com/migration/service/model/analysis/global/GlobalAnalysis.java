package com.migration.service.model.analysis.global;

import com.migration.service.model.analysis.Util.TriangleCountResult;
import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledge;
import org.neo4j.driver.*;
import org.neo4j.driver.util.Pair;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GlobalAnalysis {

	private List<Pair<String, Value>> triangleCountResults;

	private final Driver driver;
	public GlobalAnalysis(Driver driver) {
		this.driver = driver;
	}
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

	public void executeGlobalAnalyses() {
		List<String> nodeNames = getAllNodeNames();
		HashMap<String, List<String>> nodeLabels = getAllNodesWithLabels();
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
