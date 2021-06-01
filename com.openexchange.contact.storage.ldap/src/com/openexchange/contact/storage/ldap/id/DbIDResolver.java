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

package com.openexchange.contact.storage.ldap.id;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.internal.LdapServiceLookup;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.impl.IDGenerator;

/**
 * {@link DbIDResolver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DbIDResolver extends DefaultLdapIDResolver {

    private static final String SELECT_CONTACTID_STMT =
        "SELECT contact FROM ldapIds WHERE cid=? AND fid=? AND ldapId=?;";
    private static final String SELECT_LDAPID_STMT =
        "SELECT ldapId FROM ldapIds WHERE cid=? AND fid=? AND contact=?;";
    private static final String INSERT_ID_STMT =
        "INSERT INTO ldapIds (cid,fid,contact,ldapId) VALUES (?,?,?,?);";
    private static final String SELECT_IDS_STMT =
        "SELECT contact,ldapId FROM ldapIds WHERE cid=? AND fid=?;";

    private ConcurrentMap<String, Integer> contactIDs;
    private ConcurrentMap<Integer, String> ldapIDs;

    /**
     * Initializes a new {@link DbIDResolver}.
     *
     * @param contextID the context ID
     * @param folderID the folder ID
     * @throws OXException
     */
    public DbIDResolver(int contextID, int folderID) throws OXException {
        super(contextID, folderID);
        initCache();
    }

    @Override
    public int getContactID(String ldapID) throws OXException {
        Integer contactID = contactIDs.get(ldapID);
        if (null != contactID) {
            return contactID.intValue();
        }
        DatabaseService databaseService = LdapServiceLookup.getService(DatabaseService.class);
        {
            Connection connection = databaseService.getReadOnly(contextID);
            try {
                int id = loadContactID(connection, contextID, folderID, ldapID);
                if (-1 != id) {
                    this.contactIDs.put(ldapID, Integer.valueOf(id));
                    return id;
                }
            } catch (SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            } finally {
                databaseService.backReadOnly(contextID, connection);
            }
        }
        {
            boolean committed = false;
            Connection connection = databaseService.getWritable(contextID);
            try {
                connection.setAutoCommit(false);
                int id = createContactID(connection, contextID, folderID, ldapID);
                connection.commit();
                committed = true;
                this.contactIDs.put(ldapID, Integer.valueOf(id));
                return id;
            } catch (SQLException e) {
                Databases.rollback(connection);
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                if (false == committed) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
                databaseService.backWritable(contextID, connection);
            }
        }
    }

    @Override
    public String getLdapID(int contactID) throws OXException {
        String ldapID = ldapIDs.get(Integer.valueOf(contactID));
        if (null != ldapID) {
            return ldapID;
        }
        DatabaseService databaseService = LdapServiceLookup.getService(DatabaseService.class);
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            ldapID = loadLdapID(connection, contextID, folderID, contactID);
            if (null == ldapID) {
                throw LdapExceptionCodes.NO_MAPPED_LDAP_ID.create(I(contactID), I(folderID), I(contextID));
            }
            this.ldapIDs.put(Integer.valueOf(contactID), ldapID);
            return ldapID;
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    private void initCache() throws OXException {
        DatabaseService databaseService = LdapServiceLookup.getService(DatabaseService.class);
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            this.contactIDs = loadIDs(connection, contextID, folderID);
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
        if (null != contactIDs) {
            this.ldapIDs = new ConcurrentHashMap<Integer, String>(contactIDs.size(), 0.9f, 1);
            for (Entry<String, Integer> entry : contactIDs.entrySet()) {
                ldapIDs.put(entry.getValue(), entry.getKey());
            }
        }
    }

    private static String loadLdapID(Connection connection, int contextID, int folderID, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SELECT_LDAPID_STMT);
            stmt.setInt(1, contextID);
            stmt.setInt(2, folderID);
            stmt.setInt(3, id);
            resultSet = stmt.executeQuery();
            return resultSet.next() ? resultSet.getString("ldapId") : null;
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
        }
    }

    private static int loadContactID(Connection connection, int contextID, int folderID, String ldapID) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SELECT_CONTACTID_STMT);
            stmt.setInt(1, contextID);
            stmt.setInt(2, folderID);
            stmt.setString(3, ldapID);
            resultSet = stmt.executeQuery();
            return resultSet.next() ? resultSet.getInt("contact") : -1;
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
        }
    }

    private static int createContactID(Connection connection, int contextID, int folderID, String ldapID) throws SQLException {
        PreparedStatement stmt = null;
        try {
            int id = IDGenerator.getId(contextID, com.openexchange.groupware.Types.CONTACT, connection);
            stmt = connection.prepareStatement(INSERT_ID_STMT);
            stmt.setInt(1, contextID);
            stmt.setInt(2, folderID);
            stmt.setInt(3, id);
            stmt.setString(4, ldapID);
            if (1 != stmt.executeUpdate()) {
                throw new SQLException("0 rows affected");
            }
            return id;
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static ConcurrentMap<String, Integer> loadIDs(Connection connection, int contextID, int folderID) throws SQLException {
        ConcurrentMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SELECT_IDS_STMT);
            stmt.setInt(1, contextID);
            stmt.setInt(2, folderID);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Integer contactID = Integer.valueOf(resultSet.getInt(1));
                String ldapID = resultSet.getString(2);
                map.put(ldapID, contactID);
            }
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
        }
        return map;
    }

}