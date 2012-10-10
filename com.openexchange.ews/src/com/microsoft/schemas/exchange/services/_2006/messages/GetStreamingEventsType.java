
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfSubscriptionIdsType;


/**
 * <p>Java class for GetStreamingEventsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetStreamingEventsType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="SubscriptionIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfSubscriptionIdsType"/>
 *         &lt;element name="ConnectionTimeout" type="{http://schemas.microsoft.com/exchange/services/2006/types}StreamingSubscriptionConnectionTimeoutType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetStreamingEventsType", propOrder = {
    "subscriptionIds",
    "connectionTimeout"
})
public class GetStreamingEventsType
    extends BaseRequestType
{

    @XmlElement(name = "SubscriptionIds", required = true)
    protected NonEmptyArrayOfSubscriptionIdsType subscriptionIds;
    @XmlElement(name = "ConnectionTimeout")
    protected int connectionTimeout;

    /**
     * Gets the value of the subscriptionIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfSubscriptionIdsType }
     *     
     */
    public NonEmptyArrayOfSubscriptionIdsType getSubscriptionIds() {
        return subscriptionIds;
    }

    /**
     * Sets the value of the subscriptionIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfSubscriptionIdsType }
     *     
     */
    public void setSubscriptionIds(NonEmptyArrayOfSubscriptionIdsType value) {
        this.subscriptionIds = value;
    }

    /**
     * Gets the value of the connectionTimeout property.
     * 
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the value of the connectionTimeout property.
     * 
     */
    public void setConnectionTimeout(int value) {
        this.connectionTimeout = value;
    }

}
