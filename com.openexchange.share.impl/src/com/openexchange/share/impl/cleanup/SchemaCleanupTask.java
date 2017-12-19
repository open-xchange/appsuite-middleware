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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link SchemaCleanupTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SchemaCleanupTask extends AbstractTask<Map<Integer, List<GuestCleanupTask>>> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaCleanupTask.class);

    protected final ServiceLookup services;
    protected final int representativeContextId;
    protected final long guestExpiry;

    /**
     * Initializes a new {@link SchemaCleanupTask}.
     *
     * @param services A service lookup reference
     * @param representativeContextId The context ID located in target schema
     * @param guestExpiry the time span (in milliseconds) after which an unused guest user can be deleted permanently
     */
    public SchemaCleanupTask(ServiceLookup services, int representativeContextId, long guestExpiry) {
        super();
        this.services = services;
        this.representativeContextId = representativeContextId;
        this.guestExpiry = guestExpiry;
    }

    @Override
    public Map<Integer, List<GuestCleanupTask>> call() throws Exception {
        try {
            return cleanSchema();
        } catch (OXException e) {
            if ("CTX-0002".equals(e.getErrorCode())) {
                LOG.debug("Context {} no longer found, cancelling cleanup.", representativeContextId, e);
                return Collections.emptyMap();
            }
            throw e;
        }
    }

    private Map<Integer, List<GuestCleanupTask>> cleanSchema() throws OXException {
        /*
         * Check if context needs update
         */
        Updater updater = Updater.getInstance();
        UpdateStatus status = updater.getStatus(representativeContextId);
        if (status.needsBackgroundUpdates() || status.needsBlockingUpdates() || status.backgroundUpdatesRunning() || status.blockingUpdatesRunning()) {
            LOG.info("Schema of context {} needs update, skipping cleanup task.", I(representativeContextId));
            return Collections.emptyMap();
        }

        /*
         * gather guest users in context & create appropriate clean-up task for them
         */
        return getCleanUpTasksForSchema();
    }

    private Map<Integer, List<GuestCleanupTask>> getCleanUpTasksForSchema() throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = databaseService.getReadOnly(representativeContextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id FROM user WHERE guestCreatedBy>0");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                LOG.debug("No guest users found in schema of context {}, skipping cleanup task.", I(representativeContextId));
                return Collections.emptyMap();
            }

            Map<Integer, List<GuestCleanupTask>> cleanUpTasksForSchema = new LinkedHashMap<>(512);
            do {
                Integer contextId = Integer.valueOf(rs.getInt(1));
                List<GuestCleanupTask> cleanupTasks = cleanUpTasksForSchema.get(contextId);
                if (null == cleanupTasks) {
                    cleanupTasks = new LinkedList<>();
                    cleanUpTasksForSchema.put(contextId, cleanupTasks);
                }
                int guestID = rs.getInt(2);
                cleanupTasks.add(new GuestCleanupTask(services, contextId.intValue(), guestID, guestExpiry));
            } while (rs.next());

            if (LOG.isDebugEnabled()) {
                for (Map.Entry<Integer,List<GuestCleanupTask>> entry : cleanUpTasksForSchema.entrySet()) {
                    LOG.debug("Found {} guest users in context {}, preparing corresponding cleanup tasks.", I(entry.getValue().size()), entry.getKey());
                }
            }

            return cleanUpTasksForSchema;
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(representativeContextId, con);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + representativeContextId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SchemaCleanupTask)) {
            return false;
        }
        SchemaCleanupTask other = (SchemaCleanupTask) obj;
        if (representativeContextId != other.representativeContextId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SchemaCleanupTask [representativeContextId=" + representativeContextId + "]";
    }

}
