
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * A permission on a folder
 * 
 * <p>Java class for BasePermissionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BasePermissionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserId" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserIdType"/>
 *         &lt;element name="CanCreateItems" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CanCreateSubFolders" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsFolderOwner" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsFolderVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsFolderContact" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="EditItems" type="{http://schemas.microsoft.com/exchange/services/2006/types}PermissionActionType" minOccurs="0"/>
 *         &lt;element name="DeleteItems" type="{http://schemas.microsoft.com/exchange/services/2006/types}PermissionActionType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BasePermissionType", propOrder = {
    "userId",
    "canCreateItems",
    "canCreateSubFolders",
    "isFolderOwner",
    "isFolderVisible",
    "isFolderContact",
    "editItems",
    "deleteItems"
})
@XmlSeeAlso({
    CalendarPermissionType.class,
    PermissionType.class
})
public abstract class BasePermissionType {

    @XmlElement(name = "UserId", required = true)
    protected UserIdType userId;
    @XmlElement(name = "CanCreateItems")
    protected Boolean canCreateItems;
    @XmlElement(name = "CanCreateSubFolders")
    protected Boolean canCreateSubFolders;
    @XmlElement(name = "IsFolderOwner")
    protected Boolean isFolderOwner;
    @XmlElement(name = "IsFolderVisible")
    protected Boolean isFolderVisible;
    @XmlElement(name = "IsFolderContact")
    protected Boolean isFolderContact;
    @XmlElement(name = "EditItems")
    protected PermissionActionType editItems;
    @XmlElement(name = "DeleteItems")
    protected PermissionActionType deleteItems;

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link UserIdType }
     *     
     */
    public UserIdType getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserIdType }
     *     
     */
    public void setUserId(UserIdType value) {
        this.userId = value;
    }

    /**
     * Gets the value of the canCreateItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCreateItems() {
        return canCreateItems;
    }

    /**
     * Sets the value of the canCreateItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCreateItems(Boolean value) {
        this.canCreateItems = value;
    }

    /**
     * Gets the value of the canCreateSubFolders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanCreateSubFolders() {
        return canCreateSubFolders;
    }

    /**
     * Sets the value of the canCreateSubFolders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanCreateSubFolders(Boolean value) {
        this.canCreateSubFolders = value;
    }

    /**
     * Gets the value of the isFolderOwner property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsFolderOwner() {
        return isFolderOwner;
    }

    /**
     * Sets the value of the isFolderOwner property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsFolderOwner(Boolean value) {
        this.isFolderOwner = value;
    }

    /**
     * Gets the value of the isFolderVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsFolderVisible() {
        return isFolderVisible;
    }

    /**
     * Sets the value of the isFolderVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsFolderVisible(Boolean value) {
        this.isFolderVisible = value;
    }

    /**
     * Gets the value of the isFolderContact property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsFolderContact() {
        return isFolderContact;
    }

    /**
     * Sets the value of the isFolderContact property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsFolderContact(Boolean value) {
        this.isFolderContact = value;
    }

    /**
     * Gets the value of the editItems property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionActionType }
     *     
     */
    public PermissionActionType getEditItems() {
        return editItems;
    }

    /**
     * Sets the value of the editItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionActionType }
     *     
     */
    public void setEditItems(PermissionActionType value) {
        this.editItems = value;
    }

    /**
     * Gets the value of the deleteItems property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionActionType }
     *     
     */
    public PermissionActionType getDeleteItems() {
        return deleteItems;
    }

    /**
     * Sets the value of the deleteItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionActionType }
     *     
     */
    public void setDeleteItems(PermissionActionType value) {
        this.deleteItems = value;
    }

}
