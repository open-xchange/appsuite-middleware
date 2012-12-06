
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfSearchItemKindsType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfSmtpAddressType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfStringsType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfUserMailboxesType;


/**
 * 
 *           Request type for the FindMailboxStatisticsByKeywords web method.
 *         
 * 
 * <p>Java class for FindMailboxStatisticsByKeywordsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindMailboxStatisticsByKeywordsType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="Mailboxes" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfUserMailboxesType"/>
 *         &lt;element name="Keywords" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType"/>
 *         &lt;element name="Language" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Senders" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfSmtpAddressType" minOccurs="0"/>
 *         &lt;element name="Recipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfSmtpAddressType" minOccurs="0"/>
 *         &lt;element name="FromDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ToDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="MessageTypes" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfSearchItemKindsType" minOccurs="0"/>
 *         &lt;element name="SearchDumpster" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IncludePersonalArchive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IncludeUnsearchableItems" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindMailboxStatisticsByKeywordsType", propOrder = {
    "mailboxes",
    "keywords",
    "language",
    "senders",
    "recipients",
    "fromDate",
    "toDate",
    "messageTypes",
    "searchDumpster",
    "includePersonalArchive",
    "includeUnsearchableItems"
})
public class FindMailboxStatisticsByKeywordsType
    extends BaseRequestType
{

    @XmlElement(name = "Mailboxes", required = true)
    protected ArrayOfUserMailboxesType mailboxes;
    @XmlElement(name = "Keywords", required = true)
    protected ArrayOfStringsType keywords;
    @XmlElement(name = "Language")
    protected String language;
    @XmlElement(name = "Senders")
    protected ArrayOfSmtpAddressType senders;
    @XmlElement(name = "Recipients")
    protected ArrayOfSmtpAddressType recipients;
    @XmlElement(name = "FromDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar fromDate;
    @XmlElement(name = "ToDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar toDate;
    @XmlElement(name = "MessageTypes")
    protected ArrayOfSearchItemKindsType messageTypes;
    @XmlElement(name = "SearchDumpster")
    protected Boolean searchDumpster;
    @XmlElement(name = "IncludePersonalArchive")
    protected Boolean includePersonalArchive;
    @XmlElement(name = "IncludeUnsearchableItems")
    protected Boolean includeUnsearchableItems;

    /**
     * Gets the value of the mailboxes property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfUserMailboxesType }
     *     
     */
    public ArrayOfUserMailboxesType getMailboxes() {
        return mailboxes;
    }

    /**
     * Sets the value of the mailboxes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfUserMailboxesType }
     *     
     */
    public void setMailboxes(ArrayOfUserMailboxesType value) {
        this.mailboxes = value;
    }

    /**
     * Gets the value of the keywords property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getKeywords() {
        return keywords;
    }

    /**
     * Sets the value of the keywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setKeywords(ArrayOfStringsType value) {
        this.keywords = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     * Gets the value of the senders property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSmtpAddressType }
     *     
     */
    public ArrayOfSmtpAddressType getSenders() {
        return senders;
    }

    /**
     * Sets the value of the senders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSmtpAddressType }
     *     
     */
    public void setSenders(ArrayOfSmtpAddressType value) {
        this.senders = value;
    }

    /**
     * Gets the value of the recipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSmtpAddressType }
     *     
     */
    public ArrayOfSmtpAddressType getRecipients() {
        return recipients;
    }

    /**
     * Sets the value of the recipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSmtpAddressType }
     *     
     */
    public void setRecipients(ArrayOfSmtpAddressType value) {
        this.recipients = value;
    }

    /**
     * Gets the value of the fromDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFromDate() {
        return fromDate;
    }

    /**
     * Sets the value of the fromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFromDate(XMLGregorianCalendar value) {
        this.fromDate = value;
    }

    /**
     * Gets the value of the toDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getToDate() {
        return toDate;
    }

    /**
     * Sets the value of the toDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setToDate(XMLGregorianCalendar value) {
        this.toDate = value;
    }

    /**
     * Gets the value of the messageTypes property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSearchItemKindsType }
     *     
     */
    public ArrayOfSearchItemKindsType getMessageTypes() {
        return messageTypes;
    }

    /**
     * Sets the value of the messageTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSearchItemKindsType }
     *     
     */
    public void setMessageTypes(ArrayOfSearchItemKindsType value) {
        this.messageTypes = value;
    }

    /**
     * Gets the value of the searchDumpster property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSearchDumpster() {
        return searchDumpster;
    }

    /**
     * Sets the value of the searchDumpster property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSearchDumpster(Boolean value) {
        this.searchDumpster = value;
    }

    /**
     * Gets the value of the includePersonalArchive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludePersonalArchive() {
        return includePersonalArchive;
    }

    /**
     * Sets the value of the includePersonalArchive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludePersonalArchive(Boolean value) {
        this.includePersonalArchive = value;
    }

    /**
     * Gets the value of the includeUnsearchableItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeUnsearchableItems() {
        return includeUnsearchableItems;
    }

    /**
     * Sets the value of the includeUnsearchableItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeUnsearchableItems(Boolean value) {
        this.includeUnsearchableItems = value;
    }

}
