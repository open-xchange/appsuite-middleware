
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RelativeYearlyRecurrencePatternType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RelativeYearlyRecurrencePatternType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}RecurrencePatternBaseType">
 *       &lt;sequence>
 *         &lt;element name="DaysOfWeek" type="{http://schemas.microsoft.com/exchange/services/2006/types}DayOfWeekType"/>
 *         &lt;element name="DayOfWeekIndex" type="{http://schemas.microsoft.com/exchange/services/2006/types}DayOfWeekIndexType"/>
 *         &lt;element name="Month" type="{http://schemas.microsoft.com/exchange/services/2006/types}MonthNamesType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelativeYearlyRecurrencePatternType", propOrder = {
    "daysOfWeek",
    "dayOfWeekIndex",
    "month"
})
public class RelativeYearlyRecurrencePatternType
    extends RecurrencePatternBaseType
{

    @XmlElement(name = "DaysOfWeek", required = true)
    protected DayOfWeekType daysOfWeek;
    @XmlElement(name = "DayOfWeekIndex", required = true)
    protected DayOfWeekIndexType dayOfWeekIndex;
    @XmlElement(name = "Month", required = true)
    protected MonthNamesType month;

    /**
     * Gets the value of the daysOfWeek property.
     * 
     * @return
     *     possible object is
     *     {@link DayOfWeekType }
     *     
     */
    public DayOfWeekType getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * Sets the value of the daysOfWeek property.
     * 
     * @param value
     *     allowed object is
     *     {@link DayOfWeekType }
     *     
     */
    public void setDaysOfWeek(DayOfWeekType value) {
        this.daysOfWeek = value;
    }

    /**
     * Gets the value of the dayOfWeekIndex property.
     * 
     * @return
     *     possible object is
     *     {@link DayOfWeekIndexType }
     *     
     */
    public DayOfWeekIndexType getDayOfWeekIndex() {
        return dayOfWeekIndex;
    }

    /**
     * Sets the value of the dayOfWeekIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link DayOfWeekIndexType }
     *     
     */
    public void setDayOfWeekIndex(DayOfWeekIndexType value) {
        this.dayOfWeekIndex = value;
    }

    /**
     * Gets the value of the month property.
     * 
     * @return
     *     possible object is
     *     {@link MonthNamesType }
     *     
     */
    public MonthNamesType getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonthNamesType }
     *     
     */
    public void setMonth(MonthNamesType value) {
        this.month = value;
    }

}
