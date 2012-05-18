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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.mail.Constants;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ElapsedFolderJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElapsedFolderJob extends AbstractMailJob {

    private static final long serialVersionUID = 7241539353635873191L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ElapsedFolderJob.class));

    private static final String SIMPLE_NAME = ElapsedFolderJob.class.getSimpleName();

    private volatile Thread runner;

    private final long start;

    private final String identifier;

    /**
     * Initializes a new {@link ElapsedFolderJob}.
     * 
     * @param info The job information
     */
    public ElapsedFolderJob(final MailJobInfo info, final long start) {
        super(info);
        this.start = start;
        identifier =
            new StringBuilder(SIMPLE_NAME).append('@').append(contextId).append('@').append(userId).append('@').append(accountId).toString();
    }

    /**
     * Gets the identifier
     * 
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Cancels this job.
     */
    public void cancel() {
        final Thread runner = this.runner;
        if (runner != null) {
            try {
                runner.interrupt();
            } catch (final Exception e) {
                // Ignore
            }
        }
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
    protected void performMailJob() throws OXException, InterruptedException {
        runner = Thread.currentThread();
        try {
            final List<String> exceededFolders = getElapsedFolders(System.currentTimeMillis());
            if (exceededFolders.isEmpty()) {
                return;
            }
            final IndexingService indexingService = Services.getService(IndexingService.class);
            for (final String fullName : exceededFolders) {
                final FolderJob folderJob = new FolderJob(fullName, info).setSpan(Constants.HOUR_MILLIS);
                indexingService.addJob(folderJob);
            }
        } catch (final RuntimeException e) {
            LOG.error(SIMPLE_NAME + " \"" + info + "\" failed.", e);
        } finally {
            runner = null;
        }
    }

    private List<String> getElapsedFolders(final long now) throws OXException {
        final DatabaseService databaseService = Services.optService(DatabaseService.class);
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
