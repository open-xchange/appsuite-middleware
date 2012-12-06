
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfConversationsType;


/**
 * <p>Java class for FindConversationResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindConversationResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="Conversations" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfConversationsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindConversationResponseMessageType", propOrder = {
    "conversations"
})
public class FindConversationResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "Conversations", required = true)
    protected ArrayOfConversationsType conversations;

    /**
     * Gets the value of the conversations property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfConversationsType }
     *     
     */
    public ArrayOfConversationsType getConversations() {
        return conversations;
    }

    /**
     * Sets the value of the conversations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfConversationsType }
     *     
     */
    public void setConversations(ArrayOfConversationsType value) {
        this.conversations = value;
    }

}
