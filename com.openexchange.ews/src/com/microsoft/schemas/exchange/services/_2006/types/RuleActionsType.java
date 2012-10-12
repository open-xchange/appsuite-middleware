
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Rule actions
 * 
 * <p>Java class for RuleActionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuleActionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AssignCategories" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="CopyToFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType" minOccurs="0"/>
 *         &lt;element name="Delete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ForwardAsAttachmentToRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *         &lt;element name="ForwardToRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *         &lt;element name="MarkImportance" type="{http://schemas.microsoft.com/exchange/services/2006/types}ImportanceChoicesType" minOccurs="0"/>
 *         &lt;element name="MarkAsRead" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="MoveToFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType" minOccurs="0"/>
 *         &lt;element name="PermanentDelete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="RedirectToRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *         &lt;element name="SendSMSAlertToRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *         &lt;element name="ServerReplyWithMessage" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *         &lt;element name="StopProcessingRules" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuleActionsType", propOrder = {
    "assignCategories",
    "copyToFolder",
    "delete",
    "forwardAsAttachmentToRecipients",
    "forwardToRecipients",
    "markImportance",
    "markAsRead",
    "moveToFolder",
    "permanentDelete",
    "redirectToRecipients",
    "sendSMSAlertToRecipients",
    "serverReplyWithMessage",
    "stopProcessingRules"
})
public class RuleActionsType {

    @XmlElement(name = "AssignCategories")
    protected ArrayOfStringsType assignCategories;
    @XmlElement(name = "CopyToFolder")
    protected TargetFolderIdType copyToFolder;
    @XmlElement(name = "Delete")
    protected Boolean delete;
    @XmlElement(name = "ForwardAsAttachmentToRecipients")
    protected ArrayOfEmailAddressesType forwardAsAttachmentToRecipients;
    @XmlElement(name = "ForwardToRecipients")
    protected ArrayOfEmailAddressesType forwardToRecipients;
    @XmlElement(name = "MarkImportance")
    protected ImportanceChoicesType markImportance;
    @XmlElement(name = "MarkAsRead")
    protected Boolean markAsRead;
    @XmlElement(name = "MoveToFolder")
    protected TargetFolderIdType moveToFolder;
    @XmlElement(name = "PermanentDelete")
    protected Boolean permanentDelete;
    @XmlElement(name = "RedirectToRecipients")
    protected ArrayOfEmailAddressesType redirectToRecipients;
    @XmlElement(name = "SendSMSAlertToRecipients")
    protected ArrayOfEmailAddressesType sendSMSAlertToRecipients;
    @XmlElement(name = "ServerReplyWithMessage")
    protected ItemIdType serverReplyWithMessage;
    @XmlElement(name = "StopProcessingRules")
    protected Boolean stopProcessingRules;

    /**
     * Gets the value of the assignCategories property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getAssignCategories() {
        return assignCategories;
    }

    /**
     * Sets the value of the assignCategories property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setAssignCategories(ArrayOfStringsType value) {
        this.assignCategories = value;
    }

    /**
     * Gets the value of the copyToFolder property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getCopyToFolder() {
        return copyToFolder;
    }

    /**
     * Sets the value of the copyToFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setCopyToFolder(TargetFolderIdType value) {
        this.copyToFolder = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDelete() {
        return delete;
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDelete(Boolean value) {
        this.delete = value;
    }

    /**
     * Gets the value of the forwardAsAttachmentToRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getForwardAsAttachmentToRecipients() {
        return forwardAsAttachmentToRecipients;
    }

    /**
     * Sets the value of the forwardAsAttachmentToRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setForwardAsAttachmentToRecipients(ArrayOfEmailAddressesType value) {
        this.forwardAsAttachmentToRecipients = value;
    }

    /**
     * Gets the value of the forwardToRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getForwardToRecipients() {
        return forwardToRecipients;
    }

    /**
     * Sets the value of the forwardToRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setForwardToRecipients(ArrayOfEmailAddressesType value) {
        this.forwardToRecipients = value;
    }

    /**
     * Gets the value of the markImportance property.
     * 
     * @return
     *     possible object is
     *     {@link ImportanceChoicesType }
     *     
     */
    public ImportanceChoicesType getMarkImportance() {
        return markImportance;
    }

    /**
     * Sets the value of the markImportance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportanceChoicesType }
     *     
     */
    public void setMarkImportance(ImportanceChoicesType value) {
        this.markImportance = value;
    }

    /**
     * Gets the value of the markAsRead property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMarkAsRead() {
        return markAsRead;
    }

    /**
     * Sets the value of the markAsRead property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMarkAsRead(Boolean value) {
        this.markAsRead = value;
    }

    /**
     * Gets the value of the moveToFolder property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getMoveToFolder() {
        return moveToFolder;
    }

    /**
     * Sets the value of the moveToFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setMoveToFolder(TargetFolderIdType value) {
        this.moveToFolder = value;
    }

    /**
     * Gets the value of the permanentDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPermanentDelete() {
        return permanentDelete;
    }

    /**
     * Sets the value of the permanentDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPermanentDelete(Boolean value) {
        this.permanentDelete = value;
    }

    /**
     * Gets the value of the redirectToRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getRedirectToRecipients() {
        return redirectToRecipients;
    }

    /**
     * Sets the value of the redirectToRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setRedirectToRecipients(ArrayOfEmailAddressesType value) {
        this.redirectToRecipients = value;
    }

    /**
     * Gets the value of the sendSMSAlertToRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getSendSMSAlertToRecipients() {
        return sendSMSAlertToRecipients;
    }

    /**
     * Sets the value of the sendSMSAlertToRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setSendSMSAlertToRecipients(ArrayOfEmailAddressesType value) {
        this.sendSMSAlertToRecipients = value;
    }

    /**
     * Gets the value of the serverReplyWithMessage property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getServerReplyWithMessage() {
        return serverReplyWithMessage;
    }

    /**
     * Sets the value of the serverReplyWithMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setServerReplyWithMessage(ItemIdType value) {
        this.serverReplyWithMessage = value;
    }

    /**
     * Gets the value of the stopProcessingRules property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStopProcessingRules() {
        return stopProcessingRules;
    }

    /**
     * Sets the value of the stopProcessingRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStopProcessingRules(Boolean value) {
        this.stopProcessingRules = value;
    }

}
