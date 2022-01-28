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

	public void insert(List<SemanticKnowledge> semanticKnowledge) {
		semanticKnowledgeRepository.insert(semanticKnowledge);
	}

	public void update(List<SemanticKnowledge> updatedSemanticKnowledge){
		List<SemanticKnowledge> current = getAllSemanticKnowledge();
		SemanticKnowledge updatedSemanticKnowledgeInstance = updatedSemanticKnowledge.get(0);
		for(SemanticKnowledge currentSemanticKnowledgeInstance : current){
			if(currentSemanticKnowledgeInstance.getName().equals(updatedSemanticKnowledgeInstance.getName())){
				currentSemanticKnowledgeInstance.setKeywords(updatedSemanticKnowledgeInstance.getKeywords());
			}
		}
		semanticKnowledgeRepository.deleteAll();
		semanticKnowledgeRepository.insert(current);
	}

	public void deleteAll() {
		semanticKnowledgeRepository.deleteAll();
	}
}
