package com.gatsinski.ontology.dto;

public class IndividualDto {
    String instance;
    String type;

    public IndividualDto(String instance, String type) {
        this.instance = instance;
        this.type = type;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
