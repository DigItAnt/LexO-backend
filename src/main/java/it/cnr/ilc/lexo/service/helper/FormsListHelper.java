/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.service.helper;

import it.cnr.ilc.lexo.service.data.lexicon.output.FormItem;
import it.cnr.ilc.lexo.sparql.SparqlVariable;
import java.util.regex.Pattern;
import org.eclipse.rdf4j.query.BindingSet;

/**
 *
 * @author andreabellandi
 */
public class FormsListHelper extends TripleStoreDataHelper<FormItem> {

    @Override
    public void fillData(FormItem data, BindingSet bs) {
        data.setAuthor(getStringValue(bs, SparqlVariable.AUTHOR));
        data.setForm(getStringValue(bs, SparqlVariable.FORM));
        data.setFormInstanceName(getStringValue(bs, SparqlVariable.FORM_INSTANCE_NAME));
        data.setLabel(getStringValue(bs, SparqlVariable.WRITTEN_REPRESENTATION));
        data.setNote(getStringValue(bs, SparqlVariable.NOTE));
        data.setPhoneticRep(getStringValue(bs, SparqlVariable.PHONETIC_REPRESENTATION));
        data.setType(getLocalName(bs, SparqlVariable.FORM_TYPE));
        data.setMorphology(getMorphologyWithPoS(bs, getStringValue(bs, SparqlVariable.MORPHOLOGY), 
                getStringValue(bs, SparqlVariable.LEXICAL_ENTRY_POS)));
    }

    @Override
    public Class<FormItem> getDataClass() {
        return FormItem.class;
    }

}
