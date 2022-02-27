package com.migration.service.controller;

import com.migration.service.service.analysis.splittingStrategies.splittingByFunctionality.FunctionalitySplitting;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/splitting/functionality")
@AllArgsConstructor
public class FunctionalitySplittingController {
	private final FunctionalitySplitting functionalitySplitting;
	private final ModuleKnowledgeService moduleKnowledgeService;

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute")
	public ResponseEntity<List<ModuleKnowledge>> requestExecuteEntitySplitting(){
		moduleKnowledgeService.deleteFunctionalityBasedModules();
		functionalitySplitting.executeFunctionalitySplittingStrategy();
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFunctionalityBasedModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute/result")
	public ResponseEntity<List<ModuleKnowledge>> requestEntitySplittingResults(){
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFunctionalityBasedModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute/result/delete/component")
	public ResponseEntity<List<ModuleKnowledge>> requestDeleteComponentInModule(@RequestParam String component, String base){
		moduleKnowledgeService.deleteComponentInModule(base, component);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFunctionalityBasedModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute/result/delete/module")
	public ResponseEntity<List<ModuleKnowledge>> requestDeleteModule(@RequestParam String base){
		moduleKnowledgeService.deleteModule(base);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFunctionalityBasedModules(), HttpStatus.OK);
	}
}
