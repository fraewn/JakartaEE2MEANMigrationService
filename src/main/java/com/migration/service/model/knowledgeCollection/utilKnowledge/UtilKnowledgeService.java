package com.migration.service.model.knowledgeCollection.utilKnowledge;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class UtilKnowledgeService {
	private final UtilKnowledgeRepository utilKnowledgeRepository;

	public List<UtilKnowledge> findAll(){
		return utilKnowledgeRepository.findAll();
	}

	public void insertAll(List<UtilKnowledge> utilKnowledgeInstances){
		utilKnowledgeRepository.insert(utilKnowledgeInstances);
	}
}
