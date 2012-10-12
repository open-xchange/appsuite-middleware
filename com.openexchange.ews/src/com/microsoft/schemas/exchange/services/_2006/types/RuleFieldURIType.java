
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RuleFieldURIType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RuleFieldURIType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="RuleId"/>
 *     &lt;enumeration value="DisplayName"/>
 *     &lt;enumeration value="Priority"/>
 *     &lt;enumeration value="IsNotSupported"/>
 *     &lt;enumeration value="Actions"/>
 *     &lt;enumeration value="Condition:Categories"/>
 *     &lt;enumeration value="Condition:ContainsBodyStrings"/>
 *     &lt;enumeration value="Condition:ContainsHeaderStrings"/>
 *     &lt;enumeration value="Condition:ContainsRecipientStrings"/>
 *     &lt;enumeration value="Condition:ContainsSenderStrings"/>
 *     &lt;enumeration value="Condition:ContainsSubjectOrBodyStrings"/>
 *     &lt;enumeration value="Condition:ContainsSubjectStrings"/>
 *     &lt;enumeration value="Condition:FlaggedForAction"/>
 *     &lt;enumeration value="Condition:FromAddresses"/>
 *     &lt;enumeration value="Condition:FromConnectedAccounts"/>
 *     &lt;enumeration value="Condition:HasAttachments"/>
 *     &lt;enumeration value="Condition:Importance"/>
 *     &lt;enumeration value="Condition:IsApprovalRequest"/>
 *     &lt;enumeration value="Condition:IsAutomaticForward"/>
 *     &lt;enumeration value="Condition:IsAutomaticReply"/>
 *     &lt;enumeration value="Condition:IsEncrypted"/>
 *     &lt;enumeration value="Condition:IsMeetingRequest"/>
 *     &lt;enumeration value="Condition:IsMeetingResponse"/>
 *     &lt;enumeration value="Condition:IsNDR"/>
 *     &lt;enumeration value="Condition:IsPermissionControlled"/>
 *     &lt;enumeration value="Condition:IsReadReceipt"/>
 *     &lt;enumeration value="Condition:IsSigned"/>
 *     &lt;enumeration value="Condition:IsVoicemail"/>
 *     &lt;enumeration value="Condition:ItemClasses"/>
 *     &lt;enumeration value="Condition:MessageClassifications"/>
 *     &lt;enumeration value="Condition:NotSentToMe"/>
 *     &lt;enumeration value="Condition:SentCcMe"/>
 *     &lt;enumeration value="Condition:SentOnlyToMe"/>
 *     &lt;enumeration value="Condition:SentToAddresses"/>
 *     &lt;enumeration value="Condition:SentToMe"/>
 *     &lt;enumeration value="Condition:SentToOrCcMe"/>
 *     &lt;enumeration value="Condition:Sensitivity"/>
 *     &lt;enumeration value="Condition:WithinDateRange"/>
 *     &lt;enumeration value="Condition:WithinSizeRange"/>
 *     &lt;enumeration value="Exception:Categories"/>
 *     &lt;enumeration value="Exception:ContainsBodyStrings"/>
 *     &lt;enumeration value="Exception:ContainsHeaderStrings"/>
 *     &lt;enumeration value="Exception:ContainsRecipientStrings"/>
 *     &lt;enumeration value="Exception:ContainsSenderStrings"/>
 *     &lt;enumeration value="Exception:ContainsSubjectOrBodyStrings"/>
 *     &lt;enumeration value="Exception:ContainsSubjectStrings"/>
 *     &lt;enumeration value="Exception:FlaggedForAction"/>
 *     &lt;enumeration value="Exception:FromAddresses"/>
 *     &lt;enumeration value="Exception:FromConnectedAccounts"/>
 *     &lt;enumeration value="Exception:HasAttachments"/>
 *     &lt;enumeration value="Exception:Importance"/>
 *     &lt;enumeration value="Exception:IsApprovalRequest"/>
 *     &lt;enumeration value="Exception:IsAutomaticForward"/>
 *     &lt;enumeration value="Exception:IsAutomaticReply"/>
 *     &lt;enumeration value="Exception:IsEncrypted"/>
 *     &lt;enumeration value="Exception:IsMeetingRequest"/>
 *     &lt;enumeration value="Exception:IsMeetingResponse"/>
 *     &lt;enumeration value="Exception:IsNDR"/>
 *     &lt;enumeration value="Exception:IsPermissionControlled"/>
 *     &lt;enumeration value="Exception:IsReadReceipt"/>
 *     &lt;enumeration value="Exception:IsSigned"/>
 *     &lt;enumeration value="Exception:IsVoicemail"/>
 *     &lt;enumeration value="Exception:ItemClasses"/>
 *     &lt;enumeration value="Exception:MessageClassifications"/>
 *     &lt;enumeration value="Exception:NotSentToMe"/>
 *     &lt;enumeration value="Exception:SentCcMe"/>
 *     &lt;enumeration value="Exception:SentOnlyToMe"/>
 *     &lt;enumeration value="Exception:SentToAddresses"/>
 *     &lt;enumeration value="Exception:SentToMe"/>
 *     &lt;enumeration value="Exception:SentToOrCcMe"/>
 *     &lt;enumeration value="Exception:Sensitivity"/>
 *     &lt;enumeration value="Exception:WithinDateRange"/>
 *     &lt;enumeration value="Exception:WithinSizeRange"/>
 *     &lt;enumeration value="Action:AssignCategories"/>
 *     &lt;enumeration value="Action:CopyToFolder"/>
 *     &lt;enumeration value="Action:Delete"/>
 *     &lt;enumeration value="Action:ForwardAsAttachmentToRecipients"/>
 *     &lt;enumeration value="Action:ForwardToRecipients"/>
 *     &lt;enumeration value="Action:MarkImportance"/>
 *     &lt;enumeration value="Action:MarkAsRead"/>
 *     &lt;enumeration value="Action:MoveToFolder"/>
 *     &lt;enumeration value="Action:PermanentDelete"/>
 *     &lt;enumeration value="Action:RedirectToRecipients"/>
 *     &lt;enumeration value="Action:SendSMSAlertToRecipients"/>
 *     &lt;enumeration value="Action:ServerReplyWithMessage"/>
 *     &lt;enumeration value="Action:StopProcessingRules"/>
 *     &lt;enumeration value="IsEnabled"/>
 *     &lt;enumeration value="IsInError"/>
 *     &lt;enumeration value="Conditions"/>
 *     &lt;enumeration value="Exceptions"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RuleFieldURIType")
@XmlEnum
public enum RuleFieldURIType {

    @XmlEnumValue("RuleId")
    RULE_ID("RuleId"),
    @XmlEnumValue("DisplayName")
    DISPLAY_NAME("DisplayName"),
    @XmlEnumValue("Priority")
    PRIORITY("Priority"),
    @XmlEnumValue("IsNotSupported")
    IS_NOT_SUPPORTED("IsNotSupported"),
    @XmlEnumValue("Actions")
    ACTIONS("Actions"),
    @XmlEnumValue("Condition:Categories")
    CONDITION_CATEGORIES("Condition:Categories"),
    @XmlEnumValue("Condition:ContainsBodyStrings")
    CONDITION_CONTAINS_BODY_STRINGS("Condition:ContainsBodyStrings"),
    @XmlEnumValue("Condition:ContainsHeaderStrings")
    CONDITION_CONTAINS_HEADER_STRINGS("Condition:ContainsHeaderStrings"),
    @XmlEnumValue("Condition:ContainsRecipientStrings")
    CONDITION_CONTAINS_RECIPIENT_STRINGS("Condition:ContainsRecipientStrings"),
    @XmlEnumValue("Condition:ContainsSenderStrings")
    CONDITION_CONTAINS_SENDER_STRINGS("Condition:ContainsSenderStrings"),
    @XmlEnumValue("Condition:ContainsSubjectOrBodyStrings")
    CONDITION_CONTAINS_SUBJECT_OR_BODY_STRINGS("Condition:ContainsSubjectOrBodyStrings"),
    @XmlEnumValue("Condition:ContainsSubjectStrings")
    CONDITION_CONTAINS_SUBJECT_STRINGS("Condition:ContainsSubjectStrings"),
    @XmlEnumValue("Condition:FlaggedForAction")
    CONDITION_FLAGGED_FOR_ACTION("Condition:FlaggedForAction"),
    @XmlEnumValue("Condition:FromAddresses")
    CONDITION_FROM_ADDRESSES("Condition:FromAddresses"),
    @XmlEnumValue("Condition:FromConnectedAccounts")
    CONDITION_FROM_CONNECTED_ACCOUNTS("Condition:FromConnectedAccounts"),
    @XmlEnumValue("Condition:HasAttachments")
    CONDITION_HAS_ATTACHMENTS("Condition:HasAttachments"),
    @XmlEnumValue("Condition:Importance")
    CONDITION_IMPORTANCE("Condition:Importance"),
    @XmlEnumValue("Condition:IsApprovalRequest")
    CONDITION_IS_APPROVAL_REQUEST("Condition:IsApprovalRequest"),
    @XmlEnumValue("Condition:IsAutomaticForward")
    CONDITION_IS_AUTOMATIC_FORWARD("Condition:IsAutomaticForward"),
    @XmlEnumValue("Condition:IsAutomaticReply")
    CONDITION_IS_AUTOMATIC_REPLY("Condition:IsAutomaticReply"),
    @XmlEnumValue("Condition:IsEncrypted")
    CONDITION_IS_ENCRYPTED("Condition:IsEncrypted"),
    @XmlEnumValue("Condition:IsMeetingRequest")
    CONDITION_IS_MEETING_REQUEST("Condition:IsMeetingRequest"),
    @XmlEnumValue("Condition:IsMeetingResponse")
    CONDITION_IS_MEETING_RESPONSE("Condition:IsMeetingResponse"),
    @XmlEnumValue("Condition:IsNDR")
    CONDITION_IS_NDR("Condition:IsNDR"),
    @XmlEnumValue("Condition:IsPermissionControlled")
    CONDITION_IS_PERMISSION_CONTROLLED("Condition:IsPermissionControlled"),
    @XmlEnumValue("Condition:IsReadReceipt")
    CONDITION_IS_READ_RECEIPT("Condition:IsReadReceipt"),
    @XmlEnumValue("Condition:IsSigned")
    CONDITION_IS_SIGNED("Condition:IsSigned"),
    @XmlEnumValue("Condition:IsVoicemail")
    CONDITION_IS_VOICEMAIL("Condition:IsVoicemail"),
    @XmlEnumValue("Condition:ItemClasses")
    CONDITION_ITEM_CLASSES("Condition:ItemClasses"),
    @XmlEnumValue("Condition:MessageClassifications")
    CONDITION_MESSAGE_CLASSIFICATIONS("Condition:MessageClassifications"),
    @XmlEnumValue("Condition:NotSentToMe")
    CONDITION_NOT_SENT_TO_ME("Condition:NotSentToMe"),
    @XmlEnumValue("Condition:SentCcMe")
    CONDITION_SENT_CC_ME("Condition:SentCcMe"),
    @XmlEnumValue("Condition:SentOnlyToMe")
    CONDITION_SENT_ONLY_TO_ME("Condition:SentOnlyToMe"),
    @XmlEnumValue("Condition:SentToAddresses")
    CONDITION_SENT_TO_ADDRESSES("Condition:SentToAddresses"),
    @XmlEnumValue("Condition:SentToMe")
    CONDITION_SENT_TO_ME("Condition:SentToMe"),
    @XmlEnumValue("Condition:SentToOrCcMe")
    CONDITION_SENT_TO_OR_CC_ME("Condition:SentToOrCcMe"),
    @XmlEnumValue("Condition:Sensitivity")
    CONDITION_SENSITIVITY("Condition:Sensitivity"),
    @XmlEnumValue("Condition:WithinDateRange")
    CONDITION_WITHIN_DATE_RANGE("Condition:WithinDateRange"),
    @XmlEnumValue("Condition:WithinSizeRange")
    CONDITION_WITHIN_SIZE_RANGE("Condition:WithinSizeRange"),
    @XmlEnumValue("Exception:Categories")
    EXCEPTION_CATEGORIES("Exception:Categories"),
    @XmlEnumValue("Exception:ContainsBodyStrings")
    EXCEPTION_CONTAINS_BODY_STRINGS("Exception:ContainsBodyStrings"),
    @XmlEnumValue("Exception:ContainsHeaderStrings")
    EXCEPTION_CONTAINS_HEADER_STRINGS("Exception:ContainsHeaderStrings"),
    @XmlEnumValue("Exception:ContainsRecipientStrings")
    EXCEPTION_CONTAINS_RECIPIENT_STRINGS("Exception:ContainsRecipientStrings"),
    @XmlEnumValue("Exception:ContainsSenderStrings")
    EXCEPTION_CONTAINS_SENDER_STRINGS("Exception:ContainsSenderStrings"),
    @XmlEnumValue("Exception:ContainsSubjectOrBodyStrings")
    EXCEPTION_CONTAINS_SUBJECT_OR_BODY_STRINGS("Exception:ContainsSubjectOrBodyStrings"),
    @XmlEnumValue("Exception:ContainsSubjectStrings")
    EXCEPTION_CONTAINS_SUBJECT_STRINGS("Exception:ContainsSubjectStrings"),
    @XmlEnumValue("Exception:FlaggedForAction")
    EXCEPTION_FLAGGED_FOR_ACTION("Exception:FlaggedForAction"),
    @XmlEnumValue("Exception:FromAddresses")
    EXCEPTION_FROM_ADDRESSES("Exception:FromAddresses"),
    @XmlEnumValue("Exception:FromConnectedAccounts")
    EXCEPTION_FROM_CONNECTED_ACCOUNTS("Exception:FromConnectedAccounts"),
    @XmlEnumValue("Exception:HasAttachments")
    EXCEPTION_HAS_ATTACHMENTS("Exception:HasAttachments"),
    @XmlEnumValue("Exception:Importance")
    EXCEPTION_IMPORTANCE("Exception:Importance"),
    @XmlEnumValue("Exception:IsApprovalRequest")
    EXCEPTION_IS_APPROVAL_REQUEST("Exception:IsApprovalRequest"),
    @XmlEnumValue("Exception:IsAutomaticForward")
    EXCEPTION_IS_AUTOMATIC_FORWARD("Exception:IsAutomaticForward"),
    @XmlEnumValue("Exception:IsAutomaticReply")
    EXCEPTION_IS_AUTOMATIC_REPLY("Exception:IsAutomaticReply"),
    @XmlEnumValue("Exception:IsEncrypted")
    EXCEPTION_IS_ENCRYPTED("Exception:IsEncrypted"),
    @XmlEnumValue("Exception:IsMeetingRequest")
    EXCEPTION_IS_MEETING_REQUEST("Exception:IsMeetingRequest"),
    @XmlEnumValue("Exception:IsMeetingResponse")
    EXCEPTION_IS_MEETING_RESPONSE("Exception:IsMeetingResponse"),
    @XmlEnumValue("Exception:IsNDR")
    EXCEPTION_IS_NDR("Exception:IsNDR"),
    @XmlEnumValue("Exception:IsPermissionControlled")
    EXCEPTION_IS_PERMISSION_CONTROLLED("Exception:IsPermissionControlled"),
    @XmlEnumValue("Exception:IsReadReceipt")
    EXCEPTION_IS_READ_RECEIPT("Exception:IsReadReceipt"),
    @XmlEnumValue("Exception:IsSigned")
    EXCEPTION_IS_SIGNED("Exception:IsSigned"),
    @XmlEnumValue("Exception:IsVoicemail")
    EXCEPTION_IS_VOICEMAIL("Exception:IsVoicemail"),
    @XmlEnumValue("Exception:ItemClasses")
    EXCEPTION_ITEM_CLASSES("Exception:ItemClasses"),
    @XmlEnumValue("Exception:MessageClassifications")
    EXCEPTION_MESSAGE_CLASSIFICATIONS("Exception:MessageClassifications"),
    @XmlEnumValue("Exception:NotSentToMe")
    EXCEPTION_NOT_SENT_TO_ME("Exception:NotSentToMe"),
    @XmlEnumValue("Exception:SentCcMe")
    EXCEPTION_SENT_CC_ME("Exception:SentCcMe"),
    @XmlEnumValue("Exception:SentOnlyToMe")
    EXCEPTION_SENT_ONLY_TO_ME("Exception:SentOnlyToMe"),
    @XmlEnumValue("Exception:SentToAddresses")
    EXCEPTION_SENT_TO_ADDRESSES("Exception:SentToAddresses"),
    @XmlEnumValue("Exception:SentToMe")
    EXCEPTION_SENT_TO_ME("Exception:SentToMe"),
    @XmlEnumValue("Exception:SentToOrCcMe")
    EXCEPTION_SENT_TO_OR_CC_ME("Exception:SentToOrCcMe"),
    @XmlEnumValue("Exception:Sensitivity")
    EXCEPTION_SENSITIVITY("Exception:Sensitivity"),
    @XmlEnumValue("Exception:WithinDateRange")
    EXCEPTION_WITHIN_DATE_RANGE("Exception:WithinDateRange"),
    @XmlEnumValue("Exception:WithinSizeRange")
    EXCEPTION_WITHIN_SIZE_RANGE("Exception:WithinSizeRange"),
    @XmlEnumValue("Action:AssignCategories")
    ACTION_ASSIGN_CATEGORIES("Action:AssignCategories"),
    @XmlEnumValue("Action:CopyToFolder")
    ACTION_COPY_TO_FOLDER("Action:CopyToFolder"),
    @XmlEnumValue("Action:Delete")
    ACTION_DELETE("Action:Delete"),
    @XmlEnumValue("Action:ForwardAsAttachmentToRecipients")
    ACTION_FORWARD_AS_ATTACHMENT_TO_RECIPIENTS("Action:ForwardAsAttachmentToRecipients"),
    @XmlEnumValue("Action:ForwardToRecipients")
    ACTION_FORWARD_TO_RECIPIENTS("Action:ForwardToRecipients"),
    @XmlEnumValue("Action:MarkImportance")
    ACTION_MARK_IMPORTANCE("Action:MarkImportance"),
    @XmlEnumValue("Action:MarkAsRead")
    ACTION_MARK_AS_READ("Action:MarkAsRead"),
    @XmlEnumValue("Action:MoveToFolder")
    ACTION_MOVE_TO_FOLDER("Action:MoveToFolder"),
    @XmlEnumValue("Action:PermanentDelete")
    ACTION_PERMANENT_DELETE("Action:PermanentDelete"),
    @XmlEnumValue("Action:RedirectToRecipients")
    ACTION_REDIRECT_TO_RECIPIENTS("Action:RedirectToRecipients"),
    @XmlEnumValue("Action:SendSMSAlertToRecipients")
    ACTION_SEND_SMS_ALERT_TO_RECIPIENTS("Action:SendSMSAlertToRecipients"),
    @XmlEnumValue("Action:ServerReplyWithMessage")
    ACTION_SERVER_REPLY_WITH_MESSAGE("Action:ServerReplyWithMessage"),
    @XmlEnumValue("Action:StopProcessingRules")
    ACTION_STOP_PROCESSING_RULES("Action:StopProcessingRules"),
    @XmlEnumValue("IsEnabled")
    IS_ENABLED("IsEnabled"),
    @XmlEnumValue("IsInError")
    IS_IN_ERROR("IsInError"),
    @XmlEnumValue("Conditions")
    CONDITIONS("Conditions"),
    @XmlEnumValue("Exceptions")
    EXCEPTIONS("Exceptions");
    private final String value;

    RuleFieldURIType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RuleFieldURIType fromValue(String v) {
        for (RuleFieldURIType c: RuleFieldURIType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
