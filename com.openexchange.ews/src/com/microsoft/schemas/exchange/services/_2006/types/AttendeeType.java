
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for AttendeeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttendeeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Mailbox" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType"/>
 *         &lt;element name="ResponseType" type="{http://schemas.microsoft.com/exchange/services/2006/types}ResponseTypeType" minOccurs="0"/>
 *         &lt;element name="LastResponseTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttendeeType", propOrder = {
    "mailbox",
    "responseType",
    "lastResponseTime"
})
public class AttendeeType {

    @XmlElement(name = "Mailbox", required = true)
    protected EmailAddressType mailbox;
    @XmlElement(name = "ResponseType")
    protected ResponseTypeType responseType;
    @XmlElement(name = "LastResponseTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastResponseTime;

    /**
     * Gets the value of the mailbox property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getMailbox() {
        return mailbox;
    }

    /**
     * Sets the value of the mailbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setMailbox(EmailAddressType value) {
        this.mailbox = value;
    }

    /**
     * Gets the value of the responseType property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseTypeType }
     *     
     */
    public ResponseTypeType getResponseType() {
        return responseType;
    }

    /**
     * Sets the value of the responseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseTypeType }
     *     
     */
    public void setResponseType(ResponseTypeType value) {
        this.responseType = value;
    }

    /**
     * Gets the value of the lastResponseTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastResponseTime() {
        return lastResponseTime;
    }

    /**
     * Sets the value of the lastResponseTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastResponseTime(XMLGregorianCalendar value) {
        this.lastResponseTime = value;
    }

}
