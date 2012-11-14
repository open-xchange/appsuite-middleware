
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfBaseItemIdsType;
import com.microsoft.schemas.exchange.services._2006.types.ItemResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.SyncFolderItemsScopeType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;


/**
 * <p>Java class for SyncFolderItemsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SyncFolderItemsType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ItemShape" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemResponseShapeType"/>
 *         &lt;element name="SyncFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType"/>
 *         &lt;element name="SyncState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Ignore" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfBaseItemIdsType" minOccurs="0"/>
 *         &lt;element name="MaxChangesReturned" type="{http://schemas.microsoft.com/exchange/services/2006/types}MaxSyncChangesReturnedType"/>
 *         &lt;element name="SyncScope" type="{http://schemas.microsoft.com/exchange/services/2006/types}SyncFolderItemsScopeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncFolderItemsType", propOrder = {
    "itemShape",
    "syncFolderId",
    "syncState",
    "ignore",
    "maxChangesReturned",
    "syncScope"
})
public class SyncFolderItemsType
    extends BaseRequestType
{

    @XmlElement(name = "ItemShape", required = true)
    protected ItemResponseShapeType itemShape;
    @XmlElement(name = "SyncFolderId", required = true)
    protected TargetFolderIdType syncFolderId;
    @XmlElement(name = "SyncState")
    protected String syncState;
    @XmlElement(name = "Ignore")
    protected ArrayOfBaseItemIdsType ignore;
    @XmlElement(name = "MaxChangesReturned")
    protected int maxChangesReturned;
    @XmlElement(name = "SyncScope")
    protected SyncFolderItemsScopeType syncScope;

    /**
     * Gets the value of the itemShape property.
     * 
     * @return
     *     possible object is
     *     {@link ItemResponseShapeType }
     *     
     */
    public ItemResponseShapeType getItemShape() {
        return itemShape;
    }

    /**
     * Sets the value of the itemShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemResponseShapeType }
     *     
     */
    public void setItemShape(ItemResponseShapeType value) {
        this.itemShape = value;
    }

    /**
     * Gets the value of the syncFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getSyncFolderId() {
        return syncFolderId;
    }

    /**
     * Sets the value of the syncFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setSyncFolderId(TargetFolderIdType value) {
        this.syncFolderId = value;
    }

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
     * Gets the value of the ignore property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfBaseItemIdsType }
     *     
     */
    public ArrayOfBaseItemIdsType getIgnore() {
        return ignore;
    }

    /**
     * Sets the value of the ignore property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfBaseItemIdsType }
     *     
     */
    public void setIgnore(ArrayOfBaseItemIdsType value) {
        this.ignore = value;
    }

    /**
     * Gets the value of the maxChangesReturned property.
     * 
     */
    public int getMaxChangesReturned() {
        return maxChangesReturned;
    }

    /**
     * Sets the value of the maxChangesReturned property.
     * 
     */
    public void setMaxChangesReturned(int value) {
        this.maxChangesReturned = value;
    }

    /**
     * Gets the value of the syncScope property.
     * 
     * @return
     *     possible object is
     *     {@link SyncFolderItemsScopeType }
     *     
     */
    public SyncFolderItemsScopeType getSyncScope() {
        return syncScope;
    }

    /**
     * Sets the value of the syncScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncFolderItemsScopeType }
     *     
     */
    public void setSyncScope(SyncFolderItemsScopeType value) {
        this.syncScope = value;
    }

}
