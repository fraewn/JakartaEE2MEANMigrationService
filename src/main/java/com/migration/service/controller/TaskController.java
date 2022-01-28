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

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/semanticAnalysis/current")
	public ResponseEntity<List<SemanticKnowledge>> getCurrentSemanticKnowledge(){
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledgeService.getAllSemanticKnowledge(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/semanticAnalysis")
	public ResponseEntity<List<SemanticKnowledge>> callSemanticAnalysis(){
		semanticKnowledgeService.deleteAll();
		List<SemanticKnowledge> semanticKnowledge = semanticAnalysis.executeSemanticAnalysis();
		semanticKnowledgeService.insert(semanticKnowledge);
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledgeService.getAllSemanticKnowledge(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/semanticAnalysisForOneLayer")
	public ResponseEntity<List<SemanticKnowledge>> callSemanticAnalysisForOneLayerWithoutAdditionalKeywords(@RequestParam String layer,
																											@RequestBody List<SemanticAnalysisExtension> semanticAnalysisExtension){
		List<SemanticKnowledge> updatedSemanticKnowledge = semanticAnalysis.executeSemanticAnalysisForOneLayer(
						semanticAnalysisExtension,
						layer);
		semanticKnowledgeService.update(updatedSemanticKnowledge);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/semanticAnalysisForOneLayerExtended")
	public ResponseEntity<List<SemanticKnowledge>> callSemanticAnalysisExtended(@RequestParam String layer,
															   @RequestBody List<SemanticAnalysisExtension> semanticAnalysisExtension){
		List<SemanticKnowledge> updatedSemanticKnowledge =
				semanticAnalysis.executeSemanticAnalysisForOneLayerExtended(semanticAnalysisExtension, layer);
		semanticKnowledgeService.update(updatedSemanticKnowledge);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@GetMapping("/globalAnalyses")
	public ResponseEntity<String> callGlobalAnalyses(){
		List<NodeKnowledge> nodeKnowledge = globalAnalysis.executeGlobalAnalyses();
		nodeKnowledgeService.insertAll(nodeKnowledge);
		return new ResponseEntity<>("Global Analyses were executed", HttpStatus.OK);
	}

}
