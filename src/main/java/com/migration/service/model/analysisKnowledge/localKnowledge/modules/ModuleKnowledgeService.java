package com.migration.service.model.analysisKnowledge.localKnowledge.modules;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ModuleKnowledgeService {
	private final ModuleKnowledgeRepository moduleKnowledgeRepository;
	private final MongoTemplate mongoTemplate;

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

	public void deleteFunctionalityBasedModules(){
		/*Query query = new Query();
		query.addCriteria(Criteria.where("splittingStrategy").is("Functionality Based Splitting "));
		List<ModuleKnowledge> moduleKnowledges = mongoTemplate.find(query, ModuleKnowledge.class);*/
		moduleKnowledgeRepository.deleteAll(moduleKnowledgeRepository.findModuleKnowledgeBySplittingStrategy( "Functionality Based " +
				"Splitting " +
				"Strategy"));
	}

	public void deleteEntityBasedModules(){
		moduleKnowledgeRepository.deleteAll(moduleKnowledgeRepository.findModuleKnowledgeBySplittingStrategy("Entity Based Splitting Strategy"));
	}

	public void deleteLouvainBasedModules(){
		moduleKnowledgeRepository.deleteAll(this.findAllLouvainBasedModules());
	}

	public List<ModuleKnowledge> findAllEntityBasedModules(){
		return moduleKnowledgeRepository.findModuleKnowledgeBySplittingStrategy("Entity Based Splitting Strategy");
	}

	public List<ModuleKnowledge> findAllFunctionalityBasedModules(){
		return moduleKnowledgeRepository.findModuleKnowledgeBySplittingStrategy("Functionality Based Splitting Strategy");
	}
	public List<ModuleKnowledge> findAllLouvainBasedModules(){
		return moduleKnowledgeRepository.findModuleKnowledgeBySplittingStrategy("Louvain");
	}
	public List<ModuleKnowledge> findModuleKnowledgeByStrategy(String strategy){
		return moduleKnowledgeRepository.findModuleKnowledgeBySplittingStrategy(strategy);
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

	public List<ModuleKnowledge> findAllFinalModules() {
		return moduleKnowledgeRepository.findModuleKnowledgeBySplittingStrategy("Manual Assignment");
	}

	public void updateByBase(String base, ModuleKnowledge updatedModuleKnowledgeInstance){
		ModuleKnowledge moduleKnowledge = this.findModuleKnowledgeByBase(base);
		this.deleteOne(moduleKnowledge);
		this.insertOne(updatedModuleKnowledgeInstance);
	}

	public void deleteModule(String base) {
		ModuleKnowledge moduleKnowledge = this.findModuleKnowledgeByBase(base);
		this.deleteOne(moduleKnowledge);
	}
}
