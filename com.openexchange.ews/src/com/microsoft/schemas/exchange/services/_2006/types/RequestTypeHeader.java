
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequestTypeHeader complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestTypeHeader">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequestType" type="{http://schemas.microsoft.com/exchange/services/2006/types}AvailabilityProxyRequestType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestTypeHeader", propOrder = {
    "requestType"
})
public class RequestTypeHeader {

    @XmlElement(name = "RequestType", required = true)
    protected AvailabilityProxyRequestType requestType;

    /**
     * Gets the value of the requestType property.
     * 
     * @return
     *     possible object is
     *     {@link AvailabilityProxyRequestType }
     *     
     */
    public AvailabilityProxyRequestType getRequestType() {
        return requestType;
    }

    /**
     * Sets the value of the requestType property.
     * 
     * @param value
     *     allowed object is
     *     {@link AvailabilityProxyRequestType }
     *     
     */
    public void setRequestType(AvailabilityProxyRequestType value) {
        this.requestType = value;
    }

}
