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

package com.openexchange.mail.mime;

import static com.openexchange.i18n.TextPattern.LINE_SEPARATOR;
import com.openexchange.i18n.LocalizableStrings;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link MimeMailExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class MimeMailExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link MimeMailExceptionMessage}.
     */
    private MimeMailExceptionMessage() {
        super();
    }

    /**
     * There was an issue in authenticating your E-Mail password. This may be because of a recent password change. To continue please logout
     * now and then log back in with your most current password. (server=%1$s | user=%2$s)
     */
    public final static String LOGIN_FAILED_MSG = "There was an issue in authenticating your E-Mail password. This may be due to a recent password change. To continue please log out now and then log back in with your most current password. (server=%1$s | user=%2$s)";

    /**
     * Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s
     */
    public final static String INVALID_CREDENTIALS_MSG = "Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s";

    /**
     * Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server:
     * %5$s
     */
    public final static String INVALID_CREDENTIALS_EXT_MSG = "Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server: %5$s";

    /**
     * Mail folder "%1$s" could not be found.
     */
    public final static String FOLDER_NOT_FOUND_MSG = "Mail folder \"%1$s\" could not be found.";

    /**
     * Mail folder "%1$s" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    public final static String FOLDER_NOT_FOUND_EXT_MSG = "Mail folder \"%1$s\" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).";

    /**
     * Folder "%1$s" has been closed due to some reason.<br>
     * Probably your request took too long.
     * <p>
     * This exception is thrown when a method is invoked on a Messaging object and the Folder that owns that object has died due to some
     * reason. Following the exception, the Folder is reset to the "closed" state.
     * </p>
     */
    public final static String FOLDER_CLOSED_MSG = "Folder \"%1$s\" has been closed."+LINE_SEPARATOR+"Probably your request took too long.";

    /**
     * Folder "%1$s" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) due to some reason.<br>
     * Probably your request took too long.
     */
    public final static String FOLDER_CLOSED_EXT_MSG = "Folder \"%1$s\" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) ."+LINE_SEPARATOR+"Probably your request took too long.";

    /**
     * Illegal write attempt: %1$s
     * <p>
     * The exception thrown when a write is attempted on a read-only attribute of any Messaging object.
     * </p>
     */
    public final static String ILLEGAL_WRITE_MSG = "Illegal write attempt: %1$s";

    /**
     * Mail(s) could not be found in folder
     * <p>
     * The exception thrown when an invalid method is invoked on an expunged Message. The only valid methods on an expunged Message are
     * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
     * </p>
     */
    public final static String MESSAGE_REMOVED = String.format(MailExceptionCode.MAIL_NOT_FOUND.getMessage(), "", "");

    /**
     * Method not supported: %1$s
     * <p>
     * The exception thrown when a method is not supported by the implementation
     * </p>
     */
    public final static String METHOD_NOT_SUPPORTED_MSG = "Method not supported: %1$s";

    /**
     * Session attempts to instantiate a provider that doesn't exist: %1$s
     */
    public final static String NO_SUCH_PROVIDER_MSG = "Session attempts to instantiate a provider that does not exist: %1$s";

    /**
     * Invalid email address %1$s
     */
    public final static String INVALID_EMAIL_ADDRESS_MSG = "Invalid E-Mail address %1$s";

    /**
     * Wrong message header: %1$s
     * <p>
     * The exception thrown due to an error in parsing RFC822 or MIME headers
     * </p>
     */
    public final static String PARSE_ERROR_MSG = "Wrong message header: %1$s";

    /**
     * An attempt was made to open a read-only folder with read-write "%1$s"
     */
    public final static String READ_ONLY_FOLDER_MSG = "An attempt was made to open a read-only folder with read-write \"%1$s\"";

    /**
     * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    public final static String READ_ONLY_FOLDER_EXT_MSG = "An attempt was made to open a read-only folder with read-write \"%1$s\" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)";

    /**
     * Invalid search expression: %1$s
     */
    public final static String SEARCH_ERROR_MSG = "Invalid search expression: %1$s";

    /**
     * Message could not be sent because it is too large
     */
    public final static String MESSAGE_TOO_LARGE_MSG = "Message could not be sent because it is too large";

    /**
     * Message could not be sent to following recipients: %1$s
     * <p>
     * The exception includes those addresses to which the message could not be sent as well as the valid addresses to which the message was
     * sent and valid addresses to which the message was not sent.
     * </p>
     */
    public final static String SEND_FAILED_MSG = "Message could not be sent to the following recipients: %1$s";

    /**
     * Message could not be sent to following recipients: %1$s %2$s (arbitrary server information)
     */
    public final static String SEND_FAILED_EXT_MSG = "Message could not be sent to the following recipients: %1$s %2$s";

    /**
     * Lost connection to mail server.
     */
    public final static String STORE_CLOSED_MSG = "Lost connection to mail server.";

    /**
     * Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).
     */
    public final static String STORE_CLOSED_EXT_MSG = "Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).";

    /**
     * Could not bind mail connection to local port %1$s
     * <p>
     * Signals that an error occurred while attempting to bind a socket to a local address and port. Typically, the port is in use, or the
     * requested local address could not be assigned.
     * </p>
     */
    public final static String BIND_ERROR_MSG = "Could not bind connection to local port %1$s";

    /**
     * Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
     * <p>
     * An error occurred while attempting to connect to remote mail server. Typically, the connection was refused remotely (e.g., no process
     * is listening on the remote address/port).
     * </p>
     */
    public final static String CONNECT_ERROR_MSG = "Connection was refused or timed out while attempting to connect to remote server %1$s for user %2$s.";

    /**
     * Connection was reset
     */
    public final static String CONNECTION_RESET_MSG = "Connection was reset. Please try again.";

    /**
     * No route to host: mail server %1$s cannot be reached
     * <p>
     * Signals that an error occurred while attempting to connect to remote mail server. Typically, the remote mail server cannot be reached
     * because of an intervening firewall, or if an intermediate router is down.
     * </p>
     */
    public final static String NO_ROUTE_TO_HOST_MSG = "No route to host: server (%1$s) cannot be reached";

    /**
     * Port %1$s was unreachable on remote mail server
     */
    public final static String PORT_UNREACHABLE_MSG = "Port %1$s was unreachable on remote server";

    /**
     * Connection is broken due to a socket exception on remote mail server: %1$s
     */
    public final static String BROKEN_CONNECTION_MSG = "Connection is broken due to a socket exception on remote server: %1$s";

    /**
     * A socket error occurred: %1$s
     */
    public final static String SOCKET_ERROR_MSG = "A socket error occurred: %1$s";

    /**
     * The IP address of host "%1$s" could not be determined
     */
    public final static String UNKNOWN_HOST_MSG = "The IP address of host \"%1$s\" could not be determined";

    /**
     * Messaging error: %1$s
     */
    public final static String MESSAGING_ERROR_MSG = "Messaging error: %1$s";

    /**
     * The quota on mail server exceeded. Error message: %1$s
     */
    public final static String QUOTA_EXCEEDED_MSG = "The quota on mail server exceeded. Error message: %1$s";

    /**
     * The quota on mail server "%1$s" exceeded with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    public final static String QUOTA_EXCEEDED_EXT_MSG = "The quota on mail server \"%1$s\" exceeded with login %2$s (user=%3$s, context=%4$s). Error message: %5$s";

    // A command sent to mail server failed. Server response: %1$s
    public final static String COMMAND_FAILED_MSG = "A command sent to mail server failed. Server response: %1$s";

    /**
     * A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    public final static String COMMAND_FAILED_EXT_MSG = "A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s";

    /**
     * Mail server indicates a bad command. Server response: %1$s
     */
    public final static String BAD_COMMAND_MSG = "Mail server indicates a bad command. Server response: %1$s";

    /**
     * Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    public final static String BAD_COMMAND_EXT_MSG = "Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s";

    /**
     * Error in mail server protocol. Error message: %1$s
     */
    public final static String PROTOCOL_ERROR_MSG = "Error in mail server protocol. Error message: %1$s";

    // Protocol error in data sent to the mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
    public final static String PROTOCOL_ERROR_EXT_MSG = "Protocol error in data sent to the mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s";

    /**
     * Message could not be sent: %1$s
     */
    public final static String SEND_FAILED_MSG_MSG = "Message could not be sent: %1$s";

    /**
     * Message could not be sent: %1$s %2$s (arbitrary server information)
     */
    public final static String SEND_FAILED_MSG_EXT_MSG = "Message could not be sent: %1$s %2$s";

    /**
     * Message cannot be displayed.
     */
    public final static String MESSAGE_NOT_DISPLAYED_MSG = "Message cannot be displayed.";

    /**
     * Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s
     */
    public final static String TRANSPORT_INVALID_CREDENTIALS_MSG = "Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s";

    /**
     * Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail
     * transport server: %5$s
     */
    public final static String TRANSPORT_INVALID_CREDENTIALS_EXT_MSG = "Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail transport server: %5$s";

    /**
     * Error processing mail server response. The administrator has been informed.
     */
    public final static String PROCESSING_ERROR_MSG = "Error processing mail server response. The administrator has been informed.";

    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.
     */
    public final static String PROCESSING_ERROR_EXT_MSG = "Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.";

    /**
     * An I/O error occurred: %1$s
     */
    public final static String IO_ERROR_MSG = MailExceptionCode.IO_ERROR.getMessage();

    /**
     * I/O error "%1$s" occurred in communication with "%2$s" mail server for login %3$s (user=%4$s, context=%5$s).
     */
    public final static String IO_ERROR_EXT_MSG = "I/O error \"%1$s\" occurred in communication with \"%2$s\" mail server for login %3$s (user=%4$s, context=%5$s).";

    /**
     * Error processing mail server response. The administrator has been informed. Error message: %1$s
     */
    public final static String PROCESSING_ERROR_WE_MSG = "Error processing mail server response. The administrator has been informed. Error message: %1$s";

    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error
     * message: %5$s
     */
    public final static String PROCESSING_ERROR_WE_EXT_MSG = "Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error message: %5$s";

    public static final String IN_USE_ERROR_MSG = "That mailbox is already in use by another process. Please try again later." + Strings.getLineSeparator() + "Error message: %1$s";

    public static final String IN_USE_ERROR_EXT_MSG = "That mailbox is already in use by another process on %1$s mail server for login %2$s (user=%3$s, context=%4$s). Please try again later." + Strings.getLineSeparator() + "Error message: %5$s";;

}
