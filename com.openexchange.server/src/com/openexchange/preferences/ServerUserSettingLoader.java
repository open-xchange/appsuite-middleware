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

package com.openexchange.preferences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link ServerUserSettingLoader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServerUserSettingLoader  {

    private static final ServerUserSettingLoader INSTANCE = new ServerUserSettingLoader();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ServerUserSettingLoader getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------------- //

    /**
     * Safely loads the server user setting map for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The server user setting map or <code>null</code>
     */
    public Map<String,Object> loadForSafe(final int userId, final int contextId) {
        try {
            return loadFor(userId, contextId);
        } catch (final Exception e) {
            final Logger logger = org.slf4j.LoggerFactory.getLogger(ServerUserSettingLoader.class);
            logger.error("", e);
            return null;
        }
    }

    /**
     * Loads the server user setting map for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The server user setting map
     * @throws OXException If operation fails
     */
    public Map<String,Object> loadFor(final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT contact_collect_folder,contact_collect_enabled,defaultStatusPrivate,defaultStatusPublic,contactCollectOnMailAccess,contactCollectOnMailTransport,folderTree FROM user_setting_server WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final Map<String,Object> newMap = new ConcurrentHashMap<String, Object>(12, 0.9f, 1);
            newMap.put("__lastAccessed", Long.valueOf(System.currentTimeMillis()));

            do {
                {
                    final int folderId = rs.getInt(1);
                    if (!rs.wasNull()) {
                        newMap.put("contact_collect_folder", Integer.valueOf(folderId));
                    }
                }

                {
                    final int enabled = rs.getInt(2);
                    if (rs.wasNull()) {
                        newMap.put("contact_collect_enabled", Boolean.FALSE);
                    } else {
                        newMap.put("contact_collect_enabled", Boolean.valueOf(enabled > 0));
                    }
                }

                {
                    final int defaultStatusPrivate = rs.getInt(3);
                    if (!rs.wasNull()) {
                        newMap.put("defaultStatusPrivate", Integer.valueOf(defaultStatusPrivate));
                    }
                }

                {
                    final int defaultStatusPublic = rs.getInt(4);
                    if (!rs.wasNull()) {
                        newMap.put("defaultStatusPublic", Integer.valueOf(defaultStatusPublic));
                    }
                }

                {
                    final int contactCollectOnMailAccess = rs.getInt(5);
                    if (rs.wasNull()) {
                        newMap.put("contactCollectOnMailAccess", Boolean.FALSE);
                    } else {
                        newMap.put("contactCollectOnMailAccess", Boolean.valueOf(contactCollectOnMailAccess > 0));
                    }
                }

                {
                    final int contactCollectOnMailTransport = rs.getInt(6);
                    if (rs.wasNull()) {
                        newMap.put("contactCollectOnMailTransport", Boolean.FALSE);
                    } else {
                        newMap.put("contactCollectOnMailTransport", Boolean.valueOf(contactCollectOnMailTransport > 0));
                    }
                }

                {
                    final int folderTree = rs.getInt(7);
                    if (!rs.wasNull()) {
                        newMap.put("folderTree", Integer.valueOf(folderTree));
                    }
                }
            } while (rs.next());

            return newMap;
        } catch (final SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }


    }

}
