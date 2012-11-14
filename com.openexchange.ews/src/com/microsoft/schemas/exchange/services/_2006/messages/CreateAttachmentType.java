
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ItemIdType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfAttachmentsType;


/**
 * <p>Java class for CreateAttachmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateAttachmentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ParentItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType"/>
 *         &lt;element name="Attachments" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAttachmentsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateAttachmentType", propOrder = {
    "parentItemId",
    "attachments"
})
public class CreateAttachmentType
    extends BaseRequestType
{

    @XmlElement(name = "ParentItemId", required = true)
    protected ItemIdType parentItemId;
    @XmlElement(name = "Attachments", required = true)
    protected NonEmptyArrayOfAttachmentsType attachments;

    /**
     * Gets the value of the parentItemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getParentItemId() {
        return parentItemId;
    }

    /**
     * Sets the value of the parentItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setParentItemId(ItemIdType value) {
        this.parentItemId = value;
    }

    /**
     * Gets the value of the attachments property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAttachmentsType }
     *     
     */
    public NonEmptyArrayOfAttachmentsType getAttachments() {
        return attachments;
    }

    /**
     * Sets the value of the attachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAttachmentsType }
     *     
     */
    public void setAttachments(NonEmptyArrayOfAttachmentsType value) {
        this.attachments = value;
    }

}
