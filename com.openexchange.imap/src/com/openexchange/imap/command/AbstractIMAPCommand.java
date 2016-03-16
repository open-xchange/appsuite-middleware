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

package com.openexchange.imap.command;

import static com.openexchange.imap.IMAPCommandsCollection.performCommand;
import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotaExceededException;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * {@link AbstractIMAPCommand} - Abstract class for an IMAP command.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractIMAPCommand<T> {

    protected static final String[] ARGS_EMPTY = { "" };

    protected static final String[] ARGS_ALL = { "1:*" };

    /**
     * The IMAP folder associated with the command to execute
     */
    protected final IMAPFolder imapFolder;

    /**
     * Indicates if processing should be stopped right at the beginning and the default value should be returned
     */
    protected boolean returnDefaultValue;

    private final CallbackIMAPProtocolCommand protocolCommand;

    /**
     * Initializes a new {@link AbstractIMAPCommand}.
     *
     * @param imapFolder The IMAP folder
     */
    protected AbstractIMAPCommand(final IMAPFolder imapFolder) {
        super();
        this.imapFolder = imapFolder;
        this.protocolCommand = new CallbackIMAPProtocolCommand(this, imapFolder);
    }

    private static final class CallbackIMAPProtocolCommand implements IMAPFolder.ProtocolCommand {

        private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CallbackIMAPProtocolCommand.class);

        private final AbstractIMAPCommand<?> abstractIMAPCommand;
        private final IMAPFolder imapFolder;

        protected CallbackIMAPProtocolCommand(final AbstractIMAPCommand<?> abstractIMAPCommand, final IMAPFolder imapFolder) {
            super();
            this.abstractIMAPCommand = abstractIMAPCommand;
            this.imapFolder = imapFolder;
        }

        @Override
        public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
            if (abstractIMAPCommand.returnDefaultValue) {
                /*
                 * Abort processing
                 */
                return abstractIMAPCommand.getDefaultValue();
            }
            final String[] args = abstractIMAPCommand.getArgs();
            Response[] r = null;
            Response response = null;
            for (int argsIndex = 0; argsIndex < args.length; argsIndex++) {
                final String imapCmd = abstractIMAPCommand.getCommand(argsIndex);
                r = performCommand(protocol, imapCmd);
                response = r[r.length - 1];
                if (response.isOK()) {
                    try {
                        for (int index = 0; (index < r.length) && abstractIMAPCommand.addLoopCondition(); index++) {
                            if (abstractIMAPCommand.handleResponse(r[index])) {
                                /*
                                 * Discard handled response
                                 */
                                r[index] = null;
                            }
                        }
                        /*
                         * Safely dispatch unhandled responses
                         */
                        try {
                            protocol.notifyResponseHandlers(r);
                        } catch (final RuntimeException e) {
                            // Ignore runtime error in trailing Protocol.notifyResponseHandlers() invocation
                            LOG.debug("Runtime error during Protocol.notifyResponseHandlers() invocation.", e);
                        }
                    } catch (final MessagingException e) {
                        final ProtocolException pe = new ProtocolException(e.getMessage());
                        pe.initCause(e);
                        throw pe;
                    }
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return abstractIMAPCommand.getDefaultValue();
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(imapCmd));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        imapCmd,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    final String error = com.openexchange.java.Strings.toLowerCase(response.toString());
                    if (MimeMailException.isOverQuotaException(error)) {
                        /*
                         * Assume a quota exceeded exception
                         */
                        throw new QuotaExceededException(response);
                    }
                    if (null != error && error.indexOf("[nonexistent]") >= 0) {
                        /*
                         * Treat as an empty folder
                         */
                        return abstractIMAPCommand.getDefaultValue();
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(imapCmd));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        imapCmd,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(imapCmd));
                    protocol.handleResult(response);
                }
            }
            try {
                return abstractIMAPCommand.getReturnVal();
            } catch (final MessagingException e) {
                final ProtocolException pe = new ProtocolException(e.getMessage());
                pe.initCause(e);
                throw pe;
            }
        }
    }

    public final T doCommand() throws MessagingException {
        final long start = System.currentTimeMillis();
        final @SuppressWarnings("unchecked") T obj = (T) imapFolder.doCommand(protocolCommand);
        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        return obj;
    }

    /**
     * Returns the debug info.
     *
     * @param The argument index
     * @return The debug info
     */
    protected String getDebugInfo(final int argsIndex) {
        return null;
    }

    /**
     * Gets the IMAP command to be executed.
     *
     * @param argsIndex - the argument index
     * @return the IMAP command to be executed
     */
    protected abstract String getCommand(final int argsIndex);

    /**
     * Gets the IMAP command's arguments whereas each argument <b>must not</b> exceed 16384 bytes.
     *
     * @return the IMAP command's arguments
     */
    protected abstract String[] getArgs();

    /**
     * Define a <code>boolean</code> value that is included in inner response loop.
     *
     * @return A <code>boolean</code> value
     */
    protected abstract boolean addLoopCondition();

    /**
     * Gets the default value that ought to be returned if the error <code>"No matching messages"</code> occurs.
     *
     * @return The default value
     */
    protected abstract T getDefaultValue();

    /**
     * Handles the current response.
     *
     * @param response The response
     * @throws MessagingException If a message-related error occurs
     */
    protected abstract boolean handleResponse(Response response) throws MessagingException;

    /**
     * Gets the return value.
     *
     * @return The return value
     * @throws MessagingException If a message-related error occurs
     */
    protected abstract T getReturnVal() throws MessagingException;

}
