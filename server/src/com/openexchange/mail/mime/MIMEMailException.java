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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.session.Session;
import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * {@link MIMEMailException} - For MIME related errors.
 * <p>
 * Taken from {@link MailException}:
 * <p>
 * The detail number range in subclasses generated in mail bundles is supposed to start with 2000 and may go up to 2999.
 * <p>
 * The detail number range in subclasses generated in transport bundles is supposed to start with 3000 and may go up to 3999.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MIMEMailException extends MailException {

    private static final transient org.apache.commons.logging.Log LOG = com.openexchange.exception.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(MIMEMailException.class));

    private static final long serialVersionUID = -3401580182929349354L;

    public static enum Code {

        /**
         * There was an issue in authenticating your E-Mail password. This may be because of a recent password change. To continue please
         * logout now and then log back in with your most current password. (server=%1$s | user=%2$s)
         */
        LOGIN_FAILED("There was an issue in authenticating your E-Mail password. This may be because of a recent password change. " + "To continue please logout now and then log back in with your most current password. (server=%1$s | user=%2$s)", Category.PERMISSION, 1000),
        /**
         * Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s
         */
        INVALID_CREDENTIALS("Wrong or missing login data to access mail server %1$s. Error message from mail server: %2$s", Category.PERMISSION, 1001),
        /**
         * Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server: %5$s
         */
        INVALID_CREDENTIALS_EXT("Wrong or missing login data to access mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail server: %5$s", Category.PERMISSION, INVALID_CREDENTIALS.detailNumber),
        /**
         * Mail folder "%1$s" could not be found.
         */
        FOLDER_NOT_FOUND("Mail folder \"%1$s\" could not be found.", Category.CODE_ERROR, 1002),
        /**
         * Mail folder "%1$s" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).
         */
        FOLDER_NOT_FOUND_EXT("Mail folder \"%1$s\" could not be found on mail server %2$s with login %3$s (user=%4$s, context=%5$s).", Category.CODE_ERROR, FOLDER_NOT_FOUND.detailNumber),
        /**
         * Folder "%1$s" has been closed due to some reason.<br>
         * Probably your request took too long.
         * <p>
         * This exception is thrown when a method is invoked on a Messaging object and the Folder that owns that object has died due to some
         * reason. Following the exception, the Folder is reset to the "closed" state.
         * </p>
         */
        FOLDER_CLOSED("Folder \"%1$s\" is closed due to some reason.\nProbably your request took too long", Category.CODE_ERROR, 1003),
        /**
         * Folder "%1$s" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) due to some reason.<br>
         * Probably your request took too long.
         */
        FOLDER_CLOSED_EXT("Folder \"%1$s\" has been closed on mail server %2$s with login %3$s (user=%4$s, context=%5$s) due to some reason.\nProbably your request took too long.", Category.CODE_ERROR, FOLDER_CLOSED.detailNumber),
        /**
         * Illegal write attempt: %1$s
         * <p>
         * The exception thrown when a write is attempted on a read-only attribute of any Messaging object.
         * </p>
         */
        ILLEGAL_WRITE("Illegal write attempt: %1$s", Category.CODE_ERROR, 1004),
        /**
         * Mail(s) could not be found in folder
         * <p>
         * The exception thrown when an invalid method is invoked on an expunged Message. The only valid methods on an expunged Message are
         * <code>isExpunged()</code> and <code>getMessageNumber()</code>.
         * </p>
         */
        MESSAGE_REMOVED(String.format(MailException.Code.MAIL_NOT_FOUND.getMessage(), "", ""), MailException.Code.MAIL_NOT_FOUND.getCategory(), MailException.Code.MAIL_NOT_FOUND.getNumber()),
        /**
         * Method not supported: %1$s
         * <p>
         * The exception thrown when a method is not supported by the implementation
         * </p>
         */
        METHOD_NOT_SUPPORTED("Method not supported: %1$s", Category.CODE_ERROR, 1006),
        /**
         * Session attempts to instantiate a provider that doesn't exist: %1$s
         */
        NO_SUCH_PROVIDER("Session attempts to instantiate a provider that doesn't exist: %1$s", Category.CODE_ERROR, 1007),
        /**
         * Invalid email address %1$s
         */
        INVALID_EMAIL_ADDRESS("Invalid email address %1$s", Category.USER_INPUT, 1008),
        /**
         * Wrong message header: %1$s
         * <p>
         * The exception thrown due to an error in parsing RFC822 or MIME headers
         * </p>
         */
        PARSE_ERROR("Wrong message header: %1$s", Category.USER_INPUT, 1009),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s"
         */
        READ_ONLY_FOLDER("An attempt was made to open a read-only folder with read-write \"%1$s\"", Category.PERMISSION, 1010),
        /**
         * An attempt was made to open a read-only folder with read-write "%1$s" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)
         */
        READ_ONLY_FOLDER_EXT("An attempt was made to open a read-only folder with read-write \"%1$s\" on mail server %2$s with login %3$s (user=%4$s, context=%5$s)", Category.PERMISSION, 1010),
        /**
         * Invalid search expression: %1$s
         */
        SEARCH_ERROR("Invalid search expression: %1$s", Category.CODE_ERROR, 1011),
        /**
         * Message could not be sent because it is too large
         */
        MESSAGE_TOO_LARGE("Message could not be sent because it is too large", Category.INTERNAL_ERROR, 1012),
        /**
         * Message could not be sent to following recipients: %1$s
         * <p>
         * The exception includes those addresses to which the message could not be sent as well as the valid addresses to which the message
         * was sent and valid addresses to which the message was not sent.
         * </p>
         */
        SEND_FAILED("Message could not be sent to the following recipients: %1$s", Category.USER_INPUT, 1013),
        /**
         * Store already closed: %1$s
         */
        STORE_CLOSED("Store already closed: %1$s", Category.CODE_ERROR, 1014),
        /**
         * Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s): %5$s
         */
        STORE_CLOSED_EXT("Connection closed to mail server %1$s with login %2$s (user=%3$s, context=%4$s): %5$s", STORE_CLOSED.category, STORE_CLOSED.detailNumber),
        /**
         * Could not bind mail connection to local port %1$s
         * <p>
         * Signals that an error occurred while attempting to bind a socket to a local address and port. Typically, the port is in use, or
         * the requested local address could not be assigned.
         * </p>
         */
        BIND_ERROR("Could not bind connection to local port %1$s", Category.SETUP_ERROR, 1015),
        /**
         * Connection was refused or timed out while attempting to connect to remote mail server %1$s for user %2$s.
         * <p>
         * An error occurred while attempting to connect to remote mail server. Typically, the connection was refused remotely (e.g., no
         * process is listening on the remote address/port).
         * </p>
         */
        CONNECT_ERROR("Connection was refused or timed out while attempting to connect to remote server %1$s for user %2$s.", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1016),
        /**
         * Connection was reset
         */
        CONNECTION_RESET("Connection was reset. Please try again.", Category.TRY_AGAIN, 1017),
        /**
         * No route to host: mail server %1$s cannot be reached
         * <p>
         * Signals that an error occurred while attempting to connect to remote mail server. Typically, the remote mail server cannot be
         * reached because of an intervening firewall, or if an intermediate router is down.
         * </p>
         */
        NO_ROUTE_TO_HOST("No route to host: server (%1$s) cannot be reached", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1018),
        /**
         * Port %1$s was unreachable on remote mail server
         */
        PORT_UNREACHABLE("Port %1$s was unreachable on remote server", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1019),
        /**
         * Connection is broken due to a socket exception on remote mail server: %1$s
         */
        BROKEN_CONNECTION("Connection is broken due to a socket exception on remote server: %1$s", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1020),
        /**
         * A socket error occurred: %1$s
         */
        SOCKET_ERROR("A socket error occurred: %1$s", Category.CODE_ERROR, 1021),
        /**
         * The IP address of host "%1$s" could not be determined
         */
        UNKNOWN_HOST("The IP address of host \"%1$s\" could not be determined", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1022),
        /**
         * Messaging error: %1$s
         */
        MESSAGING_ERROR("Messaging error: %1$s", Category.CODE_ERROR, 1023),
        /**
         * The quota on mail server exceeded.
         */
        QUOTA_EXCEEDED("The quota on mail server exceeded.", Category.EXTERNAL_RESOURCE_FULL, 1024),
        /**
         * The quota on mail server "%1$s" exceeded with login %2$s (user=%3$s, context=%4$s).
         */
        QUOTA_EXCEEDED_EXT("The quota on mail server \"%1$s\" exceeded with login %2$s (user=%3$s, context=%4$s).", QUOTA_EXCEEDED.category, QUOTA_EXCEEDED.detailNumber),
        /**
         * A command to mail server failed. Server response: %1$s
         */
        COMMAND_FAILED("A command to mail server failed. Server response: %1$s.", Category.CODE_ERROR, 1025),
        /**
         * A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
         */
        COMMAND_FAILED_EXT("A command failed on mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s", COMMAND_FAILED.category, COMMAND_FAILED.detailNumber),
        /**
         * Mail server indicates a bad command. Server response: %1$s
         */
        BAD_COMMAND("Mail server indicates a bad command. Server response: %1$s", Category.CODE_ERROR, 1026),
        /**
         * Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s
         */
        BAD_COMMAND_EXT("Bad command indicated by mail server %1$s with login %2$s (user=%3$s, context=%4$s). Server response: %5$s", BAD_COMMAND.category, BAD_COMMAND.detailNumber),
        /**
         * An error in mail server protocol. Error message: %1$s
         */
        PROTOCOL_ERROR("An error in mail server protocol. Error message: %1$s", Category.CODE_ERROR, 1027),
        /**
         * An error in protocol to mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s
         */
        PROTOCOL_ERROR_EXT("An error in protocol to mail server %1$s with login %2$s (user=%3$s, context=%4$s). Error message: %5$s", PROTOCOL_ERROR.category, PROTOCOL_ERROR.detailNumber),
        /**
         * Message could not be sent: %1$s
         */
        SEND_FAILED_MSG("Message could not be sent: %1$s", Category.CODE_ERROR, 1028),
        /**
         * Message cannot be displayed.
         */
        MESSAGE_NOT_DISPLAYED("Message cannot be displayed.", Category.SUBSYSTEM_OR_SERVICE_DOWN, 1029),
        /**
         * Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s
         */
        TRANSPORT_INVALID_CREDENTIALS("Wrong or missing login data to access mail transport server %1$s. Error message from mail transport server: %2$s", Category.PERMISSION, 1030),
        /**
         * Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail transport server: %5$s
         */
        TRANSPORT_INVALID_CREDENTIALS_EXT("Wrong or missing login data to access mail transport server %1$s with login %2$s (user=%3$s, context=%4$s). Error message from mail transport server: %5$s", Category.PERMISSION, TRANSPORT_INVALID_CREDENTIALS.detailNumber),
        /**
         * Error processing mail server response. The administrator has been informed.
         */
        PROCESSING_ERROR("Error processing mail server response. The administrator has been informed.", Category.CODE_ERROR, 1031),
        /**
         * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.
         */
        PROCESSING_ERROR_EXT("Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed.", Category.CODE_ERROR, PROCESSING_ERROR.detailNumber),
        /**
         * An I/O error occurred: %1$s
         */
        IO_ERROR(MailException.Code.IO_ERROR),
        /**
         * I/O error "%1$s" occurred in communication with "%2$s" mail server for login %3$s (user=%4$s, context=%5$s).
         */
        IO_ERROR_EXT(MailException.Code.IO_ERROR, "I/O error \"%1$s\" occurred in communication with \"%2$s\" mail server for login %3$s (user=%4$s, context=%5$s)."),
        /**
         * Error processing mail server response. The administrator has been informed. Error message: %1$s
         */
        PROCESSING_ERROR_WE("Error processing mail server response. The administrator has been informed. Error message: %1$s", Category.CODE_ERROR, PROCESSING_ERROR.detailNumber),
        /**
         * Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error message: %5$s
         */
        PROCESSING_ERROR_WE_EXT("Error processing %1$s mail server response for login %2$s (user=%3$s, context=%4$s). The administrator has been informed. Error message: %5$s", Category.CODE_ERROR, PROCESSING_ERROR_WE.detailNumber),
        ;
        
        private final String message;

        private final int detailNumber;

        private final Category category;

        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.detailNumber = detailNumber;
            this.category = category;
        }

        private Code(final MailException.Code code, final String message) {
            this.message = message;
            this.detailNumber = code.getNumber();
            this.category = code.getCategory();
        }

        private Code(final MailException.Code code) {
            this.message = code.getMessage();
            this.detailNumber = code.getNumber();
            this.category = code.getCategory();
        }

        public Category getCategory() {
            return category;
        }

        public int getNumber() {
            return detailNumber;
        }

        public String getMessage() {
            return message;
        }
    }

    public MIMEMailException(final AbstractOXException cause) {
        super(cause);
    }

    public MIMEMailException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    public MIMEMailException(final Code code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.MAIL, code.getCategory(), code.getNumber(), code.getMessage(), cause);
        super.setMessageArgs(messageArgs);
    }

    private static final transient Object[] EMPTY_ARGS = new Object[0];

    public MIMEMailException(final Code code) {
        this(code, EMPTY_ARGS);
    }

    public MIMEMailException(final Component component, final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(component, category, detailNumber, message, cause);
    }

    /**
     * Handles given instance of {@link MessagingException} and creates an appropriate instance of {@link MIMEMailException}
     * <p>
     * This is just a convenience method that simply invokes {@link #handleMessagingException(MessagingException, MailConfig)} with the
     * latter parameter set to <code>null</code>.
     * 
     * @param e The messaging exception
     * @return An appropriate instance of {@link MIMEMailException}
     */
    public static MIMEMailException handleMessagingException(final MessagingException e) {
        return handleMessagingException(e, null, null);
    }

    /**
     * Handles given instance of {@link MessagingException} and creates an appropriate instance of {@link MIMEMailException}
     * 
     * @param e The messaging exception
     * @param mailConfig The corresponding mail configuration used to add information like mail server etc.
     * @return An appropriate instance of {@link MIMEMailException}
     */
    public static MIMEMailException handleMessagingException(final MessagingException e, final MailConfig mailConfig) {
        return handleMessagingException(e, mailConfig, mailConfig.getSession());
    }

    private static final String STR_EMPTY = "";

    private static final String ERR_TMP = "temporary error, please try again later";

    private static final String ERR_AUTH_FAILED = "bad authentication failed";

    private static final String ERR_MSG_TOO_LARGE = "message too large";

    private static final String ERR_QUOTA = "quota";

    /**
     * ConnectionResetException
     */
    private static final String EXC_CONNECTION_RESET_EXCEPTION = "ConnectionResetException";

    /**
     * Handles given instance of {@link MessagingException} and creates an appropriate instance of {@link MIMEMailException}
     * 
     * @param e The messaging exception
     * @param mailConfig The corresponding mail configuration used to add information like mail server etc.
     * @param session The session providing user information
     * @return An appropriate instance of {@link MIMEMailException}
     */
    public static MIMEMailException handleMessagingException(final MessagingException e, final MailConfig mailConfig, final Session session) {
        try {
            if ((e instanceof javax.mail.AuthenticationFailedException) || ((e.getMessage() != null) && (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(
                ERR_AUTH_FAILED) != -1))) {
                final boolean temporary = (e.getMessage() != null) && ERR_TMP.equals(e.getMessage().toLowerCase(Locale.ENGLISH));
                if (temporary) {
                    return new MIMEMailException(
                        MIMEMailException.Code.LOGIN_FAILED,
                        e,
                        mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                        mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
                }
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        MIMEMailException.Code.INVALID_CREDENTIALS_EXT,
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        e.getMessage());
                }
                return new MIMEMailException(
                    MIMEMailException.Code.INVALID_CREDENTIALS,
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    e.getMessage());
            } else if (e instanceof javax.mail.FolderClosedException) {
                final Folder f = ((javax.mail.FolderClosedException) e).getFolder();
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.FOLDER_CLOSED_EXT,
                        e,
                        null == f ? e.getMessage() : f.getFullName(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return new MIMEMailException(Code.FOLDER_CLOSED, e, null == f ? e.getMessage() : f.getFullName());
            } else if (e instanceof javax.mail.FolderNotFoundException) {
                final Folder f = ((javax.mail.FolderNotFoundException) e).getFolder();
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.FOLDER_NOT_FOUND_EXT,
                        e,
                        null == f ? e.getMessage() : f.getFullName(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return new MIMEMailException(Code.FOLDER_NOT_FOUND, e, null == f ? e.getMessage() : f.getFullName());
            } else if (e instanceof javax.mail.IllegalWriteException) {
                return new MIMEMailException(Code.ILLEGAL_WRITE, e, e.getMessage());
            } else if (e instanceof javax.mail.MessageRemovedException) {
                return new MIMEMailException(Code.MESSAGE_REMOVED, e, e.getMessage());
            } else if (e instanceof javax.mail.MethodNotSupportedException) {
                return new MIMEMailException(Code.METHOD_NOT_SUPPORTED, e, e.getMessage());
            } else if (e instanceof javax.mail.NoSuchProviderException) {
                return new MIMEMailException(Code.NO_SUCH_PROVIDER, e, e.getMessage());
            } else if (e instanceof javax.mail.internet.ParseException) {
                if (e instanceof javax.mail.internet.AddressException) {
                    final String ref = ((AddressException) e).getRef() == null ? STR_EMPTY : ((AddressException) e).getRef();
                    return new MIMEMailException(Code.INVALID_EMAIL_ADDRESS, e, ref);
                }
                return new MIMEMailException(Code.PARSE_ERROR, e, e.getMessage());
            } else if (e instanceof javax.mail.ReadOnlyFolderException) {
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.READ_ONLY_FOLDER_EXT,
                        e,
                        e.getMessage(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return new MIMEMailException(Code.READ_ONLY_FOLDER, e, e.getMessage());
            } else if (e instanceof javax.mail.search.SearchException) {
                return new MIMEMailException(Code.SEARCH_ERROR, e, e.getMessage());
            } else if (e instanceof com.sun.mail.smtp.SMTPSendFailedException) {
                final SMTPSendFailedException exc = (SMTPSendFailedException) e;
                if ((exc.getReturnCode() == 552) || (exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1)) {
                    return new MIMEMailException(Code.MESSAGE_TOO_LARGE, exc, new Object[0]);
                }
                final Address[] addrs = exc.getInvalidAddresses();
                if (null == addrs || addrs.length == 0) {
                    // No invalid addresses available
                    return new MIMEMailException(Code.SEND_FAILED_MSG, exc, exc.getMessage());
                }
                return new MIMEMailException(Code.SEND_FAILED, exc, Arrays.toString(exc.getInvalidAddresses()));
            } else if (e instanceof javax.mail.SendFailedException) {
                final SendFailedException exc = (SendFailedException) e;
                if (exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1) {
                    return new MIMEMailException(Code.MESSAGE_TOO_LARGE, exc, new Object[0]);
                }
                final Exception nextException = exc.getNextException();
                if (nextException instanceof com.sun.mail.smtp.SMTPSendFailedException) {
                    final SMTPSendFailedException smtpExc = (SMTPSendFailedException) nextException;
                    final Address[] invalidAddresses = smtpExc.getInvalidAddresses();
                    if (null == invalidAddresses || invalidAddresses.length == 0) {
                        return new MIMEMailException(Code.SEND_FAILED_MSG, exc, exc.getMessage());
                    }
                }
                final Address[] addrs = exc.getInvalidAddresses();
                if (null == addrs || addrs.length == 0) {
                    // No invalid addresses available
                    return new MIMEMailException(Code.SEND_FAILED_MSG, exc, exc.getMessage());
                }
                return new MIMEMailException(Code.SEND_FAILED, exc, Arrays.toString(addrs));
            } else if (e instanceof javax.mail.StoreClosedException) {
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.STORE_CLOSED_EXT,
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        e.getMessage());
                }
                return new MIMEMailException(Code.STORE_CLOSED, e, e.getMessage());
            }
            final Exception nextException = e.getNextException();
            if (nextException == null) {
                if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_QUOTA) != -1) {
                    return new MIMEMailException(Code.QUOTA_EXCEEDED, e, EMPTY_ARGS);
                } else if ("Unable to load BODYSTRUCTURE".equals(e.getMessage())) {
                    return new MIMEMailException(Code.MESSAGE_NOT_DISPLAYED, e, EMPTY_ARGS);
                }
                /*
                 * Default case
                 */
                return new MIMEMailException(Code.MESSAGING_ERROR, e, e.getMessage());
            }
            /*
             * Messaging exception has a nested exception
             */
            if (nextException instanceof java.net.BindException) {
                return new MIMEMailException(Code.BIND_ERROR, e, mailConfig == null ? STR_EMPTY : Integer.valueOf(mailConfig.getPort()));
            } else if (nextException instanceof com.sun.mail.iap.ConnectionException) {
                mailInterfaceMonitor.changeNumBrokenConnections(true);
                return new MIMEMailException(
                    Code.CONNECT_ERROR,
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
            } else if (nextException instanceof java.net.ConnectException) {
                /*
                 * Most modern IP stack implementations sense connection idleness, and abort the connection attempt, resulting in a
                 * java.net.ConnectionException
                 */
                mailInterfaceMonitor.changeNumTimeoutConnections(true);
                final MIMEMailException me = new MIMEMailException(
                    Code.CONNECT_ERROR,
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
                return me;
            } else if (nextException.getClass().getName().endsWith(EXC_CONNECTION_RESET_EXCEPTION)) {
                mailInterfaceMonitor.changeNumBrokenConnections(true);
                return new MIMEMailException(Code.CONNECTION_RESET, e, new Object[0]);
            } else if (nextException instanceof java.net.NoRouteToHostException) {
                return new MIMEMailException(Code.NO_ROUTE_TO_HOST, e, mailConfig == null ? STR_EMPTY : mailConfig.getServer());
            } else if (nextException instanceof java.net.PortUnreachableException) {
                return new MIMEMailException(
                    Code.PORT_UNREACHABLE,
                    e,
                    mailConfig == null ? STR_EMPTY : Integer.valueOf(mailConfig.getPort()));
            } else if (nextException instanceof java.net.SocketException) {
                /*
                 * Treat dependent on message
                 */
                final SocketException se = (SocketException) nextException;
                if ("Socket closed".equals(se.getMessage()) || "Connection reset".equals(se.getMessage())) {
                    mailInterfaceMonitor.changeNumBrokenConnections(true);
                    return new MIMEMailException(Code.BROKEN_CONNECTION, e, mailConfig == null ? STR_EMPTY : mailConfig.getServer());
                }
                return new MIMEMailException(Code.SOCKET_ERROR, e, e.getMessage());
            } else if (nextException instanceof java.net.UnknownHostException) {
                return new MIMEMailException(Code.UNKNOWN_HOST, e, e.getMessage());
            } else if (nextException instanceof java.net.SocketTimeoutException) {
                mailInterfaceMonitor.changeNumBrokenConnections(true);
                return new MIMEMailException(
                    Code.CONNECT_ERROR,
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
            } else if (nextException instanceof com.openexchange.mail.mime.QuotaExceededException) {
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.QUOTA_EXCEEDED_EXT,
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        nextException.getMessage());
                }
                return new MIMEMailException(Code.QUOTA_EXCEEDED, e, nextException.getMessage());
            } else if (nextException instanceof com.sun.mail.iap.CommandFailedException) {
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.PROCESSING_ERROR_WE_EXT,
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        skipTag(nextException.getMessage()));
                }
                return new MIMEMailException(Code.PROCESSING_ERROR_WE, e, skipTag(nextException.getMessage()));
            } else if (nextException instanceof com.sun.mail.iap.BadCommandException) {
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.PROCESSING_ERROR_EXT,
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        nextException.getMessage());
                }
                return new MIMEMailException(Code.PROCESSING_ERROR, e, nextException.getMessage());
            } else if (nextException instanceof com.sun.mail.iap.ProtocolException) {
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.PROCESSING_ERROR_EXT,
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        nextException.getMessage());
                }
                return new MIMEMailException(Code.PROCESSING_ERROR, e, nextException.getMessage());
            } else if (nextException instanceof java.io.IOException) {
                if (null != mailConfig && null != session) {
                    return new MIMEMailException(
                        Code.IO_ERROR_EXT,
                        e,
                        nextException.getMessage(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return new MIMEMailException(Code.IO_ERROR, nextException, nextException.getMessage());
            } else if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_QUOTA) != -1) {
                return new MIMEMailException(Code.QUOTA_EXCEEDED, e, EMPTY_ARGS);
            }
            /*
             * Default case
             */
            return new MIMEMailException(Code.MESSAGING_ERROR, nextException, nextException.getMessage());
        } catch (final Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(t.getMessage(), t);
            }
            /*
             * This routine should not fail since it's purpose is wrap a corresponding mail error around specified messaging error
             */
            return new MIMEMailException(Code.MESSAGING_ERROR, e, e.getMessage());
        }
    }

    private static final Pattern PATTERN_TAG = Pattern.compile("A[0-9]+ (.+)");

    private static String skipTag(final String serverResponse) {
        if (null == serverResponse) {
            return null;
        }
        final Matcher m = PATTERN_TAG.matcher(serverResponse);
        if (m.matches()) {
            return m.group(1);
        }
        return serverResponse;
    }

}
