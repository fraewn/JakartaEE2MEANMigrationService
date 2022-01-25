package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SemanticKnowledgeService {
	private final SemanticKnowledgeRepository semanticKnowledgeRepository;

	public List<SemanticKnowledge> getAllSemanticKnowledge(){
		return semanticKnowledgeRepository.findAll();
	}

	public void insert(SemanticKnowledge semanticKnowledge) {
		semanticKnowledgeRepository.insert(semanticKnowledge);
	}
}
