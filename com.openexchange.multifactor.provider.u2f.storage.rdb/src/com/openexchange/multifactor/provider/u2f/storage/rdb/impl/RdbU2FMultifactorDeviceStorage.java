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

package com.openexchange.multifactor.provider.u2f.storage.rdb.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.multifactor.provider.u2f.impl.U2FMultifactorDevice;
import com.openexchange.multifactor.provider.u2f.storage.U2FMultifactorDeviceStorage;
import com.openexchange.multifactor.storage.impl.MultifactorStorageCommon;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbU2FMultifactorDeviceStorage}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class RdbU2FMultifactorDeviceStorage extends MultifactorStorageCommon implements U2FMultifactorDeviceStorage {

    private static final String INSERT_DEVICE = "INSERT INTO multifactor_u2f_device (id, name, cid, user, enabled, keyHandle, publicKey, attestationCertificate, counter, compromised) VALUES (UNHEX(?),?,?,?,?,?,?,?,?,?)";
    private static final String DELETE_FOR_USER = "DELETE FROM multifactor_u2f_device WHERE cid=? AND user=? AND id=UNHEX(?)";
    private static final String GET_BY_ID = "SELECT LOWER(HEX(id)) as id, name, user, cid, enabled, keyHandle, publicKey, attestationCertificate, counter, compromised FROM multifactor_u2f_device WHERE cid=? AND user=? AND id=UNHEX(?)";
    private static final String GET_BY_USER = "SELECT LOWER(HEX(id)) as id, name, user, cid, enabled, keyHandle, publicKey, attestationCertificate, counter, compromised FROM multifactor_u2f_device WHERE cid=? AND user=? ";
    private static final String INCREMENT_COUNTER = "UPDATE multifactor_u2f_device SET counter = GREATEST(counter + 1, ?) WHERE cid=? AND user=? AND id=UNHEX(?)";
    private static final String UPDATE_NAME = "UPDATE multifactor_u2f_device SET name = ? WHERE cid=? AND user=? AND id=UNHEX(?)";
    private static final String GET_COUNT = "SELECT COUNT(*) FROM multifactor_u2f_device WHERE cid=? AND user=?";
    private static final String DELETE_ALL_FOR_USER = "DELETE FROM multifactor_u2f_device WHERE cid=? AND user=?";
    private static final String DELETE_FOR_CONTEXT = "DELETE FROM multifactor_u2f_device WHERE cid=?";

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link RdbU2FMultifactorDeviceStorage}.
     *
     * @param rdbTotpMultifactorStorageActivator
     */
    public RdbU2FMultifactorDeviceStorage(ServiceLookup serviceLookup) {
        this.serviceLookup = Objects.requireNonNull(serviceLookup, "seviceLookup must not be null");
    }

    private DatabaseService getDatabaseService() throws OXException {
        return serviceLookup.getServiceSafe(DatabaseService.class);
    }

    /**
     * Create a U2F device from database result set
     *
     * @param resultSet The {@link ResultSet}
     * @return A {@link U2FMultifactorDevice}
     * @throws SQLException in case a {@link U2FMultifactorDevice} couldn't be created from the given {@link ResultSet}
     */
    private U2FMultifactorDevice createDeviceFrom(ResultSet resultSet) throws SQLException {
        final U2FMultifactorDevice device = new U2FMultifactorDevice(
            resultSet.getObject("id").toString(),
            resultSet.getObject("keyHandle").toString(),
            resultSet.getObject("attestationCertificate").toString(),
            resultSet.getObject("publicKey").toString(),
            resultSet.getInt("counter"),
            resultSet.getBoolean("compromised"));
        device.enable(resultSet.getBoolean("enabled") ? Boolean.TRUE : Boolean.FALSE);
        device.setName(resultSet.getString("name"));
        return device;
    }

    /**
     * Creates a collection of {@link U2FMultifactorDevice}s from the given {@link ResultSet}
     *
     * @param executeQuery The {@link ResultSet}
     * @return A {@link Collection} of {@link U2FMultifactorDevice}s
     * @throws SQLException if the {@link Collection} couldn't be created from the given {@link ResultSet}
     */
    private Collection<U2FMultifactorDevice> createDevicesFrom(ResultSet executeQuery) throws SQLException {
        final Collection<U2FMultifactorDevice> ret = new ArrayList<U2FMultifactorDevice>();
        while (executeQuery.next()) {
            ret.add(createDeviceFrom(executeQuery));
        }
        return ret;
    }

    @Override
    public void registerDevice(int contextId, int userId, U2FMultifactorDevice device) throws OXException {
        final DatabaseService dbs = getDatabaseService();
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(INSERT_DEVICE);
            int index = 1;
            statement.setString(index++, device.getId());
            statement.setString(index++, device.getName());
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setBoolean(index++, device.isEnabled().booleanValue());
            statement.setString(index++, device.getKeyHandle());
            statement.setString(index++, device.getPublicKey());
            statement.setString(index++, device.getAttestationCertificate());
            statement.setLong(index++, device.getCounter());
            statement.setBoolean(index++, device.isCompromised());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                dbs.backWritable(connection);
            }
        }
    }

    @Override
    public boolean unregisterDevice(int contextId, int userId, String deviceId) throws OXException {
        final DatabaseService dbs = getDatabaseService();
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        int rows = 0;
        try {
            statement = connection.prepareStatement(DELETE_FOR_USER);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setString(index++, deviceId);
            rows = statement.executeUpdate();
            return rows > 0 ? true : false;
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                if (rows > 0) {
                    dbs.backWritable(connection);
                } else {
                    dbs.backWritableAfterReading(connection);
                }
            }
        }
    }

    @Override
    public Collection<U2FMultifactorDevice> getDevices(int contextId, int userId) throws OXException {
        final DatabaseService dbs = getDatabaseService();
        final Connection connection = dbs.getReadOnly(contextId);
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(GET_BY_USER);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            return createDevicesFrom(statement.executeQuery());
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                dbs.backReadOnly(connection);
            }
        }
    }

    @Override
    public int getCount(int contextId, int userId) throws OXException {
        final DatabaseService dbs = getDatabaseService();
        final Connection connection = dbs.getReadOnly(contextId);
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(GET_COUNT);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create("Error getting device count for TOTP devices");
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(result, statement);
            if (connection != null) {
                dbs.backReadOnly(connection);
            }
        }

    }


    @Override
    public Optional<U2FMultifactorDevice> getDevice(int contextId, int userId, String deviceId) throws OXException {
        final DatabaseService dbs = getDatabaseService();
        final Connection connection = dbs.getReadOnly(contextId);
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(GET_BY_ID);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setString(index++, deviceId);
            result = statement.executeQuery();
            if (result.next()) {
                return Optional.of(createDeviceFrom(result));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(result, statement);
            if (connection != null) {
                dbs.backReadOnly(connection);
            }
        }
    }

    @Override
    public boolean incrementCounter(int contextId, int userId, String deviceId, long current) throws OXException {
        final DatabaseService dbs = getDatabaseService();
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        int rows = 0;
        try {
            statement = connection.prepareStatement(INCREMENT_COUNTER);
            int index = 1;
            statement.setLong(index++, current);
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setString(index++, deviceId);
            rows = statement.executeUpdate();
            return rows > 0 ? true : false;
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                if (rows > 0) {
                    dbs.backWritable(connection);
                } else {
                    dbs.backWritableAfterReading(connection);
                }
            }
        }
    }

    @Override
    public boolean renameDevice(int contextId, int userId, String deviceId, String name) throws OXException {
        final DatabaseService dbs = getDatabaseService();
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        int rows = 0;
        try {
            statement = connection.prepareStatement(UPDATE_NAME);
            int index = 1;
            statement.setString(index++, name);
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setString(index++, deviceId);
            rows = statement.executeUpdate();
            return rows > 0 ? true : false;
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                if (rows > 0) {
                    dbs.backWritable(connection);
                } else {
                    dbs.backWritableAfterReading(connection);
                }
            }
        }
    }


    @Override
    public boolean deleteAllForUser(int userId, int contextId) throws OXException {
        return deleteAllForUser(getDatabaseService(), DELETE_ALL_FOR_USER, userId, contextId);
    }

    @Override
    public boolean deleteAllForContext(int contextId) throws OXException {
        return deleteAllForContext(getDatabaseService(), DELETE_FOR_CONTEXT, contextId);
    }
}