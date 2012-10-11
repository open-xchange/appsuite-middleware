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
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * {@link OXException} - For MIME related errors.
 * <p>
 * Taken from {@link OXException}:
 * <p>
 * The detail number range in subclasses generated in mail bundles is supposed to start with 2000 and may go up to 2999.
 * <p>
 * The detail number range in subclasses generated in transport bundles is supposed to start with 3000 and may go up to 3999.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MimeMailException extends OXException {

    private static final transient org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MimeMailException.class));

    private static final long serialVersionUID = -3401580182929349354L;

    /**
     * Initializes a new {@link MimeMailException}.
     * 
     * @param code
     * @param displayMessage
     * @param displayArgs
     */
    public MimeMailException(final int code, final String displayMessage, final Object... displayArgs) {
        super(code, displayMessage, displayArgs);
    }

    /**
     * Initializes a new {@link MimeMailException}.
     * 
     * @param code
     * @param displayMessage
     * @param cause
     * @param displayArgs
     */
    public MimeMailException(final int code, final String displayMessage, final Throwable cause, final Object... displayArgs) {
        super(code, displayMessage, cause, displayArgs);
    }

    /**
     * Handles given instance of {@link MessagingException} and creates an appropriate instance of {@link OXException}
     * <p>
     * This is just a convenience method that simply invokes {@link #handleMessagingException(MessagingException, MailConfig)} with the
     * latter parameter set to <code>null</code>.
     * 
     * @param e The messaging exception
     * @return An appropriate instance of {@link OXException}
     */
    public static OXException handleMessagingException(final MessagingException e) {
        return handleMessagingException(e, null, null);
    }

    /**
     * Handles given instance of {@link MessagingException} and creates an appropriate instance of {@link OXException}
     * 
     * @param e The messaging exception
     * @param mailConfig The corresponding mail configuration used to add information like mail server etc.
     * @return An appropriate instance of {@link OXException}
     */
    public static OXException handleMessagingException(final MessagingException e, final MailConfig mailConfig) {
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

    private static final Object[] EMPTY_ARGS = new Object[0];

    /**
     * Handles given instance of {@link MessagingException} and creates an appropriate instance of {@link OXException}
     * 
     * @param e The messaging exception
     * @param mailConfig The corresponding mail configuration used to add information like mail server etc.
     * @param session The session providing user information
     * @return An appropriate instance of {@link OXException}
     */
    public static OXException handleMessagingException(final MessagingException e, final MailConfig mailConfig, final Session session) {
        try {
            if ((e instanceof javax.mail.AuthenticationFailedException) || ((e.getMessage() != null) && (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_AUTH_FAILED) != -1))) {
                if (null != mailConfig && MailAccount.DEFAULT_ID == mailConfig.getAccountId()) {
                    return MimeMailExceptionCode.LOGIN_FAILED.create(e, mailConfig.getServer(), mailConfig.getLogin());
                }
                if ((e.getMessage() != null) && ERR_TMP.equals(e.getMessage().toLowerCase(Locale.ENGLISH))) {
                    return MimeMailExceptionCode.LOGIN_FAILED.create(
                        e,
                        mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                        mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
                }
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.INVALID_CREDENTIALS_EXT.create(
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        e.getMessage());
                }
                return MimeMailExceptionCode.INVALID_CREDENTIALS.create(
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    e.getMessage());
            } else if (e instanceof javax.mail.FolderClosedException) {
                final Folder f = ((javax.mail.FolderClosedException) e).getFolder();
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.FOLDER_CLOSED_EXT.create(
                        e,
                        null == f ? e.getMessage() : f.getFullName(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return MimeMailExceptionCode.FOLDER_CLOSED.create(e, null == f ? e.getMessage() : f.getFullName());
            } else if (e instanceof javax.mail.FolderNotFoundException) {
                final Folder f = ((javax.mail.FolderNotFoundException) e).getFolder();
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.FOLDER_NOT_FOUND_EXT.create(
                        e,
                        null == f ? e.getMessage() : f.getFullName(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return MimeMailExceptionCode.FOLDER_NOT_FOUND.create(e, null == f ? e.getMessage() : f.getFullName());
            } else if (e instanceof javax.mail.IllegalWriteException) {
                return MimeMailExceptionCode.ILLEGAL_WRITE.create(e, e.getMessage());
            } else if (e instanceof javax.mail.MessageRemovedException) {
                return MimeMailExceptionCode.MESSAGE_REMOVED.create(e, e.getMessage());
            } else if (e instanceof javax.mail.MethodNotSupportedException) {
                return MimeMailExceptionCode.METHOD_NOT_SUPPORTED.create(e, e.getMessage());
            } else if (e instanceof javax.mail.NoSuchProviderException) {
                return MimeMailExceptionCode.NO_SUCH_PROVIDER.create(e, e.getMessage());
            } else if (e instanceof javax.mail.internet.ParseException) {
                if (e instanceof javax.mail.internet.AddressException) {
                    final String optRef = ((AddressException) e).getRef();
                    return MimeMailExceptionCode.INVALID_EMAIL_ADDRESS.create(e, optRef == null ? STR_EMPTY : optRef);
                }
                return MimeMailExceptionCode.PARSE_ERROR.create(e, e.getMessage());
            } else if (e instanceof javax.mail.ReadOnlyFolderException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.READ_ONLY_FOLDER_EXT.create(
                        e,
                        e.getMessage(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return MimeMailExceptionCode.READ_ONLY_FOLDER.create(e, e.getMessage());
            } else if (e instanceof javax.mail.search.SearchException) {
                return MimeMailExceptionCode.SEARCH_ERROR.create(e, e.getMessage());
            } else if (e instanceof com.sun.mail.smtp.SMTPSendFailedException) {
                final SMTPSendFailedException exc = (SMTPSendFailedException) e;
                if ((exc.getReturnCode() == 552) || (exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1)) {
                    return MimeMailExceptionCode.MESSAGE_TOO_LARGE.create(exc, new Object[0]);
                }
                final Address[] addrs = exc.getInvalidAddresses();
                if (null == addrs || addrs.length == 0) {
                    // No invalid addresses available
                    return MimeMailExceptionCode.SEND_FAILED_MSG.create(exc, exc.getMessage());
                }
                return MimeMailExceptionCode.SEND_FAILED.create(exc, Arrays.toString(exc.getInvalidAddresses()));
            } else if (e instanceof javax.mail.SendFailedException) {
                final SendFailedException exc = (SendFailedException) e;
                if (exc.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_MSG_TOO_LARGE) > -1) {
                    return MimeMailExceptionCode.MESSAGE_TOO_LARGE.create(exc, new Object[0]);
                }
                final Exception nextException = exc.getNextException();
                if (nextException instanceof com.sun.mail.smtp.SMTPSendFailedException) {
                    final SMTPSendFailedException smtpExc = (SMTPSendFailedException) nextException;
                    final Address[] invalidAddresses = smtpExc.getInvalidAddresses();
                    if (null == invalidAddresses || invalidAddresses.length == 0) {
                        return MimeMailExceptionCode.SEND_FAILED_MSG_EXT.create(exc, exc.getMessage(), '(' + smtpExc.getMessage() + ')');
                    }
                }
                String serverInfo = null;
                if (nextException instanceof com.sun.mail.smtp.SMTPAddressFailedException) {
                    serverInfo = nextException.getMessage();
                }
                final Address[] addrs = exc.getInvalidAddresses();
                if (null == addrs || addrs.length == 0) {
                    // No invalid addresses available
                    return MimeMailExceptionCode.SEND_FAILED_MSG.create(exc, exc.getMessage());
                }
                return MimeMailExceptionCode.SEND_FAILED_EXT.create(
                    exc,
                    Arrays.toString(addrs),
                    null == serverInfo ? "" : '(' + serverInfo + ')');
            } else if (e instanceof javax.mail.StoreClosedException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.STORE_CLOSED_EXT.create(
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        EMPTY_ARGS);
                }
                return MimeMailExceptionCode.STORE_CLOSED.create(e, EMPTY_ARGS);
            }
            final Exception nextException = e.getNextException();
            if (nextException == null) {
                if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_QUOTA) != -1) {
                    return MimeMailExceptionCode.QUOTA_EXCEEDED.create(e, EMPTY_ARGS);
                } else if ("Unable to load BODYSTRUCTURE".equals(e.getMessage())) {
                    return MimeMailExceptionCode.MESSAGE_NOT_DISPLAYED.create(e, EMPTY_ARGS);
                }
                /*
                 * Default case
                 */
                return MimeMailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
            }
            /*
             * Messaging exception has a nested exception
             */
            if (nextException instanceof java.net.BindException) {
                return MimeMailExceptionCode.BIND_ERROR.create(e, mailConfig == null ? STR_EMPTY : Integer.valueOf(mailConfig.getPort()));
            } else if (nextException instanceof com.sun.mail.iap.ConnectionException) {
                mailInterfaceMonitor.changeNumBrokenConnections(true);
                return MimeMailExceptionCode.CONNECT_ERROR.create(
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
            } else if (nextException instanceof java.net.ConnectException) {
                /*
                 * Most modern IP stack implementations sense connection idleness, and abort the connection attempt, resulting in a
                 * java.net.ConnectionException
                 */
                mailInterfaceMonitor.changeNumTimeoutConnections(true);
                final OXException me =
                    MimeMailExceptionCode.CONNECT_ERROR.create(
                        e,
                        mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                        mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
                return me;
            } else if (nextException.getClass().getName().endsWith(EXC_CONNECTION_RESET_EXCEPTION)) {
                mailInterfaceMonitor.changeNumBrokenConnections(true);
                return MimeMailExceptionCode.CONNECTION_RESET.create(e, new Object[0]);
            } else if (nextException instanceof java.net.NoRouteToHostException) {
                return MimeMailExceptionCode.NO_ROUTE_TO_HOST.create(e, mailConfig == null ? STR_EMPTY : mailConfig.getServer());
            } else if (nextException instanceof java.net.PortUnreachableException) {
                return MimeMailExceptionCode.PORT_UNREACHABLE.create(
                    e,
                    mailConfig == null ? STR_EMPTY : Integer.valueOf(mailConfig.getPort()));
            } else if (nextException instanceof java.net.SocketException) {
                /*
                 * Treat dependent on message
                 */
                final SocketException se = (SocketException) nextException;
                if ("Socket closed".equals(se.getMessage()) || "Connection reset".equals(se.getMessage())) {
                    mailInterfaceMonitor.changeNumBrokenConnections(true);
                    return MimeMailExceptionCode.BROKEN_CONNECTION.create(e, mailConfig == null ? STR_EMPTY : mailConfig.getServer());
                }
                return MimeMailExceptionCode.SOCKET_ERROR.create(e, e.getMessage());
            } else if (nextException instanceof java.net.UnknownHostException) {
                return MimeMailExceptionCode.UNKNOWN_HOST.create(e, e.getMessage());
            } else if (nextException instanceof java.net.SocketTimeoutException) {
                mailInterfaceMonitor.changeNumBrokenConnections(true);
                return MimeMailExceptionCode.CONNECT_ERROR.create(
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
            } else if (nextException instanceof com.openexchange.mail.mime.QuotaExceededException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.QUOTA_EXCEEDED_EXT.create(
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        nextException.getMessage());
                }
                return MimeMailExceptionCode.QUOTA_EXCEEDED.create(e, nextException.getMessage());
            } else if (nextException instanceof com.sun.mail.iap.CommandFailedException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.PROCESSING_ERROR_WE_EXT.create(
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        skipTag(nextException.getMessage()));
                }
                return MimeMailExceptionCode.PROCESSING_ERROR_WE.create(e, skipTag(nextException.getMessage()));
            } else if (nextException instanceof com.sun.mail.iap.BadCommandException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.PROCESSING_ERROR_EXT.create(
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        nextException.getMessage());
                }
                return MimeMailExceptionCode.PROCESSING_ERROR.create(e, nextException.getMessage());
            } else if (nextException instanceof com.sun.mail.iap.ProtocolException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.PROCESSING_ERROR_EXT.create(
                        e,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        nextException.getMessage());
                }
                return MimeMailExceptionCode.PROCESSING_ERROR.create(e, nextException.getMessage());
            } else if (nextException instanceof java.io.IOException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.IO_ERROR_EXT.create(
                        e,
                        nextException.getMessage(),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return MimeMailExceptionCode.IO_ERROR.create(nextException, nextException.getMessage());
            } else if (e.getMessage().toLowerCase(Locale.ENGLISH).indexOf(ERR_QUOTA) != -1) {
                return MimeMailExceptionCode.QUOTA_EXCEEDED.create(e, EMPTY_ARGS);
            }
            /*
             * Default case
             */
            return MimeMailExceptionCode.MESSAGING_ERROR.create(nextException, nextException.getMessage());
        } catch (final Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(t.getMessage(), t);
            }
            /*
             * This routine should not fail since it's purpose is wrap a corresponding mail error around specified messaging error
             */
            return MimeMailExceptionCode.MESSAGING_ERROR.create(e, e.getMessage());
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
