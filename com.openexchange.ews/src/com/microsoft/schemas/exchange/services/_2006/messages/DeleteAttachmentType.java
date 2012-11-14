
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfRequestAttachmentIdsType;


/**
 * <p>Java class for DeleteAttachmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeleteAttachmentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="AttachmentIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfRequestAttachmentIdsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeleteAttachmentType", propOrder = {
    "attachmentIds"
})
public class DeleteAttachmentType
    extends BaseRequestType
{

    @XmlElement(name = "AttachmentIds", required = true)
    protected NonEmptyArrayOfRequestAttachmentIdsType attachmentIds;

    /**
     * Gets the value of the attachmentIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfRequestAttachmentIdsType }
     *     
     */
    public NonEmptyArrayOfRequestAttachmentIdsType getAttachmentIds() {
        return attachmentIds;
    }

    /**
     * Sets the value of the attachmentIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfRequestAttachmentIdsType }
     *     
     */
    public void setAttachmentIds(NonEmptyArrayOfRequestAttachmentIdsType value) {
        this.attachmentIds = value;
    }

}
