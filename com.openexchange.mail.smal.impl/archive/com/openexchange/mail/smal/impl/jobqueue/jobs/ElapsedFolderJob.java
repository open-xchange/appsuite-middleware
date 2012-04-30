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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.index.Constants;
import com.openexchange.mail.smal.impl.jobqueue.JobQueue;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ElapsedFolderJob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElapsedFolderJob extends AbstractMailSyncJob {

    private static final long serialVersionUID = -3561304349504231252L;

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ElapsedFolderJob.class));

    private static final String SIMPLE_NAME = ElapsedFolderJob.class.getSimpleName();

    private final long start;

    private final String identifier;

    /**
     * Initializes a new {@link ElapsedFolderJob}.
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public ElapsedFolderJob(final int accountId, final int userId, final int contextId, final long start) {
        super(accountId, userId, contextId);
        this.start = start;
        identifier =
            new StringBuilder(SIMPLE_NAME).append('@').append(contextId).append('@').append(userId).append('@').append(accountId).toString();
    }

    /**
     * Checks if this job may already start.
     *
     * @param now The current time millis
     * @return <code>true</code> if job may start; otherwise <code>false</code>
     */
    public boolean mayStart(final long now) {
        return now >= start;
    }

    @Override
    public int getRanking() {
        return -1;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void perform() {
        if (canceled) {
            return;
        }
        try {
            final List<String> exceededFolders = getElapsedFolders(System.currentTimeMillis());
            if (exceededFolders.isEmpty()) {
                return;
            }
            final JobQueue queue = JobQueue.getInstance();
            for (final String fullName : exceededFolders) {
                final FolderJob folderJob = new FolderJob(fullName, accountId, userId, contextId).setSpan(Constants.HOUR_MILLIS);
                if (queue.addJob(folderJob)) {
                    LOG.debug("Folder job \"" + folderJob.toString() + "\" scheduled to job queue.");
                } else {
                    LOG.debug("Folder job \"" + folderJob.toString() + "\" denied by job queue. Either due to capacity restrictions or because a similar job is already in queue.");
                }
            }
        } catch (final Exception e) {
            cancel();
            LOG.error("Elapsed folder job failed.", e);
        }
    }

    private List<String> getElapsedFolders(final long now) throws OXException {
        final DatabaseService databaseService = SmalServiceLookup.getServiceStatic(DatabaseService.class);
        if (null == databaseService) {
            return java.util.Collections.emptyList();
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fullName FROM mailSync WHERE cid = ? AND user = ? AND accountId = ? AND timestamp < ?");
            int pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, userId);
            stmt.setLong(pos++, accountId);
            stmt.setLong(pos, now - Constants.HOUR_MILLIS); // Grab all entries since that time stamp
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return java.util.Collections.emptyList();
            }
            final List<String> list = new LinkedList<String>();
            do {
                list.add(rs.getString(1));
            } while (rs.next());
            return list;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backWritable(contextId, con);
        }
    }

}
