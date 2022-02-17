package com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.functionalityKnowledge;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value="functionalityKnowledge")
public class FunctionalitySplittingProfile {

	// represents the static knowledge about a JavaEE Functionality like e.g. javax.transaction in the migration context
	@Id
	private String id;
	private String name;
	private boolean excluded;
	private List<String> allowedJavaEEComponents;
	private List<String> keywords;

	// options: NONE, AS_MODULE, AS_PERSISTENCE_EXTENSION, AS_OTHER_EXTENSION
	private List<String> applicableUsage;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isExcluded() {
		return excluded;
	}

	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

	public List<String> getAllowedJavaEEComponents() {
		return allowedJavaEEComponents;
	}

	public void setAllowedJavaEEComponents(List<String> allowedJavaEEComponents) {
		this.allowedJavaEEComponents = allowedJavaEEComponents;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<String> getApplicableUsage() {
		return applicableUsage;
	}

	public void setApplicableUsage(List<String> applicableUsage) {
		this.applicableUsage = applicableUsage;
	}
}
