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

package com.openexchange.service.indexing.mail.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.service.indexing.mail.FakeSession;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.session.Session;

/**
 * {@link FlagsObserverJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FlagsObserverJob extends AbstractMailJob {

    private static final long serialVersionUID = -7281521171077091128L;

    private static final String SIMPLE_NAME = FlagsObserverJob.class.getSimpleName();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FlagsObserverJob.class));

    private final String fullName;

    private volatile List<MailMessage> storageMails;

    /**
     * Initializes a new {@link FlagsObserverJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     * 
     * @param fullName The folder full name
     * @param info The information
     */
    public FlagsObserverJob(final String fullName, final MailJobInfo info) {
        super(info);
        this.fullName = fullName;
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

    private static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS };

    @Override
    public void performJob() throws OXException, InterruptedException {
        final List<MailMessage> storageMails = this.storageMails;
        if (null == storageMails) {
            return;
        }
        try {
            /*
             * Check flags of contained mails
             */
            final IndexAdapter indexAdapter = getAdapter();
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
            final Session session = new FakeSession(info.primaryPassword, userId, contextId);
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
        } catch (final Exception e) {
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
        }
    }

}
