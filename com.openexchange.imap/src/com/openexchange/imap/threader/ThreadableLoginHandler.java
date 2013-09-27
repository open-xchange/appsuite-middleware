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

package com.openexchange.imap.threader;

import gnu.trove.set.hash.TLongHashSet;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPAccess;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPProtocol;
import com.openexchange.imap.threader.ThreadableCache.ThreadableCacheEntry;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link ThreadableLoginHandler} - The {@link LoginHandlerService login handler} obtaining <tt>Threadable</tt> for sent folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadableLoginHandler implements LoginHandlerService, NonTransient {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ThreadableLoginHandler}.
     *
     * @param services The service look-up
     */
    public ThreadableLoginHandler(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void handleLogout(final LoginResult logout) throws OXException {
        // Nothing to do
    }

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        if (!ThreadableCache.isThreadableCacheEnabled()) {
            return;
        }
        final ServiceLookup services = this.services;
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    final MailAccountStorageService storageService = services.getService(MailAccountStorageService.class);
                    final Session session = login.getSession();
                    final MailAccount[] accounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                    for (final MailAccount account : accounts) {
                        if (account.getMailProtocol().equals(IMAPProtocol.getInstance().getName())) {
                            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                            try {
                                mailAccess = MailAccess.getInstance(session, account.getId());
                                mailAccess.connect(true);
                                final String sentFolder = mailAccess.getFolderStorage().getSentFolder();
                                final ThreadableCacheEntry entry =
                                    ThreadableCache.getInstance().getEntry(sentFolder, account.getId(), session);
                                synchronized (entry) {
                                    if (null == entry.getThreadable()) {
                                        final IMAPFolder sent = (IMAPFolder) ((IMAPAccess) mailAccess).getIMAPStore().getFolder(sentFolder);
                                        sent.open(Folder.READ_ONLY);
                                        try {
                                            final Threadable threadable = Threadables.getAllThreadablesFrom(sent, -1);
                                            entry.set(new TLongHashSet(IMAPCommandsCollection.getUIDCollection(sent)), threadable, false);
                                        } finally {
                                            sent.close(false);
                                        }
                                    }
                                }
                            } catch (final MessagingException e) {
                                throw MimeMailException.handleMessagingException(
                                    e,
                                    null == mailAccess ? null : mailAccess.getMailConfig(),
                                    session);
                            } finally {
                                if (null != mailAccess) {
                                    mailAccess.close(true);
                                }
                            }
                        }
                    }
                } catch (final OXException e) {
                    // Ignore
                }
            } // End of run()
        };
        ThreadPools.getThreadPool().submit(ThreadPools.task(r));
    }
}
