package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.entitySplitting;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value="entitySplittingProfile")
public class EntitySplittingProfile {
	@Id
	String id;
	int searchDepth;
	List<String> filteredJavaEEComponents;
	List<String> allowedJavaEEComponents;
	String centralJavaEEComponent;
	String substitutionalCentralJavaEEComponent;

	public int getSearchDepth() {
		return searchDepth;
	}

	public void setSearchDepth(int searchDepth) {
		this.searchDepth = searchDepth;
	}

	public List<String> getFilteredJavaEEComponents() {
		return filteredJavaEEComponents;
	}

	public void setFilteredJavaEEComponents(List<String> filteredJavaEEComponents) {
		this.filteredJavaEEComponents = filteredJavaEEComponents;
	}

	public List<String> getAllowedJavaEEComponents() {
		return allowedJavaEEComponents;
	}

	public void setAllowedJavaEEComponents(List<String> allowedJavaEEComponents) {
		this.allowedJavaEEComponents = allowedJavaEEComponents;
	}

	public String getCentralJavaEEComponent() {
		return centralJavaEEComponent;
	}

	public void setCentralJavaEEComponent(String centralJavaEEComponent) {
		this.centralJavaEEComponent = centralJavaEEComponent;
	}

	public String getSubstitutionalCentralJavaEEComponent() {
		return substitutionalCentralJavaEEComponent;
	}

	public void setSubstitutionalCentralJavaEEComponent(String substitutionalCentralJavaEEComponent) {
		this.substitutionalCentralJavaEEComponent = substitutionalCentralJavaEEComponent;
	}
}
