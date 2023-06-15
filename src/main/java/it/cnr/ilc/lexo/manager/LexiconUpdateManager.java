/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.manager;

import it.cnr.ilc.lexo.LexOProperties;
import it.cnr.ilc.lexo.service.data.lexicon.input.ComponentPositionUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.EtymologicalLinkUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.EtymologyUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.FormUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.GenericRelationUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.LanguageUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalEntryUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.LexicalSenseUpdater;
import it.cnr.ilc.lexo.service.data.lexicon.input.LinguisticRelationUpdater;
import it.cnr.ilc.lexo.sparql.SparqlDeleteData;
import it.cnr.ilc.lexo.sparql.SparqlInsertData;
import it.cnr.ilc.lexo.sparql.SparqlPrefix;
import it.cnr.ilc.lexo.sparql.SparqlSelectData;
import it.cnr.ilc.lexo.sparql.SparqlUpdateData;
import it.cnr.ilc.lexo.sparql.SparqlVariable;
import it.cnr.ilc.lexo.util.EnumUtil;
import it.cnr.ilc.lexo.util.OntoLexEntity;
import it.cnr.ilc.lexo.util.RDFQueryUtil;
import it.cnr.ilc.lexo.util.StringUtil;
import it.cnr.ilc.lexo.util.RelationCategory;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.UpdateExecutionException;

/**
 *
 * @author andreabellandi
 */
public final class LexiconUpdateManager implements Manager, Cached {

    public final String URL_PATTERN = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";
    public final Pattern pattern = Pattern.compile(URL_PATTERN);
    private final String lexvoPrefixWWW = "http://www.lexvo.org/";
    private final String libraryOfCongressPrefixWWW = "http://www.id.loc.gov/vocabulary/";
    private final String lexvoPrefix = "http://lexvo.org/";
    private final String libraryOfCongressPrefix = "http://id.loc.gov/vocabulary/";
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat(LexOProperties.getProperty("manager.operationTimestampFormat"));

    @Override
    public void reloadCache() {
    }

    private void validateConfidenceValue(String value) throws ManagerException {
        double v = Double.parseDouble(value);
        if ((v < 0.0) || (v > 1.0)) {
            throw new ManagerException("the confidence value is not valid");
        }
    }

    public void validateLexiconLanguageAttribute(String attribute) throws ManagerException {
        Manager.validateWithEnum("attribute", OntoLexEntity.LanguageAttributes.class, attribute);
    }

    public void validateLexicalEntryAttribute(String attribute) throws ManagerException {
        Manager.validateWithEnum("attribute", OntoLexEntity.LexicalEntryAttributes.class, attribute);
    }

    public void validateLexicalEntryType(String type) throws ManagerException {
        Manager.validateWithEnum("type", OntoLexEntity.LexicalEntryTypes.class, type);
    }

    public void validateFormType(String type) throws ManagerException {
        Manager.validateWithEnum("type", OntoLexEntity.FormTypes.class, type);
    }

    public void validateConceptRef(String type) throws ManagerException {
        Manager.validateWithEnum("type", OntoLexEntity.ReferenceTypes.class, type);
    }

    public void validateLexicoSemanticRel(String type) throws ManagerException {
        Manager.validateWithEnum("type", OntoLexEntity.LexicoSemanticProperty.class, type);
    }
    
    public void validateTranslationSet(String type) throws ManagerException {
        Manager.validateWithEnum("type", OntoLexEntity.TranslationSet.class, type);
    }

    public void validateLexicoSemanticValue(String type, String value) throws ManagerException {
        if (!Manager.validateLexinfo(type, value)) {
            throw new ManagerException("value " + value + " is not valid");
        }
    }

    public void validateLexicalRel(String rel) throws ManagerException {
        if (rel.contains(SparqlPrefix.LEXINFO.getUri())) {
            if (!Manager.validateLexinfo(RelationCategory.LEXICAL_RELATION, rel)) {
                throw new ManagerException("relation " + rel + " is not valid");
            }
        } else {
            Manager.validateWithEnum("rel", OntoLexEntity.LexicalRel.class, rel);
        }
    }

    public void validateSenseRel(String rel) throws ManagerException {
        if (rel.contains(SparqlPrefix.LEXINFO.getUri())) {
            if (!Manager.validateLexinfo(RelationCategory.SENSE_RELATION, rel)) {
                throw new ManagerException("lexinfo relation is not valid");
            }
        } else {
            Manager.validateWithEnum("rel", OntoLexEntity.SenseRel.class, rel);
        }
    }

    public void validateConceptRel(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", OntoLexEntity.LexicalConceptRel.class, rel);
    }

    public void validateFormRel(String rel) throws ManagerException {
        if (rel.contains(SparqlPrefix.LEXINFO.getUri())) {
            if (!Manager.validateLexinfo(RelationCategory.FORM_RELATION, rel)) {
                throw new ManagerException("lexinfo relation is not valid");
            }
        }
    }

    public void validateLexicon(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", OntoLexEntity.Lexicon.class, rel);
    }

    public void validateDecomp(String rel) throws ManagerException {
        Manager.validateWithEnum("rel", OntoLexEntity.Decomp.class, rel);
    }

    public void validateLexicalEntryStatus(String status) throws ManagerException {
        Manager.validateWithEnum("status", EnumUtil.LexicalEntryStatus.class, status);
    }

    public void validateLexicalEntryLanguage(String lang) throws ManagerException {
        Manager.validateLanguage(lang);
    }

    public void validateGenericReferenceRelation(String relation) throws ManagerException {
        Manager.validateWithEnum("relation", EnumUtil.GenericRelationReference.class, relation);
    }

    public void validatePositionRelation(String relation) throws ManagerException {
        Manager.validateWithEnum("relation", EnumUtil.PositionRelation.class, relation);
    }

    public void validateGenericBibliographyRelation(String relation) throws ManagerException {
        Manager.validateWithEnum("relation", EnumUtil.GenericRelationBibliography.class, relation);
    }

    public void validateGenericDecompRelation(String relation) throws ManagerException {
        Manager.validateWithEnum("relation", OntoLexEntity.GenericRelationDecomp.class, relation);
    }
    
    public void validateMetadataTypes(String type) throws ManagerException {
        Manager.validateWithEnum("type", EnumUtil.MetadataTypes.class, type);
    }

    public void validateEtyLink(String type) throws ManagerException {
        Manager.validateWithEnum("type", OntoLexEntity.EtyLinkTypes.class, type);
    }

    public void validateURL(String url, String... urlPrefix) throws ManagerException {
        if (!StringUtil.validateURL(url)) {
            throw new ManagerException(url + " is not a valid url");
        } else {
            if (urlPrefix.length > 0) {
                if (!StringUtil.prefixedURL(url, urlPrefix)) {
                    throw new ManagerException(url + " is not an admissible url");
                }
            }
        }
    }

    public void languageUpdatePermission(String id) throws ManagerException {
        if (ManagerFactory.getManager(UtilityManager.class).lexicalEntriesNumberByLanguage(id) > 0) {
            throw new ManagerException(" Language cannot be modified or deleted. Remove all its entries first");
        }
    }

    public void validateMorphology(String trait, String value) throws ManagerException {
        Manager.validateMorphology(trait, value);
    }

    public void validateFormAttribute(String attribute) throws ManagerException {
        Manager.validateWithEnum("attribute", OntoLexEntity.FormAttributes.class, attribute);
    }

    public void validateLexicalSenseAttribute(String attribute) throws ManagerException {
        Manager.validateWithEnum("attribute", OntoLexEntity.LexicalSenseAttributes.class, attribute);
    }

    public void validateEtymologyAttribute(String attribute) throws ManagerException {
        Manager.validateWithEnum("attribute", OntoLexEntity.EtymologyAttributes.class, attribute);
    }

    public void validateEtymologicalLinkAttribute(String attribute) throws ManagerException {
        Manager.validateWithEnum("attribute", OntoLexEntity.EtymologicalLinkAttributes.class, attribute);
    }

    public String updateLexiconLanguage(String id, LanguageUpdater lu) throws ManagerException {
        validateLexiconLanguageAttribute(lu.getRelation());
        if (lu.getRelation().equals(OntoLexEntity.LanguageAttributes.Lexvo.toString())) {
            validateURL(lu.getValue(), lexvoPrefix, libraryOfCongressPrefix, lexvoPrefixWWW, libraryOfCongressPrefixWWW);
            return updateLexiconLanguage(id, lu.getRelation(), "<" + lu.getValue() + ">");
        } else if (lu.getRelation().equals(OntoLexEntity.LanguageAttributes.Description.toString())) {
            return updateLexiconLanguage(id, lu.getRelation(), "\"" + lu.getValue() + "\"");
        } else if (lu.getRelation().equals(OntoLexEntity.LanguageAttributes.Confidence.toString())) {
            validateConfidenceValue(lu.getValue());
            return updateLexiconLanguage(id, lu.getRelation(), lu.getValue());
        } else if (lu.getRelation().equals(OntoLexEntity.LanguageAttributes.Language.toString())) {
            languageUpdatePermission(id);
            return updateLexiconLanguage(id, lu.getRelation(), "\"" + lu.getValue() + "\"");
        } else {
            return null;
        }
    }

    public String updateLexiconLanguage(String id, String relation, String valueToInsert) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty()) {
            throw new ManagerException("value cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_LEXICON_LANGUAGE.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", "?" + SparqlVariable.TARGET)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));

        return lastupdate;
    }

    public String updateLexicalEntry(String id, String relation, String valueToInsert, String valueToDelete) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty()) {
            throw new ManagerException("value cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_LEXICAL_ENTRY.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", valueToDelete)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    public String updateLexicalEntry(String id, LexicalEntryUpdater leu, String user, String type) throws ManagerException {
        validateLexicalEntryAttribute(leu.getRelation());
        if (leu.getRelation().equals(OntoLexEntity.LexicalEntryAttributes.Label.toString())) {
            String lang = ManagerFactory.getManager(UtilityManager.class).getLanguage(id);
            return updateLexicalEntry(id, leu.getRelation(),
                    (lang != null) ? ("\"" + leu.getValue() + "\"@" + lang) : "\"" + leu.getValue() + "\"", "?" + SparqlVariable.TARGET);
        } else if (leu.getRelation().equals(OntoLexEntity.LexicalEntryAttributes.Type.toString())) {
            validateLexicalEntryType(leu.getValue());
            return updateLexicalEntry(id, leu.getRelation(), "<" + leu.getValue() + ">", "<" + type + ">");
        } else if (leu.getRelation().equals(OntoLexEntity.LexicalEntryAttributes.Status.toString())) {
            validateLexicalEntryStatus(leu.getValue());
            if (leu.getValue().isEmpty()) {
                throw new ManagerException("status cannot be empty");
            }
            return updateStatus(id, leu, user);
        } else if (leu.getRelation().equals(OntoLexEntity.LexicalEntryAttributes.Language.toString())) {
            validateLexicalEntryLanguage(leu.getValue());
            return updateLanguage(id, leu);
        } else if (leu.getRelation().equals(OntoLexEntity.LexicalEntryAttributes.Note.toString())) {
            return updateLexicalEntry(id, leu.getRelation(), "\"" + leu.getValue() + "\"", "?" + SparqlVariable.TARGET);
        } else if (leu.getRelation().equals(OntoLexEntity.LanguageAttributes.Confidence.toString())) {
            validateConfidenceValue(leu.getValue());
            return updateLexicalEntry(id, leu.getRelation(),
                    leu.getValue(),
                    "?" + SparqlVariable.TARGET);
        } else {
            return null;
        }
    }

    private String updateLanguage(String id, String label, String lang) {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_LEXICAL_ENTRY_LANGUAGE.replaceAll("_ID_", id)
                .replaceAll("_LABEL_", label)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\"")
                .replaceAll("_LANG_", lang));

        return lastupdate;
    }

    private String updateLanguage(String id, LexicalEntryUpdater leu) throws QueryEvaluationException {
        String label = getLabel(id);
        if (label != null) {
            return updateLanguage(id, label, leu.getValue());
        }
        return null;
    }

    public String getLabel(String id) throws QueryEvaluationException {
        try ( TupleQueryResult result = RDFQueryUtil.evaluateTQuery(SparqlSelectData.LEXICON_ENTRY_LANGUAGE.replaceAll("_ID_", id))) {

            while (result.hasNext()) {
                BindingSet bs = result.next();
                return (bs.getBinding(SparqlVariable.LABEL) != null) ? ((Literal) bs.getBinding(SparqlVariable.LABEL).getValue()).getLabel() : null;
            }
        } catch (QueryEvaluationException qee) {
        }
        return null;
    }

    private String getStatus(String id) {
        try ( TupleQueryResult result = RDFQueryUtil.evaluateTQuery(SparqlSelectData.LEXICON_ENTRY_STATUS.replaceAll("_ID_", id))) {
            while (result.hasNext()) {
                BindingSet bs = result.next();
                return (bs.getBinding(SparqlVariable.LABEL) != null) ? ((Literal) bs.getBinding(SparqlVariable.LABEL).getValue()).getLabel() : null;
            }
        } catch (QueryEvaluationException qee) {
        }
        return null;
    }

    private String updateStatus(String id, LexicalEntryUpdater leu, String user) throws QueryEvaluationException, ManagerException {
        String status = getStatus(id);
        if (status != null) {
            if (checkStatus(status, leu.getValue())) {
                if (compareAdmissibleStatus(status, leu.getValue()) == 1) {
                    return statusForewarding(id, leu.getValue(), status, user);
                } else {
                    return statusBackwarding(id, leu.getValue(), status, user);
                }
            } else {
                throw new ManagerException("The current status " + status + " cannot be changed to " + leu.getValue());
            }
        }
        return null;
    }

    private String statusForewarding(String id, String status, String currentStatus, String user) throws QueryEvaluationException {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_LEXICAL_ENTRY_FOREWARDING_STATUS.replaceAll("_ID_", id)
                .replaceAll("_NEW_ROLE_", getRoleName(status))
                .replaceAll("_NEW_DATE_", getDateName(status))
                .replaceAll("_USER_", "\"" + user + "\"")
                .replaceAll("_STATUS_", "\"" + status + "\"")
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));

        return lastupdate;
    }

    private String statusBackwarding(String id, String status, String currentStatus, String user) throws QueryEvaluationException {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_LEXICAL_ENTRY_BACKWARDING_STATUS.replaceAll("_ID_", id)
                .replaceAll("_CURRENT_ROLE_", getRoleName(currentStatus))
                .replaceAll("_CURRENT_DATE_", getDateName(currentStatus))
                .replaceAll("_NEW_ROLE_", getRoleName(status))
                .replaceAll("_NEW_DATE_", getDateName(status))
                .replaceAll("_USER_", "\"" + user + "\"")
                .replaceAll("_STATUS_", "\"" + status + "\"")
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    private String getRoleName(String status) {
        if (status.equals(EnumUtil.LexicalEntryStatus.Completed.toString())) {
            return SparqlPrefix.DCT.getPrefix() + "author";
        } else if (status.equals(EnumUtil.LexicalEntryStatus.Reviewed.toString())) {
            return SparqlPrefix.LOC.getPrefix() + "rev";
        } else if (status.equals(EnumUtil.LexicalEntryStatus.Working.toString())) {
            return SparqlPrefix.DCT.getPrefix() + "creator";
        } else {
            return null;
        }
    }

    private String getDateName(String status) {
        if (status.equals(EnumUtil.LexicalEntryStatus.Completed.toString())) {
            return SparqlPrefix.DCT.getPrefix() + "dateSubmitted";
        } else if (status.equals(EnumUtil.LexicalEntryStatus.Reviewed.toString())) {
            return SparqlPrefix.DCT.getPrefix() + "dateAccepted";
        } else if (status.equals(EnumUtil.LexicalEntryStatus.Working.toString())) {
            return SparqlPrefix.DCT.getPrefix() + "created";
        } else {
            return null;
        }
    }

    private boolean checkStatus(String current, String next) {
        if (current.equals(next)) {
            return false;
        } else if (current.equals(EnumUtil.LexicalEntryStatus.Working.toString())
                && (next.equals(EnumUtil.LexicalEntryStatus.Reviewed.toString()))) {
            return false;
        } else if (current.equals(EnumUtil.LexicalEntryStatus.Reviewed.toString())
                && (next.equals(EnumUtil.LexicalEntryStatus.Working.toString()))) {
            return false;
        } else {
            return true;
        }
    }

    // 1 next > current; -1 current > next
    private int compareAdmissibleStatus(String current, String next) {
        if (current.equals(EnumUtil.LexicalEntryStatus.Working.toString())) {
            if (next.equals(EnumUtil.LexicalEntryStatus.Completed.toString())) {
                return 1;
            } else if (next.equals(EnumUtil.LexicalEntryStatus.Reviewed.toString())) {
                return 1;
            }
        } else if (current.equals(EnumUtil.LexicalEntryStatus.Completed.toString())) {
            if (next.equals(EnumUtil.LexicalEntryStatus.Working.toString())) {
                return -1;
            } else if (next.equals(EnumUtil.LexicalEntryStatus.Reviewed.toString())) {
                return 1;
            }
        } else if (current.equals(EnumUtil.LexicalEntryStatus.Reviewed.toString())) {
            if (next.equals(EnumUtil.LexicalEntryStatus.Completed.toString())) {
                return -1;
            } else if (next.equals(EnumUtil.LexicalEntryStatus.Working.toString())) {
                return -1;
            }
        }
        return 0;
    }

    public String updateForm(String id, FormUpdater fu) throws ManagerException {
        UtilityManager utilityManager = ManagerFactory.getManager(UtilityManager.class);
        validateFormAttribute(fu.getRelation());
        if (fu.getRelation().equals(OntoLexEntity.FormAttributes.Type.toString())) {
            validateFormType(fu.getValue());
            String leid = utilityManager.getLexicalEntryByForm(id);
            return updateFormType(id, leid, fu.getValue());
        } else if (fu.getRelation().equals(OntoLexEntity.FormAttributes.Note.toString())) {
            return updateForm(id, fu.getRelation(), "\"" + fu.getValue() + "\"");
        } else if (fu.getRelation().equals(OntoLexEntity.FormAttributes.WrittenRep.toString())) {
            String lang = utilityManager.getLanguage(id);
            return updateForm(id, fu.getRelation(),
                    (lang != null) ? ("\"" + fu.getValue() + "\"@" + lang) : "\"" + fu.getValue() + "\"");
        } else if (fu.getRelation().equals(OntoLexEntity.FormAttributes.PhoneticRep.toString())) {
            String lang = utilityManager.getLanguage(id);
            return updateForm(id, fu.getRelation(),
                    (lang != null) ? ("\"" + fu.getValue() + "\"@" + lang) : "\"" + fu.getValue() + "\"");
        } else if (fu.getRelation().equals(OntoLexEntity.FormAttributes.Transliteration.toString())) {
            String lang = utilityManager.getLanguage(id);
            return updateForm(id, fu.getRelation(),
                    (lang != null) ? ("\"" + fu.getValue() + "\"@" + lang) : "\"" + fu.getValue() + "\"");
        } else if (fu.getRelation().equals(OntoLexEntity.FormAttributes.Segmentation.toString())) {
            String lang = utilityManager.getLanguage(id);
            return updateForm(id, fu.getRelation(),
                    (lang != null) ? ("\"" + fu.getValue() + "\"@" + lang) : "\"" + fu.getValue() + "\"");
        } else if (fu.getRelation().equals(OntoLexEntity.FormAttributes.Pronunciation.toString())) {
            String lang = utilityManager.getLanguage(id);
            return updateForm(id, fu.getRelation(),
                    (lang != null) ? ("\"" + fu.getValue() + "\"@" + lang) : "\"" + fu.getValue() + "\"");
        } else if (fu.getRelation().equals(OntoLexEntity.FormAttributes.Romanization.toString())) {
            String lang = utilityManager.getLanguage(id);
            return updateForm(id, fu.getRelation(),
                    (lang != null) ? ("\"" + fu.getValue() + "\"@" + lang) : "\"" + fu.getValue() + "\"");
        } else if (fu.getRelation().equals(OntoLexEntity.LanguageAttributes.Confidence.toString())) {
            validateConfidenceValue(fu.getValue());
            return updateForm(id, fu.getRelation(), fu.getValue());
        } else {
            return null;
        }
    }

    public String updateForm(String id, String relation, String valueToInsert) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty()) {
            throw new ManagerException("value cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_FORM.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", "?" + SparqlVariable.TARGET)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));

        return lastupdate;
    }

    public String updateFormType(String id, String leid, String formType) throws ManagerException, UpdateExecutionException {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_FORM_TYPE.replaceAll("_ID_", id)
                .replaceAll("_LEID_", leid)
                .replaceAll("_FORM_TYPE_", formType)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    public String updateLexicalSense(String id, LexicalSenseUpdater lsu) throws ManagerException {
        validateLexicalSenseAttribute(lsu.getRelation());
        if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Note.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "\"" + lsu.getValue() + "\"");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Definition.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "\"" + lsu.getValue() + "\"");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Description.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "\"" + lsu.getValue() + "\"");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Explanation.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "\"" + lsu.getValue() + "\"");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Gloss.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "\"" + lsu.getValue() + "\"");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Reference.toString())) {
            validateURL(lsu.getValue());
            return updateLexicalSense(id, lsu.getRelation(), "<" + lsu.getValue() + ">");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.SenseExample.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "\"" + lsu.getValue() + "\"");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.SenseTranslation.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "\"" + lsu.getValue() + "\"");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Topic.toString())) {
            validateURL(lsu.getValue());
            return updateLexicalSense(id, lsu.getRelation(), "<" + lsu.getValue() + ">");
        } else if (lsu.getRelation().equals(OntoLexEntity.LexicalSenseAttributes.Usage.toString())) {
            return updateLexicalSense(id, lsu.getRelation(), "[ rdf:value \"" + lsu.getValue() + "\" ]");
        } else if (lsu.getRelation().equals(OntoLexEntity.LanguageAttributes.Confidence.toString())) {
            validateConfidenceValue(lsu.getValue());
            return updateLexicalSense(id, lsu.getRelation(), lsu.getValue());
        } else {
            return null;
        }
    }

    public String updateLexicalSense(String id, String relation, String valueToInsert) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty()) {
            throw new ManagerException("value cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_LEXICAL_SENSE.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", "?" + SparqlVariable.TARGET)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    public String updateEtymology(String id, EtymologyUpdater eu) throws ManagerException {
        validateEtymologyAttribute(eu.getRelation());
        if (eu.getRelation().equals(OntoLexEntity.EtymologyAttributes.Note.toString())) {
            return updateEtymology(id, eu.getRelation(), "\"" + eu.getValue() + "\"");
        } else if (eu.getRelation().equals(OntoLexEntity.EtymologyAttributes.Label.toString())) {
            return updateEtymology(id, eu.getRelation(), "\"" + eu.getValue() + "\"");
        } else if (eu.getRelation().equals(OntoLexEntity.EtymologyAttributes.HypothesisOf.toString())) {
            return updateEtymology(id, eu.getRelation(), "\"" + eu.getValue() + "\"");
        } else if (eu.getRelation().equals(OntoLexEntity.LanguageAttributes.Confidence.toString())) {
            validateConfidenceValue(eu.getValue());
            return updateEtymology(id, eu.getRelation(), eu.getValue());
        } else {
            return null;
        }
    }

    public String updateEtymologicalLink(String id, EtymologicalLinkUpdater elu) throws ManagerException {
        validateEtymologicalLinkAttribute(elu.getRelation());
        if (elu.getRelation().equals(OntoLexEntity.EtymologicalLinkAttributes.Note.toString())) {
            return updateEtymologicalLink(id, elu.getRelation(), "\"" + elu.getValue() + "\"");
        } else if (elu.getRelation().equals(OntoLexEntity.EtymologicalLinkAttributes.Type.toString())) {
            return updateEtymologicalLink(id, elu.getRelation(), "\"" + elu.getValue() + "\"");
        } else if (elu.getRelation().equals(OntoLexEntity.EtymologicalLinkAttributes.Label.toString())) {
            return updateEtymologicalLink(id, elu.getRelation(), "\"" + elu.getValue() + "\"");
        } else if (elu.getRelation().equals(OntoLexEntity.LanguageAttributes.Confidence.toString())) {
            validateConfidenceValue(elu.getValue());
            return updateEtymologicalLink(id, elu.getRelation(), elu.getValue());
        } else {
            return null;
        }
    }

    public String updateEtymology(String id, String relation, String valueToInsert) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty()) {
            throw new ManagerException("value cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_ETYMOLOGY.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", "?" + SparqlVariable.TARGET)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    public String updateEtymologicalLink(String id, String relation, String valueToInsert) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty()) {
            throw new ManagerException("value cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_ETYMOLOGICAL_LINK.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", "?" + SparqlVariable.TARGET)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    public String updateLinguisticRelation(String id, LinguisticRelationUpdater lru) throws ManagerException {
        if (lru.getType().equals(EnumUtil.LinguisticRelation.Morphology.toString())) {
            validateMorphology(lru.getRelation(), lru.getValue());
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.ConceptRef.toString())) {
            // currently the property ranges over an external url only 
            validateURL(lru.getValue());
            validateConceptRef(lru.getRelation());
            if (ManagerFactory.getManager(UtilityManager.class).existsNamespace(SimpleValueFactory.getInstance().createIRI(lru.getValue()).getNamespace())) {
                throw new ManagerException("External links supported only");
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.LexicoSemanticRel.toString())) {
            // vartrans reified relation
            validateLexicoSemanticRel(lru.getRelation());
            if (lru.getType().contains(OntoLexEntity.LexicoSemanticProperty.Category.toString())) {
                validateLexicoSemanticValue(RelationCategory.LEXICAL_CATEGORY, lru.getValue());
                validateLexicoSemanticValue(RelationCategory.SEMANTIC_CATEGORY, lru.getValue());
                validateLexicoSemanticValue(RelationCategory.FORM_CATEGORY, lru.getValue());
            } else {
                if (!StringUtil.existsIRI(lru.getValue())) {
                    validateURL(lru.getValue());
                }
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.EtymologicalLink.toString())) {
            validateEtyLink(lru.getRelation());
            if (!StringUtil.existsIRI(lru.getValue())) {
                validateURL(lru.getValue());
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.TranslationSet.toString())) {
            validateTranslationSet(lru.getRelation());
            if (!StringUtil.existsIRI(lru.getValue())) {
                validateURL(lru.getValue());
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.LexicalRel.toString())) {
            validateLexicalRel(lru.getRelation());
            if (!StringUtil.existsIRI(lru.getValue())) {
                validateURL(lru.getValue());
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.SenseRel.toString())) {
            validateSenseRel(lru.getRelation());
            if (!StringUtil.existsIRI(lru.getValue())) {
                validateURL(lru.getValue());
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.FormRel.toString())) {
            validateFormRel(lru.getRelation());
            if (!StringUtil.existsIRI(lru.getValue())) {
                validateURL(lru.getValue());
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.Decomp.toString())) {
            validateDecomp(lru.getRelation());
            // currently external IRIs are not considered
            if (lru.getRelation().equals(OntoLexEntity.Decomp.SubTerm.toString())) {
                if (StringUtil.existsIRI(lru.getValue())) {
                } else {
                    throw new ManagerException(lru.getValue() + " is not a valid Lexical Entry");
                }
            } else if (lru.getRelation().equals(OntoLexEntity.Decomp.Constituent.toString())) {
                if (StringUtil.existsIRI(lru.getValue())) {
                } else {
                    throw new ManagerException(lru.getValue() + " is not a valid Lexical Entry");
                }
            } else if (lru.getRelation().equals(OntoLexEntity.Decomp.CorrespondsTo.toString())) {
                if (StringUtil.existsTypedIRI(lru.getValue(), OntoLexEntity.LexicalEntryTypes.LexicalEntry.toString())) {
                } else {
                    throw new ManagerException(lru.getValue() + " is not a valid Lexical Entry");
                }
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.ConceptRel.toString())) {
            validateConceptRel(lru.getRelation());
            if (lru.getRelation().equals(OntoLexEntity.LexicalConceptRel.evokes.toString())) {
                if (StringUtil.existsTypedIRI(lru.getValue(), OntoLexEntity.LexicalConcepts.LexicalConcept.toString())) {
                } else {
                    throw new ManagerException(lru.getValue() + " is not a valid Lexical Concept");
                }
            } else if (lru.getRelation().equals(OntoLexEntity.LexicalConceptRel.isEvokedBy.toString())) {
                if (StringUtil.existsTypedIRI(lru.getValue(), OntoLexEntity.LexicalEntryTypes.LexicalEntry.toString())) {
                } else {
                    throw new ManagerException(lru.getValue() + " is not a valid Lexical entry");
                }
            } else if (lru.getRelation().equals(OntoLexEntity.LexicalConceptRel.lexicalizedSense.toString())) {
                if (StringUtil.existsTypedIRI(lru.getValue(), OntoLexEntity.CoreClasses.LexicalSense.toString())) {
                } else {
                    throw new ManagerException(lru.getValue() + " is not a valid Lexical sense");
                }
            } else if (lru.getRelation().equals(OntoLexEntity.LexicalConceptRel.isLexicalizedSenseOf.toString())) {
                if (StringUtil.existsTypedIRI(lru.getValue(), OntoLexEntity.LexicalConcepts.LexicalConcept.toString())) {
                } else {
                    throw new ManagerException(lru.getValue() + " is not a valid Lexical concept");
                }
            } else if (lru.getRelation().equals(OntoLexEntity.LexicalConceptRel.concept.toString())) {
                // currently the property ranges over an external url only 
                validateURL(lru.getValue());
                if (ManagerFactory.getManager(UtilityManager.class).existsNamespace(SimpleValueFactory.getInstance().createIRI(lru.getValue()).getNamespace())) {
                    throw new ManagerException("External links supported only");
                } else {
                }
            } else if (lru.getRelation().equals(OntoLexEntity.LexicalConceptRel.isConceptOf.toString())) {
                throw new ManagerException("still to implement");
            } else if (lru.getRelation().equals(OntoLexEntity.LexicalConceptRel.conceptRel.toString())) {
                throw new ManagerException("still to implement");
//                if (!StringUtil.existsIRI(lru.getValue())) {
//                validateURL(lru.getValue());
//            }
            }
        } else if (lru.getType().equals(EnumUtil.LinguisticRelation.Lexicon.toString())) {
            validateLexicon(lru.getRelation());

        } else {
            throw new ManagerException(lru.getType() + " is not a valid relation type");
        }
        return (lru.getCurrentValue() != null)
                ? (lru.getCurrentValue().isEmpty()
                ? createLinguisticRelation(id, lru.getRelation(), lru.getValue())
                : updateLinguisticRelation(id, lru.getRelation(), lru.getValue(), lru.getCurrentValue()))
                : createLinguisticRelation(id, lru.getRelation(), lru.getValue());
    }

    public String createLinguisticRelation(String id, String relation, String valueToInsert) throws ManagerException, UpdateExecutionException {
        // TODO: it should be verified if the relation can be applied to the id type (for example, denotes requires the id type to be LexicalEntry
        if (relation.contains(OntoLexEntity.LexicalRel.Cognate.toString())) {
            createCognate(valueToInsert);
        }
        return updateLinguisticRelation(SparqlInsertData.CREATE_LINGUISTIC_RELATION, id, relation,
                valueToInsert, "?" + SparqlVariable.TARGET);
    }

    private void createCognate(String valueToInsert) throws ManagerException {
        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI i = factory.createIRI(valueToInsert);
        UtilityManager utilityManager = ManagerFactory.getManager(UtilityManager.class);
        if (utilityManager.existsNamespace(i.getNamespace())) {
            RDFQueryUtil.update(SparqlInsertData.CREATE_COGNATE_TYPE.replace("_ID_", valueToInsert));
        }
    }

    public String updateLinguisticRelation(String id, String relation, String valueToInsert, String currentValue) throws ManagerException, UpdateExecutionException {
        if (relation.contains(OntoLexEntity.LexicalRel.Cognate.toString())) {
            updateCognate(valueToInsert, currentValue);
        }
        if (ManagerFactory.getManager(UtilityManager.class).existsLinguisticRelation(id, relation, currentValue)) {
            return updateLinguisticRelation(SparqlUpdateData.UPDATE_LINGUISTIC_RELATION, id, relation,
                    valueToInsert, currentValue);
        } else {
            throw new ManagerException("IRI " + id + " does not exist or <" + id + ", " + relation + ", " + currentValue + "> does not exist");
        }
    }

    private void updateCognate(String valueToInsert, String currentValue) throws ManagerException {
        //String iri = currentValue.replace("<", "").replace(">", "");
        if (ManagerFactory.getManager(UtilityManager.class).existsNamespace(SimpleValueFactory.getInstance().createIRI(currentValue).getNamespace())) {
            if (!ManagerFactory.getManager(UtilityManager.class).isCognate(currentValue, 1)) {
                // currentValue is involved in this cognate relation only, so its Cognate type is removed
                RDFQueryUtil.update(SparqlDeleteData.DELETE_COGNATE_TYPE.replaceAll("_ID_", currentValue));
            }
            createCognate(valueToInsert);
        } else {
            createCognate(valueToInsert);
        }
    }

    public String updateLinguisticRelation(String query, String id, String relation, String valueToInsert, String valueToDelete) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty() || valueToDelete.isEmpty()) {
            throw new ManagerException("values cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(query.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", valueToDelete)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    public String updateGenericRelation(String id, GenericRelationUpdater gru) throws ManagerException {
        if (gru.getType().equals(EnumUtil.GenericRelation.Reference.toString())) {
            validateGenericReferenceRelation(gru.getRelation());
            if (StringUtil.existsIRI(gru.getValue())) {
                // CONSTRAINT: sameAs holds with external links only
                if (gru.getRelation().contains(EnumUtil.GenericRelationReference.sameAs.toString())) {
                    throw new ManagerException(gru.getRelation() + " links to external links only");
                }
                setTargetValue(gru, false);
            } else {
                validateURL(gru.getValue());
                setTargetValue(gru, false);
            }
        } else if (gru.getType().equals(EnumUtil.GenericRelation.Bibliography.toString())) {
            validateGenericBibliographyRelation(gru.getRelation());
            setTargetValue(gru, true);
        } else if (gru.getType().equals(EnumUtil.GenericRelation.Decomp.toString())) {
            validateGenericDecompRelation(gru.getRelation());
            setTargetValue(gru, true);
        } else if (gru.getType().equals(EnumUtil.GenericRelation.Metadata.toString())) {
            validateMetadataTypes(gru.getRelation());
            setTargetValue(gru, true);
        } else if (gru.getType().equals(EnumUtil.GenericRelation.Extension.toString())) {
            // TEMPORARY SOLUTION: extensions manager will be developed
            if (gru.getRelation().contains("stemType")) {
                setTargetValue(gru, true);
            } else {
                throw new ManagerException("Extension not supported");
            }
//        } else if (gru.getType().equals(EnumUtil.GenericRelation.Confidence.toString())) {
//            if (gru.getRelation().contains(EnumUtil.GenericRelationConfidence.confidence.toString())) {
//            }
        } else {
            throw new ManagerException(gru.getType() + " is not a valid relation type");
        }
        return (gru.getCurrentValue() != null)
                ? (gru.getCurrentValue().isEmpty()
                ? createGenericRelation(id, gru.getRelation(), gru.getValue())
                : updateGenericRelation(id, gru.getRelation(), gru.getValue(), gru.getCurrentValue()))
                : createGenericRelation(id, gru.getRelation(), gru.getValue());
    }

    private void setTargetValue(GenericRelationUpdater gru, boolean literal) {
        gru.setValue((literal ? "\"" : "<") + gru.getValue() + (literal ? "\"" : ">"));
        if (gru.getCurrentValue() != null) {
            if (!gru.getCurrentValue().isEmpty()) {
                gru.setCurrentValue((literal ? "\"" : "<") + gru.getCurrentValue() + (literal ? "\"" : ">"));
            }
        }
    }

    public String createGenericRelation(String id, String relation, String valueToInsert) throws ManagerException, UpdateExecutionException {
        // TODO: it should be verified if the relation can be applied to the id type (for example, sameAs must link same types)
        return updateGenericRelation(SparqlInsertData.CREATE_GENERIC_RELATION, id,
                relation,
                //                relation.equals(EnumUtil.GenericRelationConfidence.confidence.toString()) ? Float.toString(Float.parseFloat(valueToInsert)) : valueToInsert, "?" + SparqlVariable.TARGET);
                valueToInsert, "?" + SparqlVariable.TARGET);
    }

    public String updateGenericRelation(String id, String relation, String valueToInsert, String currentValue) throws ManagerException, UpdateExecutionException {
        if (ManagerFactory.getManager(UtilityManager.class).existsGenericRelation(id, relation, currentValue)) {
            return updateGenericRelation(SparqlUpdateData.UPDATE_GENERIC_RELATION, id,
                    relation,
                    //                    relation.equals(EnumUtil.GenericRelationConfidence.confidence.toString()) ? Float.toString(Float.parseFloat(valueToInsert)) : valueToInsert,
                    //                    relation.equals(EnumUtil.GenericRelationConfidence.confidence.toString()) ? Float.toString(Float.parseFloat(currentValue)) : currentValue);
                    valueToInsert,
                    currentValue);
        } else {
            throw new ManagerException("IRI " + id + " does not exist or <" + id + ", " + relation + ", " + currentValue + "> does not exist");
        }
    }

    public String updateGenericRelation(String query, String id, String relation, String valueToInsert, String valueToDelete) throws ManagerException, UpdateExecutionException {
        if (valueToInsert.isEmpty() || valueToDelete.isEmpty()) {
            throw new ManagerException("values cannot be empty");
        }
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(query.replaceAll("_ID_", id)
                .replaceAll("_RELATION_", relation)
                .replaceAll("_VALUE_TO_INSERT_", valueToInsert)
                .replaceAll("_VALUE_TO_DELETE_", valueToDelete)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

    public String updateComponentPosition(String lexicalEntryID, ComponentPositionUpdater cpu) throws ManagerException {
        if (cpu.getType().equals(EnumUtil.LinguisticRelation.Decomp.toString())) {
            validatePositionRelation(cpu.getRelation());
            UtilityManager utilityManager = ManagerFactory.getManager(UtilityManager.class);
            if (cpu.getComponent() != null) {
                if (!cpu.getComponent().isEmpty()) {
                    if (!utilityManager.existsLinguisticRelation(lexicalEntryID, cpu.getRelation(), cpu.getComponent())) {
                        throw new ManagerException("The statement to update <" + lexicalEntryID + ", " + cpu.getRelation() + ", " + cpu.getComponent() + "> does not exist");
                    }
                }
            } else {
                throw new ManagerException("The component field can not be null or empty");
            }
            if (cpu.getPosition() != 0) {
                if (cpu.getPosition() != (int) cpu.getPosition()) {
                    throw new ManagerException(cpu.getPosition() + " must be an integer number");
                } else {
                    if (cpu.getCurrentPosition() != 0) {
                        if (cpu.getCurrentPosition() != (int) cpu.getCurrentPosition()) {
                            throw new ManagerException(cpu.getCurrentPosition() + " must be an integer number");
                        } else {
                            return updateComponentPosition(lexicalEntryID, cpu.getComponent(), cpu.getPosition(), cpu.getCurrentPosition());
                        }
                    } else {
                        return createComponentPosition(lexicalEntryID, cpu.getComponent(), cpu.getPosition());
                    }
                }
            } else {
                throw new ManagerException("position can not be 0");
            }
        } else {
            throw new ManagerException(cpu.getType() + " is not a valid relation type");
        }
    }

    public String createComponentPosition(String lexicalEntryID, String component, int position) throws ManagerException, UpdateExecutionException {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlInsertData.CREATE_COMPONENT_POSITION.replaceAll("_IDLE_", lexicalEntryID)
                .replaceAll("POSITION", String.valueOf(position))
                .replaceAll("_IDCOMPONENT_", component));
        return lastupdate;
    }

    public String updateComponentPosition(String lexicalEntryID, String component, int position, int currentPosition) throws ManagerException, UpdateExecutionException {
        String lastupdate = timestampFormat.format(new Timestamp(System.currentTimeMillis()));
        RDFQueryUtil.update(SparqlUpdateData.UPDATE_COMPONENT_POSITION.replaceAll("_ID_", lexicalEntryID)
                .replaceAll("POSITION", String.valueOf(position))
                .replaceAll("CURR_POSITION", String.valueOf(currentPosition))
                .replaceAll("_IDCOMPONENT_", component)
                .replaceAll("_LAST_UPDATE_", "\"" + lastupdate + "\""));
        return lastupdate;
    }

}
