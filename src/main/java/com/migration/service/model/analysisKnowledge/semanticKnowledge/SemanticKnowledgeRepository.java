package com.migration.service.model.analysisKnowledge.semanticKnowledge;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemanticKnowledgeRepository extends MongoRepository<SemanticKnowledge, String> {
	public SemanticKnowledge findByName(String name);
}
