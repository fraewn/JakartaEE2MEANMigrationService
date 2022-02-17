package com.migration.service.service;

import lombok.AllArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JavaEEGraphService {
	private final Driver driver;
	public JavaEEGraphService(Driver driver){
		this.driver = driver;
	}

	public List<Node> getNodeByName(String name){
		String query = "Match(n {name:'" + name + "'}) return n";
		System.out.println(query);
		try(Session session = driver.session()) {
			System.out.println(session.run(query).list(result -> result.get("n").asNode().get("name").asString()));
			System.out.println(session.run(query).list(result -> result.get("n").asNode()));
			return session.run(query).list(result -> result.get("n").asNode());
		}
	}
}
