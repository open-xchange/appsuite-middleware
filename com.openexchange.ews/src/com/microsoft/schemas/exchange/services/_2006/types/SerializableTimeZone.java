
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SerializableTimeZone complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SerializableTimeZone">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Bias" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="StandardTime" type="{http://schemas.microsoft.com/exchange/services/2006/types}SerializableTimeZoneTime"/>
 *         &lt;element name="DaylightTime" type="{http://schemas.microsoft.com/exchange/services/2006/types}SerializableTimeZoneTime"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SerializableTimeZone", propOrder = {
    "bias",
    "standardTime",
    "daylightTime"
})
public class SerializableTimeZone {

    @XmlElement(name = "Bias")
    protected int bias;
    @XmlElement(name = "StandardTime", required = true)
    protected SerializableTimeZoneTime standardTime;
    @XmlElement(name = "DaylightTime", required = true)
    protected SerializableTimeZoneTime daylightTime;

    /**
     * Gets the value of the bias property.
     * 
     */
    public int getBias() {
        return bias;
    }

    /**
     * Sets the value of the bias property.
     * 
     */
    public void setBias(int value) {
        this.bias = value;
    }

    /**
     * Gets the value of the standardTime property.
     * 
     * @return
     *     possible object is
     *     {@link SerializableTimeZoneTime }
     *     
     */
    public SerializableTimeZoneTime getStandardTime() {
        return standardTime;
    }

    /**
     * Sets the value of the standardTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link SerializableTimeZoneTime }
     *     
     */
    public void setStandardTime(SerializableTimeZoneTime value) {
        this.standardTime = value;
    }

    /**
     * Gets the value of the daylightTime property.
     * 
     * @return
     *     possible object is
     *     {@link SerializableTimeZoneTime }
     *     
     */
    public SerializableTimeZoneTime getDaylightTime() {
        return daylightTime;
    }

    /**
     * Sets the value of the daylightTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link SerializableTimeZoneTime }
     *     
     */
    public void setDaylightTime(SerializableTimeZoneTime value) {
        this.daylightTime = value;
    }

}
