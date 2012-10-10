
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SyncFolderItemsReadFlagType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SyncFolderItemsReadFlagType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType"/>
 *         &lt;element name="IsRead" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncFolderItemsReadFlagType", propOrder = {
    "itemId",
    "isRead"
})
public class SyncFolderItemsReadFlagType {

    @XmlElement(name = "ItemId", required = true)
    protected ItemIdType itemId;
    @XmlElement(name = "IsRead")
    protected boolean isRead;

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
     * Gets the value of the isRead property.
     * 
     */
    public boolean isIsRead() {
        return isRead;
    }

    /**
     * Sets the value of the isRead property.
     * 
     */
    public void setIsRead(boolean value) {
        this.isRead = value;
    }

}
