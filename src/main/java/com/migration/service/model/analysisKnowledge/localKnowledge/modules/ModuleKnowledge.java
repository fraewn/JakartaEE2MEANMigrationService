package com.migration.service.model.analysisKnowledge.localKnowledge.modules;

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

	//id/name
	private String base;

	// entity or functionality
	private String usage;
	private List<String> usedModules;

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

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public List<String> getUsedModules() {
		return usedModules;
	}

	public void setUsedModules(List<String> usedModules) {
		this.usedModules = usedModules;
	}
}
