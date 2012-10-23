
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SubscribeResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubscribeResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="SubscriptionId" type="{http://schemas.microsoft.com/exchange/services/2006/types}SubscriptionIdType" minOccurs="0"/>
 *         &lt;element name="Watermark" type="{http://schemas.microsoft.com/exchange/services/2006/types}WatermarkType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubscribeResponseMessageType", propOrder = {
    "subscriptionId",
    "watermark"
})
public class SubscribeResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "SubscriptionId")
    protected String subscriptionId;
    @XmlElement(name = "Watermark")
    protected String watermark;

    /**
     * Gets the value of the subscriptionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the value of the subscriptionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubscriptionId(String value) {
        this.subscriptionId = value;
    }

    /**
     * Gets the value of the watermark property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWatermark() {
        return watermark;
    }

    /**
     * Sets the value of the watermark property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWatermark(String value) {
        this.watermark = value;
    }

}
