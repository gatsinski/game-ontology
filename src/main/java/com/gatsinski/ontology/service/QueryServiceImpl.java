package com.gatsinski.ontology.service;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QueryServiceImpl implements QueryService {

    @Override
    public Query getQuery(String query) {
        return QueryFactory.create(query);
    }

    @Override
    public Optional<OntModel> getOntModel() {
        try {
            String fileName = "/home/gatsinski/Projects/game-ontology/game-ontology.rdf";
            File file = new File(fileName);
            FileReader reader = new FileReader(file);
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            ontModel.read(reader,null);
            return Optional.of(ontModel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
