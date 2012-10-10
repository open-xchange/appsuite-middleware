
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetServiceConfigurationResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetServiceConfigurationResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="ResponseMessages" type="{http://schemas.microsoft.com/exchange/services/2006/messages}ArrayOfServiceConfigurationResponseMessageType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetServiceConfigurationResponseMessageType", propOrder = {
    "responseMessages"
})
public class GetServiceConfigurationResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "ResponseMessages")
    protected ArrayOfServiceConfigurationResponseMessageType responseMessages;

    /**
     * Gets the value of the responseMessages property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfServiceConfigurationResponseMessageType }
     *     
     */
    public ArrayOfServiceConfigurationResponseMessageType getResponseMessages() {
        return responseMessages;
    }

    /**
     * Sets the value of the responseMessages property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfServiceConfigurationResponseMessageType }
     *     
     */
    public void setResponseMessages(ArrayOfServiceConfigurationResponseMessageType value) {
        this.responseMessages = value;
    }

}
