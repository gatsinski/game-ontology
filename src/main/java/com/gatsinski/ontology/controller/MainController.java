package com.gatsinski.ontology.controller;

import com.gatsinski.ontology.dto.QueryDto;
import com.gatsinski.ontology.dto.IndividualDto;
import com.gatsinski.ontology.service.QueryService;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


@Controller
public class MainController {

    @Autowired
    private QueryService queryService;

    @GetMapping("/")
    public String home() {
        return "redirect:/armies";
    }

    @GetMapping(value="/ontologies")
    public String getOntologies(Model model) {
        List<Object> list = new ArrayList<>();
        String fileName = "/home/gatsinski/Projects/game-ontology/game-ontology.rdf";

        class OntologyDto {
            String name;
            String uri;

            public OntologyDto(String name, String uri) {
                this.name = name;
                this.uri = uri;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }
        }

        try {
            File file = new File(fileName);
            FileReader reader = new FileReader(file);
            OntModel ontologyModel = ModelFactory
                    .createOntologyModel(OntModelSpec.OWL_DL_MEM);
            ontologyModel.read(reader,null);
            Iterator ontologiesIter = ontologyModel.listOntologies();
            while (ontologiesIter.hasNext()) {
                Ontology ontology = (Ontology) ontologiesIter.next();

                list.add(new OntologyDto(ontology.getLocalName(), ontology.getURI()));
            }
            model.addAttribute("ontologies", list);
            return "ontologies";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping(value="/armies")
    public String getArmies(Model model) {
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
           + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
           + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
           + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
           + "PREFIX game:  <http://www.semanticweb.org/gatsinski/ontologies/2019/0/game-ontology#>"
           + "SELECT * WHERE {?army rdf:type game:Army . }";
        Query query = queryService.getQuery(queryString);
        Optional<OntModel> optionalOntModel = queryService.getOntModel();
        if (optionalOntModel.isPresent()) {
            OntModel ontModel = optionalOntModel.get();
            QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            try {
                ResultSet resultSet = queryExecution.execSelect();
                List<String> armies = new ArrayList<>();
                while (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.nextSolution();
                    Resource resource = querySolution.getResource("army");
                    armies.add(resource.getLocalName());
                }
                model.addAttribute("armies", armies);
            } finally {
                queryExecution.close();
            }
        }
        return "armies";
    }

    @GetMapping(value="/equipment")
    public String getEquipment(Model model) {
        String baseQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX game: <http://www.semanticweb.org/gatsinski/ontologies/2019/0/game-ontology#>\n" +
            "\n" +
            "SELECT ?group ?individual\n" +
            "WHERE {?group  rdfs:subClassOf game:%s . ?group ^rdf:type ?individual }";

        String[] equipmentTypes = { "Armor", "Horse", "Weapons" };
        for (String equipmentType : equipmentTypes) {
            String queryString = String.format(baseQueryString, equipmentType);
            Query query = queryService.getQuery(queryString);
            Optional<OntModel> optionalOntModel = queryService.getOntModel();
            if (optionalOntModel.isPresent()) {
                OntModel ontModel = optionalOntModel.get();
                QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
                try {
                    ResultSet resultSet = queryExecution.execSelect();
                    List<IndividualDto> equipment = new ArrayList<>();
                    while (resultSet.hasNext()) {
                        QuerySolution querySolution = resultSet.nextSolution();
                        Resource individual = querySolution.getResource("individual");
                        Resource group = querySolution.getResource("group");
                        IndividualDto individualDto = new IndividualDto(individual.getURI(), group.getURI());
                        equipment.add(individualDto);
                    }
                    model.addAttribute(equipmentType, equipment);
                } finally {
                    queryExecution.close();
                }
            }
        }
        return "equipment";
    }

    @GetMapping(value="/items")
    public String getItems(Model model) {
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX game: <http://www.semanticweb.org/gatsinski/ontologies/2019/0/game-ontology#>\n" +
            "\n" +
            "SELECT ?group ?individual\n" +
            "WHERE {?group  rdfs:subClassOf game:Item . ?group ^rdf:type ?individual }";

        Query query = queryService.getQuery(queryString);
        Optional<OntModel> optionalOntModel = queryService.getOntModel();
        if (optionalOntModel.isPresent()) {
            OntModel ontModel = optionalOntModel.get();
            QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            try {
                ResultSet resultSet = queryExecution.execSelect();
                List<IndividualDto> items = new ArrayList<>();
                while (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.nextSolution();
                    Resource individual = querySolution.getResource("individual");
                    Resource group = querySolution.getResource("group");
                    IndividualDto individualDto = new IndividualDto(individual.getURI(), group.getURI());
                    items.add(individualDto);
                }
                model.addAttribute("items", items);
            } finally {
                queryExecution.close();
            }
        }
        return "items";
    }

    @GetMapping(value="/players")
    public String getPlayers(Model model) {
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
            + "PREFIX game:  <http://www.semanticweb.org/gatsinski/ontologies/2019/0/game-ontology#>"
            + "SELECT * WHERE {?player rdf:type game:Player . }";
        Query query = queryService.getQuery(queryString);
        Optional<OntModel> optionalOntModel = queryService.getOntModel();
        if (optionalOntModel.isPresent()) {
            OntModel ontModel = optionalOntModel.get();
            QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            try {
                ResultSet resultSet = queryExecution.execSelect();
                List<String> players = new ArrayList<>();
                while (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.nextSolution();
                    Resource resource = querySolution.getResource("player");
                    players.add(resource.getLocalName());
                }
                model.addAttribute("players", players);
            } finally {
                queryExecution.close();
            }
        }
        return "players";
    }

    @GetMapping(value="/skills")
    public String getSkills(Model model) {
        String baseQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX game: <http://www.semanticweb.org/gatsinski/ontologies/2019/0/game-ontology#>\n" +
            "\n" +
            "SELECT ?group ?individual\n" +
            "WHERE {?group  rdfs:subClassOf game:%s . ?group ^rdf:type ?individual }";

        String[] skillTypes = { "LeaderSkill", "PartySkill", "PersonalSkill" };
        for (String skillType : skillTypes) {
            String queryString = String.format(baseQueryString, skillType);
            Query query = queryService.getQuery(queryString);
            Optional<OntModel> optionalOntModel = queryService.getOntModel();
            if (optionalOntModel.isPresent()) {
                OntModel ontModel = optionalOntModel.get();
                QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
                try {
                    ResultSet resultSet = queryExecution.execSelect();
                    List<IndividualDto> skills = new ArrayList<>();
                    while (resultSet.hasNext()) {
                        QuerySolution querySolution = resultSet.nextSolution();
                        Resource individual = querySolution.getResource("individual");
                        Resource group = querySolution.getResource("group");
                        IndividualDto individualDto = new IndividualDto(individual.getURI(), group.getURI());
                        skills.add(individualDto);
                    }
                    model.addAttribute(skillType, skills);
                } finally {
                    queryExecution.close();
                }
            }
        }
        return "skills";
    }

    @ModelAttribute("queryDto")
    public QueryDto queryDto() {
        return new QueryDto();
    }

    @GetMapping(value="/query")
    public String showQueryForm() {
        return "query";
    }

    @PostMapping(value="/query")
    public String submitQuery(Model model,
                              @ModelAttribute("queryDto") @Valid QueryDto queryDto,
                              BindingResult result) {
        if (result.hasErrors()) {
            return "query";
        }

        Query query = queryService.getQuery(queryDto.getQuery());
        Optional<OntModel> optionalOntModel = queryService.getOntModel();
        if (optionalOntModel.isPresent()) {
            OntModel ontModel = optionalOntModel.get();
            QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            try {
                ResultSet resultSet = queryExecution.execSelect();
                List<String> variableNames = resultSet.getResultVars();
                List<List> rows = new ArrayList<>();
                while (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.nextSolution();
                    List<String> values = new ArrayList<>();
                    for (String variableName : variableNames) {
                        Resource resource = querySolution.getResource(variableName);
                        values.add(resource.toString());
                    }
                    rows.add(values);
                }
                model.addAttribute("variableNames", variableNames);
                model.addAttribute("rows", rows);
            } finally {
                queryExecution.close();
            }
        }
        return "query";

    }

}
