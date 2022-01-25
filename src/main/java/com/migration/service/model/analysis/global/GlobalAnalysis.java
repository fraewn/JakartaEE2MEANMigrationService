package com.migration.service.model.analysis.global;

import com.migration.service.model.knowledgeCollection.globalKnowledge.NodeKnowledge;
import org.neo4j.driver.*;
import org.neo4j.driver.util.Pair;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

@Component
public class GlobalAnalysis {

	private List<Pair<String, Value>> triangleCountResults;

	private final Driver driver;
	public GlobalAnalysis(Driver driver) {
		this.driver = driver;
	}


	public void executeGlobalAnalyses() {
		// Liste mit allen Knoten aus neo4j holen
		/*List<String> nodeNames = getAllNodes();
		for(String nodeName : nodeNames){
			// nodeKnowledge entity erstellen
			NodeKnowledge nodeKnowledge = new NodeKnowledge();
			// für jeden Knoten in den Resultaten der globalen Strategien die Werte raussuchen und rein schreiben

		}*/
		executeTriangleCount();

	}

	public List<String> getAllNodes(){
		try(Session session = driver.session()){
			return session.run("MATCH (n) return n").list(result -> result.get("n").asNode().get("name").asString());
		}
	}

	public void executeTriangleCount(){
		try(Session session = driver.session()) {
			Result result = session.run("CALL algo.triangleCount.stream(null, null, {concurrency:8}) " +
					"YIELD nodeId, triangles, coefficient " +
					"return algo.getNodeById(nodeId).name as name, triangles, coefficient");


			for (Result it = result; it.hasNext(); ) {
				Record record = it.next();

				// [name: "LocalAudit.java", triangles: 1, coefficient: 0.047619047619047616]
				// System.out.println(record.fields());
				triangleCountResults = record.fields();

				// how to iterate the triangle count result list:
				// during this iteration we can search for our class name with pair.value().equals("classname.java")
				// if yes, we set the attributes to the values
				/*for(Pair pair : triangleCountResults){
					System.out.println(pair.key());
					System.out.println(pair.value());
				}*/

				//System.out.println(value);
				/*for (int i = 0; i < value.size(); i++) {
					neighbourNodes.add(value.get(i).get("name").toString());
				}*/
			}
		}

	}
}
