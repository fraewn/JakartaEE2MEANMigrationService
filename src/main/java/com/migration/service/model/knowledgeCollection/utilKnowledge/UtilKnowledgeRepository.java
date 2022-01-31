package com.migration.service.model.knowledgeCollection.utilKnowledge;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilKnowledgeRepository extends MongoRepository<UtilKnowledge, String>  {
}
