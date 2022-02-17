package com.migration.service.model.analysisKnowledge.localKnowledge.splittingStrategies.entitySplitting;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntitySplittingProfileRepository extends MongoRepository<EntitySplittingProfile, String> {
}
