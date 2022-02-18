package com.migration.service.model.migrationKnowledge.entityMigration;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EntityModelService {
	private EntityModelRepository entityModelRepository;

	public List<EntityModel> findAll(){
		return entityModelRepository.findAll();
	}

	public void insertAll(List<EntityModel> entityModels){
		this.deleteAll();
		entityModelRepository.insert(entityModels);
	}

	public void insertOne(EntityModel entityModel){
		if(this.findByName(entityModel.getName())!=null) {
			this.deleteOne(this.findByName(entityModel.getName()));
		}
		entityModelRepository.insert(entityModel);
	}

	public void deleteAll(){
		entityModelRepository.deleteAll();
	}

	public EntityModel findByName(String name){
		return entityModelRepository.findByName(name);
	}

	public void deleteOne(EntityModel entityModel){
		entityModelRepository.delete(entityModel);
	}
}
