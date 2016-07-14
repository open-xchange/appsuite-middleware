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

package com.openexchange.quota.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link AmountQuotas} provides static helper methods for groupware modules
 * that need to maintain quotas for item amounts.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class AmountQuotas {

    private static final Logger LOG = LoggerFactory.getLogger(AmountQuotas.class);

    /**
     * Adjusts the quota amount limit for the given modules in the database. Existing limits for a module are overwritten, or new limits
     * are stored if not yet defined.
     *
     * @param contextID The context ID
     * @param moduleIDs The module IDs to set the limit in the database for
     * @param limit The limit to set
     * @param connection A writable connection to the database
     * @throws OXException
     */
    public static void setLimit(int contextID, List<String> moduleIDs, long limit, Connection connection) throws OXException {
        for (final String module : moduleIDs) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                // Determine if already present
                final boolean exists;
                {
                    stmt = connection.prepareStatement("SELECT 1 FROM quota_context WHERE cid=? AND module=?");
                    stmt.setInt(1, contextID);
                    stmt.setString(2, module);
                    rs = stmt.executeQuery();
                    exists = rs.next();
                    Databases.closeSQLStuff(rs, stmt);
                    stmt = null;
                    rs = null;
                }
                // Insert/update row
                if (exists) {
                    if (limit < 0) {
                        // Delete
                        stmt = connection.prepareStatement("DELETE FROM quota_context WHERE cid=? AND module=?");
                        stmt.setInt(1, contextID);
                        stmt.setString(2, module);
                        stmt.executeUpdate();
                    } else {
                        // Update
                        stmt = connection.prepareStatement("UPDATE quota_context SET value=? WHERE cid=? AND module=?");
                        stmt.setLong(1, limit <= 0 ? 0 : limit);
                        stmt.setInt(2, contextID);
                        stmt.setString(3, module);
                        stmt.executeUpdate();
                    }
                } else {
                    if (limit >= 0) {
                        // Insert
                        stmt = connection.prepareStatement("INSERT INTO quota_context (cid, module, value) VALUES (?, ?, ?)");
                        stmt.setInt(1, contextID);
                        stmt.setString(2, module);
                        stmt.setLong(3, limit <= 0 ? 0 : limit);
                        stmt.executeUpdate();
                    }
                }
            } catch (final SQLException e) {
                throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }
    }

    /**
     * Gets the configured amount limit in a specific module for the supplied session's user. The value is either retrieved directly from
     * the database, or, if not defined there, through the user's configuration view.
     *
     * @param session The session
     * @param moduleID The module ID
     * @param configViewFactory A reference to the config view factory
     * @param connection An open database connection
     * @return The limit or {@link Quota#UNLIMITED} if no amount quota is defined
     * @throws OXException
     */
    public static long getLimit(Session session, String moduleID, ConfigViewFactory configViewFactory, Connection connection) throws OXException {
        return getLimit(session, moduleID, configViewFactory, connection, Quota.UNLIMITED);
    }

    /**
     * Gets the configured amount limit in a specific module for the supplied session's user. The value is either retrieved directly from
     * the database, or, if not defined there, through the user's configuration view.<br>
     * <br>
     * If no value can be found the provided default value will be returned
     *
     * @param session The session
     * @param moduleID The module ID
     * @param configViewFactory A reference to the config view factory
     * @param connection An open database connection
     * @param defaultValue Value that will be returned if amount can not be found
     * @return The limit or the defaultValue if no amount quota is defined
     * @throws OXException
     */
    public static long getLimit(Session session, String moduleID, ConfigViewFactory configViewFactory, Connection connection, long defaultValue) throws OXException {
        final Long quotaFromDB = getQuotaFromDB(connection, session.getContextId(), moduleID);
        if (null != quotaFromDB) {
            return quotaFromDB.longValue();
        }

        return getConfiguredLimitByModuleId(session, moduleID, configViewFactory, defaultValue);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the configured amount limit in a specific module for the supplied session's user. The value is retrieved directly from user's
     * configuration view.
     * <p>
     * If no value can be found {@link Quota#UNLIMITED} will be returned
     *
     * @param session The session
     * @param moduleID The module ID
     * @param configViewFactory A reference to the config view factory
     * @return The limit or {@link Quota#UNLIMITED} if no amount quota is defined
     * @throws OXException
     */
    public static long getConfiguredLimitByModuleId(Session session, String moduleID, ConfigViewFactory configViewFactory) throws OXException {
        return getConfiguredLimitByModuleId(session, moduleID, configViewFactory, Quota.UNLIMITED);
    }

    /**
     * Gets the configured amount limit in a specific module for the supplied session's user. The value is retrieved directly from user's
     * configuration view.
     * <p>
     * If no value can be found the provided default value will be returned
     *
     * @param session The session
     * @param moduleID The module ID
     * @param configViewFactory A reference to the config view factory
     * @param defaultValue Value that will be returned if amount can not be found
     * @return The limit or the defaultValue if no amount quota is defined
     * @throws OXException
     */
    public static long getConfiguredLimitByModuleId(Session session, String moduleID, ConfigViewFactory configViewFactory, long defaultValue) throws OXException {
        ConfigView configView = configViewFactory.getView(session.getUserId(), session.getContextId());
        // Get property
        ConfigProperty<String> property = configView.property(new StringBuilder("com.openexchange.quota.").append(moduleID).toString(), String.class);
        if (!property.isDefined()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(property.get().trim());
        } catch (final RuntimeException e) {
            LOG.warn("Couldn't detect quota for {} (user={}, context={})", moduleID, session.getUserId(), session.getContextId(), e);
            return defaultValue;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the configured amount limit for a specified property name using the supplied session's user. The value is retrieved directly
     * from user's configuration view.
     * <p>
     * If no value can be found {@link Quota#UNLIMITED} will be returned
     *
     * @param session The session
     * @param propName The name of the property providing the amount limit
     * @param configViewFactory A reference to the config view factory
     * @return The limit or {@link Quota#UNLIMITED} if no amount quota is defined
     * @throws OXException
     */
    public static long getConfiguredLimitByPropertyName(Session session, String propName, ConfigViewFactory configViewFactory) throws OXException {
        return getConfiguredLimitByPropertyName(session, propName, configViewFactory, Quota.UNLIMITED);
    }

    /**
     * Gets the configured amount limit in a specific module for the supplied session's user. The value is retrieved directly from user's
     * configuration view.
     * <p>
     * If no value can be found the provided default value will be returned
     *
     * @param session The session
     * @param propName The name of the property providing the amount limit
     * @param configViewFactory A reference to the config view factory
     * @param defaultValue Value that will be returned if amount can not be found
     * @return The limit or the defaultValue if no amount quota is defined
     * @throws OXException
     */
    public static long getConfiguredLimitByPropertyName(Session session, String propName, ConfigViewFactory configViewFactory, long defaultValue) throws OXException {
        ConfigView configView = configViewFactory.getView(session.getUserId(), session.getContextId());
        // Get property
        ConfigProperty<String> property = configView.property(propName, String.class);
        if (!property.isDefined()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(property.get().trim());
        } catch (final RuntimeException e) {
            LOG.warn("Couldn't detect quota from property {} (user={}, context={})", propName, session.getUserId(), session.getContextId(), e);
            return defaultValue;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the configured amount limit in a specific module for the supplied session's user. The value is either retrieved directly from
     * the database, or, if not defined there, through the user's configuration view.
     *
     * @param session The session
     * @param moduleID The module ID
     * @param configViewFactory A reference to the config view factory
     * @param dbService A reference to the database service
     * @return The limit or {@link Quota#UNLIMITED} if no amount quota is defined
     * @throws OXException
     */
    public static long getLimit(Session session, String moduleID, ConfigViewFactory configViewFactory, DatabaseService dbService) throws OXException {
        Connection connection = null;
        try {
            connection = dbService.getReadOnly(session.getContextId());
            return getLimit(session, moduleID, configViewFactory, connection);
        } finally {
            if (null != connection) {
                dbService.backReadOnly(session.getContextId(), connection);
            }
        }
    }

    /**
     * Gets the amount limit for specified module from database for given context.
     *
     * @param connection The connection to use
     * @param contextId The context identifier
     * @param moduleID The module identifier
     * @return The amount limit or <code>null</code> if no such quota is available in database
     * @throws OXException If retrieval from database fails
     */
    public static Long getQuotaFromDB(Connection connection, int contextId, String moduleID) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT value FROM quota_context WHERE cid=? AND module=?");
            stmt.setLong(1, contextId);
            stmt.setString(2, moduleID);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final long retval = rs.getLong(1);
            if (rs.wasNull()) {
                return null;
            }
            return Long.valueOf(retval <= 0 ? Quota.UNLIMITED : retval);
        } catch (final SQLException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Gets the available quota module identifiers from database
     *
     * @param connection The connection to use
     * @param contextId The context identifier
     * @return The available quota module identifiers or an empty array
     * @throws OXException If quota module identifiers cannot be fetched from database
     */
    public static String[] getQuotaModuleIDs(Connection connection, int contextId) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT module FROM quota_context WHERE cid=?");
            stmt.setLong(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new String[0];
            }

            List<String> modules = new ArrayList<String>(8);
            do {
                String module = rs.getString(1);
                if (!rs.wasNull()) {
                    modules.add(module);
                }
            } while (rs.next());
            return modules.toArray(new String[modules.size()]);
        } catch (final SQLException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Initializes a new {@link AmountQuotas}.
     */
    private AmountQuotas() {
        super();
    }

}
