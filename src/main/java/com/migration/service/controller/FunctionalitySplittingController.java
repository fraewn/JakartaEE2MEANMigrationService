package com.migration.service.controller;

import com.migration.service.model.analysis.local.splittingStrategies.splittingByFunctionality.FunctionalitySplitting;
import com.migration.service.model.knowledgeCollection.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.modules.ModuleKnowledgeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/splitting/functionality")
@AllArgsConstructor
public class FunctionalitySplittingController {
	private final FunctionalitySplitting functionalitySplitting;
	private final ModuleKnowledgeService moduleKnowledgeService;

	//@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute")
	public ResponseEntity<List<ModuleKnowledge>> requestExecuteEntitySplitting(){
		return new ResponseEntity<List<ModuleKnowledge>>(functionalitySplitting.executeFunctionalitySplittingStrategy(), HttpStatus.OK);
	}
}
