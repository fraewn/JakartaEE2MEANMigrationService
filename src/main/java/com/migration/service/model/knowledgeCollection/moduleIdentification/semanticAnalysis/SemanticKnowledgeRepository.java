package com.migration.service.model.knowledgeCollection.moduleIdentification.semanticAnalysis;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemanticKnowledgeRepository extends MongoRepository<SemanticKnowledge, String> {

}
