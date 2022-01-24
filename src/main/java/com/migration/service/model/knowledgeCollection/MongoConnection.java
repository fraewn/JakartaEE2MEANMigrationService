package com.migration.service.model.knowledgeCollection;

import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoConnection {
	private final MongoDatabaseFactory mongoDatabaseFactory;

	public MongoConnection(MongoDatabaseFactory mongoDatabaseFactory){
		this.mongoDatabaseFactory = mongoDatabaseFactory;
	}

	/*private final MongoTemplate mongoTemplate;

	public MongoConnection(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}*/
}
