package com.migration.service.model.analysis.local.splittingStrategies.semanticAnalysis;

public class SemanticAnalysisExtension {
	private String layer;
	private int searchExtent;
	private String[] additionalKeywords;

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public int getSearchExtent() {
		return searchExtent;
	}

	public void setSearchExtent(int searchExtent) {
		this.searchExtent = searchExtent;
	}

	public String[] getAdditionalKeywords() {
		return additionalKeywords;
	}

	public void setAdditionalKeywords(String[] additionalKeywords) {
		this.additionalKeywords = additionalKeywords;
	}
}
