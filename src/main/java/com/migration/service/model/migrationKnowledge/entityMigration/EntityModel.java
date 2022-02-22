package com.migration.service.model.migrationKnowledge.entityMigration;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

@Document(value="entityModel")
public class EntityModel {

	@Id
	private String id;
	private String name;
	private List<String> attributes;
	private String identifyingAttribute;

	// you only need to know if yes because the attribute's name is the name of the related entity
	private HashMap<String, Boolean> attributeIsRelatedOtherEntity;
	private HashMap<String, String> relationTypes;
	private HashMap<String, String> attributeTypes;
	private boolean createOne;
	private boolean createMany;
	private boolean readOne;
	private boolean readMany;
	private boolean writeOne;
	private boolean writeMany;
	private boolean deleteOne;
	private boolean deleteMany;
	private boolean batchMethodNeeded;

	public boolean isBatchMethodNeeded() {
		return batchMethodNeeded;
	}

	public void setBatchMethodNeeded(boolean batchMethodNeeded) {
		this.batchMethodNeeded = batchMethodNeeded;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	public String getIdentifyingAttribute() {
		return identifyingAttribute;
	}

	public void setIdentifyingAttribute(String identifyingAttribute) {
		this.identifyingAttribute = identifyingAttribute;
	}

	public HashMap<String, Boolean> getAttributeIsRelatedOtherEntity() {
		return attributeIsRelatedOtherEntity;
	}

	public void setAttributeIsRelatedOtherEntity(HashMap<String, Boolean> attributeIsRelatedOtherEntity) {
		this.attributeIsRelatedOtherEntity = attributeIsRelatedOtherEntity;
	}

	public boolean isCreateOne() {
		return createOne;
	}

	public void setCreateOne(boolean createOne) {
		this.createOne = createOne;
	}

	public boolean isCreateMany() {
		return createMany;
	}

	public void setCreateMany(boolean createMany) {
		this.createMany = createMany;
	}

	public boolean isReadOne() {
		return readOne;
	}

	public void setReadOne(boolean readOne) {
		this.readOne = readOne;
	}

	public boolean isReadMany() {
		return readMany;
	}

	public void setReadMany(boolean readMany) {
		this.readMany = readMany;
	}

	public boolean isWriteOne() {
		return writeOne;
	}

	public void setWriteOne(boolean writeOne) {
		this.writeOne = writeOne;
	}

	public boolean isWriteMany() {
		return writeMany;
	}

	public void setWriteMany(boolean writeMany) {
		this.writeMany = writeMany;
	}

	public boolean isDeleteOne() {
		return deleteOne;
	}

	public void setDeleteOne(boolean deleteOne) {
		this.deleteOne = deleteOne;
	}

	public boolean isDeleteMany() {
		return deleteMany;
	}

	public void setDeleteMany(boolean deleteMany) {
		this.deleteMany = deleteMany;
	}

	public HashMap<String, String> getAttributeTypes() {
		return attributeTypes;
	}

	public void setAttributeTypes(HashMap<String, String> attributeTypes) {
		this.attributeTypes = attributeTypes;
	}

	public HashMap<String, String> getRelationTypes() {
		return relationTypes;
	}

	public void setRelationTypes(HashMap<String, String> relationTypes) {
		this.relationTypes = relationTypes;
	}
}
