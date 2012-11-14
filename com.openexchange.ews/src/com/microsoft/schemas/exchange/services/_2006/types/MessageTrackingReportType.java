
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for MessageTrackingReportType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MessageTrackingReportType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="Sender" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *         &lt;element name="PurportedSender" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *         &lt;element name="Subject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SubmitTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="OriginalRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *         &lt;element name="RecipientTrackingEvents" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientTrackingEventType"/>
 *         &lt;element name="Properties" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTrackingPropertiesType" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageTrackingReportType", propOrder = {

})
public class MessageTrackingReportType {

    @XmlElement(name = "Sender")
    protected EmailAddressType sender;
    @XmlElement(name = "PurportedSender")
    protected EmailAddressType purportedSender;
    @XmlElement(name = "Subject")
    protected String subject;
    @XmlElement(name = "SubmitTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar submitTime;
    @XmlElement(name = "OriginalRecipients")
    protected ArrayOfEmailAddressesType originalRecipients;
    @XmlElement(name = "RecipientTrackingEvents", required = true)
    protected ArrayOfRecipientTrackingEventType recipientTrackingEvents;
    @XmlElement(name = "Properties")
    protected ArrayOfTrackingPropertiesType properties;

    /**
     * Gets the value of the sender property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getSender() {
        return sender;
    }

    /**
     * Sets the value of the sender property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setSender(EmailAddressType value) {
        this.sender = value;
    }

    /**
     * Gets the value of the purportedSender property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getPurportedSender() {
        return purportedSender;
    }

    /**
     * Sets the value of the purportedSender property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setPurportedSender(EmailAddressType value) {
        this.purportedSender = value;
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
     * Gets the value of the submitTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSubmitTime() {
        return submitTime;
    }

    /**
     * Sets the value of the submitTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSubmitTime(XMLGregorianCalendar value) {
        this.submitTime = value;
    }

    /**
     * Gets the value of the originalRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getOriginalRecipients() {
        return originalRecipients;
    }

    /**
     * Sets the value of the originalRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setOriginalRecipients(ArrayOfEmailAddressesType value) {
        this.originalRecipients = value;
    }

    /**
     * Gets the value of the recipientTrackingEvents property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRecipientTrackingEventType }
     *     
     */
    public ArrayOfRecipientTrackingEventType getRecipientTrackingEvents() {
        return recipientTrackingEvents;
    }

    /**
     * Sets the value of the recipientTrackingEvents property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRecipientTrackingEventType }
     *     
     */
    public void setRecipientTrackingEvents(ArrayOfRecipientTrackingEventType value) {
        this.recipientTrackingEvents = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTrackingPropertiesType }
     *     
     */
    public ArrayOfTrackingPropertiesType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTrackingPropertiesType }
     *     
     */
    public void setProperties(ArrayOfTrackingPropertiesType value) {
        this.properties = value;
    }

}
