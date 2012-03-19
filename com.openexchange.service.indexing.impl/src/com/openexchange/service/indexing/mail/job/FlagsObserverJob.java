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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.service.indexing.mail.MailJobInfo;

/**
 * {@link FlagsObserverJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FlagsObserverJob extends AbstractMailJob {

    private static final long serialVersionUID = 2196500619109207972L;

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
        IndexAccess<MailMessage> indexAccess = null;
        try {
            /*
             * Check flags of contained mails
             */
            indexAccess = getIndexAccess();
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
            final int length = mailIds.length;
            int off = 0;
            final StringBuilder queryBuilder = new StringBuilder(128);
            queryBuilder.append('(').append(FIELD_USER).append(':').append(userId).append(')');
            queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(contextId).append(')');
            queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
            queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
            final int resLen = queryBuilder.length();
            final int maxRowx = GET_ROWS;
            final String fieldId = FIELD_ID;
            final Map<String, MailMessage> indexMap = new HashMap<String, MailMessage>(length);
            while (off < length) {
                int endIndex = off + maxRowx;
                if (endIndex >= length) {
                    endIndex = length;
                }
                final int len = endIndex - off;
                queryBuilder.setLength(resLen);
                queryBuilder.append(" AND (").append(fieldId).append(':').append(mailIds[off]);
                for (int i = off + 1; i < len; i++) {
                    queryBuilder.append(" OR ").append(fieldId).append(':').append(mailIds[i]);
                }
                queryBuilder.append(')');
                final Map<String, Object> params = new HashMap<String, Object>(4);
                // TODO: params.put("fields", FIELDS);
                params.put("sort", FIELD_RECEIVED_DATE);
                params.put("order", "desc");
                final QueryParameters queryParameter =
                    new QueryParameters.Builder(queryBuilder.toString()).setOffset(0).setLength(len).setType(
                        IndexDocument.Type.MAIL).setParameters(params).build();
                final IndexResult<MailMessage> indexResult = indexAccess.query(queryParameter);
                if (0 < indexResult.getNumFound()) {
                    for (final IndexDocument<MailMessage> indexDocument : indexResult.getResults()) {
                        final MailMessage mailMessage = indexDocument.getObject();
                        indexMap.put(mailMessage.getMailId(), mailMessage);
                    }
                }
                off = endIndex;
            }
            /*
             * Changed ones
             */
            Set<String> changedIds = new HashSet<String>(indexMap.keySet());
            List<MailMessage> changedMails = new ArrayList<MailMessage>(changedIds.size());
            for (final String mailId : changedIds) {
                final MailMessage storageMail = storageMap.get(mailId);
                final MailMessage indexMail = indexMap.get(mailId);
                if (FolderJob.isDifferent(storageMail, indexMail)) {
                    storageMail.setAccountId(accountId);
                    storageMail.setFolder(fullName);
                    storageMail.setMailId(mailId);
                    changedMails.add(storageMail);
                }
            }
            changedIds = null;
            /*
             * Change flags
             */
            indexAccess.change(toDocuments(changedMails), IndexAccess.ALL_FIELDS);
            changedMails = null;
        } catch (final Exception e) {
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
        } finally {
            releaseAccess(indexAccess);
        }
    }

}
