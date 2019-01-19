package com.gatsinski.ontology.dto;

import javax.validation.constraints.NotBlank;

public class QueryDto {

    @NotBlank
    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
