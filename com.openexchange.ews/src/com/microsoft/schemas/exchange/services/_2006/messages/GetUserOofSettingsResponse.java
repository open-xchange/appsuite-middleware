
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ExternalAudience;
import com.microsoft.schemas.exchange.services._2006.types.UserOofSettings;


/**
 * <p>Java class for GetUserOofSettingsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetUserOofSettingsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ResponseMessage" type="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType"/>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}OofSettings" minOccurs="0"/>
 *         &lt;element name="AllowExternalOof" type="{http://schemas.microsoft.com/exchange/services/2006/types}ExternalAudience" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUserOofSettingsResponse", propOrder = {
    "responseMessage",
    "oofSettings",
    "allowExternalOof"
})
public class GetUserOofSettingsResponse {

    @XmlElement(name = "ResponseMessage", required = true)
    protected ResponseMessageType responseMessage;
    @XmlElement(name = "OofSettings", namespace = "http://schemas.microsoft.com/exchange/services/2006/types")
    protected UserOofSettings oofSettings;
    @XmlElement(name = "AllowExternalOof")
    protected ExternalAudience allowExternalOof;

    /**
     * Gets the value of the responseMessage property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseMessageType }
     *     
     */
    public ResponseMessageType getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets the value of the responseMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseMessageType }
     *     
     */
    public void setResponseMessage(ResponseMessageType value) {
        this.responseMessage = value;
    }

    /**
     * Gets the value of the oofSettings property.
     * 
     * @return
     *     possible object is
     *     {@link UserOofSettings }
     *     
     */
    public UserOofSettings getOofSettings() {
        return oofSettings;
    }

    /**
     * Sets the value of the oofSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserOofSettings }
     *     
     */
    public void setOofSettings(UserOofSettings value) {
        this.oofSettings = value;
    }

    /**
     * Gets the value of the allowExternalOof property.
     * 
     * @return
     *     possible object is
     *     {@link ExternalAudience }
     *     
     */
    public ExternalAudience getAllowExternalOof() {
        return allowExternalOof;
    }

    /**
     * Sets the value of the allowExternalOof property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExternalAudience }
     *     
     */
    public void setAllowExternalOof(ExternalAudience value) {
        this.allowExternalOof = value;
    }

}
