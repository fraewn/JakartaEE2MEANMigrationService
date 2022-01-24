package com.migration.service.model.knowledgeCollection;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value="nodeKnowledge")
public class NodeKnowledge {
	// represents the knowledge about a architecture element/ node collected during the migration strategies' executions
	@Id
	private String id;
	private NodeType type;
	private double triangleScore;
	private double triangleCoefficientScore;
	private double betweenessCentralityScore;
	private double pageRankScore;
	private double closenessCentralityScore;
	private boolean classIsEntity;
	private String representedEntity;
	private boolean reviewNecessary;
	private boolean review;
	private String[] keywords;
}
