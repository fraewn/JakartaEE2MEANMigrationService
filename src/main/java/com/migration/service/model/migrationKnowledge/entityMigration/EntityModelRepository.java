package com.migration.service.model.migrationKnowledge.entityMigration;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityModelRepository extends MongoRepository<EntityModel, String> {
	public EntityModel findByName(String name);

}
