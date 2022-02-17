package com.migration.service.controller;
import com.migration.service.service.analysis.local.splittingStrategies.splittingByEntity.EntitySplitting;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.entitySplitting.EntitySplittingProfile;
import com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.entitySplitting.EntitySplittingProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/splitting/entity")
@AllArgsConstructor
public class EntitySplittingController {
	private final EntitySplittingProfileService entitySplittingProfileService;
	private final EntitySplitting entitySplitting;
	private final ModuleKnowledgeService moduleKnowledgeService;

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/profile/current")
	public ResponseEntity<List<EntitySplittingProfile>> requestAllEntitySplittingProfiles(){
		return new ResponseEntity<List<EntitySplittingProfile>>(entitySplittingProfileService.findAll(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/profile/insert")
	public ResponseEntity<List<EntitySplittingProfile>> requestInsertEntitySplittingProfile(@RequestBody EntitySplittingProfile entitySplittingProfile){
		entitySplittingProfileService.insertOne(entitySplittingProfile);
		return new ResponseEntity<List<EntitySplittingProfile>>(entitySplittingProfileService.findAll(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/profile/update")
	public ResponseEntity<List<EntitySplittingProfile>> requestUpdateEntitySplittingProfile(@RequestBody List<EntitySplittingProfile> entitySplittingProfiles){
		entitySplittingProfileService.update(entitySplittingProfiles.get(0));
		return new ResponseEntity<List<EntitySplittingProfile>>(entitySplittingProfileService.findAll(), HttpStatus.OK);
	}

	//@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/profile/delete")
	public ResponseEntity<String> requestDeleteEntitySplittingProfiles(){
		entitySplittingProfileService.deleteAll();
		return new ResponseEntity<>("Deleting all entity splitting profiles was successful", HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute")
	public ResponseEntity<List<ModuleKnowledge>> requestExecuteEntitySplitting(){
		moduleKnowledgeService.deleteEntityBasedModules();
		entitySplitting.executeEntitySplitting();
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllEntityBasedModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute/result")
	public ResponseEntity<List<ModuleKnowledge>> requestEntitySplittingResults(){
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllEntityBasedModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute/result/delete/component")
	public ResponseEntity<List<ModuleKnowledge>> requestDeleteComponentInModule(@RequestParam String component, String base){
		moduleKnowledgeService.deleteComponentInModule(base, component);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllEntityBasedModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/execute/result/delete/module")
	public ResponseEntity<List<ModuleKnowledge>> requestDeleteModule(@RequestParam String base){
		moduleKnowledgeService.deleteModule(base);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllEntityBasedModules(), HttpStatus.OK);
	}
}
