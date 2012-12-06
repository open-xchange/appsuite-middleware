
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The set of permissions on a folder
 * 
 * <p>Java class for CalendarPermissionSetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalendarPermissionSetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CalendarPermissions" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfCalendarPermissionsType"/>
 *         &lt;element name="UnknownEntries" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfUnknownEntriesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarPermissionSetType", propOrder = {
    "calendarPermissions",
    "unknownEntries"
})
public class CalendarPermissionSetType {

    @XmlElement(name = "CalendarPermissions", required = true)
    protected ArrayOfCalendarPermissionsType calendarPermissions;
    @XmlElement(name = "UnknownEntries")
    protected ArrayOfUnknownEntriesType unknownEntries;

    /**
     * Gets the value of the calendarPermissions property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfCalendarPermissionsType }
     *     
     */
    public ArrayOfCalendarPermissionsType getCalendarPermissions() {
        return calendarPermissions;
    }

    /**
     * Sets the value of the calendarPermissions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfCalendarPermissionsType }
     *     
     */
    public void setCalendarPermissions(ArrayOfCalendarPermissionsType value) {
        this.calendarPermissions = value;
    }

    /**
     * Gets the value of the unknownEntries property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfUnknownEntriesType }
     *     
     */
    public ArrayOfUnknownEntriesType getUnknownEntries() {
        return unknownEntries;
    }

    /**
     * Sets the value of the unknownEntries property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfUnknownEntriesType }
     *     
     */
    public void setUnknownEntries(ArrayOfUnknownEntriesType value) {
        this.unknownEntries = value;
    }

}
