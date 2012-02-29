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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.internal;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;


/**
 * {@link Executor} - Constructs and performs the actual database statements.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Executor {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Executor.class));
    
    /**
     * Initializes a new {@link Executor}.
     */
    public Executor() {
        super();        
    }
    
    public Contact selectSingle(final Connection connection, final Table table, final int contextID, final int objectID, final ContactField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Tools.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.OBJECT_ID.getDbName()).append("=?;");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            resultSet = logExecuteQuery(stmt);
            return resultSet.next() ? Tools.fromResultSet(resultSet, fields) : null; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }
    
    public List<Contact> select(final Connection connection, final Table table, final int contextID, final int folderID, final ContactField[] fields) throws SQLException, OXException {
        return this.select(connection, table, contextID, folderID, Long.MIN_VALUE, fields);
    }
    
    public List<Contact> select(final Connection connection, final Table table, final int contextID, final int folderID, final long minLastModified, final ContactField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Tools.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.FOLDER_ID.getDbName()).append("=?");
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(";");
        } else {
            stringBuilder.append(" AND ").append(ContactField.LAST_MODIFIED.getDbName()).append("<=?;");
        }             
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final List<Contact> contacts = new ArrayList<Contact>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, folderID);
            if (Long.MIN_VALUE != minLastModified) {
                stmt.setLong(3, minLastModified);
            }
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                contacts.add(Tools.fromResultSet(resultSet, fields));
            }
            return contacts; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public List<Contact> select(final Connection connection, final Table table, final int contextID, final int[] objectIDs, final ContactField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Tools.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.OBJECT_ID.getDbName()).append(" IN (")
            .append(Tools.toCSV(objectIDs)).append(");");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final List<Contact> contacts = new ArrayList<Contact>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                contacts.add(Tools.fromResultSet(resultSet, fields));
            }
            return contacts; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }
    
    public DistributionListEntryObject[] select(final Connection connection, final Table table, final int contextID, final int objectID) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Tools.getColumns(Tools.DISTLIST_DATABASE_FIELDS_ARRAY)).append(" FROM ").append(table).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.OBJECT_ID.getDbName()).append("=?;"); 
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final List<DistributionListEntryObject> members = new ArrayList<DistributionListEntryObject>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                members.add(Tools.fromDistListResultSet(resultSet));
            }
            return members.toArray(new DistributionListEntryObject[members.size()]); 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public Map<Integer, List<DistributionListEntryObject>> select(final Connection connection, final Table table, final int contextID, final int[] objectIDs) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Tools.getColumns(Tools.DISTLIST_DATABASE_FIELDS_ARRAY)).append(" FROM ").append(table).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.OBJECT_ID.getDbName()).append(" IN (")
            .append(Tools.toCSV(objectIDs)).append(");");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final Map<Integer, List<DistributionListEntryObject>> members = new HashMap<Integer, List<DistributionListEntryObject>>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                final int parentContactObjectID = resultSet.getInt(ContactField.OBJECT_ID.getDbName());
                if (false == members.containsKey(parentContactObjectID)) {
                    members.put(parentContactObjectID, new ArrayList<DistributionListEntryObject>());
                }
                members.get(parentContactObjectID).add(Tools.fromDistListResultSet(resultSet));
            }
            return members; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public int insert(final Connection connection, final Table table, final Contact contact, final ContactField[] fields) throws SQLException, OXException {        
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ").append(table).append(" (").append(Tools.getColumns(fields))
            .append(") VALUES (").append(Tools.getParameters(fields.length)).append(");");
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Tools.setParameters(stmt, contact, fields);
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }    
    
    public int insert(final Connection connection, final Table table, final int contactID, final int contextID, final DistributionListEntryObject member) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ").append(table).append(" (").append(Tools.getColumns(Tools.DISTLIST_DATABASE_FIELDS_ARRAY))
            .append(") VALUES (").append(Tools.getParameters(Tools.DISTLIST_DATABASE_FIELDS_ARRAY.length)).append(");");
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Tools.setParameters(stmt, Tools.DISTLIST_DATABASE_FIELDS_ARRAY, member, contactID, contextID);
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int insert(final Connection connection, final Table table, final int contactID, final int contextID, final DistributionListEntryObject[] distributionList) throws SQLException, OXException {
        int rowCount = 0;
        for (final DistributionListEntryObject member : distributionList) {
            rowCount += this.insert(connection, table, contactID, contextID, member);            
        }
        return rowCount;
    }    
    
    public int insertFrom(final Connection connection, final Table from, final Table to, final int contextID, final int objectID, final long minLastModified) throws SQLException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ").append(to).append(" SELECT * FROM ").append(from).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.OBJECT_ID.getDbName()).append("=?");
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(";");
        } else {
            stringBuilder.append(" AND ").append(ContactField.LAST_MODIFIED.getDbName()).append("<=?;");
        }             
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            if (Long.MIN_VALUE != minLastModified) {
                stmt.setLong(3, minLastModified);
            }
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
            
    public int insertFrom(final Connection connection, final Table from, final Table to, final int contextID, final int objectID) throws SQLException {
        return this.insertFrom(connection, from, to, contextID, objectID, Long.MIN_VALUE);
    }
            
    public int update(final Connection connection, final Table table, final long minLastModified, final Contact contact, final ContactField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(table).append(" SET ").append(Tools.getAssignments(fields)).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.OBJECT_ID.getDbName())
            .append("=? AND ").append(ContactField.LAST_MODIFIED.getDbName()).append("<=?;"); 
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Tools.setParameters(stmt, contact, fields);
            stmt.setInt(1 + fields.length, contact.getContextId());
            stmt.setInt(2 + fields.length, contact.getObjectID());
            stmt.setLong(3 + fields.length, minLastModified);
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int delete(final Connection connection, final Table table, final int contextID, final int objectID, final long minLastModified) throws SQLException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM ").append(table).append(" WHERE ")
            .append(ContactField.CONTEXTID.getDbName()).append("=? AND ").append(ContactField.OBJECT_ID.getDbName()).append("=?");
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(";");
        } else {
            stringBuilder.append(" AND ").append(ContactField.LAST_MODIFIED.getDbName()).append("<=?;");
        }             
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            if (Long.MIN_VALUE != minLastModified) {
                stmt.setLong(3, minLastModified);
            }
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int delete(final Connection connection, final Table table, final int contextID, final int objectID) throws SQLException {
        return this.delete(connection, table, contextID, objectID, Long.MIN_VALUE);
    } 
    
    private static ResultSet logExecuteQuery(final PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            LOG.debug("executeUpdate: " + stmt.toString());
            return stmt.executeQuery();
        }   
    }
    
    private static int logExecuteUpdate(final PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            final int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: " + stmt.toString() + " - " + rowCount + " rows affected.");
            return rowCount;
        }   
    }

}
