
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.SyncFolderItemsChangesType;


/**
 * <p>Java class for SyncFolderItemsResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SyncFolderItemsResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="SyncState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IncludesLastItemInRange" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Changes" type="{http://schemas.microsoft.com/exchange/services/2006/types}SyncFolderItemsChangesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncFolderItemsResponseMessageType", propOrder = {
    "syncState",
    "includesLastItemInRange",
    "changes"
})
public class SyncFolderItemsResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "SyncState")
    protected String syncState;
    @XmlElement(name = "IncludesLastItemInRange")
    protected Boolean includesLastItemInRange;
    @XmlElement(name = "Changes")
    protected SyncFolderItemsChangesType changes;

    /**
     * Gets the value of the syncState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSyncState() {
        return syncState;
    }

    /**
     * Sets the value of the syncState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSyncState(String value) {
        this.syncState = value;
    }

    /**
     * Gets the value of the includesLastItemInRange property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludesLastItemInRange() {
        return includesLastItemInRange;
    }

    /**
     * Sets the value of the includesLastItemInRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludesLastItemInRange(Boolean value) {
        this.includesLastItemInRange = value;
    }

    /**
     * Gets the value of the changes property.
     * 
     * @return
     *     possible object is
     *     {@link SyncFolderItemsChangesType }
     *     
     */
    public SyncFolderItemsChangesType getChanges() {
        return changes;
    }

    /**
     * Sets the value of the changes property.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncFolderItemsChangesType }
     *     
     */
    public void setChanges(SyncFolderItemsChangesType value) {
        this.changes = value;
    }

}
