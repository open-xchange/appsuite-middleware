
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.FreeBusyView;


/**
 * <p>Java class for FreeBusyResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FreeBusyResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ResponseMessage" type="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType" minOccurs="0"/>
 *         &lt;element name="FreeBusyView" type="{http://schemas.microsoft.com/exchange/services/2006/types}FreeBusyView" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FreeBusyResponseType", propOrder = {
    "responseMessage",
    "freeBusyView"
})
public class FreeBusyResponseType {

    @XmlElement(name = "ResponseMessage")
    protected ResponseMessageType responseMessage;
    @XmlElement(name = "FreeBusyView")
    protected FreeBusyView freeBusyView;

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
     * Gets the value of the freeBusyView property.
     * 
     * @return
     *     possible object is
     *     {@link FreeBusyView }
     *     
     */
    public FreeBusyView getFreeBusyView() {
        return freeBusyView;
    }

    /**
     * Sets the value of the freeBusyView property.
     * 
     * @param value
     *     allowed object is
     *     {@link FreeBusyView }
     *     
     */
    public void setFreeBusyView(FreeBusyView value) {
        this.freeBusyView = value;
    }

}
