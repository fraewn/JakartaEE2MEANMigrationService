package com.migration.service.model.migrationKnowledge.entityMigration;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityModelRepository extends MongoRepository<EntityModel, String> {
	public EntityModel findByName(String name);
}
