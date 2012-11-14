
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ConversationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConversationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ConversationId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *         &lt;element name="ConversationTopic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UniqueRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="GlobalUniqueRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="UniqueUnreadSenders" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="GlobalUniqueUnreadSenders" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="UniqueSenders" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="GlobalUniqueSenders" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="LastDeliveryTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="GlobalLastDeliveryTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Categories" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="GlobalCategories" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="FlagStatus" type="{http://schemas.microsoft.com/exchange/services/2006/types}FlagStatusType" minOccurs="0"/>
 *         &lt;element name="GlobalFlagStatus" type="{http://schemas.microsoft.com/exchange/services/2006/types}FlagStatusType" minOccurs="0"/>
 *         &lt;element name="HasAttachments" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="GlobalHasAttachments" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="MessageCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="GlobalMessageCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="UnreadCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="GlobalUnreadCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Size" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="GlobalSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ItemClasses" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfItemClassType" minOccurs="0"/>
 *         &lt;element name="GlobalItemClasses" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfItemClassType" minOccurs="0"/>
 *         &lt;element name="Importance" type="{http://schemas.microsoft.com/exchange/services/2006/types}ImportanceChoicesType" minOccurs="0"/>
 *         &lt;element name="GlobalImportance" type="{http://schemas.microsoft.com/exchange/services/2006/types}ImportanceChoicesType" minOccurs="0"/>
 *         &lt;element name="ItemIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseItemIdsType" minOccurs="0"/>
 *         &lt;element name="GlobalItemIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseItemIdsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConversationType", propOrder = {
    "conversationId",
    "conversationTopic",
    "uniqueRecipients",
    "globalUniqueRecipients",
    "uniqueUnreadSenders",
    "globalUniqueUnreadSenders",
    "uniqueSenders",
    "globalUniqueSenders",
    "lastDeliveryTime",
    "globalLastDeliveryTime",
    "categories",
    "globalCategories",
    "flagStatus",
    "globalFlagStatus",
    "hasAttachments",
    "globalHasAttachments",
    "messageCount",
    "globalMessageCount",
    "unreadCount",
    "globalUnreadCount",
    "size",
    "globalSize",
    "itemClasses",
    "globalItemClasses",
    "importance",
    "globalImportance",
    "itemIds",
    "globalItemIds"
})
public class ConversationType {

    @XmlElement(name = "ConversationId")
    protected ItemIdType conversationId;
    @XmlElement(name = "ConversationTopic")
    protected String conversationTopic;
    @XmlElement(name = "UniqueRecipients")
    protected ArrayOfStringsType uniqueRecipients;
    @XmlElement(name = "GlobalUniqueRecipients")
    protected ArrayOfStringsType globalUniqueRecipients;
    @XmlElement(name = "UniqueUnreadSenders")
    protected ArrayOfStringsType uniqueUnreadSenders;
    @XmlElement(name = "GlobalUniqueUnreadSenders")
    protected ArrayOfStringsType globalUniqueUnreadSenders;
    @XmlElement(name = "UniqueSenders")
    protected ArrayOfStringsType uniqueSenders;
    @XmlElement(name = "GlobalUniqueSenders")
    protected ArrayOfStringsType globalUniqueSenders;
    @XmlElement(name = "LastDeliveryTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastDeliveryTime;
    @XmlElement(name = "GlobalLastDeliveryTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar globalLastDeliveryTime;
    @XmlElement(name = "Categories")
    protected ArrayOfStringsType categories;
    @XmlElement(name = "GlobalCategories")
    protected ArrayOfStringsType globalCategories;
    @XmlElement(name = "FlagStatus")
    protected FlagStatusType flagStatus;
    @XmlElement(name = "GlobalFlagStatus")
    protected FlagStatusType globalFlagStatus;
    @XmlElement(name = "HasAttachments")
    protected Boolean hasAttachments;
    @XmlElement(name = "GlobalHasAttachments")
    protected Boolean globalHasAttachments;
    @XmlElement(name = "MessageCount")
    protected Integer messageCount;
    @XmlElement(name = "GlobalMessageCount")
    protected Integer globalMessageCount;
    @XmlElement(name = "UnreadCount")
    protected Integer unreadCount;
    @XmlElement(name = "GlobalUnreadCount")
    protected Integer globalUnreadCount;
    @XmlElement(name = "Size")
    protected Integer size;
    @XmlElement(name = "GlobalSize")
    protected Integer globalSize;
    @XmlElement(name = "ItemClasses")
    protected ArrayOfItemClassType itemClasses;
    @XmlElement(name = "GlobalItemClasses")
    protected ArrayOfItemClassType globalItemClasses;
    @XmlElement(name = "Importance")
    protected ImportanceChoicesType importance;
    @XmlElement(name = "GlobalImportance")
    protected ImportanceChoicesType globalImportance;
    @XmlElement(name = "ItemIds")
    protected NonEmptyArrayOfBaseItemIdsType itemIds;
    @XmlElement(name = "GlobalItemIds")
    protected NonEmptyArrayOfBaseItemIdsType globalItemIds;

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
     * Gets the value of the conversationTopic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConversationTopic() {
        return conversationTopic;
    }

    /**
     * Sets the value of the conversationTopic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConversationTopic(String value) {
        this.conversationTopic = value;
    }

    /**
     * Gets the value of the uniqueRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getUniqueRecipients() {
        return uniqueRecipients;
    }

    /**
     * Sets the value of the uniqueRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setUniqueRecipients(ArrayOfStringsType value) {
        this.uniqueRecipients = value;
    }

    /**
     * Gets the value of the globalUniqueRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getGlobalUniqueRecipients() {
        return globalUniqueRecipients;
    }

    /**
     * Sets the value of the globalUniqueRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setGlobalUniqueRecipients(ArrayOfStringsType value) {
        this.globalUniqueRecipients = value;
    }

    /**
     * Gets the value of the uniqueUnreadSenders property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getUniqueUnreadSenders() {
        return uniqueUnreadSenders;
    }

    /**
     * Sets the value of the uniqueUnreadSenders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setUniqueUnreadSenders(ArrayOfStringsType value) {
        this.uniqueUnreadSenders = value;
    }

    /**
     * Gets the value of the globalUniqueUnreadSenders property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getGlobalUniqueUnreadSenders() {
        return globalUniqueUnreadSenders;
    }

    /**
     * Sets the value of the globalUniqueUnreadSenders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setGlobalUniqueUnreadSenders(ArrayOfStringsType value) {
        this.globalUniqueUnreadSenders = value;
    }

    /**
     * Gets the value of the uniqueSenders property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getUniqueSenders() {
        return uniqueSenders;
    }

    /**
     * Sets the value of the uniqueSenders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setUniqueSenders(ArrayOfStringsType value) {
        this.uniqueSenders = value;
    }

    /**
     * Gets the value of the globalUniqueSenders property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getGlobalUniqueSenders() {
        return globalUniqueSenders;
    }

    /**
     * Sets the value of the globalUniqueSenders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setGlobalUniqueSenders(ArrayOfStringsType value) {
        this.globalUniqueSenders = value;
    }

    /**
     * Gets the value of the lastDeliveryTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastDeliveryTime() {
        return lastDeliveryTime;
    }

    /**
     * Sets the value of the lastDeliveryTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastDeliveryTime(XMLGregorianCalendar value) {
        this.lastDeliveryTime = value;
    }

    /**
     * Gets the value of the globalLastDeliveryTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getGlobalLastDeliveryTime() {
        return globalLastDeliveryTime;
    }

    /**
     * Sets the value of the globalLastDeliveryTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setGlobalLastDeliveryTime(XMLGregorianCalendar value) {
        this.globalLastDeliveryTime = value;
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
     * Gets the value of the globalCategories property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getGlobalCategories() {
        return globalCategories;
    }

    /**
     * Sets the value of the globalCategories property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setGlobalCategories(ArrayOfStringsType value) {
        this.globalCategories = value;
    }

    /**
     * Gets the value of the flagStatus property.
     * 
     * @return
     *     possible object is
     *     {@link FlagStatusType }
     *     
     */
    public FlagStatusType getFlagStatus() {
        return flagStatus;
    }

    /**
     * Sets the value of the flagStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlagStatusType }
     *     
     */
    public void setFlagStatus(FlagStatusType value) {
        this.flagStatus = value;
    }

    /**
     * Gets the value of the globalFlagStatus property.
     * 
     * @return
     *     possible object is
     *     {@link FlagStatusType }
     *     
     */
    public FlagStatusType getGlobalFlagStatus() {
        return globalFlagStatus;
    }

    /**
     * Sets the value of the globalFlagStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlagStatusType }
     *     
     */
    public void setGlobalFlagStatus(FlagStatusType value) {
        this.globalFlagStatus = value;
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
     * Gets the value of the globalHasAttachments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isGlobalHasAttachments() {
        return globalHasAttachments;
    }

    /**
     * Sets the value of the globalHasAttachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGlobalHasAttachments(Boolean value) {
        this.globalHasAttachments = value;
    }

    /**
     * Gets the value of the messageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMessageCount() {
        return messageCount;
    }

    /**
     * Sets the value of the messageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMessageCount(Integer value) {
        this.messageCount = value;
    }

    /**
     * Gets the value of the globalMessageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGlobalMessageCount() {
        return globalMessageCount;
    }

    /**
     * Sets the value of the globalMessageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGlobalMessageCount(Integer value) {
        this.globalMessageCount = value;
    }

    /**
     * Gets the value of the unreadCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getUnreadCount() {
        return unreadCount;
    }

    /**
     * Sets the value of the unreadCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setUnreadCount(Integer value) {
        this.unreadCount = value;
    }

    /**
     * Gets the value of the globalUnreadCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGlobalUnreadCount() {
        return globalUnreadCount;
    }

    /**
     * Sets the value of the globalUnreadCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGlobalUnreadCount(Integer value) {
        this.globalUnreadCount = value;
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
     * Gets the value of the globalSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGlobalSize() {
        return globalSize;
    }

    /**
     * Sets the value of the globalSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGlobalSize(Integer value) {
        this.globalSize = value;
    }

    /**
     * Gets the value of the itemClasses property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfItemClassType }
     *     
     */
    public ArrayOfItemClassType getItemClasses() {
        return itemClasses;
    }

    /**
     * Sets the value of the itemClasses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfItemClassType }
     *     
     */
    public void setItemClasses(ArrayOfItemClassType value) {
        this.itemClasses = value;
    }

    /**
     * Gets the value of the globalItemClasses property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfItemClassType }
     *     
     */
    public ArrayOfItemClassType getGlobalItemClasses() {
        return globalItemClasses;
    }

    /**
     * Sets the value of the globalItemClasses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfItemClassType }
     *     
     */
    public void setGlobalItemClasses(ArrayOfItemClassType value) {
        this.globalItemClasses = value;
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
     * Gets the value of the globalImportance property.
     * 
     * @return
     *     possible object is
     *     {@link ImportanceChoicesType }
     *     
     */
    public ImportanceChoicesType getGlobalImportance() {
        return globalImportance;
    }

    /**
     * Sets the value of the globalImportance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportanceChoicesType }
     *     
     */
    public void setGlobalImportance(ImportanceChoicesType value) {
        this.globalImportance = value;
    }

    /**
     * Gets the value of the itemIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseItemIdsType }
     *     
     */
    public NonEmptyArrayOfBaseItemIdsType getItemIds() {
        return itemIds;
    }

    /**
     * Sets the value of the itemIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseItemIdsType }
     *     
     */
    public void setItemIds(NonEmptyArrayOfBaseItemIdsType value) {
        this.itemIds = value;
    }

    /**
     * Gets the value of the globalItemIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseItemIdsType }
     *     
     */
    public NonEmptyArrayOfBaseItemIdsType getGlobalItemIds() {
        return globalItemIds;
    }

    /**
     * Sets the value of the globalItemIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseItemIdsType }
     *     
     */
    public void setGlobalItemIds(NonEmptyArrayOfBaseItemIdsType value) {
        this.globalItemIds = value;
    }

}
