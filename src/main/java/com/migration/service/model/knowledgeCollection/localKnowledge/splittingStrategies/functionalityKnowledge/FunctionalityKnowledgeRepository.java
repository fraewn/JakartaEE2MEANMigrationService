package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.functionalityKnowledge;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FunctionalityKnowledgeRepository extends MongoRepository<FunctionalityKnowledge, String> {
}
