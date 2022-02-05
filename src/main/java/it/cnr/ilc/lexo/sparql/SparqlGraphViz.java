/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.sparql;

/**
 *
 * @author andreabellandi
 */
public class SparqlGraphViz {

    public static final String GRAPH_VIZ_SENSE_SUMMARY
            = SparqlPrefix.RDFS.getSparqlPrefix() + "\n"
            + SparqlPrefix.INST.getSparqlPrefix() + "\n"
            + SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.SKOS.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEXINFO.getSparqlPrefix() + "\n"
            + SparqlPrefix.LUC.getSparqlPrefix() + "\n"
            + "SELECT"
            + " ?" + SparqlVariable.LEXICAL_ENTRY
            + " ?" + SparqlVariable.SENSE_DEFINITION
            + " ?" + SparqlVariable.LEXICAL_ENTRY_POS
            + " ?" + SparqlVariable.LABEL
            + " ?" + SparqlVariable.SENSE
            + "\n"
            + "WHERE { \n"
            + "    ?search a inst:" + SparqlVariable.LEXICAL_SENSE_INDEX + " ;\n"
            + "      luc:query \"lexicalSenseIRI:[IRI]\" ;\n"
            + "      luc:entities ?" + SparqlVariable.SENSE + " .\n"
            + "    ?" + SparqlVariable.SENSE + " skos:definition ?" + SparqlVariable.SENSE_DEFINITION + " ; \n"
            + "        ontolex:isSenseOf ?" + SparqlVariable.LEXICAL_ENTRY + " .\n"
            + "    ?" + SparqlVariable.LEXICAL_ENTRY + " lexinfo:partOfSpeech ?" + SparqlVariable.LEXICAL_ENTRY_POS + " ;\n"
            + "        rdfs:label ?" + SparqlVariable.LABEL + " .\n"
            + "} ";

    public static final String GRAPH_VIZ_SENSE_LINKS
            = SparqlPrefix.INST.getSparqlPrefix() + "\n"
            + SparqlPrefix.ONTO.getSparqlPrefix() + "\n"
            + SparqlPrefix.LUC.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEXINFO.getSparqlPrefix() + "\n"
            + "SELECT  ?" + SparqlVariable.GRAPH + " "
            + "(count(?in" + SparqlVariable.HYPERNYM + ") as ?in" + SparqlVariable.HYPERNYM + "Count)\n"
            + "(count(?out" + SparqlVariable.HYPERNYM + ") as ?out" + SparqlVariable.HYPERNYM + "Count)\n"
            + "(count(?in" + SparqlVariable.HYPONYM + ") as ?in" + SparqlVariable.HYPONYM + "Count) \n"
            + "(count(?out" + SparqlVariable.HYPONYM + ") as ?out" + SparqlVariable.HYPONYM + "Count) \n"
            + "(count(?in" + SparqlVariable.MERONYM_TERM + ") as ?in" + SparqlVariable.MERONYM_TERM + "Count)  \n"
            + "(count(?out" + SparqlVariable.MERONYM_TERM + ") as ?out" + SparqlVariable.MERONYM_TERM + "Count)  \n"
            + "(count(?in" + SparqlVariable.PART_MERONYM + ") as ?in" + SparqlVariable.PART_MERONYM + "Count) \n"
            + "(count(?out" + SparqlVariable.PART_MERONYM + ") as ?out" + SparqlVariable.PART_MERONYM + "Count) \n"
            + "(count(?out" + SparqlVariable.SYNONYM + ") as ?out" + SparqlVariable.SYNONYM + "Count) \n"
            + "(count(?in" + SparqlVariable.SYNONYM + ") as ?in" + SparqlVariable.SYNONYM + "Count)\n"
            + "FROM NAMED onto:explicit\n"
            + "FROM NAMED onto:implicit\n"
            + "WHERE {\n"
            + "  ?search a inst:" + SparqlVariable.LEXICAL_SENSE_INDEX + " ;\n"
            + "      luc:query \"lexicalSenseIRI:[IRI]\" ;\n"
            + "      luc:entities ?" + SparqlVariable.SENSE + " .\n"
            + "{ GRAPH ?" + SparqlVariable.GRAPH + " { ?sense lexinfo:hypernym ?outhypernym . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?inhypernym lexinfo:hypernym ?sense . } }\n"
            + " UNION\n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?sense lexinfo:hyponym ?outhyponym . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?inhyponym lexinfo:hyponym ?sense . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?sense lexinfo:meronymTerm ?outmeronymTerm . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?inmeronymTerm lexinfo:meronymTerm ?sense . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?sense lexinfo:partMeronym ?outpartMeronym . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?inpartMeronym lexinfo:partMeronym ?sense . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?sense lexinfo:synonym ?outsynonym . } }\n"
            + " UNION \n"
            + "    { GRAPH ?" + SparqlVariable.GRAPH + " { ?insynonym lexinfo:synonym ?sense . } }\n"
            + "} GROUP BY ?" + SparqlVariable.GRAPH;

    public static final String GRAPH_VIZ_NODE_GRAPH
            // lex:_NODE_ lexinfo:synonym ?target
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.ONTO.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEXINFO.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.SKOS.getSparqlPrefix() + "\n"
            + SparqlPrefix.RDFS.getSparqlPrefix() + "\n"
            + "SELECT ?graph ?source ?label ?pos ?def ?labelTarget ?target ?posTarget ?defTarget\n"
            + "FROM NAMED onto:explicit\n"
            + "FROM NAMED onto:implicit \n"
            + "WHERE {\n"
            + "    GRAPH ?graph { ?source lexinfo:_RELATION_ ?target } .\n"
            + "    ?source skos:definition ?def .\n"
            + "    ?le ontolex:sense ?source ;\n"
            + "        lexinfo:partOfSpeech ?pos ;\n"
            + "        rdfs:label ?label .\n"
            + "    ?target skos:definition ?defTarget .\n"
            + "    ?leTarget ontolex:sense ?target ;\n"
            + "           lexinfo:partOfSpeech ?posTarget ;\n"
            + "           rdfs:label ?labelTarget .\n"
            + "FILTER(regex(str(_NODE_VARIABLE_), \"http://lexica/mylexicon#_NODE_ID_\"))\n"
            + "}";

    public static final String GRAPH_VIZ_EDGE_GRAPH
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.ONTO.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEXINFO.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.RDFS.getSparqlPrefix() + "\n"
            + "SELECT ?graph ?source ?target ?sourceLabel ?targetLabel ?relation\n"
            + "FROM NAMED onto:implicit\n"
            + "FROM NAMED onto:explicit\n"
            + "WHERE {\n"
            + "    GRAPH ?graph { ?source ?relation ?target .\n"
            + "        VALUES ?source { lex:_SOURCE_ } .\n"
            + "        VALUES ?target { lex:_TARGET_ } .\n"
            + "        VALUES ?relation { lexinfo:_RELATION_ } .\n"
            + "    }\n"
            + "    ?source ontolex:isSenseOf [ rdfs:label ?sourceLabel ] .\n"
            + "    ?target ontolex:isSenseOf [ rdfs:label ?targetLabel ] .\n"
            + "}";

}