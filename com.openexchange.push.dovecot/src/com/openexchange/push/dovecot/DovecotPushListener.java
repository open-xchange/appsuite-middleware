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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.push.dovecot;

import java.net.URI;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDelegator;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.SessionInfo;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.ProtocolCommand;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;


/**
 * {@link DovecotPushListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class DovecotPushListener implements PushListener, Runnable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DovecotPushListener.class);

    /** The timeout threshold; cluster lock timeout minus one minute */
    private static final long TIMEOUT_THRESHOLD_MILLIS = DovecotPushClusterLock.TIMEOUT_MILLIS - 60000L;

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final boolean permanent;
    private final Session session;
    private final ServiceLookup services;
    private final DovecotPushManagerService pushManager;
    private ScheduledTimerTask refreshTask;

    /**
     * Initializes a new {@link DovecotPushListener}.
     */
    public DovecotPushListener(Session session, boolean permanent, DovecotPushManagerService pushManager, ServiceLookup services) {
        super();
        this.pushManager = pushManager;
        this.permanent = permanent;
        this.session = session;
        this.services = services;
    }

    private boolean isUserValid() {
        try {
            ContextService contextService = services.getService(ContextService.class);
            Context context = contextService.loadContext(session.getContextId());
            if (!context.isEnabled()) {
                return false;
            }

            UserService userService = services.getService(UserService.class);
            User user = userService.getUser(session.getUserId(), context);
            return user.isMailEnabled();
        } catch (OXException e) {
            return false;
        }
    }

    @Override
    public void run() {
        try {
            if (!isUserValid()) {
                unregister(false);
                return;
            }

            pushManager.refreshLock(new SessionInfo(session, permanent));
        } catch (Exception e) {
            LOGGER.warn("Failed to refresh lock for user {} in context {}", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the permanent flag
     *
     * @return The permanent flag
     */
    public boolean isPermanent() {
        return permanent;
    }

    @Override
    public void notifyNewMail() throws OXException {
        // Do nothing as we notify on incoming push event
    }

    /**
     * Initializes registration for this listener.
     *
     * @param uri The URL end-point
     * @param authLogin The option login
     * @param authPassword The optional password
     * @throws OXException If registration fails
     */
    public synchronized void initateRegistration(final URI uri, final String authLogin, final String authPassword) throws OXException {
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (null == timerService) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            MailService mailService = services.getOptionalService(MailService.class);
            if (null == mailService) {
                // Currently no MailService available
                return;
            }

            // Connect it
            final Session session = this.session;
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            // Get IMAP store
            IMAPStore imapStore = getImapFolderStorageFrom(mailAccess).getImapStore();
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder("INBOX");

            imapFolder.doCommand(new ProtocolCommand() {

                @Override
                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                    // Craft IMAP command
                    String command;
                    {
                        StringBuilder cmdBuilder = new StringBuilder(32).append("SETMETADATA \"\" ");
                        cmdBuilder.append("(/private/vendor/vendor.dovecot/http-notify ");

                        // User
                        cmdBuilder.append("user=").append(session.getUserId()).append('@').append(session.getContextId());

                        // URL
                        cmdBuilder.append('\t').append("url=").append(uri);

                        // Auth data
                        if (!Strings.isEmpty(authLogin) && !Strings.isEmpty(authPassword)) {
                            cmdBuilder.append('\t').append("auth=basic:").append(authLogin).append(':').append(authPassword);
                        }

                        cmdBuilder.append(")");
                        command = cmdBuilder.toString();
                    }

                    // Issue command
                    Response[] r = IMAPCommandsCollection.performCommand(protocol, command);
                    Response response = r[r.length - 1];
                    if (response.isOK()) {
                        return Boolean.TRUE;
                    } else if (response.isBAD()) {
                        throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        protocol.handleResult(response);
                    }
                    return Boolean.FALSE;
                }
            });

        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            closeMailAccess(mailAccess);
            mailAccess = null;
        }

        long delay = TIMEOUT_THRESHOLD_MILLIS;
        refreshTask = timerService.scheduleAtFixedRate(this, delay, delay);
    }

    /**
     * Unregisters this listeners.
     *
     * @throws OXException If unregistration fails
     */
    public synchronized boolean unregister(boolean tryToReconnect) throws OXException {
        // Cancel timer task
        ScheduledTimerTask refreshTask = this.refreshTask;
        if (null != refreshTask) {
            this.refreshTask = null;
            refreshTask.cancel();
        }

        boolean reconnected = false;
        DovecotPushListener anotherListener = tryToReconnect ? pushManager.injectAnotherListenerFor(session) : null;
        if (null == anotherListener) {
            // No other listener available
            // Give up lock and return
            try {
                pushManager.releaseLock(new SessionInfo(session, permanent));
            } catch (Exception e) {
                LOGGER.warn("Failed to release lock for user {} in context {}.", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
            }
        } else {
            try {
                // No need to re-execute registration
                reconnected = true;
            } catch (Exception e) {
                LOGGER.warn("Failed to start new listener for user {} in context {}.", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                // Give up lock and return
                try {
                    pushManager.releaseLock(new SessionInfo(session, permanent));
                } catch (Exception x) {
                    LOGGER.warn("Failed to release DB lock for user {} in context {}.", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), x);
                }
            }
        }

        if (false == reconnected) {
            doUnregistration();
        }

        return reconnected;
    }

    private void doUnregistration() throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            MailService mailService = services.getOptionalService(MailService.class);
            if (null != mailService) {
                // Connect it
                mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
                mailAccess.connect(false);

                // Get IMAP store
                IMAPStore imapStore = getImapFolderStorageFrom(mailAccess).getImapStore();
                final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder("INBOX");

                imapFolder.doCommand(new ProtocolCommand() {

                    @Override
                    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                        // Craft IMAP command
                        String command = "SETMETADATA \"\" (/private/vendor/vendor.dovecot/http-notify NIL)";

                        // Issue command
                        Response[] r = IMAPCommandsCollection.performCommand(protocol, command);
                        Response response = r[r.length - 1];
                        if (response.isOK()) {
                            return Boolean.TRUE;
                        } else if (response.isBAD()) {
                            throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                        } else if (response.isNO()) {
                            throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                        } else {
                            protocol.handleResult(response);
                        }
                        return Boolean.FALSE;
                    }
                });
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            closeMailAccess(mailAccess);
            mailAccess = null;
        }
    }

    private static void closeMailAccess(final MailAccess<?, ?> mailAccess) {
        if (null != mailAccess) {
            try {
                mailAccess.close(false);
            } catch (final Exception x) {
                // Ignore
            }
        }
    }

    private static IMAPFolderStorage getImapFolderStorageFrom(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        IMailFolderStorage fstore = mailAccess.getFolderStorage();
        if (!(fstore instanceof IMAPFolderStorage)) {
            if (!(fstore instanceof IMailFolderStorageDelegator)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation: " + fstore.getClass().getName());
            }
            fstore = ((IMailFolderStorageDelegator) fstore).getDelegateFolderStorage();
            if (!(fstore instanceof IMAPFolderStorage)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation: " + fstore.getClass().getName());
            }
        }
        return (IMAPFolderStorage) fstore;
    }

}
