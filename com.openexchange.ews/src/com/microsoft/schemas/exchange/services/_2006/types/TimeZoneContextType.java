
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for TimeZoneContextType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TimeZoneContextType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TimeZoneDefinition" type="{http://schemas.microsoft.com/exchange/services/2006/types}TimeZoneDefinitionType"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute namespace='http://schemas.xmlsoap.org/soap/envelope/'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeZoneContextType", propOrder = {
    "timeZoneDefinition"
})
public class TimeZoneContextType {

    @XmlElement(name = "TimeZoneDefinition", required = true)
    protected TimeZoneDefinitionType timeZoneDefinition;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the timeZoneDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link TimeZoneDefinitionType }
     *     
     */
    public TimeZoneDefinitionType getTimeZoneDefinition() {
        return timeZoneDefinition;
    }

    /**
     * Sets the value of the timeZoneDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeZoneDefinitionType }
     *     
     */
    public void setTimeZoneDefinition(TimeZoneDefinitionType value) {
        this.timeZoneDefinition = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
