
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WorkingPeriod complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WorkingPeriod">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DayOfWeek" type="{http://schemas.microsoft.com/exchange/services/2006/types}DaysOfWeekType"/>
 *         &lt;element name="StartTimeInMinutes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="EndTimeInMinutes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkingPeriod", propOrder = {
    "dayOfWeek",
    "startTimeInMinutes",
    "endTimeInMinutes"
})
public class WorkingPeriod {

    @XmlList
    @XmlElement(name = "DayOfWeek", required = true)
    protected List<DayOfWeekType> dayOfWeek;
    @XmlElement(name = "StartTimeInMinutes")
    protected int startTimeInMinutes;
    @XmlElement(name = "EndTimeInMinutes")
    protected int endTimeInMinutes;

    /**
     * Gets the value of the dayOfWeek property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dayOfWeek property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDayOfWeek().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DayOfWeekType }
     * 
     * 
     */
    public List<DayOfWeekType> getDayOfWeek() {
        if (dayOfWeek == null) {
            dayOfWeek = new ArrayList<DayOfWeekType>();
        }
        return this.dayOfWeek;
    }

    /**
     * Gets the value of the startTimeInMinutes property.
     * 
     */
    public int getStartTimeInMinutes() {
        return startTimeInMinutes;
    }

    /**
     * Sets the value of the startTimeInMinutes property.
     * 
     */
    public void setStartTimeInMinutes(int value) {
        this.startTimeInMinutes = value;
    }

    /**
     * Gets the value of the endTimeInMinutes property.
     * 
     */
    public int getEndTimeInMinutes() {
        return endTimeInMinutes;
    }

    /**
     * Sets the value of the endTimeInMinutes property.
     * 
     */
    public void setEndTimeInMinutes(int value) {
        this.endTimeInMinutes = value;
    }

}
