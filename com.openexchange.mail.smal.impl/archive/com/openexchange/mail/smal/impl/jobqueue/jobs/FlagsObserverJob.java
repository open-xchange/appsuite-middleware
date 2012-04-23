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

package com.openexchange.mail.smal.impl.jobqueue.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.adapter.IndexAdapter;
import com.openexchange.mail.smal.impl.jobqueue.Job;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link FlagsObserverJob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FlagsObserverJob extends AbstractMailSyncJob {

    private static final long serialVersionUID = -7281521171077091128L;

    private static final String SIMPLE_NAME = FlagsObserverJob.class.getSimpleName();

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FlagsObserverJob.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final int GATE_PERFORM = -1;

    private static final int GATE_OPEN = 0;

    private static final int GATE_REPLACE = 1;

    private final String fullName;

    private final String identifier;

    private final AtomicInteger gate;

    private volatile int ranking;

    private volatile boolean error;

    private volatile List<MailMessage> storageMails;

    /**
     * Initializes a new {@link FlagsObserverJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     *
     * @param fullName The folder full name
     * @param accountId The account ID
     * @param userId The user ID
     * @param contextId The context ID
     */
    public FlagsObserverJob(final String fullName, final int accountId, final int userId, final int contextId) {
        super(accountId, userId, contextId);
        gate = new AtomicInteger(0);
        ranking = 0;
        this.fullName = fullName;
        // A unique name to ensure not filtered
        identifier =
            new StringBuilder(SIMPLE_NAME).append('@').append(contextId).append('@').append(userId).append('@').append(accountId).append(
                '@').append(fullName).append('@').append(UUID.randomUUID().toString()).toString();
    }

    /**
     * Sets the ranking
     *
     * @param ranking The ranking to set
     * @return This folder job with specified ranking applied
     */
    public FlagsObserverJob setRanking(final int ranking) {
        this.ranking = ranking;
        return this;
    }

    /**
     * Sets the storage mails
     *
     * @param storageMails The storage mails to set
     * @return This folder job with specified storage mails applied
     */
    public FlagsObserverJob setStorageMails(final List<MailMessage> storageMails) {
        this.storageMails = storageMails;
        return this;
    }

    @Override
    public void replaceWith(final Job anotherJob) {
        if (!identifier.equals(anotherJob.getIdentifier())) {
            return;
        }
        int state;
        do {
            state = gate.get();
            if (GATE_PERFORM == state) {
                // Already performed
                return;
            }
        } while (state != GATE_OPEN || !gate.compareAndSet(state, GATE_REPLACE));
        final FlagsObserverJob anotherFolderJob = (FlagsObserverJob) anotherJob;
        this.ranking = anotherFolderJob.ranking;
        this.storageMails = anotherFolderJob.storageMails;
        gate.set(0);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getRanking() {
        return ranking;
    }

    private static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS };

    @Override
    public void perform() {
        final List<MailMessage> storageMails = this.storageMails;
        if (null == storageMails) {
            return;
        }
        if (error) {
            cancel();
            return;
        }
        int state;
        do {
            state = gate.get();
            if (GATE_PERFORM == state) {
                // Already performed?
                return;
            }
        } while (state != GATE_OPEN || !gate.compareAndSet(state, GATE_PERFORM));
        try {
            /*
             * Check flags of contained mails
             */
            final long st = DEBUG ? System.currentTimeMillis() : 0L;
            try {
                final IndexAdapter indexAdapter = getAdapter();
                final Session session =
                    SmalServiceLookup.getServiceStatic(SessiondService.class).getAnyActiveSessionForUser(userId, contextId);
                /*
                 * Get the mails from index
                 */
                final Map<String, MailMessage> storageMap = new HashMap<String, MailMessage>(storageMails.size());
                final String[] mailIds = new String[storageMails.size()];
                {
                    int i = 0;
                    for (final MailMessage m : storageMails) {
                        final String mailId = m.getMailId();
                        mailIds[i++] = mailId;
                        storageMap.put(mailId, m);
                    }
                }
                final List<MailMessage> indexedMails = indexAdapter.getMessages(mailIds, fullName, null, null, FIELDS, accountId, session);
                final Map<String, MailMessage> indexMap;
                if (indexedMails.isEmpty()) {
                    indexMap = Collections.emptyMap();
                } else {
                    indexMap = new HashMap<String, MailMessage>(indexedMails.size());
                    for (final MailMessage mailMessage : indexedMails) {
                        indexMap.put(mailMessage.getMailId(), mailMessage);
                    }
                }
                /*
                 * Changed ones
                 */
                Set<String> changedIds = new HashSet<String>(indexMap.keySet());
                List<MailMessage> changedMails = new ArrayList<MailMessage>(changedIds.size());
                for (final Iterator<String> iterator = changedIds.iterator(); iterator.hasNext();) {
                    final String mailId = iterator.next();
                    final MailMessage storageMail = storageMap.get(mailId);
                    final MailMessage indexMail = indexMap.get(mailId);
                    boolean different = false;
                    if (storageMail.getFlags() != indexMail.getFlags()) {
                        storageMail.setAccountId(accountId);
                        storageMail.setFolder(fullName);
                        storageMail.setMailId(mailId);
                        changedMails.add(storageMail);
                        different = true;
                    }
                    if (storageMail.getFlags() != indexMail.getFlags()) {
                        storageMail.setAccountId(accountId);
                        storageMail.setFolder(fullName);
                        storageMail.setMailId(mailId);
                        changedMails.add(storageMail);
                        different = true;
                    }
                    if (different) {
                        iterator.remove();
                    }
                }
                changedIds = null;
                /*
                 * Change flags
                 */
                indexAdapter.change(changedMails, session);
                changedMails = null;
            } finally {
                if (DEBUG) {
                    final long dur = System.currentTimeMillis() - st;
                    LOG.debug("FlagsObserverJob \"" + identifier + "\" took " + dur + "msec for folder " + fullName + " in account " + accountId);
                }
            }
        } catch (final Exception e) {
            error = true;
            cancel();
            LOG.error("FlagsObserverJob \"" + identifier + "\" failed.", e);
        }
    }

}
