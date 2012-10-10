
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SerializableTimeZoneTime complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SerializableTimeZoneTime">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Bias" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Time" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DayOrder" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *         &lt;element name="Month" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *         &lt;element name="DayOfWeek" type="{http://schemas.microsoft.com/exchange/services/2006/types}DayOfWeekType"/>
 *         &lt;element name="Year" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SerializableTimeZoneTime", propOrder = {
    "bias",
    "time",
    "dayOrder",
    "month",
    "dayOfWeek",
    "year"
})
public class SerializableTimeZoneTime {

    @XmlElement(name = "Bias")
    protected int bias;
    @XmlElement(name = "Time", required = true)
    protected String time;
    @XmlElement(name = "DayOrder")
    protected short dayOrder;
    @XmlElement(name = "Month")
    protected short month;
    @XmlElement(name = "DayOfWeek", required = true)
    protected DayOfWeekType dayOfWeek;
    @XmlElement(name = "Year")
    protected String year;

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
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the dayOrder property.
     * 
     */
    public short getDayOrder() {
        return dayOrder;
    }

    /**
     * Sets the value of the dayOrder property.
     * 
     */
    public void setDayOrder(short value) {
        this.dayOrder = value;
    }

    /**
     * Gets the value of the month property.
     * 
     */
    public short getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     * 
     */
    public void setMonth(short value) {
        this.month = value;
    }

    /**
     * Gets the value of the dayOfWeek property.
     * 
     * @return
     *     possible object is
     *     {@link DayOfWeekType }
     *     
     */
    public DayOfWeekType getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Sets the value of the dayOfWeek property.
     * 
     * @param value
     *     allowed object is
     *     {@link DayOfWeekType }
     *     
     */
    public void setDayOfWeek(DayOfWeekType value) {
        this.dayOfWeek = value;
    }

    /**
     * Gets the value of the year property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYear() {
        return year;
    }

    /**
     * Sets the value of the year property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYear(String value) {
        this.year = value;
    }

}
