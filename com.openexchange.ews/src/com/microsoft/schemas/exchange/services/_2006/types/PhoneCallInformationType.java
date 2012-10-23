
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PhoneCallInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PhoneCallInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PhoneCallState" type="{http://schemas.microsoft.com/exchange/services/2006/types}PhoneCallStateType"/>
 *         &lt;element name="ConnectionFailureCause" type="{http://schemas.microsoft.com/exchange/services/2006/types}ConnectionFailureCauseType"/>
 *         &lt;element name="SIPResponseText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SIPResponseCode" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PhoneCallInformationType", propOrder = {
    "phoneCallState",
    "connectionFailureCause",
    "sipResponseText",
    "sipResponseCode"
})
public class PhoneCallInformationType {

    @XmlElement(name = "PhoneCallState", required = true)
    protected PhoneCallStateType phoneCallState;
    @XmlElement(name = "ConnectionFailureCause", required = true)
    protected ConnectionFailureCauseType connectionFailureCause;
    @XmlElement(name = "SIPResponseText")
    protected String sipResponseText;
    @XmlElement(name = "SIPResponseCode")
    protected Integer sipResponseCode;

    /**
     * Gets the value of the phoneCallState property.
     * 
     * @return
     *     possible object is
     *     {@link PhoneCallStateType }
     *     
     */
    public PhoneCallStateType getPhoneCallState() {
        return phoneCallState;
    }

    /**
     * Sets the value of the phoneCallState property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhoneCallStateType }
     *     
     */
    public void setPhoneCallState(PhoneCallStateType value) {
        this.phoneCallState = value;
    }

    /**
     * Gets the value of the connectionFailureCause property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionFailureCauseType }
     *     
     */
    public ConnectionFailureCauseType getConnectionFailureCause() {
        return connectionFailureCause;
    }

    /**
     * Sets the value of the connectionFailureCause property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionFailureCauseType }
     *     
     */
    public void setConnectionFailureCause(ConnectionFailureCauseType value) {
        this.connectionFailureCause = value;
    }

    /**
     * Gets the value of the sipResponseText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSIPResponseText() {
        return sipResponseText;
    }

    /**
     * Sets the value of the sipResponseText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSIPResponseText(String value) {
        this.sipResponseText = value;
    }

    /**
     * Gets the value of the sipResponseCode property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSIPResponseCode() {
        return sipResponseCode;
    }

    /**
     * Sets the value of the sipResponseCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSIPResponseCode(Integer value) {
        this.sipResponseCode = value;
    }

}
