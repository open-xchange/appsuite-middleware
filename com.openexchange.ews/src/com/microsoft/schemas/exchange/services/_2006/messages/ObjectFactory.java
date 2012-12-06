
package com.microsoft.schemas.exchange.services._2006.messages;

import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.microsoft.schemas.exchange.services._2006.messages package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetUserAvailabilityRequest_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetUserAvailabilityRequest");
    private final static QName _GetServiceConfiguration_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetServiceConfiguration");
    private final static QName _DeleteAttachment_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteAttachment");
    private final static QName _ExportItems_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ExportItems");
    private final static QName _SendNotificationResult_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SendNotificationResult");
    private final static QName _DeleteItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteItem");
    private final static QName _GetUserConfiguration_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetUserConfiguration");
    private final static QName _EmptyFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "EmptyFolder");
    private final static QName _FindConversationResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindConversationResponse");
    private final static QName _UnsubscribeResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UnsubscribeResponse");
    private final static QName _GetUserOofSettingsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetUserOofSettingsResponse");
    private final static QName _GetMailTips_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetMailTips");
    private final static QName _UploadItemsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UploadItemsResponse");
    private final static QName _FindMessageTrackingReport_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindMessageTrackingReport");
    private final static QName _AddDelegateResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "AddDelegateResponse");
    private final static QName _CreateFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateFolderResponse");
    private final static QName _CopyFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CopyFolderResponse");
    private final static QName _MoveFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "MoveFolderResponse");
    private final static QName _ApplyConversationAction_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ApplyConversationAction");
    private final static QName _GetSharingMetadataResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetSharingMetadataResponse");
    private final static QName _SetUserOofSettingsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SetUserOofSettingsResponse");
    private final static QName _PlayOnPhone_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "PlayOnPhone");
    private final static QName _SyncFolderHierarchy_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SyncFolderHierarchy");
    private final static QName _Unsubscribe_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "Unsubscribe");
    private final static QName _GetAttachment_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetAttachment");
    private final static QName _RefreshSharingFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "RefreshSharingFolderResponse");
    private final static QName _GetDelegate_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetDelegate");
    private final static QName _EmptyFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "EmptyFolderResponse");
    private final static QName _FindItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindItemResponse");
    private final static QName _MoveItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "MoveItem");
    private final static QName _RemoveDelegate_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "RemoveDelegate");
    private final static QName _ResolveNamesResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ResolveNamesResponse");
    private final static QName _GetRoomsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetRoomsResponse");
    private final static QName _GetServiceConfigurationResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetServiceConfigurationResponse");
    private final static QName _UpdateFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateFolder");
    private final static QName _FindItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindItem");
    private final static QName _DeleteUserConfigurationResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteUserConfigurationResponse");
    private final static QName _CopyFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CopyFolder");
    private final static QName _GetEvents_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetEvents");
    private final static QName _FindFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindFolderResponse");
    private final static QName _CopyItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CopyItemResponse");
    private final static QName _GetMessageTrackingReportResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetMessageTrackingReportResponse");
    private final static QName _UpdateUserConfiguration_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateUserConfiguration");
    private final static QName _FindMailboxStatisticsByKeywords_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindMailboxStatisticsByKeywords");
    private final static QName _SubscribeResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SubscribeResponse");
    private final static QName _GetMessageTrackingReport_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetMessageTrackingReport");
    private final static QName _GetRoomListsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetRoomListsResponse");
    private final static QName _DeleteItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteItemResponse");
    private final static QName _CreateAttachment_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateAttachment");
    private final static QName _GetItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetItemResponse");
    private final static QName _SetUserOofSettingsRequest_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SetUserOofSettingsRequest");
    private final static QName _ExpandDL_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ExpandDL");
    private final static QName _CopyItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CopyItem");
    private final static QName _CreateManagedFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateManagedFolder");
    private final static QName _GetSharingFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetSharingFolderResponse");
    private final static QName _FindMailboxStatisticsByKeywordsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindMailboxStatisticsByKeywordsResponse");
    private final static QName _GetStreamingEvents_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetStreamingEvents");
    private final static QName _DeleteUserConfiguration_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteUserConfiguration");
    private final static QName _ExportItemsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ExportItemsResponse");
    private final static QName _UpdateInboxRulesResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateInboxRulesResponse");
    private final static QName _GetFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetFolderResponse");
    private final static QName _DisconnectPhoneCall_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DisconnectPhoneCall");
    private final static QName _SyncFolderItemsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SyncFolderItemsResponse");
    private final static QName _GetSharingFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetSharingFolder");
    private final static QName _UpdateUserConfigurationResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateUserConfigurationResponse");
    private final static QName _GetSharingMetadata_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetSharingMetadata");
    private final static QName _DisconnectPhoneCallResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DisconnectPhoneCallResponse");
    private final static QName _SendItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SendItem");
    private final static QName _CreateItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateItemResponse");
    private final static QName _ExpandDLResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ExpandDLResponse");
    private final static QName _RemoveDelegateResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "RemoveDelegateResponse");
    private final static QName _GetRooms_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetRooms");
    private final static QName _GetPhoneCallInformation_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetPhoneCallInformation");
    private final static QName _RefreshSharingFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "RefreshSharingFolder");
    private final static QName _SyncFolderItems_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SyncFolderItems");
    private final static QName _GetPasswordExpirationDateResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetPasswordExpirationDateResponse");
    private final static QName _SyncFolderHierarchyResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SyncFolderHierarchyResponse");
    private final static QName _SendNotification_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SendNotification");
    private final static QName _FindMessageTrackingReportResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindMessageTrackingReportResponse");
    private final static QName _GetPasswordExpirationDate_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetPasswordExpirationDate");
    private final static QName _GetServerTimeZones_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetServerTimeZones");
    private final static QName _DeleteFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteFolderResponse");
    private final static QName _CreateAttachmentResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateAttachmentResponse");
    private final static QName _GetUserAvailabilityResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetUserAvailabilityResponse");
    private final static QName _CreateFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateFolder");
    private final static QName _ResolveNames_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ResolveNames");
    private final static QName _FindConversation_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindConversation");
    private final static QName _GetAttachmentResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetAttachmentResponse");
    private final static QName _GetItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetItem");
    private final static QName _GetDelegateResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetDelegateResponse");
    private final static QName _UpdateDelegate_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateDelegate");
    private final static QName _ApplyConversationActionResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ApplyConversationActionResponse");
    private final static QName _GetPhoneCallInformationResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetPhoneCallInformationResponse");
    private final static QName _AddDelegate_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "AddDelegate");
    private final static QName _Subscribe_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "Subscribe");
    private final static QName _GetUserConfigurationResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetUserConfigurationResponse");
    private final static QName _CreateItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateItem");
    private final static QName _GetFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetFolder");
    private final static QName _MoveItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "MoveItemResponse");
    private final static QName _DeleteAttachmentResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteAttachmentResponse");
    private final static QName _SendItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SendItemResponse");
    private final static QName _UpdateInboxRules_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateInboxRules");
    private final static QName _FindFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindFolder");
    private final static QName _GetInboxRulesResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetInboxRulesResponse");
    private final static QName _PlayOnPhoneResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "PlayOnPhoneResponse");
    private final static QName _CreateUserConfigurationResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateUserConfigurationResponse");
    private final static QName _DeleteFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteFolder");
    private final static QName _ConvertId_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ConvertId");
    private final static QName _GetServerTimeZonesResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetServerTimeZonesResponse");
    private final static QName _UploadItems_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UploadItems");
    private final static QName _UpdateDelegateResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateDelegateResponse");
    private final static QName _GetUserOofSettingsRequest_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetUserOofSettingsRequest");
    private final static QName _UpdateFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateFolderResponse");
    private final static QName _GetInboxRules_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetInboxRules");
    private final static QName _UpdateItemResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateItemResponse");
    private final static QName _GetMailTipsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetMailTipsResponse");
    private final static QName _CreateManagedFolderResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateManagedFolderResponse");
    private final static QName _GetRoomLists_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetRoomLists");
    private final static QName _UpdateItem_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateItem");
    private final static QName _GetEventsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetEventsResponse");
    private final static QName _ConvertIdResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ConvertIdResponse");
    private final static QName _CreateUserConfiguration_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateUserConfiguration");
    private final static QName _MoveFolder_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "MoveFolder");
    private final static QName _GetStreamingEventsResponse_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetStreamingEventsResponse");
    private final static QName _ArrayOfResponseMessagesTypeCreateUserConfigurationResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateUserConfigurationResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeSubscribeResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SubscribeResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeFindItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeResolveNamesResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ResolveNamesResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetUserConfigurationResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetUserConfigurationResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeDeleteAttachmentResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteAttachmentResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeApplyConversationActionResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ApplyConversationActionResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeExportItemsResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ExportItemsResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeUpdateFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeCopyFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CopyFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeConvertIdResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ConvertIdResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeCreateFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeUpdateItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeCopyItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CopyItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeSyncFolderItemsResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SyncFolderItemsResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetStreamingEventsResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetStreamingEventsResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeRefreshSharingFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "RefreshSharingFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeSendNotificationResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SendNotificationResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeExpandDLResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ExpandDLResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetAttachmentResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetAttachmentResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeEmptyFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "EmptyFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeUploadItemsResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UploadItemsResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeUpdateUserConfigurationResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UpdateUserConfigurationResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetSharingMetadataResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetSharingMetadataResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeSyncFolderHierarchyResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SyncFolderHierarchyResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeMoveFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "MoveFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeMoveItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "MoveItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeFindFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetSharingFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetSharingFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetServerTimeZonesResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetServerTimeZonesResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeCreateItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeUnsubscribeResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "UnsubscribeResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeCreateManagedFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateManagedFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeCreateAttachmentResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "CreateAttachmentResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeDeleteItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeDeleteFolderResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteFolderResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeFindMailboxStatisticsByKeywordsResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "FindMailboxStatisticsByKeywordsResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeSendItemResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "SendItemResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeGetEventsResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "GetEventsResponseMessage");
    private final static QName _ArrayOfResponseMessagesTypeDeleteUserConfigurationResponseMessage_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "DeleteUserConfigurationResponseMessage");
    private final static QName _ArrayOfServiceConfigurationTypeConfigurationName_QNAME = new QName("http://schemas.microsoft.com/exchange/services/2006/messages", "ConfigurationName");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.microsoft.schemas.exchange.services._2006.messages
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ItemInfoResponseMessageType }
     * 
     */
    public ItemInfoResponseMessageType createItemInfoResponseMessageType() {
        return new ItemInfoResponseMessageType();
    }

    /**
     * Create an instance of {@link ConvertIdResponseMessageType }
     * 
     */
    public ConvertIdResponseMessageType createConvertIdResponseMessageType() {
        return new ConvertIdResponseMessageType();
    }

    /**
     * Create an instance of {@link SyncFolderItemsResponseType }
     * 
     */
    public SyncFolderItemsResponseType createSyncFolderItemsResponseType() {
        return new SyncFolderItemsResponseType();
    }

    /**
     * Create an instance of {@link GetSharingFolderType }
     * 
     */
    public GetSharingFolderType createGetSharingFolderType() {
        return new GetSharingFolderType();
    }

    /**
     * Create an instance of {@link CreateFolderType }
     * 
     */
    public CreateFolderType createCreateFolderType() {
        return new CreateFolderType();
    }

    /**
     * Create an instance of {@link BaseResponseMessageType }
     * 
     */
    public BaseResponseMessageType createBaseResponseMessageType() {
        return new BaseResponseMessageType();
    }

    /**
     * Create an instance of {@link GetStreamingEventsResponseMessageType }
     * 
     */
    public GetStreamingEventsResponseMessageType createGetStreamingEventsResponseMessageType() {
        return new GetStreamingEventsResponseMessageType();
    }

    /**
     * Create an instance of {@link PlayOnPhoneResponseMessageType }
     * 
     */
    public PlayOnPhoneResponseMessageType createPlayOnPhoneResponseMessageType() {
        return new PlayOnPhoneResponseMessageType();
    }

    /**
     * Create an instance of {@link GetAttachmentResponseType }
     * 
     */
    public GetAttachmentResponseType createGetAttachmentResponseType() {
        return new GetAttachmentResponseType();
    }

    /**
     * Create an instance of {@link UploadItemsResponseType }
     * 
     */
    public UploadItemsResponseType createUploadItemsResponseType() {
        return new UploadItemsResponseType();
    }

    /**
     * Create an instance of {@link SubscribeResponseType }
     * 
     */
    public SubscribeResponseType createSubscribeResponseType() {
        return new SubscribeResponseType();
    }

    /**
     * Create an instance of {@link GetDelegateType }
     * 
     */
    public GetDelegateType createGetDelegateType() {
        return new GetDelegateType();
    }

    /**
     * Create an instance of {@link BaseMoveCopyItemType }
     * 
     */
    public BaseMoveCopyItemType createBaseMoveCopyItemType() {
        return new BaseMoveCopyItemType();
    }

    /**
     * Create an instance of {@link FindMessageTrackingReportRequestType }
     * 
     */
    public FindMessageTrackingReportRequestType createFindMessageTrackingReportRequestType() {
        return new FindMessageTrackingReportRequestType();
    }

    /**
     * Create an instance of {@link SuggestionsResponseType }
     * 
     */
    public SuggestionsResponseType createSuggestionsResponseType() {
        return new SuggestionsResponseType();
    }

    /**
     * Create an instance of {@link GetPhoneCallInformationResponseMessageType }
     * 
     */
    public GetPhoneCallInformationResponseMessageType createGetPhoneCallInformationResponseMessageType() {
        return new GetPhoneCallInformationResponseMessageType();
    }

    /**
     * Create an instance of {@link DeleteItemResponseType }
     * 
     */
    public DeleteItemResponseType createDeleteItemResponseType() {
        return new DeleteItemResponseType();
    }

    /**
     * Create an instance of {@link DeleteAttachmentResponseType }
     * 
     */
    public DeleteAttachmentResponseType createDeleteAttachmentResponseType() {
        return new DeleteAttachmentResponseType();
    }

    /**
     * Create an instance of {@link MoveFolderResponseType }
     * 
     */
    public MoveFolderResponseType createMoveFolderResponseType() {
        return new MoveFolderResponseType();
    }

    /**
     * Create an instance of {@link DeleteAttachmentResponseMessageType }
     * 
     */
    public DeleteAttachmentResponseMessageType createDeleteAttachmentResponseMessageType() {
        return new DeleteAttachmentResponseMessageType();
    }

    /**
     * Create an instance of {@link DeleteUserConfigurationResponseType }
     * 
     */
    public DeleteUserConfigurationResponseType createDeleteUserConfigurationResponseType() {
        return new DeleteUserConfigurationResponseType();
    }

    /**
     * Create an instance of {@link UpdateUserConfigurationResponseType }
     * 
     */
    public UpdateUserConfigurationResponseType createUpdateUserConfigurationResponseType() {
        return new UpdateUserConfigurationResponseType();
    }

    /**
     * Create an instance of {@link FindFolderResponseType }
     * 
     */
    public FindFolderResponseType createFindFolderResponseType() {
        return new FindFolderResponseType();
    }

    /**
     * Create an instance of {@link UpdateUserConfigurationType }
     * 
     */
    public UpdateUserConfigurationType createUpdateUserConfigurationType() {
        return new UpdateUserConfigurationType();
    }

    /**
     * Create an instance of {@link GetItemType }
     * 
     */
    public GetItemType createGetItemType() {
        return new GetItemType();
    }

    /**
     * Create an instance of {@link PlayOnPhoneType }
     * 
     */
    public PlayOnPhoneType createPlayOnPhoneType() {
        return new PlayOnPhoneType();
    }

    /**
     * Create an instance of {@link RemoveDelegateType }
     * 
     */
    public RemoveDelegateType createRemoveDelegateType() {
        return new RemoveDelegateType();
    }

    /**
     * Create an instance of {@link CreateManagedFolderRequestType }
     * 
     */
    public CreateManagedFolderRequestType createCreateManagedFolderRequestType() {
        return new CreateManagedFolderRequestType();
    }

    /**
     * Create an instance of {@link SetUserOofSettingsRequest }
     * 
     */
    public SetUserOofSettingsRequest createSetUserOofSettingsRequest() {
        return new SetUserOofSettingsRequest();
    }

    /**
     * Create an instance of {@link UpdateInboxRulesRequestType }
     * 
     */
    public UpdateInboxRulesRequestType createUpdateInboxRulesRequestType() {
        return new UpdateInboxRulesRequestType();
    }

    /**
     * Create an instance of {@link UpdateItemResponseMessageType }
     * 
     */
    public UpdateItemResponseMessageType createUpdateItemResponseMessageType() {
        return new UpdateItemResponseMessageType();
    }

    /**
     * Create an instance of {@link GetEventsType }
     * 
     */
    public GetEventsType createGetEventsType() {
        return new GetEventsType();
    }

    /**
     * Create an instance of {@link GetInboxRulesResponseType }
     * 
     */
    public GetInboxRulesResponseType createGetInboxRulesResponseType() {
        return new GetInboxRulesResponseType();
    }

    /**
     * Create an instance of {@link GetFolderResponseType }
     * 
     */
    public GetFolderResponseType createGetFolderResponseType() {
        return new GetFolderResponseType();
    }

    /**
     * Create an instance of {@link GetSharingMetadataType }
     * 
     */
    public GetSharingMetadataType createGetSharingMetadataType() {
        return new GetSharingMetadataType();
    }

    /**
     * Create an instance of {@link FindMailboxStatisticsByKeywordsType }
     * 
     */
    public FindMailboxStatisticsByKeywordsType createFindMailboxStatisticsByKeywordsType() {
        return new FindMailboxStatisticsByKeywordsType();
    }

    /**
     * Create an instance of {@link SyncFolderHierarchyType }
     * 
     */
    public SyncFolderHierarchyType createSyncFolderHierarchyType() {
        return new SyncFolderHierarchyType();
    }

    /**
     * Create an instance of {@link CopyItemType }
     * 
     */
    public CopyItemType createCopyItemType() {
        return new CopyItemType();
    }

    /**
     * Create an instance of {@link SyncFolderHierarchyResponseMessageType }
     * 
     */
    public SyncFolderHierarchyResponseMessageType createSyncFolderHierarchyResponseMessageType() {
        return new SyncFolderHierarchyResponseMessageType();
    }

    /**
     * Create an instance of {@link GetServiceConfigurationResponseMessageType }
     * 
     */
    public GetServiceConfigurationResponseMessageType createGetServiceConfigurationResponseMessageType() {
        return new GetServiceConfigurationResponseMessageType();
    }

    /**
     * Create an instance of {@link ArrayOfFreeBusyResponse }
     * 
     */
    public ArrayOfFreeBusyResponse createArrayOfFreeBusyResponse() {
        return new ArrayOfFreeBusyResponse();
    }

    /**
     * Create an instance of {@link DelegateUserResponseMessageType }
     * 
     */
    public DelegateUserResponseMessageType createDelegateUserResponseMessageType() {
        return new DelegateUserResponseMessageType();
    }

    /**
     * Create an instance of {@link ExportItemsResponseType }
     * 
     */
    public ExportItemsResponseType createExportItemsResponseType() {
        return new ExportItemsResponseType();
    }

    /**
     * Create an instance of {@link ExpandDLResponseMessageType }
     * 
     */
    public ExpandDLResponseMessageType createExpandDLResponseMessageType() {
        return new ExpandDLResponseMessageType();
    }

    /**
     * Create an instance of {@link MoveItemResponseType }
     * 
     */
    public MoveItemResponseType createMoveItemResponseType() {
        return new MoveItemResponseType();
    }

    /**
     * Create an instance of {@link DeleteFolderType }
     * 
     */
    public DeleteFolderType createDeleteFolderType() {
        return new DeleteFolderType();
    }

    /**
     * Create an instance of {@link FindFolderType }
     * 
     */
    public FindFolderType createFindFolderType() {
        return new FindFolderType();
    }

    /**
     * Create an instance of {@link RemoveDelegateResponseMessageType }
     * 
     */
    public RemoveDelegateResponseMessageType createRemoveDelegateResponseMessageType() {
        return new RemoveDelegateResponseMessageType();
    }

    /**
     * Create an instance of {@link GetStreamingEventsResponseType }
     * 
     */
    public GetStreamingEventsResponseType createGetStreamingEventsResponseType() {
        return new GetStreamingEventsResponseType();
    }

    /**
     * Create an instance of {@link UpdateInboxRulesResponseType }
     * 
     */
    public UpdateInboxRulesResponseType createUpdateInboxRulesResponseType() {
        return new UpdateInboxRulesResponseType();
    }

    /**
     * Create an instance of {@link ServiceConfigurationResponseMessageType }
     * 
     */
    public ServiceConfigurationResponseMessageType createServiceConfigurationResponseMessageType() {
        return new ServiceConfigurationResponseMessageType();
    }

    /**
     * Create an instance of {@link UploadItemsType }
     * 
     */
    public UploadItemsType createUploadItemsType() {
        return new UploadItemsType();
    }

    /**
     * Create an instance of {@link MoveFolderType }
     * 
     */
    public MoveFolderType createMoveFolderType() {
        return new MoveFolderType();
    }

    /**
     * Create an instance of {@link GetServerTimeZonesType }
     * 
     */
    public GetServerTimeZonesType createGetServerTimeZonesType() {
        return new GetServerTimeZonesType();
    }

    /**
     * Create an instance of {@link FindItemResponseMessageType }
     * 
     */
    public FindItemResponseMessageType createFindItemResponseMessageType() {
        return new FindItemResponseMessageType();
    }

    /**
     * Create an instance of {@link ArrayOfDelegateUserResponseMessageType }
     * 
     */
    public ArrayOfDelegateUserResponseMessageType createArrayOfDelegateUserResponseMessageType() {
        return new ArrayOfDelegateUserResponseMessageType();
    }

    /**
     * Create an instance of {@link GetPasswordExpirationDateType }
     * 
     */
    public GetPasswordExpirationDateType createGetPasswordExpirationDateType() {
        return new GetPasswordExpirationDateType();
    }

    /**
     * Create an instance of {@link SubscribeResponseMessageType }
     * 
     */
    public SubscribeResponseMessageType createSubscribeResponseMessageType() {
        return new SubscribeResponseMessageType();
    }

    /**
     * Create an instance of {@link SendNotificationResponseMessageType }
     * 
     */
    public SendNotificationResponseMessageType createSendNotificationResponseMessageType() {
        return new SendNotificationResponseMessageType();
    }

    /**
     * Create an instance of {@link ArrayOfResponseMessagesType }
     * 
     */
    public ArrayOfResponseMessagesType createArrayOfResponseMessagesType() {
        return new ArrayOfResponseMessagesType();
    }

    /**
     * Create an instance of {@link CreateUserConfigurationResponseType }
     * 
     */
    public CreateUserConfigurationResponseType createCreateUserConfigurationResponseType() {
        return new CreateUserConfigurationResponseType();
    }

    /**
     * Create an instance of {@link SyncFolderItemsResponseMessageType }
     * 
     */
    public SyncFolderItemsResponseMessageType createSyncFolderItemsResponseMessageType() {
        return new SyncFolderItemsResponseMessageType();
    }

    /**
     * Create an instance of {@link SyncFolderHierarchyResponseType }
     * 
     */
    public SyncFolderHierarchyResponseType createSyncFolderHierarchyResponseType() {
        return new SyncFolderHierarchyResponseType();
    }

    /**
     * Create an instance of {@link EmptyFolderType }
     * 
     */
    public EmptyFolderType createEmptyFolderType() {
        return new EmptyFolderType();
    }

    /**
     * Create an instance of {@link GetRoomListsResponseMessageType }
     * 
     */
    public GetRoomListsResponseMessageType createGetRoomListsResponseMessageType() {
        return new GetRoomListsResponseMessageType();
    }

    /**
     * Create an instance of {@link GetUserConfigurationResponseMessageType }
     * 
     */
    public GetUserConfigurationResponseMessageType createGetUserConfigurationResponseMessageType() {
        return new GetUserConfigurationResponseMessageType();
    }

    /**
     * Create an instance of {@link GetStreamingEventsType }
     * 
     */
    public GetStreamingEventsType createGetStreamingEventsType() {
        return new GetStreamingEventsType();
    }

    /**
     * Create an instance of {@link MoveItemType }
     * 
     */
    public MoveItemType createMoveItemType() {
        return new MoveItemType();
    }

    /**
     * Create an instance of {@link ConvertIdResponseType }
     * 
     */
    public ConvertIdResponseType createConvertIdResponseType() {
        return new ConvertIdResponseType();
    }

    /**
     * Create an instance of {@link FindMailboxStatisticsByKeywordsResponseMessageType }
     * 
     */
    public FindMailboxStatisticsByKeywordsResponseMessageType createFindMailboxStatisticsByKeywordsResponseMessageType() {
        return new FindMailboxStatisticsByKeywordsResponseMessageType();
    }

    /**
     * Create an instance of {@link UpdateDelegateType }
     * 
     */
    public UpdateDelegateType createUpdateDelegateType() {
        return new UpdateDelegateType();
    }

    /**
     * Create an instance of {@link GetServerTimeZonesResponseMessageType }
     * 
     */
    public GetServerTimeZonesResponseMessageType createGetServerTimeZonesResponseMessageType() {
        return new GetServerTimeZonesResponseMessageType();
    }

    /**
     * Create an instance of {@link UpdateItemType }
     * 
     */
    public UpdateItemType createUpdateItemType() {
        return new UpdateItemType();
    }

    /**
     * Create an instance of {@link GetPhoneCallInformationType }
     * 
     */
    public GetPhoneCallInformationType createGetPhoneCallInformationType() {
        return new GetPhoneCallInformationType();
    }

    /**
     * Create an instance of {@link GetEventsResponseMessageType }
     * 
     */
    public GetEventsResponseMessageType createGetEventsResponseMessageType() {
        return new GetEventsResponseMessageType();
    }

    /**
     * Create an instance of {@link CopyFolderResponseType }
     * 
     */
    public CopyFolderResponseType createCopyFolderResponseType() {
        return new CopyFolderResponseType();
    }

    /**
     * Create an instance of {@link GetInboxRulesRequestType }
     * 
     */
    public GetInboxRulesRequestType createGetInboxRulesRequestType() {
        return new GetInboxRulesRequestType();
    }

    /**
     * Create an instance of {@link GetSharingFolderResponseMessageType }
     * 
     */
    public GetSharingFolderResponseMessageType createGetSharingFolderResponseMessageType() {
        return new GetSharingFolderResponseMessageType();
    }

    /**
     * Create an instance of {@link GetFolderType }
     * 
     */
    public GetFolderType createGetFolderType() {
        return new GetFolderType();
    }

    /**
     * Create an instance of {@link CopyItemResponseType }
     * 
     */
    public CopyItemResponseType createCopyItemResponseType() {
        return new CopyItemResponseType();
    }

    /**
     * Create an instance of {@link SetUserOofSettingsResponse }
     * 
     */
    public SetUserOofSettingsResponse createSetUserOofSettingsResponse() {
        return new SetUserOofSettingsResponse();
    }

    /**
     * Create an instance of {@link FindItemResponseType }
     * 
     */
    public FindItemResponseType createFindItemResponseType() {
        return new FindItemResponseType();
    }

    /**
     * Create an instance of {@link AttachmentInfoResponseMessageType }
     * 
     */
    public AttachmentInfoResponseMessageType createAttachmentInfoResponseMessageType() {
        return new AttachmentInfoResponseMessageType();
    }

    /**
     * Create an instance of {@link ExpandDLResponseType }
     * 
     */
    public ExpandDLResponseType createExpandDLResponseType() {
        return new ExpandDLResponseType();
    }

    /**
     * Create an instance of {@link DeleteAttachmentType }
     * 
     */
    public DeleteAttachmentType createDeleteAttachmentType() {
        return new DeleteAttachmentType();
    }

    /**
     * Create an instance of {@link DeleteItemType }
     * 
     */
    public DeleteItemType createDeleteItemType() {
        return new DeleteItemType();
    }

    /**
     * Create an instance of {@link GetServerTimeZonesResponseType }
     * 
     */
    public GetServerTimeZonesResponseType createGetServerTimeZonesResponseType() {
        return new GetServerTimeZonesResponseType();
    }

    /**
     * Create an instance of {@link ApplyConversationActionType }
     * 
     */
    public ApplyConversationActionType createApplyConversationActionType() {
        return new ApplyConversationActionType();
    }

    /**
     * Create an instance of {@link GetSharingMetadataResponseMessageType }
     * 
     */
    public GetSharingMetadataResponseMessageType createGetSharingMetadataResponseMessageType() {
        return new GetSharingMetadataResponseMessageType();
    }

    /**
     * Create an instance of {@link CreateUserConfigurationType }
     * 
     */
    public CreateUserConfigurationType createCreateUserConfigurationType() {
        return new CreateUserConfigurationType();
    }

    /**
     * Create an instance of {@link UpdateFolderType }
     * 
     */
    public UpdateFolderType createUpdateFolderType() {
        return new UpdateFolderType();
    }

    /**
     * Create an instance of {@link MailTipsResponseMessageType }
     * 
     */
    public MailTipsResponseMessageType createMailTipsResponseMessageType() {
        return new MailTipsResponseMessageType();
    }

    /**
     * Create an instance of {@link UpdateDelegateResponseMessageType }
     * 
     */
    public UpdateDelegateResponseMessageType createUpdateDelegateResponseMessageType() {
        return new UpdateDelegateResponseMessageType();
    }

    /**
     * Create an instance of {@link UpdateItemResponseType }
     * 
     */
    public UpdateItemResponseType createUpdateItemResponseType() {
        return new UpdateItemResponseType();
    }

    /**
     * Create an instance of {@link GetUserConfigurationResponseType }
     * 
     */
    public GetUserConfigurationResponseType createGetUserConfigurationResponseType() {
        return new GetUserConfigurationResponseType();
    }

    /**
     * Create an instance of {@link GetUserAvailabilityRequestType }
     * 
     */
    public GetUserAvailabilityRequestType createGetUserAvailabilityRequestType() {
        return new GetUserAvailabilityRequestType();
    }

    /**
     * Create an instance of {@link ArrayOfMailTipsResponseMessageType }
     * 
     */
    public ArrayOfMailTipsResponseMessageType createArrayOfMailTipsResponseMessageType() {
        return new ArrayOfMailTipsResponseMessageType();
    }

    /**
     * Create an instance of {@link ExportItemsResponseMessageType }
     * 
     */
    public ExportItemsResponseMessageType createExportItemsResponseMessageType() {
        return new ExportItemsResponseMessageType();
    }

    /**
     * Create an instance of {@link CreateItemResponseType }
     * 
     */
    public CreateItemResponseType createCreateItemResponseType() {
        return new CreateItemResponseType();
    }

    /**
     * Create an instance of {@link ExportItemsType }
     * 
     */
    public ExportItemsType createExportItemsType() {
        return new ExportItemsType();
    }

    /**
     * Create an instance of {@link SendItemType }
     * 
     */
    public SendItemType createSendItemType() {
        return new SendItemType();
    }

    /**
     * Create an instance of {@link CreateAttachmentResponseType }
     * 
     */
    public CreateAttachmentResponseType createCreateAttachmentResponseType() {
        return new CreateAttachmentResponseType();
    }

    /**
     * Create an instance of {@link SyncFolderItemsType }
     * 
     */
    public SyncFolderItemsType createSyncFolderItemsType() {
        return new SyncFolderItemsType();
    }

    /**
     * Create an instance of {@link UnsubscribeType }
     * 
     */
    public UnsubscribeType createUnsubscribeType() {
        return new UnsubscribeType();
    }

    /**
     * Create an instance of {@link ArrayOfServiceConfigurationType }
     * 
     */
    public ArrayOfServiceConfigurationType createArrayOfServiceConfigurationType() {
        return new ArrayOfServiceConfigurationType();
    }

    /**
     * Create an instance of {@link UnsubscribeResponseType }
     * 
     */
    public UnsubscribeResponseType createUnsubscribeResponseType() {
        return new UnsubscribeResponseType();
    }

    /**
     * Create an instance of {@link FreeBusyResponseType }
     * 
     */
    public FreeBusyResponseType createFreeBusyResponseType() {
        return new FreeBusyResponseType();
    }

    /**
     * Create an instance of {@link GetRoomListsType }
     * 
     */
    public GetRoomListsType createGetRoomListsType() {
        return new GetRoomListsType();
    }

    /**
     * Create an instance of {@link ResolveNamesResponseMessageType }
     * 
     */
    public ResolveNamesResponseMessageType createResolveNamesResponseMessageType() {
        return new ResolveNamesResponseMessageType();
    }

    /**
     * Create an instance of {@link UpdateFolderResponseType }
     * 
     */
    public UpdateFolderResponseType createUpdateFolderResponseType() {
        return new UpdateFolderResponseType();
    }

    /**
     * Create an instance of {@link SubscribeType }
     * 
     */
    public SubscribeType createSubscribeType() {
        return new SubscribeType();
    }

    /**
     * Create an instance of {@link CreateItemType }
     * 
     */
    public CreateItemType createCreateItemType() {
        return new CreateItemType();
    }

    /**
     * Create an instance of {@link ResolveNamesType }
     * 
     */
    public ResolveNamesType createResolveNamesType() {
        return new ResolveNamesType();
    }

    /**
     * Create an instance of {@link BaseMoveCopyFolderType }
     * 
     */
    public BaseMoveCopyFolderType createBaseMoveCopyFolderType() {
        return new BaseMoveCopyFolderType();
    }

    /**
     * Create an instance of {@link AddDelegateResponseMessageType }
     * 
     */
    public AddDelegateResponseMessageType createAddDelegateResponseMessageType() {
        return new AddDelegateResponseMessageType();
    }

    /**
     * Create an instance of {@link EmptyFolderResponseType }
     * 
     */
    public EmptyFolderResponseType createEmptyFolderResponseType() {
        return new EmptyFolderResponseType();
    }

    /**
     * Create an instance of {@link GetUserConfigurationType }
     * 
     */
    public GetUserConfigurationType createGetUserConfigurationType() {
        return new GetUserConfigurationType();
    }

    /**
     * Create an instance of {@link FolderInfoResponseMessageType }
     * 
     */
    public FolderInfoResponseMessageType createFolderInfoResponseMessageType() {
        return new FolderInfoResponseMessageType();
    }

    /**
     * Create an instance of {@link ExpandDLType }
     * 
     */
    public ExpandDLType createExpandDLType() {
        return new ExpandDLType();
    }

    /**
     * Create an instance of {@link FindMessageTrackingReportResponseMessageType }
     * 
     */
    public FindMessageTrackingReportResponseMessageType createFindMessageTrackingReportResponseMessageType() {
        return new FindMessageTrackingReportResponseMessageType();
    }

    /**
     * Create an instance of {@link GetAttachmentType }
     * 
     */
    public GetAttachmentType createGetAttachmentType() {
        return new GetAttachmentType();
    }

    /**
     * Create an instance of {@link DisconnectPhoneCallResponseMessageType }
     * 
     */
    public DisconnectPhoneCallResponseMessageType createDisconnectPhoneCallResponseMessageType() {
        return new DisconnectPhoneCallResponseMessageType();
    }

    /**
     * Create an instance of {@link GetServiceConfigurationType }
     * 
     */
    public GetServiceConfigurationType createGetServiceConfigurationType() {
        return new GetServiceConfigurationType();
    }

    /**
     * Create an instance of {@link GetRoomsResponseMessageType }
     * 
     */
    public GetRoomsResponseMessageType createGetRoomsResponseMessageType() {
        return new GetRoomsResponseMessageType();
    }

    /**
     * Create an instance of {@link RefreshSharingFolderResponseMessageType }
     * 
     */
    public RefreshSharingFolderResponseMessageType createRefreshSharingFolderResponseMessageType() {
        return new RefreshSharingFolderResponseMessageType();
    }

    /**
     * Create an instance of {@link GetDelegateResponseMessageType }
     * 
     */
    public GetDelegateResponseMessageType createGetDelegateResponseMessageType() {
        return new GetDelegateResponseMessageType();
    }

    /**
     * Create an instance of {@link ArrayOfServiceConfigurationResponseMessageType }
     * 
     */
    public ArrayOfServiceConfigurationResponseMessageType createArrayOfServiceConfigurationResponseMessageType() {
        return new ArrayOfServiceConfigurationResponseMessageType();
    }

    /**
     * Create an instance of {@link GetMailTipsType }
     * 
     */
    public GetMailTipsType createGetMailTipsType() {
        return new GetMailTipsType();
    }

    /**
     * Create an instance of {@link FindConversationType }
     * 
     */
    public FindConversationType createFindConversationType() {
        return new FindConversationType();
    }

    /**
     * Create an instance of {@link RefreshSharingFolderType }
     * 
     */
    public RefreshSharingFolderType createRefreshSharingFolderType() {
        return new RefreshSharingFolderType();
    }

    /**
     * Create an instance of {@link GetMessageTrackingReportResponseMessageType }
     * 
     */
    public GetMessageTrackingReportResponseMessageType createGetMessageTrackingReportResponseMessageType() {
        return new GetMessageTrackingReportResponseMessageType();
    }

    /**
     * Create an instance of {@link GetRoomsType }
     * 
     */
    public GetRoomsType createGetRoomsType() {
        return new GetRoomsType();
    }

    /**
     * Create an instance of {@link CopyFolderType }
     * 
     */
    public CopyFolderType createCopyFolderType() {
        return new CopyFolderType();
    }

    /**
     * Create an instance of {@link GetEventsResponseType }
     * 
     */
    public GetEventsResponseType createGetEventsResponseType() {
        return new GetEventsResponseType();
    }

    /**
     * Create an instance of {@link CreateAttachmentType }
     * 
     */
    public CreateAttachmentType createCreateAttachmentType() {
        return new CreateAttachmentType();
    }

    /**
     * Create an instance of {@link GetItemResponseType }
     * 
     */
    public GetItemResponseType createGetItemResponseType() {
        return new GetItemResponseType();
    }

    /**
     * Create an instance of {@link ApplyConversationActionResponseType }
     * 
     */
    public ApplyConversationActionResponseType createApplyConversationActionResponseType() {
        return new ApplyConversationActionResponseType();
    }

    /**
     * Create an instance of {@link CreateFolderResponseType }
     * 
     */
    public CreateFolderResponseType createCreateFolderResponseType() {
        return new CreateFolderResponseType();
    }

    /**
     * Create an instance of {@link CreateManagedFolderResponseType }
     * 
     */
    public CreateManagedFolderResponseType createCreateManagedFolderResponseType() {
        return new CreateManagedFolderResponseType();
    }

    /**
     * Create an instance of {@link FindMailboxStatisticsByKeywordsResponseType }
     * 
     */
    public FindMailboxStatisticsByKeywordsResponseType createFindMailboxStatisticsByKeywordsResponseType() {
        return new FindMailboxStatisticsByKeywordsResponseType();
    }

    /**
     * Create an instance of {@link ResponseMessageType }
     * 
     */
    public ResponseMessageType createResponseMessageType() {
        return new ResponseMessageType();
    }

    /**
     * Create an instance of {@link AddDelegateType }
     * 
     */
    public AddDelegateType createAddDelegateType() {
        return new AddDelegateType();
    }

    /**
     * Create an instance of {@link FindConversationResponseMessageType }
     * 
     */
    public FindConversationResponseMessageType createFindConversationResponseMessageType() {
        return new FindConversationResponseMessageType();
    }

    /**
     * Create an instance of {@link ConvertIdType }
     * 
     */
    public ConvertIdType createConvertIdType() {
        return new ConvertIdType();
    }

    /**
     * Create an instance of {@link SendNotificationResultType }
     * 
     */
    public SendNotificationResultType createSendNotificationResultType() {
        return new SendNotificationResultType();
    }

    /**
     * Create an instance of {@link GetUserOofSettingsRequest }
     * 
     */
    public GetUserOofSettingsRequest createGetUserOofSettingsRequest() {
        return new GetUserOofSettingsRequest();
    }

    /**
     * Create an instance of {@link GetPasswordExpirationDateResponseMessageType }
     * 
     */
    public GetPasswordExpirationDateResponseMessageType createGetPasswordExpirationDateResponseMessageType() {
        return new GetPasswordExpirationDateResponseMessageType();
    }

    /**
     * Create an instance of {@link ResolveNamesResponseType }
     * 
     */
    public ResolveNamesResponseType createResolveNamesResponseType() {
        return new ResolveNamesResponseType();
    }

    /**
     * Create an instance of {@link GetUserOofSettingsResponse }
     * 
     */
    public GetUserOofSettingsResponse createGetUserOofSettingsResponse() {
        return new GetUserOofSettingsResponse();
    }

    /**
     * Create an instance of {@link SendItemResponseType }
     * 
     */
    public SendItemResponseType createSendItemResponseType() {
        return new SendItemResponseType();
    }

    /**
     * Create an instance of {@link GetMailTipsResponseMessageType }
     * 
     */
    public GetMailTipsResponseMessageType createGetMailTipsResponseMessageType() {
        return new GetMailTipsResponseMessageType();
    }

    /**
     * Create an instance of {@link FindFolderResponseMessageType }
     * 
     */
    public FindFolderResponseMessageType createFindFolderResponseMessageType() {
        return new FindFolderResponseMessageType();
    }

    /**
     * Create an instance of {@link DeleteFolderResponseType }
     * 
     */
    public DeleteFolderResponseType createDeleteFolderResponseType() {
        return new DeleteFolderResponseType();
    }

    /**
     * Create an instance of {@link DeleteUserConfigurationType }
     * 
     */
    public DeleteUserConfigurationType createDeleteUserConfigurationType() {
        return new DeleteUserConfigurationType();
    }

    /**
     * Create an instance of {@link SendNotificationResponseType }
     * 
     */
    public SendNotificationResponseType createSendNotificationResponseType() {
        return new SendNotificationResponseType();
    }

    /**
     * Create an instance of {@link FindItemType }
     * 
     */
    public FindItemType createFindItemType() {
        return new FindItemType();
    }

    /**
     * Create an instance of {@link GetMessageTrackingReportRequestType }
     * 
     */
    public GetMessageTrackingReportRequestType createGetMessageTrackingReportRequestType() {
        return new GetMessageTrackingReportRequestType();
    }

    /**
     * Create an instance of {@link UploadItemsResponseMessageType }
     * 
     */
    public UploadItemsResponseMessageType createUploadItemsResponseMessageType() {
        return new UploadItemsResponseMessageType();
    }

    /**
     * Create an instance of {@link DisconnectPhoneCallType }
     * 
     */
    public DisconnectPhoneCallType createDisconnectPhoneCallType() {
        return new DisconnectPhoneCallType();
    }

    /**
     * Create an instance of {@link GetUserAvailabilityResponseType }
     * 
     */
    public GetUserAvailabilityResponseType createGetUserAvailabilityResponseType() {
        return new GetUserAvailabilityResponseType();
    }

    /**
     * Create an instance of {@link ResponseMessageType.MessageXml }
     * 
     */
    public ResponseMessageType.MessageXml createResponseMessageTypeMessageXml() {
        return new ResponseMessageType.MessageXml();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserAvailabilityRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetUserAvailabilityRequest")
    public JAXBElement<GetUserAvailabilityRequestType> createGetUserAvailabilityRequest(GetUserAvailabilityRequestType value) {
        return new JAXBElement<GetUserAvailabilityRequestType>(_GetUserAvailabilityRequest_QNAME, GetUserAvailabilityRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceConfigurationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetServiceConfiguration")
    public JAXBElement<GetServiceConfigurationType> createGetServiceConfiguration(GetServiceConfigurationType value) {
        return new JAXBElement<GetServiceConfigurationType>(_GetServiceConfiguration_QNAME, GetServiceConfigurationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteAttachmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteAttachment")
    public JAXBElement<DeleteAttachmentType> createDeleteAttachment(DeleteAttachmentType value) {
        return new JAXBElement<DeleteAttachmentType>(_DeleteAttachment_QNAME, DeleteAttachmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExportItemsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ExportItems")
    public JAXBElement<ExportItemsType> createExportItems(ExportItemsType value) {
        return new JAXBElement<ExportItemsType>(_ExportItems_QNAME, ExportItemsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendNotificationResultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SendNotificationResult")
    public JAXBElement<SendNotificationResultType> createSendNotificationResult(SendNotificationResultType value) {
        return new JAXBElement<SendNotificationResultType>(_SendNotificationResult_QNAME, SendNotificationResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteItem")
    public JAXBElement<DeleteItemType> createDeleteItem(DeleteItemType value) {
        return new JAXBElement<DeleteItemType>(_DeleteItem_QNAME, DeleteItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserConfigurationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetUserConfiguration")
    public JAXBElement<GetUserConfigurationType> createGetUserConfiguration(GetUserConfigurationType value) {
        return new JAXBElement<GetUserConfigurationType>(_GetUserConfiguration_QNAME, GetUserConfigurationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EmptyFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "EmptyFolder")
    public JAXBElement<EmptyFolderType> createEmptyFolder(EmptyFolderType value) {
        return new JAXBElement<EmptyFolderType>(_EmptyFolder_QNAME, EmptyFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindConversationResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindConversationResponse")
    public JAXBElement<FindConversationResponseMessageType> createFindConversationResponse(FindConversationResponseMessageType value) {
        return new JAXBElement<FindConversationResponseMessageType>(_FindConversationResponse_QNAME, FindConversationResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsubscribeResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UnsubscribeResponse")
    public JAXBElement<UnsubscribeResponseType> createUnsubscribeResponse(UnsubscribeResponseType value) {
        return new JAXBElement<UnsubscribeResponseType>(_UnsubscribeResponse_QNAME, UnsubscribeResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserOofSettingsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetUserOofSettingsResponse")
    public JAXBElement<GetUserOofSettingsResponse> createGetUserOofSettingsResponse(GetUserOofSettingsResponse value) {
        return new JAXBElement<GetUserOofSettingsResponse>(_GetUserOofSettingsResponse_QNAME, GetUserOofSettingsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMailTipsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetMailTips")
    public JAXBElement<GetMailTipsType> createGetMailTips(GetMailTipsType value) {
        return new JAXBElement<GetMailTipsType>(_GetMailTips_QNAME, GetMailTipsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadItemsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UploadItemsResponse")
    public JAXBElement<UploadItemsResponseType> createUploadItemsResponse(UploadItemsResponseType value) {
        return new JAXBElement<UploadItemsResponseType>(_UploadItemsResponse_QNAME, UploadItemsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindMessageTrackingReportRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindMessageTrackingReport")
    public JAXBElement<FindMessageTrackingReportRequestType> createFindMessageTrackingReport(FindMessageTrackingReportRequestType value) {
        return new JAXBElement<FindMessageTrackingReportRequestType>(_FindMessageTrackingReport_QNAME, FindMessageTrackingReportRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddDelegateResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "AddDelegateResponse")
    public JAXBElement<AddDelegateResponseMessageType> createAddDelegateResponse(AddDelegateResponseMessageType value) {
        return new JAXBElement<AddDelegateResponseMessageType>(_AddDelegateResponse_QNAME, AddDelegateResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateFolderResponse")
    public JAXBElement<CreateFolderResponseType> createCreateFolderResponse(CreateFolderResponseType value) {
        return new JAXBElement<CreateFolderResponseType>(_CreateFolderResponse_QNAME, CreateFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CopyFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CopyFolderResponse")
    public JAXBElement<CopyFolderResponseType> createCopyFolderResponse(CopyFolderResponseType value) {
        return new JAXBElement<CopyFolderResponseType>(_CopyFolderResponse_QNAME, CopyFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MoveFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "MoveFolderResponse")
    public JAXBElement<MoveFolderResponseType> createMoveFolderResponse(MoveFolderResponseType value) {
        return new JAXBElement<MoveFolderResponseType>(_MoveFolderResponse_QNAME, MoveFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplyConversationActionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ApplyConversationAction")
    public JAXBElement<ApplyConversationActionType> createApplyConversationAction(ApplyConversationActionType value) {
        return new JAXBElement<ApplyConversationActionType>(_ApplyConversationAction_QNAME, ApplyConversationActionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSharingMetadataResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetSharingMetadataResponse")
    public JAXBElement<GetSharingMetadataResponseMessageType> createGetSharingMetadataResponse(GetSharingMetadataResponseMessageType value) {
        return new JAXBElement<GetSharingMetadataResponseMessageType>(_GetSharingMetadataResponse_QNAME, GetSharingMetadataResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetUserOofSettingsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SetUserOofSettingsResponse")
    public JAXBElement<SetUserOofSettingsResponse> createSetUserOofSettingsResponse(SetUserOofSettingsResponse value) {
        return new JAXBElement<SetUserOofSettingsResponse>(_SetUserOofSettingsResponse_QNAME, SetUserOofSettingsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PlayOnPhoneType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "PlayOnPhone")
    public JAXBElement<PlayOnPhoneType> createPlayOnPhone(PlayOnPhoneType value) {
        return new JAXBElement<PlayOnPhoneType>(_PlayOnPhone_QNAME, PlayOnPhoneType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SyncFolderHierarchyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SyncFolderHierarchy")
    public JAXBElement<SyncFolderHierarchyType> createSyncFolderHierarchy(SyncFolderHierarchyType value) {
        return new JAXBElement<SyncFolderHierarchyType>(_SyncFolderHierarchy_QNAME, SyncFolderHierarchyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsubscribeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "Unsubscribe")
    public JAXBElement<UnsubscribeType> createUnsubscribe(UnsubscribeType value) {
        return new JAXBElement<UnsubscribeType>(_Unsubscribe_QNAME, UnsubscribeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAttachmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetAttachment")
    public JAXBElement<GetAttachmentType> createGetAttachment(GetAttachmentType value) {
        return new JAXBElement<GetAttachmentType>(_GetAttachment_QNAME, GetAttachmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefreshSharingFolderResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "RefreshSharingFolderResponse")
    public JAXBElement<RefreshSharingFolderResponseMessageType> createRefreshSharingFolderResponse(RefreshSharingFolderResponseMessageType value) {
        return new JAXBElement<RefreshSharingFolderResponseMessageType>(_RefreshSharingFolderResponse_QNAME, RefreshSharingFolderResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDelegateType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetDelegate")
    public JAXBElement<GetDelegateType> createGetDelegate(GetDelegateType value) {
        return new JAXBElement<GetDelegateType>(_GetDelegate_QNAME, GetDelegateType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EmptyFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "EmptyFolderResponse")
    public JAXBElement<EmptyFolderResponseType> createEmptyFolderResponse(EmptyFolderResponseType value) {
        return new JAXBElement<EmptyFolderResponseType>(_EmptyFolderResponse_QNAME, EmptyFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindItemResponse")
    public JAXBElement<FindItemResponseType> createFindItemResponse(FindItemResponseType value) {
        return new JAXBElement<FindItemResponseType>(_FindItemResponse_QNAME, FindItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MoveItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "MoveItem")
    public JAXBElement<MoveItemType> createMoveItem(MoveItemType value) {
        return new JAXBElement<MoveItemType>(_MoveItem_QNAME, MoveItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoveDelegateType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "RemoveDelegate")
    public JAXBElement<RemoveDelegateType> createRemoveDelegate(RemoveDelegateType value) {
        return new JAXBElement<RemoveDelegateType>(_RemoveDelegate_QNAME, RemoveDelegateType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResolveNamesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ResolveNamesResponse")
    public JAXBElement<ResolveNamesResponseType> createResolveNamesResponse(ResolveNamesResponseType value) {
        return new JAXBElement<ResolveNamesResponseType>(_ResolveNamesResponse_QNAME, ResolveNamesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRoomsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetRoomsResponse")
    public JAXBElement<GetRoomsResponseMessageType> createGetRoomsResponse(GetRoomsResponseMessageType value) {
        return new JAXBElement<GetRoomsResponseMessageType>(_GetRoomsResponse_QNAME, GetRoomsResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceConfigurationResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetServiceConfigurationResponse")
    public JAXBElement<GetServiceConfigurationResponseMessageType> createGetServiceConfigurationResponse(GetServiceConfigurationResponseMessageType value) {
        return new JAXBElement<GetServiceConfigurationResponseMessageType>(_GetServiceConfigurationResponse_QNAME, GetServiceConfigurationResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateFolder")
    public JAXBElement<UpdateFolderType> createUpdateFolder(UpdateFolderType value) {
        return new JAXBElement<UpdateFolderType>(_UpdateFolder_QNAME, UpdateFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindItem")
    public JAXBElement<FindItemType> createFindItem(FindItemType value) {
        return new JAXBElement<FindItemType>(_FindItem_QNAME, FindItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUserConfigurationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteUserConfigurationResponse")
    public JAXBElement<DeleteUserConfigurationResponseType> createDeleteUserConfigurationResponse(DeleteUserConfigurationResponseType value) {
        return new JAXBElement<DeleteUserConfigurationResponseType>(_DeleteUserConfigurationResponse_QNAME, DeleteUserConfigurationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CopyFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CopyFolder")
    public JAXBElement<CopyFolderType> createCopyFolder(CopyFolderType value) {
        return new JAXBElement<CopyFolderType>(_CopyFolder_QNAME, CopyFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEventsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetEvents")
    public JAXBElement<GetEventsType> createGetEvents(GetEventsType value) {
        return new JAXBElement<GetEventsType>(_GetEvents_QNAME, GetEventsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindFolderResponse")
    public JAXBElement<FindFolderResponseType> createFindFolderResponse(FindFolderResponseType value) {
        return new JAXBElement<FindFolderResponseType>(_FindFolderResponse_QNAME, FindFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CopyItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CopyItemResponse")
    public JAXBElement<CopyItemResponseType> createCopyItemResponse(CopyItemResponseType value) {
        return new JAXBElement<CopyItemResponseType>(_CopyItemResponse_QNAME, CopyItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMessageTrackingReportResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetMessageTrackingReportResponse")
    public JAXBElement<GetMessageTrackingReportResponseMessageType> createGetMessageTrackingReportResponse(GetMessageTrackingReportResponseMessageType value) {
        return new JAXBElement<GetMessageTrackingReportResponseMessageType>(_GetMessageTrackingReportResponse_QNAME, GetMessageTrackingReportResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateUserConfigurationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateUserConfiguration")
    public JAXBElement<UpdateUserConfigurationType> createUpdateUserConfiguration(UpdateUserConfigurationType value) {
        return new JAXBElement<UpdateUserConfigurationType>(_UpdateUserConfiguration_QNAME, UpdateUserConfigurationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindMailboxStatisticsByKeywordsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindMailboxStatisticsByKeywords")
    public JAXBElement<FindMailboxStatisticsByKeywordsType> createFindMailboxStatisticsByKeywords(FindMailboxStatisticsByKeywordsType value) {
        return new JAXBElement<FindMailboxStatisticsByKeywordsType>(_FindMailboxStatisticsByKeywords_QNAME, FindMailboxStatisticsByKeywordsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscribeResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SubscribeResponse")
    public JAXBElement<SubscribeResponseType> createSubscribeResponse(SubscribeResponseType value) {
        return new JAXBElement<SubscribeResponseType>(_SubscribeResponse_QNAME, SubscribeResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMessageTrackingReportRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetMessageTrackingReport")
    public JAXBElement<GetMessageTrackingReportRequestType> createGetMessageTrackingReport(GetMessageTrackingReportRequestType value) {
        return new JAXBElement<GetMessageTrackingReportRequestType>(_GetMessageTrackingReport_QNAME, GetMessageTrackingReportRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRoomListsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetRoomListsResponse")
    public JAXBElement<GetRoomListsResponseMessageType> createGetRoomListsResponse(GetRoomListsResponseMessageType value) {
        return new JAXBElement<GetRoomListsResponseMessageType>(_GetRoomListsResponse_QNAME, GetRoomListsResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteItemResponse")
    public JAXBElement<DeleteItemResponseType> createDeleteItemResponse(DeleteItemResponseType value) {
        return new JAXBElement<DeleteItemResponseType>(_DeleteItemResponse_QNAME, DeleteItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateAttachmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateAttachment")
    public JAXBElement<CreateAttachmentType> createCreateAttachment(CreateAttachmentType value) {
        return new JAXBElement<CreateAttachmentType>(_CreateAttachment_QNAME, CreateAttachmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetItemResponse")
    public JAXBElement<GetItemResponseType> createGetItemResponse(GetItemResponseType value) {
        return new JAXBElement<GetItemResponseType>(_GetItemResponse_QNAME, GetItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetUserOofSettingsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SetUserOofSettingsRequest")
    public JAXBElement<SetUserOofSettingsRequest> createSetUserOofSettingsRequest(SetUserOofSettingsRequest value) {
        return new JAXBElement<SetUserOofSettingsRequest>(_SetUserOofSettingsRequest_QNAME, SetUserOofSettingsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExpandDLType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ExpandDL")
    public JAXBElement<ExpandDLType> createExpandDL(ExpandDLType value) {
        return new JAXBElement<ExpandDLType>(_ExpandDL_QNAME, ExpandDLType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CopyItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CopyItem")
    public JAXBElement<CopyItemType> createCopyItem(CopyItemType value) {
        return new JAXBElement<CopyItemType>(_CopyItem_QNAME, CopyItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateManagedFolderRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateManagedFolder")
    public JAXBElement<CreateManagedFolderRequestType> createCreateManagedFolder(CreateManagedFolderRequestType value) {
        return new JAXBElement<CreateManagedFolderRequestType>(_CreateManagedFolder_QNAME, CreateManagedFolderRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSharingFolderResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetSharingFolderResponse")
    public JAXBElement<GetSharingFolderResponseMessageType> createGetSharingFolderResponse(GetSharingFolderResponseMessageType value) {
        return new JAXBElement<GetSharingFolderResponseMessageType>(_GetSharingFolderResponse_QNAME, GetSharingFolderResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindMailboxStatisticsByKeywordsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindMailboxStatisticsByKeywordsResponse")
    public JAXBElement<FindMailboxStatisticsByKeywordsResponseType> createFindMailboxStatisticsByKeywordsResponse(FindMailboxStatisticsByKeywordsResponseType value) {
        return new JAXBElement<FindMailboxStatisticsByKeywordsResponseType>(_FindMailboxStatisticsByKeywordsResponse_QNAME, FindMailboxStatisticsByKeywordsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStreamingEventsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetStreamingEvents")
    public JAXBElement<GetStreamingEventsType> createGetStreamingEvents(GetStreamingEventsType value) {
        return new JAXBElement<GetStreamingEventsType>(_GetStreamingEvents_QNAME, GetStreamingEventsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUserConfigurationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteUserConfiguration")
    public JAXBElement<DeleteUserConfigurationType> createDeleteUserConfiguration(DeleteUserConfigurationType value) {
        return new JAXBElement<DeleteUserConfigurationType>(_DeleteUserConfiguration_QNAME, DeleteUserConfigurationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExportItemsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ExportItemsResponse")
    public JAXBElement<ExportItemsResponseType> createExportItemsResponse(ExportItemsResponseType value) {
        return new JAXBElement<ExportItemsResponseType>(_ExportItemsResponse_QNAME, ExportItemsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateInboxRulesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateInboxRulesResponse")
    public JAXBElement<UpdateInboxRulesResponseType> createUpdateInboxRulesResponse(UpdateInboxRulesResponseType value) {
        return new JAXBElement<UpdateInboxRulesResponseType>(_UpdateInboxRulesResponse_QNAME, UpdateInboxRulesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetFolderResponse")
    public JAXBElement<GetFolderResponseType> createGetFolderResponse(GetFolderResponseType value) {
        return new JAXBElement<GetFolderResponseType>(_GetFolderResponse_QNAME, GetFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DisconnectPhoneCallType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DisconnectPhoneCall")
    public JAXBElement<DisconnectPhoneCallType> createDisconnectPhoneCall(DisconnectPhoneCallType value) {
        return new JAXBElement<DisconnectPhoneCallType>(_DisconnectPhoneCall_QNAME, DisconnectPhoneCallType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SyncFolderItemsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SyncFolderItemsResponse")
    public JAXBElement<SyncFolderItemsResponseType> createSyncFolderItemsResponse(SyncFolderItemsResponseType value) {
        return new JAXBElement<SyncFolderItemsResponseType>(_SyncFolderItemsResponse_QNAME, SyncFolderItemsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSharingFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetSharingFolder")
    public JAXBElement<GetSharingFolderType> createGetSharingFolder(GetSharingFolderType value) {
        return new JAXBElement<GetSharingFolderType>(_GetSharingFolder_QNAME, GetSharingFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateUserConfigurationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateUserConfigurationResponse")
    public JAXBElement<UpdateUserConfigurationResponseType> createUpdateUserConfigurationResponse(UpdateUserConfigurationResponseType value) {
        return new JAXBElement<UpdateUserConfigurationResponseType>(_UpdateUserConfigurationResponse_QNAME, UpdateUserConfigurationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSharingMetadataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetSharingMetadata")
    public JAXBElement<GetSharingMetadataType> createGetSharingMetadata(GetSharingMetadataType value) {
        return new JAXBElement<GetSharingMetadataType>(_GetSharingMetadata_QNAME, GetSharingMetadataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DisconnectPhoneCallResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DisconnectPhoneCallResponse")
    public JAXBElement<DisconnectPhoneCallResponseMessageType> createDisconnectPhoneCallResponse(DisconnectPhoneCallResponseMessageType value) {
        return new JAXBElement<DisconnectPhoneCallResponseMessageType>(_DisconnectPhoneCallResponse_QNAME, DisconnectPhoneCallResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SendItem")
    public JAXBElement<SendItemType> createSendItem(SendItemType value) {
        return new JAXBElement<SendItemType>(_SendItem_QNAME, SendItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateItemResponse")
    public JAXBElement<CreateItemResponseType> createCreateItemResponse(CreateItemResponseType value) {
        return new JAXBElement<CreateItemResponseType>(_CreateItemResponse_QNAME, CreateItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExpandDLResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ExpandDLResponse")
    public JAXBElement<ExpandDLResponseType> createExpandDLResponse(ExpandDLResponseType value) {
        return new JAXBElement<ExpandDLResponseType>(_ExpandDLResponse_QNAME, ExpandDLResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoveDelegateResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "RemoveDelegateResponse")
    public JAXBElement<RemoveDelegateResponseMessageType> createRemoveDelegateResponse(RemoveDelegateResponseMessageType value) {
        return new JAXBElement<RemoveDelegateResponseMessageType>(_RemoveDelegateResponse_QNAME, RemoveDelegateResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRoomsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetRooms")
    public JAXBElement<GetRoomsType> createGetRooms(GetRoomsType value) {
        return new JAXBElement<GetRoomsType>(_GetRooms_QNAME, GetRoomsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPhoneCallInformationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetPhoneCallInformation")
    public JAXBElement<GetPhoneCallInformationType> createGetPhoneCallInformation(GetPhoneCallInformationType value) {
        return new JAXBElement<GetPhoneCallInformationType>(_GetPhoneCallInformation_QNAME, GetPhoneCallInformationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefreshSharingFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "RefreshSharingFolder")
    public JAXBElement<RefreshSharingFolderType> createRefreshSharingFolder(RefreshSharingFolderType value) {
        return new JAXBElement<RefreshSharingFolderType>(_RefreshSharingFolder_QNAME, RefreshSharingFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SyncFolderItemsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SyncFolderItems")
    public JAXBElement<SyncFolderItemsType> createSyncFolderItems(SyncFolderItemsType value) {
        return new JAXBElement<SyncFolderItemsType>(_SyncFolderItems_QNAME, SyncFolderItemsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPasswordExpirationDateResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetPasswordExpirationDateResponse")
    public JAXBElement<GetPasswordExpirationDateResponseMessageType> createGetPasswordExpirationDateResponse(GetPasswordExpirationDateResponseMessageType value) {
        return new JAXBElement<GetPasswordExpirationDateResponseMessageType>(_GetPasswordExpirationDateResponse_QNAME, GetPasswordExpirationDateResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SyncFolderHierarchyResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SyncFolderHierarchyResponse")
    public JAXBElement<SyncFolderHierarchyResponseType> createSyncFolderHierarchyResponse(SyncFolderHierarchyResponseType value) {
        return new JAXBElement<SyncFolderHierarchyResponseType>(_SyncFolderHierarchyResponse_QNAME, SyncFolderHierarchyResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendNotificationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SendNotification")
    public JAXBElement<SendNotificationResponseType> createSendNotification(SendNotificationResponseType value) {
        return new JAXBElement<SendNotificationResponseType>(_SendNotification_QNAME, SendNotificationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindMessageTrackingReportResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindMessageTrackingReportResponse")
    public JAXBElement<FindMessageTrackingReportResponseMessageType> createFindMessageTrackingReportResponse(FindMessageTrackingReportResponseMessageType value) {
        return new JAXBElement<FindMessageTrackingReportResponseMessageType>(_FindMessageTrackingReportResponse_QNAME, FindMessageTrackingReportResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPasswordExpirationDateType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetPasswordExpirationDate")
    public JAXBElement<GetPasswordExpirationDateType> createGetPasswordExpirationDate(GetPasswordExpirationDateType value) {
        return new JAXBElement<GetPasswordExpirationDateType>(_GetPasswordExpirationDate_QNAME, GetPasswordExpirationDateType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServerTimeZonesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetServerTimeZones")
    public JAXBElement<GetServerTimeZonesType> createGetServerTimeZones(GetServerTimeZonesType value) {
        return new JAXBElement<GetServerTimeZonesType>(_GetServerTimeZones_QNAME, GetServerTimeZonesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteFolderResponse")
    public JAXBElement<DeleteFolderResponseType> createDeleteFolderResponse(DeleteFolderResponseType value) {
        return new JAXBElement<DeleteFolderResponseType>(_DeleteFolderResponse_QNAME, DeleteFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateAttachmentResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateAttachmentResponse")
    public JAXBElement<CreateAttachmentResponseType> createCreateAttachmentResponse(CreateAttachmentResponseType value) {
        return new JAXBElement<CreateAttachmentResponseType>(_CreateAttachmentResponse_QNAME, CreateAttachmentResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserAvailabilityResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetUserAvailabilityResponse")
    public JAXBElement<GetUserAvailabilityResponseType> createGetUserAvailabilityResponse(GetUserAvailabilityResponseType value) {
        return new JAXBElement<GetUserAvailabilityResponseType>(_GetUserAvailabilityResponse_QNAME, GetUserAvailabilityResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateFolder")
    public JAXBElement<CreateFolderType> createCreateFolder(CreateFolderType value) {
        return new JAXBElement<CreateFolderType>(_CreateFolder_QNAME, CreateFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResolveNamesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ResolveNames")
    public JAXBElement<ResolveNamesType> createResolveNames(ResolveNamesType value) {
        return new JAXBElement<ResolveNamesType>(_ResolveNames_QNAME, ResolveNamesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindConversationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindConversation")
    public JAXBElement<FindConversationType> createFindConversation(FindConversationType value) {
        return new JAXBElement<FindConversationType>(_FindConversation_QNAME, FindConversationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAttachmentResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetAttachmentResponse")
    public JAXBElement<GetAttachmentResponseType> createGetAttachmentResponse(GetAttachmentResponseType value) {
        return new JAXBElement<GetAttachmentResponseType>(_GetAttachmentResponse_QNAME, GetAttachmentResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetItem")
    public JAXBElement<GetItemType> createGetItem(GetItemType value) {
        return new JAXBElement<GetItemType>(_GetItem_QNAME, GetItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDelegateResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetDelegateResponse")
    public JAXBElement<GetDelegateResponseMessageType> createGetDelegateResponse(GetDelegateResponseMessageType value) {
        return new JAXBElement<GetDelegateResponseMessageType>(_GetDelegateResponse_QNAME, GetDelegateResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateDelegateType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateDelegate")
    public JAXBElement<UpdateDelegateType> createUpdateDelegate(UpdateDelegateType value) {
        return new JAXBElement<UpdateDelegateType>(_UpdateDelegate_QNAME, UpdateDelegateType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ApplyConversationActionResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ApplyConversationActionResponse")
    public JAXBElement<ApplyConversationActionResponseType> createApplyConversationActionResponse(ApplyConversationActionResponseType value) {
        return new JAXBElement<ApplyConversationActionResponseType>(_ApplyConversationActionResponse_QNAME, ApplyConversationActionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPhoneCallInformationResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetPhoneCallInformationResponse")
    public JAXBElement<GetPhoneCallInformationResponseMessageType> createGetPhoneCallInformationResponse(GetPhoneCallInformationResponseMessageType value) {
        return new JAXBElement<GetPhoneCallInformationResponseMessageType>(_GetPhoneCallInformationResponse_QNAME, GetPhoneCallInformationResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddDelegateType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "AddDelegate")
    public JAXBElement<AddDelegateType> createAddDelegate(AddDelegateType value) {
        return new JAXBElement<AddDelegateType>(_AddDelegate_QNAME, AddDelegateType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscribeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "Subscribe")
    public JAXBElement<SubscribeType> createSubscribe(SubscribeType value) {
        return new JAXBElement<SubscribeType>(_Subscribe_QNAME, SubscribeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserConfigurationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetUserConfigurationResponse")
    public JAXBElement<GetUserConfigurationResponseType> createGetUserConfigurationResponse(GetUserConfigurationResponseType value) {
        return new JAXBElement<GetUserConfigurationResponseType>(_GetUserConfigurationResponse_QNAME, GetUserConfigurationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateItem")
    public JAXBElement<CreateItemType> createCreateItem(CreateItemType value) {
        return new JAXBElement<CreateItemType>(_CreateItem_QNAME, CreateItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetFolder")
    public JAXBElement<GetFolderType> createGetFolder(GetFolderType value) {
        return new JAXBElement<GetFolderType>(_GetFolder_QNAME, GetFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MoveItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "MoveItemResponse")
    public JAXBElement<MoveItemResponseType> createMoveItemResponse(MoveItemResponseType value) {
        return new JAXBElement<MoveItemResponseType>(_MoveItemResponse_QNAME, MoveItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteAttachmentResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteAttachmentResponse")
    public JAXBElement<DeleteAttachmentResponseType> createDeleteAttachmentResponse(DeleteAttachmentResponseType value) {
        return new JAXBElement<DeleteAttachmentResponseType>(_DeleteAttachmentResponse_QNAME, DeleteAttachmentResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SendItemResponse")
    public JAXBElement<SendItemResponseType> createSendItemResponse(SendItemResponseType value) {
        return new JAXBElement<SendItemResponseType>(_SendItemResponse_QNAME, SendItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateInboxRulesRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateInboxRules")
    public JAXBElement<UpdateInboxRulesRequestType> createUpdateInboxRules(UpdateInboxRulesRequestType value) {
        return new JAXBElement<UpdateInboxRulesRequestType>(_UpdateInboxRules_QNAME, UpdateInboxRulesRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindFolder")
    public JAXBElement<FindFolderType> createFindFolder(FindFolderType value) {
        return new JAXBElement<FindFolderType>(_FindFolder_QNAME, FindFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetInboxRulesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetInboxRulesResponse")
    public JAXBElement<GetInboxRulesResponseType> createGetInboxRulesResponse(GetInboxRulesResponseType value) {
        return new JAXBElement<GetInboxRulesResponseType>(_GetInboxRulesResponse_QNAME, GetInboxRulesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PlayOnPhoneResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "PlayOnPhoneResponse")
    public JAXBElement<PlayOnPhoneResponseMessageType> createPlayOnPhoneResponse(PlayOnPhoneResponseMessageType value) {
        return new JAXBElement<PlayOnPhoneResponseMessageType>(_PlayOnPhoneResponse_QNAME, PlayOnPhoneResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateUserConfigurationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateUserConfigurationResponse")
    public JAXBElement<CreateUserConfigurationResponseType> createCreateUserConfigurationResponse(CreateUserConfigurationResponseType value) {
        return new JAXBElement<CreateUserConfigurationResponseType>(_CreateUserConfigurationResponse_QNAME, CreateUserConfigurationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteFolder")
    public JAXBElement<DeleteFolderType> createDeleteFolder(DeleteFolderType value) {
        return new JAXBElement<DeleteFolderType>(_DeleteFolder_QNAME, DeleteFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertIdType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ConvertId")
    public JAXBElement<ConvertIdType> createConvertId(ConvertIdType value) {
        return new JAXBElement<ConvertIdType>(_ConvertId_QNAME, ConvertIdType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServerTimeZonesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetServerTimeZonesResponse")
    public JAXBElement<GetServerTimeZonesResponseType> createGetServerTimeZonesResponse(GetServerTimeZonesResponseType value) {
        return new JAXBElement<GetServerTimeZonesResponseType>(_GetServerTimeZonesResponse_QNAME, GetServerTimeZonesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadItemsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UploadItems")
    public JAXBElement<UploadItemsType> createUploadItems(UploadItemsType value) {
        return new JAXBElement<UploadItemsType>(_UploadItems_QNAME, UploadItemsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateDelegateResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateDelegateResponse")
    public JAXBElement<UpdateDelegateResponseMessageType> createUpdateDelegateResponse(UpdateDelegateResponseMessageType value) {
        return new JAXBElement<UpdateDelegateResponseMessageType>(_UpdateDelegateResponse_QNAME, UpdateDelegateResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserOofSettingsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetUserOofSettingsRequest")
    public JAXBElement<GetUserOofSettingsRequest> createGetUserOofSettingsRequest(GetUserOofSettingsRequest value) {
        return new JAXBElement<GetUserOofSettingsRequest>(_GetUserOofSettingsRequest_QNAME, GetUserOofSettingsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateFolderResponse")
    public JAXBElement<UpdateFolderResponseType> createUpdateFolderResponse(UpdateFolderResponseType value) {
        return new JAXBElement<UpdateFolderResponseType>(_UpdateFolderResponse_QNAME, UpdateFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetInboxRulesRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetInboxRules")
    public JAXBElement<GetInboxRulesRequestType> createGetInboxRules(GetInboxRulesRequestType value) {
        return new JAXBElement<GetInboxRulesRequestType>(_GetInboxRules_QNAME, GetInboxRulesRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateItemResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateItemResponse")
    public JAXBElement<UpdateItemResponseType> createUpdateItemResponse(UpdateItemResponseType value) {
        return new JAXBElement<UpdateItemResponseType>(_UpdateItemResponse_QNAME, UpdateItemResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMailTipsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetMailTipsResponse")
    public JAXBElement<GetMailTipsResponseMessageType> createGetMailTipsResponse(GetMailTipsResponseMessageType value) {
        return new JAXBElement<GetMailTipsResponseMessageType>(_GetMailTipsResponse_QNAME, GetMailTipsResponseMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateManagedFolderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateManagedFolderResponse")
    public JAXBElement<CreateManagedFolderResponseType> createCreateManagedFolderResponse(CreateManagedFolderResponseType value) {
        return new JAXBElement<CreateManagedFolderResponseType>(_CreateManagedFolderResponse_QNAME, CreateManagedFolderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRoomListsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetRoomLists")
    public JAXBElement<GetRoomListsType> createGetRoomLists(GetRoomListsType value) {
        return new JAXBElement<GetRoomListsType>(_GetRoomLists_QNAME, GetRoomListsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateItemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateItem")
    public JAXBElement<UpdateItemType> createUpdateItem(UpdateItemType value) {
        return new JAXBElement<UpdateItemType>(_UpdateItem_QNAME, UpdateItemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEventsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetEventsResponse")
    public JAXBElement<GetEventsResponseType> createGetEventsResponse(GetEventsResponseType value) {
        return new JAXBElement<GetEventsResponseType>(_GetEventsResponse_QNAME, GetEventsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertIdResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ConvertIdResponse")
    public JAXBElement<ConvertIdResponseType> createConvertIdResponse(ConvertIdResponseType value) {
        return new JAXBElement<ConvertIdResponseType>(_ConvertIdResponse_QNAME, ConvertIdResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateUserConfigurationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateUserConfiguration")
    public JAXBElement<CreateUserConfigurationType> createCreateUserConfiguration(CreateUserConfigurationType value) {
        return new JAXBElement<CreateUserConfigurationType>(_CreateUserConfiguration_QNAME, CreateUserConfigurationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MoveFolderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "MoveFolder")
    public JAXBElement<MoveFolderType> createMoveFolder(MoveFolderType value) {
        return new JAXBElement<MoveFolderType>(_MoveFolder_QNAME, MoveFolderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStreamingEventsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetStreamingEventsResponse")
    public JAXBElement<GetStreamingEventsResponseType> createGetStreamingEventsResponse(GetStreamingEventsResponseType value) {
        return new JAXBElement<GetStreamingEventsResponseType>(_GetStreamingEventsResponse_QNAME, GetStreamingEventsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateUserConfigurationResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeCreateUserConfigurationResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeCreateUserConfigurationResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscribeResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SubscribeResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<SubscribeResponseMessageType> createArrayOfResponseMessagesTypeSubscribeResponseMessage(SubscribeResponseMessageType value) {
        return new JAXBElement<SubscribeResponseMessageType>(_ArrayOfResponseMessagesTypeSubscribeResponseMessage_QNAME, SubscribeResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindItemResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FindItemResponseMessageType> createArrayOfResponseMessagesTypeFindItemResponseMessage(FindItemResponseMessageType value) {
        return new JAXBElement<FindItemResponseMessageType>(_ArrayOfResponseMessagesTypeFindItemResponseMessage_QNAME, FindItemResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResolveNamesResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ResolveNamesResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResolveNamesResponseMessageType> createArrayOfResponseMessagesTypeResolveNamesResponseMessage(ResolveNamesResponseMessageType value) {
        return new JAXBElement<ResolveNamesResponseMessageType>(_ArrayOfResponseMessagesTypeResolveNamesResponseMessage_QNAME, ResolveNamesResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserConfigurationResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetUserConfigurationResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetUserConfigurationResponseMessageType> createArrayOfResponseMessagesTypeGetUserConfigurationResponseMessage(GetUserConfigurationResponseMessageType value) {
        return new JAXBElement<GetUserConfigurationResponseMessageType>(_ArrayOfResponseMessagesTypeGetUserConfigurationResponseMessage_QNAME, GetUserConfigurationResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteAttachmentResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteAttachmentResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<DeleteAttachmentResponseMessageType> createArrayOfResponseMessagesTypeDeleteAttachmentResponseMessage(DeleteAttachmentResponseMessageType value) {
        return new JAXBElement<DeleteAttachmentResponseMessageType>(_ArrayOfResponseMessagesTypeDeleteAttachmentResponseMessage_QNAME, DeleteAttachmentResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ApplyConversationActionResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeApplyConversationActionResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeApplyConversationActionResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExportItemsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ExportItemsResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ExportItemsResponseMessageType> createArrayOfResponseMessagesTypeExportItemsResponseMessage(ExportItemsResponseMessageType value) {
        return new JAXBElement<ExportItemsResponseMessageType>(_ArrayOfResponseMessagesTypeExportItemsResponseMessage_QNAME, ExportItemsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FolderInfoResponseMessageType> createArrayOfResponseMessagesTypeUpdateFolderResponseMessage(FolderInfoResponseMessageType value) {
        return new JAXBElement<FolderInfoResponseMessageType>(_ArrayOfResponseMessagesTypeUpdateFolderResponseMessage_QNAME, FolderInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CopyFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FolderInfoResponseMessageType> createArrayOfResponseMessagesTypeCopyFolderResponseMessage(FolderInfoResponseMessageType value) {
        return new JAXBElement<FolderInfoResponseMessageType>(_ArrayOfResponseMessagesTypeCopyFolderResponseMessage_QNAME, FolderInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertIdResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ConvertIdResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ConvertIdResponseMessageType> createArrayOfResponseMessagesTypeConvertIdResponseMessage(ConvertIdResponseMessageType value) {
        return new JAXBElement<ConvertIdResponseMessageType>(_ArrayOfResponseMessagesTypeConvertIdResponseMessage_QNAME, ConvertIdResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FolderInfoResponseMessageType> createArrayOfResponseMessagesTypeCreateFolderResponseMessage(FolderInfoResponseMessageType value) {
        return new JAXBElement<FolderInfoResponseMessageType>(_ArrayOfResponseMessagesTypeCreateFolderResponseMessage_QNAME, FolderInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateItemResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<UpdateItemResponseMessageType> createArrayOfResponseMessagesTypeUpdateItemResponseMessage(UpdateItemResponseMessageType value) {
        return new JAXBElement<UpdateItemResponseMessageType>(_ArrayOfResponseMessagesTypeUpdateItemResponseMessage_QNAME, UpdateItemResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ItemInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CopyItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ItemInfoResponseMessageType> createArrayOfResponseMessagesTypeCopyItemResponseMessage(ItemInfoResponseMessageType value) {
        return new JAXBElement<ItemInfoResponseMessageType>(_ArrayOfResponseMessagesTypeCopyItemResponseMessage_QNAME, ItemInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SyncFolderItemsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SyncFolderItemsResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<SyncFolderItemsResponseMessageType> createArrayOfResponseMessagesTypeSyncFolderItemsResponseMessage(SyncFolderItemsResponseMessageType value) {
        return new JAXBElement<SyncFolderItemsResponseMessageType>(_ArrayOfResponseMessagesTypeSyncFolderItemsResponseMessage_QNAME, SyncFolderItemsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStreamingEventsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetStreamingEventsResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetStreamingEventsResponseMessageType> createArrayOfResponseMessagesTypeGetStreamingEventsResponseMessage(GetStreamingEventsResponseMessageType value) {
        return new JAXBElement<GetStreamingEventsResponseMessageType>(_ArrayOfResponseMessagesTypeGetStreamingEventsResponseMessage_QNAME, GetStreamingEventsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPasswordExpirationDateResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetPasswordExpirationDateResponse", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetPasswordExpirationDateResponseMessageType> createArrayOfResponseMessagesTypeGetPasswordExpirationDateResponse(GetPasswordExpirationDateResponseMessageType value) {
        return new JAXBElement<GetPasswordExpirationDateResponseMessageType>(_GetPasswordExpirationDateResponse_QNAME, GetPasswordExpirationDateResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefreshSharingFolderResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "RefreshSharingFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<RefreshSharingFolderResponseMessageType> createArrayOfResponseMessagesTypeRefreshSharingFolderResponseMessage(RefreshSharingFolderResponseMessageType value) {
        return new JAXBElement<RefreshSharingFolderResponseMessageType>(_ArrayOfResponseMessagesTypeRefreshSharingFolderResponseMessage_QNAME, RefreshSharingFolderResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendNotificationResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SendNotificationResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<SendNotificationResponseMessageType> createArrayOfResponseMessagesTypeSendNotificationResponseMessage(SendNotificationResponseMessageType value) {
        return new JAXBElement<SendNotificationResponseMessageType>(_ArrayOfResponseMessagesTypeSendNotificationResponseMessage_QNAME, SendNotificationResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExpandDLResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ExpandDLResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ExpandDLResponseMessageType> createArrayOfResponseMessagesTypeExpandDLResponseMessage(ExpandDLResponseMessageType value) {
        return new JAXBElement<ExpandDLResponseMessageType>(_ArrayOfResponseMessagesTypeExpandDLResponseMessage_QNAME, ExpandDLResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetAttachmentResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<AttachmentInfoResponseMessageType> createArrayOfResponseMessagesTypeGetAttachmentResponseMessage(AttachmentInfoResponseMessageType value) {
        return new JAXBElement<AttachmentInfoResponseMessageType>(_ArrayOfResponseMessagesTypeGetAttachmentResponseMessage_QNAME, AttachmentInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FolderInfoResponseMessageType> createArrayOfResponseMessagesTypeGetFolderResponseMessage(FolderInfoResponseMessageType value) {
        return new JAXBElement<FolderInfoResponseMessageType>(_ArrayOfResponseMessagesTypeGetFolderResponseMessage_QNAME, FolderInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "EmptyFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeEmptyFolderResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeEmptyFolderResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadItemsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UploadItemsResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<UploadItemsResponseMessageType> createArrayOfResponseMessagesTypeUploadItemsResponseMessage(UploadItemsResponseMessageType value) {
        return new JAXBElement<UploadItemsResponseMessageType>(_ArrayOfResponseMessagesTypeUploadItemsResponseMessage_QNAME, UploadItemsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRoomListsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetRoomListsResponse", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetRoomListsResponseMessageType> createArrayOfResponseMessagesTypeGetRoomListsResponse(GetRoomListsResponseMessageType value) {
        return new JAXBElement<GetRoomListsResponseMessageType>(_GetRoomListsResponse_QNAME, GetRoomListsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ItemInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ItemInfoResponseMessageType> createArrayOfResponseMessagesTypeGetItemResponseMessage(ItemInfoResponseMessageType value) {
        return new JAXBElement<ItemInfoResponseMessageType>(_ArrayOfResponseMessagesTypeGetItemResponseMessage_QNAME, ItemInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UpdateUserConfigurationResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeUpdateUserConfigurationResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeUpdateUserConfigurationResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSharingMetadataResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetSharingMetadataResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetSharingMetadataResponseMessageType> createArrayOfResponseMessagesTypeGetSharingMetadataResponseMessage(GetSharingMetadataResponseMessageType value) {
        return new JAXBElement<GetSharingMetadataResponseMessageType>(_ArrayOfResponseMessagesTypeGetSharingMetadataResponseMessage_QNAME, GetSharingMetadataResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SyncFolderHierarchyResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SyncFolderHierarchyResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<SyncFolderHierarchyResponseMessageType> createArrayOfResponseMessagesTypeSyncFolderHierarchyResponseMessage(SyncFolderHierarchyResponseMessageType value) {
        return new JAXBElement<SyncFolderHierarchyResponseMessageType>(_ArrayOfResponseMessagesTypeSyncFolderHierarchyResponseMessage_QNAME, SyncFolderHierarchyResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "MoveFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FolderInfoResponseMessageType> createArrayOfResponseMessagesTypeMoveFolderResponseMessage(FolderInfoResponseMessageType value) {
        return new JAXBElement<FolderInfoResponseMessageType>(_ArrayOfResponseMessagesTypeMoveFolderResponseMessage_QNAME, FolderInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ItemInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "MoveItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ItemInfoResponseMessageType> createArrayOfResponseMessagesTypeMoveItemResponseMessage(ItemInfoResponseMessageType value) {
        return new JAXBElement<ItemInfoResponseMessageType>(_ArrayOfResponseMessagesTypeMoveItemResponseMessage_QNAME, ItemInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindFolderResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FindFolderResponseMessageType> createArrayOfResponseMessagesTypeFindFolderResponseMessage(FindFolderResponseMessageType value) {
        return new JAXBElement<FindFolderResponseMessageType>(_ArrayOfResponseMessagesTypeFindFolderResponseMessage_QNAME, FindFolderResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSharingFolderResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetSharingFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetSharingFolderResponseMessageType> createArrayOfResponseMessagesTypeGetSharingFolderResponseMessage(GetSharingFolderResponseMessageType value) {
        return new JAXBElement<GetSharingFolderResponseMessageType>(_ArrayOfResponseMessagesTypeGetSharingFolderResponseMessage_QNAME, GetSharingFolderResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServerTimeZonesResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetServerTimeZonesResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetServerTimeZonesResponseMessageType> createArrayOfResponseMessagesTypeGetServerTimeZonesResponseMessage(GetServerTimeZonesResponseMessageType value) {
        return new JAXBElement<GetServerTimeZonesResponseMessageType>(_ArrayOfResponseMessagesTypeGetServerTimeZonesResponseMessage_QNAME, GetServerTimeZonesResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ItemInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ItemInfoResponseMessageType> createArrayOfResponseMessagesTypeCreateItemResponseMessage(ItemInfoResponseMessageType value) {
        return new JAXBElement<ItemInfoResponseMessageType>(_ArrayOfResponseMessagesTypeCreateItemResponseMessage_QNAME, ItemInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "UnsubscribeResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeUnsubscribeResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeUnsubscribeResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateManagedFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FolderInfoResponseMessageType> createArrayOfResponseMessagesTypeCreateManagedFolderResponseMessage(FolderInfoResponseMessageType value) {
        return new JAXBElement<FolderInfoResponseMessageType>(_ArrayOfResponseMessagesTypeCreateManagedFolderResponseMessage_QNAME, FolderInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentInfoResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "CreateAttachmentResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<AttachmentInfoResponseMessageType> createArrayOfResponseMessagesTypeCreateAttachmentResponseMessage(AttachmentInfoResponseMessageType value) {
        return new JAXBElement<AttachmentInfoResponseMessageType>(_ArrayOfResponseMessagesTypeCreateAttachmentResponseMessage_QNAME, AttachmentInfoResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeDeleteItemResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeDeleteItemResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteFolderResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeDeleteFolderResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeDeleteFolderResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRoomsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetRoomsResponse", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetRoomsResponseMessageType> createArrayOfResponseMessagesTypeGetRoomsResponse(GetRoomsResponseMessageType value) {
        return new JAXBElement<GetRoomsResponseMessageType>(_GetRoomsResponse_QNAME, GetRoomsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindMailboxStatisticsByKeywordsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "FindMailboxStatisticsByKeywordsResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<FindMailboxStatisticsByKeywordsResponseMessageType> createArrayOfResponseMessagesTypeFindMailboxStatisticsByKeywordsResponseMessage(FindMailboxStatisticsByKeywordsResponseMessageType value) {
        return new JAXBElement<FindMailboxStatisticsByKeywordsResponseMessageType>(_ArrayOfResponseMessagesTypeFindMailboxStatisticsByKeywordsResponseMessage_QNAME, FindMailboxStatisticsByKeywordsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "SendItemResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeSendItemResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeSendItemResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEventsResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "GetEventsResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<GetEventsResponseMessageType> createArrayOfResponseMessagesTypeGetEventsResponseMessage(GetEventsResponseMessageType value) {
        return new JAXBElement<GetEventsResponseMessageType>(_ArrayOfResponseMessagesTypeGetEventsResponseMessage_QNAME, GetEventsResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "DeleteUserConfigurationResponseMessage", scope = ArrayOfResponseMessagesType.class)
    public JAXBElement<ResponseMessageType> createArrayOfResponseMessagesTypeDeleteUserConfigurationResponseMessage(ResponseMessageType value) {
        return new JAXBElement<ResponseMessageType>(_ArrayOfResponseMessagesTypeDeleteUserConfigurationResponseMessage_QNAME, ResponseMessageType.class, ArrayOfResponseMessagesType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/exchange/services/2006/messages", name = "ConfigurationName", scope = ArrayOfServiceConfigurationType.class)
    public JAXBElement<List<String>> createArrayOfServiceConfigurationTypeConfigurationName(List<String> value) {
        return new JAXBElement<List<String>>(_ArrayOfServiceConfigurationTypeConfigurationName_QNAME, ((Class) List.class), ArrayOfServiceConfigurationType.class, ((List<String> ) value));
    }

}
