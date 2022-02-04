package com.migration.service.model.analysis.local.splittingStrategies.splittingByEntity;

import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.ontologyKnowledge.OntologyKnowledgeService;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledgeService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

public class EntitySplitting {
	private final SemanticKnowledgeService semanticKnowledgeService;
	private final NodeKnowledgeService nodeKnowledgeService;
	private final OntologyKnowledgeService ontologyKnowledgeService;
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
	public EntitySplitting(Driver driver, SemanticKnowledgeService semanticKnowledgeService,
						  NodeKnowledgeService nodeKnowledgeService, OntologyKnowledgeService ontologyKnowledgeService) {
		this.driver = driver;
		this.semanticKnowledgeService = semanticKnowledgeService;
		this.nodeKnowledgeService = nodeKnowledgeService;
		this.ontologyKnowledgeService = ontologyKnowledgeService;

	}
	public void executeEntitySplitting(){

	}

	public List<String> getEntityNeighbours(String entityName){
		String query = "MATCH (n:Entity {name: \'" + entityName + "\'})\n" +
				"CALL apoc.neighbors.byhop(n, \'CALLS_METHOD|IMPLEMENTS|" +
				"EXTENDS|INJECTS|IS_ENTITY\', 3) " +
				"YIELD nodes " +
				"RETURN n, nodes";
		try(Session session = driver.session()) {
			return session.run(query).list(result -> result.get("nodes").asNode().get("name").asString());
		}
	}

	public List<String> getServiceNeighbours(String serviceName){
		String query = "MATCH (n:JavaImplementation {name: \'" + serviceName + "\'}) " +
				"CALL apoc.neighbors.byhop(n, \">CALLS_METHOD|>IMPLEMENTS| EXTENDS|INJECTS|IS_ENTITY\", 1) " +
				"YIELD nodes " +
				"unwind nodes as k " +
				"return k.name";
		try(Session session = driver.session()) {
			// test necessary
			return session.run(query).list(result -> result.get("k.name").asString());
		}
	}

	public String findServiceInEntityNeighbours(List<String> entityNeighbours){
		String serviceJavaEEComponentKeyword = "";
		for(String neighbour : entityNeighbours){
			if(neighbour.equals(serviceJavaEEComponentKeyword)){
				return "";
			}
		}
		return "";
	}


}
