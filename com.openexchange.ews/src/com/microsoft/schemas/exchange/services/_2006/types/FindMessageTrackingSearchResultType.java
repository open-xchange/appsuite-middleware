
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for FindMessageTrackingSearchResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindMessageTrackingSearchResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="Subject" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Sender" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType"/>
 *         &lt;element name="PurportedSender" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *         &lt;element name="Recipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType"/>
 *         &lt;element name="SubmittedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="MessageTrackingReportId" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType"/>
 *         &lt;element name="PreviousHopServer" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType" minOccurs="0"/>
 *         &lt;element name="FirstHopServer" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType" minOccurs="0"/>
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
@XmlType(name = "FindMessageTrackingSearchResultType", propOrder = {

})
public class FindMessageTrackingSearchResultType {

    @XmlElement(name = "Subject", required = true)
    protected String subject;
    @XmlElement(name = "Sender", required = true)
    protected EmailAddressType sender;
    @XmlElement(name = "PurportedSender")
    protected EmailAddressType purportedSender;
    @XmlElement(name = "Recipients", required = true)
    protected ArrayOfRecipientsType recipients;
    @XmlElement(name = "SubmittedTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar submittedTime;
    @XmlElement(name = "MessageTrackingReportId", required = true)
    protected String messageTrackingReportId;
    @XmlElement(name = "PreviousHopServer")
    protected String previousHopServer;
    @XmlElement(name = "FirstHopServer")
    protected String firstHopServer;
    @XmlElement(name = "Properties")
    protected ArrayOfTrackingPropertiesType properties;

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
     * Gets the value of the recipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public ArrayOfRecipientsType getRecipients() {
        return recipients;
    }

    /**
     * Sets the value of the recipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public void setRecipients(ArrayOfRecipientsType value) {
        this.recipients = value;
    }

    /**
     * Gets the value of the submittedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSubmittedTime() {
        return submittedTime;
    }

    /**
     * Sets the value of the submittedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSubmittedTime(XMLGregorianCalendar value) {
        this.submittedTime = value;
    }

    /**
     * Gets the value of the messageTrackingReportId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageTrackingReportId() {
        return messageTrackingReportId;
    }

    /**
     * Sets the value of the messageTrackingReportId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageTrackingReportId(String value) {
        this.messageTrackingReportId = value;
    }

    /**
     * Gets the value of the previousHopServer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousHopServer() {
        return previousHopServer;
    }

    /**
     * Sets the value of the previousHopServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousHopServer(String value) {
        this.previousHopServer = value;
    }

    /**
     * Gets the value of the firstHopServer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstHopServer() {
        return firstHopServer;
    }

    /**
     * Sets the value of the firstHopServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstHopServer(String value) {
        this.firstHopServer = value;
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
