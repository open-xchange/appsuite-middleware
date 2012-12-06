
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.AttachmentResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfRequestAttachmentIdsType;


/**
 * <p>Java class for GetAttachmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetAttachmentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="AttachmentShape" type="{http://schemas.microsoft.com/exchange/services/2006/types}AttachmentResponseShapeType" minOccurs="0"/>
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
@XmlType(name = "GetAttachmentType", propOrder = {
    "attachmentShape",
    "attachmentIds"
})
public class GetAttachmentType
    extends BaseRequestType
{

    @XmlElement(name = "AttachmentShape")
    protected AttachmentResponseShapeType attachmentShape;
    @XmlElement(name = "AttachmentIds", required = true)
    protected NonEmptyArrayOfRequestAttachmentIdsType attachmentIds;

    /**
     * Gets the value of the attachmentShape property.
     * 
     * @return
     *     possible object is
     *     {@link AttachmentResponseShapeType }
     *     
     */
    public AttachmentResponseShapeType getAttachmentShape() {
        return attachmentShape;
    }

    /**
     * Sets the value of the attachmentShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttachmentResponseShapeType }
     *     
     */
    public void setAttachmentShape(AttachmentResponseShapeType value) {
        this.attachmentShape = value;
    }

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
