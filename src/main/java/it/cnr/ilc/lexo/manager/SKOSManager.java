/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.manager;

import it.cnr.ilc.lexo.LexOProperties;
import it.cnr.ilc.lexo.service.data.lexicon.input.skos.SKOSUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.output.ConceptSet;
import it.cnr.ilc.lexo.service.data.lexicon.output.LexicalConcept;
import it.cnr.ilc.lexo.sparql.SparqlDeleteData;
import it.cnr.ilc.lexo.sparql.SparqlInsertData;
import it.cnr.ilc.lexo.sparql.SparqlPrefix;
import it.cnr.ilc.lexo.sparql.skos.SparqlSKOSInsert;
import it.cnr.ilc.lexo.sparql.skos.SparqlSKOSUpdate;
import it.cnr.ilc.lexo.util.RDFQueryUtil;
import it.cnr.ilc.lexo.util.SKOSEntity;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 *
 * @author andreabellandi
 */
public class SKOSManager implements Manager, Cached {

    private final String namespace = LexOProperties.getProperty("repository.lexicon.namespace");
    private final String idInstancePrefix = LexOProperties.getProperty("repository.instance.id");
    private final String lexicalConceptNs = LexOProperties.getProperty("repository.lexicon.lexicalConcept.namespace");
    private final String lexicalizationModel = LexOProperties.getProperty("skos.lexicalizationModel");
    private final List<String> allowedLanguages = Arrays.asList("it", "en", "es", "fr");

    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat(LexOProperties.getProperty("manager.operationTimestampFormat"));

    private static final ValueFactory skosFactory = SimpleValueFactory.getInstance();
    private static final UtilityManager utilityManager = ManagerFactory.getManager(UtilityManager.class);

    public String getNamespace() {
        return namespace;
    }

    @Override
    public void reloadCache() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void validateSemanticRelation(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", SKOSEntity.SemanticRelation.class, rel);
    }

    public void validateLexicalRelation(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", SKOSEntity.LexicalLabel.class, rel);
    }

    public void validateSchemeRelation(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", SKOSEntity.SchemeProperty.class, rel);
    }

    public void validateNotation(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", SKOSEntity.Notation.class, rel);
    }

    public void validateMappingRelation(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", SKOSEntity.MappingProperty.class, rel);
    }

    public void validateNoteRelation(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", SKOSEntity.NoteProperty.class, rel);
    }
    
    public void validateLexicalRelationXL(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", SKOSEntity.SKOSXLLexicalLabel.class, rel);
    }
    
    public void validateLexicalizationModel(String rel) throws ManagerException {
        switch (lexicalizationModel) {
            case "label" :
                if (!rel.equals("label")) throw new ManagerException(rel + " not recognized in the lexicalization model choosen (" + lexicalizationModel + ")");
                break;
            case "skos" :
                validateLexicalRelation(rel);
                break;
            case "skos-xl":
                validateLexicalRelationXL(rel);
                break;
        }
    }

    public void validateScheme(String scheme, String referenceScheme) throws ManagerException {
        if (referenceScheme == null) {
            if (!scheme.equals(SparqlPrefix.SKOS.getUri())) {
                throw new ManagerException(scheme + " not recognized");
            }
        } else {
            if (!scheme.equals(referenceScheme)) {
                throw new ManagerException(scheme + " not recognized in the lexicalization model choosen (" + lexicalizationModel + ")");
            }
        }
    }

    public void validateLanguage(String lang) throws ManagerException {
        if (!allowedLanguages.contains(lang)) {
            throw new ManagerException(lang + " not allowed");
        }
    }

    public LexicalConcept createLexicalConcept(String author) throws ManagerException {
        Timestamp tm = new Timestamp(System.currentTimeMillis());
        String id = idInstancePrefix + tm.toString();
        String created = timestampFormat.format(tm);
        String _id = id.replaceAll("\\s+", "").replaceAll(":", "_").replaceAll("\\.", "_");
        RDFQueryUtil.update(SparqlInsertData.CREATE_LEXICAL_CONCEPT.replaceAll("_ID_", _id)
                .replace("_AUTHOR_", author)
                .replace("_CREATED_", created)
                .replace("_MODIFIED_", created));
        return setLexicalConcept(_id, created, author);
    }

    public ConceptSet createConceptSet(String author) throws ManagerException {
        Timestamp tm = new Timestamp(System.currentTimeMillis());
        String id = idInstancePrefix + tm.toString();
        String created = timestampFormat.format(tm);
        String _id = id.replaceAll("\\s+", "").replaceAll(":", "_").replaceAll("\\.", "_");
        RDFQueryUtil.update(SparqlInsertData.CREATE_CONCEPT_SET.replaceAll("_ID_", _id)
                .replace("_AUTHOR_", author)
                .replace("_CREATED_", created)
                .replace("_MODIFIED_", created));
        return setConceptSet(_id, created, author);
    }

    private LexicalConcept setLexicalConcept(String id, String created, String author) {
        LexicalConcept lc = new LexicalConcept();
        lc.setCreator(author);
        lc.setLexicalConceptInstanceName(id);
        lc.setLexicalConcept(getNamespace() + id);
        lc.setLastUpdate(created);
        lc.setCreationDate(created);
        return lc;
    }

    private ConceptSet setConceptSet(String id, String created, String author) {
        ConceptSet cs = new ConceptSet();
        cs.setCreator(author);
        cs.setConceptSetInstanceName(id);
        cs.setConceptSet(getNamespace() + id);
        cs.setLastUpdate(created);
        cs.setCreationDate(created);
        return cs;
    }

    public void deleteLexicalConcept(String id) throws ManagerException {
        RDFQueryUtil.update(SparqlDeleteData.DELETE_LEXICAL_CONCEPT.replaceAll("_ID_", id));
    }

    public void deleteConceptSet(String id) throws ManagerException {
        RDFQueryUtil.update(SparqlDeleteData.DELETE_CONCEPT_SET.replaceAll("_ID_", id));
    }

    public String updateSemanticRelation(SKOSUpdater su) throws ManagerException {
        // all the values of SKOSUpdater are full IRIs
        if (!inputCheck(su, false)) {
            throw new ManagerException("input json does not contain all the needed fields");
        }
        if (utilityManager.existsGenericRelation(su.getSource(), su.getRelation(), "<" + su.getTarget() + ">")) {
            // update
            entityCheck(su.getOldTarget(), lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString());
            return update(su);
        } else {
            // new
            relationCheck(su.getRelation());
            IRI relation = skosFactory.createIRI(su.getRelation());
            validateScheme(relation.getNamespace(), null);
            validateSemanticRelation(relation.getLocalName());
            entityCheck(su.getSource(), lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString());
            entityCheck(su.getTarget(), lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString());
            return create(su);
        }
    }

    public String updateSchemeProperty(SKOSUpdater su) throws ManagerException {
        // all the values of SKOSUpdater are full IRIs
        if (!inputCheck(su, false)) {
            throw new ManagerException("input json does not contain all the needed fields");
        }
        if (utilityManager.existsGenericRelation(su.getSource(), su.getRelation(), "<" + su.getTarget() + ">")) {
            // update
            entityCheck(su.getOldTarget(),
                    su.getRelation().equals(SKOSEntity.SchemeProperty.hasTopConcept.toString())
                    ? lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString() : lexicalConceptNs + SKOSEntity.SKOSClass.ConceptScheme.toString());
            return update(su);
        } else {
            // new
            relationCheck(su.getRelation());
            IRI relation = skosFactory.createIRI(su.getRelation());
            validateScheme(relation.getNamespace(), null);
            validateSchemeRelation(relation.getLocalName());
            entityCheck(su.getSource(), su.getRelation().equals(SKOSEntity.SchemeProperty.hasTopConcept.toString())
                    ? lexicalConceptNs + SKOSEntity.SKOSClass.ConceptScheme.toString() : lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString());
            entityCheck(su.getTarget(), su.getRelation().equals(SKOSEntity.SchemeProperty.hasTopConcept.toString())
                    ? lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString() : lexicalConceptNs + SKOSEntity.SKOSClass.ConceptScheme.toString());
            return create(su);
        }
    }

    public String updateMappingProperty(SKOSUpdater su) throws ManagerException {
        // all the values of SKOSUpdater are full IRIs
        if (!inputCheck(su, false)) {
            throw new ManagerException("input json does not contain all the needed fields");
        }
        if (utilityManager.existsGenericRelation(su.getSource(), su.getRelation(), "<" + su.getTarget() + ">")) {
            // update
            entityCheck(su.getOldTarget(), lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString());
            return update(su);
        } else {
            // new
            relationCheck(su.getRelation());
            IRI relation = skosFactory.createIRI(su.getRelation());
            validateScheme(relation.getNamespace(), null);
            validateMappingRelation(relation.getLocalName());
            entityCheck(su.getSource(), lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString());
            entityCheck(su.getTarget(), lexicalConceptNs + SKOSEntity.SKOSClass.Concept.toString());
            return create(su);
        }
    }

    public String updateNotation(SKOSUpdater su) throws ManagerException {
        // all the values of SKOSUpdater are full IRIs
        if (!inputCheck(su, true)) {
            throw new ManagerException("input json does not contain all the needed fields");
        }
        if (utilityManager.existsGenericRelation(su.getSource(), su.getRelation(), "\"" + su.getTarget() + "\"@" + su.getLanguage())) {
            // update
            return updateLiteral(su);
        } else {
            // new
            relationCheck(su.getRelation());
            IRI relation = skosFactory.createIRI(su.getRelation());
            validateScheme(relation.getNamespace(), null);
            validateNotation(relation.getLocalName());
            validateLanguage(su.getLanguage());
            return createLiteral(su);
        }
    }

    public String updateNoteProperty(SKOSUpdater su) throws ManagerException {
        // all the values of SKOSUpdater are full IRIs
        if (!inputCheck(su, true)) {
            throw new ManagerException("input json does not contain all the needed fields");
        }
        if (utilityManager.existsGenericRelation(su.getSource(), su.getRelation(), "\"" + su.getTarget() + "\"@" + su.getLanguage())) {
            // update
            return updateLiteral(su);
        } else {
            // new
            relationCheck(su.getRelation());
            IRI relation = skosFactory.createIRI(su.getRelation());
            validateScheme(relation.getNamespace(), null);
            validateNoteRelation(relation.getLocalName());
            validateLanguage(su.getLanguage());
            return createLiteral(su);
        }
    }

    public String updateLexicalProperty(SKOSUpdater su) throws ManagerException {
        // all the values of SKOSUpdater are full IRIs
        // label and skos supported only (skos-xl to be implemented)
        if (!inputCheck(su, true)) {
            throw new ManagerException("input json does not contain all the needed fields");
        }
        switch (lexicalizationModel) {
            case "label":
                return updateLexicalPropertyLabel(su);
            case "skos":
                return updateLexicalPropertySKOS(su);
            case "skos-xl":
                return updateLexicalPropertySKOSXL(su);
            default:
        }
        return null;
    }

    private String updateLexicalPropertyLabel(SKOSUpdater su) throws ManagerException {
        if (utilityManager.existsGenericRelation(su.getSource(), su.getRelation(), "\"" + su.getTarget() + "\"@" + su.getLanguage())) {
            // update
            return updateLiteral(su);
        } else {
            // new
            relationCheck(su.getRelation());
            IRI relation = skosFactory.createIRI(su.getRelation());
            validateScheme(relation.getNamespace(), SparqlPrefix.RDFS.getUri());
            validateLexicalizationModel(relation.getLocalName());
            validateLanguage(su.getLanguage());
            return createLiteral(su);
        }
    }

    private String updateLexicalPropertySKOS(SKOSUpdater su) throws ManagerException {
        if (utilityManager.existsGenericRelation(su.getSource(), su.getRelation(), "\"" + su.getTarget() + "\"@" + su.getLanguage())) {
            // update
            return updateLiteral(su);
        } else {
            // new
            relationCheck(su.getRelation());
            IRI relation = skosFactory.createIRI(su.getRelation());
            validateScheme(relation.getNamespace(), SparqlPrefix.RDFS.getUri());
            validateLexicalizationModel(relation.getLocalName());
            validateLanguage(su.getLanguage());
            return createLiteral(su);
        }
    }

    private String updateLexicalPropertySKOSXL(SKOSUpdater su) throws ManagerException {
        throw new ManagerException("to implement");
    }

    private String update(SKOSUpdater su) {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlSKOSUpdate.UPDATE_RELATION.replaceAll("_ID_", su.getSource())
                .replaceAll("_RELATION_", "<" + su.getRelation() + ">")
                .replaceAll("_VALUE_TO_INSERT_", "<" + su.getTarget() + ">")
                .replaceAll("_VALUE_TO_DELETE_", "<" + su.getOldTarget() + ">")
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    private String updateLiteral(SKOSUpdater su) {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlSKOSUpdate.UPDATE_RELATION.replaceAll("_ID_", su.getSource())
                .replaceAll("_RELATION_", "<" + su.getRelation() + ">")
                .replaceAll("_VALUE_TO_INSERT_", "\"" + su.getTarget() + "\"@" + su.getLanguage())
                .replaceAll("_VALUE_TO_DELETE_", "\"" + su.getOldTarget() + "\"@" + su.getLanguage())
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    private String create(SKOSUpdater su) {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlSKOSInsert.CREATE_RELATION.replaceAll("_ID_", su.getSource())
                .replaceAll("_RELATION_", "<" + su.getRelation() + ">")
                .replaceAll("_VALUE_TO_INSERT_", "<" + su.getTarget() + ">")
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    private String createLiteral(SKOSUpdater su) {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlSKOSInsert.CREATE_RELATION.replaceAll("_ID_", su.getSource())
                .replaceAll("_RELATION_", "<" + su.getRelation() + ">")
                .replaceAll("_VALUE_TO_INSERT_", "\"" + su.getTarget() + "\"@" + su.getLanguage())
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    private boolean inputCheck(SKOSUpdater su, boolean literal) {
        if (su.getRelation() != null && su.getSource() != null && su.getTarget() != null) {
            if (!su.getRelation().isEmpty() && !su.getSource().isEmpty() && !su.getTarget().isEmpty()) {
                if (literal) {
                    if (su.getLanguage() != null) {
                        if (!su.getLanguage().isEmpty()) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void relationCheck(String rel) throws ManagerException {
        if (rel == null) {
            throw new ManagerException("relation field cannot be null");
        }
        if (rel.isEmpty()) {
            throw new ManagerException("relation field cannot be empty");
        }
    }

    private void entityCheck(String val, String type) throws ManagerException {
        if (val == null) {
            throw new ManagerException("source or target fields cannot be null");
        }
        if (val.isEmpty()) {
            throw new ManagerException("source or target fields cannot be empty");
        }
        if (!utilityManager.existsTyped(val, type)) {
            throw new ManagerException(val + "does not exist or it has no " + type + " type");
        }
    }

}
