/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.service.helper;

import it.cnr.ilc.lexo.service.data.lexicon.output.LinkedEntity;
import it.cnr.ilc.lexo.sparql.SparqlVariable;
import org.eclipse.rdf4j.query.BindingSet;

/**
 *
 * @author andreabellandi
 */
public class DirectRelationHelper extends TripleStoreDataHelper<LinkedEntity> {

    @Override
    public void fillData(LinkedEntity data, BindingSet bs) {
        data.setEntity(getStringValue(bs, SparqlVariable.LEXICAL_ENTITY));
        data.setEntityType(getTypes(bs, getStringValue(bs, SparqlVariable.TYPE)));
        data.setInferred(getStringValue(bs, SparqlVariable.GRAPH).contains("implicit"));
        String label = getLiteralLabel(bs, SparqlVariable.LABEL);
        String lang = getLiteralLanguage(bs, SparqlVariable.LABEL);
        data.setLabel(label.isEmpty() ? "" : label + (lang.isEmpty() ? "" : "@" + lang));
        data.setEntity(getStringValue(bs, SparqlVariable.LEXICAL_ENTITY));
        data.setLink(getStringValue(bs, SparqlVariable.PROPERTY_NAME));
    }

    @Override
    public Class<LinkedEntity> getDataClass() {
        return LinkedEntity.class;
    }

}
