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
public class SparqlIndex {

    public static final String LEXICAL_ENTRY_INDEX
            = SparqlPrefix.INST + "\n"
            + SparqlPrefix.LUC + "\n"
            + "INSERT DATA {\n"
            + "    inst:" + SparqlVariable.LEXICAL_ENTRY_INDEX + " :createConnector '''\n"
            + "{\n"
            + "  \"types\": [\n"
            + "    \"http://www.w3.org/ns/lemon/ontolex#LexicalEntry\"\n"
            + "  ],\n"
            + "  \"fields\": [\n"
            + "    {\n"
            + "      \"fieldName\": \"writtenForm\",\n"
            + "      \"propertyChain\": [\n"
            + "        \"http://www.w3.org/2000/01/rdf-schema#label\"\n"
            + "      ],\n"
            + "	  \"multivalued\": false,\n"
            + "	  \"analyzed\": false\n"
            + "    },\n"
            + "   {\n"
            + "      \"fieldName\": \"lexicalEntryIRI\",\n"
            + "      \"propertyChain\": [\n"
            + "        \"$self\"\n"
            + "      ]\n"
            + "   },\n"
            + "   {\n"
            + "        \"fieldName\": \"writtenFormLanguage\",\n"
            + "        \"propertyChain\": [\n"
            + "          \"http://www.w3.org/2000/01/rdf-schema#label\",\n"
            + "          \"lang()\"\n"
            + "        ],\n"
            + "        \"analyzed\": false\n"
            + "    },\n"
            + "    {\n"
            + "        \"fieldName\": \"type\",\n"
            + "        \"propertyChain\": [\n"
            + "          \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\",\n"
            + "          \"http://www.w3.org/2000/01/rdf-schema#label\"\n"
            + "        ],\n"
            + "		\"languages\": [\n"
            + "		  \"en\"\n"
            + "		]\n"
            + "    },\n"
            + "    {\n"
            + "        \"fieldName\": \"pos\",\n"
            + "        \"propertyChain\": [\n"
            + "          \"http://www.lexinfo.net/ontology/3.0/lexinfo#partOfSpeech\",\n"
            + "		  \"http://www.w3.org/2000/01/rdf-schema#label\"\n"
            + "        ],\n"
            + "		\"languages\": [\n"
            + "		  \"en\"\n"
            + "		]\n"
            + "    },\n"
            + "    {\n"
            + "    	\"fieldName\": \"state\",\n"
            + "    	\"propertyChain\": [\n"
            + "    		\"http://purl.org/dc/terms/valid\"\n"
            + "    	]\n"
            + "    },\n"
            + "	{\n"
            + "    	\"fieldName\": \"author\",\n"
            + "    	\"propertyChain\": [\n"
            + "    		\"http://purl.org/dc/terms/contributor\"\n"
            + "    	]\n"
            + "    }\n"
            + "  ]\n"
            + "}\n"
            + "''' .\n"
            + "}";

    public static final String DELETE_INDEX
            = SparqlPrefix.INST + "\n"
            + "INSERT DATA {\n"
            + "  inst:[INDEX_NAME] :dropConnector \"\" .\n"
            + "}";

}
