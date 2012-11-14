
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UploadItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UploadItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ParentFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType"/>
 *         &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *         &lt;element name="Data" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *       &lt;attribute name="CreateAction" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}CreateActionType" />
 *       &lt;attribute name="IsAssociated" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UploadItemType", propOrder = {
    "parentFolderId",
    "itemId",
    "data"
})
public class UploadItemType {

    @XmlElement(name = "ParentFolderId", required = true)
    protected FolderIdType parentFolderId;
    @XmlElement(name = "ItemId")
    protected ItemIdType itemId;
    @XmlElement(name = "Data", required = true)
    protected byte[] data;
    @XmlAttribute(name = "CreateAction", required = true)
    protected CreateActionType createAction;
    @XmlAttribute(name = "IsAssociated")
    protected Boolean isAssociated;

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
     * Gets the value of the itemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getItemId() {
        return itemId;
    }

    /**
     * Sets the value of the itemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setItemId(ItemIdType value) {
        this.itemId = value;
    }

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setData(byte[] value) {
        this.data = ((byte[]) value);
    }

    /**
     * Gets the value of the createAction property.
     * 
     * @return
     *     possible object is
     *     {@link CreateActionType }
     *     
     */
    public CreateActionType getCreateAction() {
        return createAction;
    }

    /**
     * Sets the value of the createAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateActionType }
     *     
     */
    public void setCreateAction(CreateActionType value) {
        this.createAction = value;
    }

    /**
     * Gets the value of the isAssociated property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAssociated() {
        return isAssociated;
    }

    /**
     * Sets the value of the isAssociated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAssociated(Boolean value) {
        this.isAssociated = value;
    }

}
