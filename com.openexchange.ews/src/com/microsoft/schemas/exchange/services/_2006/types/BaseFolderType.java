
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BaseFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseFolderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType" minOccurs="0"/>
 *         &lt;element name="ParentFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType" minOccurs="0"/>
 *         &lt;element name="FolderClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TotalCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ChildFolderCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ExtendedProperty" type="{http://schemas.microsoft.com/exchange/services/2006/types}ExtendedPropertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ManagedFolderInformation" type="{http://schemas.microsoft.com/exchange/services/2006/types}ManagedFolderInformationType" minOccurs="0"/>
 *         &lt;element name="EffectiveRights" type="{http://schemas.microsoft.com/exchange/services/2006/types}EffectiveRightsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseFolderType", propOrder = {
    "folderId",
    "parentFolderId",
    "folderClass",
    "displayName",
    "totalCount",
    "childFolderCount",
    "extendedProperty",
    "managedFolderInformation",
    "effectiveRights"
})
@XmlSeeAlso({
    ContactsFolderType.class,
    CalendarFolderType.class,
    FolderType.class
})
public abstract class BaseFolderType {

    @XmlElement(name = "FolderId")
    protected FolderIdType folderId;
    @XmlElement(name = "ParentFolderId")
    protected FolderIdType parentFolderId;
    @XmlElement(name = "FolderClass")
    protected String folderClass;
    @XmlElement(name = "DisplayName")
    protected String displayName;
    @XmlElement(name = "TotalCount")
    protected Integer totalCount;
    @XmlElement(name = "ChildFolderCount")
    protected Integer childFolderCount;
    @XmlElement(name = "ExtendedProperty")
    protected List<ExtendedPropertyType> extendedProperty;
    @XmlElement(name = "ManagedFolderInformation")
    protected ManagedFolderInformationType managedFolderInformation;
    @XmlElement(name = "EffectiveRights")
    protected EffectiveRightsType effectiveRights;

    /**
     * Gets the value of the folderId property.
     * 
     * @return
     *     possible object is
     *     {@link FolderIdType }
     *     
     */
    public FolderIdType getFolderId() {
        return folderId;
    }

    /**
     * Sets the value of the folderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderIdType }
     *     
     */
    public void setFolderId(FolderIdType value) {
        this.folderId = value;
    }

    /**
     * Gets the value of the parentFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link FolderIdType }
     *     
     */
    public FolderIdType getParentFolderId() {
        return parentFolderId;
    }

    /**
     * Sets the value of the parentFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderIdType }
     *     
     */
    public void setParentFolderId(FolderIdType value) {
        this.parentFolderId = value;
    }

    /**
     * Gets the value of the folderClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolderClass() {
        return folderClass;
    }

    /**
     * Sets the value of the folderClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolderClass(String value) {
        this.folderClass = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the totalCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the value of the totalCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalCount(Integer value) {
        this.totalCount = value;
    }

    /**
     * Gets the value of the childFolderCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getChildFolderCount() {
        return childFolderCount;
    }

    /**
     * Sets the value of the childFolderCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setChildFolderCount(Integer value) {
        this.childFolderCount = value;
    }

    /**
     * Gets the value of the extendedProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extendedProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtendedProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExtendedPropertyType }
     * 
     * 
     */
    public List<ExtendedPropertyType> getExtendedProperty() {
        if (extendedProperty == null) {
            extendedProperty = new ArrayList<ExtendedPropertyType>();
        }
        return this.extendedProperty;
    }

    /**
     * Gets the value of the managedFolderInformation property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedFolderInformationType }
     *     
     */
    public ManagedFolderInformationType getManagedFolderInformation() {
        return managedFolderInformation;
    }

    /**
     * Sets the value of the managedFolderInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedFolderInformationType }
     *     
     */
    public void setManagedFolderInformation(ManagedFolderInformationType value) {
        this.managedFolderInformation = value;
    }

    /**
     * Gets the value of the effectiveRights property.
     * 
     * @return
     *     possible object is
     *     {@link EffectiveRightsType }
     *     
     */
    public EffectiveRightsType getEffectiveRights() {
        return effectiveRights;
    }

    /**
     * Sets the value of the effectiveRights property.
     * 
     * @param value
     *     allowed object is
     *     {@link EffectiveRightsType }
     *     
     */
    public void setEffectiveRights(EffectiveRightsType value) {
        this.effectiveRights = value;
    }

}
