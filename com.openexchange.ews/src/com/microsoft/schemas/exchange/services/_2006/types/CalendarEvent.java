
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CalendarEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalendarEvent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StartTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="EndTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="BusyType" type="{http://schemas.microsoft.com/exchange/services/2006/types}LegacyFreeBusyType"/>
 *         &lt;element name="CalendarEventDetails" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarEventDetails" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarEvent", propOrder = {
    "startTime",
    "endTime",
    "busyType",
    "calendarEventDetails"
})
public class CalendarEvent {

    @XmlElement(name = "StartTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startTime;
    @XmlElement(name = "EndTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endTime;
    @XmlElement(name = "BusyType", required = true)
    protected LegacyFreeBusyType busyType;
    @XmlElement(name = "CalendarEventDetails")
    protected CalendarEventDetails calendarEventDetails;

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
     * Gets the value of the busyType property.
     * 
     * @return
     *     possible object is
     *     {@link LegacyFreeBusyType }
     *     
     */
    public LegacyFreeBusyType getBusyType() {
        return busyType;
    }

    /**
     * Sets the value of the busyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LegacyFreeBusyType }
     *     
     */
    public void setBusyType(LegacyFreeBusyType value) {
        this.busyType = value;
    }

    /**
     * Gets the value of the calendarEventDetails property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarEventDetails }
     *     
     */
    public CalendarEventDetails getCalendarEventDetails() {
        return calendarEventDetails;
    }

    /**
     * Sets the value of the calendarEventDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarEventDetails }
     *     
     */
    public void setCalendarEventDetails(CalendarEventDetails value) {
        this.calendarEventDetails = value;
    }

}
