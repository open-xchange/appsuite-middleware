
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;


/**
 * <p>Java class for TimeZoneType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TimeZoneType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="BaseOffset" type="{http://www.w3.org/2001/XMLSchema}duration"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="Standard" type="{http://schemas.microsoft.com/exchange/services/2006/types}TimeChangeType"/>
 *           &lt;element name="Daylight" type="{http://schemas.microsoft.com/exchange/services/2006/types}TimeChangeType"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="TimeZoneName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeZoneType", propOrder = {
    "baseOffset",
    "standard",
    "daylight"
})
public class TimeZoneType {

    @XmlElement(name = "BaseOffset")
    protected Duration baseOffset;
    @XmlElement(name = "Standard")
    protected TimeChangeType standard;
    @XmlElement(name = "Daylight")
    protected TimeChangeType daylight;
    @XmlAttribute(name = "TimeZoneName")
    protected String timeZoneName;

    /**
     * Gets the value of the baseOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getBaseOffset() {
        return baseOffset;
    }

    /**
     * Sets the value of the baseOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setBaseOffset(Duration value) {
        this.baseOffset = value;
    }

    /**
     * Gets the value of the standard property.
     * 
     * @return
     *     possible object is
     *     {@link TimeChangeType }
     *     
     */
    public TimeChangeType getStandard() {
        return standard;
    }

    /**
     * Sets the value of the standard property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeChangeType }
     *     
     */
    public void setStandard(TimeChangeType value) {
        this.standard = value;
    }

    /**
     * Gets the value of the daylight property.
     * 
     * @return
     *     possible object is
     *     {@link TimeChangeType }
     *     
     */
    public TimeChangeType getDaylight() {
        return daylight;
    }

    /**
     * Sets the value of the daylight property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeChangeType }
     *     
     */
    public void setDaylight(TimeChangeType value) {
        this.daylight = value;
    }

    /**
     * Gets the value of the timeZoneName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeZoneName() {
        return timeZoneName;
    }

    /**
     * Sets the value of the timeZoneName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeZoneName(String value) {
        this.timeZoneName = value;
    }

}
