
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfTimeZoneIdType;


/**
 * <p>Java class for GetServerTimeZonesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetServerTimeZonesType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="Ids" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfTimeZoneIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ReturnFullTimeZoneData" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetServerTimeZonesType", propOrder = {
    "ids"
})
public class GetServerTimeZonesType
    extends BaseRequestType
{

    @XmlElement(name = "Ids")
    protected NonEmptyArrayOfTimeZoneIdType ids;
    @XmlAttribute(name = "ReturnFullTimeZoneData")
    protected Boolean returnFullTimeZoneData;

    /**
     * Gets the value of the ids property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfTimeZoneIdType }
     *     
     */
    public NonEmptyArrayOfTimeZoneIdType getIds() {
        return ids;
    }

    /**
     * Sets the value of the ids property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfTimeZoneIdType }
     *     
     */
    public void setIds(NonEmptyArrayOfTimeZoneIdType value) {
        this.ids = value;
    }

    /**
     * Gets the value of the returnFullTimeZoneData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReturnFullTimeZoneData() {
        return returnFullTimeZoneData;
    }

    /**
     * Sets the value of the returnFullTimeZoneData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReturnFullTimeZoneData(Boolean value) {
        this.returnFullTimeZoneData = value;
    }

}
