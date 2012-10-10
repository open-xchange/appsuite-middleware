
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfCalendarEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfCalendarEvent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CalendarEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarEvent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCalendarEvent", propOrder = {
    "calendarEvent"
})
public class ArrayOfCalendarEvent {

    @XmlElement(name = "CalendarEvent")
    protected List<CalendarEvent> calendarEvent;

    /**
     * Gets the value of the calendarEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the calendarEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCalendarEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CalendarEvent }
     * 
     * 
     */
    public List<CalendarEvent> getCalendarEvent() {
        if (calendarEvent == null) {
            calendarEvent = new ArrayList<CalendarEvent>();
        }
        return this.calendarEvent;
    }

}
