
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Rule predicates, used as rule conditions or exceptions
 * 
 * <p>Java class for RulePredicatesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RulePredicatesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Categories" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="ContainsBodyStrings" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="ContainsHeaderStrings" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="ContainsRecipientStrings" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="ContainsSenderStrings" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="ContainsSubjectOrBodyStrings" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="ContainsSubjectStrings" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="FlaggedForAction" type="{http://schemas.microsoft.com/exchange/services/2006/types}FlaggedForActionType" minOccurs="0"/>
 *         &lt;element name="FromAddresses" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *         &lt;element name="FromConnectedAccounts" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="HasAttachments" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Importance" type="{http://schemas.microsoft.com/exchange/services/2006/types}ImportanceChoicesType" minOccurs="0"/>
 *         &lt;element name="IsApprovalRequest" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsAutomaticForward" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsAutomaticReply" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsEncrypted" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsMeetingRequest" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsMeetingResponse" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsNDR" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsPermissionControlled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsReadReceipt" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsSigned" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsVoicemail" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ItemClasses" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="MessageClassifications" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="NotSentToMe" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="SentCcMe" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="SentOnlyToMe" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="SentToAddresses" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *         &lt;element name="SentToMe" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="SentToOrCcMe" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Sensitivity" type="{http://schemas.microsoft.com/exchange/services/2006/types}SensitivityChoicesType" minOccurs="0"/>
 *         &lt;element name="WithinDateRange" type="{http://schemas.microsoft.com/exchange/services/2006/types}RulePredicateDateRangeType" minOccurs="0"/>
 *         &lt;element name="WithinSizeRange" type="{http://schemas.microsoft.com/exchange/services/2006/types}RulePredicateSizeRangeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RulePredicatesType", propOrder = {
    "categories",
    "containsBodyStrings",
    "containsHeaderStrings",
    "containsRecipientStrings",
    "containsSenderStrings",
    "containsSubjectOrBodyStrings",
    "containsSubjectStrings",
    "flaggedForAction",
    "fromAddresses",
    "fromConnectedAccounts",
    "hasAttachments",
    "importance",
    "isApprovalRequest",
    "isAutomaticForward",
    "isAutomaticReply",
    "isEncrypted",
    "isMeetingRequest",
    "isMeetingResponse",
    "isNDR",
    "isPermissionControlled",
    "isReadReceipt",
    "isSigned",
    "isVoicemail",
    "itemClasses",
    "messageClassifications",
    "notSentToMe",
    "sentCcMe",
    "sentOnlyToMe",
    "sentToAddresses",
    "sentToMe",
    "sentToOrCcMe",
    "sensitivity",
    "withinDateRange",
    "withinSizeRange"
})
public class RulePredicatesType {

    @XmlElement(name = "Categories")
    protected ArrayOfStringsType categories;
    @XmlElement(name = "ContainsBodyStrings")
    protected ArrayOfStringsType containsBodyStrings;
    @XmlElement(name = "ContainsHeaderStrings")
    protected ArrayOfStringsType containsHeaderStrings;
    @XmlElement(name = "ContainsRecipientStrings")
    protected ArrayOfStringsType containsRecipientStrings;
    @XmlElement(name = "ContainsSenderStrings")
    protected ArrayOfStringsType containsSenderStrings;
    @XmlElement(name = "ContainsSubjectOrBodyStrings")
    protected ArrayOfStringsType containsSubjectOrBodyStrings;
    @XmlElement(name = "ContainsSubjectStrings")
    protected ArrayOfStringsType containsSubjectStrings;
    @XmlElement(name = "FlaggedForAction")
    protected FlaggedForActionType flaggedForAction;
    @XmlElement(name = "FromAddresses")
    protected ArrayOfEmailAddressesType fromAddresses;
    @XmlElement(name = "FromConnectedAccounts")
    protected ArrayOfStringsType fromConnectedAccounts;
    @XmlElement(name = "HasAttachments")
    protected Boolean hasAttachments;
    @XmlElement(name = "Importance")
    protected ImportanceChoicesType importance;
    @XmlElement(name = "IsApprovalRequest")
    protected Boolean isApprovalRequest;
    @XmlElement(name = "IsAutomaticForward")
    protected Boolean isAutomaticForward;
    @XmlElement(name = "IsAutomaticReply")
    protected Boolean isAutomaticReply;
    @XmlElement(name = "IsEncrypted")
    protected Boolean isEncrypted;
    @XmlElement(name = "IsMeetingRequest")
    protected Boolean isMeetingRequest;
    @XmlElement(name = "IsMeetingResponse")
    protected Boolean isMeetingResponse;
    @XmlElement(name = "IsNDR")
    protected Boolean isNDR;
    @XmlElement(name = "IsPermissionControlled")
    protected Boolean isPermissionControlled;
    @XmlElement(name = "IsReadReceipt")
    protected Boolean isReadReceipt;
    @XmlElement(name = "IsSigned")
    protected Boolean isSigned;
    @XmlElement(name = "IsVoicemail")
    protected Boolean isVoicemail;
    @XmlElement(name = "ItemClasses")
    protected ArrayOfStringsType itemClasses;
    @XmlElement(name = "MessageClassifications")
    protected ArrayOfStringsType messageClassifications;
    @XmlElement(name = "NotSentToMe")
    protected Boolean notSentToMe;
    @XmlElement(name = "SentCcMe")
    protected Boolean sentCcMe;
    @XmlElement(name = "SentOnlyToMe")
    protected Boolean sentOnlyToMe;
    @XmlElement(name = "SentToAddresses")
    protected ArrayOfEmailAddressesType sentToAddresses;
    @XmlElement(name = "SentToMe")
    protected Boolean sentToMe;
    @XmlElement(name = "SentToOrCcMe")
    protected Boolean sentToOrCcMe;
    @XmlElement(name = "Sensitivity")
    protected SensitivityChoicesType sensitivity;
    @XmlElement(name = "WithinDateRange")
    protected RulePredicateDateRangeType withinDateRange;
    @XmlElement(name = "WithinSizeRange")
    protected RulePredicateSizeRangeType withinSizeRange;

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
     * Gets the value of the containsBodyStrings property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getContainsBodyStrings() {
        return containsBodyStrings;
    }

    /**
     * Sets the value of the containsBodyStrings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setContainsBodyStrings(ArrayOfStringsType value) {
        this.containsBodyStrings = value;
    }

    /**
     * Gets the value of the containsHeaderStrings property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getContainsHeaderStrings() {
        return containsHeaderStrings;
    }

    /**
     * Sets the value of the containsHeaderStrings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setContainsHeaderStrings(ArrayOfStringsType value) {
        this.containsHeaderStrings = value;
    }

    /**
     * Gets the value of the containsRecipientStrings property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getContainsRecipientStrings() {
        return containsRecipientStrings;
    }

    /**
     * Sets the value of the containsRecipientStrings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setContainsRecipientStrings(ArrayOfStringsType value) {
        this.containsRecipientStrings = value;
    }

    /**
     * Gets the value of the containsSenderStrings property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getContainsSenderStrings() {
        return containsSenderStrings;
    }

    /**
     * Sets the value of the containsSenderStrings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setContainsSenderStrings(ArrayOfStringsType value) {
        this.containsSenderStrings = value;
    }

    /**
     * Gets the value of the containsSubjectOrBodyStrings property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getContainsSubjectOrBodyStrings() {
        return containsSubjectOrBodyStrings;
    }

    /**
     * Sets the value of the containsSubjectOrBodyStrings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setContainsSubjectOrBodyStrings(ArrayOfStringsType value) {
        this.containsSubjectOrBodyStrings = value;
    }

    /**
     * Gets the value of the containsSubjectStrings property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getContainsSubjectStrings() {
        return containsSubjectStrings;
    }

    /**
     * Sets the value of the containsSubjectStrings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setContainsSubjectStrings(ArrayOfStringsType value) {
        this.containsSubjectStrings = value;
    }

    /**
     * Gets the value of the flaggedForAction property.
     * 
     * @return
     *     possible object is
     *     {@link FlaggedForActionType }
     *     
     */
    public FlaggedForActionType getFlaggedForAction() {
        return flaggedForAction;
    }

    /**
     * Sets the value of the flaggedForAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlaggedForActionType }
     *     
     */
    public void setFlaggedForAction(FlaggedForActionType value) {
        this.flaggedForAction = value;
    }

    /**
     * Gets the value of the fromAddresses property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getFromAddresses() {
        return fromAddresses;
    }

    /**
     * Sets the value of the fromAddresses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setFromAddresses(ArrayOfEmailAddressesType value) {
        this.fromAddresses = value;
    }

    /**
     * Gets the value of the fromConnectedAccounts property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getFromConnectedAccounts() {
        return fromConnectedAccounts;
    }

    /**
     * Sets the value of the fromConnectedAccounts property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setFromConnectedAccounts(ArrayOfStringsType value) {
        this.fromConnectedAccounts = value;
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
     * Gets the value of the isApprovalRequest property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsApprovalRequest() {
        return isApprovalRequest;
    }

    /**
     * Sets the value of the isApprovalRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsApprovalRequest(Boolean value) {
        this.isApprovalRequest = value;
    }

    /**
     * Gets the value of the isAutomaticForward property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAutomaticForward() {
        return isAutomaticForward;
    }

    /**
     * Sets the value of the isAutomaticForward property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAutomaticForward(Boolean value) {
        this.isAutomaticForward = value;
    }

    /**
     * Gets the value of the isAutomaticReply property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAutomaticReply() {
        return isAutomaticReply;
    }

    /**
     * Sets the value of the isAutomaticReply property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAutomaticReply(Boolean value) {
        this.isAutomaticReply = value;
    }

    /**
     * Gets the value of the isEncrypted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsEncrypted() {
        return isEncrypted;
    }

    /**
     * Sets the value of the isEncrypted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsEncrypted(Boolean value) {
        this.isEncrypted = value;
    }

    /**
     * Gets the value of the isMeetingRequest property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsMeetingRequest() {
        return isMeetingRequest;
    }

    /**
     * Sets the value of the isMeetingRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsMeetingRequest(Boolean value) {
        this.isMeetingRequest = value;
    }

    /**
     * Gets the value of the isMeetingResponse property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsMeetingResponse() {
        return isMeetingResponse;
    }

    /**
     * Sets the value of the isMeetingResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsMeetingResponse(Boolean value) {
        this.isMeetingResponse = value;
    }

    /**
     * Gets the value of the isNDR property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsNDR() {
        return isNDR;
    }

    /**
     * Sets the value of the isNDR property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsNDR(Boolean value) {
        this.isNDR = value;
    }

    /**
     * Gets the value of the isPermissionControlled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsPermissionControlled() {
        return isPermissionControlled;
    }

    /**
     * Sets the value of the isPermissionControlled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsPermissionControlled(Boolean value) {
        this.isPermissionControlled = value;
    }

    /**
     * Gets the value of the isReadReceipt property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsReadReceipt() {
        return isReadReceipt;
    }

    /**
     * Sets the value of the isReadReceipt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsReadReceipt(Boolean value) {
        this.isReadReceipt = value;
    }

    /**
     * Gets the value of the isSigned property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsSigned() {
        return isSigned;
    }

    /**
     * Sets the value of the isSigned property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsSigned(Boolean value) {
        this.isSigned = value;
    }

    /**
     * Gets the value of the isVoicemail property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsVoicemail() {
        return isVoicemail;
    }

    /**
     * Sets the value of the isVoicemail property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsVoicemail(Boolean value) {
        this.isVoicemail = value;
    }

    /**
     * Gets the value of the itemClasses property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getItemClasses() {
        return itemClasses;
    }

    /**
     * Sets the value of the itemClasses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setItemClasses(ArrayOfStringsType value) {
        this.itemClasses = value;
    }

    /**
     * Gets the value of the messageClassifications property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getMessageClassifications() {
        return messageClassifications;
    }

    /**
     * Sets the value of the messageClassifications property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setMessageClassifications(ArrayOfStringsType value) {
        this.messageClassifications = value;
    }

    /**
     * Gets the value of the notSentToMe property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNotSentToMe() {
        return notSentToMe;
    }

    /**
     * Sets the value of the notSentToMe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNotSentToMe(Boolean value) {
        this.notSentToMe = value;
    }

    /**
     * Gets the value of the sentCcMe property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSentCcMe() {
        return sentCcMe;
    }

    /**
     * Sets the value of the sentCcMe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSentCcMe(Boolean value) {
        this.sentCcMe = value;
    }

    /**
     * Gets the value of the sentOnlyToMe property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSentOnlyToMe() {
        return sentOnlyToMe;
    }

    /**
     * Sets the value of the sentOnlyToMe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSentOnlyToMe(Boolean value) {
        this.sentOnlyToMe = value;
    }

    /**
     * Gets the value of the sentToAddresses property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getSentToAddresses() {
        return sentToAddresses;
    }

    /**
     * Sets the value of the sentToAddresses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setSentToAddresses(ArrayOfEmailAddressesType value) {
        this.sentToAddresses = value;
    }

    /**
     * Gets the value of the sentToMe property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSentToMe() {
        return sentToMe;
    }

    /**
     * Sets the value of the sentToMe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSentToMe(Boolean value) {
        this.sentToMe = value;
    }

    /**
     * Gets the value of the sentToOrCcMe property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSentToOrCcMe() {
        return sentToOrCcMe;
    }

    /**
     * Sets the value of the sentToOrCcMe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSentToOrCcMe(Boolean value) {
        this.sentToOrCcMe = value;
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
     * Gets the value of the withinDateRange property.
     * 
     * @return
     *     possible object is
     *     {@link RulePredicateDateRangeType }
     *     
     */
    public RulePredicateDateRangeType getWithinDateRange() {
        return withinDateRange;
    }

    /**
     * Sets the value of the withinDateRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link RulePredicateDateRangeType }
     *     
     */
    public void setWithinDateRange(RulePredicateDateRangeType value) {
        this.withinDateRange = value;
    }

    /**
     * Gets the value of the withinSizeRange property.
     * 
     * @return
     *     possible object is
     *     {@link RulePredicateSizeRangeType }
     *     
     */
    public RulePredicateSizeRangeType getWithinSizeRange() {
        return withinSizeRange;
    }

    /**
     * Sets the value of the withinSizeRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link RulePredicateSizeRangeType }
     *     
     */
    public void setWithinSizeRange(RulePredicateSizeRangeType value) {
        this.withinSizeRange = value;
    }

}
