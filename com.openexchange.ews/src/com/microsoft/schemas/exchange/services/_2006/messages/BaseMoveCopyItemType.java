
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseItemIdsType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;


/**
 * <p>Java class for BaseMoveCopyItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseMoveCopyItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ToFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType"/>
 *         &lt;element name="ItemIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseItemIdsType"/>
 *         &lt;element name="ReturnNewItemIds" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseMoveCopyItemType", propOrder = {
    "toFolderId",
    "itemIds",
    "returnNewItemIds"
})
@XmlSeeAlso({
    CopyItemType.class,
    MoveItemType.class
})
public class BaseMoveCopyItemType
    extends BaseRequestType
{

    @XmlElement(name = "ToFolderId", required = true)
    protected TargetFolderIdType toFolderId;
    @XmlElement(name = "ItemIds", required = true)
    protected NonEmptyArrayOfBaseItemIdsType itemIds;
    @XmlElement(name = "ReturnNewItemIds")
    protected Boolean returnNewItemIds;

    /**
     * Gets the value of the toFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getToFolderId() {
        return toFolderId;
    }

    /**
     * Sets the value of the toFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setToFolderId(TargetFolderIdType value) {
        this.toFolderId = value;
    }

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
     * Gets the value of the returnNewItemIds property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReturnNewItemIds() {
        return returnNewItemIds;
    }

    /**
     * Sets the value of the returnNewItemIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReturnNewItemIds(Boolean value) {
        this.returnNewItemIds = value;
    }

}
