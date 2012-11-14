
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfDelegateUserType;
import com.microsoft.schemas.exchange.services._2006.types.DeliverMeetingRequestsType;


/**
 * <p>Java class for AddDelegateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AddDelegateType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseDelegateType">
 *       &lt;sequence>
 *         &lt;element name="DelegateUsers" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfDelegateUserType"/>
 *         &lt;element name="DeliverMeetingRequests" type="{http://schemas.microsoft.com/exchange/services/2006/types}DeliverMeetingRequestsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AddDelegateType", propOrder = {
    "delegateUsers",
    "deliverMeetingRequests"
})
public class AddDelegateType
    extends BaseDelegateType
{

    @XmlElement(name = "DelegateUsers", required = true)
    protected ArrayOfDelegateUserType delegateUsers;
    @XmlElement(name = "DeliverMeetingRequests")
    protected DeliverMeetingRequestsType deliverMeetingRequests;

    /**
     * Gets the value of the delegateUsers property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfDelegateUserType }
     *     
     */
    public ArrayOfDelegateUserType getDelegateUsers() {
        return delegateUsers;
    }

    /**
     * Sets the value of the delegateUsers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfDelegateUserType }
     *     
     */
    public void setDelegateUsers(ArrayOfDelegateUserType value) {
        this.delegateUsers = value;
    }

    /**
     * Gets the value of the deliverMeetingRequests property.
     * 
     * @return
     *     possible object is
     *     {@link DeliverMeetingRequestsType }
     *     
     */
    public DeliverMeetingRequestsType getDeliverMeetingRequests() {
        return deliverMeetingRequests;
    }

    /**
     * Sets the value of the deliverMeetingRequests property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeliverMeetingRequestsType }
     *     
     */
    public void setDeliverMeetingRequests(DeliverMeetingRequestsType value) {
        this.deliverMeetingRequests = value;
    }

}
