
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MovedCopiedEventType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MovedCopiedEventType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseObjectChangedEventType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="OldFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType"/>
 *           &lt;element name="OldItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType"/>
 *         &lt;/choice>
 *         &lt;element name="OldParentFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MovedCopiedEventType", propOrder = {
    "oldFolderId",
    "oldItemId",
    "oldParentFolderId"
})
public class MovedCopiedEventType
    extends BaseObjectChangedEventType
{

    @XmlElement(name = "OldFolderId")
    protected FolderIdType oldFolderId;
    @XmlElement(name = "OldItemId")
    protected ItemIdType oldItemId;
    @XmlElement(name = "OldParentFolderId", required = true)
    protected FolderIdType oldParentFolderId;

    /**
     * Gets the value of the oldFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link FolderIdType }
     *     
     */
    public FolderIdType getOldFolderId() {
        return oldFolderId;
    }

    /**
     * Sets the value of the oldFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderIdType }
     *     
     */
    public void setOldFolderId(FolderIdType value) {
        this.oldFolderId = value;
    }

    /**
     * Gets the value of the oldItemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getOldItemId() {
        return oldItemId;
    }

    /**
     * Sets the value of the oldItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setOldItemId(ItemIdType value) {
        this.oldItemId = value;
    }

    /**
     * Gets the value of the oldParentFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link FolderIdType }
     *     
     */
    public FolderIdType getOldParentFolderId() {
        return oldParentFolderId;
    }

    /**
     * Sets the value of the oldParentFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderIdType }
     *     
     */
    public void setOldParentFolderId(FolderIdType value) {
        this.oldParentFolderId = value;
    }

}
