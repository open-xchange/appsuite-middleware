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

package com.openexchange.contact.storage.ldap.id;

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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.tools.sql.DBUtils;

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
                DBUtils.rollback(connection);
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                if (false == committed) {
                    DBUtils.rollback(connection);
                }
                DBUtils.autocommit(connection);
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
            if (null != ldapID) {
                this.ldapIDs.put(Integer.valueOf(contactID), ldapID);
                return ldapID;
            } else {
                throw LdapExceptionCodes.NO_MAPPED_LDAP_ID.create(contactID, folderID, contextID);
            }
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
            DBUtils.closeSQLStuff(resultSet, stmt);
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
            DBUtils.closeSQLStuff(resultSet, stmt);
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
            DBUtils.closeSQLStuff(stmt);
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
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
        return map;
    }

}