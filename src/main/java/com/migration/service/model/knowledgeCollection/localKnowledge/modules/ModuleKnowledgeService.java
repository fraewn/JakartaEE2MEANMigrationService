package com.migration.service.model.knowledgeCollection.localKnowledge.modules;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ModuleKnowledgeService {
	private final ModuleKnowledgeRepository moduleKnowledgeRepository;
	public List<ModuleKnowledge> findAll(){
		return moduleKnowledgeRepository.findAll();
	}

	public void insertOne(ModuleKnowledge moduleKnowledge){
		moduleKnowledgeRepository.insert(moduleKnowledge);
	}

	public void insertAll(List<ModuleKnowledge> moduleKnowledge){
		moduleKnowledgeRepository.insert(moduleKnowledge);
	}

	public void deleteAll(){
		moduleKnowledgeRepository.deleteAll();
	}

	public void deleteOne(ModuleKnowledge moduleKnowledge){
		moduleKnowledgeRepository.delete(moduleKnowledge);
	}

	public ModuleKnowledge findModuleKnowledgeByBase(String base){
		return moduleKnowledgeRepository.findModuleKnowledgeByBase(base);
	}

	public void deleteComponentInModule(String base, String component){
		ModuleKnowledge moduleKnowledge = findModuleKnowledgeByBase(base);
		deleteOne(moduleKnowledge);
		moduleKnowledge.getModuleCluster().remove(component);
		insertOne(moduleKnowledge);
	}
}
