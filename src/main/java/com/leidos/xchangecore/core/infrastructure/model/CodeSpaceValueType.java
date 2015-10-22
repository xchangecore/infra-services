package com.leidos.xchangecore.core.infrastructure.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * The CodeSpaceValueType data model.
 * 
 * @ssdd
 */
@Entity
// @Embeddable
public class CodeSpaceValueType {

    @Id
    @Column(name = "CODE_SPACE_ID")
    @GeneratedValue
    private Integer id;

    private String codeSpace;
    private String label;
    private String value;

    public boolean equals(Object obj) {

        CodeSpaceValueType codeObject = (CodeSpaceValueType) obj;
        String hash = codeSpace + value;
        String codeObjectHash = codeObject.getCodeSpace() + codeObject.getValue();
        return (hash.equals(codeObjectHash));
    }

    public int hashCode() {

        String hash = codeSpace + value;
        return hash.hashCode();
    }

    /**
     * Gets the id.
     * 
     * @return the id
     * @ssdd
     */
    public Integer getId() {

        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id the new id
     * @ssdd
     */
    public void setId(Integer id) {

        this.id = id;
    }

    /**
     * Gets the code space.
     * 
     * @return the code space
     * @ssdd
     */
    public String getCodeSpace() {

        return codeSpace;
    }

    /**
     * Sets the code space.
     * 
     * @param codeSpace the new code space
     * @ssdd
     */
    public void setCodeSpace(String codeSpace) {

        this.codeSpace = codeSpace;
    }

    /**
     * Gets the label.
     * 
     * @return the label
     * @ssdd
     */
    public String getLabel() {

        return label;
    }

    /**
     * Sets the label.
     * 
     * @param label the new label
     * @ssdd
     */
    public void setLabel(String label) {

        this.label = label;
    }

    /**
     * Gets the value.
     * 
     * @return the value
     * @ssdd
     */
    public String getValue() {

        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value the new value
     * @ssdd
     */
    public void setValue(String value) {

        this.value = value;
    }
}