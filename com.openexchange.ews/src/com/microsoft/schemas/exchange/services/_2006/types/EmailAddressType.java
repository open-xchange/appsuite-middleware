
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Identifier for a fully resolved email address
 * 
 * <p>Java class for EmailAddressType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EmailAddressType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseEmailAddressType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EmailAddress" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType" minOccurs="0"/>
 *         &lt;element name="RoutingType" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType" minOccurs="0"/>
 *         &lt;element name="MailboxType" type="{http://schemas.microsoft.com/exchange/services/2006/types}MailboxTypeType" minOccurs="0"/>
 *         &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EmailAddressType", propOrder = {
    "name",
    "emailAddress",
    "routingType",
    "mailboxType",
    "itemId"
})
public class EmailAddressType
    extends BaseEmailAddressType
{

    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "EmailAddress")
    protected String emailAddress;
    @XmlElement(name = "RoutingType")
    protected String routingType;
    @XmlElement(name = "MailboxType")
    protected MailboxTypeType mailboxType;
    @XmlElement(name = "ItemId")
    protected ItemIdType itemId;

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
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(String value) {
        this.emailAddress = value;
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

    /**
     * Gets the value of the itemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getItemId() {
        return itemId;
    }

    /**
     * Sets the value of the itemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setItemId(ItemIdType value) {
        this.itemId = value;
    }

}
