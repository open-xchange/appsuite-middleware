
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CalendarPermissionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalendarPermissionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BasePermissionType">
 *       &lt;sequence>
 *         &lt;element name="ReadItems" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarPermissionReadAccessType" minOccurs="0"/>
 *         &lt;element name="CalendarPermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarPermissionLevelType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarPermissionType", propOrder = {
    "readItems",
    "calendarPermissionLevel"
})
public class CalendarPermissionType
    extends BasePermissionType
{

    @XmlElement(name = "ReadItems")
    protected CalendarPermissionReadAccessType readItems;
    @XmlElement(name = "CalendarPermissionLevel", required = true)
    protected CalendarPermissionLevelType calendarPermissionLevel;

    /**
     * Gets the value of the readItems property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarPermissionReadAccessType }
     *     
     */
    public CalendarPermissionReadAccessType getReadItems() {
        return readItems;
    }

    /**
     * Sets the value of the readItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarPermissionReadAccessType }
     *     
     */
    public void setReadItems(CalendarPermissionReadAccessType value) {
        this.readItems = value;
    }

    /**
     * Gets the value of the calendarPermissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarPermissionLevelType }
     *     
     */
    public CalendarPermissionLevelType getCalendarPermissionLevel() {
        return calendarPermissionLevel;
    }

    /**
     * Sets the value of the calendarPermissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarPermissionLevelType }
     *     
     */
    public void setCalendarPermissionLevel(CalendarPermissionLevelType value) {
        this.calendarPermissionLevel = value;
    }

}
