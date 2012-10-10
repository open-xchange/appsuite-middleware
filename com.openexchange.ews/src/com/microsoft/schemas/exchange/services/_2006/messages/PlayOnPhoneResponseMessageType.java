
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.PhoneCallIdType;


/**
 * <p>Java class for PlayOnPhoneResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlayOnPhoneResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="PhoneCallId" type="{http://schemas.microsoft.com/exchange/services/2006/types}PhoneCallIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlayOnPhoneResponseMessageType", propOrder = {
    "phoneCallId"
})
public class PlayOnPhoneResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "PhoneCallId")
    protected PhoneCallIdType phoneCallId;

    /**
     * Gets the value of the phoneCallId property.
     * 
     * @return
     *     possible object is
     *     {@link PhoneCallIdType }
     *     
     */
    public PhoneCallIdType getPhoneCallId() {
        return phoneCallId;
    }

    /**
     * Sets the value of the phoneCallId property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhoneCallIdType }
     *     
     */
    public void setPhoneCallId(PhoneCallIdType value) {
        this.phoneCallId = value;
    }

}
