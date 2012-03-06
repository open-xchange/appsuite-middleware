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

import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;


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
        stringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?;");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            resultSet = logExecuteQuery(stmt);
            return resultSet.next() ? Mappers.CONTACT.fromResultSet(resultSet, fields) : null; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }
    
    public List<Contact> select(final Connection connection, final Table table, final int contextID, final int folderID, final ContactField[] fields) throws SQLException, OXException {
        return this.select(connection, table, contextID, folderID, Long.MIN_VALUE, fields);
    }
    
    public List<Contact> select(final Connection connection, final Table table, final int contextID, final int folderID, final long minLastModified, final ContactField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(";");
        } else {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
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
                contacts.add(Mappers.CONTACT.fromResultSet(resultSet, fields));
            }
            return contacts; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public List<Contact> select(final Connection connection, final Table table, final int contextID, final int[] objectIDs, final ContactField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append(" IN (").append(Tools.toCSV(objectIDs)).append(");");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final List<Contact> contacts = new ArrayList<Contact>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                contacts.add(Mappers.CONTACT.fromResultSet(resultSet, fields));
            }
            return contacts; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }
    
    public DistListMember[] select(final Connection connection, final Table table, final int contextID, final int objectID, final DistListMemberField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Mappers.DISTLIST.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel()).append("=?;"); 
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final List<DistListMember> members = new ArrayList<DistListMember>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                members.add(Mappers.DISTLIST.fromResultSet(resultSet, fields));
            }
            return members.toArray(new DistListMember[members.size()]); 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public Map<Integer, List<DistListMember>> select(final Connection connection, final Table table, final int contextID, final int[] objectIDs, final DistListMemberField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Mappers.DISTLIST.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel()).append(" IN (")
            .append(Tools.toCSV(objectIDs)).append(");");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final Map<Integer, List<DistListMember>> members = new HashMap<Integer, List<DistListMember>>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                final int parentContactObjectID = resultSet.getInt(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel());
                if (resultSet.wasNull()) {
                	throw new IllegalArgumentException("need " + DistListMemberField.PARENT_CONTACT_ID + "in fields");
                }
                if (false == members.containsKey(parentContactObjectID)) {
                    members.put(parentContactObjectID, new ArrayList<DistListMember>());
                }
                members.get(parentContactObjectID).add(Mappers.DISTLIST.fromResultSet(resultSet, fields));
            }
            return members; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public int insert(final Connection connection, final Table table, final Contact contact, final ContactField[] fields) throws SQLException, OXException {        
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ").append(table).append(" (").append(Mappers.CONTACT.getColumns(fields))
            .append(") VALUES (").append(Tools.getParameters(fields.length)).append(");");
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Mappers.CONTACT.setParameters(stmt, contact, fields);
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }    
    
    public int insert(final Connection connection, final Table table, final DistListMember member, final DistListMemberField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ").append(table).append(" (").append(Mappers.DISTLIST.getColumns(fields))
            .append(") VALUES (").append(Tools.getParameters(fields.length)).append(");");
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Mappers.DISTLIST.setParameters(stmt, member, fields);
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int insert(final Connection connection, final Table table, final DistListMember[] members, final DistListMemberField[] fields) throws SQLException, OXException {
        int rowCount = 0;
        for (final DistListMember member : members) {
            rowCount += this.insert(connection, table, member, fields);            
        }
        return rowCount;
    }    
    
    public int insertFrom(final Connection connection, final Table from, final Table to, final int contextID, final int objectID, final long minLastModified) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ").append(to).append(" SELECT * FROM ").append(from).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(";");
        } else {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
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
            
    public int insertFrom(final Connection connection, final Table from, final Table to, final int contextID, final int objectID) throws SQLException, OXException {
        return this.insertFrom(connection, from, to, contextID, objectID, Long.MIN_VALUE);
    }
            
    public int update(final Connection connection, final Table table, final long minLastModified, final Contact contact, final ContactField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(table).append(" SET ").append(Mappers.CONTACT.getAssignments(fields)).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel())
            .append("=? AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;"); 
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Mappers.CONTACT.setParameters(stmt, contact, fields);
            stmt.setInt(1 + fields.length, contact.getContextId());
            stmt.setInt(2 + fields.length, contact.getObjectID());
            stmt.setLong(3 + fields.length, minLastModified);
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int delete(final Connection connection, final Table table, final int contextID, final int objectID, final long minLastModified) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(";");
        } else {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
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
    
    public int delete(final Connection connection, final Table table, final int contextID, final int objectID) throws SQLException, OXException {
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
