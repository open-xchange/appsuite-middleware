
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CalendarEventDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalendarEventDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Subject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Location" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsMeeting" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="IsRecurring" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="IsException" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="IsReminderSet" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="IsPrivate" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarEventDetails", propOrder = {
    "id",
    "subject",
    "location",
    "isMeeting",
    "isRecurring",
    "isException",
    "isReminderSet",
    "isPrivate"
})
public class CalendarEventDetails {

    @XmlElement(name = "ID")
    protected String id;
    @XmlElement(name = "Subject")
    protected String subject;
    @XmlElement(name = "Location")
    protected String location;
    @XmlElement(name = "IsMeeting")
    protected boolean isMeeting;
    @XmlElement(name = "IsRecurring")
    protected boolean isRecurring;
    @XmlElement(name = "IsException")
    protected boolean isException;
    @XmlElement(name = "IsReminderSet")
    protected boolean isReminderSet;
    @XmlElement(name = "IsPrivate")
    protected boolean isPrivate;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the isMeeting property.
     * 
     */
    public boolean isIsMeeting() {
        return isMeeting;
    }

    /**
     * Sets the value of the isMeeting property.
     * 
     */
    public void setIsMeeting(boolean value) {
        this.isMeeting = value;
    }

    /**
     * Gets the value of the isRecurring property.
     * 
     */
    public boolean isIsRecurring() {
        return isRecurring;
    }

    /**
     * Sets the value of the isRecurring property.
     * 
     */
    public void setIsRecurring(boolean value) {
        this.isRecurring = value;
    }

    /**
     * Gets the value of the isException property.
     * 
     */
    public boolean isIsException() {
        return isException;
    }

    /**
     * Sets the value of the isException property.
     * 
     */
    public void setIsException(boolean value) {
        this.isException = value;
    }

    /**
     * Gets the value of the isReminderSet property.
     * 
     */
    public boolean isIsReminderSet() {
        return isReminderSet;
    }

    /**
     * Sets the value of the isReminderSet property.
     * 
     */
    public void setIsReminderSet(boolean value) {
        this.isReminderSet = value;
    }

    /**
     * Gets the value of the isPrivate property.
     * 
     */
    public boolean isIsPrivate() {
        return isPrivate;
    }

    /**
     * Sets the value of the isPrivate property.
     * 
     */
    public void setIsPrivate(boolean value) {
        this.isPrivate = value;
    }

}
