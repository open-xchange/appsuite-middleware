
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ItemResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseItemIdsType;


/**
 * <p>Java class for GetItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ItemShape" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemResponseShapeType"/>
 *         &lt;element name="ItemIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseItemIdsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetItemType", propOrder = {
    "itemShape",
    "itemIds"
})
public class GetItemType
    extends BaseRequestType
{

    @XmlElement(name = "ItemShape", required = true)
    protected ItemResponseShapeType itemShape;
    @XmlElement(name = "ItemIds", required = true)
    protected NonEmptyArrayOfBaseItemIdsType itemIds;

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

}
