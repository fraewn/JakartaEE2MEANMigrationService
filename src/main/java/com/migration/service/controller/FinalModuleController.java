package com.migration.service.controller;

import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledge;
import com.migration.service.model.analysisKnowledge.localKnowledge.modules.ModuleKnowledgeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/v1/splitting/finalModules")
@AllArgsConstructor
public class FinalModuleController {
	private ModuleKnowledgeService moduleKnowledgeService;

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/add")
	public ResponseEntity<List<ModuleKnowledge>> requestUpdateEntitySplittingProfile(@RequestBody ModuleKnowledge moduleKnowledge){
		if(moduleKnowledgeService.findModuleKnowledgeByBase(moduleKnowledge.getBase())==null){
			moduleKnowledgeService.insertOne(moduleKnowledge);
		}
		else{
			moduleKnowledgeService.updateByBase(moduleKnowledge.getBase(), moduleKnowledge);
		}
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFinalModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/get")
	public ResponseEntity<List<ModuleKnowledge>> requestUpdateEntitySplittingProfile(){
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFinalModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/delete")
	public ResponseEntity<List<ModuleKnowledge>> requestDeleteModuleComponent(@RequestBody ModuleKnowledge moduleKnowledge){
		ModuleKnowledge moduleKnowledgeInstance = moduleKnowledgeService.findModuleKnowledgeByBase(moduleKnowledge.getBase());
		moduleKnowledgeService.deleteOne(moduleKnowledgeInstance);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFinalModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/update/usage")
	public ResponseEntity<List<ModuleKnowledge>> updateModuleKind(@RequestParam String kind, String base){
		ModuleKnowledge moduleKnowledge = moduleKnowledgeService.findModuleKnowledgeByBase(base);
		moduleKnowledgeService.deleteOne(moduleKnowledge);
		moduleKnowledge.setUsage(kind);
		moduleKnowledgeService.insertOne(moduleKnowledge);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFinalModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/update/usedModules")
	public ResponseEntity<List<ModuleKnowledge>> updateUsedModules(@RequestParam String usedModule, String base){
		ModuleKnowledge moduleKnowledge = moduleKnowledgeService.findModuleKnowledgeByBase(base);
		moduleKnowledgeService.deleteOne(moduleKnowledge);
		List<String> usages;
		if(moduleKnowledge.getUsedModules()!=null) {
			 usages = moduleKnowledge.getUsedModules();
		}
		else{
			usages = new ArrayList<>();
		}
		usages.add(usedModule);
		moduleKnowledge.setUsedModules(usages);
		moduleKnowledgeService.insertOne(moduleKnowledge);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFinalModules(), HttpStatus.OK);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/delete/usedModule")
	public ResponseEntity<List<ModuleKnowledge>> deleteUsedModule(@RequestParam String usedModule, String base){
		ModuleKnowledge moduleKnowledge = moduleKnowledgeService.findModuleKnowledgeByBase(base);
		moduleKnowledgeService.deleteOne(moduleKnowledge);
		moduleKnowledge.getUsedModules().remove(usedModule);
		moduleKnowledgeService.insertOne(moduleKnowledge);
		return new ResponseEntity<List<ModuleKnowledge>>(moduleKnowledgeService.findAllFinalModules(), HttpStatus.OK);
	}

}
