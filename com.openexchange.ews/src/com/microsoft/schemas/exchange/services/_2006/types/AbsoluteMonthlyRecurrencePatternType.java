
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbsoluteMonthlyRecurrencePatternType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbsoluteMonthlyRecurrencePatternType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}IntervalRecurrencePatternBaseType">
 *       &lt;sequence>
 *         &lt;element name="DayOfMonth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbsoluteMonthlyRecurrencePatternType", propOrder = {
    "dayOfMonth"
})
public class AbsoluteMonthlyRecurrencePatternType
    extends IntervalRecurrencePatternBaseType
{

    @XmlElement(name = "DayOfMonth")
    protected int dayOfMonth;

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

}
