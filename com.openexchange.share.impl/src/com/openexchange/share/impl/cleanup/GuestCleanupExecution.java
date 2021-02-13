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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.share.impl.cleanup;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.AbstractCleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;

/**
 * {@link GuestCleanupExecution}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class GuestCleanupExecution extends AbstractCleanUpExecution {

    private static final Logger LOG = LoggerFactory.getLogger(GuestCleanupExecution.class);

    private final ServiceLookup services;
    private final long guestExpiry;

    /**
     * Initializes a new {@link GuestCleanupExecution}.
     *
     * @param services A service lookup reference
     * @param guestExpiry the time span (in milliseconds) after which an unused guest user can be deleted permanently
     */
    public GuestCleanupExecution(ServiceLookup services, long guestExpiry) {
        super();
        this.services = services;
        this.guestExpiry = guestExpiry;
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        try {
            cleanupSchema(schema, connectionProvider.getConnection());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, "Interrupted during cleanup");
        } catch (Exception e) {
            if (OXException.class.isInstance(e)) {
                throw (OXException) e;
            }
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup");
        }
    }

    private void cleanupSchema(String schema, Connection connection) throws Exception {
        List<GuestCleanupTask> cleanupTasks = getCleanupTasksForSchema(connection);
        if (cleanupTasks.isEmpty()) {
            LOG.debug("No guest users found in database schema '{}', skipping cleanup task.", schema);
            return;
        }
        LOG.debug("Found {} guest users in database schema '{}', preparing corresponding cleanup tasks.", I(cleanupTasks.size()), schema);
        for (GuestCleanupTask cleanupTask : cleanupTasks) {
            if (Thread.currentThread().isInterrupted()) {
                LOG.info("Interrupting guest cleanup task on schema '{}'.", schema);
                return;
            }
            cleanupTask.call();
        }
    }

    private List<GuestCleanupTask> getCleanupTasksForSchema(Connection connection) throws OXException {
        List<GuestCleanupTask> cleanupTasks = new LinkedList<GuestCleanupTask>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT cid, id FROM user WHERE guestCreatedBy>0;");
            rs = stmt.executeQuery();
            while (rs.next()) {
                cleanupTasks.add(new GuestCleanupTask(services, rs.getInt(1), rs.getInt(2), guestExpiry));
            }
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        return cleanupTasks;
    }

}
