
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.PullSubscriptionRequestType;
import com.microsoft.schemas.exchange.services._2006.types.PushSubscriptionRequestType;
import com.microsoft.schemas.exchange.services._2006.types.StreamingSubscriptionRequestType;


/**
 * <p>Java class for SubscribeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubscribeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;choice>
 *         &lt;element name="PullSubscriptionRequest" type="{http://schemas.microsoft.com/exchange/services/2006/types}PullSubscriptionRequestType"/>
 *         &lt;element name="PushSubscriptionRequest" type="{http://schemas.microsoft.com/exchange/services/2006/types}PushSubscriptionRequestType"/>
 *         &lt;element name="StreamingSubscriptionRequest" type="{http://schemas.microsoft.com/exchange/services/2006/types}StreamingSubscriptionRequestType"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubscribeType", propOrder = {
    "pullSubscriptionRequest",
    "pushSubscriptionRequest",
    "streamingSubscriptionRequest"
})
public class SubscribeType
    extends BaseRequestType
{

    @XmlElement(name = "PullSubscriptionRequest")
    protected PullSubscriptionRequestType pullSubscriptionRequest;
    @XmlElement(name = "PushSubscriptionRequest")
    protected PushSubscriptionRequestType pushSubscriptionRequest;
    @XmlElement(name = "StreamingSubscriptionRequest")
    protected StreamingSubscriptionRequestType streamingSubscriptionRequest;

    /**
     * Gets the value of the pullSubscriptionRequest property.
     * 
     * @return
     *     possible object is
     *     {@link PullSubscriptionRequestType }
     *     
     */
    public PullSubscriptionRequestType getPullSubscriptionRequest() {
        return pullSubscriptionRequest;
    }

    /**
     * Sets the value of the pullSubscriptionRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link PullSubscriptionRequestType }
     *     
     */
    public void setPullSubscriptionRequest(PullSubscriptionRequestType value) {
        this.pullSubscriptionRequest = value;
    }

    /**
     * Gets the value of the pushSubscriptionRequest property.
     * 
     * @return
     *     possible object is
     *     {@link PushSubscriptionRequestType }
     *     
     */
    public PushSubscriptionRequestType getPushSubscriptionRequest() {
        return pushSubscriptionRequest;
    }

    /**
     * Sets the value of the pushSubscriptionRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link PushSubscriptionRequestType }
     *     
     */
    public void setPushSubscriptionRequest(PushSubscriptionRequestType value) {
        this.pushSubscriptionRequest = value;
    }

    /**
     * Gets the value of the streamingSubscriptionRequest property.
     * 
     * @return
     *     possible object is
     *     {@link StreamingSubscriptionRequestType }
     *     
     */
    public StreamingSubscriptionRequestType getStreamingSubscriptionRequest() {
        return streamingSubscriptionRequest;
    }

    /**
     * Sets the value of the streamingSubscriptionRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link StreamingSubscriptionRequestType }
     *     
     */
    public void setStreamingSubscriptionRequest(StreamingSubscriptionRequestType value) {
        this.streamingSubscriptionRequest = value;
    }

}
