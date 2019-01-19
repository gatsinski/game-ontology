package com.gatsinski.ontology.service;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;

import java.util.Optional;

public interface QueryService {
    Query getQuery(String query);

    Optional<OntModel> getOntModel();
}
