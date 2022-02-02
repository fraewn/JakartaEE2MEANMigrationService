package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.ontologyKnowledge;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Member;

@Document(value="ontologyKnowledge")
public class OntologyKnowledge {
	@Id
	String id;
	String layer;
	String knowledgeSource;
	String javaEEComponent;
	String description;
	String associatedKeyword;
	String MEANLocation;
	String MEANComponent;
	boolean externalLibraryNecessary;
	String defaultLibrary;

	public OntologyKnowledge(){}

	public OntologyKnowledge(String layer, String knowledgeSource, String javaEEComponent, String description, String associatedKeyword,
							 String MEANLocation, String MEANComponent, boolean externalLibraryNecessary, String defaultLibrary) {
		this.layer = layer;
		this.knowledgeSource = knowledgeSource;
		this.javaEEComponent = javaEEComponent;
		this.description = description;
		this.associatedKeyword = associatedKeyword;
		this.MEANLocation = MEANLocation;
		this.MEANComponent = MEANComponent;
		this.externalLibraryNecessary = externalLibraryNecessary;
		this.defaultLibrary = defaultLibrary;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public String getKnowledgeSource() {
		return knowledgeSource;
	}

	public void setKnowledgeSource(String knowledgeSource) {
		this.knowledgeSource = knowledgeSource;
	}

	public String getJavaEEComponent() {
		return javaEEComponent;
	}

	public void setJavaEEComponent(String javaEEComponent) {
		this.javaEEComponent = javaEEComponent;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAssociatedKeyword() {
		return associatedKeyword;
	}

	public void setAssociatedKeyword(String associatedKeyword) {
		this.associatedKeyword = associatedKeyword;
	}

	public String getMEANLocation() {
		return MEANLocation;
	}

	public void setMEANLocation(String MEANLocation) {
		this.MEANLocation = MEANLocation;
	}

	public String getMEANComponent() {
		return MEANComponent;
	}

	public void setMEANComponent(String MEANComponent) {
		this.MEANComponent = MEANComponent;
	}

	public boolean isExternalLibraryNecessary() {
		return externalLibraryNecessary;
	}

	public void setExternalLibraryNecessary(boolean externalLibraryNecessary) {
		this.externalLibraryNecessary = externalLibraryNecessary;
	}

	public String getDefaultLibrary() {
		return defaultLibrary;
	}

	public void setDefaultLibrary(String defaultLibrary) {
		this.defaultLibrary = defaultLibrary;
	}
}
