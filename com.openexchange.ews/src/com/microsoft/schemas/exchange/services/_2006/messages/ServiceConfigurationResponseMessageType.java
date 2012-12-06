
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.MailTipsServiceConfiguration;
import com.microsoft.schemas.exchange.services._2006.types.ProtectionRulesServiceConfiguration;
import com.microsoft.schemas.exchange.services._2006.types.UnifiedMessageServiceConfiguration;


/**
 * <p>Java class for ServiceConfigurationResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceConfigurationResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="MailTipsConfiguration" type="{http://schemas.microsoft.com/exchange/services/2006/types}MailTipsServiceConfiguration" minOccurs="0"/>
 *         &lt;element name="UnifiedMessagingConfiguration" type="{http://schemas.microsoft.com/exchange/services/2006/types}UnifiedMessageServiceConfiguration" minOccurs="0"/>
 *         &lt;element name="ProtectionRulesConfiguration" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRulesServiceConfiguration" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceConfigurationResponseMessageType", propOrder = {
    "mailTipsConfiguration",
    "unifiedMessagingConfiguration",
    "protectionRulesConfiguration"
})
public class ServiceConfigurationResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "MailTipsConfiguration")
    protected MailTipsServiceConfiguration mailTipsConfiguration;
    @XmlElement(name = "UnifiedMessagingConfiguration")
    protected UnifiedMessageServiceConfiguration unifiedMessagingConfiguration;
    @XmlElement(name = "ProtectionRulesConfiguration")
    protected ProtectionRulesServiceConfiguration protectionRulesConfiguration;

    /**
     * Gets the value of the mailTipsConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link MailTipsServiceConfiguration }
     *     
     */
    public MailTipsServiceConfiguration getMailTipsConfiguration() {
        return mailTipsConfiguration;
    }

    /**
     * Sets the value of the mailTipsConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailTipsServiceConfiguration }
     *     
     */
    public void setMailTipsConfiguration(MailTipsServiceConfiguration value) {
        this.mailTipsConfiguration = value;
    }

    /**
     * Gets the value of the unifiedMessagingConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link UnifiedMessageServiceConfiguration }
     *     
     */
    public UnifiedMessageServiceConfiguration getUnifiedMessagingConfiguration() {
        return unifiedMessagingConfiguration;
    }

    /**
     * Sets the value of the unifiedMessagingConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnifiedMessageServiceConfiguration }
     *     
     */
    public void setUnifiedMessagingConfiguration(UnifiedMessageServiceConfiguration value) {
        this.unifiedMessagingConfiguration = value;
    }

    /**
     * Gets the value of the protectionRulesConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link ProtectionRulesServiceConfiguration }
     *     
     */
    public ProtectionRulesServiceConfiguration getProtectionRulesConfiguration() {
        return protectionRulesConfiguration;
    }

    /**
     * Sets the value of the protectionRulesConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtectionRulesServiceConfiguration }
     *     
     */
    public void setProtectionRulesConfiguration(ProtectionRulesServiceConfiguration value) {
        this.protectionRulesConfiguration = value;
    }

}
