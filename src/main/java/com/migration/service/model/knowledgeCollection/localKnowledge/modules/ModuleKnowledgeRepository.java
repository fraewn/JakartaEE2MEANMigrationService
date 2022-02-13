package com.migration.service.model.knowledgeCollection.localKnowledge.modules;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleKnowledgeRepository extends MongoRepository<ModuleKnowledge, String> {
	public ModuleKnowledge findModuleKnowledgeByBase(String base);
	public List<ModuleKnowledge> findModuleKnowledgeBySplittingStrategy(String base);
}
