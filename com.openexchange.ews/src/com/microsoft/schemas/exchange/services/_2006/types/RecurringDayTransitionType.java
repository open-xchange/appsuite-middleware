
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RecurringDayTransitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecurringDayTransitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}RecurringTimeTransitionType">
 *       &lt;sequence>
 *         &lt;element name="DayOfWeek" type="{http://schemas.microsoft.com/exchange/services/2006/types}DayOfWeekType"/>
 *         &lt;element name="Occurrence" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecurringDayTransitionType", propOrder = {
    "dayOfWeek",
    "occurrence"
})
public class RecurringDayTransitionType
    extends RecurringTimeTransitionType
{

    @XmlElement(name = "DayOfWeek", required = true)
    protected DayOfWeekType dayOfWeek;
    @XmlElement(name = "Occurrence")
    protected int occurrence;

    /**
     * Gets the value of the dayOfWeek property.
     * 
     * @return
     *     possible object is
     *     {@link DayOfWeekType }
     *     
     */
    public DayOfWeekType getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Sets the value of the dayOfWeek property.
     * 
     * @param value
     *     allowed object is
     *     {@link DayOfWeekType }
     *     
     */
    public void setDayOfWeek(DayOfWeekType value) {
        this.dayOfWeek = value;
    }

    /**
     * Gets the value of the occurrence property.
     * 
     */
    public int getOccurrence() {
        return occurrence;
    }

    /**
     * Sets the value of the occurrence property.
     * 
     */
    public void setOccurrence(int value) {
        this.occurrence = value;
    }

}
