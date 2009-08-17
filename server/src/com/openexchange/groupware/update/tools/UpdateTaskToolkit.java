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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaStoreImpl;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaException;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link UpdateTaskToolkit} - Toolkit for update tasks.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class UpdateTaskToolkit {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UpdateTaskToolkit.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(UpdateTaskToolkit.class);

    private static final Object LOCK = new Object();

    /**
     * Initializes a new {@link UpdateTaskToolkit}.
     */
    private UpdateTaskToolkit() {
        super();
    }

    /**
     * Gets all schemas with their versions.
     * 
     * @return All schemas with their versions
     * @throws UpdateException If retrieving schemas and versions fails
     */
    public static Map<String, Integer> getSchemasAndVersions() throws UpdateException {
        /*
         * Get schemas with their context IDs
         */
        final Map<String, Set<Integer>> schemasAndContexts = getSchemasAndContexts();
        /*
         * Get version for each schema
         */
        final int size = schemasAndContexts.size();
        final Map<String, Integer> schemasAndVersions = new HashMap<String, Integer>(size);
        final Iterator<Map.Entry<String, Set<Integer>>> it = schemasAndContexts.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<String, Set<Integer>> entry = it.next();
            final Schema schema = getSchema(entry.getValue().iterator().next().intValue());
            schemasAndVersions.put(entry.getKey(), Integer.valueOf(schema.getDBVersion()));
        }
        return schemasAndVersions;
    }

    private static final String SQL_SELECT_SCHEMAS = "SELECT db_schema, cid FROM context_server2db_pool";

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 14 }, msg = { "A SQL error occurred while reading schema version information: %1$s." })
    private static Map<String, Set<Integer>> getSchemasAndContexts() throws UpdateException {
        try {
            Connection writeCon = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                writeCon = Database.get(false);
                stmt = writeCon.prepareStatement(SQL_SELECT_SCHEMAS);
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
                    contextIds.add(Integer.valueOf(contextId));
                }

                return schemasAndContexts;
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
                if (writeCon != null) {
                    Database.back(false, writeCon);
                }
            }
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            throw new UpdateException(e);
        } catch (final SQLException e) {
            throw EXCEPTION.create(9, e, e.getMessage());
        }

    }

    /**
     * Sets the schema's version number to given version number
     * 
     * @param versionNumber The version number to set
     * @param contextId A valid context identifier contained in target schema
     * @throws UpdateException If changing version number fails
     */
    @OXThrowsMultiple(category = { Category.CODE_ERROR, }, desc = { "" }, exceptionId = { 13 }, msg = { "Current version number %1$s is already lower than or equal to desired version number %2$s." })
    public static void resetVersion(final int versionNumber, final int contextId) throws UpdateException {
        synchronized (LOCK) {
            /*
             * Get schema for given context ID
             */
            final Schema schema = getSchema(contextId);
            /*
             * Check version number
             */
            if (schema.getDBVersion() <= versionNumber) {
                throw EXCEPTION.create(13, Integer.valueOf(schema.getDBVersion()), Integer.valueOf(versionNumber));
            }
            /*
             * Lock schema
             */
            lockSchema(schema, contextId);
            try {
                /*
                 * Apply new version number
                 */
                setVersionNumber(versionNumber, schema, contextId);
            } finally {
                /*
                 * Unlock schema
                 */
                unlockSchema(schema, contextId);
            }
        }
    }

    private static final String SQL_UPDATE_VERSION = "UPDATE version SET version = ?";

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.INTERNAL_ERROR, Category.PERMISSION, Category.INTERNAL_ERROR }, desc = {
        "", "", "", "" }, exceptionId = { 9, 10, 11, 12 }, msg = {
        "A SQL error occurred while reading schema version information: %1$s.", "Though expected, SQL query returned no result.",
        "Update conflict detected. Schema %1$s is not marked as LOCKED.", "Table update failed. Schema %1$s could not be updated." })
    private static void setVersionNumber(final int versionNumber, final Schema schema, final int contextId) throws UpdateException {
        try {
            boolean error = false;
            Connection writeCon = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                /*
                 * Try to obtain exclusive lock on table 'version'
                 */
                writeCon = Database.get(contextId, true);
                writeCon.setAutoCommit(false); // BEGIN
                stmt = writeCon.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    error = true;
                    throw EXCEPTION.create(10);
                } else if (!rs.getBoolean(1)) {
                    /*
                     * Schema is NOT locked by update process
                     */
                    error = true;
                    throw EXCEPTION.create(11, schema.getSchema());
                }
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                /*
                 * Update schema
                 */
                stmt = writeCon.prepareStatement(SQL_UPDATE_VERSION);
                stmt.setInt(1, versionNumber);
                if (stmt.executeUpdate() == 0) {
                    /*
                     * Schema could not be unlocked
                     */
                    error = true;
                    throw EXCEPTION.create(12, schema.getSchema());
                }
                /*
                 * Everything went fine. Schema is marked as unlocked
                 */
                writeCon.commit(); // COMMIT
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
                if (writeCon != null) {
                    if (error) {
                        try {
                            writeCon.rollback();
                        } catch (final SQLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    if (!writeCon.getAutoCommit()) {
                        try {
                            writeCon.setAutoCommit(true);
                        } catch (final SQLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    Database.back(contextId, true, writeCon);
                }
            }
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            throw new UpdateException(e);
        } catch (final SQLException e) {
            throw EXCEPTION.create(9, e, e.getMessage());
        }
    }

    /**
     * Gets the schema for given context identifier.
     * 
     * @param contextId The context identifier
     * @return The schema for given context identifier
     * @throws UpdateException If schema cannot be resolved for given context identifier
     */
    public static Schema getSchema(final int contextId) throws UpdateException {
        try {
            return SchemaStore.getInstance(SchemaStoreImpl.class.getCanonicalName()).getSchema(ContextStorage.getStorageContext(contextId));
        } catch (final SchemaException e) {
            LOG.error(e.getMessage(), e);
            throw new UpdateException(e);
        } catch (final ContextException e) {
            LOG.error(e.getMessage(), e);
            throw new UpdateException(e);
        }
    }

    /*-
     * ++++++++++++++++++++++++++++ ++ + HELPER METHODS + ++ ++++++++++++++++++++++++++++
     */

    private static final String SQL_SELECT_LOCKED_FOR_UPDATE = "SELECT locked FROM version FOR UPDATE";

    private static final String SQL_UPDATE_LOCKED = "UPDATE version SET locked = ?";

    /**
     * Locks given schema.
     * 
     * @param schema The schema to lock
     * @param contextId A valid context identifier contained in given schema
     * @throws UpdateException If locking schema fails
     */
    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.INTERNAL_ERROR, Category.PERMISSION, Category.INTERNAL_ERROR }, desc = {
        "", "", "", "" }, exceptionId = { 1, 2, 3, 4 }, msg = {
        "A SQL error occurred while reading schema version information: %1$s.", "Though expected, SQL query returned no result.",
        "Update conflict detected. Another process is currently updating schema %1$s.",
        "Table update failed. Schema %1$s could not be locked." })
    private static void lockSchema(final Schema schema, final int contextId) throws UpdateException {
        /*
         * Start of update process, so lock schema
         */
        boolean error = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection writeCon = null;
        try {
            writeCon = Database.get(contextId, true);
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            throw new UpdateException(e);
        }
        try {
            /*
             * Try to obtain exclusive lock on table 'version'
             */
            writeCon.setAutoCommit(false); // BEGIN
            stmt = writeCon.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                error = true;
                throw EXCEPTION.create(2);
            } else if (rs.getBoolean(1)) {
                /*
                 * Schema is already locked by another update process
                 */
                error = true;
                throw EXCEPTION.create(3, schema.getSchema());
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            /*
             * Lock schema
             */
            stmt = writeCon.prepareStatement(SQL_UPDATE_LOCKED);
            stmt.setBoolean(1, true);
            if (stmt.executeUpdate() == 0) {
                /*
                 * Schema could not be locked
                 */
                error = true;
                throw EXCEPTION.create(4, schema.getSchema());
            }
            /*
             * Everything went fine. Schema is marked as locked
             */
            writeCon.commit(); // COMMIT
        } catch (final SQLException e) {
            error = true;
            throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (writeCon != null) {
                if (error) {
                    try {
                        writeCon.rollback();
                    } catch (final SQLException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                try {
                    if (!writeCon.getAutoCommit()) {
                        writeCon.setAutoCommit(true);
                    }
                } catch (final SQLException e) {
                    LOG.error(e.getMessage(), e);
                }
                Database.back(contextId, true, writeCon);
            }
        }
    } // End of lockSchema()

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.INTERNAL_ERROR, Category.PERMISSION, Category.INTERNAL_ERROR }, desc = {
        "", "", "", "" }, exceptionId = { 5, 6, 7, 8 }, msg = {
        "A SQL error occurred while reading schema version information: %1$s.", "Though expected, SQL query returned no result.",
        "Update conflict detected. Schema %1$s is not marked as LOCKED.", "Table update failed. Schema %1$s could not be unlocked." })
    private static void unlockSchema(final Schema schema, final int contextId) throws UpdateException {
        try {
            boolean error = false;
            Connection writeCon = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                /*
                 * Try to obtain exclusive lock on table 'version'
                 */
                writeCon = Database.get(contextId, true);
                writeCon.setAutoCommit(false); // BEGIN
                stmt = writeCon.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    error = true;
                    throw EXCEPTION.create(6);
                } else if (!rs.getBoolean(1)) {
                    /*
                     * Schema is NOT locked by update process
                     */
                    error = true;
                    throw EXCEPTION.create(7, schema.getSchema());
                }
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                /*
                 * Update & unlock schema
                 */
                stmt = writeCon.prepareStatement(SQL_UPDATE_LOCKED);
                stmt.setBoolean(1, false);
                if (stmt.executeUpdate() == 0) {
                    /*
                     * Schema could not be unlocked
                     */
                    error = true;
                    throw EXCEPTION.create(8, schema.getSchema());
                }
                /*
                 * Everything went fine. Schema is marked as unlocked
                 */
                writeCon.commit(); // COMMIT
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
                if (writeCon != null) {
                    if (error) {
                        try {
                            writeCon.rollback();
                        } catch (final SQLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    if (!writeCon.getAutoCommit()) {
                        try {
                            writeCon.setAutoCommit(true);
                        } catch (final SQLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    Database.back(contextId, true, writeCon);
                }
            }
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            throw new UpdateException(e);
        } catch (final SQLException e) {
            throw EXCEPTION.create(5, e, e.getMessage());
        }
    } // End of unlockSchema()

}
