package com.migration.service.model.analysisKnowledge.ontologyKnowledge;

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

	public OntologyKnowledge findByJavaEEComponent(String javaEEComponent){
		return ontologyKnowledgeRepository.findByJavaEEComponent(javaEEComponent);
	}

	public OntologyKnowledge findByKnowledgeSource(String knowledgeSource){
		return ontologyKnowledgeRepository.findByKnowledgeSource(knowledgeSource);
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
			ontologyKnowledgeRepository.deleteAll();
			// das hier nur machen, wenn die nicht bereits mit keywords gefüllt sind!
			List<OntologyKnowledge> ontologyKnowledge = new ArrayList<>();
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component", "Database Entity", "Representation of the " +
					"entity in the database for an entity", "", "Backend", "MongoCollection", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Entity Implementation", "Object " +
					"representation of the entity " +
					"for an entity", "", "Backend", "Model", true, "mongoose"));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Service", "Enables direct access to data processing " +
					"for an entity", "ServiceImpl.java", "Backend", "Route", false, "express.js"));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Service Interface", "Encapsulates access to data processing " +
					"for an entity", "Service.java", "", "none", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Data Access Object", "Offers CRUD-functionality to database. " +
					"Used by the service", "DAO.java", "Backend", "RestController", true, "mongoose"));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "javax.ws.rs", "REST Controller", "Enables external communication " +
					"via network using the REST paradigm ", "REST", "Backend", "RestApi", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "jws.soap","SOAP API", "Enables network communication via the SOAP " +
					"protocol ", "SOAP", "Backend", "SoapRoute", true, "soap"));
			ontologyKnowledge.add(new OntologyKnowledge("Web Layer", "jws.soap","WSDL Endpoint", "Describes the form of external " +
					"communication via network using the SOAP protocol ", "wsdl", "Backend", "SoapController", true, "soap"));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Database Entity",
					"Definition of roles in the application", "", "Backend", "UserEntityAttribute", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "Default Component","Database Entity", "Definition of " +
					"permissions each role has", "", "Backend", "Authorisation", true, "accesscontrol"));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "Default Component","View Authorisation", "Secure " +
					"application so users only see authorised content", "", "Frontend", "RoleGuard", true,
					"CanActivate"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.security","Authentication Management", "Authentication" +
					" mechanisms", "", "Frontend", "AuthGuard", true, "CanActivate"));
			// keyword "View.java" hilft hier, noch besser zu unterscheiden, weil gibt ja hier sehr viele matches die javax.faces nutzen
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "org.primefaces.PrimeFaces","View", "View design", "View.java", "Frontend",
					"component_html", false, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "javax.faces","View Controller", "View logic, controlling of " +
					"calls to other layers", "", "Frontend", "component_ts", true, "Angular Materials"));
			// hier würde "DataService.java" als Keyword helfen
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "","View Data Transformation", "Transforms data from " +
					"entities in the form the view classes need it", "Object.java", "Frontend", "service_ts", false ,
					""));
			ontologyKnowledge.add(new OntologyKnowledge("Presentation Layer", "","View Data Service", "Processes data coming from/needed " +
					"by the view classes","DataService.java", "Frontend","service_ts", true,"HTTP Client, Router, Subject"));
			ontologyKnowledge.add(new OntologyKnowledge("", "Default Component","Cross Section", "functionality used by all layers, e.g. logging",
					"", "Frontend/Backend", "Class", true, "" ));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.enterprise.event","Event Management",
					"Handling incoming events and execute tasks scheduled for them", "", "Frontend", "subject", true, "rsjx"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.batch","Batch Feature",
					"CRUD huge loads of data in batches", "javax.batch", "Backend", "ControllerMethod", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.mail","Mail Feature",
					"Sending emails", "", "Backend", "_js", true, ""));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "jms","Messaging Feature",
					"Communication per messages and queues via network", "", "Backend", "Class", true, "kafka"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.crypto","Encryption",
					"Encrypt data", "", "Backend", "middleware", true, "bicrypt"));
			ontologyKnowledge.add(new OntologyKnowledge("Persistence Layer", "javax.transaction","Transaction Feature",
					"Transactional data operations", "", "Backend", "ControllerMethod", true, "mongoose transactions"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.security","Authentication Feature",
					"Authentication" +
					" mechanisms", "", "Backend", "Middleware", true, "jwt"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.management","Application Management Feature", "Monitors " +
					"and " +
					"manages the application", "", "External",	"External", true, "ELK-Stack"));
			ontologyKnowledge.add(new OntologyKnowledge("Service Layer", "javax.ejb.Schedule","Scheduling Feature", "Schedules processes",
					"",
					"Backend",	"Class", true, "cron"));
			this.insertAll(ontologyKnowledge);
		System.out.println("executed");
	}
}
