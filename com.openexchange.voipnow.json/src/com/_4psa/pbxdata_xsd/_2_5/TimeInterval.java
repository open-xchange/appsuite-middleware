
package com._4psa.pbxdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com._4psa.pbxmessages_xsd._2_5.EditTimeIntervalRequest;


/**
 * Time interval data
 *
 * <p>Java class for TimeInterval complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TimeInterval">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *         &lt;element name="endTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *         &lt;element name="startDay" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/>
 *         &lt;element name="endDay" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/>
 *         &lt;element name="startWkday" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/>
 *         &lt;element name="endWkday" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/>
 *         &lt;element name="month" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeInterval", propOrder = {
    "startTime",
    "endTime",
    "startDay",
    "endDay",
    "startWkday",
    "endWkday",
    "month"
})
@XmlSeeAlso({
    com._4psa.pbxmessagesinfo_xsd._2_5.GetTimeIntervalsResponseType.TimeInterval.class,
    EditTimeIntervalRequest.class
})
public class TimeInterval {

    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar startTime;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar endTime;
    @XmlElement(defaultValue = "1")
    @XmlSchemaType(name = "unsignedInt")
    protected Long startDay;
    @XmlElement(defaultValue = "31")
    @XmlSchemaType(name = "unsignedInt")
    protected Long endDay;
    @XmlElement(defaultValue = "2")
    @XmlSchemaType(name = "unsignedInt")
    protected Long startWkday;
    @XmlElement(defaultValue = "1")
    @XmlSchemaType(name = "unsignedInt")
    protected Long endWkday;
    protected BigInteger month;

    /**
     * Gets the value of the startTime property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setStartTime(XMLGregorianCalendar value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the endTime property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setEndTime(XMLGregorianCalendar value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the startDay property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getStartDay() {
        return startDay;
    }

    /**
     * Sets the value of the startDay property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setStartDay(Long value) {
        this.startDay = value;
    }

    /**
     * Gets the value of the endDay property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getEndDay() {
        return endDay;
    }

    /**
     * Sets the value of the endDay property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setEndDay(Long value) {
        this.endDay = value;
    }

    /**
     * Gets the value of the startWkday property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getStartWkday() {
        return startWkday;
    }

    /**
     * Sets the value of the startWkday property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setStartWkday(Long value) {
        this.startWkday = value;
    }

    /**
     * Gets the value of the endWkday property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getEndWkday() {
        return endWkday;
    }

    /**
     * Sets the value of the endWkday property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setEndWkday(Long value) {
        this.endWkday = value;
    }

    /**
     * Gets the value of the month property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setMonth(BigInteger value) {
        this.month = value;
    }

}
