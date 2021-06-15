/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.service.data.lexicon.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.cnr.ilc.lexo.service.data.Data;

/**
 *
 * @author andreabellandi
 */
@ApiModel(description = "Input model representing a language updater")
public class LanguageUpdater implements Data {
    
    @ApiModelProperty(value = "language relation/attribute", example = "description", 
            allowableValues = "language, lexvo, description")
    private String relation;
    @ApiModelProperty(value = "realtion/attribute value", example = "test", allowEmptyValue = false)
    private String value;

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
