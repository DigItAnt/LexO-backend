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
public class SparqlQueryUtil {

    public static final String ASK_ENTITY_LINGUISTIC_RELATION
            = "ASK { <_ID_> <_RELATION_> <_VALUE_> }";

    public static final String ASK_ENTITY_GENERIC_RELATION
            = "ASK { <_ID_> <_RELATION_> _VALUE_ }";

    public static final String ASK_ENTITY_SKOS_RELATION
            = "ASK { <_ID_> <_RELATION_> _VALUE_ }";

    public static final String ASK_LABEL_RELATION
            = SparqlPrefix.SKOS.getSparqlPrefix() + "\n"
            + "ask { <_ID_> <_RELATION_> ?label \n"
            + "    FILTER (lang(?label) = \"_LANGUAGE_\") }";

    public static final String BIBLIOGRAFY_BY_ITEMKEY
            = SparqlPrefix.RDF.getSparqlPrefix() + "\n"
            + SparqlPrefix.DCT.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.BIBLIOGRAPHY_ID + "\n"
            + "WHERE { \n"
            + "       <_ID_> dct:references ?" + SparqlVariable.BIBLIOGRAPHY_ID + " .\n"
            + "       ?" + SparqlVariable.BIBLIOGRAPHY_ID + " dct:publisher \"_ITEMKEY_\" }";

    public static final String IS_LEXICON_LANGUAGE
            = SparqlPrefix.LIME.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.LIME.getPrefix() + "Lexicon }";

    public static final String DICTIONARY_HAS_ENTRY
            = SparqlPrefix.LEXICOG.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> " + SparqlPrefix.LEXICOG.getPrefix() + "entry ?entry }";

    public static final String IS_DICTIONARY
            = SparqlPrefix.LEXICOG.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.LEXICOG.getPrefix() + "LexicographicResource }";

    public static final String IS_LEXICALENTRY_ID
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "LexicalEntry }";

    public static final String IS_DICTENTRY_COMPONENT_ID
            = SparqlPrefix.LEXICOG.getSparqlPrefix() + "\n"
            + "ASK { \n"
            + "{ <_ID_> a " + SparqlPrefix.LEXICOG.getPrefix() + "Entry }\n"
            + " UNION \n"
            + "{ <_ID_> a " + SparqlPrefix.LEXICOG.getPrefix() + "LexicographicComponent }\n"
            + "}";

    public static final String LEXICALENTRY_TYPE
            = SparqlPrefix.SESAME.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.LEXICAL_ENTRY_TYPE + "\n"
            + "WHERE { <_ID_> rdf:type ?" + SparqlVariable.LEXICAL_ENTRY_TYPE + " .\n"
            + "FILTER(regex(str(?" + SparqlVariable.LEXICAL_ENTRY_TYPE + "),\""
            + SparqlPrefix.ONTOLEX.getUri() + "|" + SparqlPrefix.ETY.getUri() + "\")) }";

    public static final String IS_COMPONENT_ID
            = SparqlPrefix.DECOMP.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.DECOMP.getPrefix() + "Component }";

    public static final String IS_COLLOCATION_ID
            = SparqlPrefix.FRAC.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.FRAC.getPrefix() + "Collocation }";

    public static final String IS_FORMRESTRICTION_ID
            = SparqlPrefix.LEXICOG.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.LEXICOG.getPrefix() + "FormRestriction }";

    public static final String IS_LEXICOSEMANTIC_RELATION_ID
            = SparqlPrefix.VARTRANS.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.VARTRANS.getPrefix() + "LexicoSemanticRelation }";

    public static final String IS_TRANSLATIONSET_ID
            = SparqlPrefix.VARTRANS.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.VARTRANS.getPrefix() + "TranslationSet }";
    public static final String IS_LEXICAL_CONCEPT_ID
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "LexicalConcept }";

    public static final String IS_CONCEPT_SET_ID
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "ConceptSet }";

    public static final String IS_LEXICALENTRY_ID_OR_COMPONENT_ID
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.DECOMP.getSparqlPrefix() + "\n"
            + "ASK { \n"
            + "{ <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "LexicalEntry }\n"
            + " UNION \n"
            + "{ <_ID_> a " + SparqlPrefix.DECOMP.getPrefix() + "Component }\n"
            + "}";

    public static final String IS_ADMISSIBLE_HEAD_OF_COLLOCATION_ID
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + SparqlPrefix.FRAC.getSparqlPrefix() + "\n"
            + "ASK { \n"
            + "{ <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "LexicalEntry }\n"
            + " UNION \n"
            + "{ <_ID_> a " + SparqlPrefix.FRAC.getPrefix() + "Collocation }\n"
            + " UNION \n"
            + "{ <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "Form }\n"
            + " UNION \n"
            + "{ <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "LexicalSense }\n"
            + "}";

    public static final String IS_ETYMON_ID
            = SparqlPrefix.ETY.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ETY.getPrefix() + "Etymon }";

    public static final String HAS_LEXICALENTRY_CHILDREN
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> " + SparqlPrefix.ONTOLEX.getPrefix() + "sense|" + SparqlPrefix.ONTOLEX.getPrefix() + "lexicalForm ?f}";

    public static final String HAS_DICTIONARYENTRY_COMPONENTS
            = SparqlPrefix.RDFS.getSparqlPrefix() + "\n"
            + SparqlPrefix.LEXICOG.getSparqlPrefix() + "\n"
            + "ASK { \n"
            + "       { <_ID_> rdfs:member ?m }\n"
            + "          UNION \n"
            + "       { <_ID_> lexicog:subComponent ?sc }\n"
            + "    }";

    public static final String EXISTS_ID
            = "ASK { <_ID_> a ?type }";

    public static final String EXISTS_TYPE_ID
            = "ASK { <_ID_> a <_TYPE_> }";

    public static final String EXISTS_LANGUAGE
            = SparqlPrefix.LIME.getSparqlPrefix() + "\n"
            + "ASK { ?lex a " + SparqlPrefix.LIME.getPrefix() + "Lexicon ;"
            + "      " + SparqlPrefix.LIME.getPrefix() + "language \"_LANG_\" . }";

    public static final String EXISTS_DICT_LANGUAGE
            = SparqlPrefix.LIME.getSparqlPrefix() + "\n"
            + "ASK { ?lex a " + SparqlPrefix.LEXICOG.getPrefix() + "LexicographicResource ;"
            + "      " + SparqlPrefix.LIME.getPrefix() + "language \"_LANG_\" . }";

    public static final String IS_FORM_ID
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "Form }";

    public static final String IS_ETYMOLOGY_ID
            = SparqlPrefix.ETY.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ETY.getPrefix() + "Etymology }";

    public static final String IS_ETYMOLOGICAL_LINK_ID
            = SparqlPrefix.ETY.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ETY.getPrefix() + "EtyLink }";

    public static final String IS_LEXICALSENSE_ID
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "ASK { <_ID_> a " + SparqlPrefix.ONTOLEX.getPrefix() + "LexicalSense }";

    public static final String LANGUAGE
            = SparqlPrefix.LIME.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.LEXICON_LANGUAGE + " \n"
            + "WHERE { ?" + SparqlVariable.LEXICON + " " + SparqlPrefix.LIME.getPrefix() + "entry  <_ID_> ;\n"
            + "        " + SparqlPrefix.LIME.getPrefix() + "language ?" + SparqlVariable.LEXICON_LANGUAGE + " }";

    public static final String FORM_LANGUAGE
            = SparqlPrefix.LIME.getSparqlPrefix() + "\n"
            + SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.LEXICON_LANGUAGE + " \n"
            + "WHERE { ?" + SparqlVariable.LEXICAL_ENTRY + " " + SparqlPrefix.ONTOLEX.getPrefix() + "lexicalForm  <_ID_> .\n"
            + "        ?" + SparqlVariable.LEXICON + " " + SparqlPrefix.LIME.getPrefix() + "entry ?" + SparqlVariable.LEXICAL_ENTRY + " ; \n"
            + "                                        " + SparqlPrefix.LIME.getPrefix() + "language ?" + SparqlVariable.LEXICON_LANGUAGE + " . }";

    public static final String DICTIONARY_LANGUAGE
            = SparqlPrefix.LEXICOG.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.DICT_LANGUAGE + " \n"
            + "WHERE { ?" + SparqlVariable.DICT_ELEMENT + " " + SparqlPrefix.LEXICOG.getPrefix() + "entry  <_ID_> ;\n"
            + "        " + SparqlPrefix.LIME.getPrefix() + "language ?" + SparqlVariable.DICT_LANGUAGE + " }";

    public static final String LEXICAL_ENTRY_BY_FORM
            = SparqlPrefix.ONTOLEX.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.LEXICAL_ENTRY + " \n"
            + "WHERE { ?" + SparqlVariable.LEXICAL_ENTRY + " " + SparqlPrefix.ONTOLEX.getPrefix() + "lexicalForm <_ID_> }";

    public static final String LEXICAL_ENTRY_NUMBER_BY_LANGUAGE
            = SparqlPrefix.LIME.getSparqlPrefix() + "\n"
            + "SELECT (count(?count) as ?" + SparqlVariable.LABEL_COUNT + ") \n"
            + "WHERE { <_ID_> " + SparqlPrefix.LIME.getPrefix() + "entry ?count }";

    public static final String LEXICAL_ENTRY_LABEL
            = SparqlPrefix.RDFS.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.LABEL + " \n"
            + "WHERE { <_ID_> " + SparqlPrefix.RDFS.getPrefix() + "label ?" + SparqlVariable.LABEL + " }";

    public static final String UNIQUE_ID
            = "ASK { <_ID_> ?p ?o }";

    public static final String INDIRECT_RELATION_TYPE
            = SparqlPrefix.ONTO.getSparqlPrefix() + "\n"
            + SparqlPrefix.VARTRANS.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.TYPE + " \n"
            + "FROM onto:explicit\n"
            + "WHERE { <_ID_> a ?" + SparqlVariable.TYPE + " .\n"
            + "        FILTER(STRSTARTS(STR(?" + SparqlVariable.TYPE + "), str(vartrans:)))\n"
            + " }";

    public static final String IS_COGNATE
            = SparqlPrefix.ETY.getSparqlPrefix() + "\n"
            + "ASK WHERE { \n"
            + "  {\n"
            + "     SELECT (count(?" + SparqlVariable.LEXICAL_ENTRY + ") as ?" + SparqlVariable.LABEL_COUNT + ")\n"
            + "     WHERE {\n"
            + "        ?" + SparqlVariable.LEXICAL_ENTRY + " ety:cognate <_ID_> \n"
            + "     }\n"
            + "  }\n"
            + "  FILTER( ?" + SparqlVariable.LABEL_COUNT + " > _COG_NUMBER_ )\n"
            + "}";

    public static final String IMAGE_URL
            = SparqlPrefix.FOAF.getSparqlPrefix() + "\n"
            + "SELECT ?" + SparqlVariable.IDENTIFIER + " \n"
            + "WHERE { <_ID_> foaf:depiction ?" + SparqlVariable.IDENTIFIER + " }";
}
