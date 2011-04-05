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

package com.openexchange.push.imapidle;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPAccess;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.push.PushException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidle.services.ImapIdleServiceRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link ImapIdlePushListener} - The IMAP IDLE {@link PushListener}.
 * 
 */
public final class ImapIdlePushListener implements PushListener {

    
    /**
     * @param debugEnabled the debugEnabled to set
     */
    public static final void setDebugEnabled(boolean debugEnabled) {
        DEBUG_ENABLED = debugEnabled;
    }

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ImapIdlePushListener.class);

    private static boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    /**
     * A placeholder constant for account ID.
     */
    private static final int ACCOUNT_ID = 0;

    private static volatile String folder;
    
    private static int errordelay;

    /**
     * Gets the account ID constant.
     * 
     * @return The account ID constant
     */
    public static int getAccountId() {
        return ACCOUNT_ID;
    }

    /**
     * Sets static folder fullname.
     * 
     * @param folder The folder fullname
     */
    public static void setFolder(final String folder) {
        ImapIdlePushListener.folder = folder;
    }

    /**
     * Gets static folder fullname.
     * 
     * @return The folder fullname
     */
    public static String getFolder() {
        return folder;
    }

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     * 
     * @param session The needed session to obtain and connect mail access instance
     * @param startTimerTask <code>true</code> to start a timer task for this listener
     * @return A new {@link ImapIdlePushListener}.
     */
    public static ImapIdlePushListener newInstance(final Session session) {
        return new ImapIdlePushListener(session);
    }

    /*
     * Member section
     */

    private final AtomicBoolean running;

    private final Session session;

    private final int userId;

    private final int contextId;

    private volatile Future<Object> imapIdleFuture;

    private MailAccess<?, ?> mailAccess;

    private MailService mailService;
    
    /**
     * Initializes a new {@link ImapIdlePushListener}.
     * 
     * @param session The needed session to obtain and connect mail access instance
     */
    private ImapIdlePushListener(final Session session) {
        super();
        running = new AtomicBoolean();
        this.session = session;
        userId = session.getUserId();
        contextId = session.getContextId();
        mailAccess = null;
        mailService = null;
        errordelay = 1000;
    }

    
    /**
     * @return the errordelay
     */
    public static final int getErrordelay() {
        return errordelay;
    }

    
    /**
     * @param errordelay the errordelay to set
     */
    public static final void setErrordelay(int errordelay) {
        ImapIdlePushListener.errordelay = errordelay;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128).append("session-ID=").append(session.getSessionID());
        sb.append(", user=").append(userId).append(", context=").append(contextId);
        sb.append(", imapIdleFuture=").append(imapIdleFuture);
        return sb.toString();
    }

    /**
     * Opens this listener
     * 
     * @throws PushException If listener cannot be opened
     */
    public void open() throws PushException {
        final ThreadPoolService threadPoolService;
        try {
            threadPoolService = ImapIdleServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class, true);
            IMailProperties imcf = IMAPAccess.getInstance(session).getMailConfig().getMailProperties();
            if( imcf.isWatcherEnabled() ) {
                LOG.error("com.openexchange.mail.watcherEnabled is enabled, please disable it!");
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("com.openexchange.mail.watcherEnabled is enabled, please disable it!");
            }
            mailService = ImapIdleServiceRegistry.getServiceRegistry().getService(MailService.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        } catch (MailException e) {
            throw new PushException(e);
        }
        imapIdleFuture = threadPoolService.submit(ThreadPools.task(new ImapIdlePushListenerTask(this)));
    }

    /**
     * Closes this listener.
     */
    public void close() {
        if( null != imapIdleFuture ) {
            imapIdleFuture.cancel(true);
            imapIdleFuture = null;
        }
    }

    /**
     * Check for new mails
     * 
     * @throws PushException If check for new mails fails
     */
    public void checkNewMail() throws PushException {
        if (!running.compareAndSet(false, true)) {
            /*
             * Still in process...
             */
            if (DEBUG_ENABLED) {
                LOG.info(new StringBuilder(64).append("Listener still in process for user ").append(userId).append(" in context ").append(
                    contextId).append(". Return immediately.").toString());
            }
            return;
        }
        try {
            mailAccess = mailService.getMailAccess(session, ACCOUNT_ID);
            mailAccess.connect();
            Object fstore = mailAccess.getFolderStorage();
            if( ! (fstore instanceof IMAPFolderStorage) ) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation");
            }
            IMAPFolderStorage istore = (IMAPFolderStorage) fstore;
            IMAPStore imapStore = istore.getImapStore();
            IMAPFolder inbox = (IMAPFolder) imapStore.getFolder(folder);
            if( ! inbox.isOpen() ) {
                inbox.open(IMAPFolder.READ_WRITE);
            }
            if( DEBUG_ENABLED ) {
                LOG.info("starting IDLE for Context: " + session.getContextId() + ", Login: " + session.getLoginName());
            }
            inbox.idle(true);
            if( inbox.getNewMessageCount() > 0 ) {
                if( DEBUG_ENABLED ) {
                    int nmails = inbox.getNewMessageCount();
                    LOG.info("IDLE: " + nmails + " new mail(s) for Context: " + session.getContextId() + ", Login: " + session.getLoginName());
                }
                notifyNewMail();
            }
            /* NOTE: we cannot throw Exceptions because that would stop the IDLE'ing when e.g.
             * IMAP server is down/busy for a moment or if e.g. cyrus client timeout happens
             * (idling for too long)
             */
        } catch (MailException e) {
            // throw new PushException(e);
            LOG.error("ERROR in IDLE'ing: " + e.getMessage() + ", sleeping for " + errordelay + "ms");
            try {
                Thread.sleep(errordelay);
            } catch (InterruptedException e1) {
                LOG.error("ERROR in IDLE'ing: " + e.getMessage(), e);
            }
        } catch (MessagingException e) {
            LOG.error("ERROR in IDLE'ing: " + e.getMessage() + ", sleeping for " + errordelay + "ms");
            try {
                Thread.sleep(errordelay);
            } catch (InterruptedException e1) {
                LOG.error("ERROR in IDLE'ing: " + e.getMessage(), e);
            }
        } finally {
            if( null != mailAccess ) {
                mailAccess.close(true);
            }
            running.set(false);
        }
    }

    public void notifyNewMail() throws PushException {
        PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(ACCOUNT_ID, folder), session);
    }

}
