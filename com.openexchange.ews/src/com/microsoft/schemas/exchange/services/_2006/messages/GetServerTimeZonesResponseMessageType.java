
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfTimeZoneDefinitionType;


/**
 * <p>Java class for GetServerTimeZonesResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetServerTimeZonesResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="TimeZoneDefinitions" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTimeZoneDefinitionType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetServerTimeZonesResponseMessageType", propOrder = {
    "timeZoneDefinitions"
})
public class GetServerTimeZonesResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "TimeZoneDefinitions", required = true)
    protected ArrayOfTimeZoneDefinitionType timeZoneDefinitions;

    /**
     * Gets the value of the timeZoneDefinitions property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTimeZoneDefinitionType }
     *     
     */
    public ArrayOfTimeZoneDefinitionType getTimeZoneDefinitions() {
        return timeZoneDefinitions;
    }

    /**
     * Sets the value of the timeZoneDefinitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTimeZoneDefinitionType }
     *     
     */
    public void setTimeZoneDefinitions(ArrayOfTimeZoneDefinitionType value) {
        this.timeZoneDefinitions = value;
    }

}
