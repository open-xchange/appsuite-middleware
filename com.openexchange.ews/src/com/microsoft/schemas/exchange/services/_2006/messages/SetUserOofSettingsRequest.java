
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.EmailAddress;
import com.microsoft.schemas.exchange.services._2006.types.UserOofSettings;


/**
 * <p>Java class for SetUserOofSettingsRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SetUserOofSettingsRequest">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}Mailbox"/>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}UserOofSettings"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SetUserOofSettingsRequest", propOrder = {
    "mailbox",
    "userOofSettings"
})
public class SetUserOofSettingsRequest
    extends BaseRequestType
{

    @XmlElement(name = "Mailbox", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", required = true)
    protected EmailAddress mailbox;
    @XmlElement(name = "UserOofSettings", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", required = true)
    protected UserOofSettings userOofSettings;

    /**
     * Gets the value of the mailbox property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddress }
     *     
     */
    public EmailAddress getMailbox() {
        return mailbox;
    }

    /**
     * Sets the value of the mailbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddress }
     *     
     */
    public void setMailbox(EmailAddress value) {
        this.mailbox = value;
    }

    /**
     * Gets the value of the userOofSettings property.
     * 
     * @return
     *     possible object is
     *     {@link UserOofSettings }
     *     
     */
    public UserOofSettings getUserOofSettings() {
        return userOofSettings;
    }

    /**
     * Sets the value of the userOofSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserOofSettings }
     *     
     */
    public void setUserOofSettings(UserOofSettings value) {
        this.userOofSettings = value;
    }

}
