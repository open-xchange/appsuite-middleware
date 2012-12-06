
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for EmailAddressDictionaryEntryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EmailAddressDictionaryEntryType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="Key" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressKeyType" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="RoutingType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="MailboxType" type="{http://schemas.microsoft.com/exchange/services/2006/types}MailboxTypeType" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EmailAddressDictionaryEntryType", propOrder = {
    "value"
})
public class EmailAddressDictionaryEntryType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "Key", required = true)
    protected EmailAddressKeyType key;
    @XmlAttribute(name = "Name")
    protected String name;
    @XmlAttribute(name = "RoutingType")
    protected String routingType;
    @XmlAttribute(name = "MailboxType")
    protected MailboxTypeType mailboxType;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressKeyType }
     *     
     */
    public EmailAddressKeyType getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressKeyType }
     *     
     */
    public void setKey(EmailAddressKeyType value) {
        this.key = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the routingType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoutingType() {
        return routingType;
    }

    /**
     * Sets the value of the routingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoutingType(String value) {
        this.routingType = value;
    }

    /**
     * Gets the value of the mailboxType property.
     * 
     * @return
     *     possible object is
     *     {@link MailboxTypeType }
     *     
     */
    public MailboxTypeType getMailboxType() {
        return mailboxType;
    }

    /**
     * Sets the value of the mailboxType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailboxTypeType }
     *     
     */
    public void setMailboxType(MailboxTypeType value) {
        this.mailboxType = value;
    }

}
