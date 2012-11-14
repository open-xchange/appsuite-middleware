
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BaseResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseResponseMessageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ResponseMessages" type="{http://schemas.microsoft.com/exchange/services/2006/messages}ArrayOfResponseMessagesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseResponseMessageType", propOrder = {
    "responseMessages"
})
@XmlSeeAlso({
    DeleteAttachmentResponseType.class,
    MoveItemResponseType.class,
    SendItemResponseType.class,
    ApplyConversationActionResponseType.class,
    GetUserConfigurationResponseType.class,
    UpdateItemResponseType.class,
    CreateManagedFolderResponseType.class,
    GetEventsResponseType.class,
    ConvertIdResponseType.class,
    GetStreamingEventsResponseType.class,
    GetServerTimeZonesResponseType.class,
    CreateUserConfigurationResponseType.class,
    UpdateFolderResponseType.class,
    SendNotificationResponseType.class,
    SyncFolderHierarchyResponseType.class,
    UpdateUserConfigurationResponseType.class,
    SyncFolderItemsResponseType.class,
    CreateItemResponseType.class,
    ExpandDLResponseType.class,
    GetAttachmentResponseType.class,
    DeleteFolderResponseType.class,
    CreateAttachmentResponseType.class,
    FindFolderResponseType.class,
    CopyItemResponseType.class,
    DeleteUserConfigurationResponseType.class,
    FindMailboxStatisticsByKeywordsResponseType.class,
    GetFolderResponseType.class,
    ExportItemsResponseType.class,
    SubscribeResponseType.class,
    DeleteItemResponseType.class,
    GetItemResponseType.class,
    UnsubscribeResponseType.class,
    UploadItemsResponseType.class,
    CreateFolderResponseType.class,
    FindItemResponseType.class,
    EmptyFolderResponseType.class,
    ResolveNamesResponseType.class,
    MoveFolderResponseType.class,
    CopyFolderResponseType.class
})
public class BaseResponseMessageType {

    @XmlElement(name = "ResponseMessages", required = true)
    protected ArrayOfResponseMessagesType responseMessages;

    /**
     * Gets the value of the responseMessages property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfResponseMessagesType }
     *     
     */
    public ArrayOfResponseMessagesType getResponseMessages() {
        return responseMessages;
    }

    /**
     * Sets the value of the responseMessages property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfResponseMessagesType }
     *     
     */
    public void setResponseMessages(ArrayOfResponseMessagesType value) {
        this.responseMessages = value;
    }

}
