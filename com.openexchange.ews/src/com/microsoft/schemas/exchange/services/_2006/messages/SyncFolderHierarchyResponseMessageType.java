
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.SyncFolderHierarchyChangesType;


/**
 * <p>Java class for SyncFolderHierarchyResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SyncFolderHierarchyResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="SyncState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IncludesLastFolderInRange" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Changes" type="{http://schemas.microsoft.com/exchange/services/2006/types}SyncFolderHierarchyChangesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncFolderHierarchyResponseMessageType", propOrder = {
    "syncState",
    "includesLastFolderInRange",
    "changes"
})
public class SyncFolderHierarchyResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "SyncState")
    protected String syncState;
    @XmlElement(name = "IncludesLastFolderInRange")
    protected Boolean includesLastFolderInRange;
    @XmlElement(name = "Changes")
    protected SyncFolderHierarchyChangesType changes;

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
     * Gets the value of the includesLastFolderInRange property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludesLastFolderInRange() {
        return includesLastFolderInRange;
    }

    /**
     * Sets the value of the includesLastFolderInRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludesLastFolderInRange(Boolean value) {
        this.includesLastFolderInRange = value;
    }

    /**
     * Gets the value of the changes property.
     * 
     * @return
     *     possible object is
     *     {@link SyncFolderHierarchyChangesType }
     *     
     */
    public SyncFolderHierarchyChangesType getChanges() {
        return changes;
    }

    /**
     * Sets the value of the changes property.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncFolderHierarchyChangesType }
     *     
     */
    public void setChanges(SyncFolderHierarchyChangesType value) {
        this.changes = value;
    }

}
