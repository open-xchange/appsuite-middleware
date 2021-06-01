/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.mime;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MimeMailExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class MimeMailExceptionMessage implements LocalizableStrings {

    /**
     * There was an issue while authenticating. This may be due to a recent password change. To continue please log out and log back in with
     * your most current password.
     */
    public final static String LOGIN_FAILED_MSG_DISPLAY = "There was an issue while authenticating. This may be due to a recent password change. To continue please log out and log back in with your most current password.";

    /**
     * The provided login information seem to be wrong. Please try again.
     */
    public final static String INVALID_CREDENTIALS_MSG_DISPLAY = "The provided login data seem to be wrong. Please correct them.";

    /**
     * The provided login information to access mail server %1$s seem to be wrong. Please try again.
     */
    public final static String INVALID_CREDENTIALS_EXT_MSG_DISPLAY = "The provided login data to access mail server %1$s seem to be wrong. Please correct them.";

    /**
     * Mail folder "%1$s" could not be found.
     */
    public final static String FOLDER_NOT_FOUND_MSG_DISPLAY = "Mail folder \"%1$s\" could not be found.";

    /**
     * Mail folder "%1$s" could not be found on mail server %2$s.
     */
    public final static String FOLDER_NOT_FOUND_EXT_MSG_DISPLAY = "Mail folder \"%1$s\" could not be found on mail server %2$s.";

    /**
     * Folder "%1$s" has been closed. Probably your request took too long.
     */
    public final static String FOLDER_CLOSED_MSG_DISPLAY = "Lost connection to mail server. Probably your request took too long. Please try again later.";

    /**
     * Folder "%1$s" has been closed on mail server %2$s. Probably your request took too long.
     */
    public final static String FOLDER_CLOSED_EXT_MSG_DISPLAY = "Connection closed to mail server %2$s. Probably your request took too long. Please try again later.";

    /**
     * Mail(s) could not be found in the given folder.
     */
    public final static String MESSAGE_REMOVED_DISPLAY = "Mail(s) could not be found in the given folder.";

    /**
     * The given E-Mail address "%1$s" is invalid.
     */
    public final static String INVALID_EMAIL_ADDRESS_MSG_DISPLAY = "The given E-Mail address \"%1$s\" is invalid.";

    /**
     * You do not have the appropriate permissions to change the content of folder "%1$s".
     */
    public final static String READ_ONLY_FOLDER_MSG_DISPLAY = "You do not have the appropriate permissions to change the content of folder \"%1$s\".";

    /**
     * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    public final static String READ_ONLY_FOLDER_EXT_MSG_DISPLAY = "You do not have the appropriate permissions to change the content of folder \"%1$s\" on mail server %2$s.";

    /**
     * Invalid search expression: %1$s
     */
    public final static String SEARCH_ERROR_MSG_DISPLAY = "The search expression \"%1$s\" you entered is invalid.";

    /**
     * Message could not be sent because it is too large.
     */
    public final static String MESSAGE_TOO_LARGE_MSG_DISPLAY = "Message could not be sent because it is too large.";

    /**
     * Message could not be sent because it is too large.
     */
    public final static String MESSAGE_TOO_LARGE_EXT_MSG_DISPLAY = "Message could not be sent because it is too large (%1$s).";

    /**
     * Message could not be sent to following recipients: %1$s.
     */
    public final static String SEND_FAILED_MSG_DISPLAY = "Message could not be sent to the following recipients: %1$s.";

    /**
     * Message could not be sent to following recipients: %1$s (%2$s)
     */
    public final static String SEND_FAILED_EXT_MSG_DISPLAY = "Message could not be sent to the following recipients: %1$s (%2$s)";

    /**
     * Message could not be sent. Error message from mail transport server: %1$s
     */
    public static final String SEND_FAILED_MSG_ERROR_MSG_DISPLAY = "Message could not be sent. Error message from mail transport server: %1$s";

    /**
     * Message could not be sent. Error message from mail transport server: %1$s (%2$s)
     */
    public static final String SEND_FAILED_EXT_MSG_ERROR_MSG_DISPLAY = "Message could not be sent. Error message from mail transport server: %1$s (%2$s)";

    /**
     * Lost connection to mail server.
     */
    public final static String STORE_CLOSED_MSG_DISPLAY = "Lost connection to mail server. Please try again later.";

    /**
     * Connection closed to mail server %1$s.
     */
    public final static String STORE_CLOSED_EXT_MSG_DISPLAY = "Connection closed to mail server %1$s. Please try again later.";

    /**
     * The connection to remote server %1$s was refused or timed out while attempting to connect.
     */
    public final static String CONNECT_ERROR_MSG_DISPLAY = "The connection to remote server %1$s was refused or timed out while attempting to connect. Please try again later.";

    /**
     * The connection to remote server %1$s timed out while awaiting the response.
     */
    public final static String READ_TIMEOUT_MSG_DISPLAY = "The connection to remote server %1$s timed out while awaiting the response. Please try again later.";

    /**
     * Mail server %1$s unexpectedly closed connection for user %2$s.
     */
    public final static String CONNECTION_CLOSED_MSG_DISPLAY = "Mail server %1$s unexpectedly closed connection. Please try again later.";

    /**
     * The allowed quota on mail server exceeded.
     */
    public final static String QUOTA_EXCEEDED_MSG_DISPLAY = "The allowed quota on mail server exceeded.";

    /**
     * The allowed storage limit for your account \"%6$s\" was exceeded.
     */
    public final static String QUOTA_EXCEEDED_EXT_MSG_DISPLAY = "The allowed storage limit for your account \"%6$s\" was exceeded.";

    /**
     * The mailbox is already in use. Please try again later.
     */
    public static final String IN_USE_ERROR_MSG_DISPLAY = "The mailbox is already in use. Please try again later.";

    /**
     * The mailbox on mail server %1$s for login %2$s is already in use by one of your other processes or clients. Please try again later.
     */
    public static final String IN_USE_ERROR_EXT_MSG_DISPLAY = "The mailbox on mail server %1$s for login %2$s is already in use by one of your other processes or clients. Please try again later.";

    // Wrong or missing login data to access mail transport server %1$s. Please check associated account's settings/credentials.
    public static final String TRANSPORT_INVALID_CREDENTIALS_MSG_DISPLAY = "Wrong or missing login data to access mail transport server %1$s. Please check associated account's settings/credentials.";

    /**
     * The message could not be sent due to a mail server temporary failure. Please try again later.
     */
    public static final String TEMPORARY_FAILURE = "The message could not be sent due to a mail server temporary failure. Please try again later.";

    // Timeout while trying to send to the following recipient: %1$s
    public static final String SEND_TIMED_OUT_ERROR_MSG_DISPLAY = "Timeout while trying to send to the following recipient: %1$s. Please try again later.";

    // Mail server denies access
    public static final String AUTHORIZATION_FAILED_MSG_DISPLAY = "Mail server denies access.";

    // The user tried to append or move a message to a mail folder, which does not exist.
    public static final String TRYCREATE_MSG_DISPLAY = "Destination folder does not exist. Please try to create it and retry the operation.";

    // Temporary failure because a subsystem is down. For example, an IMAP server that uses an LDAP server for authentication might use this response code when the LDAP/Radius server is down.
    public static final String TEMPORARY_AUTH_FAILURE_MSG_DISPLAY = "A temporary failure occurred during login. Please try again later.";

    // Either authentication succeeded or the server no longer had the necessary data; either way, access is no longer permitted using that passphrase. The client or user should get a new passphrase.
    public static final String PASSWORD_EXPIRED_MSG_DISPLAY = "Access to mail server is no longer permitted using your password. Please obtain a new one.";

    // Access to mail server is not permitted due to a lack of privacy. E.g. client is supposed to switch to a connection with Transport Layer Security (TLS).
    public static final String PRIVACY_REQUIRED_MSG_DISPLAY = "Access to mail server is not permitted due to a lack of privacy.";

    // Message was blocked by outgoing SMTP server; e.g. due to triggering a filter such as a URL in the message being found in a domain black list.
    public static final String MESSAGE_REJECTED_MSG_DISPLAY = "Message could not be sent because it has been rejected.";

    // Message was blocked by outgoing SMTP server; e.g. due to triggering a filter such as a URL in the message being found in a domain black list.
    public static final String MESSAGE_REJECTED_EXT_MSG_DISPLAY = "Message could not be sent because it has been rejected (%1$s).";

    // A remote end-point abruptly aborted the connection; e.g. SMTP server goes down while currently trying to send a message.
    public static final String CONNECTION_RESET_MSG_DISPLAY = "Remote end-point abruptly aborted the connection. Please try again later.";

    // A remote end-point abruptly aborted the connection; e.g. SMTP server goes down while currently trying to send a message.
    public static final String CONNECTION_RESET_EXT_MSG_DISPLAY = "Remote end-point \"%1$s\" abruptly aborted the connection. Please try again later.";

}
