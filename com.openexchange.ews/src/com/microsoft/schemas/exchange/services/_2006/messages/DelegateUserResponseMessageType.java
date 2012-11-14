
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.DelegateUserType;


/**
 * <p>Java class for DelegateUserResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DelegateUserResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="DelegateUser" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateUserType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelegateUserResponseMessageType", propOrder = {
    "delegateUser"
})
public class DelegateUserResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "DelegateUser")
    protected DelegateUserType delegateUser;

    /**
     * Gets the value of the delegateUser property.
     * 
     * @return
     *     possible object is
     *     {@link DelegateUserType }
     *     
     */
    public DelegateUserType getDelegateUser() {
        return delegateUser;
    }

    /**
     * Sets the value of the delegateUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegateUserType }
     *     
     */
    public void setDelegateUser(DelegateUserType value) {
        this.delegateUser = value;
    }

}
