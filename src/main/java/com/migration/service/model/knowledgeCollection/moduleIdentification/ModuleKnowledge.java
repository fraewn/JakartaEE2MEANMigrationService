package com.migration.service.model.knowledgeCollection.moduleIdentification;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value="moduleKnowledge")
public class ModuleKnowledge {

	// represents a cluster/ module
	@Id
	private String id;
	private int timesExecuted;
	private String[] moduleCluster;
	private SplittingStrategy splittingStrategy;
	private String[] keywords;
	// timestamp
}
