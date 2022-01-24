package com.migration.service.model.knowledgeCollection.moduleIdentification.functionalitySplittingStrategy;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FunctionalityKnowledgeRepository extends MongoRepository<FunctionalityKnowledge, String> {
}
