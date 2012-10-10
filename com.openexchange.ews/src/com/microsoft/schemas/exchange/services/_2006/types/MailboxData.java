
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MailboxData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailboxData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Email" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddress"/>
 *         &lt;element name="AttendeeType" type="{http://schemas.microsoft.com/exchange/services/2006/types}MeetingAttendeeType"/>
 *         &lt;element name="ExcludeConflicts" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailboxData", propOrder = {
    "email",
    "attendeeType",
    "excludeConflicts"
})
public class MailboxData {

    @XmlElement(name = "Email", required = true)
    protected EmailAddress email;
    @XmlElement(name = "AttendeeType", required = true)
    protected MeetingAttendeeType attendeeType;
    @XmlElement(name = "ExcludeConflicts")
    protected Boolean excludeConflicts;

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddress }
     *     
     */
    public EmailAddress getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddress }
     *     
     */
    public void setEmail(EmailAddress value) {
        this.email = value;
    }

    /**
     * Gets the value of the attendeeType property.
     * 
     * @return
     *     possible object is
     *     {@link MeetingAttendeeType }
     *     
     */
    public MeetingAttendeeType getAttendeeType() {
        return attendeeType;
    }

    /**
     * Sets the value of the attendeeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeetingAttendeeType }
     *     
     */
    public void setAttendeeType(MeetingAttendeeType value) {
        this.attendeeType = value;
    }

    /**
     * Gets the value of the excludeConflicts property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isExcludeConflicts() {
        return excludeConflicts;
    }

    /**
     * Sets the value of the excludeConflicts property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExcludeConflicts(Boolean value) {
        this.excludeConflicts = value;
    }

}
