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

package com.openexchange.advertisement.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.osgi.Services;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link AbstractAdvertisementConfigService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public abstract class AbstractAdvertisementConfigService implements AdvertisementConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(AdvertisementConfigService.class);

    private static final String SQL_INSERT_MAPPING = "REPLACE INTO advertisement_mapping (reseller,package,configId) VALUES (?,?,?);";
    private static final String SQL_INSERT_CONFIG = "INSERT INTO advertisement_config (config) VALUES (?);";
    private static final String SQL_UPDATE_CONFIG = "UPDATE advertisement_config SET config=? where configId=?;";
    private static final String SQL_SELECT_MAPPING_SIMPLE = "Select * from advertisement_mapping where reseller=? AND package=?;";
    private static final String SQL_SELECT_MAPPING = "Select * from advertisement_mapping where (reseller=? OR reseller=?) AND (package";
    private static final String SQL_SELECT_CONFIG = "Select * from advertisement_config where configId=?;";
    private static final String RESELLER_ALL = "OX_ALL";
    private static final String PACKAGE_ALL = "OX_ALL";
    private static final String PREVIEW_CONFIG = "com.openexchange.advertisement.preview";



    @Override
    public boolean isAvailable(Session session) {
        // TODO Check if a config is available
        return true;
    }

    abstract String getReseller(Session session) throws OXException;

    abstract List<String> getPackages(Session session) throws OXException;;

    @Override
    public JSONObject getConfig(Session session) throws OXException {
        
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        String id = view.get(PREVIEW_CONFIG, String.class);
        if (!Strings.isEmpty(id)) {
            int configId = Integer.valueOf(id);

            DatabaseService dbService = Services.getService(DatabaseService.class);
            Connection con = dbService.getReadOnly();
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement(SQL_SELECT_CONFIG);
                stmt.setInt(1, configId);
                result = stmt.executeQuery();
                if (result.next()) {
                    return new JSONObject(result.getString(2));
                }
            } catch (SQLException e) {
                throw new OXException(e);
            } catch (JSONException e) {
                LOG.warn("Invalid advertisement configuration data with id %s", configId);
                // Invalid advertisement data. Fallback to reseller and package
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        // ignore
                    }
                }
                if (result != null) {
                    try {
                        result.close();
                    } catch (SQLException e) {
                        // ignore
                    }
                }
                dbService.backReadOnly(con);
            }

        }
        

        String reseller = getReseller(session);
        List<String> packs = getPackages(session);


        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet result = null;
        StringBuilder sql = new StringBuilder(SQL_SELECT_MAPPING);
        try {

            if (packs != null && packs.size() != 0) {
                sql.append(" IN (");
                sql = new StringBuilder(DBUtils.getIN(sql.toString(), packs.size()));
                sql.append(" OR package=?);");
            } else {
                sql.append("=?);");
            }
            int x = 1;
            stmt = con.prepareStatement(sql.toString());
            stmt.setString(x++, reseller);
            stmt.setString(x++, RESELLER_ALL);
            if (packs != null) {
                for (String pack : packs) {
                    stmt.setString(x++, pack);
                }
            }
            stmt.setString(x, PACKAGE_ALL);
            result = stmt.executeQuery();
            List<AdvertisementConfig> list = new ArrayList<>(4);
            while (result.next()) {
                list.add(new AdvertisementConfig(result.getString(1), result.getString(2), result.getString(3)));
            }
            Collections.sort(list);
            stmt.close();
            result.close();
            for (AdvertisementConfig config : list) {
                try {
                    int configId = Integer.valueOf(config.getConfigId());
                    stmt = con.prepareStatement(SQL_SELECT_CONFIG);
                    stmt.setInt(1, configId);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        return new JSONObject(result.getString(2));
                    }
                    stmt.close();
                    result.close();
                } catch (JSONException e) {
                    LOG.error("Invalid advertisement configuration data for reseller %s and package %s", reseller, config.getPackage());
                }
            }
            throw new OXException(1, "No valid configurations available for user %s in context %s", session.getUserId(), session.getContextId());

        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            dbService.backReadOnly(con);
        }
    }

    @Override
    public void putConfig(int userId, int ctxId, String config) throws OXException {
        // TODO put config

    }

    @Override
    public void putConfig(String reseller, String pack, String config) throws OXException {

        if (Strings.isEmpty(reseller)) {
            reseller = RESELLER_ALL;
        }
        if (Strings.isEmpty(pack)) {
            pack = PACKAGE_ALL;
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {

            stmt = con.prepareStatement(SQL_SELECT_MAPPING_SIMPLE);
            stmt.setString(1, reseller);
            stmt.setString(2, pack);
            result = stmt.executeQuery();
            
            if (result.next()) {

                int configId = Integer.valueOf(result.getString(3));
                result.close();
                stmt.close();
                stmt = con.prepareStatement(SQL_UPDATE_CONFIG);
                stmt.setString(1, config);
                stmt.setInt(2, configId);
                stmt.execute();

            } else {
                result.close();
                stmt.close();

                stmt = con.prepareStatement(SQL_INSERT_CONFIG);
                stmt.setString(1, config);
                stmt.execute();
                result = stmt.getGeneratedKeys();
                if (!result.next()) {
                    // some error
                }
                int resultConfigId = result.getInt(1);

                stmt = con.prepareStatement(SQL_INSERT_MAPPING);
                stmt.setString(1, reseller);
                stmt.setString(2, pack);
                stmt.setInt(3, resultConfigId);
                stmt.execute();
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                // ignore
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            dbService.backWritable(con);
        }

    }

    @Override
    public void putConfig(String reseller, String config) throws OXException {
        putConfig(reseller, null, config);
    }

    @Override
    public void putConfigByName(String name, String config) throws OXException {
        // TODO put config

    }

    private class AdvertisementConfig implements Comparable<AdvertisementConfig> {

        String reseller;
        String pack;
        String configId;

        public AdvertisementConfig(String reseller, String pack, String configId) {
            this.reseller = reseller;
            this.pack = pack;
            this.configId = configId;
        }

        public Object getPackage() {
            return pack;
        }

        public String getConfigId() {
            return configId;
        }

        @Override
        public int compareTo(AdvertisementConfig o) {

            if (this.reseller != RESELLER_ALL && o.reseller == RESELLER_ALL) {
                return 1;
            }

            if (this.reseller == RESELLER_ALL && o.reseller != RESELLER_ALL) {
                return -1;
            }

            if (this.pack != PACKAGE_ALL && o.pack == PACKAGE_ALL) {
                return 1;
            }

            if (this.pack == PACKAGE_ALL && o.pack != PACKAGE_ALL) {
                return -1;
            }

            return 0;
        }

    }

}
