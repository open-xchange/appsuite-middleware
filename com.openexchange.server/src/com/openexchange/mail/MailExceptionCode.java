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

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.upload.impl.UploadUtility;

/**
 * Mail exception codes.
 * <p>
 * The detail number range in subclasses generated in mail bundles is supposed to start with <code>2000</code> and may go up to
 * <code>2999</code>.
 * <p>
 * The detail number range in subclasses generated in transport bundles is supposed to start with <code>3000</code> and may go up to
 * <code>3999</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailExceptionCode implements DisplayableOXExceptionCode {
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", CATEGORY_ERROR, 0),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAMETER("Missing parameter %1$s", CATEGORY_ERROR, 1),
    /**
     * Invalid CATEGORY_PERMISSION_DENIED values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s
     */
    INVALID_PERMISSION("Invalid permission values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s", CATEGORY_ERROR, 2),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 3),
    /**
     * Missing parameter in user's mail config: %1$s
     */
    MISSING_CONNECT_PARAM("Missing parameter in user's mail config: %1$s", CATEGORY_ERROR, 4),
    /**
     * Configuration error: %1$s
     */
    CONFIG_ERROR("Configuration error: %1$s", CATEGORY_CONFIGURATION, 5),
    /**
     * Invalid multipart content. Number of enclosed contents is 0
     */
    INVALID_MULTIPART_CONTENT("Invalid multipart content. Number of enclosed contents is 0", CATEGORY_ERROR, 6),
    /**
     * A part's content could not be read from message %1$s in mail folder %2$s
     */
    UNREADBALE_PART_CONTENT("A part's content could not be read from message %1$s in mail folder %2$s", MailExceptionStrings.UNREADBALE_PART_CONTENT_MSG, CATEGORY_USER_INPUT, 7),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 8),
    /**
     * Invalid message path: %1$s
     */
    INVALID_MAIL_IDENTIFIER("Invalid message path: %1$s", CATEGORY_ERROR, 9),
    /**
     * Unknown color label index: %1$s
     */
    UNKNOWN_COLOR_LABEL("Unknown color label: %1$s", CATEGORY_ERROR, 10),
    /**
     * Cannot instantiate class %1$s.
     */
    INSTANTIATION_PROBLEM("Cannot instantiate class %1$s.", CATEGORY_CONFIGURATION, 11),
    /**
     * Cannot initialize mail module
     */
    INITIALIZATION_PROBLEM("Cannot initialize mail module", CATEGORY_CONFIGURATION, 12),
    /**
     * No mail module access permitted
     */
    NO_MAIL_ACCESS("No mail module access permitted", MailExceptionStrings.NO_MAIL_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 13),
    /**
     * Mail account is disabled for admin user in context %1$s
     */
    ACCOUNT_DOES_NOT_EXIST("Mail account is disabled for admin user in context %1$s", MailExceptionStrings.ACCOUNT_DOES_NOT_EXIST_MSG, CATEGORY_CONFIGURATION, 14),
    /**
     * Process was interrupted. Please try again.
     */
    INTERRUPT_ERROR("Process was interrupted. Please try again.", OXExceptionStrings.MESSAGE_RETRY, CATEGORY_TRY_AGAIN, 15),
    /**
     * Unsupported charset-encoding: %1$s
     */
    ENCODING_ERROR("Unsupported charset encoding: %1$s", CATEGORY_ERROR, 16),
    /**
     * Header %1$s could not be properly parsed
     */
    HEADER_PARSE_ERROR("Header %1$s could not be properly parsed", CATEGORY_ERROR, 17),
    /**
     * Missing default %1$s folder in user mail settings
     */
    MISSING_DEFAULT_FOLDER_NAME("Missing default %1$s folder in user mail settings", CATEGORY_CONFIGURATION, 18),
    /**
     * Spam handler initialization failed: %1$s
     */
    SPAM_HANDLER_INIT_FAILED("Spam handler initialization failed: %1$s", CATEGORY_CONFIGURATION, 19),
    /**
     * Invalid Content-Type value: %1$s
     */
    INVALID_CONTENT_TYPE("Invalid content type value: %1$s", CATEGORY_ERROR, 20),
    /**
     * Messaging error: %1$s. TODO: Maybe change to: Broken/Bad message
     */
    MESSAGING_ERROR("Messaging error: %1$s", CATEGORY_ERROR, 21),
    /**
     * Message field %1$s cannot be handled
     */
    INVALID_FIELD("Message field %1$s cannot be handled", CATEGORY_ERROR, 22),
    /**
     * Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    INVALID_FIELD_EXT("Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)", CATEGORY_ERROR, 22),
    /**
     * Versit error: %1$s
     */
    VERSIT_ERROR("Versit error: %1$s", MailExceptionStrings.VERSIT_ERROR_MSG, CATEGORY_ERROR, 23),
    /**
     * No attachment was found with id %1$s in message
     */
    NO_ATTACHMENT_FOUND("No attachment was found with id %1$s in message", MailExceptionStrings.NO_ATTACHMENT_FOUND_MSG,
        CATEGORY_USER_INPUT, 24),
    /**
     * Versit attachment could not be saved due to an unsupported MIME type: %1$s
     */
    UNSUPPORTED_VERSIT_ATTACHMENT("Versit attachment could not be saved due to an unsupported MIME type: %1$s",
        MailExceptionStrings.UNSUPPORTED_VERSIT_ATTACHMENT_MSG, CATEGORY_USER_INPUT, 25),
    /**
     * Invalid parameter name: %1$s
     */
    INVALID_PARAMETER("Invalid parameter name: %1$s", CATEGORY_ERROR, 26),
    /**
     * Could not create a PartModifier instance from name %1$s
     */
    PART_MODIFIER_CREATION_FAILED("Could not create a PartModifier instance from name %1$s", CATEGORY_ERROR, 27),
    /**
     * Upload quota (%1$s) exceeded for file %2$s (size=%3$s)
     */
    UPLOAD_QUOTA_EXCEEDED_FOR_FILE("Upload quota (%1$s) exceeded for file %2$s (size=%3$s)", MailExceptionStrings.UPLOAD_QUOTA_EXCEEDED_FOR_FILE_MSG, CATEGORY_USER_INPUT, 28),
    /**
     * Upload quota (%1$s) exceeded
     */
    UPLOAD_QUOTA_EXCEEDED("Upload quota (%1$s) exceeded", MailExceptionStrings.UPLOAD_QUOTA_EXCEEDED_MSG, CATEGORY_USER_INPUT, 29),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAM(MISSING_PARAMETER),
    /**
     * Invalid integer value %1$s
     */
    INVALID_INT_VALUE("Invalid integer value %1$s", CATEGORY_ERROR, 31),
    /**
     * Mail(s) %1$s could not be found in folder %2$s
     */
    MAIL_NOT_FOUND("Mail(s) %1$s could not be found in folder %2$s", MailExceptionStrings.MAIL_NOT_FOUND_SIMPLE_MSG,
        CATEGORY_USER_INPUT, 32),
    /**
     * Mail could not be found
     */
    MAIL_NOT_FOUND_SIMPLE("Mail could not be found", MailExceptionStrings.MAIL_NOT_FOUND_SIMPLE_MSG, CATEGORY_USER_INPUT, 32),
    /**
     * Action %1$s is not supported by %2$s
     */
    UNSUPPORTED_ACTION("Action %1$s is not supported by %2$s", CATEGORY_ERROR, 33),
    /**
     * Message could not be sent
     */
    SEND_FAILED_UNKNOWN("Message could not be sent", MailExceptionStrings.SEND_FAILED_UNKNOWN_MSG, CATEGORY_USER_INPUT, 35),
    /**
     * Unknown action: %1$s
     */
    UNKNOWN_ACTION("Unknown or unsupported action: %1$s", CATEGORY_ERROR, 36),
    /**
     * Missing field %1$s
     */
    MISSING_FIELD("Missing field %1$s", CATEGORY_ERROR, 37),
    /**
     * Unsupported MIME type %1$s
     */
    UNSUPPORTED_MIME_TYPE("Unsupported MIME type %1$s", CATEGORY_ERROR, 38),
    /**
     * This message could not be moved to trash folder as your mailbox is nearly full.<br>
     * Please try to empty your deleted items first, or delete smaller messages first.
     */
    DELETE_FAILED_OVER_QUOTA("This message could not be moved to trash folder as your mailbox is nearly full. Please try to empty"
        + " your deleted items first, or delete smaller messages first.", MailExceptionStrings.DELETE_FAILED_OVER_QUOTA_MSG,
        CATEGORY_CAPACITY, 39),
    /**
     * The message part with sequence ID %1$s could not be found in message %2$s in folder %3$s
     */
    PART_NOT_FOUND("The message part with sequence ID %1$s could not be found in message %2$s in folder %3$s.",CATEGORY_ERROR, 40),
    /**
     * No content available in mail part
     */
    NO_CONTENT("No content available in mail part", MailExceptionStrings.NO_CONTENT_MSG, CATEGORY_USER_INPUT, 41),
    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder due to exceeded quota.
     */
    COPY_TO_SENT_FOLDER_FAILED_QUOTA("Message has been successfully sent. Due to exceeded quota a copy could not be placed in"
        + " your sent folder though.", MailExceptionStrings.COPY_TO_SENT_FOLDER_FAILED_QUOTA_MSG, CATEGORY_CAPACITY, 42),
    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder
     */
    COPY_TO_SENT_FOLDER_FAILED("Message has been successfully sent. A copy could not be placed in your sent folder though.",
        MailExceptionStrings.COPY_TO_SENT_FOLDER_FAILED_MSG, CATEGORY_WARNING, 43),
    /**
     * No provider could be found for protocol/URL "%1$s"
     */
    UNKNOWN_PROTOCOL("No provider could be found for protocol/URL \"%1$s\"", CATEGORY_CONFIGURATION, 44),
    /**
     * Protocol cannot be parsed: %1$s
     */
    PROTOCOL_PARSE_ERROR("Protocol cannot be parsed: %1$s", CATEGORY_ERROR, 45),
    /**
     * Bad value %1$s in parameter %2$s
     */
    BAD_PARAM_VALUE("Bad value %1$s in parameter %2$s", MailExceptionStrings.BAD_PARAM_VALUE_MSG, CATEGORY_USER_INPUT, 46),
    /**
     * No reply on multiple message possible
     */
    NO_MULTIPLE_REPLY("No reply on multiple message possible", MailExceptionStrings.NO_MULTIPLE_REPLY_MSG, CATEGORY_USER_INPUT, 47),
    /**
     * legal system flag argument %1$s. Flag must be to the power of 2
     */
    ILLEGAL_FLAG_ARGUMENT("Illegal system flag argument %1$s. Flag must be to the power of 2", CATEGORY_ERROR, 48),
    /**
     * Attachment %1$s not found inside mail %2$s of mail folder %3$s
     */
    ATTACHMENT_NOT_FOUND("Attachment %1$s not found inside mail %2$s of mail folder %3$s", MailExceptionStrings.ATTACHMENT_NOT_FOUND_MSG,
        CATEGORY_USER_INPUT, 49),
    /**
     * Folder %1$s does not hold messages and is therefore not selectable
     */
    FOLDER_DOES_NOT_HOLD_MESSAGES("Folder %1$s does not hold messages and is therefore not selectable",
        MailExceptionStrings.FOLDER_DOES_NOT_HOLD_MESSAGES_MSG, CATEGORY_PERMISSION_DENIED, 50),
    /**
     * Folder %1$s does not hold messages and is therefore not selectable on server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    FOLDER_DOES_NOT_HOLD_MESSAGES_EXT("Folder %1$s does not hold messages and is therefore not selectable on server %2$s with"
        + "login %3$s (user=%4$s, context=%5$s)", MailExceptionStrings.FOLDER_DOES_NOT_HOLD_MESSAGES_MSG, CATEGORY_PERMISSION_DENIED, 50),
    /**
     * Insufficient folder attributes: Either existence status or full name have to be present to determine if a mail folder create or
     * update shall be performed
     */
    INSUFFICIENT_FOLDER_ATTR("Insufficient folder attributes: Either existence status or full name have to be present to determine"
        + " if a mail folder create or update shall be performed", CATEGORY_ERROR, 51),
    /**
     * Root folder must not be modified or deleted
     */
    NO_ROOT_FOLDER_MODIFY_DELETE("Root folder must not be modified or deleted", MailExceptionStrings.NO_ROOT_FOLDER_MODIFY_DELETE_MSG,
        CATEGORY_USER_INPUT, 52),
    /**
     * No transport provider could be found for protocol/URL "%1$s"
     */
    UNKNOWN_TRANSPORT_PROTOCOL("No transport provider could be found for protocol/URL \"%1$s\"", CATEGORY_CONFIGURATION, 53),
    /**
     * Missing mail folder fullname
     */
    MISSING_FULLNAME("Missing mail folder full name", CATEGORY_ERROR, 54),
    /**
     * Image attachment with Content-Id "%1$s" not found inside mail %2$s of mail folder %3$s
     */
    IMAGE_ATTACHMENT_NOT_FOUND("Image attachment with content id \"%1$s\" not found inside mail %2$s of mail folder %3$s",
        MailExceptionStrings.IMAGE_ATTACHMENT_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 55),
    /**
     * The specified email address %1$s is not covered by allowed email address aliases
     */
    INVALID_SENDER("The specified E-Mail address %1$s is not covered by allowed E-Mail address aliases.",
        MailExceptionStrings.INVALID_SENDER_MSG, CATEGORY_USER_INPUT, 56),
    /**
     * Checking default folders on server %1$s for user %2$s (%3$s) in context on %4$s failed: %5$s
     */
    DEFAULT_FOLDER_CHECK_FAILED("Checking default folders on server %1$s for user %2$s (%3$s) in context on %4$s failed: %5$s",
        MailExceptionStrings.DEFAULT_FOLDER_CHECK_FAILED_MSG, CATEGORY_USER_INPUT, 57),
    /**
     * The types of specified data source are not supported
     */
    UNSUPPORTED_DATASOURCE("The types of specified data source are not supported", MailExceptionStrings.UNSUPPORTED_DATASOURCE_MSG,
        CATEGORY_USER_INPUT, 58),
    /**
     * Mail cannot be parsed. Invalid or incomplete mail data.
     */
    UNPARSEABLE_MESSAGE("Mail cannot be parsed. Invalid or incomplete mail data.", MailExceptionStrings.UNPARSEABLE_MESSAGE_MSG,
        CATEGORY_USER_INPUT, 59),
    /**
     * Mail folder cannot be created/renamed. Empty folder name.
     */
    INVALID_FOLDER_NAME_EMPTY("Mail folder cannot be created/renamed. Empty folder name.",
        MailExceptionStrings.INVALID_FOLDER_NAME_EMPTY_MSG, CATEGORY_USER_INPUT, 60),
    /**
     * Invalid folder name: "%1$s". Please avoid the following characters: %2$s
     */
    INVALID_FOLDER_NAME("Invalid folder name: \"%1$s\". Please avoid the following characters: %2$s", MailExceptionStrings.INVALID_FOLDER_NAME_MSG, CATEGORY_USER_INPUT, 61),
    /**
     * Invalid Content-Disposition value: %1$s
     */
    INVALID_CONTENT_DISPOSITION("Invalid Content-Disposition value: %1$s", CATEGORY_ERROR, 62),
    /**
     * A folder named %1$s already exists.
     */
    DUPLICATE_FOLDER("A folder named %1$s already exists.", MailExceptionStrings.DUPLICATE_FOLDER_MSG, CATEGORY_PERMISSION_DENIED, 63),
    /**
     * A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    DUPLICATE_FOLDER_EXT("A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s).",
        MailExceptionStrings.DUPLICATE_FOLDER_MSG, CATEGORY_PERMISSION_DENIED, 63),
    /**
     * No create access on mail folder %1$s.
     */
    NO_CREATE_ACCESS("No create access on mail folder %1$s.", MailExceptionStrings.NO_CREATE_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 64),
    /**
     * No create access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    NO_CREATE_ACCESS_EXT("No create access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).",
        MailExceptionStrings.NO_CREATE_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 64),
    /**
     * Mail account %1$s with ID %2$s does not support mail transport.
     */
    NO_TRANSPORT_SUPPORT("Mail account %1$s with ID %2$s does not support mail transport.", MailExceptionStrings.NO_TRANSPORT_SUPPORT_MSG,
        CATEGORY_USER_INPUT, 65),
    /**
     * Mail folder could not be found: %1$s.
     */
    FOLDER_NOT_FOUND("Mail folder could not be found: %1$s.", MailExceptionStrings.FOLDER_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 66),
    /**
     * Mail folder could not be found.
     */
    FOLDER_NOT_FOUND_SIMPLE("Mail folder could not be found.", MailExceptionStrings.FOLDER_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 66),
    /**
     * Referenced mail %1$s could not be found in folder %2$s. Therefore reply/forward operation cannot be performed.
     */
    REFERENCED_MAIL_NOT_FOUND("Referenced mail %1$s could not be found in folder %2$s. Therefore reply/forward operation cannot"
        + " be performed.", MailExceptionStrings.REFERENCED_MAIL_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 67),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    PATTERN_TOO_SHORT("In order to accomplish the search, %1$d or more characters are required.",
        MailExceptionStrings.PATTERN_TOO_SHORT_MSG, CATEGORY_USER_INPUT, 68),
    /**
     * Mail folder must not be deleted: %1$s.
     */
    FOLDER_DELETION_DENIED("Mail folder must not be deleted: %1$s.", MailExceptionStrings.FOLDER_DELETION_DENIED_MSG,
        CATEGORY_USER_INPUT, 69),
    /**
     * No delete access on mail folder: %1$s.
     */
    NO_DELETE_ACCESS("No delete access on mail folder: %1$s.", MailExceptionStrings.NO_DELETE_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 70),
    /**
     * Mail folder must not be moved: %1$s.
     */
    FOLDER_MOVE_DENIED("Mail folder must not be moved: %1$s.", MailExceptionStrings.FOLDER_MOVE_DENIED_MSG, CATEGORY_USER_INPUT, 71),
    /**
     * Mail folder must not be updated: %1$s.
     */
    FOLDER_UPDATE_DENIED("Mail folder must not be updated: %1$s.", MailExceptionStrings.FOLDER_UPDATE_DENIED_MSG, CATEGORY_USER_INPUT, 72),
    /**
     * No write access on mail folder: %1$s.
     */
    NO_WRITE_ACCESS("No write access on mail folder: %1$s.", MailExceptionStrings.NO_WRITE_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 73),
    /**
     * No connection available to access mailbox
     */
    NOT_CONNECTED("No connection available to access mailbox", MailExceptionStrings.NOT_CONNECTED_MSG, CATEGORY_USER_INPUT, 74),
    /**
     * Mail could not be found in folder %1$s for Message-Id: %2$s
     */
    MAIL_NOT_FOUN_BY_MESSAGE_ID("Mail could not be found in folder %1$s for message identifier: %2$s",
        MailExceptionStrings.MAIL_NOT_FOUN_BY_MESSAGE_ID_MSG, CATEGORY_USER_INPUT, 32),
    /**
     * Sent quota exceeded, you are only allowed to sent 1 mail in %1$s seconds.
     */
    SENT_QUOTA_EXCEEDED("Sent quota exceeded. You are only allowed to send 1 E-Mail in %1$s seconds.",
        MailExceptionStrings.SENT_QUOTA_EXCEEDED_MSG, CATEGORY_USER_INPUT, 75),
    /**
     * Please limit your recipients to %1$s  (including To/Cc/Bcc), and click 'Send' again.
     */
    RECIPIENTS_EXCEEDED("Please limit your recipients to %1$s  (including To/Cc/Bcc), and click 'Send' again.",
        MailExceptionStrings.RECIPIENTS_EXCEEDED_MSG, CATEGORY_USER_INPUT, 76),
    /**
     * Unable to parse mail server URI "%1$s".
     */
    URI_PARSE_FAILED("Unable to parse mail server URI \"%1$s\".", MailExceptionStrings.URI_PARSE_FAILED_MSG, CATEGORY_USER_INPUT, 77),
    /**
     * Mail attachment expired or absent.
     */
    ATTACHMENT_EXPIRED("Mail attachment expired or absent.", MailExceptionStrings.ATTACHMENT_EXPIRED_MSG, CATEGORY_USER_INPUT, 78),
    /**
     * Account has been checked successfully but with a non-secure connection.
     */
    NON_SECURE_WARNING("Account has been checked successfully but with a non-secure connection.",
        MailExceptionStrings.NON_SECURE_WARNING_MSG, CATEGORY_WARNING, 79),
    /**
     * Cannot forward more than %1$s messages at once. Please divide the messages to forward in chunks of appropriate size.
     */
    TOO_MANY_FORWARD_MAILS("Cannot forward more than %1$s messages at once. Please divide the messages to forward in chunks"
        + " of appropriate size.", MailExceptionStrings.TOO_MANY_FORWARD_MAILS_MSG, CATEGORY_USER_INPUT, 80),
    /**
     * Your account has been created but will not use a secure connection.
     */
    NON_SECURE_CREATION("Your account has been created but will not use a secure connection.", MailExceptionStrings.NON_SECURE_CREATION_MSG,
        CATEGORY_WARNING, 81),
    /**
     * Your E-Mail has been successfully sent, but the original E-Mail could not be flagged as "replied" and/or "forwarded".
     */
    FLAG_FAIL("Your E-Mail has been successfully sent, but the original E-Mail could not be flagged as \"replied\" and/or"
        + " \"forwarded\".", MailExceptionStrings.FLAG_FAIL_MSG, CATEGORY_WARNING, 82),
    /**
     * Invalid folder name: "%1$s". Please avoid the following characters: %2$s
     */
    INVALID_FOLDER_NAME2("Invalid folder name: \"%1$s\"", MailExceptionStrings.INVALID_FOLDER_NAME_MSG, INVALID_FOLDER_NAME.getCategory(),
        INVALID_FOLDER_NAME.getNumber()),

    /**
     * Mail folder cannot be created/renamed. Folder name exceeds max length of %1$s.
     */
    INVALID_FOLDER_NAME_TOO_LONG("Mail folder cannot be created/renamed. Folder name exceeds max length of %1$s.",
        MailExceptionStrings.INVALID_FOLDER_NAME_TOO_LONG_MSG, CATEGORY_USER_INPUT, 83),
    /**
     * The attachments to this mail exceeded the size limit for attachments. Instead of sending the attachment in the email, the
     *  attachment was published and the link added to your email. Whoever receives the email can then download the attachment.
     */
    USED_PUBLISHING_FEATURE("The attachments to this E-Mail exceeded the size limit for attachments. Instead of sending the"
        + " attachment with the E-Mail, the attachment was published and the link added to your E-Mail. Whoever receives the"
        + " E-Mail can then download the attachment.",  MailExceptionStrings.USED_PUBLISHING_FEATURE_MSG, CATEGORY_WARNING, 84),
    /**
     * Draft message could not be saved
     */
    DRAFT_FAILED_UNKNOWN("Draft message could not be saved", MailExceptionStrings.DRAFT_FAILED_UNKNOWN_MSG, CATEGORY_USER_INPUT, 85),
    /**
     * There was a problem processing the request. Please (refresh view and) try again.
     */
    PROCESSING_ERROR("There was a problem processing the request. Please (refresh view and) try again.",
        MailExceptionStrings.MAIL_MESSAGE_RETRY, CATEGORY_TRY_AGAIN, 86),
    /**
     * Invoked method is not supported.
     */
    UNSUPPORTED_OPERATION("Invoked method is not supported.", CATEGORY_ERROR, 87),
    /**
     * Maximum message size is exceeded. Max. is %1$s.
     */
    MAX_MESSAGE_SIZE_EXCEEDED("Maximum message size is exceeded. Max. is %1$s.", MailExceptionStrings.MAX_MESSAGE_SIZE_EXCEEDED_MSG, CATEGORY_USER_INPUT, 88),
    /**
     * Re-sending message denied because message is not located in %1$s folder.
     */
    RESEND_DENIED("Re-sending message denied because message is not located in %1$s folder.", MailExceptionStrings.RESEND_DENIED_MSG, CATEGORY_USER_INPUT, 89),
    /**
     * Ping to server %1$s failed for login %2$s with reason: %3$s
     */
    PING_FAILED("Ping to server %1$s failed for login %2$s with reason: %3$s", MailExceptionStrings.PING_FAILED_MSG, CATEGORY_WARNING, 90),
    /**
     * Ping to server %1$s failed with login %2$s due to invalid credentials.
     */
    PING_FAILED_AUTH("Ping to server %1$s failed with login %2$s due to invalid credentials.", MailExceptionStrings.PING_FAILED_AUTH_MSG,
        CATEGORY_WARNING, 91),
    /**
     * Server %1$s does not support being accessed using a secure connection.
     */
    NON_SECURE_DENIED("Server %1$s does not support being accessed using a secure connection.", MailExceptionStrings.NON_SECURE_DENIED_MSG, CATEGORY_PERMISSION_DENIED, 92),
    /**
     * Invalid argument(s) supplied
     */
    ILLEGAL_ARGUMENT("Invalid argument(s) supplied.", CATEGORY_ERROR, 93),
    /**
     * The maximum number of files that may be attached to an E-Mail is exceeded. Max. is %1$s.
     */
    MAX_DRIVE_ATTACHMENTS_EXCEEDED("The maximum number of files that may be attached to an E-Mail is exceeded. Max. is %1$s.", MailExceptionStrings.MAX_DRIVE_ATTACHMENTS_EXCEEDED_MSG, CATEGORY_USER_INPUT, 94),
    /**
     * The client request is not permitted. Details: %1$s
     */
    REQUEST_NOT_PERMITTED("The client request is not permitted. Details: %1$s", MailExceptionStrings.REQUEST_NOT_PERMITTED_MSG, CATEGORY_PERMISSION_DENIED, 95),
    /**
     * Sending the message denied.
     */
    SEND_DENIED("Sending the message denied.", MailExceptionStrings.SEND_DENIED, CATEGORY_PERMISSION_DENIED, 96),
    /**
     * Default folder %1$s must not be updated
     */
    NO_DEFAULT_FOLDER_UPDATE("Default folder %1$s must not be updated", MailExceptionStrings.NO_DEFAULT_FOLDER_UPDATE_MSG, CATEGORY_PERMISSION_DENIED, 97),
    /**
     * Default folder %1$s must not be renamed
     */
    NO_DEFAULT_FOLDER_RENAME("Default folder %1$s must not be renamed", MailExceptionStrings.NO_DEFAULT_FOLDER_RENAME_MSG, CATEGORY_PERMISSION_DENIED, 98),
    /**
     * Default folder %1$s must not be deleted
     */
    NO_DEFAULT_FOLDER_DELETE("Default folder %1$s must not be deleted", MailExceptionStrings.NO_DEFAULT_FOLDER_DELETE_MSG, CATEGORY_PERMISSION_DENIED, 99),
    /**
     * Archive folder does not allow subfolders.
     */
    ARCHIVE_SUBFOLDER_NOT_ALLOWED("Archive folder does not allow subfolders.", MailExceptionStrings.ARCHIVE_SUBFOLDER_NOT_ALLOWED_MSG, CATEGORY_PERMISSION_DENIED, 100),
    /**
     * Invalid message data: %1$s
     */
    INVALID_MESSAGE("Invalid message data: %1$s", MailExceptionStrings.INVALID_MESSAGE_MSG, CATEGORY_USER_INPUT, 101),
    /**
     * The message content is too big from message %1$s in mail folder %2$s
     */
    CONTENT_TOO_BIG("The message content is too big from message %1$s in mail folder %2$s", MailExceptionStrings.CONTENT_TOO_BIG_MSG, CATEGORY_ERROR, 102),
    /**
     * Invalid parameter value: %1$s
     */
    INVALID_PARAMETER_VALUE("Invalid parameter value: %1$s", MailExceptionStrings.INVALID_PARAMETER_VALUE_MSG, CATEGORY_USER_INPUT, 103),
    /**
     * The flag name %1$s is not valid. Flags may not begin with a leading '\\'.
     */
    INVALID_FLAG_WITH_LEADING_BACKSLASH("The flag name %1$s is not valid. Flags may not begin with a leading '\\'.", MailExceptionStrings.INVALID_FLAG_WITH_LEADING_BACKSLASH_MSG, CATEGORY_USER_INPUT, 104),
    /**
     * The attachments to this mail exceeded the size limit for attachments. Instead of sending the attachment in the email, the
     * attachments are shared and the links added to your email. Whoever receives the email can then access the attachments.
     */
    USED_SHARING_FEATURE("The attachments to this mail exceeded the size limit for attachments. Instead of sending the attachment in the email, the" +
        " attachments are shared and the links added to your email. Whoever receives the email can then access the attachments.",  MailExceptionStrings.USED_SHARING_FEATURE_MSG, CATEGORY_WARNING, 105),
    /**
     * User %1$s in context %2$s is not allowed to compose a share message
     */
    SHARING_NOT_POSSIBLE("User %1$s in context %2$s is not allowed to compose a share message", MailExceptionStrings.SHARING_NOT_POSSIBLE_MSG, CATEGORY_PERMISSION_DENIED, 106),
    /**
     * User %1$s in context %2$s did not specify an expiration date for a share compose message, but is required to do so
     */
    EXPIRATION_DATE_MISSING("User %1$s in context %2$s did not specify an expiration date for a share compose message, but is required to do so", MailExceptionStrings.EXPIRATION_DATE_MISSING_MSG, CATEGORY_USER_INPUT, 107),
    /**
     * Authentication type "%1$s" is not supported by server %2$s
     */
    AUTH_TYPE_NOT_SUPPORTED("Authentication type \"%1$s\" is not supported by server %2$s", MailExceptionStrings.AUTH_TYPE_NOT_SUPPORTED_MSG, CATEGORY_CONFIGURATION, 108),
    /**
     * An account status response providing no information for a user
     */
    ACCOUNT_STATUS("Account status is \"%1$s\".", CATEGORY_SERVICE_DOWN, 109),
    /**
     * An account status response providing information for a user. Account status is "%1$s". User info: %2$s
     */
    ACCOUNT_STATUS_WITH_INFO("Account status is \"%1$s\". User info: %2$s", MailExceptionStrings.ACCOUNT_STATUS_WITH_INFO_MSG, ACCOUNT_STATUS.getCategory(), ACCOUNT_STATUS.getNumber()), // Yapp, same error code

    ;

    private static final String PREFIX = "MSG";

    /**
     * Gets the <code>"MSG"</code> prefix for mail-related exceptions
     *
     * @return The <code>"MSG"</code> prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    /**
     * Checks if specified {@code OXException}'s prefix is equal to this {@code OXExceptionCode} enumeration.
     *
     * @param e The {@code OXException} to check
     * @return <code>true</code> if prefix is equal; otherwise <code>false</code>
     */
    public static boolean hasPrefix(OXException e) {
        if (null == e) {
            return false;
        }
        return PREFIX.equals(e.getPrefix());
    }

    private final String message;
    private final String displayMessage;
    private final int number;
    private final Category category;

    private MailExceptionCode(String message, Category category, int detailNumber) {
        this(message, null, category, detailNumber);
    }

    private MailExceptionCode(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.number = detailNumber;
        this.category = category;
    }

    private MailExceptionCode(MailExceptionCode source) {
        message = source.message;
        number = source.number;
        category = source.category;
        displayMessage = source.displayMessage;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param category The optional category
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Category category, Object... args) {
        return OXExceptionFactory.getInstance().create(this, category, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param category The optional category
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Category category, Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, category, cause, args);
    }

    // --------------------------------------------------------------------------------------- //

    /**
     * Converts given number of bytes to a human readable format.
     *
     * @param size The number of bytes
     * @param precision The number of digits allowed after dot
     * @param longName <code>true</code> to use unit's long name (e.g. <code>Megabytes</code>) or short name (e.g. <code>MB</code>)
     * @param realSize <code>true</code> to bytes' real size of <code>1024</code> used for detecting proper unit; otherwise
     *            <code>false</code> to narrow unit with <code>1000</code>.
     * @return The number of bytes in a human readable format
     */
    public static String getSize(long size, int precision, boolean longName, boolean realSize) {
        return UploadUtility.getSize(size, precision, longName, realSize);
    }

    /**
     * Checks if given {@link OXException} instance contains a <tt>java.net.SocketException</tt> in its cause chain.
     *
     * @param e The <tt>OXException</tt> instance to check
     * @return <code>true</code> if <tt>OXException</tt> instance contains a <tt>java.net.SocketException</tt> in its cause chain; otherwise <code>false</code>
     */
    public static boolean containsSocketError(OXException e) {
        if (null == e) {
            return false;
        }
        return containsSocketError0(e.getCause());
    }

    private static boolean containsSocketError0(Throwable t) {
        if (null == t) {
            return false;
        }
        if ((t instanceof com.sun.mail.util.MailConnectException) || (t instanceof java.net.SocketException)) {
            // Whatever... Bind error, no route to host, connect error, connection reset, ...
            return true;
        }
        return containsSocketError0(t.getCause());
    }

}
