package com.migration.service;

import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledge;
import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledgeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(ServiceApplication.class, args);



	}

	/*@Bean
	CommandLineRunner runner(SemanticKnowledgeRepository semanticKnowledgeRepository, MongoTemplate mongoTemplate) {
		return args -> {
			SemanticKnowledge semanticKnowledgeExample = new SemanticKnowledge();
			semanticKnowledgeExample.setName("Persistence Layer");
			semanticKnowledgeExample.setKeywords(new String[]{"DAO.java", "ServiceImpl.java", "Service.java"});
			System.out.println(semanticKnowledgeExample.getName());
			semanticKnowledgeRepository.insert(semanticKnowledgeExample);
			Query query = new Query();
			query.addCriteria(Criteria.where("name").is("Persistence Layer"));
			List<SemanticKnowledge> semanticKnowledge = mongoTemplate.find(query, SemanticKnowledge.class);
			if(semanticKnowledge.size() > 0 ){
				System.out.println("found one!");
			}
		};
	}*/

}
