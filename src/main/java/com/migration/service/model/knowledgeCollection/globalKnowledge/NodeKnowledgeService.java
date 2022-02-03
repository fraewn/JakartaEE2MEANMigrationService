package com.migration.service.model.knowledgeCollection.globalKnowledge;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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