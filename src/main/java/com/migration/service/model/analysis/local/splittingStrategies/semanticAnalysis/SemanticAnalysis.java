package com.migration.service.model.analysis.local.splittingStrategies.semanticAnalysis;

import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SemanticAnalysis {
	private final Driver driver;


	// Klasse soll mir aus neo4j die Daten besorgen für die semantische analyse
	// dh nachbarn der layer knoten
	// dann in denen analysieren welche Wörter oft vorkommen
	// dann ein semantic knowledge object machen
	// dann den semnatic knowledge service aufrufen und das persistieren
	public SemanticAnalysis(Driver driver) {
		this.driver = driver;
	}

	public void executeSemanticAnalysis(){
		List<String> layers = getLayers();
		List<String> neighbourNodes = new ArrayList<>();
		try(Session session = driver.session()){
			for(String layer : layers) {
				neighbourNodes.clear();
				Result result = session.run("MATCH (n:Layer {name: '" + layer + "'}) " +
						"CALL apoc.neighbors.byhop(n, 'CALLS_METHOD|IMPLEMENTS|" +
						"EXTENDS|INJECTS|IS_ENTITY|BELONGS_TO|USES_FUNCTIONALITY', 3) " +
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
				System.out.println("++++++++");
				System.out.println(layer);
				System.out.println(commonWordsPerLayer.size());
				for(String word : commonWordsPerLayer){
					System.out.println(word);
				}
			}

		}
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
}