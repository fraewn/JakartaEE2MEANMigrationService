package com.migration.service.model.knowledgeCollection.moduleIdentification.semanticAnalysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemanticKnowledgeDAO {
	@Autowired
	SemanticKnowledgeRepository semanticKnowledgeRepository;

	public void createSemanticKnowledge(SemanticKnowledge semanticKnowledge){
		semanticKnowledgeRepository.insert(semanticKnowledge);
	}

	public List<SemanticKnowledge> getAllSemanticKnowledge(){
		return semanticKnowledgeRepository.findAll();
	}
}
