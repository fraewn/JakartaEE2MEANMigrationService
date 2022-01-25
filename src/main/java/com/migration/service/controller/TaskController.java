package com.migration.service.controller;


import com.migration.service.model.analysis.local.splittingStrategies.semanticAnalysis.SemanticAnalysis;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledgeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tasks")
@AllArgsConstructor
public class TaskController {
	private final SemanticKnowledgeService semanticKnowledgeService;
	private final SemanticAnalysis semanticAnalysis;

	@GetMapping("/semanticKnowledge/show")
	@ResponseBody
	public List<SemanticKnowledge> fetchAllSemanticKnowledge(){
		List<SemanticKnowledge> semanticKnowledgeList = semanticKnowledgeService.getAllSemanticKnowledge();
		if(semanticKnowledgeList.size() > 0 ){
			System.out.println("found one!");
		}
		// automatically converts List to JSON and sends as http response
		return semanticKnowledgeList;
	}

	@PostMapping("/semanticKnowledge/insert")
	public ResponseEntity<String> insertSemanticKnowledge(@RequestBody SemanticKnowledge semanticKnowledge){
		semanticKnowledgeService.insert(semanticKnowledge);
		return new ResponseEntity<>("Inserting semantic knowledge was successful", HttpStatus.OK);
	}

	@GetMapping("/semanticAnalysis")
	public ResponseEntity<String> callSemanticAnalysis(){
		semanticAnalysis.executeSemanticAnalysis();
		return new ResponseEntity<>("Semantic Analysis was executed", HttpStatus.OK);
	}

}
