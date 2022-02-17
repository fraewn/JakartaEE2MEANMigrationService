package com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.functionalityKnowledge;

import com.migration.service.service.analysis.local.splittingStrategies.splittingByFunctionality.FunctionalitySplitting;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@Service
public class FunctionalitySplittingProfileService {
	private FunctionalitySplittingProfileRepository functionalitySplittingProfileRepository;
	private FunctionalitySplitting functionalitySplitting;
	public void insertAll(List<FunctionalitySplittingProfile> functionalitySplittingProfiles){
		functionalitySplittingProfileRepository.insert(functionalitySplittingProfiles);
	}
	public void insertOne(FunctionalitySplittingProfile functionalitySplittingProfile){
		functionalitySplittingProfileRepository.insert(functionalitySplittingProfile);
	}

	public void deleteAll(){
		functionalitySplittingProfileRepository.deleteAll();
	}
	public List<FunctionalitySplittingProfile> findAll(){
		return functionalitySplittingProfileRepository.findAll();
	}

	public void createDefault(){
		List<String> functionalities = functionalitySplitting.findFunctionalities();
		List<String> searchGroup = new ArrayList<>();
		searchGroup.add("Entity Implementation");
		List<FunctionalitySplittingProfile> functionalitySplittingProfiles = new ArrayList<>();
		for(String functionality : functionalities){
			FunctionalitySplittingProfile functionalitySplittingProfile = new FunctionalitySplittingProfile();
			functionalitySplittingProfile.setName(functionality);

		}

	}
}
