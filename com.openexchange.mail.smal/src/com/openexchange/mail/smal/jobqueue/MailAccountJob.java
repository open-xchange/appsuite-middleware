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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.jobqueue;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.adapter.IndexAdapter;


/**
 * {@link MailAccountJob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountJob extends Job {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(MailAccountJob.class));

    private final int contextId;
    
    private final int userId;
    
    private final int accountId;

    private final Queue<String> folders;

    private volatile boolean initialized;

    private volatile boolean error;

    /**
     * Initializes a new {@link MailAccountJob}.
     * @param accountId
     * @param userId
     * @param contextId
     */
    public MailAccountJob(final int accountId, final int userId, final int contextId) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.contextId = contextId;
        folders = new ConcurrentLinkedQueue<String>();
    }

    private void init() throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = MailAccess.getInstance(userId, contextId, accountId);
        mailAccess.connect(true);
        try {
            final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            handleSubfolders(MailFolder.DEFAULT_FOLDER_ID, folderStorage);
        } finally {
            mailAccess.close(true);
        }
        initialized = true;
    }

    private void handleSubfolders(final String fullName, final IMailFolderStorage folderStorage) throws OXException {
        for (final MailFolder mailFolder : folderStorage.getSubfolders(fullName, true)) {
            final String subFullName = mailFolder.getFullname();
            folders.offer(subFullName);
            handleSubfolders(subFullName, folderStorage);
        }
    }

    @Override
    public int getRanking() {
        return 0;
    }

    private static final MailField[] FIELDS = new MailField[] { MailField.ID };

    @Override
    public void perform() {
        if (error) {
            cancel();
            return;
        }
        try {
            if (!initialized) {
                init();
            }
            /*
             * Process full names in queue
             */
            while (!folders.isEmpty()) {
                final String fullName = folders.poll();
                if (null != fullName) {
                    final IndexAdapter indexAdapter = null; // TODO:
                    final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = MailAccess.getInstance(userId, contextId, accountId);
                    mailAccess.connect(true);
                    try {
                        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                        final MailMessage[] mails = messageStorage.searchMessages(fullName, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS);
                        for (final MailMessage mail : mails) {
                            final MailMessage fullMail = messageStorage.getMessage(fullName, mail.getMailId(), false);
                            indexAdapter.add(fullMail, mailAccess.getSession());
                        }
                        
                    } finally {
                        mailAccess.close(true);
                    }
                    /*
                     * Re-enqueue for next run
                     */
                    final BlockingQueue<Job> queue = getQueue();
                    if (null == queue || !queue.offer(this)) {
                        LOG.error("Re-enqueueing mail account job failed.");
                        cancel();
                    }
                    return;
                }
            }
        } catch (final Exception e) {
            error = true;
            cancel();
            LOG.error("Mail account job failed.", e);
        }
    }

}
