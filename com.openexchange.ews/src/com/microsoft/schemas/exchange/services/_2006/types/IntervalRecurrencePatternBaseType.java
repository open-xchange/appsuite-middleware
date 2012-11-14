
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IntervalRecurrencePatternBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IntervalRecurrencePatternBaseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}RecurrencePatternBaseType">
 *       &lt;sequence>
 *         &lt;element name="Interval" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IntervalRecurrencePatternBaseType", propOrder = {
    "interval"
})
@XmlSeeAlso({
    WeeklyRecurrencePatternType.class,
    DailyRecurrencePatternType.class,
    RelativeMonthlyRecurrencePatternType.class,
    RegeneratingPatternBaseType.class,
    AbsoluteMonthlyRecurrencePatternType.class
})
public abstract class IntervalRecurrencePatternBaseType
    extends RecurrencePatternBaseType
{

    @XmlElement(name = "Interval")
    protected int interval;

    /**
     * Gets the value of the interval property.
     * 
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Sets the value of the interval property.
     * 
     */
    public void setInterval(int value) {
        this.interval = value;
    }

}
