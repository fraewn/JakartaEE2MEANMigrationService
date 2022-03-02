package com.migration.service.controller;


import com.migration.service.model.analysisKnowledge.ontologyKnowledge.OntologyKnowledgeService;
import com.migration.service.service.codeGeneration.GenerateCode;
import com.migration.service.service.architectureGeneration.CreateMEANArchitectureGraph;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.service.JavaEEGraphService;
import lombok.AllArgsConstructor;
import org.neo4j.driver.types.Node;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/mean/architecture")
@AllArgsConstructor
public class MEANArchitectureGraphController {
	private final ModuleKnowledgeService moduleKnowledgeService;
	private JavaEEGraphService javaEEGraphService;
	private CreateMEANArchitectureGraph createMEANArchitectureGraph;
	private OntologyKnowledgeService ontologyKnowledgeService;
	private GenerateCode generateCode;

	@CrossOrigin(origins = "http://localhost:8081")
	@GetMapping("/javaEENode")
	public ResponseEntity<List<Node>> requestJavaEENode(@RequestParam String name){
		return new ResponseEntity<List<Node>>(javaEEGraphService.getNodeByName(name), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/create")
	public ResponseEntity<List<ModuleKnowledge>> requestCreateGraphCreation(){
		createMEANArchitectureGraph.createArchitecture();
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFunctionalityBasedModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/getNode")
	public ResponseEntity<List<ModuleKnowledge>> requestNode(){
		String name = "Report.java";
		createMEANArchitectureGraph.createBackendModel(name);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFunctionalityBasedModules(), HttpStatus.OK);
	}

	//@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/generate")
	public ResponseEntity<List<ModuleKnowledge>> requestCodeGeneration(@RequestParam String path){
		generateCode.executeCodeGeneration(path);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFunctionalityBasedModules(), HttpStatus.OK);
	}
}
