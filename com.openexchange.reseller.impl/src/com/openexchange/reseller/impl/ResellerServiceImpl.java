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

package com.openexchange.reseller.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerExceptionCodes;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.reseller.data.ResellerAdmin.ResellerAdminBuilder;
import com.openexchange.reseller.data.ResellerCapability;
import com.openexchange.reseller.data.ResellerConfigProperty;
import com.openexchange.reseller.data.ResellerTaxonomy;
import com.openexchange.reseller.data.Restriction;

/**
 * {@link ResellerServiceImpl} - The reseller service implementation.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class ResellerServiceImpl implements ResellerService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResellerServiceImpl.class);
    }

    private final DatabaseService dbService;

    /**
     * Initializes a new {@link ResellerServiceImpl}.
     *
     * @param databaseService The database service to use
     */
    public ResellerServiceImpl(DatabaseService databaseService) {
        super();
        dbService = databaseService;
    }

    @Override
    public ResellerAdmin getReseller(int cid) throws OXException {
        return getResellerAdmin(cid, null);
    }

    @Override
    public ResellerAdmin getResellerByName(String resellerName) throws OXException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        Connection con = dbService.getReadOnly();
        try {
            prep = con.prepareStatement("SELECT sid FROM subadmin WHERE name=?");
            prep.setString(1, resellerName);
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw ResellerExceptionCodes.NO_RESELLER_WITH_NAME_FOUND.create(resellerName);
            }
            return getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), con);
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public ResellerAdmin getResellerById(int resellerId) throws OXException {
        return getResellerById(resellerId, null);
    }

    /**
     * Retrieves the reseller administrator path for the specified context.
     * <p>
     * First in list is root reseller administrator, last one in list is the reseller administrator for given context.
     *
     * @param contextId The context identifier
     * @return An <b>immutable list</b> with the path of the reseller sub-administrators
     * @throws OXException If reseller administrator path cannot be returned
     */
    @Override
    public List<ResellerAdmin> getResellerAdminPath(int cid) throws OXException {
        return getResellerAdminPath(cid, null);
    }

    /**
     * Retrieves all reseller sub-administrators for the specified parent reseller administrator.
     *
     * @param parentId The parent identifier
     * @return An <b>immutable list</b> with all reseller sub-administrators
     * @throws OXException If sub-administrator cannot be returned
     */
    @Override
    public List<ResellerAdmin> getSubResellers(int parentId) throws OXException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        Connection con = dbService.getReadOnly();
        try {
            prep = con.prepareStatement("SELECT sid FROM subadmin WHERE pid=?");
            prep.setInt(1, parentId);
            rs = prep.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            ImmutableList.Builder<ResellerAdmin> subadmins = ImmutableList.builder();
            do {
                subadmins.addAll(getData(ImmutableList.of(ResellerAdmin.builder().id(I(rs.getInt(1))).build()), con));
            } while (rs.next());
            return subadmins.build();
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    /**
     * Retrieves all reseller administrators.
     *
     * @return The reseller administrators as an <b>immutable list</b>
     * @throws OXException If reseller administrators cannot be returned
     */
    @Override
    public List<ResellerAdmin> getAll() throws OXException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        Connection con = dbService.getReadOnly();
        try {
            prep = con.prepareStatement("SELECT sid, pid, name, displayName, password, passwordMech, salt FROM subadmin");
            rs = prep.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            ImmutableList.Builder<ResellerAdmin> ret = ImmutableList.builder();
            do {
                ret.add(parseResellerAdminBuilder(rs).build());
            } while (rs.next());
            return ret.build();
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Retrieves all capabilities for the reseller with the specified identifier
     *
     * @param resellerId The reseller identifier
     * @return The capabilities as an <b>immutable set</b>
     * @throws OXException If an error is occurred
     */
    @Override
    public Set<ResellerCapability> getCapabilities(int resellerId) throws OXException {
        return getCapabilitiesByReseller(resellerId, null);
    }

    /**
     * Retrieves all capabilities for the context with the specified identifier
     * by traversing up the reseller admin path and merging all capabilities
     * from all resellers in that path.
     *
     * @param contextId The context identifier
     * @return The capabilities as an <b>immutable set</b>
     * @throws OXException If an error is occurred
     */
    @Override
    public Set<ResellerCapability> getCapabilitiesByContext(int contextId) throws OXException {
        return getCapabilitiesByContext(contextId, null);
    }

    @Override
    public ResellerConfigProperty getConfigProperty(int resellerId, String key) throws OXException {
        return getConfigPropertyByReseller(resellerId, key, null);
    }

    @Override
    public ResellerConfigProperty getConfigPropertyByContext(int contextId, String key) throws OXException {
        return getConfigPropertyByContext(contextId, key, null);
    }

    /**
     * Retrieves all configuration properties for the specified reseller
     *
     * @param resellerId The reseller identifier
     * @return An <b>immutable map</b> with all configuration properties
     * @throws OXException If an error is occurred
     */
    @Override
    public Map<String, ResellerConfigProperty> getAllConfigProperties(int resellerId) throws OXException {
        return getAllConfigPropertiesByReseller(resellerId, null);
    }

    /**
     * Retrieves all configuration properties for the specified context
     * by traversing up the reseller admin path and fetching the all properties found
     * in the reseller path. The root reseller has lowest priority, while the leaf reseller
     * the highest.
     *
     * @param contextId The context identifier
     * @return An <b>immutable map</b> with all configuration properties
     * @throws OXException If an error is occurred
     */
    @Override
    public Map<String, ResellerConfigProperty> getAllConfigPropertiesByContext(int contextId) throws OXException {
        return getAllConfigPropertiesByContext(contextId, null);
    }

    /**
     * Retrieves the specified configuration properties for the specified reseller
     *
     * @param resellerId The reseller identifier
     * @param keys A set of property keys
     * @return An <b>immutable map</b> with the specified configuration properties
     * @throws OXException If an error is occurred
     */
    @Override
    public Map<String, ResellerConfigProperty> getConfigProperties(int resellerId, Set<String> keys) throws OXException {
        return getConfigPropertiesByReseller(resellerId, keys, null);
    }

    /**
     * Retrieves the specified configuration properties for the specified context
     * by traversing up the reseller admin path and fetching the all properties found
     * in the reseller path. The root reseller has lowest priority, while the leaf reseller
     * the highest.
     *
     * @param contextId The context identifier
     * @param keys A set of property keys
     * @return An <b>immutable map</b> with the specified configuration properties
     * @throws OXException If an error is occurred
     */
    @Override
    public Map<String, ResellerConfigProperty> getConfigPropertiesByContext(int contextId, Set<String> keys) throws OXException {
        return getConfigPropertiesByContext(contextId, keys, null);
    }

    /**
     * Retrieves all taxonomies for the specified reseller
     *
     * @param resellerId The reseller identifier
     * @return An <b>immutable set</b> with all taxonomies
     * @throws OXException If an error is occurred
     */
    @Override
    public Set<ResellerTaxonomy> getTaxonomies(int resellerId) throws OXException {
        return getTaxonomiesByReseller(resellerId, null);
    }

    /**
     * Retrieves all taxonomies for the specified context by traversing up the reseller admin path
     * and fetching and merging all taxonomies found in the reseller path.
     *
     * @param contextId The context identifier
     * @return An <b>immutable set</b> with all taxonomies
     * @throws OXException If an error is occurred
     */
    @Override
    public Set<ResellerTaxonomy> getTaxonomiesByContext(int contextId) throws OXException {
        return getTaxonomiesByContext(contextId, null);
    }

    //----------------------------------------------------- HELPERS ------------------------------------------------------------------------

    /**
     * Optionally gets the reseller admin path. If no reseller exists for the specified
     * context, then an empty list will be returned.
     *
     * @param cid The context identifier
     * @param connection The connection
     * @return A List with the path
     * @throws OXException If an error is occurred
     */
    private List<ResellerAdmin> optResellerAdminPath(int cid, Connection connection) throws OXException {
        return getResellerAdminPath(cid, connection, false);
    }

    /**
     * Gets the reseller admin path. If no reseller exists for the specified
     * context an exception will be thrown
     *
     * @param cid The context identifier
     * @param connection The connection
     * @return A List with the path
     * @throws OXException If an error is occurred
     */
    private List<ResellerAdmin> getResellerAdminPath(int cid, Connection connection) throws OXException {
        return getResellerAdminPath(cid, connection, true);
    }

    /**
     * Retrieves the reseller admin path
     *
     * @param cid The context identifier
     * @param connection The connection
     * @param throwEx Whether to throw an exception if the the specified context has no reseller
     * @return A List with the path
     * @throws OXException If an error is occurred
     */
    private List<ResellerAdmin> getResellerAdminPath(int cid, Connection connection, boolean throwEx) throws OXException {
        boolean connectionInit = false;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInit = true;
            }
            prep = connection.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, cid);
            rs = prep.executeQuery();
            if (rs.next()) {
                return renderPath(connection, rs);
            }
            if (throwEx) {
                throw ResellerExceptionCodes.NO_RESELLER_FOUND_FOR_CTX.create(Integer.valueOf(cid));
            }
            return ImmutableList.of();
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            if (connectionInit) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Renders the reseller admin path by creating a list with resellers. The first
     * element of the list is the root reseller while the last element is the leaf.
     *
     * @param connection The connection
     * @param rs The result set
     * @return A List with the path
     * @throws SQLException If an SQL error is occurred
     * @throws OXException If an error is occurred
     */
    private List<ResellerAdmin> renderPath(Connection connection, ResultSet rs) throws SQLException, OXException {
        ResellerAdmin admin = getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), connection);
        if (admin.getParentId() == null || admin.getParentId().intValue() == 0) {
            return ImmutableList.of(admin);
        }

        List<ResellerAdmin> path = new ArrayList<>(4);
        path.add(admin);
        do {
            admin = getData(ResellerAdmin.builder().id(admin.getParentId()).build(), connection);
            path.add(admin);
        } while (admin.getParentId() != null && admin.getParentId().intValue() != 0);
        Collections.reverse(path);
        return path;
    }

    /**
     *
     * Optionally retrieves the reseller admin for the specified context identifier
     *
     * @param contextId The context identifier
     * @param connection The optional connection
     * @return The reseller admin or <code>null</code> if no admin exists
     * @throws OXException If an error is occurred
     */
    public ResellerAdmin optResellerAdmin(int contextId, Connection connection) throws OXException {
        return getResellerAdmin(contextId, connection, false);
    }

    /**
     *
     * Retrieves the reseller admin for the specified context identifier
     *
     * @param contextId The context identifier
     * @param connection The optional connection
     * @return The reseller admin
     * @throws OXException If an error is occurred
     */
    private ResellerAdmin getResellerAdmin(int contextId, Connection connection) throws OXException {
        return getResellerAdmin(contextId, connection, true);
    }

    /**
     * Retrieves the reseller admin
     *
     * @param contextId The context identifier
     * @param connection The optional connection
     * @param throwEx Whether to throw an exception if the reseller admin does not exist
     * @return The reseller admin or <code>null</code> (dictated by the throwEx parameter)
     * @throws OXException If an error is occurred
     */
    private ResellerAdmin getResellerAdmin(int contextId, Connection connection, boolean throwEx) throws OXException {
        boolean connectionInit = false;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInit = true;
            }
            prep = connection.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, contextId);
            rs = prep.executeQuery();
            if (rs.next()) {
                return getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), connection);
            }
            if (throwEx) {
                throw ResellerExceptionCodes.NO_RESELLER_FOUND_FOR_CTX.create(Integer.valueOf(contextId));
            }
            return null;
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            if (connectionInit) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Gets the reseller admin by identifier.
     *
     * @param resellerId The reseller identifier
     * @param connection The connection
     * @return The reseller admin
     * @throws OXException If an error is occurred
     */
    private ResellerAdmin getResellerById(int resellerId, Connection connection) throws OXException {
        return getResellerById(resellerId, connection, true);
    }

    /**
     * Gets the reseller admin by identifier.
     *
     * @param resellerId The reseller identifier
     * @param connection The connection
     * @param throwEx Whether to throw an exception if no reseller exists.
     * @return The reseller admin or <code>null</code> (controlled by throwEx)
     * @throws OXException If an error is occurred
     */
    private ResellerAdmin getResellerById(int resellerId, Connection connection, boolean throwEx) throws OXException {
        boolean connectionInit = false;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInit = true;
            }
            prep = connection.prepareStatement("SELECT sid FROM subadmin WHERE sid=?");
            prep.setInt(1, resellerId);
            rs = prep.executeQuery();
            if (rs.next()) {
                return getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), connection);
            }
            if (throwEx) {
                throw ResellerExceptionCodes.NO_RESELLER_FOUND.create(Integer.valueOf(resellerId));
            }
            return null;
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            if (connectionInit) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves the metadata for the specified {@link ResellerAdmin}
     *
     * @param admin The {@link ResellerAdmin}
     * @param con The optional {@link ConnectException}
     * @return The {@link ResellerAdmin} metadata
     * @throws SQLException If an SQL error is occurred
     * @throws OXException If an OX error is occurred
     */
    private ResellerAdmin getData(ResellerAdmin admin, Connection connection) throws SQLException, OXException {
        return getData(ImmutableList.of(admin), connection).get(0);
    }

    /**
     * Retrieves the metadata for the specified {@link ResellerAdmin}s
     *
     * @param admins The {@link ResellerAdmin}s
     * @param con The optional {@link ConnectException}
     * @return The {@link ResellerAdmin} metadata
     * @throws SQLException If an SQL error is occurred
     * @throws OXException If an OX error is occurred
     */
    private List<ResellerAdmin> getData(List<ResellerAdmin> admins, Connection con) throws SQLException, OXException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean connectionInit = false;
        try {
            if (con == null) {
                con = dbService.getReadOnly();
                connectionInit = true;
            }
            List<ResellerAdmin> ret = new ArrayList<>(admins.size());
            for (ResellerAdmin adm : admins) {
                prep = con.prepareStatement("SELECT sid, pid, name, displayName, password, passwordMech, salt FROM subadmin WHERE sid=?");
                prep.setInt(1, adm.getId().intValue());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw ResellerExceptionCodes.NO_RESELLER_FOUND.create(adm.getId());
                }

                Integer id = Integer.valueOf(rs.getInt("sid"));
                Integer parentId = Integer.valueOf(rs.getInt("pid"));
                ResellerAdminBuilder builder = parseResellerAdminBuilder(rs);
                Databases.closeSQLStuff(rs, prep);
                rs = null;
                prep = null;

                List<Restriction> restrictions = getRestrictionDataForAdmin(id, parentId, con);
                if (false == restrictions.isEmpty()) {
                    builder.restrictions(restrictions);
                }
                Set<ResellerCapability> capabilities = getCapabilitiesByReseller(i(id), con);
                if (false == capabilities.isEmpty()) {
                    builder.capabilities(capabilities);
                }
                Set<ResellerTaxonomy> taxonomies = getTaxonomiesByReseller(i(id), con);
                if (false == taxonomies.isEmpty()) {
                    builder.taxonomies(taxonomies);
                }
                Map<String, ResellerConfigProperty> configuration = getAllConfigPropertiesByReseller(i(id), con);
                if (false == configuration.isEmpty()) {
                    builder.configuration(configuration);
                }
                ret.add(builder.build());
            }
            return ret;
        } finally {
            Databases.closeSQLStuff(rs, prep);
            if (connectionInit) {
                dbService.backReadOnly(con);
            }
        }
    }

    /**
     * Retrieves the subadmin restrictions for the {@link ResellerAdmin} with the specified identifier.
     *
     * @param id The reseller identifier
     * @param parentId The parent of the reseller
     * @param con The {@link Connection}
     * @return The restrictions
     * @throws SQLException If an SQL error is occurred
     */
    private List<Restriction> getRestrictionDataForAdmin(Integer id, Integer parentId, Connection con) throws SQLException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT subadmin_restrictions.rid,sid,name,value FROM subadmin_restrictions INNER JOIN restrictions ON subadmin_restrictions.rid=restrictions.rid WHERE sid=?");
            prep.setInt(1, i(i(parentId) > 0 ? parentId : id));
            rs = prep.executeQuery();

            ImmutableList.Builder<Restriction> res = null;
            while (rs.next()) {
                Restriction r = parseRestriction(rs);
                if (i(parentId) > 0 && Restriction.SUBADMIN_CAN_CREATE_SUBADMINS.equals(r.getName())) {
                    continue;
                }
                if (res == null) {
                    res = ImmutableList.builder();
                }
                res.add(r);
            }
            return res == null ? ImmutableList.of() : res.build();
        } finally {
            Databases.closeSQLStuff(rs, prep);
        }
    }

    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_RID = "rid";

    /**
     * Parses the specified {@link ResultSet} to a {@link Restriction}.
     *
     * @param resultSet The {@link ResultSet} to parse
     * @return The {@link Restriction}
     * @throws SQLException If an SQL error is occurred
     */
    private Restriction parseRestriction(ResultSet resultSet) throws SQLException {
        return new Restriction(Integer.valueOf(resultSet.getInt(COLUMN_RID)), resultSet.getString(COLUMN_NAME), resultSet.getString(COLUMN_VALUE));
    }

    /**
     * Parses the specified {@link ResultSet} to a {@link ResellerAdminBuilder}.
     *
     * @param resultSet The {@link ResultSet} to parse
     * @return The {@link ResellerAdminBuilder}
     * @throws SQLException If an SQL error is occurred
     */
    private ResellerAdminBuilder parseResellerAdminBuilder(ResultSet resultSet) throws SQLException {
        ResellerAdminBuilder builder = ResellerAdmin.builder().id(Integer.valueOf(resultSet.getInt("sid")));
        builder.name(resultSet.getString(COLUMN_NAME));
        builder.parentId(Integer.valueOf(resultSet.getInt("pid")));
        builder.displayname(resultSet.getString("displayName"));
        builder.password(resultSet.getString("password"));
        builder.passwordMech(resultSet.getString("passwordMech"));
        builder.salt(resultSet.getString("salt"));
        return builder;
    }

    /**
     * Retrieves the capabilities for the specified context. Traverses the admin path upwards
     * to locate and merge all stored capabilities.
     *
     * @param contextId The context identifier
     * @param connection The optional connection
     * @return The capabilities
     * @throws OXException If an error is occurred
     */
    private Set<ResellerCapability> getCapabilitiesByContext(int contextId, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            List<ResellerAdmin> path = optResellerAdminPath(contextId, connection);
            if (path.isEmpty()) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<ResellerCapability> capabilities = ImmutableSet.builder();
            for (ResellerAdmin admin : path) {
                capabilities.addAll(getCapabilitiesByReseller(admin.getId().intValue(), connection));
            }
            return capabilities.build();
        } finally {
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Returns all capabilities for the specified reseller.
     *
     * @param resellerId The reseller identifier
     * @param connection The optional database connection
     * @return A Set with all capabilities for the specified reseller
     * @throws OXException If an error is occurred
     */
    private Set<ResellerCapability> getCapabilitiesByReseller(int resellerId, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            stmt = connection.prepareStatement("SELECT capability FROM subadmin_capabilities WHERE sid=?");
            stmt.setLong(1, resellerId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<ResellerCapability> capas = ImmutableSet.builder();
            do {
                capas.add(new ResellerCapability(rs.getString(1), resellerId));
            } while (rs.next());
            return capas.build();
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw new OXException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves the value of the specified property.
     *
     * @param resellerId The reseller identifier
     * @param key The key of the property
     * @param connection the optional database connection
     * @return The value of the property if it exists; <code>null</code> otherwise
     * @throws OXException If an error is occurred
     */
    private ResellerConfigProperty getConfigPropertyByReseller(int resellerId, String key, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            stmt = connection.prepareStatement("SELECT propertyValue FROM subadmin_config_properties WHERE sid = ? AND propertyKey = ?");
            stmt.setInt(1, resellerId);
            stmt.setString(2, key);
            rs = stmt.executeQuery();
            return rs.next() ? new ResellerConfigProperty(key, rs.getString(1), resellerId) : null;
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw new OXException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves the value of the specified property.
     *
     * @param contextId The reseller identifier
     * @param key The key of the property
     * @param connection the optional database connection
     * @return The value of the property if it exists; <code>null</code> otherwise
     * @throws OXException If an error is occurred
     */
    private ResellerConfigProperty getConfigPropertyByContext(int contextId, String key, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            List<ResellerAdmin> path = optResellerAdminPath(contextId, connection);
            if (path.isEmpty()) {
                return null;
            }
            ResellerConfigProperty propertyValue = null;
            for (ResellerAdmin admin : path) {
                ResellerConfigProperty candidate = getConfigPropertyByReseller(admin.getId().intValue(), key, connection);
                if (candidate == null) {
                    continue;
                }
                propertyValue = candidate;
            }
            return propertyValue;
        } finally {
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves all configuration properties for the specified reseller only, i.e. no up traversal.
     *
     * @param resellerId The reseller identifier
     * @param connection The connection
     * @return A Map with all configured properties
     * @throws OXException If an error is occurred
     */
    private Map<String, ResellerConfigProperty> getAllConfigPropertiesByReseller(int resellerId, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            stmt = connection.prepareStatement("SELECT propertyKey, propertyValue FROM subadmin_config_properties WHERE sid = ?");
            stmt.setInt(1, resellerId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyMap();
            }

            ImmutableMap.Builder<String, ResellerConfigProperty> props = ImmutableMap.builder();
            do {
                props.put(rs.getString(1), new ResellerConfigProperty(rs.getString(1), rs.getString(2), resellerId));
            }  while (rs.next());
            return props.build();
        } catch (SQLException e) {
            LoggerHolder.LOG.error("SQL Error", e);
            throw new OXException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves all configuration properties for the specified context by traversing up
     * the admin path and fetching all configuration properties from all resellers in the
     * path.
     *
     * @param contextId The context identifier
     * @param connection The connection
     * @return A Map with all configured properties
     * @throws OXException If an error is occurred
     */
    private Map<String, ResellerConfigProperty> getAllConfigPropertiesByContext(int contextId, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            List<ResellerAdmin> path = optResellerAdminPath(contextId, connection);
            if (path.isEmpty()) {
                return ImmutableMap.of();
            }
            ImmutableMap.Builder<String, ResellerConfigProperty> props = ImmutableMap.builder();
            for (ResellerAdmin admin : path) {
                props.putAll(getAllConfigPropertiesByReseller(admin.getId().intValue(), connection));
            }
            return props.build();
        } finally {
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves the specified configuration properties for the specified reseller only, i.e. no up traversal.
     *
     * @param resellerId The reseller identifier
     * @param keys The properties to get
     * @param connection The connection
     * @return A Map with the specified configured properties
     * @throws OXException If an error is occurred
     */
    private Map<String, ResellerConfigProperty> getConfigPropertiesByReseller(int resellerId, Set<String> keys, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            stmt = connection.prepareStatement(Databases.getIN("SELECT propertyKey, propertyValue FROM subadmin_config_properties WHERE sid = ? AND propertyKey IN (", keys.size()));
            int pIndex = 1;
            stmt.setInt(pIndex++, resellerId);
            for (String key : keys) {
                stmt.setString(pIndex++, key);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return ImmutableMap.of();
            }

            ImmutableMap.Builder<String, ResellerConfigProperty> props = ImmutableMap.builder();
            do {
                props.put(rs.getString(1), new ResellerConfigProperty(rs.getString(1), rs.getString(2), resellerId));
            } while (rs.next());
            return props.build();
        } catch (SQLException e) {
            LoggerHolder.LOG.error("SQL Error", e);
            throw new OXException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves the specified configuration properties for the specified context by traversing up
     * the admin path and fetching the specified configuration properties from all resellers in the
     * path.
     *
     * @param resellerId The reseller identifier
     * @param keys The properties to get
     * @param connection The connection
     * @return A Map with the specified configured properties
     * @throws OXException If an error is occurred
     */
    private Map<String, ResellerConfigProperty> getConfigPropertiesByContext(int contextId, Set<String> keys, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            List<ResellerAdmin> path = optResellerAdminPath(contextId, connection);
            if (path.isEmpty()) {
                return ImmutableMap.of();
            }
            ImmutableMap.Builder<String, ResellerConfigProperty> props = ImmutableMap.builder();
            for (ResellerAdmin admin : path) {
                props.putAll(getConfigPropertiesByReseller(admin.getId().intValue(), keys, connection));
            }
            return props.build();
        } finally {
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves all taxonomies for the specified reseller only, i.e. no up traversal.
     *
     * @param resellerId The reseller identifier
     * @param connection The connection
     * @return A Set with all taxonomies for the reseller
     * @throws OXException If an error is occurred
     */
    private Set<ResellerTaxonomy> getTaxonomiesByReseller(int resellerId, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            stmt = connection.prepareStatement("SELECT taxonomy FROM subadmin_taxonomies WHERE sid=?");
            stmt.setLong(1, resellerId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<ResellerTaxonomy> taxonomies = ImmutableSet.builder();
            do {
                taxonomies.add(new ResellerTaxonomy(rs.getString(1), resellerId));
            } while (rs.next());
            return taxonomies.build();
        } catch (SQLException e) {
            LoggerHolder.LOG.error("", e);
            throw new OXException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

    /**
     * Retrieves all taxonomies for the specified context by traversing up
     * the admin path and fetching all taxonomies from all resellers in the
     * path.
     *
     * @param resellerId The reseller identifier
     * @param connection The connection
     * @return A Set with all taxonomies for the reseller
     * @throws OXException If an error is occurred
     */
    private Set<ResellerTaxonomy> getTaxonomiesByContext(int contextId, Connection connection) throws OXException {
        boolean connectionInitialised = false;
        try {
            if (connection == null) {
                connection = dbService.getReadOnly();
                connectionInitialised = true;
            }
            List<ResellerAdmin> path = optResellerAdminPath(contextId, connection);
            if (path.isEmpty()) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<ResellerTaxonomy> taxonomies = ImmutableSet.builder();
            for (ResellerAdmin admin : path) {
                taxonomies.addAll(getTaxonomiesByReseller(admin.getId().intValue(), connection));
            }
            return taxonomies.build();
        } finally {
            if (connectionInitialised) {
                dbService.backReadOnly(connection);
            }
        }
    }

}
