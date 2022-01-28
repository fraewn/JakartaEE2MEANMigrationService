package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge;


import com.migration.service.model.analysis.local.splittingStrategies.semanticAnalysis.SemanticAnalysisExtension;
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

	public void updateKeywordsPerLayer(String layer, List<SemanticAnalysisExtension> semanticAnalysisExtensions){
		List<SemanticKnowledge> current = getAllSemanticKnowledge();
		for(SemanticKnowledge currentSemanticKnowledgeInstance : current){
			if(layer.equals(currentSemanticKnowledgeInstance.getName())){
				for(SemanticAnalysisExtension semanticAnalysisExtension : semanticAnalysisExtensions){
					if(layer.equals(semanticAnalysisExtension.getLayer())){
						List<String> oldKeywords = currentSemanticKnowledgeInstance.getKeywords();
						oldKeywords.addAll(semanticAnalysisExtension.getAdditionalKeywords());
					}
				}
			}
		}
		semanticKnowledgeRepository.deleteAll();
		semanticKnowledgeRepository.insert(current);
	}

	public void updateAllKeywords(List<SemanticAnalysisExtension> semanticAnalysisExtensions){
		List<SemanticKnowledge> current = getAllSemanticKnowledge();
		for(SemanticKnowledge currentSemanticKnowledgeInstance : current){
				for(SemanticAnalysisExtension semanticAnalysisExtension : semanticAnalysisExtensions){
					if(currentSemanticKnowledgeInstance.getName().equals(semanticAnalysisExtension.getLayer())){
						List<String> oldKeywords = currentSemanticKnowledgeInstance.getKeywords();
						oldKeywords.addAll(semanticAnalysisExtension.getAdditionalKeywords());
					}
				}
			}
		semanticKnowledgeRepository.deleteAll();
		semanticKnowledgeRepository.insert(current);
	}



	public void deleteAll() {
		semanticKnowledgeRepository.deleteAll();
	}
}
