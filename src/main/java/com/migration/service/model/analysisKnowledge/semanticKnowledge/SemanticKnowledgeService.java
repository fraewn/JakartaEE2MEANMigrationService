package com.migration.service.model.analysisKnowledge.semanticKnowledge;


import com.migration.service.service.analysis.semanticAnalysis.SemanticAnalysisExtension;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

	// updatedSemanticKnowledge contains only one entry, the changed knowledge for the layer to be updated
	public void updateOneLayer(List<SemanticKnowledge> updatedSemanticKnowledge){
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

	public void deleteKeywordInLayer(String layer, String keyword){
		SemanticKnowledge semanticKnowledgeInstance = semanticKnowledgeRepository.findByName(layer);
		semanticKnowledgeRepository.delete(semanticKnowledgeInstance);
		List<String> updatedKeywords = new ArrayList<>();
		for(String word : semanticKnowledgeInstance.getKeywords()){
			if(!keyword.equals(word)){
				updatedKeywords.add(word);
			}
		}
		semanticKnowledgeInstance.setKeywords(updatedKeywords);
		semanticKnowledgeRepository.insert(semanticKnowledgeInstance);
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

	public void deleteLayer(String layer){
		SemanticKnowledge semanticKnowledgeForLayer =
				semanticKnowledgeRepository.findByName(layer);
		semanticKnowledgeRepository.delete(semanticKnowledgeForLayer);
	}


	public void deleteAll() {
		semanticKnowledgeRepository.deleteAll();
	}

	public void moveKeyword(String keyword, String oldLayer, String newLayer) {
		this.deleteKeywordInLayer(oldLayer, keyword);
		this.addKeywordToLayer(keyword, newLayer);
	}

	public void addKeywordToLayer(String newKeyword, String layer){
		SemanticKnowledge semanticKnowledgeInstance = semanticKnowledgeRepository.findByName(layer);
		semanticKnowledgeRepository.delete(semanticKnowledgeInstance);
		List<String> updatedKeywords = new ArrayList<>();
		updatedKeywords.addAll(semanticKnowledgeInstance.getKeywords());
		updatedKeywords.add(newKeyword);
		semanticKnowledgeInstance.setKeywords(updatedKeywords);
		semanticKnowledgeRepository.insert(semanticKnowledgeInstance);
	}
}
