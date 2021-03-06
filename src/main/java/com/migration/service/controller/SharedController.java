package com.migration.service.controller;

import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.service.analysis.global.GlobalAnalysis;
import com.migration.service.service.analysis.semanticAnalysis.SemanticAnalysis;
import com.migration.service.service.analysis.semanticAnalysis.SemanticAnalysisExtension;
import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledge;
import com.migration.service.model.analysisKnowledge.globalKnowledge.NodeKnowledgeService;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledge;
import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledgeService;
import com.migration.service.model.analysisKnowledge.semanticKnowledge.SemanticKnowledge;
import com.migration.service.model.analysisKnowledge.semanticKnowledge.SemanticKnowledgeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tasks")
@AllArgsConstructor
public class SharedController {
	private final SemanticKnowledgeService semanticKnowledgeService;
	private final SemanticAnalysis semanticAnalysis;
	private final GlobalAnalysis globalAnalysis;
	private final NodeKnowledgeService nodeKnowledgeService;
	private final OntologyKnowledgeService ontologyKnowledgeService;
	private final ModuleKnowledgeService moduleKnowledgeService;

	// only for testing
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

	// only for testing
	@PostMapping("/semanticKnowledge/insert")
	public ResponseEntity<String> insertSemanticKnowledge(@RequestBody SemanticKnowledge semanticKnowledge){
		semanticKnowledgeService.insert(semanticKnowledge);
		return new ResponseEntity<>("Inserting semantic knowledge was successful", HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/semanticAnalysis/current")
	public ResponseEntity<List<SemanticKnowledge>> getCurrentSemanticKnowledge(){
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
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
		semanticKnowledgeService.updateOneLayer(updatedSemanticKnowledge);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/semanticAnalysisForOneLayerExtended")
	public ResponseEntity<List<SemanticKnowledge>> callSemanticAnalysisExtended(@RequestParam String layer,
															   @RequestBody List<SemanticAnalysisExtension> semanticAnalysisExtension){
		List<SemanticKnowledge> updatedSemanticKnowledge =
				semanticAnalysis.executeSemanticAnalysisForOneLayerExtended(semanticAnalysisExtension, layer);
		semanticKnowledgeService.updateOneLayer(updatedSemanticKnowledge);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/semanticAnalysisSaveAll")
	public ResponseEntity<List<SemanticKnowledge>> saveAll(@RequestParam String layer,
																				@RequestBody List<SemanticAnalysisExtension> semanticAnalysisExtension){
		semanticKnowledgeService.updateKeywordsPerLayer(layer, semanticAnalysisExtension);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@DeleteMapping("/semanticAnalysis/deleteLayer")
	public ResponseEntity<List<SemanticKnowledge>> semanticAnalysisdeleteLayer(@RequestParam String layer){
		semanticKnowledgeService.deleteLayer(layer);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	//@CrossOrigin(origins = "http://localhost:4200")
	@DeleteMapping("/semanticAnalysis/deleteAll")
	public ResponseEntity<List<SemanticKnowledge>> semanticAnalysisdeleteAll(){
		semanticKnowledgeService.deleteAll();
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@DeleteMapping("/semanticAnalysis/deleteKeywordInLayer")
	public ResponseEntity<List<SemanticKnowledge>> semanticAnalysisdeleteKeywordInLayer(@RequestParam String layer, String keyword){
		semanticKnowledgeService.deleteKeywordInLayer(layer, keyword);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/semanticAnalysis/moveKeyword")
	public ResponseEntity<List<SemanticKnowledge>> semanticAnalysisMoveKeyword(@RequestParam String oldLayer,
																						String newLayer, String keyword){
		semanticKnowledgeService.moveKeyword(keyword, oldLayer, newLayer);
		List<SemanticKnowledge> semanticKnowledge = semanticKnowledgeService.getAllSemanticKnowledge();
		return new ResponseEntity<List<SemanticKnowledge>>(semanticKnowledge, HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/globalAnalyses")
	public ResponseEntity<List<NodeKnowledge>> callGlobalAnalyses(){
		List<NodeKnowledge> nodeKnowledge = globalAnalysis.executeGlobalAnalyses();
		nodeKnowledgeService.deleteAll();
		nodeKnowledgeService.insertAll(nodeKnowledge);
		return new ResponseEntity<List<NodeKnowledge>>(nodeKnowledgeService.findAll(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/globalAnalyses/knowledge")
	public ResponseEntity<List<NodeKnowledge>> requestNodeKnowledge(){
		return new ResponseEntity<List<NodeKnowledge>>(nodeKnowledgeService.findAll(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/ontologyKnowledge")
	public ResponseEntity<List<OntologyKnowledge>> getOntologyKnowledge(){
		return new ResponseEntity<List<OntologyKnowledge>>(ontologyKnowledgeService.findAll(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/ontologyKnowledge/javaEEcomponents")
	public ResponseEntity<List<String>> getAllJavaEEComponents(){
		return new ResponseEntity<List<String>>(ontologyKnowledgeService.getAllJavaEEComponents(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/nodeKnowledge/addJavaEEComponent")
	public ResponseEntity<List<NodeKnowledge>> addJavaEEComponent(@RequestParam String name, String javaEEComponent){
		nodeKnowledgeService.updateJavaEEComponents(name, javaEEComponent);
		return new ResponseEntity<List<NodeKnowledge>>(nodeKnowledgeService.findAll(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/nodeKnowledge/deleteJavaEEComponent")
	public ResponseEntity<List<NodeKnowledge>> deleteJavaEEComponent(@RequestParam String name, String javaEEComponent){
		nodeKnowledgeService.deleteJavaEEComponent(name, javaEEComponent);
		return new ResponseEntity<List<NodeKnowledge>>(nodeKnowledgeService.findAll(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/ontologyKnowledge/associateKeyword")
	public ResponseEntity<List<OntologyKnowledge>> semanticAnalysisMoveKeyword(@RequestParam String keyword, String javaEEComponent){
		ontologyKnowledgeService.associateKeyword(keyword, javaEEComponent);
		List<OntologyKnowledge> ontologyKnowledge = ontologyKnowledgeService.findAll();
		return new ResponseEntity<List<OntologyKnowledge>>(ontologyKnowledge, HttpStatus.OK);
	}

	@GetMapping("/test")
	public ResponseEntity<List<String>> semanticAnalysisMoveKeyword(@RequestParam String nodeName){
		List<String> funcs = globalAnalysis.getFunctionalitiesForANode(nodeName);
		return new ResponseEntity<List<String>>(funcs, HttpStatus.OK);
	}


	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/globalAnalyses/louvain")
	public ResponseEntity<List<ModuleKnowledge>> callLouvain(){
		List<ModuleKnowledge> moduleKnowledges = globalAnalysis.calculateLouvainClusters();
		moduleKnowledgeService.deleteLouvainBasedModules();
		moduleKnowledgeService.insertAll(moduleKnowledges);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllLouvainBasedModules(), HttpStatus.OK);
	}







}
