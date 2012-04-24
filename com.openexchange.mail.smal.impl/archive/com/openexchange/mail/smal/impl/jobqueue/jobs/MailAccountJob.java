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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.smal.impl.SmalMailAccess;
import com.openexchange.mail.smal.impl.index.Constants;
import com.openexchange.mail.smal.impl.jobqueue.JobQueue;

/**
 * {@link MailAccountJob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountJob extends AbstractMailSyncJob {

    private static final long serialVersionUID = -6340938661683281026L;

    private static final String SIMPLE_NAME = MailAccountJob.class.getSimpleName();

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccountJob.class));

    private final String identifier;

    private final Set<String> filter;

    /**
     * Initializes a new {@link MailAccountJob}.
     *
     * @param accountId
     * @param userId
     * @param contextId
     */
    public MailAccountJob(final int accountId, final int userId, final int contextId) {
        this(accountId, userId, contextId, Collections.<String> emptySet());
    }

    /**
     * Initializes a new {@link MailAccountJob}.
     *
     * @param accountId
     * @param userId
     * @param contextId
     * @param filterFullNames The filter full names
     */
    public MailAccountJob(final int accountId, final int userId, final int contextId, final String... filterFullNames) {
        this(accountId, userId, contextId, new HashSet<String>(Arrays.asList(filterFullNames)));
    }

    /**
     * Initializes a new {@link MailAccountJob}.
     *
     * @param accountId
     * @param userId
     * @param contextId
     * @param filter The filter full names
     */
    public MailAccountJob(final int accountId, final int userId, final int contextId, final Set<String> filter) {
        super(accountId, userId, contextId);
        identifier =
            new StringBuilder(SIMPLE_NAME).append('@').append(contextId).append('@').append(userId).append('@').append(
                accountId).toString();
        this.filter = new HashSet<String>(filter);
    }

    private List<String> getList() throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = SmalMailAccess.getUnwrappedInstance(userId, contextId, accountId);
            mailAccess.connect(true);
            final List<String> fullNames = new LinkedList<String>();
            handleSubfolders(MailFolder.DEFAULT_FOLDER_ID, mailAccess.getFolderStorage(), fullNames);
            return fullNames;
        } finally {
            SmalMailAccess.closeUnwrappedInstance(mailAccess);
        }
    }

    private void handleSubfolders(final String fullName, final IMailFolderStorage folderStorage, final List<String> fullNames) throws OXException {
        for (final MailFolder mailFolder : folderStorage.getSubfolders(fullName, true)) {
            final String subFullName = mailFolder.getFullname();
            fullNames.add(subFullName);
            handleSubfolders(subFullName, folderStorage, fullNames);
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getRanking() {
        return 1;
    }

    @Override
    public void perform() {
        try {
            final JobQueue queue = JobQueue.getInstance();
            if (null == filter || filter.isEmpty()) {
                final List<String> list = getList();
                for (final String fullName : list) {
                    addJobIfShouldSync(queue, fullName);
                }
            } else {
                final List<String> list = new ArrayList<String>(filter);
                for (final String fullName : list) {
                    addJobIfShouldSync(queue, fullName);
                }
            }
        } catch (final Exception e) {
            LOG.error("Mail account job failed.", e);
        }
    }

    private void addJobIfShouldSync(final JobQueue queue, final String fullName) {
        final FolderJob folderJob = new FolderJob(fullName, accountId, userId, contextId).setSpan(Constants.HOUR_MILLIS);
        if (queue.addJob(folderJob)) {
            LOG.debug("Folder job \"" + folderJob.toString() + "\" scheduled to job queue.");
        } else {
            LOG.debug("Folder job \"" + folderJob.toString() + "\" denied by job queue. Either due to capacity restrictions or because a similar job is already in queue.");
        }
    }

}
