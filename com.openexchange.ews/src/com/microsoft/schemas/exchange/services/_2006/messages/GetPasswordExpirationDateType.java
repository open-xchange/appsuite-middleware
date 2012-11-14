
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetPasswordExpirationDateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetPasswordExpirationDateType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="MailboxSmtpAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetPasswordExpirationDateType", propOrder = {
    "mailboxSmtpAddress"
})
public class GetPasswordExpirationDateType
    extends BaseRequestType
{

    @XmlElement(name = "MailboxSmtpAddress")
    protected String mailboxSmtpAddress;

    /**
     * Gets the value of the mailboxSmtpAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMailboxSmtpAddress() {
        return mailboxSmtpAddress;
    }

    /**
     * Sets the value of the mailboxSmtpAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMailboxSmtpAddress(String value) {
        this.mailboxSmtpAddress = value;
    }

}
