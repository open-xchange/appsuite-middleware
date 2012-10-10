
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ItemIdType;


/**
 * <p>Java class for PlayOnPhoneType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlayOnPhoneType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType"/>
 *         &lt;element name="DialString" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlayOnPhoneType", propOrder = {
    "itemId",
    "dialString"
})
public class PlayOnPhoneType
    extends BaseRequestType
{

    @XmlElement(name = "ItemId", required = true)
    protected ItemIdType itemId;
    @XmlElement(name = "DialString", required = true)
    protected String dialString;

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
     * Gets the value of the dialString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDialString() {
        return dialString;
    }

    /**
     * Sets the value of the dialString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDialString(String value) {
        this.dialString = value;
    }

}
