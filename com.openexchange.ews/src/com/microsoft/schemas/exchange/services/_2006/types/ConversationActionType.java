
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ConversationActionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConversationActionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Action" type="{http://schemas.microsoft.com/exchange/services/2006/types}ConversationActionTypeType"/>
 *         &lt;element name="ConversationId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType"/>
 *         &lt;element name="ContextFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType" minOccurs="0"/>
 *         &lt;element name="ConversationLastSyncTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ProcessRightAway" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="DestinationFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType" minOccurs="0"/>
 *         &lt;element name="Categories" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="EnableAlwaysDelete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsRead" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="DeleteType" type="{http://schemas.microsoft.com/exchange/services/2006/types}DisposalType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConversationActionType", propOrder = {
    "action",
    "conversationId",
    "contextFolderId",
    "conversationLastSyncTime",
    "processRightAway",
    "destinationFolderId",
    "categories",
    "enableAlwaysDelete",
    "isRead",
    "deleteType"
})
public class ConversationActionType {

    @XmlElement(name = "Action", required = true)
    protected ConversationActionTypeType action;
    @XmlElement(name = "ConversationId", required = true)
    protected ItemIdType conversationId;
    @XmlElement(name = "ContextFolderId")
    protected TargetFolderIdType contextFolderId;
    @XmlElement(name = "ConversationLastSyncTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar conversationLastSyncTime;
    @XmlElement(name = "ProcessRightAway")
    protected Boolean processRightAway;
    @XmlElement(name = "DestinationFolderId")
    protected TargetFolderIdType destinationFolderId;
    @XmlElement(name = "Categories")
    protected ArrayOfStringsType categories;
    @XmlElement(name = "EnableAlwaysDelete")
    protected Boolean enableAlwaysDelete;
    @XmlElement(name = "IsRead")
    protected Boolean isRead;
    @XmlElement(name = "DeleteType")
    protected DisposalType deleteType;

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link ConversationActionTypeType }
     *     
     */
    public ConversationActionTypeType getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConversationActionTypeType }
     *     
     */
    public void setAction(ConversationActionTypeType value) {
        this.action = value;
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
     * Gets the value of the contextFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getContextFolderId() {
        return contextFolderId;
    }

    /**
     * Sets the value of the contextFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setContextFolderId(TargetFolderIdType value) {
        this.contextFolderId = value;
    }

    /**
     * Gets the value of the conversationLastSyncTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getConversationLastSyncTime() {
        return conversationLastSyncTime;
    }

    /**
     * Sets the value of the conversationLastSyncTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setConversationLastSyncTime(XMLGregorianCalendar value) {
        this.conversationLastSyncTime = value;
    }

    /**
     * Gets the value of the processRightAway property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isProcessRightAway() {
        return processRightAway;
    }

    /**
     * Sets the value of the processRightAway property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProcessRightAway(Boolean value) {
        this.processRightAway = value;
    }

    /**
     * Gets the value of the destinationFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getDestinationFolderId() {
        return destinationFolderId;
    }

    /**
     * Sets the value of the destinationFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setDestinationFolderId(TargetFolderIdType value) {
        this.destinationFolderId = value;
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
     * Gets the value of the enableAlwaysDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEnableAlwaysDelete() {
        return enableAlwaysDelete;
    }

    /**
     * Sets the value of the enableAlwaysDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableAlwaysDelete(Boolean value) {
        this.enableAlwaysDelete = value;
    }

    /**
     * Gets the value of the isRead property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsRead() {
        return isRead;
    }

    /**
     * Sets the value of the isRead property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRead(Boolean value) {
        this.isRead = value;
    }

    /**
     * Gets the value of the deleteType property.
     * 
     * @return
     *     possible object is
     *     {@link DisposalType }
     *     
     */
    public DisposalType getDeleteType() {
        return deleteType;
    }

    /**
     * Sets the value of the deleteType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisposalType }
     *     
     */
    public void setDeleteType(DisposalType value) {
        this.deleteType = value;
    }

}
