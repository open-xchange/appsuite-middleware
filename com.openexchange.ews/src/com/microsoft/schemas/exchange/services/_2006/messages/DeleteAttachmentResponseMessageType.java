
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.RootItemIdType;


/**
 * <p>Java class for DeleteAttachmentResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeleteAttachmentResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="RootItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}RootItemIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeleteAttachmentResponseMessageType", propOrder = {
    "rootItemId"
})
public class DeleteAttachmentResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "RootItemId")
    protected RootItemIdType rootItemId;

    /**
     * Gets the value of the rootItemId property.
     * 
     * @return
     *     possible object is
     *     {@link RootItemIdType }
     *     
     */
    public RootItemIdType getRootItemId() {
        return rootItemId;
    }

    /**
     * Sets the value of the rootItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link RootItemIdType }
     *     
     */
    public void setRootItemId(RootItemIdType value) {
        this.rootItemId = value;
    }

}
