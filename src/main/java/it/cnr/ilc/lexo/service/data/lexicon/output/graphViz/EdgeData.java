/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lexo.service.data.lexicon.output.graphViz;

import it.cnr.ilc.lexo.service.data.Data;

/**
 *
 * @author andreabellandi
 */
public class EdgeData implements Data {

    private String id;
    private String source;
    private String target;
    private String relationType;
    private String labelSource;
    private String labelTarget;
    private boolean inferred;
    private int lenght;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLabelSource() {
        return labelSource;
    }

    public void setLabelSource(String labelSource) {
        this.labelSource = labelSource;
    }

    public String getLabelTarget() {
        return labelTarget;
    }

    public void setLabelTarget(String labelTarget) {
        this.labelTarget = labelTarget;
    }

    public boolean isInferred() {
        return inferred;
    }

    public void setInferred(boolean inferred) {
        this.inferred = inferred;
    }

    public EdgeData() {
    }

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    public EdgeData(String id, String source, String target, String labelSource, String labelTarget, boolean inferred, int lenght) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.labelSource = labelSource;
        this.labelTarget = labelTarget;
        this.inferred = inferred;
        this.lenght = lenght;
    }
}
