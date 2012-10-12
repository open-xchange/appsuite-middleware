
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.MailTips;


/**
 * <p>Java class for MailTipsResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailTipsResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="MailTips" type="{http://schemas.microsoft.com/exchange/services/2006/types}MailTips" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailTipsResponseMessageType", propOrder = {
    "mailTips"
})
public class MailTipsResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "MailTips")
    protected MailTips mailTips;

    /**
     * Gets the value of the mailTips property.
     * 
     * @return
     *     possible object is
     *     {@link MailTips }
     *     
     */
    public MailTips getMailTips() {
        return mailTips;
    }

    /**
     * Sets the value of the mailTips property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailTips }
     *     
     */
    public void setMailTips(MailTips value) {
        this.mailTips = value;
    }

}
