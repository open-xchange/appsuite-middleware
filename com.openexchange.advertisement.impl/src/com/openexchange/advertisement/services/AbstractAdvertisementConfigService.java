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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.ImmutableJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.AdvertisementExceptionCodes;
import com.openexchange.advertisement.ConfigResult;
import com.openexchange.advertisement.osgi.Services;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;

/**
 * {@link AbstractAdvertisementConfigService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public abstract class AbstractAdvertisementConfigService implements AdvertisementConfigService {

    private enum Status {
        CREATED, UPDATED, DELETED, IGNORED, ERROR
    }

    private static final Logger LOG = LoggerFactory.getLogger(AdvertisementConfigService.class);

    /** The cache region name */
    public static final String CACHING_REGION = AbstractAdvertisementConfigService.class.getSimpleName();

    private static final String SQL_INSERT_MAPPING = "REPLACE INTO advertisement_mapping (reseller,package,configId) VALUES (?,?,?);";
    private static final String SQL_INSERT_CONFIG = "INSERT INTO advertisement_config (config) VALUES (?);";
    private static final String SQL_UPDATE_CONFIG = "UPDATE advertisement_config SET config=? where configId=?;";
    private static final String SQL_DELETE_CONFIG = "DELETE FROM advertisement_config where configId=?;";
    private static final String SQL_DELETE_MAPPING = "DELETE FROM advertisement_mapping where configId=?;";
    private static final String SQL_SELECT_MAPPING_SIMPLE = "Select configId from advertisement_mapping where reseller=? AND package=?;";
    private static final String SQL_SELECT_CONFIG = "Select config from advertisement_config where configId=?;";
    protected static final String RESELLER_ALL = "OX_ALL";
    protected static final String PACKAGE_ALL = "OX_ALL";
    private static final String PREVIEW_CONFIG = "com.openexchange.advertisement.preview";

    /**
     * Initializes a new {@link AbstractAdvertisementConfigService}.
     */
    protected AbstractAdvertisementConfigService() {
        super();
    }

    /**
     * Gets the reseller name associated with specified session.
     *
     * @param session The session
     * @return The reseller name
     * @throws OXException If reseller name cannot be resolved
     */
    protected abstract String getReseller(Session session) throws OXException;

    /**
     * Gets the package name associated with specified session.
     *
     * @param session The session
     * @return The package name
     * @throws OXException If package name cannot be resolved
     */
    protected abstract String getPackage(Session session) throws OXException;

    @Override
    public boolean isAvailable(Session session) {
        try {
            return getConfig(session) != null;
        } catch (OXException e) {
            return false;
        }
    }

    @Override
    public JSONObject getConfig(Session session) throws OXException {
        CacheService cacheService = Services.getService(CacheService.class);
        if (cacheService == null) {
            // get config without cache
            JSONObject result = getConfigByUserInternal(session);
            if (result == null) {
                String reseller = null;
                String pack = null;
                try {
                    reseller = getReseller(session);
                } catch (OXException e) {
                    LOG.debug("Error while retrieving the reseller for user {} in context {}.", session.getUserId(), session.getContextId(), e);
                }
                try {
                    pack = getPackage(session);
                } catch (OXException e) {
                    LOG.debug("Error while retrieving the package for user {} in context {}.", session.getUserId(), session.getContextId(), e);
                }
                // fallback to defaults in case reseller or package is empty
                if (Strings.isEmpty(reseller)) {
                    reseller = RESELLER_ALL;
                }
                if (Strings.isEmpty(pack)) {
                    pack = PACKAGE_ALL;
                }
                result = getConfigInternal(session, reseller, pack);
            }
            return result;
        }

        //check user cache first
        Cache cache = cacheService.getCache(CACHING_REGION);
        CacheKey key = cache.newCacheKey(session.getContextId(), session.getUserId());
        Object object = cache.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }

        JSONObject result = getConfigByUserInternal(session);
        if (result != null) {
            cache.put(key, ImmutableJSONObject.immutableFor(result), false);
            return result;
        }
        //  ------

        // check normal cache
        String reseller = null;
        String pack = null;
        try {
            reseller = getReseller(session);
        } catch (OXException e) {
            LOG.debug("Error while retrieving the reseller for user {} in context {}.", session.getUserId(), session.getContextId(), e);
        }
        try {
            pack = getPackage(session);
        } catch (OXException e) {
            LOG.debug("Error while retrieving the package for user {} in context {}.", session.getUserId(), session.getContextId(), e);
        }

        // fallback to defaults in case reseller or package is empty
        if (Strings.isEmpty(reseller)) {
            reseller = RESELLER_ALL;
        }
        if (Strings.isEmpty(pack)) {
            pack = PACKAGE_ALL;
        }

        key = cache.newCacheKey(-1, reseller, pack);
        object = cache.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }

        result = getConfigInternal(session, reseller, pack);
        cache.put(key, ImmutableJSONObject.immutableFor(result), false);
        return result;
    }

    private JSONObject getConfigByUserInternal(Session session) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        String id = view.get(PREVIEW_CONFIG, String.class);
        if (!Strings.isEmpty(id)) {
            int configId = Integer.parseInt(id);

            DatabaseService dbService = Services.getService(DatabaseService.class);
            Connection con = dbService.getReadOnly();
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement(SQL_SELECT_CONFIG);
                stmt.setInt(1, configId);
                result = stmt.executeQuery();
                if (result.next()) {
                    return new JSONObject(result.getString(1));
                }
            } catch (SQLException e) {
                throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
            } catch (JSONException e) {
                LOG.warn("Invalid advertisement configuration data with id {}", configId);
                // Invalid advertisement data. Fallback to reseller and package
            } finally {
                DBUtils.closeSQLStuff(result, stmt);
                dbService.backReadOnly(con);
            }

        }
        return null;
    }

    private JSONObject getConfigInternal(Session session, String reseller, String pack) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet result = null;
        StringBuilder sql = new StringBuilder(SQL_SELECT_MAPPING_SIMPLE);
        try {
            int x = 1;
            stmt = con.prepareStatement(sql.toString());
            stmt.setString(x++, reseller);
            stmt.setString(x++, pack);
            result = stmt.executeQuery();
            if (result.next()) {
                try {
                    int configId = result.getInt(1);
                    DBUtils.closeSQLStuff(result, stmt);
                    stmt = con.prepareStatement(SQL_SELECT_CONFIG);
                    stmt.setInt(1, configId);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        return new JSONObject(result.getString(1));
                    }
                } catch (JSONException e) {
                    LOG.error("Invalid advertisement configuration data for reseller {} and package {}", reseller, pack);
                }
            }

            throw AdvertisementExceptionCodes.CONFIG_NOT_FOUND.create(session.getUserId(), session.getContextId());
        } catch (SQLException e) {
            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public void setConfigByName(String name, int contextId, String config) throws OXException {
        ContextService contextService = Services.getService(ContextService.class);
        Context ctx = contextService.getContext(contextId);
        UserService userService = Services.getService(UserService.class);
        int userId = userService.getUserId(name, ctx);
        setConfig(userId, contextId, config);
    }

    @Override
    public void setConfig(int userId, int ctxId, String config) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(userId, ctxId);
        ConfigProperty<String> property = view.property("user", PREVIEW_CONFIG, String.class);

        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            if (property.isDefined()) {
                String value = property.get();
                int configId = Integer.parseInt(value);
                if (config == null) {
                    stmt = con.prepareStatement(SQL_DELETE_CONFIG);
                    stmt.setInt(1, configId);
                    stmt.execute();
                    property.set(null);
                } else {
                    stmt = con.prepareStatement(SQL_UPDATE_CONFIG);
                    stmt.setString(1, config);
                    stmt.setInt(2, configId);
                    stmt.execute();
                }
            } else {
                if (config == null) {
                    return;
                }
                stmt = con.prepareStatement(SQL_INSERT_CONFIG, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, config);
                stmt.execute();
                result = stmt.getGeneratedKeys();
                if (!result.next()) {
                    throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create("Insert operation failed to retrieve generated key.");
                }
                int resultConfigId = result.getInt(1);
                property.set(String.valueOf(resultConfigId));
            }
            //remove entry from cache
            CacheService cacheService = Services.getService(CacheService.class);
            if (cacheService != null) {
                Cache cache = cacheService.getCache(CACHING_REGION);
                cache.remove(cache.newCacheKey(ctxId, userId));
            }
        } catch (SQLException e) {
            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            dbService.backWritable(con);
        }
    }

    @Override
    public void setConfig(String reseller, String pack, String config) throws OXException {
        setConfigInternal(reseller, pack, config);
    }

    private Status setConfigInternal(String reseller, String pack, String config) throws OXException {
        Status status = Status.IGNORED;
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
        Boolean readOnly = false;
        try {

            stmt = con.prepareStatement(SQL_SELECT_MAPPING_SIMPLE);
            stmt.setString(1, reseller);
            stmt.setString(2, pack);
            result = stmt.executeQuery();

            if (result.next()) {

                int configId = Integer.valueOf(result.getString(1));
                DBUtils.closeSQLStuff(result, stmt);
                if (config == null) {
                    stmt = con.prepareStatement(SQL_DELETE_CONFIG);
                    stmt.setInt(1, configId);
                    stmt.execute();
                    DBUtils.closeSQLStuff(stmt);
                    stmt = con.prepareStatement(SQL_DELETE_MAPPING);
                    stmt.setInt(1, configId);
                    stmt.execute();
                    status = Status.DELETED;
                } else {
                    stmt = con.prepareStatement(SQL_UPDATE_CONFIG);
                    stmt.setString(1, config);
                    stmt.setInt(2, configId);
                    stmt.execute();
                    status = Status.UPDATED;
                }

            } else {
                DBUtils.closeSQLStuff(result, stmt);
                if (config == null) {
                    readOnly = true;
                    return Status.IGNORED;
                }
                stmt = con.prepareStatement(SQL_INSERT_CONFIG, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, config);
                stmt.execute();
                result = stmt.getGeneratedKeys();
                if (!result.next()) {
                    throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create("Insert operation failed to retrieve generated key.");
                }
                int resultConfigId = result.getInt(1);
                DBUtils.closeSQLStuff(stmt);
                stmt = con.prepareStatement(SQL_INSERT_MAPPING);
                stmt.setString(1, reseller);
                stmt.setString(2, pack);
                stmt.setInt(3, resultConfigId);
                stmt.execute();
                status = Status.CREATED;
            }

            //clear read cache
            CacheService cacheService = Services.getService(CacheService.class);
            if (cacheService != null) {
                Cache cache = cacheService.getCache(CACHING_REGION);
                cache.remove(cache.newCacheKey(-1, reseller, pack));
            }

        } catch (SQLException e) {
            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
            if (readOnly) {
                dbService.backWritableAfterReading(con);
            } else {
                dbService.backWritable(con);
            }
        }
        return status;
    }

    @Override
    public List<ConfigResult> setConfig(String reseller, String configs) throws OXException {
        Map<String, String> data;
        //Parse configs String
        try {
            JSONArray array = new JSONArray(configs);
            if (array.isEmpty()) {
                return Collections.emptyList();
            }
            data = new HashMap<>(array.length());

            for (Object value : array.asList()) {
                if (!(value instanceof HashMap)) {
                    throw new JSONException("Child is not a JSONObject.");
                }
                @SuppressWarnings("unchecked") 
                HashMap<String, String> obj = (HashMap<String, String>) value;
                String pack = obj.get("package");
                String config = obj.get("config") == JSONObject.NULL ? null : (String) obj.get("config");
                data.put(pack, config);
            }
        } catch (JSONException e) {
            throw AdvertisementExceptionCodes.PARSING_ERROR.create(e.getMessage());
        }
        
        
        List<ConfigResult> resultList = new ArrayList<>();
        for (String pack : data.keySet()) {
            try {
                Status status = setConfigInternal(reseller, pack, data.get(pack));
                resultList.add(new ConfigResult(status.name(), null));
            } catch (OXException e) {
                resultList.add(new ConfigResult(Status.ERROR.name(), e));
            }
        }

        return resultList;
    }
}
