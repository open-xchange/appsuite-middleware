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
    LOGIN_FAILED("There was an issue in authenticating your E-Mail password. This may be due to a recent password change. To continue please log out now and then log back in with your most current password. (server=%1$s | user=%2$s)", CATEGORY_PERMISSION_DENIED, 1000, MimeMailExceptionMessage.LOGIN_FAILED_MSG_DISPLAY),
    /**
     * Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s
     */
    INVALID_CREDENTIALS("Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s", CATEGORY_PERMISSION_DENIED, 1001, MimeMailExceptionMessage.INVALID_CREDENTIALS_MSG_DISPLAY),
    /**
     * Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server:
     * %5$s
     */
    INVALID_CREDENTIALS_EXT("Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server: %5$s", CATEGORY_PERMISSION_DENIED, INVALID_CREDENTIALS.detailNumber, MimeMailExceptionMessage.INVALID_CREDENTIALS_EXT_MSG_DISPLAY),
    /**
     * Mail folder "%1$s" could not be found.
     */
    FOLDER_NOT_FOUND("Mail folder \"%1$s\" could not be found.", CATEGORY_USER_INPUT, 1002, MimeMailExceptionMessage.FOLDER_NOT_FOUND_MSG_DISPLAY),
    /**
     * Mail folder "%1$s" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    FOLDER_NOT_FOUND_EXT("Mail folder \"%1$s\" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).", CATEGORY_USER_INPUT, FOLDER_NOT_FOUND.detailNumber, MimeMailExceptionMessage.FOLDER_NOT_FOUND_EXT_MSG_DISPLAY),
    /**
     * Folder "%1$s" has been closed due to some reason.<br>
     * Probably your request took too long.
     * <p>
     * This exception is thrown when a method is invoked on a Messaging object and the Folder that owns that object has died due to some
     * reason. Following the exception, the Folder is reset to the "closed" state.
     * </p>
     */
    FOLDER_CLOSED("Folder \"%1$s\" has been closed. Probably your request took too long.", CATEGORY_ERROR, 1003, MimeMailExceptionMessage.FOLDER_CLOSED_MSG_DISPLAY),
    /**
     * Folder "%1$s" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) due to some reason.<br>
     * Probably your request took too long.
     */
    FOLDER_CLOSED_EXT("Folder \"%1$s\" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s). Probably your request took too long.", CATEGORY_ERROR, FOLDER_CLOSED.detailNumber, MimeMailExceptionMessage.FOLDER_CLOSED_EXT_MSG_DISPLAY),
    /**
     * Illegal write attempt: %1$s
     * <p>
     * The exception thrown when a write is attempted on a read-only attribute of any Messaging object.
     * </p>
     */
    ILLEGAL_WRITE("Illegal write attempt: %1$s", CATEGORY_ERROR, 1004),
    /**
     * Mail(s) could not be found in folder
     * <p>
     * The exception thrown when an invalid method is invoked on an expunged Message. The only valid methods on an expunged Message are
     * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
     * </p>
     */
    MESSAGE_REMOVED("Mail(s) %1$s could not be found in folder %2$s", CATEGORY_USER_INPUT, 32, MimeMailExceptionMessage.MESSAGE_REMOVED_DISPLAY),
    /**
     * Method not supported: %1$s
     * <p>
     * The exception thrown when a method is not supported by the implementation
     * </p>
     */
    METHOD_NOT_SUPPORTED("Method not supported: %1$s", CATEGORY_ERROR, 1006),
    /**
     * Session attempts to instantiate a provider that doesn't exist: %1$s
     */
    NO_SUCH_PROVIDER("Session attempts to instantiate a provider that does not exist: %1$s", CATEGORY_ERROR, 1007),
    /**
     * Invalid email address: %1$s
     */
    INVALID_EMAIL_ADDRESS("Invalid E-Mail address: %1$s", CATEGORY_USER_INPUT, 1008, MimeMailExceptionMessage.INVALID_EMAIL_ADDRESS_MSG_DISPLAY),
    /**
     * Wrong message header: %1$s
     * <p>
     * The exception thrown due to an error in parsing RFC822 or MIME headers
     * </p>
     */
    PARSE_ERROR("Wrong message header: %1$s", CATEGORY_ERROR, 1009),
    /**
     * An attempt was made to open a read-only folder with read-write "%1$s"
     */
    READ_ONLY_FOLDER("An attempt was made to open a read-only folder with read-write \"%1$s\"", CATEGORY_PERMISSION_DENIED, 1010, MimeMailExceptionMessage.READ_ONLY_FOLDER_MSG_DISPLAY),
    /**
     * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    READ_ONLY_FOLDER_EXT("An attempt was made to open a read-only folder with read-write \"%1$s\" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)", CATEGORY_PERMISSION_DENIED, 1010, MimeMailExceptionMessage.READ_ONLY_FOLDER_EXT_MSG_DISPLAY),
    /**
     * Invalid search expression: %1$s
     */
    SEARCH_ERROR("Invalid search expression: %1$s", CATEGORY_USER_INPUT, 1011, MimeMailExceptionMessage.SEARCH_ERROR_MSG_DISPLAY),
    /**
     * Message could not be sent because it is too large
     */
    MESSAGE_TOO_LARGE("Message could not be sent because it is too large", CATEGORY_USER_INPUT, 1012, MimeMailExceptionMessage.MESSAGE_TOO_LARGE_MSG_DISPLAY),
    /**
     * Message could not be sent because it is too large (%1$s) (<i>arbitrary server information</i>)
     */
    MESSAGE_TOO_LARGE_EXT("Message could not be sent because it is too large (%1$s)", CATEGORY_USER_INPUT, 1012, MimeMailExceptionMessage.MESSAGE_TOO_LARGE_EXT_MSG_DISPLAY),
    /**
     * Message could not be sent to following recipients: %1$s
     * <p>
     * The exception includes those addresses to which the message could not be sent as well as the valid addresses to which the message was
     * sent and valid addresses to which the message was not sent.
     * </p>
     */
    SEND_FAILED("Message could not be sent to the following recipients: %1$s", CATEGORY_USER_INPUT, 1013, MimeMailExceptionMessage.SEND_FAILED_MSG_DISPLAY),
    /**
     * Message could not be sent to following recipients: %1$s (%2$s) (<i>arbitrary server information</i>)
     */
    SEND_FAILED_EXT("Message could not be sent to the following recipients: %1$s (%2$s)", CATEGORY_USER_INPUT, 1013, MimeMailExceptionMessage.SEND_FAILED_EXT_MSG_DISPLAY),
    /**
     * Lost connection to mail server.
     */
    STORE_CLOSED("Lost connection to mail server.", CATEGORY_SERVICE_DOWN, 1014, MimeMailExceptionMessage.STORE_CLOSED_MSG_DISPLAY),
    /**
     * Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).
     */
    STORE_CLOSED_EXT("Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).", STORE_CLOSED.category, STORE_CLOSED.detailNumber, MimeMailExceptionMessage.STORE_CLOSED_EXT_MSG_DISPLAY),
    /**
     * Could not bind mail connection to local port %1$s
     * <p>
     * Signals that an error occurred while attempting to bind a socket to a local address and port. Typically, the port is in use, or the
     * requested local address could not be assigned.
     * </p>
     */
    BIND_ERROR("Could not bind connection to local port %1$s", CATEGORY_CONFIGURATION, 1015),
    /**
     * Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
     * <p>
     * An error occurred while attempting to connect to remote mail server. Typically, the connection was refused remotely (e.g., no process
     * is listening on the remote address/port).
     * </p>
     */
    CONNECT_ERROR("Connection was refused or timed out while attempting to connect to remote server %1$s for user %2$s.", CATEGORY_SERVICE_DOWN, 1016, MimeMailExceptionMessage.CONNECT_ERROR_MSG_DISPLAY),
    /**
     * Connection was reset
     */
    CONNECTION_RESET("Connection was reset. Please try again.", CATEGORY_TRY_AGAIN, 1017),
    /**
     * No route to host: mail server %1$s cannot be reached
     * <p>
     * Signals that an error occurred while attempting to connect to remote mail server. Typically, the remote mail server cannot be reached
     * because of an intervening firewall, or if an intermediate router is down.
     * </p>
     */
    NO_ROUTE_TO_HOST("No route to host: server (%1$s) cannot be reached", CATEGORY_SERVICE_DOWN, 1018),
    /**
     * Port %1$s was unreachable on remote mail server
     */
    PORT_UNREACHABLE("Port %1$s was unreachable on remote server", CATEGORY_CONFIGURATION, 1019),
    /**
     * Connection is broken due to a socket exception on remote mail server: %1$s
     */
    BROKEN_CONNECTION("Connection is broken due to a socket exception on remote server: %1$s", CATEGORY_SERVICE_DOWN, 1020),
    /**
     * A socket error occurred
     */
    SOCKET_ERROR("A socket error occurred", CATEGORY_ERROR, 1021),
    /**
     * The IP address of host "%1$s" could not be determined
     */
    UNKNOWN_HOST("The IP address of host \"%1$s\" could not be determined", CATEGORY_CONFIGURATION, 1022),
    /**
     * Messaging error: %1$s
     */
    MESSAGING_ERROR("Messaging error: %1$s", CATEGORY_ERROR, 1023),
    /**
     * The quota on mail server exceeded. Error message: %1$s
     */
    QUOTA_EXCEEDED("The quota on mail server exceeded. Error message: %1$s", CATEGORY_CAPACITY, 1024, MimeMailExceptionMessage.QUOTA_EXCEEDED_MSG_DISPLAY),
    /**
     * The quota on mail server "%1$s" exceeded with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    QUOTA_EXCEEDED_EXT("The quota on mail server \"%1$s\" exceeded with login %2$s (user=%3$s, context=%4$s). Error message: %5$s", QUOTA_EXCEEDED.category, QUOTA_EXCEEDED.detailNumber, MimeMailExceptionMessage.QUOTA_EXCEEDED_EXT_MSG_DISPLAY),
    /**
     * A command sent to mail server failed. Server response: %1$s
     */
    COMMAND_FAILED("A command sent to mail server failed. Server response: %1$s", CATEGORY_ERROR, 1025),
    /**
     * A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    COMMAND_FAILED_EXT("A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s", COMMAND_FAILED.category, COMMAND_FAILED.detailNumber),
    /**
     * Mail server indicates a bad command. Server response: %1$s
     */
    BAD_COMMAND("Mail server indicates a bad command. Server response: %1$s", CATEGORY_ERROR, 1026),
    /**
     * Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    BAD_COMMAND_EXT("Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s", BAD_COMMAND.category, BAD_COMMAND.detailNumber),
    /**
     * An error in mail server protocol. Error message: %1$s
     */
    PROTOCOL_ERROR("Error in mail server protocol. Error message: %1$s", CATEGORY_ERROR, 1027),
    /**
     * Protocol error in data sent to the mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    PROTOCOL_ERROR_EXT("Protocol error in data sent to the mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s", PROTOCOL_ERROR.category, PROTOCOL_ERROR.detailNumber),
    /**
     * Message could not be sent: %1$s
     */
    SEND_FAILED_MSG_ERROR("Message could not be sent: %1$s", CATEGORY_ERROR, 1028, MimeMailExceptionMessage.SEND_FAILED_MSG_ERROR_MSG_DISPLAY),
    /**
     * Message could not be sent: %1$s (%2$s) (<i>arbitrary server information</i>)
     */
    SEND_FAILED_MSG_EXT_ERROR("Message could not be sent: %1$s (%2$s)", CATEGORY_ERROR, 1028, MimeMailExceptionMessage.SEND_FAILED_EXT_MSG_ERROR_MSG_DISPLAY),
    /**
     * Message cannot be displayed.
     */
    MESSAGE_NOT_DISPLAYED("Message cannot be displayed.", CATEGORY_ERROR, 1029),
    /**
     * Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s
     */
    TRANSPORT_INVALID_CREDENTIALS("Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s", CATEGORY_PERMISSION_DENIED, 1030, MimeMailExceptionMessage.TRANSPORT_INVALID_CREDENTIALS_MSG_DISPLAY),
    /**
     * Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail
     * transport server: %5$s
     */
    TRANSPORT_INVALID_CREDENTIALS_EXT("Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail transport server: %5$s", CATEGORY_PERMISSION_DENIED, TRANSPORT_INVALID_CREDENTIALS.detailNumber),
    /**
     * Error processing mail server response. The administrator has been informed.
     */
    PROCESSING_ERROR("Error processing mail server response. The administrator has been informed.", CATEGORY_ERROR, 1031),
    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.
     */
    PROCESSING_ERROR_EXT("Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.", CATEGORY_ERROR, PROCESSING_ERROR.detailNumber),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s.", CATEGORY_ERROR, 8),
    /**
     * I/O error "%1$s" occurred in communication with "%2$s" mail server for login %3$s (user=%4$s, context=%5$s).
     */
    IO_ERROR_EXT("I/O error \"%1$s\" occurred in communication with \"%2$s\" mail server for login %3$s (user=%4$s, context=%5$s).", CATEGORY_ERROR, 8),
    /**
     * Error processing mail server response. The administrator has been informed. Error message: %1$s
     */
    PROCESSING_ERROR_WE("Error processing mail server response. The administrator has been informed. Error message: %1$s", CATEGORY_ERROR, PROCESSING_ERROR.detailNumber),
    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error
     * message: %5$s
     */
    PROCESSING_ERROR_WE_EXT("Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error message: %5$s", CATEGORY_ERROR, PROCESSING_ERROR_WE.detailNumber),
    /**
     * That mailbox is already in use by another process. Please try again later.<br>
     * Error message: %1$s
     */
    IN_USE_ERROR("That mailbox is already in use by another process. Please try again later. Error message: %1$s", CATEGORY_USER_INPUT, PROCESSING_ERROR.detailNumber, MimeMailExceptionMessage.IN_USE_ERROR_MSG_DISPLAY),
    /**
     * That mailbox is already in use by another process on %1$s mail server for login %2$s (user=%3$s, context=%4$s). Please try again later.<br>
     * Error message: %5$s
     */
    IN_USE_ERROR_EXT("That mailbox is already in use by another process on %1$s mail server for login %2$s (user=%3$s, context=%4$s). Please try again later. Error message: %5$s", CATEGORY_USER_INPUT, PROCESSING_ERROR.detailNumber, MimeMailExceptionMessage.IN_USE_ERROR_EXT_MSG_DISPLAY),

    ;

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
