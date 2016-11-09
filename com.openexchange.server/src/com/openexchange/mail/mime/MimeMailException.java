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

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Store;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;
import com.openexchange.tools.exceptions.ExceptionUtils;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ResponseCode;
import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPSendTimedoutException;
import com.sun.mail.smtp.SMTPSenderFailedException;

/**
 * {@link OXException} - For MIME related errors.
 * <p>
 * Taken from {@link MailExceptionCode}:
 * <p>
 * The detail number range in subclasses generated in mail bundles is supposed to start with 2000 and may go up to 2999.
 * <p>
 * The detail number range in subclasses generated in transport bundles is supposed to start with 3000 and may go up to 3999.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MimeMailException extends OXException {

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeMailException.class);

    private static final long serialVersionUID = -3401580182929349354L;

    private static final AtomicReference<ServiceListing<MimeMailExceptionHandler>> EXCEPTION_HANDLERS_REF = new AtomicReference<ServiceListing<MimeMailExceptionHandler>>(null);

    /**
     * Sets the given exception handlers.
     *
     * @param handlers The handlers to set
     */
    public static void setExceptionHandlers(ServiceListing<MimeMailExceptionHandler> handlers) {
        EXCEPTION_HANDLERS_REF.set(handlers);
    }

    /**
     * Unsets the given exception handlers.
     *
     * @param handlers The handlers to set
     */
    public static void unsetExceptionHandlers() {
        EXCEPTION_HANDLERS_REF.set(null);
    }

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

    /**
     * Handles given instance of {@link MessagingException} and creates an appropriate instance of {@link OXException}
     *
     * @param e The messaging exception
     * @param mailConfig The corresponding mail configuration used to add information like mail server etc.
     * @param session The session providing user information
     * @return An appropriate instance of {@link OXException}
     */
    public static OXException handleMessagingException(final MessagingException e, final MailConfig mailConfig, final Session session) {
        return handleMessagingException(e, mailConfig, session, null);
    }

    private static final String STR_EMPTY = "";

    private static final String ERR_TMP = "temporary error, please try again later";

    private static final String ERR_TMP_FLR = "temporary failure";

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
     * @param folder The optional folder
     * @return An appropriate instance of {@link OXException}
     */
    public static OXException handleMessagingException(final MessagingException e, final MailConfig mailConfig, final Session session, final Folder folder) {
        try {
            // Put log properties
            if (null != mailConfig) {
                LogProperties.put(Name.MAIL_ACCOUNT_ID, Integer.valueOf(mailConfig.getAccountId()));
                LogProperties.put(Name.MAIL_HOST, mailConfig.getServer());
                LogProperties.put(Name.MAIL_LOGIN, mailConfig.getLogin());
                if (null != folder) {
                    LogProperties.put(Name.MAIL_FULL_NAME, folder.getFullName());
                }
            }
            // Consult exception handlers first
            {
                ServiceListing<MimeMailExceptionHandler> handlers = EXCEPTION_HANDLERS_REF.get();
                if (null != handlers) {
                    OXException handled = null;
                    for (MimeMailExceptionHandler handler : handlers) {
                        handled = handler.handle(e, mailConfig, session, folder);
                        if (null != handled) {
                            return handled;
                        }
                    }
                }
            }
            // Start examining MessageException
            if (e instanceof MessageRemovedException) {
                // Message has been removed in the meantime
                if (null != folder) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(e, "", folder.getFullName());
                }
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, new Object[0]);
            }
            if ((e instanceof javax.mail.AuthenticationFailedException) || ((toLowerCase(e.getMessage(), "").indexOf(ERR_AUTH_FAILED) != -1))) {
                // Authentication failed
                return handleAuthenticationFailedException(e, mailConfig, session);
            } else if (e instanceof javax.mail.FolderClosedException) {
                final Folder f = ((javax.mail.FolderClosedException) e).getFolder();
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.FOLDER_CLOSED_EXT.create(
                        e,
                        null == f ? appendInfo(e.getMessage(), folder) : f.getFullName(),
                            mailConfig.getServer(),
                            mailConfig.getLogin(),
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()));
                }
                return MimeMailExceptionCode.FOLDER_CLOSED.create(e, null == f ? appendInfo(e.getMessage(), folder) : f.getFullName());
            } else if (e instanceof javax.mail.FolderNotFoundException) {
                final Folder f = ((javax.mail.FolderNotFoundException) e).getFolder();
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.FOLDER_NOT_FOUND_EXT.create(
                        e,
                        null == f ? appendInfo(e.getMessage(), folder) : f.getFullName(),
                            mailConfig.getServer(),
                            mailConfig.getLogin(),
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()));
                }
                return MimeMailExceptionCode.FOLDER_NOT_FOUND.create(e, null == f ? appendInfo(e.getMessage(), folder) : f.getFullName());
            } else if (e instanceof javax.mail.IllegalWriteException) {
                return MimeMailExceptionCode.ILLEGAL_WRITE.create(e, appendInfo(e.getMessage(), folder));
            } else if (e instanceof javax.mail.MessageRemovedException) {
                return MimeMailExceptionCode.MESSAGE_REMOVED.create(e, appendInfo(e.getMessage(), folder));
            } else if (e instanceof javax.mail.MethodNotSupportedException) {
                return MimeMailExceptionCode.METHOD_NOT_SUPPORTED.create(e, appendInfo(e.getMessage(), folder));
            } else if (e instanceof javax.mail.NoSuchProviderException) {
                return MimeMailExceptionCode.NO_SUCH_PROVIDER.create(e, appendInfo(e.getMessage(), folder));
            } else if (e instanceof javax.mail.internet.ParseException) {
                if (e instanceof javax.mail.internet.AddressException) {
                    final String optRef = ((AddressException) e).getRef();
                    return MimeMailExceptionCode.INVALID_EMAIL_ADDRESS.create(e, optRef == null ? STR_EMPTY : optRef);
                }
                return MimeMailExceptionCode.PARSE_ERROR.create(e, appendInfo(e.getMessage(), folder));
            } else if (e instanceof javax.mail.ReadOnlyFolderException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.READ_ONLY_FOLDER_EXT.create(
                        e,
                        appendInfo(e.getMessage(), folder),
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                return MimeMailExceptionCode.READ_ONLY_FOLDER.create(e, appendInfo(e.getMessage(), folder));
            } else if (e instanceof javax.mail.search.SearchException) {
                return MimeMailExceptionCode.SEARCH_ERROR.create(e, appendInfo(e.getMessage(), folder));
            } else if (e instanceof com.sun.mail.smtp.SMTPSendTimedoutException) {
                // Encountered timeout while trying to send a message to a recipient
                SMTPSendTimedoutException timedoutException = (SMTPSendTimedoutException) e;
                String cmd = timedoutException.getCommand();
                InternetAddress addr = timedoutException.getAddr();
                return MimeMailExceptionCode.SEND_TIMED_OUT_ERROR.create(e, addr.toUnicodeString(), cmd);
            } else if (e instanceof com.sun.mail.smtp.SMTPSenderFailedException) {
                SMTPSenderFailedException failedException = (SMTPSenderFailedException) e;
                SmtpInfo smtpInfo = getSmtpInfo(failedException);

                // Message too large?
                if ((smtpInfo.retCode == 552) || (toLowerCase(smtpInfo.message, "").indexOf(ERR_MSG_TOO_LARGE) > -1)) {
                    return MimeMailExceptionCode.MESSAGE_TOO_LARGE_EXT.create(failedException, smtpInfo.toString());
                }
                return MimeMailExceptionCode.SEND_FAILED_MSG_EXT_ERROR.create(failedException, failedException.getMessage(), smtpInfo.toString());
            } else if (e instanceof com.sun.mail.smtp.SMTPAddressFailedException) {
                SMTPAddressFailedException failedException = (SMTPAddressFailedException) e;
                SmtpInfo smtpInfo = getSmtpInfo(failedException);

                // Message too large?
                if ((smtpInfo.retCode == 552) || (toLowerCase(smtpInfo.message, "").indexOf(ERR_MSG_TOO_LARGE) > -1)) {
                    return MimeMailExceptionCode.MESSAGE_TOO_LARGE_EXT.create(failedException, smtpInfo.toString());
                }
                return MimeMailExceptionCode.SEND_FAILED_MSG_EXT_ERROR.create(failedException, failedException.getMessage(), smtpInfo.toString());
            } else if (e instanceof com.sun.mail.smtp.SMTPSendFailedException) {
                SMTPSendFailedException sendFailedError = (SMTPSendFailedException) e;
                SmtpInfo smtpInfo = getSmtpInfo(sendFailedError);

                // Message too large?
                if ((smtpInfo.retCode == 552) || (toLowerCase(smtpInfo.message, "").indexOf(ERR_MSG_TOO_LARGE) > -1)) {
                    return MimeMailExceptionCode.MESSAGE_TOO_LARGE_EXT.create(sendFailedError, smtpInfo.toString());
                }
                // 452 - 452 4.1.0 ... temporary failure
                if ((sendFailedError.getReturnCode() == 452) && (toLowerCase(sendFailedError.getMessage(), "").indexOf(ERR_TMP_FLR) > -1)) {
                    return MimeMailExceptionCode.TEMPORARY_FAILURE.create(sendFailedError, getSmtpInfo(sendFailedError));
                }
                Address[] addrs = sendFailedError.getInvalidAddresses();
                if (null == addrs || addrs.length == 0) {
                    // No invalid addresses available
                    addrs = sendFailedError.getValidUnsentAddresses();
                    if (null == addrs || addrs.length == 0) {
                        // Neither valid unsent addresses
                        return MimeMailExceptionCode.SEND_FAILED_MSG_ERROR.create(sendFailedError, smtpInfo.toString());
                    }
                }

                return MimeMailExceptionCode.SEND_FAILED_EXT.create(sendFailedError, Arrays.toString(addrs), smtpInfo.toString());
            } else if (e instanceof javax.mail.SendFailedException) {
                SendFailedException exc = (SendFailedException) e;
                SmtpInfo smtpInfo = null;
                Address[] invalidAddresses = exc.getInvalidAddresses();
                {
                    final Exception nextException = exc.getNextException();
                    if (nextException instanceof com.sun.mail.smtp.SMTPSendFailedException) {
                        com.sun.mail.smtp.SMTPSendFailedException failedError = (com.sun.mail.smtp.SMTPSendFailedException) nextException;
                        smtpInfo = getSmtpInfo(failedError);
                        if (invalidAddresses == null || invalidAddresses.length == 0) {
                            invalidAddresses = failedError.getInvalidAddresses();
                            if (null == invalidAddresses || invalidAddresses.length == 0) {
                                invalidAddresses = failedError.getValidUnsentAddresses();
                            }
                        }
                    } else if (nextException instanceof com.sun.mail.smtp.SMTPSenderFailedException) {
                        com.sun.mail.smtp.SMTPSenderFailedException failedError = (com.sun.mail.smtp.SMTPSenderFailedException) nextException;
                        smtpInfo = getSmtpInfo(failedError);
                        if (invalidAddresses == null || invalidAddresses.length == 0) {
                            invalidAddresses = failedError.getInvalidAddresses();
                            if (null == invalidAddresses || invalidAddresses.length == 0) {
                                invalidAddresses = failedError.getValidUnsentAddresses();
                            }
                        }
                    } else if (nextException instanceof com.sun.mail.smtp.SMTPAddressFailedException) {
                        com.sun.mail.smtp.SMTPAddressFailedException failedError = (com.sun.mail.smtp.SMTPAddressFailedException) nextException;
                        smtpInfo = getSmtpInfo(failedError);
                        if (invalidAddresses == null || invalidAddresses.length == 0) {
                            invalidAddresses = failedError.getInvalidAddresses();
                            if (null == invalidAddresses || invalidAddresses.length == 0) {
                                invalidAddresses = failedError.getValidUnsentAddresses();
                            }
                        }
                    }
                }

                // Message too large?
                if (null != smtpInfo && ((smtpInfo.retCode == 552) || (toLowerCase(smtpInfo.message, "").indexOf(ERR_MSG_TOO_LARGE) > -1))) {
                    return MimeMailExceptionCode.MESSAGE_TOO_LARGE_EXT.create(exc, smtpInfo.toString());
                }

                // Others...
                if (null == invalidAddresses || invalidAddresses.length == 0) {
                    return MimeMailExceptionCode.SEND_FAILED_MSG_ERROR.create(exc, null == smtpInfo ? exc.getMessage() : smtpInfo.toString());
                }
                return MimeMailExceptionCode.SEND_FAILED_EXT.create(exc, Arrays.toString(invalidAddresses), null == smtpInfo ? exc.getMessage() : smtpInfo.toString());
            } else if (e instanceof javax.mail.StoreClosedException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.STORE_CLOSED_EXT.create(e, mailConfig.getServer(), mailConfig.getLogin(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), EMPTY_ARGS);
                }
                return MimeMailExceptionCode.STORE_CLOSED.create(e, EMPTY_ARGS);
            }
            final Exception nextException = e.getNextException();
            if (nextException == null) {
                if (toLowerCase(e.getMessage(), "").indexOf(ERR_QUOTA) != -1) {
                    return MimeMailExceptionCode.QUOTA_EXCEEDED.create(e, getInfo(skipTag(e.getMessage())));
                } else if ("Unable to load BODYSTRUCTURE".equals(e.getMessage())) {
                    return MimeMailExceptionCode.MESSAGE_NOT_DISPLAYED.create(e, EMPTY_ARGS);
                }
                /*
                 * Default case
                 */
                final String message = com.openexchange.java.Strings.toLowerCase(e.getMessage());
                if ("failed to load imap envelope".equals(message)) {
                    return MimeMailExceptionCode.MESSAGE_NOT_DISPLAYED.create(e);
                }
                if ("connection failure".equals(e.getMessage())) {
                    return MimeMailExceptionCode.NO_ROUTE_TO_HOST.create(e, mailConfig == null ? STR_EMPTY : mailConfig.getServer());
                }
                return MimeMailExceptionCode.MESSAGING_ERROR.create(e, appendInfo(e.getMessage(), folder));
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
                return MimeMailExceptionCode.SOCKET_ERROR.create(e, new Object[0]);
            } else if (nextException instanceof java.net.UnknownHostException) {
                return MimeMailExceptionCode.UNKNOWN_HOST.create(e, appendInfo(e.getMessage(), folder));
            } else if (nextException instanceof java.net.SocketTimeoutException) {
                mailInterfaceMonitor.changeNumBrokenConnections(true);
                return MimeMailExceptionCode.CONNECT_ERROR.create(
                    e,
                    mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                        mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
            } else if (nextException instanceof com.openexchange.mail.mime.QuotaExceededException) {
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.QUOTA_EXCEEDED_EXT.create(
                        nextException,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        appendInfo(getInfo(skipTag(nextException.getMessage())), folder));
                }
                return MimeMailExceptionCode.QUOTA_EXCEEDED.create(nextException, appendInfo(getInfo(skipTag(nextException.getMessage())), folder));
            } else if (nextException instanceof com.sun.mail.iap.CommandFailedException) {
                com.sun.mail.iap.CommandFailedException cfe = (com.sun.mail.iap.CommandFailedException) nextException;
                OXException handled = handleProtocolExceptionByResponseCode(cfe, mailConfig, session, folder);
                if (null != handled) {
                    return handled;
                }

                String msg = com.openexchange.java.Strings.toLowerCase(nextException.getMessage());
                if (isOverQuotaException(msg)) {
                    // Over quota
                    if (null != mailConfig && null != session) {
                        return MimeMailExceptionCode.QUOTA_EXCEEDED_EXT.create(
                            nextException,
                            mailConfig.getServer(),
                            mailConfig.getLogin(),
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()),
                            appendInfo(getInfo(skipTag(nextException.getMessage())), folder));
                    }
                    return MimeMailExceptionCode.QUOTA_EXCEEDED.create(nextException, appendInfo(getInfo(skipTag(nextException.getMessage())), folder));
                }
                // Regular processing error cause by arbitrary CommandFailedException
                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.PROCESSING_ERROR_WE_EXT.create(
                        nextException,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        appendInfo(getInfo(skipTag(nextException.getMessage())), folder));
                }
                return MimeMailExceptionCode.PROCESSING_ERROR_WE.create(nextException, appendInfo(getInfo(skipTag(nextException.getMessage())), folder));
            } else if (nextException instanceof com.sun.mail.iap.BadCommandException) {
                com.sun.mail.iap.BadCommandException bce = (com.sun.mail.iap.BadCommandException) nextException;
                OXException handled = handleProtocolExceptionByResponseCode(bce, mailConfig, session, folder);
                if (null != handled) {
                    return handled;
                }

                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.PROCESSING_ERROR_EXT.create(
                        nextException,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        appendInfo(nextException.getMessage(), folder));
                }
                return MimeMailExceptionCode.PROCESSING_ERROR.create(nextException, appendInfo(nextException.getMessage(), folder));
            } else if (nextException instanceof com.sun.mail.iap.ProtocolException) {
                com.sun.mail.iap.ProtocolException pe = (com.sun.mail.iap.ProtocolException) nextException;
                OXException handled = handleProtocolExceptionByResponseCode(pe, mailConfig, session, folder);
                if (null != handled) {
                    return handled;
                }

                Throwable protocolError = pe.getCause();
                if (protocolError instanceof IOException) {
                    return handleIOException((IOException) protocolError, mailConfig, session, folder);
                }

                if (null != mailConfig && null != session) {
                    return MimeMailExceptionCode.PROCESSING_ERROR_EXT.create(
                        nextException,
                        mailConfig.getServer(),
                        mailConfig.getLogin(),
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()),
                        appendInfo(nextException.getMessage(), folder));
                }
                return MimeMailExceptionCode.PROCESSING_ERROR.create(nextException, appendInfo(nextException.getMessage(), folder));
            } else if (nextException instanceof java.io.IOException) {
                return handleIOException((IOException) nextException, mailConfig, session, folder);
            } else if (toLowerCase(e.getMessage(), "").indexOf(ERR_QUOTA) != -1) {
                return MimeMailExceptionCode.QUOTA_EXCEEDED.create(e, getInfo(skipTag(e.getMessage())));
            }
            /*
             * Default case
             */
            return MimeMailExceptionCode.MESSAGING_ERROR.create(nextException, appendInfo(nextException.getMessage(), folder));
        } catch (final Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.warn("", t);
            /*
             * This routine should not fail since it's purpose is wrap a corresponding mail error around specified messaging error
             */
            return MimeMailExceptionCode.MESSAGING_ERROR.create(e, appendInfo(e.getMessage(), folder));
        }
    }

    /**
     * Handles specified IMAP protocol exception by its possibly available <a href="https://tools.ietf.org/html/rfc5530">response code</a>.<br>
     * If no such response code is present, <code>null</code> is returned.
     *
     * @param pe The IMAP protocol exception
     * @param mailConfig The optional mail configuration associated with affected user
     * @param session The optional affected user's session
     * @param folder The optional folder
     * @return The {@link OXException} instance suitable for response code or <code>null</code>
     */
    public static OXException handleProtocolExceptionByResponseCode(com.sun.mail.iap.ProtocolException pe, MailConfig mailConfig, Session session, Folder folder) {
        com.sun.mail.iap.ResponseCode rc = pe.getKnownResponseCode();
        if (null == rc) {
            return null;
        }

        switch (rc) {
            case ALREADYEXISTS:
                return MailExceptionCode.DUPLICATE_FOLDER_SIMPLE.create(pe, new Object[0]);
            case AUTHENTICATIONFAILED:
                return handleAuthenticationFailedException(pe, mailConfig, session);
            case AUTHORIZATIONFAILED:
                return handleAuthenticationFailedException(pe, mailConfig, session);
            case CANNOT:
                return MailExceptionCode.INVALID_FOLDER_NAME_SIMPLE.create(pe, pe.getResponseRest());
            case CLIENTBUG:
                break;
            case CONTACTADMIN:
                break;
            case CORRUPTION:
                break;
            case EXPIRED:
                return handleAuthenticationFailedException(pe, mailConfig, session);
            case EXPUNGEISSUED:
                break;
            case INUSE:
                {
                    // Too many sessions in use
                    if (null != mailConfig && null != session) {
                        return MimeMailExceptionCode.IN_USE_ERROR_EXT.create(
                            pe,
                            mailConfig.getServer(),
                            mailConfig.getLogin(),
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()),
                            appendInfo(getInfo(skipTag(pe.getMessage())), folder)).setCategory(CATEGORY_USER_INPUT);
                    }
                    return MimeMailExceptionCode.IN_USE_ERROR.create(pe, appendInfo(getInfo(skipTag(pe.getMessage())), folder)).setCategory(CATEGORY_USER_INPUT);
                }
            case LIMIT:
                break;
            case NONEXISTENT:
                break;
            case NOPERM:
                return MailExceptionCode.INSUFFICIENT_PERMISSIONS.create(pe, new Object[0]);
            case OVERQUOTA:
                {
                    // Over quota
                    if (null != mailConfig && null != session) {
                        return MimeMailExceptionCode.QUOTA_EXCEEDED_EXT.create(
                            pe,
                            mailConfig.getServer(),
                            mailConfig.getLogin(),
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(session.getContextId()),
                            appendInfo(getInfo(skipTag(pe.getMessage())), folder));
                    }
                    return MimeMailExceptionCode.QUOTA_EXCEEDED.create(pe, appendInfo(getInfo(skipTag(pe.getMessage())), folder));
                }
            case PRIVACYREQUIRED:
                return MailExceptionCode.NONSECURE_CONNECTION_DENIED.create(pe, new Object[0]);
            case SERVERBUG:
                break;
            case UNAVAILABLE:
                return MailExceptionCode.SUBSYSTEM_DOWN.create(pe, new Object[0]);
            default:
                break;
        }

        return null;
    }

    private static OXException handleAuthenticationFailedException(Exception authenticationFailedException, MailConfig mailConfig, Session session) {
        // Authentication failed
        if (null != mailConfig && MailAccount.DEFAULT_ID == mailConfig.getAccountId()) {
            return MimeMailExceptionCode.LOGIN_FAILED.create(authenticationFailedException, mailConfig.getServer(), mailConfig.getLogin());
        }
        if ((authenticationFailedException.getMessage() != null) && ERR_TMP.equals(com.openexchange.java.Strings.toLowerCase(authenticationFailedException.getMessage()))) {
            return MimeMailExceptionCode.LOGIN_FAILED.create(
                authenticationFailedException,
                mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                    mailConfig == null ? STR_EMPTY : mailConfig.getLogin());
        }
        if (null != mailConfig && null != session) {
            return MimeMailExceptionCode.INVALID_CREDENTIALS_EXT.create(
                authenticationFailedException,
                mailConfig.getServer(),
                mailConfig.getLogin(),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()),
                authenticationFailedException.getMessage());
        }
        return MimeMailExceptionCode.INVALID_CREDENTIALS.create(
            authenticationFailedException,
            mailConfig == null ? STR_EMPTY : mailConfig.getServer(),
                authenticationFailedException.getMessage());
    }

    private static OXException handleIOException(IOException ioException, MailConfig mailConfig, Session session, Folder folder) {
        if (null != mailConfig && null != session) {
            return MimeMailExceptionCode.IO_ERROR_EXT.create(
                ioException,
                appendInfo(ioException.getMessage(), folder),
                mailConfig.getServer(),
                mailConfig.getLogin(),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }
        return MimeMailExceptionCode.IO_ERROR.create(ioException, appendInfo(ioException.getMessage(), folder));
    }

    /**
     * Appends command information to given information string.
     *
     * @param info The information
     * @param folder The optional folder
     * @return The command with optional information appended
     */
    public static String appendInfo(final String info, final Folder folder) {
        if (null == folder) {
            return info;
        }
        final StringBuilder sb = null == info ? new StringBuilder(64) : new StringBuilder(info);
        sb.append(" (folder=\"").append(folder.getFullName()).append('"');
        final Store store = folder.getStore();
        if (null != store) {
            sb.append(", store=\"").append(store.toString()).append('"');
        }
        sb.append(')');
        return sb.toString();
    }

    private static String getInfo(final String info) {
        if (null == info) {
            return info;
        }
        final int pos = com.openexchange.java.Strings.toLowerCase(info).indexOf("error message: ");
        return pos < 0 ? info : info.substring(pos + 15);
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

    private static <E> E lookupNested(final MessagingException e, final Class<E> clazz) {
        if (null == e) {
            return null;
        }

        Exception exception = e.getNextException();
        if (clazz.isInstance(exception)) {
            return clazz.cast(exception);
        }
        return exception instanceof MessagingException ? lookupNested((MessagingException) exception, clazz) : null;
    }

    /**
     * Checks for possible exists error.
     */
    public static boolean isExistsException(final MessagingException e) {
        if (null == e) {
            return false;
        }
        return isExistsException(e.getMessage());
    }

    /**
     * Checks for possible exists error.
     */
    public static boolean isExistsException(final String msg) {
        if (null == msg) {
            return false;
        }
        final String m = com.openexchange.java.Strings.toLowerCase(msg);
        return (m.indexOf("exists") >= 0);
    }

    /**
     * Checks for possible already-exists error.
     */
    public static boolean isAlreadyExistsException(final MessagingException e) {
        if (null == e) {
            return false;
        }

        com.sun.mail.iap.ProtocolException pe = lookupNested(e, com.sun.mail.iap.ProtocolException.class);
        if (null != pe) {
            if (ResponseCode.ALREADYEXISTS == pe.getKnownResponseCode()) {
                return true;
            }
        }

        return isAlreadyExistsException(e.getMessage());
    }

    /**
     * Checks for possible already-exists error.
     */
    public static boolean isAlreadyExistsException(final String msg) {
        if (null == msg) {
            return false;
        }
        final String m = com.openexchange.java.Strings.toLowerCase(msg);
        return (m.indexOf("alreadyexists") >= 0);
    }

    /**
     * Checks for possible over-quota error.
     */
    public static boolean isOverQuotaException(final MessagingException e) {
        if (null == e) {
            return false;
        }

        com.sun.mail.iap.ProtocolException pe = lookupNested(e, com.sun.mail.iap.ProtocolException.class);
        if (null != pe) {
            if (ResponseCode.OVERQUOTA == pe.getKnownResponseCode()) {
                return true;
            }
        }

        return isOverQuotaException(e.getMessage());
    }

    /**
     * Checks for possible over-quota error.
     */
    public static boolean isOverQuotaException(String msg) {
        if (null == msg) {
            return false;
        }
        final String m = com.openexchange.java.Strings.toLowerCase(msg);
        return (m.indexOf("quota") >= 0 || m.indexOf("limit") >= 0);
    }

    /**
     * Checks for possible in-use error.
     */
    public static boolean isInUseException(MessagingException e) {
        if (null == e) {
            return false;
        }

        com.sun.mail.iap.ProtocolException pe = lookupNested(e, com.sun.mail.iap.ProtocolException.class);
        if (null != pe) {
            if (ResponseCode.INUSE == pe.getKnownResponseCode()) {
                return true;
            }
        }

        return isInUseException(com.openexchange.java.Strings.toLowerCase(e.getMessage()));
    }

    /**
     * Checks for possible in-use error.
     */
    public static boolean isInUseException(final String msg) {
        if (null == msg) {
            return false;
        }
        return (com.openexchange.java.Strings.toLowerCase(msg).indexOf("[inuse]") >= 0);
    }

    /**
     * Checks for possible command-failed error.
     */
    public static boolean isCommandFailedException(MessagingException e) {
        if (null == e) {
            return false;
        }
        CommandFailedException commandFailedError = lookupNested(e, com.sun.mail.iap.CommandFailedException.class);
        return null != commandFailedError;
    }

    /**
     * Checks if cause of specified exception indicates a communication problem; such as read timeout, EOF, etc.
     *
     * @param e The exception to examine
     * @return <code>true</code> if a communication problem is indicated; otherwise <code>false</code>
     */
    public static boolean isCommunicationException(OXException e) {
        if (null == e) {
            return false;
        }

        Throwable next = e.getCause();
        if (next instanceof OXException) {
            return isCommunicationException((OXException) next);
        }
        if (next instanceof MessagingException) {
            return isCommunicationException((MessagingException) next);
        }
        return isEitherOf(next == null ? e : next, com.sun.mail.iap.ByeIOException.class, java.net.SocketTimeoutException.class, java.io.EOFException.class);
    }

    /**
     * Checks if cause of specified messaging exception indicates a communication problem; such as read timeout, EOF, etc.
     *
     * @param e The messaging exception to examine
     * @return <code>true</code> if a communication problem is indicated; otherwise <code>false</code>
     */
    public static boolean isCommunicationException(MessagingException e) {
        if (null == e) {
            return false;
        }

        javax.mail.FolderClosedException folderClosedError = lookupNested(e, javax.mail.FolderClosedException.class);
        if (null != folderClosedError) {
            return true;
        }

        javax.mail.StoreClosedException storeClosedError = lookupNested(e, javax.mail.StoreClosedException.class);
        if (null != storeClosedError) {
            return true;
        }

        return isEitherOf(e, com.sun.mail.iap.ByeIOException.class, java.net.SocketTimeoutException.class, java.io.EOFException.class);
    }

    /**
     * Checks if cause of specified messaging exception indicates a timeout problem.
     *
     * @param e The messaging exception to examine
     * @return <code>true</code> if a timeout problem is indicated; otherwise <code>false</code>
     */
    public static boolean isTimeoutException(MessagingException e) {
        if (null == e) {
            return false;
        }

        return isEitherOf(e, java.net.SocketTimeoutException.class);
    }

    //java.net.SocketTimeoutException

    private static boolean isEitherOf(Throwable e, Class<? extends Exception>... classes) {
        if (null == e) {
            return false;
        }

        for (Class<? extends Exception> clazz : classes) {
            if (clazz.isInstance(e)) {
                return true;
            }
        }

        Throwable next = (e instanceof MessagingException) ? ((MessagingException) e).getNextException() : e.getCause();
        return null == next ? false : isEitherOf(next, classes);
    }

    // ------------------------------------------------- SMTP error stuff ----------------------------------------------------------------

    private static final class SmtpInfo {

        final int retCode;
        final String message;

        SmtpInfo(int retCode, String message) {
            super();
            this.retCode = retCode;
            this.message = message;
        }

        @Override
        public String toString() {
            return new StringBuilder(64).append(retCode).append(" - ").append(message).toString();
        }
    }

    private static SmtpInfo getSmtpInfo(SMTPSendFailedException sendFailedError) {
        if (null == sendFailedError) {
            return null;
        }

        int retCode = sendFailedError.getReturnCode();
        if ((retCode >= 400 && retCode <= 499) || (retCode >= 500 && retCode <= 599)) {
            // An SMTP error
            return new SmtpInfo(sendFailedError.getReturnCode(), sendFailedError.getMessage());
        }

        // Check if nested exception reveals the actual SMTP error
        SmtpInfo smtpInfo = optSmtpInfo(sendFailedError.getNextException());
        if (null != smtpInfo) {
            return smtpInfo;
        }

        // Return specified exception's SMTP info as last resort
        return new SmtpInfo(sendFailedError.getReturnCode(), sendFailedError.getMessage());
    }

    private static SmtpInfo getSmtpInfo(SMTPAddressFailedException sendFailedError) {
        if (null == sendFailedError) {
            return null;
        }

        int retCode = sendFailedError.getReturnCode();
        if ((retCode >= 400 && retCode <= 499) || (retCode >= 500 && retCode <= 599)) {
            // An SMTP error
            return new SmtpInfo(sendFailedError.getReturnCode(), sendFailedError.getMessage());
        }

        // Check if nested exception reveals the actual SMTP error
        SmtpInfo smtpInfo = optSmtpInfo(sendFailedError.getNextException());
        if (null != smtpInfo) {
            return smtpInfo;
        }

        // Return specified exception's SMTP info as last resort
        return new SmtpInfo(sendFailedError.getReturnCode(), sendFailedError.getMessage());
    }

    private static SmtpInfo getSmtpInfo(SMTPSenderFailedException sendFailedError) {
        if (null == sendFailedError) {
            return null;
        }

        int retCode = sendFailedError.getReturnCode();
        if ((retCode >= 400 && retCode <= 499) || (retCode >= 500 && retCode <= 599)) {
            // An SMTP error
            return new SmtpInfo(sendFailedError.getReturnCode(), sendFailedError.getMessage());
        }

        // Check if nested exception reveals the actual SMTP error
        SmtpInfo smtpInfo = optSmtpInfo(sendFailedError.getNextException());
        if (null != smtpInfo) {
            return smtpInfo;
        }

        // Return specified exception's SMTP info as last resort
        return new SmtpInfo(sendFailedError.getReturnCode(), sendFailedError.getMessage());
    }

    private static SmtpInfo optSmtpInfo(Exception possibleSmtpException) {
        if (null == possibleSmtpException) {
            return null;
        }

        if (possibleSmtpException instanceof SMTPSendFailedException) {
            return getSmtpInfo((SMTPSendFailedException) possibleSmtpException);
        }
        if (possibleSmtpException instanceof SMTPAddressFailedException) {
            return getSmtpInfo((SMTPAddressFailedException) possibleSmtpException);
        }
        if (possibleSmtpException instanceof SMTPSenderFailedException) {
            return getSmtpInfo((SMTPSenderFailedException) possibleSmtpException);
        }

        return null;
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars, final String defaultValue) {
        if (null == chars) {
            return defaultValue;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
