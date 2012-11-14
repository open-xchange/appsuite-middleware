
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseItemIdsType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;


/**
 * <p>Java class for SendItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SendItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ItemIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseItemIdsType"/>
 *         &lt;element name="SavedItemFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="SaveItemToFolder" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendItemType", propOrder = {
    "itemIds",
    "savedItemFolderId"
})
public class SendItemType
    extends BaseRequestType
{

    @XmlElement(name = "ItemIds", required = true)
    protected NonEmptyArrayOfBaseItemIdsType itemIds;
    @XmlElement(name = "SavedItemFolderId")
    protected TargetFolderIdType savedItemFolderId;
    @XmlAttribute(name = "SaveItemToFolder", required = true)
    protected boolean saveItemToFolder;

    /**
     * Gets the value of the itemIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseItemIdsType }
     *     
     */
    public NonEmptyArrayOfBaseItemIdsType getItemIds() {
        return itemIds;
    }

    /**
     * Sets the value of the itemIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseItemIdsType }
     *     
     */
    public void setItemIds(NonEmptyArrayOfBaseItemIdsType value) {
        this.itemIds = value;
    }

    /**
     * Gets the value of the savedItemFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getSavedItemFolderId() {
        return savedItemFolderId;
    }

    /**
     * Sets the value of the savedItemFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setSavedItemFolderId(TargetFolderIdType value) {
        this.savedItemFolderId = value;
    }

    /**
     * Gets the value of the saveItemToFolder property.
     * 
     */
    public boolean isSaveItemToFolder() {
        return saveItemToFolder;
    }

    /**
     * Sets the value of the saveItemToFolder property.
     * 
     */
    public void setSaveItemToFolder(boolean value) {
        this.saveItemToFolder = value;
    }

}
