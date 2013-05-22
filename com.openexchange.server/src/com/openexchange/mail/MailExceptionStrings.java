/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mail;

import static com.openexchange.i18n.TextPattern.LINE_SEPARATOR;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MailExceptionStrings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailExceptionStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link MailExceptionStrings}.
     */
    private MailExceptionStrings() {
        super();
    }

    // Displayed to user if a volatile error occurred.
    public static final String MAIL_MESSAGE_RETRY = "A messaging error occurred. Please try again later.";

    // Displayed to user if a permanent mail error occurred
    public static final String MAIL_MESSAGE = "A messaging error occurred.";

    /**
     * Unexpected error: %1$s
     */
    public final static String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s";

    /**
     * Missing parameter %1$s
     */
    public final static String MISSING_PARAMETER_MSG = "Missing parameter %1$s";

    /**
     * Invalid permission values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s
     */
    public final static String INVALID_PERMISSION_MSG = "Invalid permission values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s";

    /**
     * A JSON error occurred: %1$s
     */
    public final static String JSON_ERROR_MSG = "A JSON error occurred: %1$s";

    /**
     * Missing parameter in user's mail config: %1$s
     */
    public final static String MISSING_CONNECT_PARAM_MSG = "Missing parameter in user's mail config: %1$s";

    /**
     * Configuration error: %1$s
     */
    public final static String CONFIG_ERROR_MSG = "Configuration error: %1$s";

    /**
     * Invalid multipart content. Number of enclosed contents is 0
     */
    public final static String INVALID_MULTIPART_CONTENT_MSG = "Invalid multipart content. Number of enclosed contents is 0";

    /**
     * A part's content could not be read from message %1$s in mail folder %2$s
     */
    public final static String UNREADBALE_PART_CONTENT_MSG = "A part's content could not be read from message %1$s in mail folder %2$s";

    /**
     * An I/O error occurred: %1$s
     */
    public final static String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    /**
     * Invalid message path: %1$s
     */
    public final static String INVALID_MAIL_IDENTIFIER_MSG = "Invalid message path: %1$s";

    /**
     * Unknown color label index: %1$s
     */
    public final static String UNKNOWN_COLOR_LABEL_MSG = "Unknown color label: %1$s";

    /**
     * Cannot instantiate class %1$s.
     */
    public final static String INSTANTIATION_PROBLEM_MSG = "Cannot instantiate class %1$s.";

    /**
     * Cannot initialize mail module
     */
    public final static String INITIALIZATION_PROBLEM_MSG = "Cannot initialize mail module";

    /**
     * No mail module access permitted
     */
    public final static String NO_MAIL_ACCESS_MSG = "No mail module access permitted";

    /**
     * Mail account is disabled for admin user in context %1$s
     */
    public final static String ACCOUNT_DOES_NOT_EXIST_MSG = "Mail account is disabled for admin user in context %1$s";

    /**
     * Process was interrupted. Please try again.
     */
    public final static String INTERRUPT_ERROR_MSG = "Process was interrupted. Please try again.";

    /**
     * Unsupported charset-encoding: %1$s
     */
    public final static String ENCODING_ERROR_MSG = "Unsupported charset encoding: %1$s";

    /**
     * Header %1$s could not be properly parsed
     */
    public final static String HEADER_PARSE_ERROR_MSG = "Header %1$s could not be properly parsed";

    /**
     * Missing default %1$s folder in user mail settings
     */
    public final static String MISSING_DEFAULT_FOLDER_NAME_MSG = "Missing default %1$s folder in user mail settings";

    /**
     * Spam handler initialization failed: %1$s
     */
    public final static String SPAM_HANDLER_INIT_FAILED_MSG = "Spam handler initialization failed: %1$s";

    /**
     * Invalid Content-Type value: %1$s
     */
    public final static String INVALID_CONTENT_TYPE_MSG = "Invalid content type value: %1$s";

    /**
     * Messaging error: %1$s. TODO: Maybe change to: Broken/Bad message
     */
    public final static String MESSAGING_ERROR_MSG = "Messaging error: %1$s";

    /**
     * Message field %1$s cannot be handled
     */
    public final static String INVALID_FIELD_MSG = "Message field %1$s cannot be handled";

    /**
     * Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    public final static String INVALID_FIELD_EXT_MSG = "Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    /**
     * Versit error: %1$s
     */
    public final static String VERSIT_ERROR_MSG = "Versit error: %1$s";

    /**
     * No attachment was found with id %1$s in message
     */
    public final static String NO_ATTACHMENT_FOUND_MSG = "No attachment was found with id %1$s in message";

    /**
     * Versit attachment could not be saved due to an unsupported MIME type: %1$s
     */
    public final static String UNSUPPORTED_VERSIT_ATTACHMENT_MSG = "Versit attachment could not be saved due to an unsupported MIME type: %1$s";

    /**
     * Invalid parameter name: %1$s
     */
    public final static String INVALID_PARAMETER_MSG = "Invalid parameter name: %1$s";

    /**
     * Could not create a PartModifier instance from name %1$s
     */
    public final static String PART_MODIFIER_CREATION_FAILED_MSG = "Could not create a PartModifier instance from name %1$s";

    /**
     * Upload quota (%1$s) exceeded for file %2$s (size=%3$s)
     */
    public final static String UPLOAD_QUOTA_EXCEEDED_FOR_FILE_MSG = "Upload quota (%1$s) exceeded for file %2$s (size=%3$s)";

    /**
     * Upload quota (%1$s) exceeded
     */
    public final static String UPLOAD_QUOTA_EXCEEDED_MSG = "Upload quota (%1$s) exceeded";

    /**
     * Invalid integer value %1$s
     */
    public final static String INVALID_INT_VALUE_MSG = "Invalid integer value %1$s";

    /**
     * Mail(s) %1$s could not be found in folder %2$s
     */
    public final static String MAIL_NOT_FOUND_MSG = "Mail(s) %1$s could not be found in folder %2$s";

    // Mail could not be found
    public final static String MAIL_NOT_FOUND_SIMPLE_MSG = "Mail could not be found";

    /**
     * Action %1$s is not supported by %2$s
     */
    public final static String UNSUPPORTED_ACTION_MSG = "Action %1$s is not supported by %2$s";

    /**
     * Message could not be sent
     */
    public final static String SEND_FAILED_UNKNOWN_MSG = "Message could not be sent";

    /**
     * Unknown action: %1$s
     */
    public final static String UNKNOWN_ACTION_MSG = "Unknown or unsupported action: %1$s";

    /**
     * Missing field %1$s
     */
    public final static String MISSING_FIELD_MSG = "Missing field %1$s";

    /**
     * Unsupported MIME type %1$s
     */
    public final static String UNSUPPORTED_MIME_TYPE_MSG = "Unsupported MIME type %1$s";

    /**
     * This message could not be moved to trash folder as your mailbox is nearly full.<br>
     * Please try to empty your deleted items first, or delete smaller messages first.
     */
    public final static String DELETE_FAILED_OVER_QUOTA_MSG = "This message could not be moved to trash folder as your mailbox is nearly full."+LINE_SEPARATOR+"Please try to empty your deleted items first, or delete smaller messages first.";

    /**
     * The message part with sequence ID %1$s could not be found in message %2$s in folder %3$s
     */
    public final static String PART_NOT_FOUND_MSG = "The message part with sequence ID %1$s could not be found in message %2$s in folder %3$s.";

    /**
     * No content available in mail part
     */
    public final static String NO_CONTENT_MSG = "No content available in mail part";

    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder due to exceeded quota.
     */
    public final static String COPY_TO_SENT_FOLDER_FAILED_QUOTA_MSG = "Message has been successfully sent. Due to exceeded quota a copy could not be placed in your sent folder though.";

    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder
     */
    public final static String COPY_TO_SENT_FOLDER_FAILED_MSG = "Message has been successfully sent. A copy could not be placed in your sent folder though.";

    /**
     * No provider could be found for protocol/URL "%1$s"
     */
    public final static String UNKNOWN_PROTOCOL_MSG = "No provider could be found for protocol/URL \"%1$s\"";

    /**
     * Protocol cannot be parsed: %1$s
     */
    public final static String PROTOCOL_PARSE_ERROR_MSG = "Protocol cannot be parsed: %1$s";

    /**
     * Bad value %1$s in parameter %2$s
     */
    public final static String BAD_PARAM_VALUE_MSG = "Bad value %1$s in parameter %2$s";

    /**
     * No reply on multiple message possible
     */
    public final static String NO_MULTIPLE_REPLY_MSG = "No reply on multiple message possible";

    /**
     * legal system flag argument %1$s. Flag must be to the power of 2
     */
    public final static String ILLEGAL_FLAG_ARGUMENT_MSG = "Illegal system flag argument %1$s. Flag must be to the power of 2";

    /**
     * Attachment %1$s not found inside mail %2$s of mail folder %3$s
     */
    public final static String ATTACHMENT_NOT_FOUND_MSG = "Attachment %1$s not found inside mail %2$s of mail folder %3$s";

    /**
     * Folder %1$s does not hold messages and is therefore not selectable
     */
    public final static String FOLDER_DOES_NOT_HOLD_MESSAGES_MSG = "Folder %1$s does not hold messages and is therefore not selectable";

    /**
     * Folder %1$s does not hold messages and is therefore not selectable on server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    public final static String FOLDER_DOES_NOT_HOLD_MESSAGES_EXT_MSG = "Folder %1$s does not hold messages and is therefore not selectable on server %2$s with login %3$s (user=%4$s, context=%5$s)";

    /**
     * Insufficient folder attributes: Either existence status or fullname have to be present to determine if a mail folder create or update
     * shall be performed
     */
    public final static String INSUFFICIENT_FOLDER_ATTR_MSG = "Insufficient folder attributes: Either existence status or full name have to be present to determine if a mail folder create or update shall be performed";

    /**
     * Root folder must not be modified or deleted
     */
    public final static String NO_ROOT_FOLDER_MODIFY_DELETE_MSG = "Root folder must not be modified or deleted";

    /**
     * No transport provider could be found for protocol/URL "%1$s"
     */
    public final static String UNKNOWN_TRANSPORT_PROTOCOL_MSG = "No transport provider could be found for protocol/URL \"%1$s\"";

    /**
     * Missing mail folder fullname
     */
    public final static String MISSING_FULLNAME_MSG = "Missing mail folder full name";

    /**
     * Image attachment with Content-Id "%1$s" not found inside mail %2$s of mail folder %3$s
     */
    public final static String IMAGE_ATTACHMENT_NOT_FOUND_MSG = "Image attachment with content id \"%1$s\" not found inside mail %2$s of mail folder %3$s";

    /**
     * The specified email address %1$s is not covered by allowed email address aliases
     */
    public final static String INVALID_SENDER_MSG = "The specified E-Mail address %1$s is not covered by allowed E-Mail address aliases.";

    /**
     * Checking default folders on server %1$s for user %2$s (%3$s) in context on %4$s failed: %5$s
     */
    public final static String DEFAULT_FOLDER_CHECK_FAILED_MSG = "Checking default folders on server %1$s for user %2$s (%3$s) in context on %4$s failed: %5$s";

    /**
     * The types of specified data source are not supported
     */
    public final static String UNSUPPORTED_DATASOURCE_MSG = "The types of specified data source are not supported";

    /**
     * Mail cannot be parsed. Invalid or incomplete mail data.
     */
    public final static String UNPARSEABLE_MESSAGE_MSG = "Mail cannot be parsed. Invalid or incomplete mail data.";

    /**
     * Mail folder cannot be created/renamed. Empty folder name.
     */
    public final static String INVALID_FOLDER_NAME_EMPTY_MSG = "Mail folder cannot be created/renamed. Empty folder name.";

    /**
     * Invalid folder name: "%1$s"
     */
    public final static String INVALID_FOLDER_NAME_MSG = "Invalid folder name: \"%1$s\"";

    /**
     * Invalid Content-Disposition value: %1$s
     */
    public final static String INVALID_CONTENT_DISPOSITION_MSG = "Invalid Content-Disposition value: %1$s";

    /**
     * A folder named %1$s already exists.
     */
    public final static String DUPLICATE_FOLDER_MSG = "A folder named %1$s already exists.";

    /**
     * A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    public final static String DUPLICATE_FOLDER_EXT_MSG = "A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s).";

    /**
     * No create access on mail folder %1$s.
     */
    public final static String NO_CREATE_ACCESS_MSG = "No create access on mail folder %1$s.";

    /**
     * No create access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    public final static String NO_CREATE_ACCESS_EXT_MSG = "No create access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).";

    /**
     * Mail account %1$s with ID %2$s does not support mail transport.
     */
    public final static String NO_TRANSPORT_SUPPORT_MSG = "Mail account %1$s with ID %2$s does not support mail transport.";

    /**
     * Mail folder could not be found: %1$s.
     */
    public final static String FOLDER_NOT_FOUND_MSG = "Mail folder could not be found: %1$s.";

    // Mail folder could not be found.
    public final static String FOLDER_NOT_FOUND_SIMPLE_MSG = "Mail folder could not be found.";

    /**
     * Referenced mail %1$s could not be found in folder %2$s. Therefore reply/forward operation cannot be performed.
     */
    public final static String REFERENCED_MAIL_NOT_FOUND_MSG = "Referenced mail %1$s could not be found in folder %2$s. Therefore reply/forward operation cannot be performed.";

    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    public final static String PATTERN_TOO_SHORT_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    /**
     * Mail folder must not be deleted: %1$s.
     */
    public final static String FOLDER_DELETION_DENIED_MSG = "Mail folder must not be deleted: %1$s.";

    /**
     * No delete access on mail folder: %1$s.
     */
    public final static String NO_DELETE_ACCESS_MSG = "No delete access on mail folder: %1$s.";

    /**
     * Mail folder must not be moved: %1$s.
     */
    public final static String FOLDER_MOVE_DENIED_MSG = "Mail folder must not be moved: %1$s.";

    /**
     * Mail folder must not be updated: %1$s.
     */
    public final static String FOLDER_UPDATE_DENIED_MSG = "Mail folder must not be updated: %1$s.";

    /**
     * No write access on mail folder: %1$s.
     */
    public final static String NO_WRITE_ACCESS_MSG = "No write access on mail folder: %1$s.";

    /**
     * No connection available to access mailbox
     */
    public final static String NOT_CONNECTED_MSG = "No connection available to access mailbox";

    /**
     * Mail could not be found in folder %1$s for Message-Id: %2$s
     */
    public final static String MAIL_NOT_FOUN_BY_MESSAGE_ID_MSG = "Mail could not be found in folder %1$s for message identifier: %2$s";

    /**
     * Sent quota exceeded, you are only allowed to sent 1 mail in %1$s seconds.
     */
    public final static String SENT_QUOTA_EXCEEDED_MSG = "Sent quota exceeded. You are only allowed to send 1 E-Mail in %1$s seconds.";

    /**
     * Sent quota exceeded, you are only allowed to sent 1 mail in %1$s seconds.
     */
    public final static String RECIPIENTS_EXCEEDED_MSG = "Please limit your recipients to %1$s  (including To/Cc/Bcc), and click 'Send' again.";

    /**
     * Unable to parse mail server URI "%1$s".
     */
    public final static String URI_PARSE_FAILED_MSG = "Unable to parse mail server URI \"%1$s\".";

    /**
     * Mail attachment expired or absent.
     */
    public final static String ATTACHMENT_EXPIRED_MSG = "Mail attachment expired or absent.";

    /**
     * Account has been checked successfully but with a non-secure connection.
     */
    public final static String NON_SECURE_WARNING_MSG = "Account has been checked successfully but with a non-secure connection.";

    /**
     * Cannot forward more than %1$s messages at once. Please divide the messages to forward in chunks of appropriate size.
     */
    public final static String TOO_MANY_FORWARD_MAILS_MSG = "Cannot forward more than %1$s messages at once. Please divide the messages to forward in chunks of appropriate size.";

    /**
     * Your account has been created but will not use a secure connection.
     */
    public final static String NON_SECURE_CREATION_MSG = "Your account has been created but will not use a secure connection.";

    /**
     * Your E-Mail has been successfully sent, but the original E-Mail could not be flagged as "replied" and/or "forwarded".
     */
    public final static String FLAG_FAIL_MSG = "Your E-Mail has been successfully sent, but the original E-Mail could not be flagged as \"replied\" and/or \"forwarded\".";

    /**
     * Invalid folder name: "%1$s"
     */
    public final static String INVALID_FOLDER_NAME2_MSG = "Invalid folder name: \"%1$s\"";

    /**
     * Mail folder cannot be created/renamed. Folder name exceeds max length of %1$s.
     */
    public final static String INVALID_FOLDER_NAME_TOO_LONG_MSG = "Mail folder cannot be created/renamed. Folder name exceeds max length of %1$s.";

    /**
     * The attachments to this mail exceeded the size limit for attachments. Instead of sending the attachment in the email, the attachment
     * was published and the link added to your email. Whoever receives the email can then download the attachment.
     */
    public final static String USED_PUBLISHING_FEATURE_MSG = "The attachments to this E-Mail exceeded the size limit for attachments. Instead of sending the attachment with the E-Mail, the attachment was published and the link added to your E-Mail. Whoever receives the E-Mail can then download the attachment.";

    // Draft message could not be saved
    public static final String DRAFT_FAILED_UNKNOWN_MSG = "Draft message could not be saved";

    // There was a problem processing the request. Please (refresh view and) try again.
    public static final String PROCESSING_ERROR_MSG = "There was a problem processing the request. Please (refresh view and) try again.";

    // Invoked method is not supported.
    public static final String UNSUPPORTED_OPERATION_MSG = "Invoked method is not supported.";

    // Maximum message is exceeded. Max. is %1$s, but message is %2$s.
    public static final String MAX_MESSAGE_SIZE_EXCEEDED_MSG = "Maximum message is exceeded. Max. is %1$s, but message is %2$s.";

    // Re-sending message denied because message is not located in %1$s folder.
    public static final String RESEND_DENIED_MSG = "Re-sending message denied because message is not located in %1$s folder.";

}
