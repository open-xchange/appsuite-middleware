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

package com.openexchange.advertisement.impl.services;

import static com.openexchange.java.Autoboxing.I;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.ImmutableJSONArray;
import org.json.ImmutableJSONObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.AdvertisementExceptionCodes;
import com.openexchange.advertisement.ConfigResult;
import com.openexchange.advertisement.impl.osgi.Services;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link AbstractAdvertisementConfigService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public abstract class AbstractAdvertisementConfigService implements AdvertisementConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(AdvertisementConfigService.class);

    /** The cache region name */
    public static final String CACHING_REGION = AbstractAdvertisementConfigService.class.getSimpleName();

    private static final String SQL_INSERT_MAPPING = "REPLACE INTO advertisement_mapping (reseller,package,configId) VALUES (?,?,?);";
    private static final String SQL_INSERT_CONFIG = "INSERT INTO advertisement_config (reseller,config) VALUES (?,?);";
    private static final String SQL_UPDATE_CONFIG = "UPDATE advertisement_config SET config=? where configId=?;";
    private static final String SQL_UPDATE_MAPPING = "UPDATE advertisement_mapping SET configId=? where reseller=? and package=?;";
    private static final String SQL_DELETE_CONFIG = "DELETE FROM advertisement_config where configId=?;";
    private static final String SQL_DELETE_MAPPING = "DELETE FROM advertisement_mapping where configId=?;";
    private static final String SQL_SELECT_MAPPING_SIMPLE = "Select configId from advertisement_mapping where reseller=? AND package=?;";
    private static final String SQL_SELECT_CONFIG = "Select config from advertisement_config where configId=?;";
    private static final String PREVIEW_CONFIG = "com.openexchange.advertisement.preview";

    /**
     * Initializes a new {@link AbstractAdvertisementConfigService}.
     */
    protected AbstractAdvertisementConfigService() {
        super();
    }

    /**
     * Gets the reseller name associated with specified contextId.
     *
     * @param contextId The context identifier
     * @return The reseller name
     * @throws OXException If reseller name cannot be resolved
     */
    protected abstract String getReseller(int contextId) throws OXException;

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
    public JSONValue getConfig(Session session) throws OXException {
        CacheService cacheService = Services.getService(CacheService.class);
        if (cacheService == null) {
            // get config without cache
            return loadInternalConfiguration(session);
        }

        //check user cache first
        Cache cache = cacheService.getCache(CACHING_REGION);
        CacheKey key = cache.newCacheKey(session.getContextId(), session.getUserId());
        Object object = cache.get(key);
        if (object instanceof JSONValue) {
            return (JSONValue) object;
        }

        JSONValue result = getConfigByUserInternal(session);
        if (result != null) {
        	if (result.isObject()) {
        		cache.put(key, ImmutableJSONObject.immutableFor(result.toObject()), false);
        	} else {
        		cache.put(key, ImmutableJSONArray.immutableFor(result.toArray()), false);
        	}
            return result;
        }
        //  ------

        // check normal cache
        String reseller = null;
        String pack = null;
        try {
            reseller = getReseller(session.getContextId());
        } catch (OXException e) {
            LOG.debug("Error while retrieving the reseller for user {} in context {}.", I(session.getUserId()), I(session.getContextId()), e);
        }
        try {
            pack = getPackage(session);
        } catch (OXException e) {
            LOG.debug("Error while retrieving the package for user {} in context {}.", I(session.getUserId()), I(session.getContextId()), e);
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
        if (object instanceof JSONValue) {
            return (JSONValue) object;
        }

        result = getConfigInternal(session, reseller, pack);
        if (result.isObject()) {
    		cache.put(key, ImmutableJSONObject.immutableFor(result.toObject()), false);
    	} else {
    		cache.put(key, ImmutableJSONArray.immutableFor(result.toArray()), false);
    	}
        return result;
    }

    private JSONValue loadInternalConfiguration(Session session) throws OXException {
        JSONValue result = getConfigByUserInternal(session);
        if (result == null) {
            String reseller = null;
            String pack = null;
            try {
                reseller = getReseller(session.getContextId());
            } catch (OXException e) {
                LOG.debug("Error while retrieving the reseller for user {} in context {}.", I(session.getUserId()), I(session.getContextId()), e);
            }
            try {
                pack = getPackage(session);
            } catch (OXException e) {
                LOG.debug("Error while retrieving the package for user {} in context {}.", I(session.getUserId()), I(session.getContextId()), e);
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

    private JSONValue getConfigByUserInternal(Session session) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        String id = view.get(PREVIEW_CONFIG, String.class);
        if (Strings.isNotEmpty(id)) {
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
                	return JSONObject.parse(new StringReader(result.getString(1)));
                }
            } catch (SQLException e) {
                throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
            } catch (JSONException e) {
                LOG.warn("Invalid advertisement configuration data with id {}", I(configId));
                // Invalid advertisement data. Fallback to reseller and package
            } finally {
                Databases.closeSQLStuff(result, stmt);
                dbService.backReadOnly(con);
            }

        }
        return null;
    }

    private JSONValue getConfigInternal(Session session, String reseller, String pack) throws OXException {
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
                    Databases.closeSQLStuff(result, stmt);
                    stmt = con.prepareStatement(SQL_SELECT_CONFIG);
                    stmt.setInt(1, configId);
                    result = stmt.executeQuery();
                    if (result.next()) {
                    	return JSONObject.parse(new StringReader(result.getString(1)));
                    }
                } catch (JSONException e) {
                    LOG.error("Invalid advertisement configuration data for reseller {} and package {}", reseller, pack);
                }
            }

            throw AdvertisementExceptionCodes.CONFIG_NOT_FOUND.create(I(session.getUserId()), I(session.getContextId()));
        } catch (SQLException e) {
            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public ConfigResult setConfigByName(String name, int contextId, String config) {
        try {
            ContextService contextService = Services.getService(ContextService.class);
            Context ctx = contextService.getContext(contextId);
            UserService userService = Services.getService(UserService.class);
            int userId = userService.getUserId(name, ctx);
            return setConfig(userId, contextId, config);
        } catch (OXException e) {
            return new ConfigResult(ConfigResultType.ERROR, e);
        }
    }

    @Override
    public ConfigResult setConfig(int userId, int ctxId, String config) {
        try {
            ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
            ConfigView view = factory.getView(userId, ctxId);
            ConfigProperty<String> property = view.property(ConfigViewScope.USER.getScopeName(), PREVIEW_CONFIG, String.class);

            DatabaseService dbService = Services.getService(DatabaseService.class);
            Connection con = dbService.getWritable();
            PreparedStatement stmt = null;
            ResultSet result = null;
            boolean setProperty = false;
            String newPropertyValue = null;

            ConfigResult configResult = null;
            try {
                if (property.isDefined()) {
                    String value = property.get();
                    int configId = Integer.parseInt(value);
                    if (config == null) {
                        stmt = con.prepareStatement(SQL_DELETE_CONFIG);
                        stmt.setInt(1, configId);
                        stmt.execute();
                        setProperty = true;
                        configResult = new ConfigResult(ConfigResultType.DELETED, null);
                    } else {
                        stmt = con.prepareStatement(SQL_UPDATE_CONFIG);
                        stmt.setString(1, config);
                        stmt.setInt(2, configId);
                        if (stmt.executeUpdate() == 0) {
                            // Nothing to update. Insert new row instead.
                            Databases.closeSQLStuff(stmt);
                            String reseller = null;
                            try {
                                reseller = getReseller(ctxId);
                            } catch (OXException e) {
                                // no reseller found
                            }
                            if (reseller == null) {
                                reseller = RESELLER_ALL;
                            }
                            stmt = con.prepareStatement(SQL_INSERT_CONFIG, Statement.RETURN_GENERATED_KEYS);
                            stmt.setString(1, reseller);
                            stmt.setString(2, config);
                            stmt.execute();
                            result = stmt.getGeneratedKeys();
                            if (!result.next()) {
                                throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create("Insert operation failed to retrieve generated key.");
                            }
                            int resultConfigId = result.getInt(1);
                            setProperty = true;
                            newPropertyValue = String.valueOf(resultConfigId);
                            configResult = new ConfigResult(ConfigResultType.CREATED, null);
                        } else {
                            configResult = new ConfigResult(ConfigResultType.UPDATED, null);
                        }
                    }
                } else {
                    if (config == null) {
                        return new ConfigResult(ConfigResultType.IGNORED, null);
                    }
                    String reseller = null;
                    try {
                        reseller = getReseller(ctxId);
                    } catch (OXException e) {
                        // no reseller found
                    }
                    if (reseller == null) {
                        reseller = RESELLER_ALL;
                    }
                    stmt = con.prepareStatement(SQL_INSERT_CONFIG, Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, reseller);
                    stmt.setString(2, config);
                    stmt.execute();
                    result = stmt.getGeneratedKeys();
                    if (!result.next()) {
                        throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create("Insert operation failed to retrieve generated key.");
                    }
                    int resultConfigId = result.getInt(1);
                    setProperty = true;
                    newPropertyValue = String.valueOf(resultConfigId);
                    configResult = new ConfigResult(ConfigResultType.CREATED, null);
                }
            } catch (SQLException e) {
                return new ConfigResult(ConfigResultType.ERROR, AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage()));
            } finally {
                Databases.closeSQLStuff(result, stmt);
                dbService.backWritable(con);
            }

            if (setProperty) {
                property.set(newPropertyValue);
            }
            //remove entry from cache
            CacheService cacheService = Services.getService(CacheService.class);
            if (cacheService != null) {
                Cache cache = cacheService.getCache(CACHING_REGION);
                cache.remove(cache.newCacheKey(ctxId, userId));
            }
            return configResult;
        } catch (OXException e) {
            return new ConfigResult(ConfigResultType.ERROR, e);
        }
    }

    @Override
    public ConfigResult setConfig(String reseller, String pack, String config) {
        try {
            ConfigResultType type = setConfigInternal(reseller, pack, config);
            return new ConfigResult(type, null);
        } catch (OXException e) {
            return new ConfigResult(ConfigResultType.ERROR, e);
        }
    }

    private ConfigResultType setConfigInternal(String reseller, String pack, String config) throws OXException {
        ConfigResultType status = ConfigResultType.IGNORED;
        String resellerToUse = reseller;
        if (Strings.isEmpty(resellerToUse)) {
            resellerToUse = RESELLER_ALL;
        }
        String packToUse = pack;
        if (Strings.isEmpty(packToUse)) {
            packToUse = PACKAGE_ALL;
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        ResultSet result = null;
        boolean readOnly = false;
        try {

            stmt = con.prepareStatement(SQL_SELECT_MAPPING_SIMPLE);
            stmt.setString(1, resellerToUse);
            stmt.setString(2, packToUse);
            result = stmt.executeQuery();

            if (result.next()) {

                int configId = Integer.parseInt(result.getString(1));
                Databases.closeSQLStuff(result, stmt);
                if (config == null) {
                    stmt = con.prepareStatement(SQL_DELETE_CONFIG);
                    stmt.setInt(1, configId);
                    stmt.execute();
                    Databases.closeSQLStuff(stmt);
                    stmt = con.prepareStatement(SQL_DELETE_MAPPING);
                    stmt.setInt(1, configId);
                    stmt.execute();
                    status = ConfigResultType.DELETED;
                } else {
                    stmt = con.prepareStatement(SQL_UPDATE_CONFIG);
                    stmt.setString(1, config);
                    stmt.setInt(2, configId);
                    if (0 == stmt.executeUpdate()) {
                        // Nothing to update. Create new one instead
                        Databases.closeSQLStuff(stmt);
                        stmt = con.prepareStatement(SQL_INSERT_CONFIG, Statement.RETURN_GENERATED_KEYS);
                        stmt.setString(1, resellerToUse);
                        stmt.setString(2, config);
                        stmt.execute();
                        result = stmt.getGeneratedKeys();
                        if (!result.next()) {
                            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create("Insert operation failed to retrieve generated key.");
                        }
                        int resultConfigId = result.getInt(1);
                        Databases.closeSQLStuff(stmt);
                        stmt = con.prepareStatement(SQL_UPDATE_MAPPING);
                        stmt.setInt(1, resultConfigId);
                        stmt.setString(2, resellerToUse);
                        stmt.setString(3, packToUse);
                        stmt.execute();
                        status = ConfigResultType.CREATED;
                    } else {
                        status = ConfigResultType.UPDATED;
                    }
                }

            } else {
                Databases.closeSQLStuff(result, stmt);
                if (config == null) {
                    readOnly = true;
                    return ConfigResultType.IGNORED;
                }
                stmt = con.prepareStatement(SQL_INSERT_CONFIG, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, resellerToUse);
                stmt.setString(2, config);
                stmt.execute();
                result = stmt.getGeneratedKeys();
                if (!result.next()) {
                    throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create("Insert operation failed to retrieve generated key.");
                }
                int resultConfigId = result.getInt(1);
                Databases.closeSQLStuff(stmt);
                stmt = con.prepareStatement(SQL_INSERT_MAPPING);
                stmt.setString(1, resellerToUse);
                stmt.setString(2, packToUse);
                stmt.setInt(3, resultConfigId);
                stmt.execute();
                status = ConfigResultType.CREATED;
            }

            //clear read cache
            CacheService cacheService = Services.getService(CacheService.class);
            if (cacheService != null) {
                Cache cache = cacheService.getCache(CACHING_REGION);
                cache.remove(cache.newCacheKey(-1, resellerToUse, packToUse));
            }

        } catch (SQLException e) {
            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
            if (readOnly) {
                dbService.backWritableAfterReading(con);
            } else {
                dbService.backWritable(con);
            }
        }
        return status;
    }

    @Override
    public List<ConfigResult> setConfig(String reseller, Map<String, String> configs) {
        List<ConfigResult> resultList = new ArrayList<>();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            try {
                ConfigResultType status = setConfigInternal(reseller, entry.getKey(), entry.getValue());
                resultList.add(new ConfigResult(status, null));
            } catch (OXException e) {
                resultList.add(new ConfigResult(ConfigResultType.ERROR, e));
            }
        }

        return resultList;
    }
}
