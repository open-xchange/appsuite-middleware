
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ItemType">
 *       &lt;sequence>
 *         &lt;element name="Sender" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="ToRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *         &lt;element name="CcRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *         &lt;element name="BccRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *         &lt;element name="IsReadReceiptRequested" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsDeliveryReceiptRequested" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ConversationIndex" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="ConversationTopic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="From" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="InternetMessageId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsRead" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsResponseRequested" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="References" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReplyTo" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *         &lt;element name="ReceivedBy" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="ReceivedRepresenting" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageType", propOrder = {
    "sender",
    "toRecipients",
    "ccRecipients",
    "bccRecipients",
    "isReadReceiptRequested",
    "isDeliveryReceiptRequested",
    "conversationIndex",
    "conversationTopic",
    "from",
    "internetMessageId",
    "isRead",
    "isResponseRequested",
    "references",
    "replyTo",
    "receivedBy",
    "receivedRepresenting"
})
@XmlSeeAlso({
    MeetingMessageType.class,
    ResponseObjectCoreType.class
})
public class MessageType
    extends ItemType
{

    @XmlElement(name = "Sender")
    protected SingleRecipientType sender;
    @XmlElement(name = "ToRecipients")
    protected ArrayOfRecipientsType toRecipients;
    @XmlElement(name = "CcRecipients")
    protected ArrayOfRecipientsType ccRecipients;
    @XmlElement(name = "BccRecipients")
    protected ArrayOfRecipientsType bccRecipients;
    @XmlElement(name = "IsReadReceiptRequested")
    protected Boolean isReadReceiptRequested;
    @XmlElement(name = "IsDeliveryReceiptRequested")
    protected Boolean isDeliveryReceiptRequested;
    @XmlElement(name = "ConversationIndex")
    protected byte[] conversationIndex;
    @XmlElement(name = "ConversationTopic")
    protected String conversationTopic;
    @XmlElement(name = "From")
    protected SingleRecipientType from;
    @XmlElement(name = "InternetMessageId")
    protected String internetMessageId;
    @XmlElement(name = "IsRead")
    protected Boolean isRead;
    @XmlElement(name = "IsResponseRequested")
    protected Boolean isResponseRequested;
    @XmlElement(name = "References")
    protected String references;
    @XmlElement(name = "ReplyTo")
    protected ArrayOfRecipientsType replyTo;
    @XmlElement(name = "ReceivedBy")
    protected SingleRecipientType receivedBy;
    @XmlElement(name = "ReceivedRepresenting")
    protected SingleRecipientType receivedRepresenting;

    /**
     * Gets the value of the sender property.
     * 
     * @return
     *     possible object is
     *     {@link SingleRecipientType }
     *     
     */
    public SingleRecipientType getSender() {
        return sender;
    }

    /**
     * Sets the value of the sender property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleRecipientType }
     *     
     */
    public void setSender(SingleRecipientType value) {
        this.sender = value;
    }

    /**
     * Gets the value of the toRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public ArrayOfRecipientsType getToRecipients() {
        return toRecipients;
    }

    /**
     * Sets the value of the toRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public void setToRecipients(ArrayOfRecipientsType value) {
        this.toRecipients = value;
    }

    /**
     * Gets the value of the ccRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public ArrayOfRecipientsType getCcRecipients() {
        return ccRecipients;
    }

    /**
     * Sets the value of the ccRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public void setCcRecipients(ArrayOfRecipientsType value) {
        this.ccRecipients = value;
    }

    /**
     * Gets the value of the bccRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public ArrayOfRecipientsType getBccRecipients() {
        return bccRecipients;
    }

    /**
     * Sets the value of the bccRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public void setBccRecipients(ArrayOfRecipientsType value) {
        this.bccRecipients = value;
    }

    /**
     * Gets the value of the isReadReceiptRequested property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsReadReceiptRequested() {
        return isReadReceiptRequested;
    }

    /**
     * Sets the value of the isReadReceiptRequested property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsReadReceiptRequested(Boolean value) {
        this.isReadReceiptRequested = value;
    }

    /**
     * Gets the value of the isDeliveryReceiptRequested property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsDeliveryReceiptRequested() {
        return isDeliveryReceiptRequested;
    }

    /**
     * Sets the value of the isDeliveryReceiptRequested property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsDeliveryReceiptRequested(Boolean value) {
        this.isDeliveryReceiptRequested = value;
    }

    /**
     * Gets the value of the conversationIndex property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getConversationIndex() {
        return conversationIndex;
    }

    /**
     * Sets the value of the conversationIndex property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setConversationIndex(byte[] value) {
        this.conversationIndex = ((byte[]) value);
    }

    /**
     * Gets the value of the conversationTopic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConversationTopic() {
        return conversationTopic;
    }

    /**
     * Sets the value of the conversationTopic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConversationTopic(String value) {
        this.conversationTopic = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link SingleRecipientType }
     *     
     */
    public SingleRecipientType getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleRecipientType }
     *     
     */
    public void setFrom(SingleRecipientType value) {
        this.from = value;
    }

    /**
     * Gets the value of the internetMessageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInternetMessageId() {
        return internetMessageId;
    }

    /**
     * Sets the value of the internetMessageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInternetMessageId(String value) {
        this.internetMessageId = value;
    }

    /**
     * Gets the value of the isRead property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsRead() {
        return isRead;
    }

    /**
     * Sets the value of the isRead property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRead(Boolean value) {
        this.isRead = value;
    }

    /**
     * Gets the value of the isResponseRequested property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsResponseRequested() {
        return isResponseRequested;
    }

    /**
     * Sets the value of the isResponseRequested property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsResponseRequested(Boolean value) {
        this.isResponseRequested = value;
    }

    /**
     * Gets the value of the references property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferences() {
        return references;
    }

    /**
     * Sets the value of the references property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferences(String value) {
        this.references = value;
    }

    /**
     * Gets the value of the replyTo property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public ArrayOfRecipientsType getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the value of the replyTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public void setReplyTo(ArrayOfRecipientsType value) {
        this.replyTo = value;
    }

    /**
     * Gets the value of the receivedBy property.
     * 
     * @return
     *     possible object is
     *     {@link SingleRecipientType }
     *     
     */
    public SingleRecipientType getReceivedBy() {
        return receivedBy;
    }

    /**
     * Sets the value of the receivedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleRecipientType }
     *     
     */
    public void setReceivedBy(SingleRecipientType value) {
        this.receivedBy = value;
    }

    /**
     * Gets the value of the receivedRepresenting property.
     * 
     * @return
     *     possible object is
     *     {@link SingleRecipientType }
     *     
     */
    public SingleRecipientType getReceivedRepresenting() {
        return receivedRepresenting;
    }

    /**
     * Sets the value of the receivedRepresenting property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleRecipientType }
     *     
     */
    public void setReceivedRepresenting(SingleRecipientType value) {
        this.receivedRepresenting = value;
    }

}
