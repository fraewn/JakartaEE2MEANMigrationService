package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value="semanticKnowledge")
public class SemanticKnowledge {

	// represents the keywords/strings associated with a certain layer
	@Id
	private String id;
	private String name;
	private List<String> keywords;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}



}
