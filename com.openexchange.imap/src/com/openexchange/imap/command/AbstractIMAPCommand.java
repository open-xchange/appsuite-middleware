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

package com.openexchange.imap.command;

import static com.openexchange.imap.IMAPCommandsCollection.performCommand;
import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.util.Optional;
import javax.mail.MessagingException;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotaExceededException;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInterceptor;
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

    protected static final String[] ARGS_FIRST = { "1" };

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
    protected AbstractIMAPCommand(IMAPFolder imapFolder) {
        super();
        this.imapFolder = imapFolder;
        this.protocolCommand = new CallbackIMAPProtocolCommand(this, imapFolder);
    }

    private static final class IMAPCommandResponseInterceptor implements ResponseInterceptor {
        
        private final AbstractIMAPCommand<?> abstractIMAPCommand;
        private MessagingException exception;

        IMAPCommandResponseInterceptor(AbstractIMAPCommand<?> abstractIMAPCommand) {
            super();
            this.abstractIMAPCommand = abstractIMAPCommand;
        }

        @Override
        public boolean intercept(Response response) {
            if (exception == null) {                
                try {
                    return abstractIMAPCommand.addLoopCondition() ? abstractIMAPCommand.handleResponse(response) : false;
                } catch (MessagingException e) {
                    exception = e;
                }
            }
            return false;
        }

        MessagingException getException() {
            return exception;
        }
    }

    private static final class CallbackIMAPProtocolCommand implements IMAPFolder.ProtocolCommand {

        private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CallbackIMAPProtocolCommand.class);

        private final AbstractIMAPCommand<?> abstractIMAPCommand;
        private final IMAPFolder imapFolder;

        protected CallbackIMAPProtocolCommand(AbstractIMAPCommand<?> abstractIMAPCommand, IMAPFolder imapFolder) {
            super();
            this.abstractIMAPCommand = abstractIMAPCommand;
            this.imapFolder = imapFolder;
        }

        @Override
        public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
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
                String imapCmd = abstractIMAPCommand.getCommand(argsIndex);
                // Use interceptor for FETCH commands
                IMAPCommandResponseInterceptor nullableInterceptor = imapCmd.indexOf("FETCH ") >= 0 ? new IMAPCommandResponseInterceptor(abstractIMAPCommand) : null;
                r = performCommand(protocol, imapCmd, null, Optional.ofNullable(nullableInterceptor), false);
                response = r[r.length - 1];
                if (response.isOK()) {
                    try {
                        if (nullableInterceptor != null && nullableInterceptor.getException() != null) {
                            throw nullableInterceptor.getException();
                        }
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
                        notifyResponseHandlersSafe(r, protocol);
                    } catch (MessagingException e) {
                        final ProtocolException pe = new ProtocolException(e.getMessage());
                        pe.initCause(e);
                        throw pe;
                    }
                } else if (response.isBAD()) {
                    notifyResponseHandlersSafe(r, protocol);
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return abstractIMAPCommand.getDefaultValue();
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(imapCmd));
                    if (null != imapFolder) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_FULL_NAME, imapFolder.getFullName());
                    }
                    throw new BadCommandException(response);
                } else if (response.isNO()) {
                    notifyResponseHandlersSafe(r, protocol);
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
                    if (null != imapFolder) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_FULL_NAME, imapFolder.getFullName());
                    }
                    throw new CommandFailedException(response);
                } else {
                    notifyResponseHandlersSafe(r, protocol);
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(imapCmd));
                    protocol.handleResult(response);
                }
            }
            try {
                return abstractIMAPCommand.getReturnVal();
            } catch (MessagingException e) {
                final ProtocolException pe = new ProtocolException(e.getMessage());
                pe.initCause(e);
                throw pe;
            }
        }

        private void notifyResponseHandlersSafe(Response[] r, IMAPProtocol protocol) {
            try {
                protocol.notifyResponseHandlers(r);
            } catch (RuntimeException e) {
                // Ignore runtime error in final Protocol.notifyResponseHandlers() invocation
                LOG.debug("Runtime error during Protocol.notifyResponseHandlers() invocation.", e);
            }
        }
    }

    /**
     * Performs this IMAP command.
     *
     * @return The return value
     * @throws MessagingException If IMAP command fails
     */
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
    @SuppressWarnings("unused")
    protected String getDebugInfo(int argsIndex) {
        return null;
    }

    /**
     * Gets the IMAP command to be executed.
     *
     * @param argsIndex - the argument index
     * @return the IMAP command to be executed
     */
    protected abstract String getCommand(int argsIndex);

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
