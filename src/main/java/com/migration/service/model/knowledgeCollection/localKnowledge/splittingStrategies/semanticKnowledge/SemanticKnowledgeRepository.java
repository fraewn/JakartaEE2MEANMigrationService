package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemanticKnowledgeRepository extends MongoRepository<SemanticKnowledge, String> {

}
