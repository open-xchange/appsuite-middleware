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

import static com.openexchange.exception.OXExceptionFactory.DISPLAYABLE;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.mail.MailExceptionCode;

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
public enum MimeMailExceptionCode implements OXExceptionCode {

    /**
     * There was an issue in authenticating your E-Mail password. This may be because of a recent password change. To continue please logout
     * now and then log back in with your most current password. (server=%1$s | user=%2$s)
     */
    LOGIN_FAILED(MimeMailExceptionMessage.LOGIN_FAILED_MSG, CATEGORY_PERMISSION_DENIED, 1000),
    /**
     * Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s
     */
    INVALID_CREDENTIALS(MimeMailExceptionMessage.INVALID_CREDENTIALS_MSG, CATEGORY_PERMISSION_DENIED, 1001),
    /**
     * Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server:
     * %5$s
     */
    INVALID_CREDENTIALS_EXT(MimeMailExceptionMessage.INVALID_CREDENTIALS_EXT_MSG, CATEGORY_PERMISSION_DENIED, INVALID_CREDENTIALS.detailNumber),
    /**
     * Mail folder "%1$s" could not be found.
     */
    FOLDER_NOT_FOUND(MimeMailExceptionMessage.FOLDER_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 1002),
    /**
     * Mail folder "%1$s" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    FOLDER_NOT_FOUND_EXT(MimeMailExceptionMessage.FOLDER_NOT_FOUND_EXT_MSG, CATEGORY_USER_INPUT, FOLDER_NOT_FOUND.detailNumber),
    /**
     * Folder "%1$s" has been closed due to some reason.<br>
     * Probably your request took too long.
     * <p>
     * This exception is thrown when a method is invoked on a Messaging object and the Folder that owns that object has died due to some
     * reason. Following the exception, the Folder is reset to the "closed" state.
     * </p>
     */
    FOLDER_CLOSED(MimeMailExceptionMessage.FOLDER_CLOSED_MSG, CATEGORY_USER_INPUT, 1003, LogLevel.ERROR),
    /**
     * Folder "%1$s" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) due to some reason.<br>
     * Probably your request took too long.
     */
    FOLDER_CLOSED_EXT(MimeMailExceptionMessage.FOLDER_CLOSED_EXT_MSG, CATEGORY_USER_INPUT, FOLDER_CLOSED.detailNumber, LogLevel.ERROR),
    /**
     * Illegal write attempt: %1$s
     * <p>
     * The exception thrown when a write is attempted on a read-only attribute of any Messaging object.
     * </p>
     */
    ILLEGAL_WRITE(MimeMailExceptionMessage.ILLEGAL_WRITE_MSG, CATEGORY_ERROR, 1004),
    /**
     * Mail(s) could not be found in folder
     * <p>
     * The exception thrown when an invalid method is invoked on an expunged Message. The only valid methods on an expunged Message are
     * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
     * </p>
     */
    MESSAGE_REMOVED(MimeMailExceptionMessage.MESSAGE_REMOVED, MailExceptionCode.MAIL_NOT_FOUND.getCategory(), MailExceptionCode.MAIL_NOT_FOUND.getNumber()),
    /**
     * Method not supported: %1$s
     * <p>
     * The exception thrown when a method is not supported by the implementation
     * </p>
     */
    METHOD_NOT_SUPPORTED(MimeMailExceptionMessage.METHOD_NOT_SUPPORTED_MSG, CATEGORY_ERROR, 1006),
    /**
     * Session attempts to instantiate a provider that doesn't exist: %1$s
     */
    NO_SUCH_PROVIDER(MimeMailExceptionMessage.NO_SUCH_PROVIDER_MSG, CATEGORY_ERROR, 1007),
    /**
     * Invalid email address %1$s
     */
    INVALID_EMAIL_ADDRESS(MimeMailExceptionMessage.INVALID_EMAIL_ADDRESS_MSG, CATEGORY_USER_INPUT, 1008, LogLevel.ERROR),
    /**
     * Wrong message header: %1$s
     * <p>
     * The exception thrown due to an error in parsing RFC822 or MIME headers
     * </p>
     */
    PARSE_ERROR(MimeMailExceptionMessage.PARSE_ERROR_MSG, CATEGORY_USER_INPUT, 1009, LogLevel.ERROR),
    /**
     * An attempt was made to open a read-only folder with read-write "%1$s"
     */
    READ_ONLY_FOLDER(MimeMailExceptionMessage.READ_ONLY_FOLDER_MSG, CATEGORY_PERMISSION_DENIED, 1010),
    /**
     * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    READ_ONLY_FOLDER_EXT(MimeMailExceptionMessage.READ_ONLY_FOLDER_EXT_MSG, CATEGORY_PERMISSION_DENIED, 1010),
    /**
     * Invalid search expression: %1$s
     */
    SEARCH_ERROR(MimeMailExceptionMessage.SEARCH_ERROR_MSG, CATEGORY_USER_INPUT, 1011, LogLevel.ERROR),
    /**
     * Message could not be sent because it is too large
     */
    MESSAGE_TOO_LARGE(MimeMailExceptionMessage.MESSAGE_TOO_LARGE_MSG, CATEGORY_USER_INPUT, 1012, LogLevel.ERROR),
    /**
     * Message could not be sent to following recipients: %1$s
     * <p>
     * The exception includes those addresses to which the message could not be sent as well as the valid addresses to which the message was
     * sent and valid addresses to which the message was not sent.
     * </p>
     */
    SEND_FAILED(MimeMailExceptionMessage.SEND_FAILED_MSG, CATEGORY_USER_INPUT, 1013, LogLevel.ERROR),
    /**
     * Message could not be sent to following recipients: %1$s %2$s (arbitrary server information)
     */
    SEND_FAILED_EXT(MimeMailExceptionMessage.SEND_FAILED_EXT_MSG, CATEGORY_USER_INPUT, 1013, LogLevel.ERROR),
    /**
     * Lost connection to mail server.
     */
    STORE_CLOSED(MimeMailExceptionMessage.STORE_CLOSED_MSG, CATEGORY_SERVICE_DOWN, 1014),
    /**
     * Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s).
     */
    STORE_CLOSED_EXT(MimeMailExceptionMessage.STORE_CLOSED_EXT_MSG, STORE_CLOSED.category, STORE_CLOSED.detailNumber),
    /**
     * Could not bind mail connection to local port %1$s
     * <p>
     * Signals that an error occurred while attempting to bind a socket to a local address and port. Typically, the port is in use, or the
     * requested local address could not be assigned.
     * </p>
     */
    BIND_ERROR(MimeMailExceptionMessage.BIND_ERROR_MSG, CATEGORY_CONFIGURATION, 1015),
    /**
     * Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
     * <p>
     * An error occurred while attempting to connect to remote mail server. Typically, the connection was refused remotely (e.g., no process
     * is listening on the remote address/port).
     * </p>
     */
    CONNECT_ERROR(MimeMailExceptionMessage.CONNECT_ERROR_MSG, CATEGORY_SERVICE_DOWN, 1016),
    /**
     * Connection was reset
     */
    CONNECTION_RESET(MimeMailExceptionMessage.CONNECTION_RESET_MSG, CATEGORY_TRY_AGAIN, 1017),
    /**
     * No route to host: mail server %1$s cannot be reached
     * <p>
     * Signals that an error occurred while attempting to connect to remote mail server. Typically, the remote mail server cannot be reached
     * because of an intervening firewall, or if an intermediate router is down.
     * </p>
     */
    NO_ROUTE_TO_HOST(MimeMailExceptionMessage.NO_ROUTE_TO_HOST_MSG, CATEGORY_SERVICE_DOWN, 1018),
    /**
     * Port %1$s was unreachable on remote mail server
     */
    PORT_UNREACHABLE(MimeMailExceptionMessage.PORT_UNREACHABLE_MSG, CATEGORY_SERVICE_DOWN, 1019),
    /**
     * Connection is broken due to a socket exception on remote mail server: %1$s
     */
    BROKEN_CONNECTION(MimeMailExceptionMessage.BROKEN_CONNECTION_MSG, CATEGORY_SERVICE_DOWN, 1020),
    /**
     * A socket error occurred: %1$s
     */
    SOCKET_ERROR(MimeMailExceptionMessage.SOCKET_ERROR_MSG, CATEGORY_ERROR, 1021),
    /**
     * The IP address of host "%1$s" could not be determined
     */
    UNKNOWN_HOST(MimeMailExceptionMessage.UNKNOWN_HOST_MSG, CATEGORY_SERVICE_DOWN, 1022),
    /**
     * Messaging error: %1$s
     */
    MESSAGING_ERROR(MimeMailExceptionMessage.MESSAGING_ERROR_MSG, CATEGORY_ERROR, 1023),
    /**
     * The quota on mail server exceeded.
     */
    QUOTA_EXCEEDED(MimeMailExceptionMessage.QUOTA_EXCEEDED_MSG, CATEGORY_CAPACITY, 1024),
    /**
     * The quota on mail server "%1$s" exceeded with login %2$s (user=%3$s, context=%4$s).
     */
    QUOTA_EXCEEDED_EXT(MimeMailExceptionMessage.QUOTA_EXCEEDED_EXT_MSG, QUOTA_EXCEEDED.category, QUOTA_EXCEEDED.detailNumber),
    /**
     * A command to mail server failed. Server response: %1$s
     */
    COMMAND_FAILED(MimeMailExceptionMessage.COMMAND_FAILED_MSG, CATEGORY_ERROR, 1025),
    /**
     * A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    COMMAND_FAILED_EXT(MimeMailExceptionMessage.COMMAND_FAILED_EXT_MSG, COMMAND_FAILED.category, COMMAND_FAILED.detailNumber),
    /**
     * Mail server indicates a bad command. Server response: %1$s
     */
    BAD_COMMAND(MimeMailExceptionMessage.BAD_COMMAND_MSG, CATEGORY_ERROR, 1026),
    /**
     * Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
     */
    BAD_COMMAND_EXT(MimeMailExceptionMessage.BAD_COMMAND_EXT_MSG, BAD_COMMAND.category, BAD_COMMAND.detailNumber),
    /**
     * An error in mail server protocol. Error message: %1$s
     */
    PROTOCOL_ERROR(MimeMailExceptionMessage.PROTOCOL_ERROR_MSG, CATEGORY_ERROR, 1027),
    /**
     * An error in protocol to mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
     */
    PROTOCOL_ERROR_EXT(MimeMailExceptionMessage.PROTOCOL_ERROR_EXT_MSG, PROTOCOL_ERROR.category, PROTOCOL_ERROR.detailNumber),
    /**
     * Message could not be sent: %1$s
     */
    SEND_FAILED_MSG(MimeMailExceptionMessage.SEND_FAILED_MSG, CATEGORY_USER_INPUT, 1028, LogLevel.ERROR),
    /**
     * Message could not be sent: %1$s %2$s (arbitrary server information)
     */
    SEND_FAILED_MSG_EXT(MimeMailExceptionMessage.SEND_FAILED_EXT_MSG, CATEGORY_USER_INPUT, 1028, LogLevel.ERROR),
    /**
     * Message cannot be displayed.
     */
    MESSAGE_NOT_DISPLAYED(MimeMailExceptionMessage.MESSAGE_NOT_DISPLAYED_MSG, CATEGORY_USER_INPUT, 1029, LogLevel.ERROR),
    /**
     * Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s
     */
    TRANSPORT_INVALID_CREDENTIALS(MimeMailExceptionMessage.TRANSPORT_INVALID_CREDENTIALS_MSG, CATEGORY_PERMISSION_DENIED, 1030),
    /**
     * Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail
     * transport server: %5$s
     */
    TRANSPORT_INVALID_CREDENTIALS_EXT(MimeMailExceptionMessage.TRANSPORT_INVALID_CREDENTIALS_EXT_MSG, CATEGORY_PERMISSION_DENIED, TRANSPORT_INVALID_CREDENTIALS.detailNumber),
    /**
     * Error processing mail server response. The administrator has been informed.
     */
    PROCESSING_ERROR(MimeMailExceptionMessage.PROCESSING_ERROR_MSG, CATEGORY_USER_INPUT, 1031, LogLevel.ERROR),
    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.
     */
    PROCESSING_ERROR_EXT(MimeMailExceptionMessage.PROCESSING_ERROR_EXT_MSG, CATEGORY_USER_INPUT, PROCESSING_ERROR.detailNumber, LogLevel.ERROR),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(MailExceptionCode.IO_ERROR, LogLevel.ERROR),
    /**
     * I/O error "%1$s" occurred in communication with "%2$s" mail server for login %3$s (user=%4$s, context=%5$s).
     */
    IO_ERROR_EXT(MailExceptionCode.IO_ERROR, MimeMailExceptionMessage.IO_ERROR_EXT_MSG, LogLevel.ERROR),
    /**
     * Error processing mail server response. The administrator has been informed. Error message: %1$s
     */
    PROCESSING_ERROR_WE(MimeMailExceptionMessage.PROCESSING_ERROR_WE_MSG, CATEGORY_ERROR, PROCESSING_ERROR.detailNumber),
    /**
     * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error
     * message: %5$s
     */
    PROCESSING_ERROR_WE_EXT(MimeMailExceptionMessage.PROCESSING_ERROR_WE_EXT_MSG, CATEGORY_ERROR, PROCESSING_ERROR_WE.detailNumber), ;

    private final String message;
    private final int detailNumber;
    private final Category category;
    private final LogLevel logLevel;

    private MimeMailExceptionCode(final String message, final Category category, final int detailNumber, final LogLevel logLevel) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.logLevel = logLevel;
    }

    private MimeMailExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.logLevel = null;
    }

    private MimeMailExceptionCode(final MailExceptionCode code, final String message, final LogLevel logLevel) {
        this.message = message;
        this.detailNumber = code.getNumber();
        this.category = code.getCategory();
        this.logLevel = logLevel;
    }

    private MimeMailExceptionCode(final MailExceptionCode code, final LogLevel logLevel) {
        this.message = code.getMessage();
        this.detailNumber = code.getNumber();
        this.category = code.getCategory();
        this.logLevel = logLevel;
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
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return create(null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        final Category category = getCategory();
        final MimeMailException ret;
        if (category.getLogLevel().implies(LogLevel.DEBUG)) {
            ret = new MimeMailException(getNumber(), getMessage(), cause, args);
        } else {
            if (DISPLAYABLE.contains(category.getType())) {
                // Displayed message is equal to logged one
                ret = new MimeMailException(getNumber(), getMessage(), cause, args);
                ret.setLogMessage(getMessage(), args);
            } else {
                ret =
                    new MimeMailException(
                        getNumber(),
                        Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE,
                        cause,
                        new Object[0]);
                ret.setLogMessage(getMessage(), args);
            }
        }
        ret.addCategory(category);
        ret.setPrefix(getPrefix());
        if (null != logLevel) {
            ret.setLogLevel(logLevel);
        }
        return ret;
    }
}
