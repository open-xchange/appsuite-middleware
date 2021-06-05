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

package com.openexchange.net.ssl.management.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import com.google.common.collect.ImmutableList;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.management.Certificate;
import com.openexchange.net.ssl.management.DefaultCertificate;
import com.openexchange.net.ssl.management.exception.SSLCertificateManagementExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SSLCertificateManagementSQL}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SSLCertificateManagementSQL {

    private final ServiceLookup services;

    /**
     * Initialises a new {@link SSLCertificateManagementSQL}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public SSLCertificateManagementSQL(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Stores the specified {@link Certificate} for the specified user in the specified context.
     * If a certificate with the same fingerprint exists for the same user, then the certificate
     * is updated instead.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param certificate The {@link Certificate} to store
     * @throws OXException If an error is occurred
     */
    public void store(int userId, int contextId, Certificate certificate) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextId);

        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            store(userId, contextId, certificate, connection);

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            databaseService.backWritable(connection);
        }
    }

    /**
     * Stores the specified {@link Certificate} for the specified user in the specified context.
     * If a certificate with the same fingerprint exists for the same user, then the certificate
     * is updated instead.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param certificate The {@link Certificate} to store
     * @param connection The connection to use
     * @throws OXException If an error is occurred
     */
    public void store(int userId, int contextId, Certificate certificate, Connection connection) throws OXException {
        if (null == connection) {
            store(userId, contextId, certificate);
            return;
        }

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.INSERT_ON_DUPLICATE_UPDATE);
            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, DigestUtils.sha256Hex(certificate.getHostName()));
            preparedStatement.setString(index++, certificate.getHostName());
            preparedStatement.setString(index++, certificate.getFingerprint());
            preparedStatement.setBoolean(index++, certificate.isTrusted());
            preparedStatement.setBoolean(index++, certificate.isTrusted());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(preparedStatement);
        }
    }

    /**
     * Retrieve an unmodifiable {@link List} with all trusted/untrusted hostname/certificate combinations
     * for the specified certificate fingerprint
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return @return an unmodifiable {@link List} with {@link Certificate}s
     * @throws OXException if any error is occurred
     */
    public List<Certificate> get(int userId, int contextId, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.GET_FOR_ALL_HOSTS);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, fingerprint);

            resultSet = preparedStatement.executeQuery();
            return parse(resultSet);
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(resultSet, preparedStatement);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    /**
     * Retrieve a {@link Certificate} from the database
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return The {@link Certificate}
     * @throws OXException if the {@link Certificate} does not exist, or any other error occurs
     */
    public Certificate get(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.GET_FOR_HOST);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, DigestUtils.sha256Hex(hostname));
            preparedStatement.setString(index++, fingerprint);

            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw SSLCertificateManagementExceptionCode.CERTIFICATE_NOT_FOUND.create(fingerprint, userId, contextId);
            }

            DefaultCertificate.Builder certificate = DefaultCertificate.builder().fingerprint(fingerprint).hostName(resultSet.getString("host")).trusted(resultSet.getBoolean("trusted"));
            return certificate.build();
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(resultSet, preparedStatement);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    /**
     * Returns an unmodifiable {@link List} with all managed {@link Certificate}s for the specified
     * user in the specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return an unmodifiable {@link List} with all managed {@link Certificate}s for the specified
     *         user in the specified context
     * @throws OXException if an error is occurred
     */
    public List<Certificate> getAll(int userId, int contextId) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.GET_ALL);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);

            resultSet = preparedStatement.executeQuery();

            return parse(resultSet);
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(resultSet, preparedStatement);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    /**
     * Deletes all exceptions of the {@link Certificate} with the specified fingerprint for the specified user
     * in the specified context.
     *
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @throws OXException If the {@link Certificate} cannot be deleted
     */
    public void delete(int userId, int contextId, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextId);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.DELETE_FOR_ALL_HOSTS);
            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, fingerprint);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(preparedStatement);
            databaseService.backWritable(contextId, connection);
        }
    }

    /**
     * Deletes the {@link Certificate} with the specified fingerprint for the specified user
     * in the specified context.
     *
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param hostname The hostname
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @throws OXException If the {@link Certificate} cannot be deleted
     */
    public void delete(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextId);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.DELETE_FOR_HOST);
            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, DigestUtils.sha256Hex(hostname));
            preparedStatement.setString(index++, fingerprint);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(preparedStatement);
            databaseService.backWritable(contextId, connection);
        }
    }

    /**
     * Deletes all SSL {@link Certificate} exceptions for the specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException if an error is occurred
     */
    public void deleteAll(int userId, int contextId) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextId);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.DELETE_ALL);
            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(preparedStatement);
            databaseService.backWritable(contextId, connection);
        }
    }

    /**
     * Checks if a {@link Certificate} with the specified fingerprint for the specified
     * user in the specified context exists.
     *
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return <code>true<code> if the {@link Certificate} exists; <code>false</code> otherwise
     * @throws OXException If an error is occurred
     */
    public boolean contains(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        try {
            return contains(userId, contextId, hostname, fingerprint, connection);
        } finally {
            databaseService.backReadOnly(contextId, connection);
        }
    }

    /**
     * Checks if the {@link Certificate} with the specified fingerprint of the specified user
     * in the specified context is trusted.
     *
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @return <code>true<code> if the {@link Certificate} is trusted; <code>false</code> otherwise
     * @throws OXException If an error is occurred
     */
    public boolean isTrusted(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.IS_TRUSTED);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, DigestUtils.sha256Hex(hostname));
            preparedStatement.setString(index++, fingerprint);

            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw SSLCertificateManagementExceptionCode.CERTIFICATE_NOT_FOUND.create(fingerprint, userId, contextId);
            }
            return resultSet.getBoolean("trusted");
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(resultSet, preparedStatement);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    ////////////////////////////////////////////// HELPERS //////////////////////////////////////////////////////

    /**
     * Checks if a {@link Certificate} with the specified fingerprint for the specified
     * user in the specified context exists.
     *
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param fingerprint The fingerprint of the {@link Certificate}
     * @param connection An optional {@link Connection}
     * @return <code>true<code> if the {@link Certificate} exists; <code>false</code> otherwise
     * @throws OXException If an error is occurred
     */
    private boolean contains(int userId, int contextId, String hostname, String fingerprint, Connection connection) throws OXException {
        if (null == connection) {
            return contains(userId, contextId, hostname, fingerprint);
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.CONTAINS);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, DigestUtils.sha256Hex(hostname));
            preparedStatement.setString(index++, fingerprint);

            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw SSLCertificateManagementExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(resultSet, preparedStatement);
        }
    }

    /**
     * Parses the {@link ResultSet}
     *
     * @param resultSet The {@link ResultSet} to parse
     * @return An unmodifiable {@link List} with {@link Certificate}s, or an empty {@link List}
     * @throws SQLException if an SQL error is occurred
     */
    private List<Certificate> parse(ResultSet resultSet) throws SQLException {
        if (false == resultSet.next()) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<Certificate> certificates = ImmutableList.builder();
        do {
            DefaultCertificate.Builder certificate = DefaultCertificate.builder().fingerprint(resultSet.getString("fingerprint")).hostName(resultSet.getString("host")).trusted(resultSet.getBoolean("trusted"));
            certificates.add(certificate.build());
        } while (resultSet.next());

        return certificates.build();
    }

    /**
     * Returns the {@link DatabaseService}
     *
     * @return The {@link DatabaseService}
     * @throws OXException If the service is absent
     */
    private DatabaseService getDatabaseService() throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        if (databaseService == null) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        return databaseService;
    }
}
