package com.migration.service.model.analysis.Util;

public class TriangleCountResult {
	private Double triangleCountScore;
	private  Double triangleCoefficientScore;
	public TriangleCountResult(Double triangleCountScore, Double triangleCoefficientScore){
		this.triangleCoefficientScore = triangleCoefficientScore;
		this.triangleCountScore = triangleCountScore;
	}

	public Double getTriangleCountScore() {
		return triangleCountScore;
	}

	public Double getTriangleCoefficientScore() {
		return triangleCoefficientScore;
	}





}
