
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MailTipsServiceConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailTipsServiceConfiguration">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ServiceConfiguration">
 *       &lt;sequence>
 *         &lt;element name="MailTipsEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="MaxRecipientsPerGetMailTipsRequest" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MaxMessageSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="LargeAudienceThreshold" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ShowExternalRecipientCount" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="InternalDomains" type="{http://schemas.microsoft.com/exchange/services/2006/types}SmtpDomainList"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailTipsServiceConfiguration", propOrder = {
    "mailTipsEnabled",
    "maxRecipientsPerGetMailTipsRequest",
    "maxMessageSize",
    "largeAudienceThreshold",
    "showExternalRecipientCount",
    "internalDomains"
})
public class MailTipsServiceConfiguration
    extends ServiceConfiguration
{

    @XmlElement(name = "MailTipsEnabled")
    protected boolean mailTipsEnabled;
    @XmlElement(name = "MaxRecipientsPerGetMailTipsRequest")
    protected int maxRecipientsPerGetMailTipsRequest;
    @XmlElement(name = "MaxMessageSize")
    protected int maxMessageSize;
    @XmlElement(name = "LargeAudienceThreshold")
    protected int largeAudienceThreshold;
    @XmlElement(name = "ShowExternalRecipientCount")
    protected boolean showExternalRecipientCount;
    @XmlElement(name = "InternalDomains", required = true)
    protected SmtpDomainList internalDomains;

    /**
     * Gets the value of the mailTipsEnabled property.
     * 
     */
    public boolean isMailTipsEnabled() {
        return mailTipsEnabled;
    }

    /**
     * Sets the value of the mailTipsEnabled property.
     * 
     */
    public void setMailTipsEnabled(boolean value) {
        this.mailTipsEnabled = value;
    }

    /**
     * Gets the value of the maxRecipientsPerGetMailTipsRequest property.
     * 
     */
    public int getMaxRecipientsPerGetMailTipsRequest() {
        return maxRecipientsPerGetMailTipsRequest;
    }

    /**
     * Sets the value of the maxRecipientsPerGetMailTipsRequest property.
     * 
     */
    public void setMaxRecipientsPerGetMailTipsRequest(int value) {
        this.maxRecipientsPerGetMailTipsRequest = value;
    }

    /**
     * Gets the value of the maxMessageSize property.
     * 
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Sets the value of the maxMessageSize property.
     * 
     */
    public void setMaxMessageSize(int value) {
        this.maxMessageSize = value;
    }

    /**
     * Gets the value of the largeAudienceThreshold property.
     * 
     */
    public int getLargeAudienceThreshold() {
        return largeAudienceThreshold;
    }

    /**
     * Sets the value of the largeAudienceThreshold property.
     * 
     */
    public void setLargeAudienceThreshold(int value) {
        this.largeAudienceThreshold = value;
    }

    /**
     * Gets the value of the showExternalRecipientCount property.
     * 
     */
    public boolean isShowExternalRecipientCount() {
        return showExternalRecipientCount;
    }

    /**
     * Sets the value of the showExternalRecipientCount property.
     * 
     */
    public void setShowExternalRecipientCount(boolean value) {
        this.showExternalRecipientCount = value;
    }

    /**
     * Gets the value of the internalDomains property.
     * 
     * @return
     *     possible object is
     *     {@link SmtpDomainList }
     *     
     */
    public SmtpDomainList getInternalDomains() {
        return internalDomains;
    }

    /**
     * Sets the value of the internalDomains property.
     * 
     * @param value
     *     allowed object is
     *     {@link SmtpDomainList }
     *     
     */
    public void setInternalDomains(SmtpDomainList value) {
        this.internalDomains = value;
    }

}
