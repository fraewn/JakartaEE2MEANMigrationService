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

	public List<String> getAllJavaEEComponents(){
		List<String> javaEEComponents = new ArrayList<>();
		for(OntologyKnowledge ontologyKnowledge : ontologyKnowledgeRepository.findAll()){
			javaEEComponents.add(ontologyKnowledge.getJavaEEComponent());
		}
		return javaEEComponents;
	}

	public void setUp(){
		deleteAll();
		if(ontologyKnowledgeRepository.findAll().size()==0) {
			// das hier nur machen, wenn die nicht bereits mit keywords gefüllt sind!
			List<OntologyKnowledge> ontologyKnowledge = new ArrayList<>();
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component", "Database Entity", "Representation of the " +
					"entity in the database for an entity", "", "Backend", "Collection in Mongo", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Entity Implementation", "Object " +
					"representation of the entity " +
					"for an entity", "", "Backend", "Model", true, "mongoose"));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Service", "Enables direct access to data processing " +
					"for an entity", "ServiceImpl.java", "Backend", "Controller", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Service Interface", "Encapsulates access to data processing " +
					"for an entity", "Service.java", "", "none", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Data Access Object", "Offers CRUD-functionality to database. " +
					"Used by the service", "DAO.java", "Backend", "Controller", true, "mongoose"));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "javax.ws.rs", "REST Controller", "Enables external communication " +
					"via network using the REST paradigm ", "REST", "Backend", "REST API", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "jws.soap","WSDL Endpoint ", "Describes the form of external communication via " +
					"network using the SOAP protocol ", "SOAP", "Backend", "SOAP-API", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "jws.soap","WSDL Endpoint ", "Describes the form of external " +
					"communication via network using the SOAP protocol ", "wsdl", "Backend", "SOAP-API", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Database Entity",
					"Definition of roles in the application", "", "Backend", "Attribute role in User-Entity", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Database Entity", "Definition of " +
					"permissions each role has", "", "Backend", "Permission/Role Management", true, "accesscontrol"));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "Default Component","View Authorisation", "Secure " +
					"application so users only see authorised content", "", "Frontend", "Angular Role Guard", true,
					"CanActivate"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.security","Authentication Management", "Authentication" +
					" mechanisms", "", "Frontend", "Angular AuthGuard", true, "CanActivate"));
			// keyword "View.java" hilft hier, noch besser zu unterscheiden, weil gibt ja hier sehr viele matches die javax.faces nutzen
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "org.primefaces.PrimeFaces","View", "View design", "View.java", "Frontend",
					"HTML, CSS components", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "javax.faces","View Controller", "View logic, controlling of " +
					"calls to other layers", "", "Frontend", "TS component", true, "Angular Materials"));
			// hier würde "DataService.java" als Keyword helfen
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "","View Data Transformation", "Transforms data from " +
					"entities in the form the view classes need it", "Object.java", "Frontend", "Service", false ,
					""));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "","View Data Service", "Processes data coming from/needed " +
					"by the view classes","DataService.java", "Frontend","Service", true,"HTTP Client, Router, Subject"));
			ontologyKnowledge.add(new OntologyKnowledge("", "Default Component","Cross Section", "functionality used by all layers, e.g. logging",
					"", "Frontend/Backend", "Own class", true, "" ));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.enterprise.event","Event Management",
					"Handling incoming events and execute tasks scheduled for them", "", "Frontend", "Subject", true, "rsjx, npm"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.batch","Batch Feature",
					"CRUD huge loads of data in batches", "javax.batch", "Backend", "Method in Controller", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.mail","Mail Feature",
					"Sending emails", "", "Backend", "Own Class", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "jms","Messaging Feature",
					"Communication per messages and queues via network", "", "Backend", "Own Class", true, "kafka, npm"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.crypto","Encryption",
					"Encrypt data", "", "Backend", "Middleware", true, "bicrypt, npm"));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "javax.transaction","Transaction Feature",
					"Transactional data operations", "", "Backend", "Method in Controller", true, "mongoose transactions, npm"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.security","Authentication Management", "Authentication" +
					" mechanisms", "", "Backend", "Middleware", true, "jwt"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.management","Application Management", "Monitors and " +
					"manages the application", "", "External",	"External", true, "ELK Stack"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.ejb.Schedule","Scheduling", "Schedules processes", "",
					"Backend",	"Own Class", true, "cron"));
			this.insertAll(ontologyKnowledge);
		}
	}
}
