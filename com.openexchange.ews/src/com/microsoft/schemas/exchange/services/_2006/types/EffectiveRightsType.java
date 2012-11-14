
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EffectiveRightsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EffectiveRightsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CreateAssociated" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="CreateContents" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="CreateHierarchy" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Delete" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Modify" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Read" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="ViewPrivateItems" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EffectiveRightsType", propOrder = {
    "createAssociated",
    "createContents",
    "createHierarchy",
    "delete",
    "modify",
    "read",
    "viewPrivateItems"
})
public class EffectiveRightsType {

    @XmlElement(name = "CreateAssociated")
    protected boolean createAssociated;
    @XmlElement(name = "CreateContents")
    protected boolean createContents;
    @XmlElement(name = "CreateHierarchy")
    protected boolean createHierarchy;
    @XmlElement(name = "Delete")
    protected boolean delete;
    @XmlElement(name = "Modify")
    protected boolean modify;
    @XmlElement(name = "Read")
    protected boolean read;
    @XmlElement(name = "ViewPrivateItems")
    protected Boolean viewPrivateItems;

    /**
     * Gets the value of the createAssociated property.
     * 
     */
    public boolean isCreateAssociated() {
        return createAssociated;
    }

    /**
     * Sets the value of the createAssociated property.
     * 
     */
    public void setCreateAssociated(boolean value) {
        this.createAssociated = value;
    }

    /**
     * Gets the value of the createContents property.
     * 
     */
    public boolean isCreateContents() {
        return createContents;
    }

    /**
     * Sets the value of the createContents property.
     * 
     */
    public void setCreateContents(boolean value) {
        this.createContents = value;
    }

    /**
     * Gets the value of the createHierarchy property.
     * 
     */
    public boolean isCreateHierarchy() {
        return createHierarchy;
    }

    /**
     * Sets the value of the createHierarchy property.
     * 
     */
    public void setCreateHierarchy(boolean value) {
        this.createHierarchy = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     */
    public boolean isDelete() {
        return delete;
    }

    /**
     * Sets the value of the delete property.
     * 
     */
    public void setDelete(boolean value) {
        this.delete = value;
    }

    /**
     * Gets the value of the modify property.
     * 
     */
    public boolean isModify() {
        return modify;
    }

    /**
     * Sets the value of the modify property.
     * 
     */
    public void setModify(boolean value) {
        this.modify = value;
    }

    /**
     * Gets the value of the read property.
     * 
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Sets the value of the read property.
     * 
     */
    public void setRead(boolean value) {
        this.read = value;
    }

    /**
     * Gets the value of the viewPrivateItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isViewPrivateItems() {
        return viewPrivateItems;
    }

    /**
     * Sets the value of the viewPrivateItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setViewPrivateItems(Boolean value) {
        this.viewPrivateItems = value;
    }

}
