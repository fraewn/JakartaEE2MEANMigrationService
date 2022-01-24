package com.migration.service;

import com.migration.service.model.knowledgeCollection.moduleIdentification.semanticAnalysis.SemanticKnowledge;
import com.migration.service.model.knowledgeCollection.moduleIdentification.semanticAnalysis.SemanticKnowledgeDAO;
import com.migration.service.model.knowledgeCollection.moduleIdentification.semanticAnalysis.SemanticKnowledgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(ServiceApplication.class, args);


		/*AbstractApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
		SemanticKnowledge semanticKnowledgeExample = new SemanticKnowledge();
		semanticKnowledgeExample.setName("Persistence Layer");
		semanticKnowledgeExample.setKeywords(new String[]{"DAO.java", "ServiceImpl.java", "Service.java"});

		SemanticKnowledgeDAO semanticKnowledgeDAO = (SemanticKnowledgeDAO) context.getBean("semanticKnowledgeDAO");
		semanticKnowledgeDAO.createSemanticKnowledge(semanticKnowledgeExample);
		System.out.println(semanticKnowledgeDAO.getAllSemanticKnowledge());*/


	}

	@Bean
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
	}

}
