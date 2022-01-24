package com.migration.service;

import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/*@Configuration
@ComponentScan(basePackages = {"com.migration.service"})
@EnableMongoRepositories(basePackages = {"com.migration.service"})
public class SpringConfig {
	@Bean
	public MongoDbFactory mongoDbFactory()
	{
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		return new SimpleMongoDbFactory(mongoClient, "mydb");
	}

	@Bean
	public MongoTemplate mongoTemplate()
	{
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
		return mongoTemplate;
	}
}*/

public class SpringConfig{}
