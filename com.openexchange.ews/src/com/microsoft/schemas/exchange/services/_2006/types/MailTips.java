
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MailTips complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailTips">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RecipientAddress" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType"/>
 *         &lt;element name="PendingMailTips" type="{http://schemas.microsoft.com/exchange/services/2006/types}MailTipTypes"/>
 *         &lt;element name="OutOfOffice" type="{http://schemas.microsoft.com/exchange/services/2006/types}OutOfOfficeMailTip" minOccurs="0"/>
 *         &lt;element name="MailboxFull" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CustomMailTip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TotalMemberCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ExternalMemberCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MaxMessageSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="DeliveryRestricted" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsModerated" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="InvalidRecipient" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailTips", propOrder = {
    "recipientAddress",
    "pendingMailTips",
    "outOfOffice",
    "mailboxFull",
    "customMailTip",
    "totalMemberCount",
    "externalMemberCount",
    "maxMessageSize",
    "deliveryRestricted",
    "isModerated",
    "invalidRecipient"
})
public class MailTips {

    @XmlElement(name = "RecipientAddress", required = true)
    protected EmailAddressType recipientAddress;
    @XmlList
    @XmlElement(name = "PendingMailTips", required = true)
    protected List<String> pendingMailTips;
    @XmlElement(name = "OutOfOffice")
    protected OutOfOfficeMailTip outOfOffice;
    @XmlElement(name = "MailboxFull")
    protected Boolean mailboxFull;
    @XmlElement(name = "CustomMailTip")
    protected String customMailTip;
    @XmlElement(name = "TotalMemberCount")
    protected Integer totalMemberCount;
    @XmlElement(name = "ExternalMemberCount")
    protected Integer externalMemberCount;
    @XmlElement(name = "MaxMessageSize")
    protected Integer maxMessageSize;
    @XmlElement(name = "DeliveryRestricted")
    protected Boolean deliveryRestricted;
    @XmlElement(name = "IsModerated")
    protected Boolean isModerated;
    @XmlElement(name = "InvalidRecipient")
    protected Boolean invalidRecipient;

    /**
     * Gets the value of the recipientAddress property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getRecipientAddress() {
        return recipientAddress;
    }

    /**
     * Sets the value of the recipientAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setRecipientAddress(EmailAddressType value) {
        this.recipientAddress = value;
    }

    /**
     * Gets the value of the pendingMailTips property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pendingMailTips property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPendingMailTips().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPendingMailTips() {
        if (pendingMailTips == null) {
            pendingMailTips = new ArrayList<String>();
        }
        return this.pendingMailTips;
    }

    /**
     * Gets the value of the outOfOffice property.
     * 
     * @return
     *     possible object is
     *     {@link OutOfOfficeMailTip }
     *     
     */
    public OutOfOfficeMailTip getOutOfOffice() {
        return outOfOffice;
    }

    /**
     * Sets the value of the outOfOffice property.
     * 
     * @param value
     *     allowed object is
     *     {@link OutOfOfficeMailTip }
     *     
     */
    public void setOutOfOffice(OutOfOfficeMailTip value) {
        this.outOfOffice = value;
    }

    /**
     * Gets the value of the mailboxFull property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMailboxFull() {
        return mailboxFull;
    }

    /**
     * Sets the value of the mailboxFull property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMailboxFull(Boolean value) {
        this.mailboxFull = value;
    }

    /**
     * Gets the value of the customMailTip property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomMailTip() {
        return customMailTip;
    }

    /**
     * Sets the value of the customMailTip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomMailTip(String value) {
        this.customMailTip = value;
    }

    /**
     * Gets the value of the totalMemberCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalMemberCount() {
        return totalMemberCount;
    }

    /**
     * Sets the value of the totalMemberCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalMemberCount(Integer value) {
        this.totalMemberCount = value;
    }

    /**
     * Gets the value of the externalMemberCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getExternalMemberCount() {
        return externalMemberCount;
    }

    /**
     * Sets the value of the externalMemberCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setExternalMemberCount(Integer value) {
        this.externalMemberCount = value;
    }

    /**
     * Gets the value of the maxMessageSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Sets the value of the maxMessageSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxMessageSize(Integer value) {
        this.maxMessageSize = value;
    }

    /**
     * Gets the value of the deliveryRestricted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDeliveryRestricted() {
        return deliveryRestricted;
    }

    /**
     * Sets the value of the deliveryRestricted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeliveryRestricted(Boolean value) {
        this.deliveryRestricted = value;
    }

    /**
     * Gets the value of the isModerated property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsModerated() {
        return isModerated;
    }

    /**
     * Sets the value of the isModerated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsModerated(Boolean value) {
        this.isModerated = value;
    }

    /**
     * Gets the value of the invalidRecipient property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInvalidRecipient() {
        return invalidRecipient;
    }

    /**
     * Sets the value of the invalidRecipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInvalidRecipient(Boolean value) {
        this.invalidRecipient = value;
    }

}
