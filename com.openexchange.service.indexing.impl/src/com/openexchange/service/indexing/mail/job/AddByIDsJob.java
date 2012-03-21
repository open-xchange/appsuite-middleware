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

import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.solr.mail.SolrMailUtility;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.session.Session;

/**
 * {@link AddByIDsJob} - Adds mails to index by specified identifiers.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AddByIDsJob extends AbstractMailJob {

    private static final long serialVersionUID = 3310275939748782858L;

    private static final String SIMPLE_NAME = AddByIDsJob.class.getSimpleName();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AddByIDsJob.class));

    private final InsertType insertType;

    private final String fullName;

    private volatile List<String> mailIds;

    /**
     * Initializes a new {@link AddByIDsJob}.
     * 
     * @param fullName The folder full name
     * @param info The job information needed for being performed
     */
    public AddByIDsJob(final String fullName, final MailJobInfo info) {
        this(fullName, info, null);
    }

    /**
     * Initializes a new {@link AddByIDsJob}.
     * 
     * @param fullName The folder full name
     * @param info The job information needed for being performed
     * @param insertType The insert type; {@link InsertType#ATTACHMENTS} is default
     */
    public AddByIDsJob(final String fullName, final MailJobInfo info, final InsertType insertType) {
        super(info);
        this.fullName = fullName;
        this.insertType = null == insertType ? InsertType.ATTACHMENTS : insertType;
    }

    /**
     * Sets the mails identifiers
     * 
     * @param mailIds The identifiers to set
     * @return This folder job
     */
    public AddByIDsJob setMailIds(final List<String> mailIds) {
        this.mailIds = mailIds;
        return this;
    }

    @Override
    public void performJob() throws OXException, InterruptedException {
        final List<String> mailIds = this.mailIds;
        if (null == mailIds) {
            return;
        }
        IndexAccess<MailMessage> indexAccess = null;
        try {
            /*
             * Check flags of contained mails
             */
            indexAccess = getIndexAccess();
            final List<MailMessage> mails;
            final Session session;
            {
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                try {
                    mailAccess = mailAccessFor();
                    session = mailAccess.getSession();
                    /*
                     * Get the mails from mail storage
                     */
                    mailAccess.connect(false);
                    /*
                     * Fetch mails
                     */
                    final MailFields fields = new MailFields(SolrMailUtility.getIndexableFields());
                    fields.removeMailField(MailField.BODY);
                    fields.removeMailField(MailField.FULL);
                    mails =
                        Arrays.asList(mailAccess.getMessageStorage().getMessages(
                            fullName,
                            mailIds.toArray(new String[mailIds.size()]),
                            fields.toArray()));
                } finally {
                    getSmalAccessService().closeUnwrappedInstance(mailAccess);
                    mailAccess = null;
                }
            }
            /*
             * Add them to index
             */
            final List<IndexDocument<MailMessage>> documents = toDocuments(mails);
            try {
                switch (insertType) {
                case ENVELOPE:
                    indexAccess.addEnvelopeData(documents);
                    break;
                case BODY:
                    indexAccess.addContent(documents);
                    break;
                default:
                    indexAccess.addAttachments(documents);
                    break;
                }
            } catch (final OXException e) {
                // Batch add failed; retry one-by-one
                for (final IndexDocument<MailMessage> document : documents) {
                    try {
                        indexAccess.addAttachments(document);
                    } catch (final Exception inner) {
                        final MailMessage mail = document.getObject();
                        LOG.warn(
                            "Mail " + mail.getMailId() + " from folder " + mail.getFolder() + " of account " + accountId + " could not be added to index.",
                            inner);
                    }
                }
            }
        } catch (final RuntimeException e) {
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
        } finally {
            releaseAccess(indexAccess);
        }
    }

}
