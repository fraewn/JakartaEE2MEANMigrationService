package com.migration.service.model.knowledgeCollection.globalKnowledge;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class NodeKnowledgeService {
	private final NodeKnowledgeRepository nodeKnowledgeRepository;

	public void insert(NodeKnowledge nodeKnowledge){
		nodeKnowledgeRepository.insert(nodeKnowledge);
	}

	public void insert(List<NodeKnowledge> nodeKnowledge){
		nodeKnowledgeRepository.insert(nodeKnowledge);
	}

	public void updateJavaEEComponents(String name, String javaEEComponent){
		NodeKnowledge nodeKnowledge = nodeKnowledgeRepository.findByName(name);
		nodeKnowledgeRepository.delete(nodeKnowledge);
		List<String> javaEEComponents = nodeKnowledge.getCalculatedInterpretation();
		javaEEComponents.add(javaEEComponent);
		nodeKnowledge.setReviewNecessary(false);
		nodeKnowledge.setCalculatedInterpretation(javaEEComponents);
		nodeKnowledgeRepository.insert(nodeKnowledge);
	}

	public NodeKnowledge findByName(String name){
		return nodeKnowledgeRepository.findByName(name);
	}

	public void deleteJavaEEComponent(String name, String javaEEComponent){
		NodeKnowledge nodeKnowledge = nodeKnowledgeRepository.findByName(name);
		nodeKnowledgeRepository.delete(nodeKnowledge);
		List<String> javaEEComponents = nodeKnowledge.getCalculatedInterpretation();
		List<String> updatedJavaEEComponents = new ArrayList<>();
		for(String component : javaEEComponents){
			if(!component.equals(javaEEComponent)){
				updatedJavaEEComponents.add(component);
			}
		}
		nodeKnowledge.setCalculatedInterpretation(updatedJavaEEComponents);
		nodeKnowledgeRepository.insert(nodeKnowledge);
	}

	public void deleteAll(){
		nodeKnowledgeRepository.deleteAll();
	}

	public void insertAll(List<NodeKnowledge> nodeKnowledge){
		nodeKnowledgeRepository.insert(nodeKnowledge);
	}

	public List<NodeKnowledge> findAll(){
		return nodeKnowledgeRepository.findAll();
	}

}