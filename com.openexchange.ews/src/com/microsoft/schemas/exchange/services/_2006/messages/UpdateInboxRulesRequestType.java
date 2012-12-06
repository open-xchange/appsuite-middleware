
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfRuleOperationsType;


/**
 * <p>Java class for UpdateInboxRulesRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateInboxRulesRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="MailboxSmtpAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RemoveOutlookRuleBlob" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Operations" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRuleOperationsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateInboxRulesRequestType", propOrder = {
    "mailboxSmtpAddress",
    "removeOutlookRuleBlob",
    "operations"
})
public class UpdateInboxRulesRequestType
    extends BaseRequestType
{

    @XmlElement(name = "MailboxSmtpAddress")
    protected String mailboxSmtpAddress;
    @XmlElement(name = "RemoveOutlookRuleBlob")
    protected Boolean removeOutlookRuleBlob;
    @XmlElement(name = "Operations", required = true)
    protected ArrayOfRuleOperationsType operations;

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

    /**
     * Gets the value of the removeOutlookRuleBlob property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRemoveOutlookRuleBlob() {
        return removeOutlookRuleBlob;
    }

    /**
     * Sets the value of the removeOutlookRuleBlob property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveOutlookRuleBlob(Boolean value) {
        this.removeOutlookRuleBlob = value;
    }

    /**
     * Gets the value of the operations property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRuleOperationsType }
     *     
     */
    public ArrayOfRuleOperationsType getOperations() {
        return operations;
    }

    /**
     * Sets the value of the operations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRuleOperationsType }
     *     
     */
    public void setOperations(ArrayOfRuleOperationsType value) {
        this.operations = value;
    }

}
