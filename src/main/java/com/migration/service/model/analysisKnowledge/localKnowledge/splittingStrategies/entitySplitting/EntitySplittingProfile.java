package com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.entitySplitting;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value="entitySplittingProfile")
public class EntitySplittingProfile {
	@Id
	String id;
	int searchDepth;
	List<String> filteredJakartaEEComponents;
	List<String> allowedJakartaEEComponents;
	String centralJakartaEEComponent;
	String substitutionalCentralJakartaEEComponent;

	public int getSearchDepth() {
		return searchDepth;
	}

	public void setSearchDepth(int searchDepth) {
		this.searchDepth = searchDepth;
	}

	public List<String> getFilteredJakartaEEComponents() {
		return filteredJakartaEEComponents;
	}

	public void setFilteredJakartaEEComponents(List<String> filteredJakartaEEComponents) {
		this.filteredJakartaEEComponents = filteredJakartaEEComponents;
	}

	public List<String> getAllowedJakartaEEComponents() {
		return allowedJakartaEEComponents;
	}

	public void setAllowedJakartaEEComponents(List<String> allowedJakartaEEComponents) {
		this.allowedJakartaEEComponents = allowedJakartaEEComponents;
	}

	public String getCentralJakartaEEComponent() {
		return centralJakartaEEComponent;
	}

	public void setCentralJakartaEEComponent(String centralJakartaEEComponent) {
		this.centralJakartaEEComponent = centralJakartaEEComponent;
	}

	public String getSubstitutionalCentralJakartaEEComponent() {
		return substitutionalCentralJakartaEEComponent;
	}

	public void setSubstitutionalCentralJakartaEEComponent(String substitutionalCentralJakartaEEComponent) {
		this.substitutionalCentralJakartaEEComponent = substitutionalCentralJakartaEEComponent;
	}
}
