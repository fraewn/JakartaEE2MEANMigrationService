package com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.functionalityKnowledge;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FunctionalitySplittingProfileRepository extends MongoRepository<FunctionalitySplittingProfile, String> {
}
