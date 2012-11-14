
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DelegateUserType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DelegateUserType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserId" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserIdType"/>
 *         &lt;element name="DelegatePermissions" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegatePermissionsType" minOccurs="0"/>
 *         &lt;element name="ReceiveCopiesOfMeetingMessages" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ViewPrivateItems" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelegateUserType", propOrder = {
    "userId",
    "delegatePermissions",
    "receiveCopiesOfMeetingMessages",
    "viewPrivateItems"
})
public class DelegateUserType {

    @XmlElement(name = "UserId", required = true)
    protected UserIdType userId;
    @XmlElement(name = "DelegatePermissions")
    protected DelegatePermissionsType delegatePermissions;
    @XmlElement(name = "ReceiveCopiesOfMeetingMessages")
    protected Boolean receiveCopiesOfMeetingMessages;
    @XmlElement(name = "ViewPrivateItems")
    protected Boolean viewPrivateItems;

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link UserIdType }
     *     
     */
    public UserIdType getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserIdType }
     *     
     */
    public void setUserId(UserIdType value) {
        this.userId = value;
    }

    /**
     * Gets the value of the delegatePermissions property.
     * 
     * @return
     *     possible object is
     *     {@link DelegatePermissionsType }
     *     
     */
    public DelegatePermissionsType getDelegatePermissions() {
        return delegatePermissions;
    }

    /**
     * Sets the value of the delegatePermissions property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegatePermissionsType }
     *     
     */
    public void setDelegatePermissions(DelegatePermissionsType value) {
        this.delegatePermissions = value;
    }

    /**
     * Gets the value of the receiveCopiesOfMeetingMessages property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReceiveCopiesOfMeetingMessages() {
        return receiveCopiesOfMeetingMessages;
    }

    /**
     * Sets the value of the receiveCopiesOfMeetingMessages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReceiveCopiesOfMeetingMessages(Boolean value) {
        this.receiveCopiesOfMeetingMessages = value;
    }

    /**
     * Gets the value of the viewPrivateItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isViewPrivateItems() {
        return viewPrivateItems;
    }

    /**
     * Sets the value of the viewPrivateItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setViewPrivateItems(Boolean value) {
        this.viewPrivateItems = value;
    }

}
