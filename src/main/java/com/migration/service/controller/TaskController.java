package com.migration.service.controller;


import com.migration.service.model.analysis.global.GlobalAnalysis;
import com.migration.service.model.analysis.local.splittingStrategies.semanticAnalysis.SemanticAnalysis;
import com.migration.service.model.analysis.local.splittingStrategies.semanticAnalysis.SemanticAnalysisExtension;
import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledge;
import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledgeService;
import lombok.AllArgsConstructor;
import org.apache.tomcat.jni.Global;
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
	private final GlobalAnalysis globalAnalysis;
	private final NodeKnowledgeService nodeKnowledgeService;

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

	@PostMapping("/extendedSemanticAnalysis")
	public ResponseEntity<String> callSemanticAnalysisExtended(@RequestBody SemanticAnalysisExtension semanticAnalysisExtension){
		// TODO wenn ein analyse auftrag rein kommt, wird dieser:
		// 1) ausgeführt
		// 2) in der DB persistiert
		// 3) eine mongo query abgeschickt die die aktualisierten Daten holt
		// 4) die aktualisierten Daten mit diesem Request hier zurück gegeben
		// sonst muss im frontend immer die page aktualisiert werden
		semanticAnalysis.executeSemanticAnalysisExtended(semanticAnalysisExtension);
		return new ResponseEntity<>("Extended semantic Analysis was executed", HttpStatus.OK);
	}

	@GetMapping("/globalAnalyses")
	public ResponseEntity<String> callGlobalAnalyses(){
		List<NodeKnowledge> nodeKnowledge = globalAnalysis.executeGlobalAnalyses();
		nodeKnowledgeService.insertAll(nodeKnowledge);
		return new ResponseEntity<>("Global Analyses were executed", HttpStatus.OK);
	}

}
