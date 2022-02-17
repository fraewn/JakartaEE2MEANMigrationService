package com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.entitySplitting;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EntitySplittingProfileService {
	private EntitySplittingProfileRepository entitySplittingProfileRepository;
	public void insertAll(List<EntitySplittingProfile> entitySplittingProfiles){
		entitySplittingProfileRepository.insert(entitySplittingProfiles);
	}

	public void insertOne(EntitySplittingProfile entitySplittingProfile){
		entitySplittingProfileRepository.insert(entitySplittingProfile);
	}

	public void deleteAll(){
		entitySplittingProfileRepository.deleteAll();
	}

	public void update(EntitySplittingProfile entitySplittingProfile){
		deleteAll();
		insertOne(entitySplittingProfile);
	}

	public List<EntitySplittingProfile> findAll(){
		return entitySplittingProfileRepository.findAll();
	}

}
