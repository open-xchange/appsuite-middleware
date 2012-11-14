
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StreamingSubscriptionRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StreamingSubscriptionRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FolderIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseFolderIdsType" minOccurs="0"/>
 *         &lt;element name="EventTypes" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfNotificationEventTypesType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="SubscribeToAllFolders" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StreamingSubscriptionRequestType", propOrder = {
    "folderIds",
    "eventTypes"
})
public class StreamingSubscriptionRequestType {

    @XmlElement(name = "FolderIds")
    protected NonEmptyArrayOfBaseFolderIdsType folderIds;
    @XmlElement(name = "EventTypes", required = true)
    protected NonEmptyArrayOfNotificationEventTypesType eventTypes;
    @XmlAttribute(name = "SubscribeToAllFolders")
    protected Boolean subscribeToAllFolders;

    /**
     * Gets the value of the folderIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public NonEmptyArrayOfBaseFolderIdsType getFolderIds() {
        return folderIds;
    }

    /**
     * Sets the value of the folderIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public void setFolderIds(NonEmptyArrayOfBaseFolderIdsType value) {
        this.folderIds = value;
    }

    /**
     * Gets the value of the eventTypes property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfNotificationEventTypesType }
     *     
     */
    public NonEmptyArrayOfNotificationEventTypesType getEventTypes() {
        return eventTypes;
    }

    /**
     * Sets the value of the eventTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfNotificationEventTypesType }
     *     
     */
    public void setEventTypes(NonEmptyArrayOfNotificationEventTypesType value) {
        this.eventTypes = value;
    }

    /**
     * Gets the value of the subscribeToAllFolders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSubscribeToAllFolders() {
        return subscribeToAllFolders;
    }

    /**
     * Sets the value of the subscribeToAllFolders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSubscribeToAllFolders(Boolean value) {
        this.subscribeToAllFolders = value;
    }

}
