/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.advertisement.impl.rmi;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.advertisement.AdvertisementExceptionCodes;
import com.openexchange.advertisement.AdvertisementPackageService;
import com.openexchange.advertisement.RemoteAdvertisementService;
import com.openexchange.advertisement.impl.osgi.Services;
import com.openexchange.advertisement.impl.services.AbstractAdvertisementConfigService;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RemoteAdvertisementServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class RemoteAdvertisementServiceImpl implements RemoteAdvertisementService {

    private static final String SQL_DELETE_MAPPING_ALL = "DELETE FROM advertisement_mapping;";
    private static final String SQL_DELETE_CONFIG_ALL = "DELETE FROM advertisement_config;";

    private static final String SQL_DELETE_MAPPING = "DELETE FROM advertisement_mapping WHERE reseller=?;";
    private static final String SQL_DELETE_CONFIG = "DELETE FROM advertisement_config WHERE reseller=?;";

    private static final String SQL_DELETE_MAPPING_WHERE_IN = "DELETE FROM advertisement_mapping WHERE reseller IN (";
    private static final String SQL_DELETE_CONFIG_WHERE_IN = "DELETE FROM advertisement_config WHERE reseller IN (";
    private static final String SQL_DELETE_CONFIG_WHERE_NOT_IN = "DELETE FROM advertisement_config WHERE configId NOT IN (";

    // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
    private static final String SQL_SELECT_RESELLER_FROM_CONFIG = "SELECT reseller FROM advertisement_config GROUP BY reseller;";
    private static final String SQL_SELECT_CONFIGIDS_FROM_MAPPING = "SELECT configId FROM advertisement_mapping;";
    private static final String SQL_SELECT_CONFIGIDS_FROM_MAPPING_WHERE = "SELECT configId FROM advertisement_mapping WHERE reseller=?;";

    @Override
    public void removeConfigurations(String reseller, boolean clean, boolean includePreview) throws OXException {

        List<String> activeResellerNames = null;
        if (clean) {
            // collect active reseller
            ResellerService resellerService = Services.getService(ResellerService.class);
            List<ResellerAdmin> resellerList = resellerService.getAll();
            activeResellerNames = new ArrayList<>(resellerList.size());
            for (ResellerAdmin admin : resellerList) {
                activeResellerNames.add(admin.getName());
            }
            activeResellerNames.add(AdvertisementPackageService.DEFAULT_RESELLER);
            if (reseller != null && !includePreview && activeResellerNames.contains(reseller)) {
                // Nothing to clean
                return;
            }
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        boolean isReadOnly = true;
        try {
            if (reseller == null) {
                if (activeResellerNames == null) {
                    // All delete
                    stmt = con.prepareStatement(SQL_DELETE_CONFIG_ALL);
                    if (stmt.executeUpdate() > 0) {
                        isReadOnly = false;
                    }
                    Databases.closeSQLStuff(stmt);
                    stmt = con.prepareStatement(SQL_DELETE_MAPPING_ALL);
                    if (stmt.executeUpdate() > 0) {
                        isReadOnly = false;
                    }
                } else {
                    // All clean
                    // Calculate reseller to remove
                    stmt = con.prepareStatement(SQL_SELECT_RESELLER_FROM_CONFIG);
                    resultSet = stmt.executeQuery();
                    List<String> resellerToRemove = new ArrayList<>(resultSet.getFetchSize());
                    while (resultSet.next()) {
                        String tmp = resultSet.getString(1);
                        if (!activeResellerNames.contains(tmp)) {
                            resellerToRemove.add(tmp);
                        }
                    }

                    if (!resellerToRemove.isEmpty()) {
                        // remove reseller configs
                        Databases.closeSQLStuff(stmt);
                        String sql = DBUtils.getIN(SQL_DELETE_CONFIG_WHERE_IN, resellerToRemove.size());
                        stmt = con.prepareStatement(sql + ";");
                        int x = 1;
                        for (String remove : resellerToRemove) {
                            stmt.setString(x++, remove);
                        }
                        if (stmt.executeUpdate() > 0) {
                            isReadOnly = false;
                        }

                        // remove reseller mappings
                        Databases.closeSQLStuff(stmt);
                        sql = DBUtils.getIN(SQL_DELETE_MAPPING_WHERE_IN, resellerToRemove.size());
                        stmt = con.prepareStatement(sql + ";");
                        x = 1;
                        for (String remove : resellerToRemove) {
                            stmt.setString(x++, remove);
                        }
                        if (stmt.executeUpdate() > 0) {
                            isReadOnly = false;
                        }
                    }
                    if (includePreview) {
                        // clean all previews
                        Databases.closeSQLStuff(resultSet, stmt);
                        stmt = con.prepareStatement(SQL_SELECT_CONFIGIDS_FROM_MAPPING);
                        resultSet = stmt.executeQuery();
                        List<Integer> ids = new ArrayList<>();
                        while (resultSet.next()) {
                            ids.add(I(resultSet.getInt(1)));
                        }

                        Databases.closeSQLStuff(stmt);
                        if (ids.isEmpty()) {
                            // remove all configs, since noone has a mapping
                            stmt = con.prepareStatement(SQL_DELETE_CONFIG_ALL);
                        } else {
                            // remove all configs which does not have a mapping
                            String sql = DBUtils.getIN(SQL_DELETE_CONFIG_WHERE_NOT_IN, ids.size(), ";");
                            stmt = con.prepareStatement(sql);
                            int x = 1;
                            for (Integer id : ids) {
                                stmt.setInt(x++, id.intValue());
                            }
                        }
                        if (stmt.executeUpdate() > 0) {
                            isReadOnly = false;
                        }
                    }
                }
            } else {
                if (activeResellerNames == null) {
                    stmt = con.prepareStatement(SQL_DELETE_CONFIG);
                    stmt.setString(1, reseller);
                    if (stmt.executeUpdate() > 0) {
                        isReadOnly = false;
                    }
                    Databases.closeSQLStuff(stmt);
                    stmt = con.prepareStatement(SQL_DELETE_MAPPING);
                    stmt.setString(1, reseller);
                    if (stmt.executeUpdate() > 0) {
                        isReadOnly = false;
                    }
                } else {
                    // Clean for given reseller
                    if (!activeResellerNames.contains(reseller)) {
                        // reseller is not active and has to be removed
                        stmt = con.prepareStatement(SQL_DELETE_CONFIG);
                        stmt.setString(1, reseller);
                        if (stmt.executeUpdate() > 0) {
                            isReadOnly = false;
                        }
                        Databases.closeSQLStuff(stmt);
                        stmt = con.prepareStatement(SQL_DELETE_MAPPING);
                        stmt.setString(1, reseller);
                        if (stmt.executeUpdate() > 0) {
                            isReadOnly = false;
                        }
                        return;
                    }
                    if (includePreview) {
                        // only remove previews for the given reseller
                        Databases.closeSQLStuff(resultSet, stmt);
                        stmt = con.prepareStatement(SQL_SELECT_CONFIGIDS_FROM_MAPPING_WHERE);
                        stmt.setString(1, reseller);
                        resultSet = stmt.executeQuery();
                        List<Integer> ids = new ArrayList<>();
                        while (resultSet.next()) {
                            ids.add(I(resultSet.getInt(1)));
                        }

                        Databases.closeSQLStuff(stmt);
                        // remove all configs which does not have a mapping
                        String sql = DBUtils.getIN(SQL_DELETE_CONFIG_WHERE_NOT_IN, ids.size(), " AND reseller=?;");
                        stmt = con.prepareStatement(sql);
                        int x = 1;
                        for (Integer id : ids) {
                            stmt.setInt(x++, id.intValue());
                        }
                        stmt.setString(x, reseller);
                        if (stmt.executeUpdate() > 0) {
                            isReadOnly = false;
                        }
                    }

                }
            }

            // clear cache
            CacheService cacheService = Services.getService(CacheService.class);
            if (cacheService != null) {
                Cache cache = cacheService.getCache(AbstractAdvertisementConfigService.CACHING_REGION);
                cache.clear();
            }
        } catch (SQLException e) {
            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
            if (con != null) {
                if (isReadOnly) {
                    dbService.backReadOnly(con);
                } else {
                    dbService.backWritable(con);
                }
            }
        }


    }

}
