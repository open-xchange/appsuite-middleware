
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotificationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NotificationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SubscriptionId" type="{http://schemas.microsoft.com/exchange/services/2006/types}SubscriptionIdType"/>
 *         &lt;element name="PreviousWatermark" type="{http://schemas.microsoft.com/exchange/services/2006/types}WatermarkType" minOccurs="0"/>
 *         &lt;element name="MoreEvents" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="CopiedEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}MovedCopiedEventType"/>
 *           &lt;element name="CreatedEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}BaseObjectChangedEventType"/>
 *           &lt;element name="DeletedEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}BaseObjectChangedEventType"/>
 *           &lt;element name="ModifiedEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}ModifiedEventType"/>
 *           &lt;element name="MovedEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}MovedCopiedEventType"/>
 *           &lt;element name="NewMailEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}BaseObjectChangedEventType"/>
 *           &lt;element name="StatusEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}BaseNotificationEventType"/>
 *           &lt;element name="FreeBusyChangedEvent" type="{http://schemas.microsoft.com/exchange/services/2006/types}BaseObjectChangedEventType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotificationType", propOrder = {
    "subscriptionId",
    "previousWatermark",
    "moreEvents",
    "copiedEventOrCreatedEventOrDeletedEvent"
})
public class NotificationType {

    @XmlElement(name = "SubscriptionId", required = true)
    protected String subscriptionId;
    @XmlElement(name = "PreviousWatermark")
    protected String previousWatermark;
    @XmlElement(name = "MoreEvents")
    protected Boolean moreEvents;
    @XmlElementRefs({
        @XmlElementRef(name = "StatusEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class),
        @XmlElementRef(name = "NewMailEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class),
        @XmlElementRef(name = "CopiedEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class),
        @XmlElementRef(name = "CreatedEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class),
        @XmlElementRef(name = "FreeBusyChangedEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class),
        @XmlElementRef(name = "ModifiedEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class),
        @XmlElementRef(name = "MovedEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class),
        @XmlElementRef(name = "DeletedEvent", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends BaseNotificationEventType>> copiedEventOrCreatedEventOrDeletedEvent;

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
     * Gets the value of the previousWatermark property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousWatermark() {
        return previousWatermark;
    }

    /**
     * Sets the value of the previousWatermark property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousWatermark(String value) {
        this.previousWatermark = value;
    }

    /**
     * Gets the value of the moreEvents property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMoreEvents() {
        return moreEvents;
    }

    /**
     * Sets the value of the moreEvents property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMoreEvents(Boolean value) {
        this.moreEvents = value;
    }

    /**
     * Gets the value of the copiedEventOrCreatedEventOrDeletedEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the copiedEventOrCreatedEventOrDeletedEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCopiedEventOrCreatedEventOrDeletedEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BaseNotificationEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link BaseObjectChangedEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link MovedCopiedEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link BaseObjectChangedEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link BaseObjectChangedEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link ModifiedEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link BaseObjectChangedEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link MovedCopiedEventType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends BaseNotificationEventType>> getCopiedEventOrCreatedEventOrDeletedEvent() {
        if (copiedEventOrCreatedEventOrDeletedEvent == null) {
            copiedEventOrCreatedEventOrDeletedEvent = new ArrayList<JAXBElement<? extends BaseNotificationEventType>>();
        }
        return this.copiedEventOrCreatedEventOrDeletedEvent;
    }

}
