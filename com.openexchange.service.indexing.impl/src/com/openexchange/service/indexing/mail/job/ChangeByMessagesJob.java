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

import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.service.indexing.mail.StorageAccess;

/**
 * {@link ChangeByMessagesJob} - Changes the flags of specified mails in index.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ChangeByMessagesJob extends AbstractMailJob {

    private static final long serialVersionUID = 5042857752239105612L;

    private static final String SIMPLE_NAME = ChangeByMessagesJob.class.getSimpleName();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ChangeByMessagesJob.class));

    private final String fullName;

    private volatile List<MailMessage> messages;

    /**
     * Initializes a new {@link ChangeByMessagesJob}.
     * 
     * @param fullName The folder full name
     * @param info The job information
     */
    public ChangeByMessagesJob(final String fullName, final MailJobInfo info) {
        super(info);
        this.fullName = fullName;
    }

    /**
     * Sets the messages providing identifiers and flags (incl. color labels)
     * 
     * @param messages The messages
     * @return This folder job
     */
    public ChangeByMessagesJob setMailIds(final List<MailMessage> messages) {
        this.messages = messages;
        return this;
    }

    @Override
    protected void performMailJob() throws OXException, InterruptedException {
        List<MailMessage> messages = this.messages;
        if (null == messages) {
            try {
                /*
                 * Fetch mails
                 */
                messages = storageAccess.allMailsFromStorage(fullName);
            } finally {
                storageAccess.releaseAccess();
            }
        }
        if (null == messages || messages.isEmpty()) {
            return;
        }
        try {
            /*
             * Check flags of contained mails
             */
            final IndexAccess<MailMessage> indexAccess = storageAccess.getIndexAccess();
            for (final MailMessage message : messages) {
                message.setAccountId(accountId);
                message.setFolder(fullName);
            }
            /*
             * Change flags
             */
            indexAccess.change(toDocuments(messages), MailIndexField.getFor(StorageAccess.FIELDS));
        } catch (final RuntimeException e) {
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
        }
    }

}
