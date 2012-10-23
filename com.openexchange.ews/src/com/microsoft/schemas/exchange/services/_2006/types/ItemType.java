
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MimeContent" type="{http://schemas.microsoft.com/exchange/services/2006/types}MimeContentType" minOccurs="0"/>
 *         &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *         &lt;element name="ParentFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType" minOccurs="0"/>
 *         &lt;element name="ItemClass" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemClassType" minOccurs="0"/>
 *         &lt;element name="Subject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sensitivity" type="{http://schemas.microsoft.com/exchange/services/2006/types}SensitivityChoicesType" minOccurs="0"/>
 *         &lt;element name="Body" type="{http://schemas.microsoft.com/exchange/services/2006/types}BodyType" minOccurs="0"/>
 *         &lt;element name="Attachments" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAttachmentsType" minOccurs="0"/>
 *         &lt;element name="DateTimeReceived" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Size" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Categories" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="Importance" type="{http://schemas.microsoft.com/exchange/services/2006/types}ImportanceChoicesType" minOccurs="0"/>
 *         &lt;element name="InReplyTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsSubmitted" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsDraft" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsFromMe" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsResend" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsUnmodified" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="InternetMessageHeaders" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfInternetHeadersType" minOccurs="0"/>
 *         &lt;element name="DateTimeSent" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="DateTimeCreated" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ResponseObjects" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfResponseObjectsType" minOccurs="0"/>
 *         &lt;element name="ReminderDueBy" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ReminderIsSet" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ReminderMinutesBeforeStart" type="{http://schemas.microsoft.com/exchange/services/2006/types}ReminderMinutesBeforeStartType" minOccurs="0"/>
 *         &lt;element name="DisplayCc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HasAttachments" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ExtendedProperty" type="{http://schemas.microsoft.com/exchange/services/2006/types}ExtendedPropertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Culture" type="{http://www.w3.org/2001/XMLSchema}language" minOccurs="0"/>
 *         &lt;element name="EffectiveRights" type="{http://schemas.microsoft.com/exchange/services/2006/types}EffectiveRightsType" minOccurs="0"/>
 *         &lt;element name="LastModifiedName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LastModifiedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="IsAssociated" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="WebClientReadFormQueryString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WebClientEditFormQueryString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ConversationId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *         &lt;element name="UniqueBody" type="{http://schemas.microsoft.com/exchange/services/2006/types}BodyType" minOccurs="0"/>
 *         &lt;element name="StoreEntryId" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemType", propOrder = {
    "mimeContent",
    "itemId",
    "parentFolderId",
    "itemClass",
    "subject",
    "sensitivity",
    "body",
    "attachments",
    "dateTimeReceived",
    "size",
    "categories",
    "importance",
    "inReplyTo",
    "isSubmitted",
    "isDraft",
    "isFromMe",
    "isResend",
    "isUnmodified",
    "internetMessageHeaders",
    "dateTimeSent",
    "dateTimeCreated",
    "responseObjects",
    "reminderDueBy",
    "reminderIsSet",
    "reminderMinutesBeforeStart",
    "displayCc",
    "displayTo",
    "hasAttachments",
    "extendedProperty",
    "culture",
    "effectiveRights",
    "lastModifiedName",
    "lastModifiedTime",
    "isAssociated",
    "webClientReadFormQueryString",
    "webClientEditFormQueryString",
    "conversationId",
    "uniqueBody",
    "storeEntryId"
})
@XmlSeeAlso({
    TaskType.class,
    PostItemType.class,
    ContactItemType.class,
    CalendarItemType.class,
    MessageType.class,
    DistributionListType.class
})
public class ItemType {

    @XmlElement(name = "MimeContent")
    protected MimeContentType mimeContent;
    @XmlElement(name = "ItemId")
    protected ItemIdType itemId;
    @XmlElement(name = "ParentFolderId")
    protected FolderIdType parentFolderId;
    @XmlElement(name = "ItemClass")
    protected String itemClass;
    @XmlElement(name = "Subject")
    protected String subject;
    @XmlElement(name = "Sensitivity")
    protected SensitivityChoicesType sensitivity;
    @XmlElement(name = "Body")
    protected BodyType body;
    @XmlElement(name = "Attachments")
    protected NonEmptyArrayOfAttachmentsType attachments;
    @XmlElement(name = "DateTimeReceived")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeReceived;
    @XmlElement(name = "Size")
    protected Integer size;
    @XmlElement(name = "Categories")
    protected ArrayOfStringsType categories;
    @XmlElement(name = "Importance")
    protected ImportanceChoicesType importance;
    @XmlElement(name = "InReplyTo")
    protected String inReplyTo;
    @XmlElement(name = "IsSubmitted")
    protected Boolean isSubmitted;
    @XmlElement(name = "IsDraft")
    protected Boolean isDraft;
    @XmlElement(name = "IsFromMe")
    protected Boolean isFromMe;
    @XmlElement(name = "IsResend")
    protected Boolean isResend;
    @XmlElement(name = "IsUnmodified")
    protected Boolean isUnmodified;
    @XmlElement(name = "InternetMessageHeaders")
    protected NonEmptyArrayOfInternetHeadersType internetMessageHeaders;
    @XmlElement(name = "DateTimeSent")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeSent;
    @XmlElement(name = "DateTimeCreated")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeCreated;
    @XmlElement(name = "ResponseObjects")
    protected NonEmptyArrayOfResponseObjectsType responseObjects;
    @XmlElement(name = "ReminderDueBy")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar reminderDueBy;
    @XmlElement(name = "ReminderIsSet")
    protected Boolean reminderIsSet;
    @XmlElement(name = "ReminderMinutesBeforeStart")
    protected String reminderMinutesBeforeStart;
    @XmlElement(name = "DisplayCc")
    protected String displayCc;
    @XmlElement(name = "DisplayTo")
    protected String displayTo;
    @XmlElement(name = "HasAttachments")
    protected Boolean hasAttachments;
    @XmlElement(name = "ExtendedProperty")
    protected List<ExtendedPropertyType> extendedProperty;
    @XmlElement(name = "Culture")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    protected String culture;
    @XmlElement(name = "EffectiveRights")
    protected EffectiveRightsType effectiveRights;
    @XmlElement(name = "LastModifiedName")
    protected String lastModifiedName;
    @XmlElement(name = "LastModifiedTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastModifiedTime;
    @XmlElement(name = "IsAssociated")
    protected Boolean isAssociated;
    @XmlElement(name = "WebClientReadFormQueryString")
    protected String webClientReadFormQueryString;
    @XmlElement(name = "WebClientEditFormQueryString")
    protected String webClientEditFormQueryString;
    @XmlElement(name = "ConversationId")
    protected ItemIdType conversationId;
    @XmlElement(name = "UniqueBody")
    protected BodyType uniqueBody;
    @XmlElement(name = "StoreEntryId")
    protected byte[] storeEntryId;

    /**
     * Gets the value of the mimeContent property.
     * 
     * @return
     *     possible object is
     *     {@link MimeContentType }
     *     
     */
    public MimeContentType getMimeContent() {
        return mimeContent;
    }

    /**
     * Sets the value of the mimeContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link MimeContentType }
     *     
     */
    public void setMimeContent(MimeContentType value) {
        this.mimeContent = value;
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

    /**
     * Gets the value of the parentFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link FolderIdType }
     *     
     */
    public FolderIdType getParentFolderId() {
        return parentFolderId;
    }

    /**
     * Sets the value of the parentFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderIdType }
     *     
     */
    public void setParentFolderId(FolderIdType value) {
        this.parentFolderId = value;
    }

    /**
     * Gets the value of the itemClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getItemClass() {
        return itemClass;
    }

    /**
     * Sets the value of the itemClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setItemClass(String value) {
        this.itemClass = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
    }

    /**
     * Gets the value of the sensitivity property.
     * 
     * @return
     *     possible object is
     *     {@link SensitivityChoicesType }
     *     
     */
    public SensitivityChoicesType getSensitivity() {
        return sensitivity;
    }

    /**
     * Sets the value of the sensitivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link SensitivityChoicesType }
     *     
     */
    public void setSensitivity(SensitivityChoicesType value) {
        this.sensitivity = value;
    }

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link BodyType }
     *     
     */
    public BodyType getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link BodyType }
     *     
     */
    public void setBody(BodyType value) {
        this.body = value;
    }

    /**
     * Gets the value of the attachments property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAttachmentsType }
     *     
     */
    public NonEmptyArrayOfAttachmentsType getAttachments() {
        return attachments;
    }

    /**
     * Sets the value of the attachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAttachmentsType }
     *     
     */
    public void setAttachments(NonEmptyArrayOfAttachmentsType value) {
        this.attachments = value;
    }

    /**
     * Gets the value of the dateTimeReceived property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTimeReceived() {
        return dateTimeReceived;
    }

    /**
     * Sets the value of the dateTimeReceived property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTimeReceived(XMLGregorianCalendar value) {
        this.dateTimeReceived = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSize(Integer value) {
        this.size = value;
    }

    /**
     * Gets the value of the categories property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getCategories() {
        return categories;
    }

    /**
     * Sets the value of the categories property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setCategories(ArrayOfStringsType value) {
        this.categories = value;
    }

    /**
     * Gets the value of the importance property.
     * 
     * @return
     *     possible object is
     *     {@link ImportanceChoicesType }
     *     
     */
    public ImportanceChoicesType getImportance() {
        return importance;
    }

    /**
     * Sets the value of the importance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportanceChoicesType }
     *     
     */
    public void setImportance(ImportanceChoicesType value) {
        this.importance = value;
    }

    /**
     * Gets the value of the inReplyTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInReplyTo() {
        return inReplyTo;
    }

    /**
     * Sets the value of the inReplyTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInReplyTo(String value) {
        this.inReplyTo = value;
    }

    /**
     * Gets the value of the isSubmitted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsSubmitted() {
        return isSubmitted;
    }

    /**
     * Sets the value of the isSubmitted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsSubmitted(Boolean value) {
        this.isSubmitted = value;
    }

    /**
     * Gets the value of the isDraft property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsDraft() {
        return isDraft;
    }

    /**
     * Sets the value of the isDraft property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsDraft(Boolean value) {
        this.isDraft = value;
    }

    /**
     * Gets the value of the isFromMe property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsFromMe() {
        return isFromMe;
    }

    /**
     * Sets the value of the isFromMe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsFromMe(Boolean value) {
        this.isFromMe = value;
    }

    /**
     * Gets the value of the isResend property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsResend() {
        return isResend;
    }

    /**
     * Sets the value of the isResend property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsResend(Boolean value) {
        this.isResend = value;
    }

    /**
     * Gets the value of the isUnmodified property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsUnmodified() {
        return isUnmodified;
    }

    /**
     * Sets the value of the isUnmodified property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsUnmodified(Boolean value) {
        this.isUnmodified = value;
    }

    /**
     * Gets the value of the internetMessageHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfInternetHeadersType }
     *     
     */
    public NonEmptyArrayOfInternetHeadersType getInternetMessageHeaders() {
        return internetMessageHeaders;
    }

    /**
     * Sets the value of the internetMessageHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfInternetHeadersType }
     *     
     */
    public void setInternetMessageHeaders(NonEmptyArrayOfInternetHeadersType value) {
        this.internetMessageHeaders = value;
    }

    /**
     * Gets the value of the dateTimeSent property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTimeSent() {
        return dateTimeSent;
    }

    /**
     * Sets the value of the dateTimeSent property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTimeSent(XMLGregorianCalendar value) {
        this.dateTimeSent = value;
    }

    /**
     * Gets the value of the dateTimeCreated property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTimeCreated() {
        return dateTimeCreated;
    }

    /**
     * Sets the value of the dateTimeCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTimeCreated(XMLGregorianCalendar value) {
        this.dateTimeCreated = value;
    }

    /**
     * Gets the value of the responseObjects property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfResponseObjectsType }
     *     
     */
    public NonEmptyArrayOfResponseObjectsType getResponseObjects() {
        return responseObjects;
    }

    /**
     * Sets the value of the responseObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfResponseObjectsType }
     *     
     */
    public void setResponseObjects(NonEmptyArrayOfResponseObjectsType value) {
        this.responseObjects = value;
    }

    /**
     * Gets the value of the reminderDueBy property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReminderDueBy() {
        return reminderDueBy;
    }

    /**
     * Sets the value of the reminderDueBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReminderDueBy(XMLGregorianCalendar value) {
        this.reminderDueBy = value;
    }

    /**
     * Gets the value of the reminderIsSet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReminderIsSet() {
        return reminderIsSet;
    }

    /**
     * Sets the value of the reminderIsSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReminderIsSet(Boolean value) {
        this.reminderIsSet = value;
    }

    /**
     * Gets the value of the reminderMinutesBeforeStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReminderMinutesBeforeStart() {
        return reminderMinutesBeforeStart;
    }

    /**
     * Sets the value of the reminderMinutesBeforeStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReminderMinutesBeforeStart(String value) {
        this.reminderMinutesBeforeStart = value;
    }

    /**
     * Gets the value of the displayCc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayCc() {
        return displayCc;
    }

    /**
     * Sets the value of the displayCc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayCc(String value) {
        this.displayCc = value;
    }

    /**
     * Gets the value of the displayTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayTo() {
        return displayTo;
    }

    /**
     * Sets the value of the displayTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayTo(String value) {
        this.displayTo = value;
    }

    /**
     * Gets the value of the hasAttachments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHasAttachments() {
        return hasAttachments;
    }

    /**
     * Sets the value of the hasAttachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasAttachments(Boolean value) {
        this.hasAttachments = value;
    }

    /**
     * Gets the value of the extendedProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extendedProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtendedProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExtendedPropertyType }
     * 
     * 
     */
    public List<ExtendedPropertyType> getExtendedProperty() {
        if (extendedProperty == null) {
            extendedProperty = new ArrayList<ExtendedPropertyType>();
        }
        return this.extendedProperty;
    }

    /**
     * Gets the value of the culture property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCulture() {
        return culture;
    }

    /**
     * Sets the value of the culture property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCulture(String value) {
        this.culture = value;
    }

    /**
     * Gets the value of the effectiveRights property.
     * 
     * @return
     *     possible object is
     *     {@link EffectiveRightsType }
     *     
     */
    public EffectiveRightsType getEffectiveRights() {
        return effectiveRights;
    }

    /**
     * Sets the value of the effectiveRights property.
     * 
     * @param value
     *     allowed object is
     *     {@link EffectiveRightsType }
     *     
     */
    public void setEffectiveRights(EffectiveRightsType value) {
        this.effectiveRights = value;
    }

    /**
     * Gets the value of the lastModifiedName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastModifiedName() {
        return lastModifiedName;
    }

    /**
     * Sets the value of the lastModifiedName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModifiedName(String value) {
        this.lastModifiedName = value;
    }

    /**
     * Gets the value of the lastModifiedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets the value of the lastModifiedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastModifiedTime(XMLGregorianCalendar value) {
        this.lastModifiedTime = value;
    }

    /**
     * Gets the value of the isAssociated property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAssociated() {
        return isAssociated;
    }

    /**
     * Sets the value of the isAssociated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAssociated(Boolean value) {
        this.isAssociated = value;
    }

    /**
     * Gets the value of the webClientReadFormQueryString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebClientReadFormQueryString() {
        return webClientReadFormQueryString;
    }

    /**
     * Sets the value of the webClientReadFormQueryString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebClientReadFormQueryString(String value) {
        this.webClientReadFormQueryString = value;
    }

    /**
     * Gets the value of the webClientEditFormQueryString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebClientEditFormQueryString() {
        return webClientEditFormQueryString;
    }

    /**
     * Sets the value of the webClientEditFormQueryString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebClientEditFormQueryString(String value) {
        this.webClientEditFormQueryString = value;
    }

    /**
     * Gets the value of the conversationId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getConversationId() {
        return conversationId;
    }

    /**
     * Sets the value of the conversationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setConversationId(ItemIdType value) {
        this.conversationId = value;
    }

    /**
     * Gets the value of the uniqueBody property.
     * 
     * @return
     *     possible object is
     *     {@link BodyType }
     *     
     */
    public BodyType getUniqueBody() {
        return uniqueBody;
    }

    /**
     * Sets the value of the uniqueBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link BodyType }
     *     
     */
    public void setUniqueBody(BodyType value) {
        this.uniqueBody = value;
    }

    /**
     * Gets the value of the storeEntryId property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getStoreEntryId() {
        return storeEntryId;
    }

    /**
     * Sets the value of the storeEntryId property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setStoreEntryId(byte[] value) {
        this.storeEntryId = ((byte[]) value);
    }

}
