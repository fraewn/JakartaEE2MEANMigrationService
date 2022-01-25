package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.functionalityKnowledge;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value="functionalityKnowledge")
public class FunctionalityKnowledge {

	// represents the static knowledge about a JavaEE Functionality like e.g. javax.transaction in the migration context
	@Id
	private String id;
	private String name;
	private ApplicableUsage usageForFunctionality;
	private String questions;
	private String[] searchGroup;

}
