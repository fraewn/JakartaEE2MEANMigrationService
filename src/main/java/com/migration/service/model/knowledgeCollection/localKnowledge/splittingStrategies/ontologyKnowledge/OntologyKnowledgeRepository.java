package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.ontologyKnowledge;

import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OntologyKnowledgeRepository extends MongoRepository<OntologyKnowledge, String> {
	public OntologyKnowledge findByJavaEEComponent(String javaEEComponent);
}
