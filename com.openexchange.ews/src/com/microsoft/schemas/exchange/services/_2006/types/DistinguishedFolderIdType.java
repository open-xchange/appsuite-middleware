
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Identifier for a distinguished folder
 * 
 * <p>Java class for DistinguishedFolderIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DistinguishedFolderIdType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseFolderIdType">
 *       &lt;sequence>
 *         &lt;element name="Mailbox" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}DistinguishedFolderIdNameType" />
 *       &lt;attribute name="ChangeKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistinguishedFolderIdType", propOrder = {
    "mailbox"
})
public class DistinguishedFolderIdType
    extends BaseFolderIdType
{

    @XmlElement(name = "Mailbox")
    protected EmailAddressType mailbox;
    @XmlAttribute(name = "Id", required = true)
    protected DistinguishedFolderIdNameType id;
    @XmlAttribute(name = "ChangeKey")
    protected String changeKey;

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
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link DistinguishedFolderIdNameType }
     *     
     */
    public DistinguishedFolderIdNameType getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistinguishedFolderIdNameType }
     *     
     */
    public void setId(DistinguishedFolderIdNameType value) {
        this.id = value;
    }

    /**
     * Gets the value of the changeKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeKey() {
        return changeKey;
    }

    /**
     * Sets the value of the changeKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeKey(String value) {
        this.changeKey = value;
    }

}
