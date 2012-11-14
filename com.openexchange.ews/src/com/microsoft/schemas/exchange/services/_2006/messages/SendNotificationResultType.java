
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.SubscriptionStatusType;


/**
 * <p>Java class for SendNotificationResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SendNotificationResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SubscriptionStatus" type="{http://schemas.microsoft.com/exchange/services/2006/types}SubscriptionStatusType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendNotificationResultType", propOrder = {
    "subscriptionStatus"
})
public class SendNotificationResultType {

    @XmlElement(name = "SubscriptionStatus", required = true)
    protected SubscriptionStatusType subscriptionStatus;

    /**
     * Gets the value of the subscriptionStatus property.
     * 
     * @return
     *     possible object is
     *     {@link SubscriptionStatusType }
     *     
     */
    public SubscriptionStatusType getSubscriptionStatus() {
        return subscriptionStatus;
    }

    /**
     * Sets the value of the subscriptionStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubscriptionStatusType }
     *     
     */
    public void setSubscriptionStatus(SubscriptionStatusType value) {
        this.subscriptionStatus = value;
    }

}
