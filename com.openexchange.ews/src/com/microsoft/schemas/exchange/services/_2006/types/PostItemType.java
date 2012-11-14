
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for PostItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PostItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ItemType">
 *       &lt;sequence>
 *         &lt;element name="ConversationIndex" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="ConversationTopic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="From" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="InternetMessageId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsRead" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="PostedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="References" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sender" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PostItemType", propOrder = {
    "conversationIndex",
    "conversationTopic",
    "from",
    "internetMessageId",
    "isRead",
    "postedTime",
    "references",
    "sender"
})
public class PostItemType
    extends ItemType
{

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
    @XmlElement(name = "PostedTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar postedTime;
    @XmlElement(name = "References")
    protected String references;
    @XmlElement(name = "Sender")
    protected SingleRecipientType sender;

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
     * Gets the value of the postedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPostedTime() {
        return postedTime;
    }

    /**
     * Sets the value of the postedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setPostedTime(XMLGregorianCalendar value) {
        this.postedTime = value;
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

}
