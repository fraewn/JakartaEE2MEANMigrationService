package com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.ontologyKnowledge;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class OntologyKnowledgeService {
	private OntologyKnowledgeRepository ontologyKnowledgeRepository;

	public List<OntologyKnowledge> findAll(){
		return ontologyKnowledgeRepository.findAll();
	}

	public void insertAll(List<OntologyKnowledge> ontologyKnowledge){
		ontologyKnowledgeRepository.insert(ontologyKnowledge);
	}

	public void insertOne(OntologyKnowledge ontologyKnowledge){
		ontologyKnowledgeRepository.insert(ontologyKnowledge);
	}

	public void deleteAll(){
		ontologyKnowledgeRepository.deleteAll();
	}

	public OntologyKnowledge findByAssociatedKeyword(String associatedKeyword){
		return ontologyKnowledgeRepository.findByAssociatedKeyword(associatedKeyword);
	}

	public void associateKeyword(String keyword, String javaEEComponent){
		OntologyKnowledge ontologyKnowledge = ontologyKnowledgeRepository.findByJavaEEComponent(javaEEComponent);
		ontologyKnowledgeRepository.delete(ontologyKnowledge);
		ontologyKnowledge.setAssociatedKeyword(keyword);
		ontologyKnowledgeRepository.insert(ontologyKnowledge);
	}



	public void setUp(){
		if(ontologyKnowledgeRepository.findAll().size()==0) {
			// das hier nur machen, wenn die nicht bereits mit keywords gefüllt sind!
			List<OntologyKnowledge> ontologyKnowledge = new ArrayList<>();
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Database Entity", "Representation of the entity in the " +
					"database" +
					"for an entity", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Entity", "Object representation of the entity " +
					"for an entity", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Service", "Enables direct access to data processing " +
					"for an entity", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Service Interface", "Encapsulates access to data processing " +
					"for an entity", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Data Access Object", "Offers CRUD-functionality to database. " +
					"Used by the service", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "REST Controller", "Enables external communication via " +
					"network using the REST paradigm ", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "SOAP ", "Enables external communication via " +
					"network using the SOAP protocol ", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Roles", "Definition of roles in the application", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Authorisation Rules", "Definition of permissions each role has",
					""));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "Authentication Management", "Authentication mechanisms", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "Views", "View design", ""));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "Controller", "View logic, controlling of calls to other layers",
					""));
			ontologyKnowledge.add(new OntologyKnowledge("", "Cross Section", "functionality used by all layers, e.g. logging",
					""));
			this.insertAll(ontologyKnowledge);
		}
	}
}
