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

package com.openexchange.push.impl.credstorage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.credstorage.Credentials;
import com.openexchange.push.credstorage.DefaultCredentials;
import com.openexchange.push.impl.credstorage.Obfuscator;
import com.openexchange.push.impl.credstorage.osgi.CredStorageServices;

/**
 * {@link RdbCredentialStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class RdbCredentialStorage implements CredentialStorage {

    private final Obfuscator obfuscator;

    /**
     * Initializes a new {@link RdbCredentialStorage}.
     */
    public RdbCredentialStorage(Obfuscator obfuscator) {
        super();
        this.obfuscator = obfuscator;
    }

    @Override
    public Credentials getCredentials(int userId, int contextId) throws OXException {
        DatabaseService service = CredStorageServices.requireService(DatabaseService.class);
        Connection connection = service.getReadOnly(contextId);
        try {
            return obfuscator.unobfuscateCredentials(getCredentials(userId, contextId, connection));
        } finally {
            service.backReadOnly(contextId, connection);
        }
    }

    private Credentials getCredentials(int userId, int contextId, Connection connection) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT password, login FROM credentials WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }
            DefaultCredentials credentials = new DefaultCredentials();
            credentials.setContextId(contextId);
            credentials.setUserId(userId);
            credentials.setPassword(rs.getString(1));
            credentials.setLogin(rs.getString(2));
            return credentials;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void storeCredentials(Credentials credentials) throws OXException {
        if ((null == credentials) || (false == isValid(credentials))) {
            throw OXException.general("Invalid credentials given: " + (null == credentials ? "null" : credentials.toString()));
        }

        int contextId = credentials.getContextId();
        DatabaseService service = CredStorageServices.requireService(DatabaseService.class);
        Connection connection = service.getWritable(contextId);
        boolean modified = false;
        try {
            modified = storeCredentials(obfuscator.obfuscateCredentials(credentials), connection);
        } finally {
            if (modified) {
                service.backWritable(contextId, connection);
            } else {
                service.backWritableAfterReading(contextId, connection);
            }
        }
    }

    private boolean isValid(Credentials credentials) {
        return (null != credentials.getLogin()) && (null != credentials.getPassword());
    }

    private boolean storeCredentials(Credentials obfuscatedCredentials, Connection connection) throws OXException {
        return storeCredentials(obfuscatedCredentials, true, connection);
    }

    private boolean storeCredentials(Credentials obfuscatedCredentials, boolean retry, Connection connection) throws OXException {
        int contextId = obfuscatedCredentials.getContextId();
        int userId = obfuscatedCredentials.getUserId();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT login, password FROM credentials WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();

            boolean exists = false;
            String curLogin = null;
            String curPassword = null;
            if (rs.next()) {
                exists = true;
                curLogin = rs.getString(1);
                curPassword = rs.getString(2);
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            if (exists) {
                // Check current credentials
                if (obfuscatedCredentials.getLogin().equals(curLogin) && obfuscatedCredentials.getPassword().equals(curPassword)) {
                    // Nothing to do
                    return false;
                }

                // Perform UPDATE statement
                stmt = connection.prepareStatement("UPDATE credentials SET password=?, login=? WHERE cid=? AND user=? AND password=? AND login=?");
                stmt.setString(1, obfuscatedCredentials.getPassword());
                stmt.setString(2, obfuscatedCredentials.getLogin());
                stmt.setInt(3, contextId);
                stmt.setInt(4, userId);
                stmt.setString(5, curPassword);
                stmt.setString(6, curLogin);
                int updatedRows = stmt.executeUpdate();
                return updatedRows > 0 ? true : storeCredentials(obfuscatedCredentials, false, connection);
            }

            // Perform INSERT statement
            stmt = connection.prepareStatement("INSERT INTO credentials (cid, user, password, login) VALUES (?,?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, obfuscatedCredentials.getPassword());
            stmt.setString(4, obfuscatedCredentials.getLogin());
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                // Duplicate write attempt
                if (!retry) {
                    throw e;
                }
                return storeCredentials(obfuscatedCredentials, false, connection);
            }
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Credentials deleteCredentials(int userId, int contextId) throws OXException {
        DatabaseService service = CredStorageServices.requireService(DatabaseService.class);
        Connection connection = service.getWritable(contextId);
        Credentials deletedCredentials = null;
        try {
            deletedCredentials = deleteCredentials(userId, contextId, connection);
            return obfuscator.unobfuscateCredentials(deletedCredentials);
        } finally {
            if (null == deletedCredentials) {
                service.backWritableAfterReading(contextId, connection);
            } else {
                service.backWritable(contextId, connection);
            }
        }
    }

    private Credentials deleteCredentials(int userId, int contextId, Connection connection) throws OXException {
        PreparedStatement stmt = null;
        try {
            Credentials credentials = getCredentials(userId, contextId, connection);
            if (null == credentials) {
                return null;
            }

            stmt = connection.prepareStatement("DELETE FROM credentials WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0 ? credentials : null;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
