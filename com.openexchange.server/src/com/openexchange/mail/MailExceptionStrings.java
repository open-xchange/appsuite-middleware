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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

    public static final String MAIL_MESSAGE_RETRY = "A messaging error occurred. Please try again later.";

    public static final String MAIL_MESSAGE = "A messaging error occurred.";

    public final static String INITIALIZATION_PROBLEM_MSG = "Cannot initialize mail module.";

    public final static String NO_MAIL_ACCESS_MSG = "No mail module access permitted.";

    public final static String VERSIT_ERROR_MSG = "Error while converting contact to versit object.";

    public final static String NO_ATTACHMENT_FOUND_MSG = "No attachment was found with id \"%1$s\" in message.";

    public final static String UNSUPPORTED_VERSIT_ATTACHMENT_MSG = "Versit attachment could not be saved due to an unsupported MIME type.";

    public final static String UPLOAD_QUOTA_EXCEEDED_FOR_FILE_MSG = "Upload quota (%1$s) exceeded for file %2$s (size=%3$s).";

    public final static String UPLOAD_QUOTA_EXCEEDED_MSG = "Upload quota (%1$s) exceeded.";

    public final static String MAIL_NOT_FOUND_SIMPLE_MSG = "Mail could not be found.";

    public final static String SEND_FAILED_UNKNOWN_MSG = "Message could not be sent.";

    public final static String DELETE_FAILED_OVER_QUOTA_MSG = "This message could not be moved to trash folder as your mailbox is nearly full. Please try to empty your deleted items first, or delete smaller messages first.";

    public final static String NO_CONTENT_MSG = "No content available in mail part.";

    public final static String COPY_TO_SENT_FOLDER_FAILED_QUOTA_MSG = "Message has been successfully sent. Due to exceeded quota a copy could not be placed in your sent folder though.";

    public final static String COPY_TO_SENT_FOLDER_FAILED_MSG = "Message has been successfully sent. A copy could not be placed in your sent folder though.";

    public final static String BAD_PARAM_VALUE_MSG = "Bad value \"%1$s\" in parameter \"%2$s\".";

    public final static String NO_MULTIPLE_REPLY_MSG = "No reply on multiple messages possible.";

    public final static String ATTACHMENT_NOT_FOUND_MSG = "Attachment \"%1$s\" not found inside mail \"%2$s\" of mail folder \"%3$s\".";

    public final static String FOLDER_DOES_NOT_HOLD_MESSAGES_MSG = "Folder \"%1$s\" does not hold messages and is therefore not selectable";

    public final static String NO_ROOT_FOLDER_MODIFY_DELETE_MSG = "Root folder must not be modified or deleted.";

    public final static String IMAGE_ATTACHMENT_NOT_FOUND_MSG = "Image attachment with content id \"%1$s\" not found inside mail \"%2$s\" of mail folder \"%3$s\".";

    public final static String INVALID_SENDER_MSG = "The specified E-Mail address \"%1$s\" is not covered by allowed E-Mail address aliases.";

    public final static String DEFAULT_FOLDER_CHECK_FAILED_MSG = "Checking default folders failed for user %2$s.";

    public final static String UNSUPPORTED_DATASOURCE_MSG = "The types of specified data source are not supported.";

    public final static String UNPARSEABLE_MESSAGE_MSG = "Mail cannot be parsed. Invalid or incomplete mail data.";

    public final static String INVALID_FOLDER_NAME_EMPTY_MSG = "Mail folder cannot be created/renamed. Empty folder name.";

    public final static String INVALID_FOLDER_NAME_MSG = "Invalid folder name. Please avoid the following characters: %2$s";

    public final static String DUPLICATE_FOLDER_MSG = "A folder named \"%1$s\" already exists.";

    public final static String NO_CREATE_ACCESS_MSG = "No create access on mail folder \"%1$s\".";

    public final static String NO_TRANSPORT_SUPPORT_MSG = "Mail account \"%1$s\" with ID \"%2$s\" does not support mail transport.";

    public final static String FOLDER_NOT_FOUND_MSG = "Mail folder could not be found.";

    public final static String REFERENCED_MAIL_NOT_FOUND_MSG = "Referenced mail \"%1$s\" could not be found in folder \"%2$s\".";

    public final static String PATTERN_TOO_SHORT_MSG = "In order to accomplish the search, %1$d or more characters are required.";

    public final static String FOLDER_DELETION_DENIED_MSG = "Mail folder must not be deleted.";

    public final static String NO_DELETE_ACCESS_MSG = "No delete access on mail folder \"%1$s\".";

    public final static String FOLDER_MOVE_DENIED_MSG = "Mail folder must not be moved.";

    public final static String FOLDER_UPDATE_DENIED_MSG = "Mail folder must not be updated.";

    public final static String NO_WRITE_ACCESS_MSG = "No write access on mail folder \"%1$s\".";

    public final static String NOT_CONNECTED_MSG = "No connection available to access mailbox";

    public final static String MAIL_NOT_FOUN_BY_MESSAGE_ID_MSG = "Mail could not be found in folder \"%1$s\" by message identifier.";

    public final static String SENT_QUOTA_EXCEEDED_MSG = "Sent quota exceeded. You are only allowed to send 1 E-Mail in %1$s seconds.";

    public final static String RECIPIENTS_EXCEEDED_MSG = "Please limit your recipients to %1$s  (including To/Cc/Bcc), and click 'Send' again.";

    public final static String URI_PARSE_FAILED_MSG = "Unable to parse mail server URI \"%1$s\".";

    public final static String ATTACHMENT_EXPIRED_MSG = "Mail attachment expired or absent.";

    public final static String NON_SECURE_WARNING_MSG = "Account has been checked successfully but with a non-secure connection.";

    public final static String TOO_MANY_FORWARD_MAILS_MSG = "Cannot forward more than %1$s messages at once. Please divide the messages to forward in chunks of appropriate size.";

    public final static String NON_SECURE_CREATION_MSG = "Your account has been created but will not use a secure connection.";

    public final static String FLAG_FAIL_MSG = "Your E-Mail has been successfully sent, but the original E-Mail could not be flagged as \"replied\" and/or \"forwarded\".";

    public final static String INVALID_FOLDER_NAME_TOO_LONG_MSG = "Mail folder cannot be created/renamed. Folder name exceeds max length of %1$s.";

    public final static String USED_PUBLISHING_FEATURE_MSG = "The attachments to this E-Mail exceeded the size limit for attachments. Instead of sending the attachment with the E-Mail, the attachment was published and the link added to your E-Mail. Whoever receives the E-Mail can then download the attachment.";

    public static final String DRAFT_FAILED_UNKNOWN_MSG = "Draft message could not be saved.";

    public static final String MAX_MESSAGE_SIZE_EXCEEDED_MSG = "Maximum message size is exceeded. Max. is %1$s.";

    public static final String RESEND_DENIED_MSG = "Re-sending message denied because message is not located in \"%1$s\" folder.";

    public static final String PING_FAILED_MSG = "Validation of server %1$s failed with reason: %3$s";

    public static final String PING_FAILED_AUTH_MSG = "Validation of server %1$s failed due to invalid credentials";

    public static final String NON_SECURE_DENIED_MSG = "Server %1$s does not support being accessed using a secure connection.";

    public static final String ACCOUNT_DOES_NOT_EXIST_MSG = "Mail access is disabled for context administrator.";

    public static final String MAX_DRIVE_ATTACHMENTS_EXCEEDED_MSG = "The maximum number of files that may be attached to an E-Mail is exceeded. Max. is %1$s.";

    // Denied to update a standard/default folder like Trash
    public static final String NO_DEFAULT_FOLDER_UPDATE_MSG = "Default folder must not be updated";

    // The client request is not permitted.
    public static final String REQUEST_NOT_PERMITTED_MSG = "The client request is not permitted.";

    // Sending the message denied.
    public static final String SEND_DENIED = "Sending the message denied.";

    // Denied to rename a standard/default folder like Trash
    public static final String NO_DEFAULT_FOLDER_RENAME_MSG = "Default folder must not be renamed";

    // Denied to delete a standard/default folder like Trash
    public static final String NO_DEFAULT_FOLDER_DELETE_MSG = "Default folder must not be deleted";

    public static final String ARCHIVE_SUBFOLDER_NOT_ALLOWED_MSG = "Archive folder does not allow subfolders.";

    // The user tries to import a file as a RFC822 message, that is not valid
    public static final String INVALID_MESSAGE_MSG = "No valid file";

    // A part's content could not be read from a message; e.g. an I/O error occurred
    public static final String UNREADBALE_PART_CONTENT_MSG = "The mail part could not be read";

    // The content/body of the message is too big
    public static final String CONTENT_TOO_BIG_MSG = "The content of the mail is too big.";

    // Invalid parameter value: %1$s
    public static final String INVALID_PARAMETER_VALUE_MSG = "Invalid parameter value: %1$s";

    // The flag name %1$s is not valid. Flags may not begin with a leading '\\'.
    public static final String INVALID_FLAG_WITH_LEADING_BACKSLASH_MSG = "The flag name %1$s is not valid. Flags may not begin with a leading '\\'.";

    public static final String USED_SHARING_FEATURE_MSG = "The attachments to this E-Mail exceeded the size limit for attachments. Instead of sending the attachments with the E-Mail, the attachments are shared and the links are added to your E-Mail. Whoever receives the E-Mail can then access the attachments.";

    // User wants to compose a share message but does not hold sufficient permissions/capabilities to do so
    public static final String SHARING_NOT_POSSIBLE_MSG = "You are not allowed to compose a message containing share links";

    // User wants to compose a share message but does not specify an expiration date which is required
    public static final String EXPIRATION_DATE_MISSING_MSG = "Please specify an expiration date in order to compose a message containing share links";

    // The authentication type selected for a certain mail/transport account is not supported; e.g. user wants to authenticate via OAuth, but IMAP server does not support AUTH=XOAUTH2 capability
    public static final String AUTH_TYPE_NOT_SUPPORTED_MSG = "The selected authentication type is not supported by server %2$s";

    // Something is wrong with a subscribed E-Mail account; e.g. invalid credentials. Example: "Please check your mail account: The entered credentials are wrong"
    public static final String ACCOUNT_STATUS_WITH_INFO_MSG = "Please check your mail account: %2$s";

}
