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

package com.openexchange.net.ssl.management.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.management.Certificate;
import com.openexchange.net.ssl.management.exception.SSLCertificateManagementSQLExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SSLCertificateManagementSQL}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SSLCertificateManagementSQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSLCertificateManagementSQL.class);

    private ServiceLookup services;

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

        if (contains(userId, contextId, certificate.getFingerprint(), connection)) {
            update(userId, contextId, certificate, connection);
        } else {
            insert(userId, contextId, certificate, connection);
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
    public Certificate get(int userId, int contextId, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.GET);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, fingerprint);

            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw SSLCertificateManagementSQLExceptionCode.CERTIFICATE_NOT_FOUND.create(fingerprint, userId, contextId);
            }

            Certificate certificate = new Certificate(fingerprint);
            certificate.setCommonName(resultSet.getString("host"));
            certificate.setTrusted(resultSet.getBoolean("trusted"));
            return certificate;
        } catch (SQLException e) {
            throw SSLCertificateManagementSQLExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            databaseService.backReadOnly(connection);
        }
    }

    /**
     * Deletes the {@link Certificate} with the specified fingerprint for the specified user
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
            preparedStatement = connection.prepareStatement(SQLStatements.DELETE);
            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, fingerprint);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw SSLCertificateManagementSQLExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            closeStatement(preparedStatement);
            databaseService.backWritable(connection);
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
    public boolean contains(int userId, int contextId, String fingerprint) throws OXException {
        return contains(userId, contextId, fingerprint, null);
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
    public boolean isTrusted(int userId, int contextId, String fingerprint) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.IS_TRUSTED);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, fingerprint);

            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw SSLCertificateManagementSQLExceptionCode.CERTIFICATE_NOT_FOUND.create(fingerprint, userId, contextId);
            }
            return resultSet.getBoolean("trusted");
        } catch (SQLException e) {
            throw SSLCertificateManagementSQLExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            databaseService.backReadOnly(connection);
        }
    }

    //////////////////////////////////// HELPERS //////////////////////////////////////

    /**
     * Updates the specified {@link Certificate} for the specified user in the specified context.
     * 
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param certificate The {@link Certificate} to update
     * @param connection A writeable {@link Connection}
     * @throws OXException If the {@link Certificate} cannot be updated
     */
    private void update(int userId, int contextId, Certificate certificate, Connection connection) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.UPDATE);
            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, certificate.getFingerprint());
            preparedStatement.setBoolean(index++, certificate.isTrusted());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw SSLCertificateManagementSQLExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            closeStatement(preparedStatement);
            databaseService.backWritable(connection);
        }
    }

    /**
     * Inserts the specified {@link Certificate} for the specified user in the specified context.
     * 
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param certificate The {@link Certificate} to update
     * @param connection A writeable {@link Connection}
     * @throws OXException If the {@link Certificate} cannot be inserted
     */
    private void insert(int userId, int contextId, Certificate certificate, Connection connection) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.INSERT);
            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, certificate.getCommonName());
            preparedStatement.setString(index++, certificate.getFingerprint());
            preparedStatement.setBoolean(index++, certificate.isTrusted());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw SSLCertificateManagementSQLExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            closeStatement(preparedStatement);
            databaseService.backWritable(connection);
        }
    }

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
    private boolean contains(int userId, int contextId, String fingerprint, Connection connection) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        boolean connectionInitialised = false;
        if (connection == null) {
            connection = databaseService.getReadOnly(contextId);
            connectionInitialised = true;
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(SQLStatements.CONTAINS);

            int index = 1;
            preparedStatement.setInt(index++, contextId);
            preparedStatement.setInt(index++, userId);
            preparedStatement.setString(index++, fingerprint);

            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw SSLCertificateManagementSQLExceptionCode.SQL_PROBLEM.create(e.getMessage(), e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            if (connectionInitialised) {
                databaseService.backReadOnly(connection);
            }
        }
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
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("DatabaseService");
        }
        return databaseService;
    }

    /**
     * Closes the specified {@link PreparedStatement}
     * 
     * @param preparedStatement The {@link PreparedStatement} to close
     */
    private void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement == null) {
            return;
        }
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * Closes the specified {@link ResultSet}
     * 
     * @param resultSet The {@link ResultSet} to close
     */
    private void closeResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("", e);
        }
    }
}
