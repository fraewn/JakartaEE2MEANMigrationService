package com.migration.service.model.analysisKnowledge.ontologyKnowledge;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OntologyKnowledgeRepository extends MongoRepository<OntologyKnowledge, String> {
	public OntologyKnowledge findByJavaEEComponent(String javaEEComponent);
	public OntologyKnowledge findByAssociatedKeyword(String associatedKeyword);
	public OntologyKnowledge findByKnowledgeSource(String knowledgeSource);
}
