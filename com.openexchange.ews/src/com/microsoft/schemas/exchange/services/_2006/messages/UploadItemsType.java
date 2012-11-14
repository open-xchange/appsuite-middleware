
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfUploadItemsType;


/**
 * <p>Java class for UploadItemsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UploadItemsType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="Items" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfUploadItemsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UploadItemsType", propOrder = {
    "items"
})
public class UploadItemsType
    extends BaseRequestType
{

    @XmlElement(name = "Items", required = true)
    protected NonEmptyArrayOfUploadItemsType items;

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfUploadItemsType }
     *     
     */
    public NonEmptyArrayOfUploadItemsType getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfUploadItemsType }
     *     
     */
    public void setItems(NonEmptyArrayOfUploadItemsType value) {
        this.items = value;
    }

}
