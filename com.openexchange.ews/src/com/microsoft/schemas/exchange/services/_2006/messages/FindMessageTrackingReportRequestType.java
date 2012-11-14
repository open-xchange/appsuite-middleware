
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfTrackingPropertiesType;
import com.microsoft.schemas.exchange.services._2006.types.EmailAddressType;


/**
 * <p>Java class for FindMessageTrackingReportRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindMessageTrackingReportRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;all>
 *         &lt;element name="Scope" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType"/>
 *         &lt;element name="Domain" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType"/>
 *         &lt;element name="Sender" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *         &lt;element name="PurportedSender" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *         &lt;element name="Recipient" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *         &lt;element name="Subject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StartDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="EndDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="MessageId" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType" minOccurs="0"/>
 *         &lt;element name="FederatedDeliveryMailbox" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *         &lt;element name="DiagnosticsLevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ServerHint" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Properties" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTrackingPropertiesType" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindMessageTrackingReportRequestType", propOrder = {
    "scope",
    "domain",
    "sender",
    "purportedSender",
    "recipient",
    "subject",
    "startDateTime",
    "endDateTime",
    "messageId",
    "federatedDeliveryMailbox",
    "diagnosticsLevel",
    "serverHint",
    "properties"
})
public class FindMessageTrackingReportRequestType
    extends BaseRequestType
{

    @XmlElement(name = "Scope", required = true)
    protected String scope;
    @XmlElement(name = "Domain", required = true)
    protected String domain;
    @XmlElement(name = "Sender")
    protected EmailAddressType sender;
    @XmlElement(name = "PurportedSender")
    protected EmailAddressType purportedSender;
    @XmlElement(name = "Recipient")
    protected EmailAddressType recipient;
    @XmlElement(name = "Subject")
    protected String subject;
    @XmlElement(name = "StartDateTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDateTime;
    @XmlElement(name = "EndDateTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDateTime;
    @XmlElement(name = "MessageId")
    protected String messageId;
    @XmlElement(name = "FederatedDeliveryMailbox")
    protected EmailAddressType federatedDeliveryMailbox;
    @XmlElement(name = "DiagnosticsLevel")
    protected String diagnosticsLevel;
    @XmlElement(name = "ServerHint")
    protected String serverHint;
    @XmlElement(name = "Properties")
    protected ArrayOfTrackingPropertiesType properties;

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScope(String value) {
        this.scope = value;
    }

    /**
     * Gets the value of the domain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the value of the domain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomain(String value) {
        this.domain = value;
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
     * Gets the value of the recipient property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getRecipient() {
        return recipient;
    }

    /**
     * Sets the value of the recipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setRecipient(EmailAddressType value) {
        this.recipient = value;
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
     * Gets the value of the startDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDateTime() {
        return startDateTime;
    }

    /**
     * Sets the value of the startDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDateTime(XMLGregorianCalendar value) {
        this.startDateTime = value;
    }

    /**
     * Gets the value of the endDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDateTime() {
        return endDateTime;
    }

    /**
     * Sets the value of the endDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDateTime(XMLGregorianCalendar value) {
        this.endDateTime = value;
    }

    /**
     * Gets the value of the messageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * Gets the value of the federatedDeliveryMailbox property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getFederatedDeliveryMailbox() {
        return federatedDeliveryMailbox;
    }

    /**
     * Sets the value of the federatedDeliveryMailbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setFederatedDeliveryMailbox(EmailAddressType value) {
        this.federatedDeliveryMailbox = value;
    }

    /**
     * Gets the value of the diagnosticsLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDiagnosticsLevel() {
        return diagnosticsLevel;
    }

    /**
     * Sets the value of the diagnosticsLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDiagnosticsLevel(String value) {
        this.diagnosticsLevel = value;
    }

    /**
     * Gets the value of the serverHint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServerHint() {
        return serverHint;
    }

    /**
     * Sets the value of the serverHint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServerHint(String value) {
        this.serverHint = value;
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
