
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ConnectionStatusType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfNotificationsType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfSubscriptionIdsType;


/**
 * <p>Java class for GetStreamingEventsResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetStreamingEventsResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="Notifications" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfNotificationsType" minOccurs="0"/>
 *         &lt;element name="ErrorSubscriptionIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfSubscriptionIdsType" minOccurs="0"/>
 *         &lt;element name="ConnectionStatus" type="{http://schemas.microsoft.com/exchange/services/2006/types}ConnectionStatusType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetStreamingEventsResponseMessageType", propOrder = {
    "notifications",
    "errorSubscriptionIds",
    "connectionStatus"
})
public class GetStreamingEventsResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "Notifications")
    protected NonEmptyArrayOfNotificationsType notifications;
    @XmlElement(name = "ErrorSubscriptionIds")
    protected NonEmptyArrayOfSubscriptionIdsType errorSubscriptionIds;
    @XmlElement(name = "ConnectionStatus")
    protected ConnectionStatusType connectionStatus;

    /**
     * Gets the value of the notifications property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfNotificationsType }
     *     
     */
    public NonEmptyArrayOfNotificationsType getNotifications() {
        return notifications;
    }

    /**
     * Sets the value of the notifications property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfNotificationsType }
     *     
     */
    public void setNotifications(NonEmptyArrayOfNotificationsType value) {
        this.notifications = value;
    }

    /**
     * Gets the value of the errorSubscriptionIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfSubscriptionIdsType }
     *     
     */
    public NonEmptyArrayOfSubscriptionIdsType getErrorSubscriptionIds() {
        return errorSubscriptionIds;
    }

    /**
     * Sets the value of the errorSubscriptionIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfSubscriptionIdsType }
     *     
     */
    public void setErrorSubscriptionIds(NonEmptyArrayOfSubscriptionIdsType value) {
        this.errorSubscriptionIds = value;
    }

    /**
     * Gets the value of the connectionStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionStatusType }
     *     
     */
    public ConnectionStatusType getConnectionStatus() {
        return connectionStatus;
    }

    /**
     * Sets the value of the connectionStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionStatusType }
     *     
     */
    public void setConnectionStatus(ConnectionStatusType value) {
        this.connectionStatus = value;
    }

}
