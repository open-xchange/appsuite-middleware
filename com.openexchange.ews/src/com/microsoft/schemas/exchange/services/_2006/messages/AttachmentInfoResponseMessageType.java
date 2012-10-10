
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfAttachmentsType;


/**
 * <p>Java class for AttachmentInfoResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttachmentInfoResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="Attachments" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfAttachmentsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttachmentInfoResponseMessageType", propOrder = {
    "attachments"
})
public class AttachmentInfoResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "Attachments", required = true)
    protected ArrayOfAttachmentsType attachments;

    /**
     * Gets the value of the attachments property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAttachmentsType }
     *     
     */
    public ArrayOfAttachmentsType getAttachments() {
        return attachments;
    }

    /**
     * Sets the value of the attachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAttachmentsType }
     *     
     */
    public void setAttachments(ArrayOfAttachmentsType value) {
        this.attachments = value;
    }

}
