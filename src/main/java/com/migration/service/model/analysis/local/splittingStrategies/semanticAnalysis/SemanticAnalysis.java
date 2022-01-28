package com.migration.service.model.analysis.local.splittingStrategies.semanticAnalysis;

import com.migration.service.model.knowledgeCollection.localKnowledge.splittingStrategies.semanticKnowledge.SemanticKnowledge;
import org.neo4j.driver.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class SemanticAnalysis {
	private final Driver driver;
	public SemanticAnalysis(Driver driver) {
		this.driver = driver;
	}

	// Klasse soll mir aus neo4j die Daten besorgen für die semantische analyse
	// dh nachbarn der layer knoten
	// dann in denen analysieren welche Wörter oft vorkommen
	// dann ein semantic knowledge object machen
	// dann den semnatic knowledge service aufrufen und das persistieren


	// TODO add clean method to remove "" which come from neo4j

	public void addKeywordsToLayer(HashMap<String,List<String>> keywords){

	}

	public List<SemanticKnowledge> executeSemanticAnalysisForOneLayerWithoutAdditionalKeywords(List<SemanticAnalysisExtension> semanticAnalysisExtensions, String layer){
		List<SemanticKnowledge> semanticKnowledge = new ArrayList<>();
		List<String> neighbourNodes = new ArrayList<>();
		int searchExtent = 0;
		for(SemanticAnalysisExtension extension : semanticAnalysisExtensions){
			if(layer.equals(extension.getLayer())){
				searchExtent = extension.getSearchExtent();
			}
		}
		try(Session session = driver.session()){
				SemanticKnowledge semanticKnowledgeInstance = new SemanticKnowledge();
				neighbourNodes.clear();
				Result result = session.run("MATCH (n:Layer {name: '" + layer + "'}) " +
						"CALL apoc.neighbors.byhop(n, 'CALLS_METHOD|IMPLEMENTS|" +
						"EXTENDS|INJECTS|IS_ENTITY|BELONGS_TO|USES_FUNCTIONALITY', " + searchExtent + ") " +
						"YIELD nodes " +
						"RETURN nodes");
				for (Result it = result; it.hasNext(); ) {
					Record record = it.next();
					Value value = record.get("nodes");
					for(int i=0; i < value.size(); i++){
						neighbourNodes.add(value.get(i).get("name").toString());
					}
				}

				List<String> commonWordsPerLayer = findCommonWordsPerLayer(neighbourNodes, splitCamelCase(neighbourNodes));

				semanticKnowledgeInstance.setName(layer);
				semanticKnowledgeInstance.setKeywords(commonWordsPerLayer);

				System.out.println("++++++++");
				System.out.println(layer);
				for(String word : commonWordsPerLayer){
					System.out.println(word);
				}
				semanticKnowledge.add(semanticKnowledgeInstance);
		}
		// persist in db
		return semanticKnowledge;
	}

	public List<SemanticKnowledge> executeSemanticAnalysis(){
		List<SemanticKnowledge> semanticKnowledge = new ArrayList<>();
		// TODO vom frontend aus soll man für jedes Layer separat die analyse fahren können
		// man soll Zusatzwörter eingeben können wie z.B. "Rest", "Soap"
		int searchExtent = 3;

		List<String> layers = getLayers();
		List<String> neighbourNodes = new ArrayList<>();

		try(Session session = driver.session()){
			for(String layer : layers) {
				SemanticKnowledge semanticKnowledgeInstance = new SemanticKnowledge();
				neighbourNodes.clear();
				Result result = session.run("MATCH (n:Layer {name: '" + layer + "'}) " +
						"CALL apoc.neighbors.byhop(n, 'CALLS_METHOD|IMPLEMENTS|" +
						"EXTENDS|INJECTS|IS_ENTITY|BELONGS_TO|USES_FUNCTIONALITY', " + searchExtent + ") " +
						"YIELD nodes " +
						"RETURN nodes");
				for (Result it = result; it.hasNext(); ) {
					Record record = it.next();
					Value value = record.get("nodes");
					for(int i=0; i < value.size(); i++){
						neighbourNodes.add(value.get(i).get("name").toString());
					}
				}

				List<String> commonWordsPerLayer = findCommonWordsPerLayer(neighbourNodes, splitCamelCase(neighbourNodes));

				semanticKnowledgeInstance.setName(layer);
				semanticKnowledgeInstance.setKeywords(commonWordsPerLayer);
				// TODO example how to use checkif word is common method
				/*if(checkIfWordIsCommon(neighbourNodes, "DataService.java")){
					commonWordsPerLayer.add("DataService.java");
				}*/

				System.out.println("++++++++");
				System.out.println(layer);
				for(String word : commonWordsPerLayer){
					System.out.println(word);
				}
				semanticKnowledge.add(semanticKnowledgeInstance);
			}
		}
		return semanticKnowledge;
	}

	public void executeSemanticAnalysisExtended(SemanticAnalysisExtension semanticAnalysisExtension) {
		System.out.println(semanticAnalysisExtension.toString());
	}

	public List<String> getLayers(){
		try(Session session = driver.session()){
			return session.run("MATCH (n:Layer) return n").list(result -> result.get("n").asNode().get("name").asString());
		}
	}

	public List<String> splitCamelCase(List<String> camelCaseWords){
		List<String> splittedWords = new ArrayList<>();
		for(String word : camelCaseWords){
			word = word.replaceAll("\"", "");
			for( String w : word.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")){
				if(!splittedWords.contains(w)) {
					if (w.length() > 1) {
						w.toLowerCase();
						splittedWords.add(w);
					}
				}
			}
		}
		return splittedWords;
	}

	public List<String> findCommonWordsPerLayer(List<String> neighbourNodeNames, List<String> words){
		int wordFrequency;
		List<String> commonWordsPerLayer = new ArrayList<>();
		for(String word : words) {
			wordFrequency = 0;
			//Count each word in the file and store it in variable count
			for (String neighbourNodeName : neighbourNodeNames) {
				neighbourNodeName.toLowerCase();
				neighbourNodeName.replaceAll("\"", "");
				if (neighbourNodeName.contains(word)) {
					wordFrequency++;
				}
			}
			if (wordFrequency > 3) {
				commonWordsPerLayer.add(word);
			}
		}
		return commonWordsPerLayer;
	}

	// TODO kann genutzt werden wenn Wissensarbeiter eigene Wörter eingeben können
	public boolean checkIfWordIsCommon(List<String> neighbourNodeNames, String word){
		int wordFrequency = 0;
		for (String neighbourNodeName : neighbourNodeNames) {
			neighbourNodeName.toLowerCase();
			neighbourNodeName.replaceAll("\"", "");
			if (neighbourNodeName.contains(word)) {
				wordFrequency++;
			}
		}
		if (wordFrequency > 3) {
			return true;
		}
		return false;
	}


}
