package com.migration.service.model.knowledgeCollection.globalKnowledge;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value="nodeKnowledge")
public class NodeKnowledge {
	// represents the knowledge about a architecture element/ node collected during the migration strategies' executions
	@Id
	private String id;
	private String name;
	private List<String> label;

	private double triangleScore;
	private double triangleCoefficientScore;
	private double betweennessCentralityScore;
	private double pageRankScore;
	private double closenessCentralityScore;

	private boolean classIsEntity;
	private String representedEntity;

	private boolean reviewNecessary;
	private String review;

	private List<String> keywords;

	public boolean containsLabel(String label){
		Boolean comparison = false;
		for(String comparedLabel : this.label){
			if(label.equals(comparedLabel)){
				comparison = true;
			}
		}
		return comparison;
	}
	public String getName() { return name; }

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getLabel() {
		return label;
	}

	public void setLabel(List<String> label) {
		this.label = label;
	}

	public double getTriangleScore() {
		return triangleScore;
	}

	public void setTriangleScore(double triangleScore) {
		this.triangleScore = triangleScore;
	}

	public double getTriangleCoefficientScore() {
		return triangleCoefficientScore;
	}

	public void setTriangleCoefficientScore(double triangleCoefficientScore) {
		this.triangleCoefficientScore = triangleCoefficientScore;
	}

	public double getBetweennessCentralityScore() {
		return betweennessCentralityScore;
	}

	public void setBetweennessCentralityScore(double betweennessCentralityScore) {
		this.betweennessCentralityScore = betweennessCentralityScore;
	}

	public double getPageRankScore() {
		return pageRankScore;
	}

	public void setPageRankScore(double pageRankScore) {
		this.pageRankScore = pageRankScore;
	}

	public double getClosenessCentralityScore() {
		return closenessCentralityScore;
	}

	public void setClosenessCentralityScore(double closenessCentralityScore) {
		this.closenessCentralityScore = closenessCentralityScore;
	}

	public boolean isClassIsEntity() {
		return classIsEntity;
	}

	public void setClassIsEntity(boolean classIsEntity) {
		this.classIsEntity = classIsEntity;
	}

	public String getRepresentedEntity() {
		return representedEntity;
	}

	public void setRepresentedEntity(String representedEntity) {
		this.representedEntity = representedEntity;
	}

	public boolean isReviewNecessary() {
		return reviewNecessary;
	}

	public void setReviewNecessary(boolean reviewNecessary) {
		this.reviewNecessary = reviewNecessary;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
}
