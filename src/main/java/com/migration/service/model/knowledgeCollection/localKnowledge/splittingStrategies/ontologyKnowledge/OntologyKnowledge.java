package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.ontologyKnowledge;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value="ontologyKnowledge")
public class OntologyKnowledge {
	@Id
	String id;
	String layer;
	String javaEEComponent;
	String description;
	String associatedKeyword;

	public OntologyKnowledge(){}

	public OntologyKnowledge(String layer, String javaEEComponent, String description, String associatedKeyword) {
		this.layer = layer;
		this.javaEEComponent = javaEEComponent;
		this.description = description;
		this.associatedKeyword = associatedKeyword;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
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
}
