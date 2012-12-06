
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FolderChangeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FolderChangeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="FolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType"/>
 *           &lt;element name="DistinguishedFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}DistinguishedFolderIdType"/>
 *         &lt;/choice>
 *         &lt;element name="Updates" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfFolderChangeDescriptionsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FolderChangeType", propOrder = {
    "folderId",
    "distinguishedFolderId",
    "updates"
})
public class FolderChangeType {

    @XmlElement(name = "FolderId")
    protected FolderIdType folderId;
    @XmlElement(name = "DistinguishedFolderId")
    protected DistinguishedFolderIdType distinguishedFolderId;
    @XmlElement(name = "Updates", required = true)
    protected NonEmptyArrayOfFolderChangeDescriptionsType updates;

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
     * Gets the value of the distinguishedFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link DistinguishedFolderIdType }
     *     
     */
    public DistinguishedFolderIdType getDistinguishedFolderId() {
        return distinguishedFolderId;
    }

    /**
     * Sets the value of the distinguishedFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistinguishedFolderIdType }
     *     
     */
    public void setDistinguishedFolderId(DistinguishedFolderIdType value) {
        this.distinguishedFolderId = value;
    }

    /**
     * Gets the value of the updates property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfFolderChangeDescriptionsType }
     *     
     */
    public NonEmptyArrayOfFolderChangeDescriptionsType getUpdates() {
        return updates;
    }

    /**
     * Sets the value of the updates property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfFolderChangeDescriptionsType }
     *     
     */
    public void setUpdates(NonEmptyArrayOfFolderChangeDescriptionsType value) {
        this.updates = value;
    }

}
