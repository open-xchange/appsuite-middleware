
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WeeklyRecurrencePatternType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WeeklyRecurrencePatternType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}IntervalRecurrencePatternBaseType">
 *       &lt;sequence>
 *         &lt;element name="DaysOfWeek" type="{http://schemas.microsoft.com/exchange/services/2006/types}DaysOfWeekType"/>
 *         &lt;element name="FirstDayOfWeek" type="{http://schemas.microsoft.com/exchange/services/2006/types}DayOfWeekType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeeklyRecurrencePatternType", propOrder = {
    "daysOfWeek",
    "firstDayOfWeek"
})
public class WeeklyRecurrencePatternType
    extends IntervalRecurrencePatternBaseType
{

    @XmlList
    @XmlElement(name = "DaysOfWeek", required = true)
    protected List<DayOfWeekType> daysOfWeek;
    @XmlElement(name = "FirstDayOfWeek")
    protected DayOfWeekType firstDayOfWeek;

    /**
     * Gets the value of the daysOfWeek property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the daysOfWeek property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDaysOfWeek().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DayOfWeekType }
     * 
     * 
     */
    public List<DayOfWeekType> getDaysOfWeek() {
        if (daysOfWeek == null) {
            daysOfWeek = new ArrayList<DayOfWeekType>();
        }
        return this.daysOfWeek;
    }

    /**
     * Gets the value of the firstDayOfWeek property.
     * 
     * @return
     *     possible object is
     *     {@link DayOfWeekType }
     *     
     */
    public DayOfWeekType getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * Sets the value of the firstDayOfWeek property.
     * 
     * @param value
     *     allowed object is
     *     {@link DayOfWeekType }
     *     
     */
    public void setFirstDayOfWeek(DayOfWeekType value) {
        this.firstDayOfWeek = value;
    }

}
