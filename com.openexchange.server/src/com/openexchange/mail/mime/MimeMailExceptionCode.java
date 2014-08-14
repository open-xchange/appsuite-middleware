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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * For MIME related errors.
 * <p>
 * Taken from {@link OXException}:
 * <p>
 * The detail number range in subclasses generated in mail bundles is supposed to start with 2000 and may go up to 2999.
 * <p>
 * The detail number range in subclasses generated in transport bundles is supposed to start with 3000 and may go up to 3999.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MimeMailExceptionCode implements DisplayableOXExceptionCode {

    /**
     * There was an issue in authenticating your E-Mail password. This may be because of a recent password change. To continue please logout
     * now and then log back in with your most current password. (server=%1$s | user=%2$s)
     */
    LOGIN_FAILED(MimeMailExceptionCode.LOGIN_FAILED_MSG, CATEGORY_PERMISSION_DENIED, 1000, MimeMailExceptionMessage.LOGIN_FAILED_MSG_DISPLAY),
    /**
     * Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s
     */
    INVALID_CREDENTIALS(MimeMailExceptionCode.INVALID_CREDENTIALS_MSG, CATEGORY_PERMISSION_DENIED, 1001, MimeMailExceptionMessage.INVALID_CREDENTIALS_MSG_DISPLAY),
    /**
     * Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server:
     * %5$s
     */
    INVALID_CREDENTIALS_EXT(MimeMailExceptionCode.INVALID_CREDENTIALS_EXT_MSG, CATEGORY_PERMISSION_DENIED, INVALID_CREDENTIALS.detailNumber, MimeMailExceptionMessage.INVALID_CREDENTIALS_EXT_MSG_DISPLAY),
    /**
     * Mail folder "%1$s" could not be found.
     */
    FOLDER_NOT_FOUND(MimeMailExceptionCode.FOLDER_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 1002, MimeMailExceptionMessage.FOLDER_NOT_FOUND_MSG_DISPLAY),
    /**
     * Mail folder "%1$s" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    FOLDER_NOT_FOUND_EXT(MimeMailExceptionCode.FOLDER_NOT_FOUND_EXT_MSG, CATEGORY_USER_INPUT, FOLDER_NOT_FOUND.detailNumber, MimeMailExceptionMessage.FOLDER_NOT_FOUND_EXT_MSG_DISPLAY),
    /**
     * Folder "%1$s" has been closed due to some reason.<br>
     * Probably your request took too long.
     * <p>
     * This exception is thrown when a method is invoked on a Messaging object and the Folder that owns that object has died due to some
     * reason. Following the exception, the Folder is reset to the "closed" state.
     * </p>
     */
    FOLDER_CLOSED(MimeMailExceptionCode.FOLDER_CLOSED_MSG, CATEGORY_ERROR, 1003, MimeMailExceptionMessage.FOLDER_CLOSED_MSG_DISPLAY),
    /**
     * Folder "%1$s" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) due to some reason.<br>
     * Probably your request took too long.
     */
    FOLDER_CLOSED_EXT(MimeMailExceptionCode.FOLDER_CLOSED_EXT_MSG, CATEGORY_ERROR, FOLDER_CLOSED.detailNumber, MimeMailExceptionMessage.FOLDER_CLOSED_EXT_MSG_DISPLAY),
    /**
     * Illegal write attempt: %1$s
     * <p>
     * The exception thrown when a write is attempted on a read-only attribute of any Messaging object.
     * </p>
     */
    ILLEGAL_WRITE(MimeMailExceptionCode.ILLEGAL_WRITE_MSG, CATEGORY_ERROR, 1004),
    /**
     * Mail(s) could not be found in folder
     * <p>
     * The exception thrown when an invalid method is invoked on an expunged Message. The only valid methods on an expunged Message are
     * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
     * </p>
     */
    MESSAGE_REMOVED(MimeMailExceptionCode.MESSAGE_REMOVED_MSG, CATEGORY_USER_INPUT, 32, MimeMailExceptionMessage.MESSAGE_REMOVED_DISPLAY),
    /**
     * Method not supported: %1$s
     * <p>
     * The exception thrown when a method is not supported by the implementation
     * </p>
     */
    METHOD_NOT_SUPPORTED(MimeMailExceptionCode.METHOD_NOT_SUPPORTED_MSG, CATEGORY_ERROR, 1006),
    /**
     * Session attempts to instantiate a provider that doesn't exist: %1$s
     */
    NO_SUCH_PROVIDER(MimeMailExceptionCode.NO_SUCH_PROVIDER_MSG, CATEGORY_ERROR, 1007),
    /**
     * Invalid email address %1$s
     */
    INVALID_EMAIL_ADDRESS(MimeMailExceptionCode.INVALID_EMAIL_ADDRESS_MSG, CATEGORY_USER_INPUT, 1008, MimeMailExceptionMessage.INVALID_EMAIL_ADDRESS_MSG_DISPLAY),
    /**
     * Wrong message header: %1$s
     * <p>
     * The exception thrown due to an error in parsing RFC822 or MIME headers
     * </p>
     */
    PARSE_ERROR(MimeMailExceptionCode.PARSE_ERROR_MSG, CATEGORY_ERROR, 1009),
    /**
     * An attempt was made to open a read-only folder with read-write "%1$s"
     */
    READ_ONLY_FOLDER(MimeMailExceptionCode.READ_ONLY_FOLDER_MSG, CATEGORY_PERMISSION_DENIED, 1010, MimeMailExceptionMessage.READ_ONLY_FOLDER_MSG_DISPLAY),
    /**
     * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    READ_ONLY_FOLDER_EXT(MimeMailExceptionCode.READ_ONLY_FOLDER_EXT_MSG, CATEGORY_PERMISSION_DENIED, 1010, MimeMailExceptionMessage.READ_ONLY_FOLDER_EXT_MSG_DISPLAY),
    /**
     * Invalid search expression: %1$s
     */
    SEARCH_ERROR(MimeMailExceptionCode.SEARCH_ERROR_MSG, CATEGORY_USER_INPUT, 1011, MimeMailExceptionMessage.SEARCH_ERROR_MSG_DISPLAY),
    /**
     * Message could not be sent because it is too large
     */
    MESSAGE_TOO_LARGE(MimeMailExceptionCode.MESSAGE_TOO_LARGE_MSG, CATEGORY_USER_INPUT, 1012, MimeMailExceptionMessage.MESSAGE_TOO_LARGE_MSG_DISPLAY),
    /**
     * Message could not be sent because it is too large (%1$s) (<i>arbitrary server information</i>)
     */
    MESSAGE_TOO_LARGE_EXT(MimeMailExceptionCode.MESSAGE_TOO_LARGE_EXT_MSG, CATEGORY_USER_INPUT, 1012, MimeMailExceptionMessage.MESSAGE_TOO_LARGE_EXT_MSG_DISPLAY),
    /**
     * Message could not be sent to following recipients: %1$s
     * <p>
     * The exception includes those addresses to which the message could not be sent as well as the valid addresses to which the message was
     * sent and valid addresses to which the message was not sent.
     * </p>
     */
    SEND_FAILED(MimeMailExceptionCode.SEND_FAILED_MSG, CATEGORY_USER_INPUT, 1013, MimeMailExceptionMessage.SEND_FAILED_MSG_DISPLAY),
    /**
     * Message could not be sent to following recipients: %1$s (%2$s) (<i>arbitrary server information</i>)
     */
    SEND_FAILED_EXT(MimeMailExceptionCode.SEND_FAILED_EXT_MSG, CATEGORY_USER_INPUT, 1013, MimeMailExceptionMessage.SEND_FAILED_EXT_MSG_DISPLAY),
    /**
     * Lost connection to mail server.
     */
    STORE_CLOSED(MimeMailExceptionCode.STORE_CLOSED_MSG, CATEGORY_SERVICE_DOWN, 1014, MimeMailExceptionMessage.STORE_CLOSED_MSG_DISPLAY),
    /**
     * Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).
     */
    STORE_CLOSED_EXT(MimeMailExceptionCode.STORE_CLOSED_EXT_MSG, STORE_CLOSED.category, STORE_CLOSED.detailNumber, MimeMailExceptionMessage.STORE_CLOSED_EXT_MSG_DISPLAY),
    /**
     * Could not bind mail connection to local port %1$s
     * <p>
     * Signals that an error occurred while attempting to bind a socket to a local address and port. Typically, the port is in use, or the
     * requested local address could not be assigned.
     * </p>
     */
    BIND_ERROR(MimeMailExceptionCode.BIND_ERROR_MSG, CATEGORY_CONFIGURATION, 1015),
    /**
     * Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
     * <p>
     * An error occurred while attempting to connect to remote mail server. Typically, the connection was refused remotely (e.g., no process
     * is listening on the remote address/port).
     * </p>
     */
    CONNECT_ERROR(MimeMailExceptionCode.CONNECT_ERROR_MSG, CATEGORY_SERVICE_DOWN, 1016, MimeMailExceptionMessage.CONNECT_ERROR_MSG_DISPLAY),
    /**
     * Connection was reset
     */
    CONNECTION_RESET(MimeMailExceptionCode.CONNECTION_RESET_MSG, CATEGORY_TRY_AGAIN, 1017),
    /**
     * No route to host: mail server %1$s cannot be reached
     * <p>
     * Signals that an error occurred while attempting to connect to remote mail server. Typically, the remote mail server cannot be reached
     * because of an intervening firewall, or if an intermediate router is down.
     * </p>
     */
    NO_ROUTE_TO_HOST(MimeMailExceptionCode.NO_ROUTE_TO_HOST_MSG, CATEGORY_SERVICE_DOWN, 1018),
    /**
     * Port %1$s was unreachable on remote mail server
     */
    PORT_UNREACHABLE(MimeMailExceptionCode.PORT_UNREACHABLE_MSG, CATEGORY_CONFIGURATION, 1019),
    /**
     * Connection is broken due to a socket exception on remote mail server: %1$s
     */
    BROKEN_CONNECTION(MimeMailExceptionCode.BROKEN_CONNECTION_MSG, CATEGORY_SERVICE_DOWN, 1020),
    /**
     * A socket error occurred: %1$s
     */
    SOCKET_ERROR(MimeMailExceptionCode.SOCKET_ERROR_MSG, CATEGORY_ERROR, 1021),
    /**
     * The IP address of host "%1$s" could not be determined
     */
    UNKNOWN_HOST(MimeMailExceptionCode.UNKNOWN_HOST_MSG, CATEGORY_CONFIGURATION, 1022),
    /**
     * Messaging error: %1$s
     */
    MESSAGING_ERROR(MimeMailExceptionCode.MESSAGING_ERROR_MSG, CATEGORY_ERROR, 1023),
    /**
     * The quota on mail server exceeded. Error message: %1$s
     */
    QUOTA_EXCEEDED(MimeMailExceptionCode.QUOTA_EXCEEDED_MSG, CATEGORY_CAPACITY, 1024, MimeMailExceptionMessage.QUOTA_EXCEEDED_MSG_DISPLAY),
    /**
     * The quota on mail server "%1$s" exceeded with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    QUOTA_EXCEEDED_EXT(MimeMailExceptionCode.QUOTA_EXCEEDED_EXT_MSG, QUOTA_EXCEEDED.category, QUOTA_EXCEEDED.detailNumber, MimeMailExceptionMessage.QUOTA_EXCEEDED_EXT_MSG_DISPLAY),
    /**
     * A command sent to mail server failed. Server response: %1$s
     */
    COMMAND_FAILED(MimeMailExceptionCode.COMMAND_FAILED_MSG, CATEGORY_ERROR, 1025),
    /**
     * A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    COMMAND_FAILED_EXT(MimeMailExceptionCode.COMMAND_FAILED_EXT_MSG, COMMAND_FAILED.category, COMMAND_FAILED.detailNumber),
    /**
     * Mail server indicates a bad command. Server response: %1$s
     */
    BAD_COMMAND(MimeMailExceptionCode.BAD_COMMAND_MSG, CATEGORY_ERROR, 1026),
    /**
     * Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    BAD_COMMAND_EXT(MimeMailExceptionCode.BAD_COMMAND_EXT_MSG, BAD_COMMAND.category, BAD_COMMAND.detailNumber),
    /**
     * An error in mail server protocol. Error message: %1$s
     */
    PROTOCOL_ERROR(MimeMailExceptionCode.PROTOCOL_ERROR_MSG, CATEGORY_ERROR, 1027),
    /**
     * Protocol error in data sent to the mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    PROTOCOL_ERROR_EXT(MimeMailExceptionCode.PROTOCOL_ERROR_EXT_MSG, PROTOCOL_ERROR.category, PROTOCOL_ERROR.detailNumber),
    /**
     * Message could not be sent: %1$s
     */
    SEND_FAILED_MSG_ERROR(MimeMailExceptionCode.SEND_FAILED_MSG, CATEGORY_ERROR, 1028, MimeMailExceptionMessage.SEND_FAILED_MSG_DISPLAY),
    /**
     * Message could not be sent: %1$s %2$s (arbitrary server information)
     */
    SEND_FAILED_MSG_EXT_ERROR(MimeMailExceptionCode.SEND_FAILED_EXT_MSG, CATEGORY_ERROR, 1028, MimeMailExceptionMessage.SEND_FAILED_EXT_MSG_DISPLAY),
    /**
     * Message cannot be displayed.
     */
    MESSAGE_NOT_DISPLAYED(MimeMailExceptionCode.MESSAGE_NOT_DISPLAYED_MSG, CATEGORY_ERROR, 1029),
    /**
     * Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s
     */
    TRANSPORT_INVALID_CREDENTIALS(MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS_MSG, CATEGORY_PERMISSION_DENIED, 1030, MimeMailExceptionMessage.TRANSPORT_INVALID_CREDENTIALS_MSG_DISPLAY),
    /**
     * Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail
     * transport server: %5$s
     */
    TRANSPORT_INVALID_CREDENTIALS_EXT(MimeMailExceptionCode.TRANSPORT_INVALID_CREDENTIALS_EXT_MSG, CATEGORY_PERMISSION_DENIED, TRANSPORT_INVALID_CREDENTIALS.detailNumber),
    /**
     * Error processing mail server response. The administrator has been informed.
     */
    PROCESSING_ERROR(MimeMailExceptionCode.PROCESSING_ERROR_MSG, CATEGORY_ERROR, 1031),
    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.
     */
    PROCESSING_ERROR_EXT(MimeMailExceptionCode.PROCESSING_ERROR_EXT_MSG, CATEGORY_ERROR, PROCESSING_ERROR.detailNumber),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(MimeMailExceptionCode.IO_ERROR_MSG, CATEGORY_ERROR, 8),
    /**
     * I/O error "%1$s" occurred in communication with "%2$s" mail server for login %3$s (user=%4$s, context=%5$s).
     */
    IO_ERROR_EXT(MimeMailExceptionCode.IO_ERROR_EXT_MSG, CATEGORY_ERROR, 8),
    /**
     * Error processing mail server response. The administrator has been informed. Error message: %1$s
     */
    PROCESSING_ERROR_WE(MimeMailExceptionCode.PROCESSING_ERROR_WE_MSG, CATEGORY_ERROR, PROCESSING_ERROR.detailNumber),
    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error
     * message: %5$s
     */
    PROCESSING_ERROR_WE_EXT(MimeMailExceptionCode.PROCESSING_ERROR_WE_EXT_MSG, CATEGORY_ERROR, PROCESSING_ERROR_WE.detailNumber),
    /**
     * That mailbox is already in use by another process. Please try again later.<br>
     * Error message: %1$s
     */
    IN_USE_ERROR(MimeMailExceptionCode.IN_USE_ERROR_MSG, CATEGORY_USER_INPUT, PROCESSING_ERROR.detailNumber),
    /**
     * That mailbox is already in use by another process on %1$s mail server for login %2$s (user=%3$s, context=%4$s). Please try again later.<br>
     * Error message: %5$s
     */
    IN_USE_ERROR_EXT(MimeMailExceptionCode.IN_USE_ERROR_EXT_MSG, CATEGORY_USER_INPUT, PROCESSING_ERROR.detailNumber),

    ;

    /**
     * There was an issue in authenticating your E-Mail password. This may be because of a recent password change. To continue please logout
     * now and then log back in with your most current password. (server=%1$s | user=%2$s)
     */
    private final static String LOGIN_FAILED_MSG = "There was an issue in authenticating your E-Mail password. This may be due to a recent password change. To continue please log out now and then log back in with your most current password. (server=%1$s | user=%2$s)";

    /**
     * Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s
     */
    private final static String INVALID_CREDENTIALS_MSG = "Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s";

    /**
     * Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server:
     * %5$s
     */
    private final static String INVALID_CREDENTIALS_EXT_MSG = "Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server: %5$s";

    /**
     * Mail folder "%1$s" could not be found.
     */
    private final static String FOLDER_NOT_FOUND_MSG = "Mail folder \"%1$s\" could not be found.";

    /**
     * Mail folder "%1$s" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    private final static String FOLDER_NOT_FOUND_EXT_MSG = "Mail folder \"%1$s\" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).";

    /**
     * Folder "%1$s" has been closed due to some reason.<br>
     * Probably your request took too long.
     * <p>
     * This exception is thrown when a method is invoked on a Messaging object and the Folder that owns that object has died due to some
     * reason. Following the exception, the Folder is reset to the "closed" state.
     * </p>
     */
    private final static String FOLDER_CLOSED_MSG = "Folder \"%1$s\" has been closed. Probably your request took too long.";

    /**
     * Folder "%1$s" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) due to some reason.<br>
     * Probably your request took too long.
     */
    private final static String FOLDER_CLOSED_EXT_MSG = "Folder \"%1$s\" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s). Probably your request took too long.";

    /**
     * Illegal write attempt: %1$s
     * <p>
     * The exception thrown when a write is attempted on a read-only attribute of any Messaging object.
     * </p>
     */
    private final static String ILLEGAL_WRITE_MSG = "Illegal write attempt: %1$s";

    /**
     * Mail(s) could not be found in folder
     * <p>
     * The exception thrown when an invalid method is invoked on an expunged Message. The only valid methods on an expunged Message are
     * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
     * </p>
     */
    private final static String MESSAGE_REMOVED_MSG = "Mail(s) %1$s could not be found in folder %2$s";

    /**
     * Method not supported: %1$s
     * <p>
     * The exception thrown when a method is not supported by the implementation
     * </p>
     */
    private final static String METHOD_NOT_SUPPORTED_MSG = "Method not supported: %1$s";

    /**
     * Session attempts to instantiate a provider that doesn't exist: %1$s
     */
    private final static String NO_SUCH_PROVIDER_MSG = "Session attempts to instantiate a provider that does not exist: %1$s";

    /**
     * Invalid email address %1$s
     */
    private final static String INVALID_EMAIL_ADDRESS_MSG = "Invalid E-Mail address %1$s";

    /**
     * Wrong message header: %1$s
     * <p>
     * The exception thrown due to an error in parsing RFC822 or MIME headers
     * </p>
     */
    private final static String PARSE_ERROR_MSG = "Wrong message header: %1$s";

    /**
     * An attempt was made to open a read-only folder with read-write "%1$s"
     */
    private final static String READ_ONLY_FOLDER_MSG = "An attempt was made to open a read-only folder with read-write \"%1$s\"";

    /**
     * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    private final static String READ_ONLY_FOLDER_EXT_MSG = "An attempt was made to open a read-only folder with read-write \"%1$s\" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)";

    /**
     * Invalid search expression: %1$s
     */
    private final static String SEARCH_ERROR_MSG = "Invalid search expression: %1$s";

    /**
     * Message could not be sent because it is too large
     */
    private final static String MESSAGE_TOO_LARGE_MSG = "Message could not be sent because it is too large";

    /**
     * Message could not be sent because it is too large (%1$s) (<i>arbitrary server information</i>)
     */
    private final static String MESSAGE_TOO_LARGE_EXT_MSG = "Message could not be sent because it is too large (%1$s)";

    /**
     * Message could not be sent to following recipients: %1$s
     * <p>
     * The exception includes those addresses to which the message could not be sent as well as the valid addresses to which the message was
     * sent and valid addresses to which the message was not sent.
     * </p>
     */
    private final static String SEND_FAILED_MSG = "Message could not be sent to the following recipients: %1$s";

    /**
     * Message could not be sent to following recipients: %1$s (%2$s) (<i>arbitrary server information</i>)
     */
    private final static String SEND_FAILED_EXT_MSG = "Message could not be sent to the following recipients: %1$s (%2$s)";

    /**
     * Lost connection to mail server.
     */
    private final static String STORE_CLOSED_MSG = "Lost connection to mail server.";

    /**
     * Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).
     */
    private final static String STORE_CLOSED_EXT_MSG = "Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).";

    /**
     * Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
     * <p>
     * An error occurred while attempting to connect to remote mail server. Typically, the connection was refused remotely (e.g., no process
     * is listening on the remote address/port).
     * </p>
     */
    private final static String CONNECT_ERROR_MSG = "Connection was refused or timed out while attempting to connect to remote server %1$s for user %2$s.";

    /**
     * Could not bind mail connection to local port %1$s
     * <p>
     * Signals that an error occurred while attempting to bind a socket to a local address and port. Typically, the port is in use, or the
     * requested local address could not be assigned.
     * </p>
     */
    private final static String BIND_ERROR_MSG = "Could not bind connection to local port %1$s";

    /**
     * Connection was reset
     */
    private final static String CONNECTION_RESET_MSG = "Connection was reset. Please try again.";

    /**
     * No route to host: mail server %1$s cannot be reached
     * <p>
     * Signals that an error occurred while attempting to connect to remote mail server. Typically, the remote mail server cannot be reached
     * because of an intervening firewall, or if an intermediate router is down.
     * </p>
     */
    private final static String NO_ROUTE_TO_HOST_MSG = "No route to host: server (%1$s) cannot be reached";

    /**
     * Port %1$s was unreachable on remote mail server
     */
    private final static String PORT_UNREACHABLE_MSG = "Port %1$s was unreachable on remote server";

    /**
     * Connection is broken due to a socket exception on remote mail server: %1$s
     */
    private final static String BROKEN_CONNECTION_MSG = "Connection is broken due to a socket exception on remote server: %1$s";

    /**
     * A socket error occurred: %1$s
     */
    private final static String SOCKET_ERROR_MSG = "A socket error occurred: %1$s";

    /**
     * The IP address of host "%1$s" could not be determined
     */
    private final static String UNKNOWN_HOST_MSG = "The IP address of host \"%1$s\" could not be determined";

    /**
     * Messaging error: %1$s
     */
    private final static String MESSAGING_ERROR_MSG = "Messaging error: %1$s";

    /**
     * The quota on mail server exceeded. Error message: %1$s
     */
    private final static String QUOTA_EXCEEDED_MSG = "The quota on mail server exceeded. Error message: %1$s";

    /**
     * The quota on mail server "%1$s" exceeded with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    private final static String QUOTA_EXCEEDED_EXT_MSG = "The quota on mail server \"%1$s\" exceeded with login %2$s (user=%3$s, context=%4$s). Error message: %5$s";

    // A command sent to mail server failed. Server response: %1$s
    private final static String COMMAND_FAILED_MSG = "A command sent to mail server failed. Server response: %1$s";

    /**
     * A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    private final static String COMMAND_FAILED_EXT_MSG = "A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s";

    /**
     * Mail server indicates a bad command. Server response: %1$s
     */
    private final static String BAD_COMMAND_MSG = "Mail server indicates a bad command. Server response: %1$s";

    /**
     * Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    private final static String BAD_COMMAND_EXT_MSG = "Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s";

    /**
     * Error in mail server protocol. Error message: %1$s
     */
    private final static String PROTOCOL_ERROR_MSG = "Error in mail server protocol. Error message: %1$s";

    /**
     * Protocol error in data sent to the mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    private final static String PROTOCOL_ERROR_EXT_MSG = "Protocol error in data sent to the mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s";

    /**
     * Message cannot be displayed.
     */
    private final static String MESSAGE_NOT_DISPLAYED_MSG = "Message cannot be displayed.";

    /**
     * Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s
     */
    private final static String TRANSPORT_INVALID_CREDENTIALS_MSG = "Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s";

    /**
     * Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail
     * transport server: %5$s
     */
    private final static String TRANSPORT_INVALID_CREDENTIALS_EXT_MSG = "Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail transport server: %5$s";

    /**
     * Error processing mail server response. The administrator has been informed.
     */
    private final static String PROCESSING_ERROR_MSG = "Error processing mail server response. The administrator has been informed.";

    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.
     */
    private final static String PROCESSING_ERROR_EXT_MSG = "Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.";

    /**
     * An I/O error occurred: %1$s
     */
    private final static String IO_ERROR_MSG = "An I/O error occurred: %1$s.";

    /**
     * I/O error "%1$s" occurred in communication with "%2$s" mail server for login %3$s (user=%4$s, context=%5$s).
     */
    private final static String IO_ERROR_EXT_MSG = "I/O error \"%1$s\" occurred in communication with \"%2$s\" mail server for login %3$s (user=%4$s, context=%5$s).";

    /**
     * Error processing mail server response. The administrator has been informed. Error message: %1$s
     */
    private final static String PROCESSING_ERROR_WE_MSG = "Error processing mail server response. The administrator has been informed. Error message: %1$s";

    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error
     * message: %5$s
     */
    private final static String PROCESSING_ERROR_WE_EXT_MSG = "Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error message: %5$s";

    /**
     * That mailbox is already in use by another process. Please try again later. Error message: %1$s
     */
    private static final String IN_USE_ERROR_MSG = "That mailbox is already in use by another process. Please try again later. Error message: %1$s";

    /**
     * That mailbox is already in use by another process on %1$s mail server for login %2$s (user=%3$s, context=%4$s). Please try again
     * later. Error message: %5$s
     */
    private static final String IN_USE_ERROR_EXT_MSG = "That mailbox is already in use by another process on %1$s mail server for login %2$s (user=%3$s, context=%4$s). Please try again later. Error message: %5$s";

    private final String message;
    private final int detailNumber;
    private final Category category;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    private MimeMailExceptionCode(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    private MimeMailExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    @Override
    public String getPrefix() {
        return "MSG";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
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
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
