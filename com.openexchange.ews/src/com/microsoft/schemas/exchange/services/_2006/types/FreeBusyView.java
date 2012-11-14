
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FreeBusyView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FreeBusyView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FreeBusyViewType" type="{http://schemas.microsoft.com/exchange/services/2006/types}FreeBusyViewType"/>
 *         &lt;element name="MergedFreeBusy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CalendarEventArray" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfCalendarEvent" minOccurs="0"/>
 *         &lt;element name="WorkingHours" type="{http://schemas.microsoft.com/exchange/services/2006/types}WorkingHours" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FreeBusyView", propOrder = {
    "freeBusyViewType",
    "mergedFreeBusy",
    "calendarEventArray",
    "workingHours"
})
public class FreeBusyView {

    @XmlList
    @XmlElement(name = "FreeBusyViewType", required = true)
    protected List<String> freeBusyViewType;
    @XmlElement(name = "MergedFreeBusy")
    protected String mergedFreeBusy;
    @XmlElement(name = "CalendarEventArray")
    protected ArrayOfCalendarEvent calendarEventArray;
    @XmlElement(name = "WorkingHours")
    protected WorkingHours workingHours;

    /**
     * Gets the value of the freeBusyViewType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the freeBusyViewType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFreeBusyViewType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFreeBusyViewType() {
        if (freeBusyViewType == null) {
            freeBusyViewType = new ArrayList<String>();
        }
        return this.freeBusyViewType;
    }

    /**
     * Gets the value of the mergedFreeBusy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMergedFreeBusy() {
        return mergedFreeBusy;
    }

    /**
     * Sets the value of the mergedFreeBusy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMergedFreeBusy(String value) {
        this.mergedFreeBusy = value;
    }

    /**
     * Gets the value of the calendarEventArray property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfCalendarEvent }
     *     
     */
    public ArrayOfCalendarEvent getCalendarEventArray() {
        return calendarEventArray;
    }

    /**
     * Sets the value of the calendarEventArray property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfCalendarEvent }
     *     
     */
    public void setCalendarEventArray(ArrayOfCalendarEvent value) {
        this.calendarEventArray = value;
    }

    /**
     * Gets the value of the workingHours property.
     * 
     * @return
     *     possible object is
     *     {@link WorkingHours }
     *     
     */
    public WorkingHours getWorkingHours() {
        return workingHours;
    }

    /**
     * Sets the value of the workingHours property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkingHours }
     *     
     */
    public void setWorkingHours(WorkingHours value) {
        this.workingHours = value;
    }

}
