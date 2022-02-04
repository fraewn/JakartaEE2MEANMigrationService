package com.migration.service.model.knowledgeCollection.localKnowledge.modules;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value="moduleKnowledge")
public class ModuleKnowledge {

	// represents a cluster/ module
	@Id
	private String id;

	private List<String> moduleCluster;
	private String splittingStrategy;
	// entity or functionality:
	private String base;

	public List<String> getModuleCluster() {
		return moduleCluster;
	}

	public void setModuleCluster(List<String> moduleCluster) {
		this.moduleCluster = moduleCluster;
	}

	public String getSplittingStrategy() {
		return splittingStrategy;
	}

	public void setSplittingStrategy(String splittingStrategy) {
		this.splittingStrategy = splittingStrategy;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}
}
