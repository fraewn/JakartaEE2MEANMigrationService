package com.migration.service.model.analysisKnowledge.globalKnowledge;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeKnowledgeRepository extends MongoRepository<NodeKnowledge, String> {
	public NodeKnowledge findByName(String name);
}
