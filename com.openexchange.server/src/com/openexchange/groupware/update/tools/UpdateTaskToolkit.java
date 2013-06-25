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

package com.openexchange.groupware.update.tools;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.internal.DynamicList;
import com.openexchange.groupware.update.internal.SchemaExceptionCodes;
import com.openexchange.groupware.update.internal.UpdateExecutor;
import com.openexchange.groupware.update.internal.UpdateProcess;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link UpdateTaskToolkit} - Toolkit for update tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskToolkit {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(UpdateTaskToolkit.class));

    private static final Object LOCK = new Object();

    /**
     * Initializes a new {@link UpdateTaskToolkit}.
     */
    private UpdateTaskToolkit() {
        super();
    }

    /**
     * Force (re-)run of update task denoted by given class name
     *
     * @param className The update task's class name
     * @param schemaName A valid schema name
     * @throws OXException If update task cannot be performed
     */
    public static void forceUpdateTask(final String className, final String schemaName) throws OXException {
        forceUpdateTask(className, getContextIdBySchema(schemaName));
    }

    /**
     * Force (re-)run of update task denoted by given class name
     *
     * @param className The update task's class name
     * @param contextId The context identifier
     * @throws OXException If update task cannot be performed
     */
    public static void forceUpdateTask(final String className, final int contextId) throws OXException {
        synchronized (LOCK) {
            forceUpdateTask0(getUpdateTask(className), contextId);
        }
    }

    /**
     * Force (re-)run of update task denoted by given class name. This method should only be called when holding <code>LOCK</code>.
     *
     * @param task The update task
     * @param contextId The context identifier
     * @throws OXException If update task cannot be performed
     */
    private static void forceUpdateTask0(final UpdateTask task, final int contextId) throws OXException {
        new UpdateExecutor(getSchema(contextId), contextId, Collections.singletonList(task)).execute();
    }

    /**
     * Force (re-)run of update task denoted by given class name on all schemas.
     *
     * @param className The update task's class name
     * @throws OXException If update task cannot be performed
     */
    public static void forceUpdateTaskOnAllSchemas(final String className) throws OXException {
        synchronized (LOCK) {
            // Get update task by class name
            final UpdateTask updateTask = getUpdateTask(className);
            // Get all available schemas
            final Map<String, Set<Integer>> map = getSchemasAndContexts();
            // ... and iterate them
            final Iterator<Set<Integer>> iter = map.values().iterator();
            while (iter.hasNext()) {
                final Set<Integer> set = iter.next();
                if (!set.isEmpty()) {
                    forceUpdateTask0(updateTask, set.iterator().next().intValue());
                }
            }
        }
    }

    public static void runUpdateOnAllSchemas() throws OXException {
        synchronized (LOCK) {
            // Get all available schemas
            final Map<String, Set<Integer>> map = getSchemasAndContexts();
            // ... and iterate them
            final Iterator<Set<Integer>> iter = map.values().iterator();
            while (iter.hasNext()) {
                final Set<Integer> set = iter.next();
                if (!set.isEmpty()) {
                    final int contextId = set.iterator().next().intValue();
                    new UpdateProcess(contextId).run();
                }
            }
        }
    }

    /**
     * Gets all schemas with their versions.
     *
     * @return All schemas with their versions
     * @throws OXException If retrieving schemas and versions fails
     */
    public static Map<String, Schema> getSchemasAndVersions() throws OXException {
        // Get schemas with their context IDs
        final Map<String, Set<Integer>> schemasAndContexts = getSchemasAndContexts();
        final Map<String, Schema> schemas = new HashMap<String, Schema>(schemasAndContexts.size());
        for (final Map.Entry<String, Set<Integer>> entry : schemasAndContexts.entrySet()) {
            final Schema schema = getSchema(entry.getValue().iterator().next().intValue());
            schemas.put(entry.getKey(), schema);
        }
        return schemas;
    }

    private static final String SQL_SELECT_SCHEMAS = "SELECT db_schema,cid FROM context_server2db_pool";

    /**
     * Gets schemas and their contexts as a map.
     *
     * @return A map containing schemas and their contexts.
     * @throws OXException If an error occurs
     */
    private static Map<String, Set<Integer>> getSchemasAndContexts() throws OXException {
        final Connection con = Database.get(false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_SCHEMAS);
            rs = stmt.executeQuery();
            final Map<String, Set<Integer>> schemasAndContexts = new HashMap<String, Set<Integer>>();
            while (rs.next()) {
                final String schemaName = rs.getString(1);
                final int contextId = rs.getInt(2);
                Set<Integer> contextIds = schemasAndContexts.get(schemaName);
                if (null == contextIds) {
                    contextIds = new HashSet<Integer>();
                    schemasAndContexts.put(schemaName, contextIds);
                }
                contextIds.add(I(contextId));
            }
            return schemasAndContexts;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(false, con);
        }

    }

    public static int getContextIdBySchema(final String schemaName) throws OXException {
        final Map<String, Set<Integer>> map = getSchemasAndContexts();
        final Set<Integer> set = map.get(schemaName);
        if (null == set) {
            throw UpdateExceptionCodes.UNKNOWN_SCHEMA.create(schemaName);
        }
        return set.iterator().next().intValue();
    }

    /**
     * Sets the schema's version number to given version number
     *
     * @param versionNumber The version number to set
     * @param schemaName A valid schema name
     * @throws OXException If changing version number fails
     */
    public static void resetVersion(final int versionNumber, final String schemaName) throws OXException {
        resetVersion(versionNumber, getContextIdBySchema(schemaName));
    }

    /**
     * Sets the schema's version number to given version number
     *
     * @param versionNumber The version number to set
     * @param contextId A valid context identifier contained in target schema
     * @throws OXException If changing version number fails
     */
    public static void resetVersion(final int versionNumber, final int contextId) throws OXException {
        synchronized (LOCK) {
            // Get schema for given context ID
            final Schema schema = getSchema(contextId);
            // Check version number
            if (schema.getDBVersion() <= versionNumber) {
                throw UpdateExceptionCodes.ONLY_REDUCE.create(I(schema.getDBVersion()), I(versionNumber));
            }
            if (schema.getDBVersion() == Schema.FINAL_VERSION) {
                throw UpdateExceptionCodes.RESET_FORBIDDEN.create(schema.getSchema());
            }
            lockSchema(schema, contextId);
            try {
                // Apply new version number
                setVersionNumber(versionNumber, schema, contextId);
            } finally {
                unlockSchema(schema, contextId);
                // Invalidate schema's contexts
                try {
                    removeContexts(contextId);
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Load update task by class name.
     * @param className name of the update task class.
     * @return the update task class.
     * @throws OXException if the update task class can not be determined.
     */
    private static UpdateTask getUpdateTask(final String className) throws OXException {
        final List<UpdateTask> taskList = DynamicList.getInstance().getTaskList();
        for (final UpdateTask task : taskList) {
            if (task.getClass().getName().equals(className)) {
                return task;
            }
        }
        throw UpdateExceptionCodes.UNKNOWN_TASK.create(className);
    }

    private static final String SQL_UPDATE_VERSION = "UPDATE version SET version = ?";

    private static void setVersionNumber(final int versionNumber, final Schema schema, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, true);
        try {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                // Try to obtain exclusive lock on table 'version'
                con.setAutoCommit(false);
                stmt = con.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
                result = stmt.executeQuery();
                if (!result.next()) {
                    throw SchemaExceptionCodes.MISSING_VERSION_ENTRY.create(schema.getSchema());
                } else if (!result.getBoolean(1)) {
                    // Schema is NOT locked by update process
                    throw SchemaExceptionCodes.UPDATE_CONFLICT.create(schema.getSchema());
                }
            } catch (final SQLException e) {
                rollback(con);
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            } catch (final OXException e) {
                rollback(con);
                throw e;
            } finally {
                closeSQLStuff(result, stmt);
            }
            try {
                // Update schema
                stmt = con.prepareStatement(SQL_UPDATE_VERSION);
                stmt.setInt(1, versionNumber);
                if (stmt.executeUpdate() == 0) {
                    // Schema could not be unlocked
                    throw SchemaExceptionCodes.WRONG_ROW_COUNT.create(I(1), I(0));
                }
                // Everything went fine. Schema is marked as unlocked
                con.commit();
            } catch (final SQLException e) {
                rollback(con);
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            } catch (final OXException e) {
                rollback(con);
                throw e;
            } finally {
                closeSQLStuff(result, stmt);
            }
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    /*
     * ++++++++++++++++++++++++++++ ++ + HELPER METHODS + ++ ++++++++++++++++++++++++++++
     */

    private static SchemaUpdateState getSchema(final int contextId) throws OXException {
        return SchemaStore.getInstance().getSchema(contextId);
    }

    private static final String SQL_SELECT_LOCKED_FOR_UPDATE = "SELECT locked FROM version FOR UPDATE";

    private static void lockSchema(final Schema schema, final int contextId) throws OXException {
        SchemaStore.getInstance().lockSchema(schema, contextId, false);
    }

    private static void unlockSchema(final Schema schema, final int contextId) throws OXException {
        SchemaStore.getInstance().unlockSchema(schema, contextId, false);
    }

    private static void removeContexts(final int contextId) throws OXException, OXException {
        final int[] contextIds = Database.getContextsInSameSchema(contextId);
        final ContextStorage contextStorage = ContextStorage.getInstance();
        for (final int cid : contextIds) {
            contextStorage.invalidateContext(cid);
        }
    }
}
