/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.service.data.lexicon.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.cnr.ilc.lexo.service.data.Data;
import it.cnr.ilc.lexo.service.data.lexicon.output.skos.SKOSClass;
import it.cnr.ilc.lexo.service.data.output.Label;
import java.util.List;

/**
 *
 * @author andreabellandi
 */
@ApiModel(description = "Output model representing a lexical concept")
public class LexicalConcept extends SKOSClass implements Data {

    @ApiModelProperty(value = "IRI")
    private String lexicalConcept;
    @ApiModelProperty(value = "short IRI")
    private String lexicalConceptInstanceName;
    @ApiModelProperty(value = "the list of relations the lexical concept is involved in")
    private List<GroupedLinkedEntity> entities;
    private List<Label> labels;

    public String getLexicalConcept() {
        return lexicalConcept;
    }

    public void setLexicalConcept(String lexicalConcept) {
        this.lexicalConcept = lexicalConcept;
    }

    public String getLexicalConceptInstanceName() {
        return lexicalConceptInstanceName;
    }

    public void setLexicalConceptInstanceName(String lexicalConceptInstanceName) {
        this.lexicalConceptInstanceName = lexicalConceptInstanceName;
    }

    public List<GroupedLinkedEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<GroupedLinkedEntity> entities) {
        this.entities = entities;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

}
