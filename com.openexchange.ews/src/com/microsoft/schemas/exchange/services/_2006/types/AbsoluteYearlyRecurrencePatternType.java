
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbsoluteYearlyRecurrencePatternType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbsoluteYearlyRecurrencePatternType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}RecurrencePatternBaseType">
 *       &lt;sequence>
 *         &lt;element name="DayOfMonth" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
@XmlType(name = "AbsoluteYearlyRecurrencePatternType", propOrder = {
    "dayOfMonth",
    "month"
})
public class AbsoluteYearlyRecurrencePatternType
    extends RecurrencePatternBaseType
{

    @XmlElement(name = "DayOfMonth")
    protected int dayOfMonth;
    @XmlElement(name = "Month", required = true)
    protected MonthNamesType month;

    /**
     * Gets the value of the dayOfMonth property.
     * 
     */
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * Sets the value of the dayOfMonth property.
     * 
     */
    public void setDayOfMonth(int value) {
        this.dayOfMonth = value;
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
