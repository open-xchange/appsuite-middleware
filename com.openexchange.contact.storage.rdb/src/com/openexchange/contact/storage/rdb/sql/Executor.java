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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.sql;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.internal.DistListMember;
import com.openexchange.contact.storage.rdb.internal.Tools;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.search.ContactSearchAdapter;
import com.openexchange.contact.storage.rdb.search.SearchAdapter;
import com.openexchange.contact.storage.rdb.search.SearchTermAdapter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.log.LogFactory;
import com.openexchange.search.SearchTerm;


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
    
    /**
     * Selects a single contact from the database.
     * 
     * @param connection
     * @param table
     * @param contextID
     * @param objectID
     * @param fields
     * @return
     * @throws SQLException
     * @throws OXException
     */
    public Contact selectSingle(Connection connection, Table table, int contextID, int objectID, ContactField[] fields) 
    		throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
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

    /**
     * 
     * @param connection
     * @param contextID
     * @param objectID
     * @return
     * @throws SQLException
     */
    public Date selectNewestAttachmentDate(final Connection connection, final int contextID, final int objectID) throws SQLException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT MAX(creation_date) FROM prg_attachment WHERE cid=? AND module=? AND attached=?;");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, Types.CONTACT);
            stmt.setInt(3, objectID);
            resultSet = logExecuteQuery(stmt);
            return resultSet.next() ? new Date(resultSet.getLong(1)) : null; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }
    
    /**
     * 
     * @param connection
     * @param contextID
     * @param objectID
     * @return
     * @throws SQLException
     */
    public Map<Integer, Date> selectNewestAttachmentDates(final Connection connection, final int contextID, final int objectIDs[]) throws SQLException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT attached,MAX(creation_date) FROM prg_attachment WHERE cid=? AND module=? AND attached IN (")
        		.append(Tools.toCSV(objectIDs)).append(") GROUP BY attached;");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final Map<Integer, Date> dates = new HashMap<Integer, Date>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, Types.CONTACT);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                dates.put(Integer.valueOf(resultSet.getInt(1)), new Date(resultSet.getLong(2)));
            }            
            return dates; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }
    
    /**
     * Selects contacts from the database.
     * 
     * @param connection
     * @param table
     * @param contextID
     * @param folderID
     * @param objectIDs
     * @param minLastModified
     * @param fields
     * @param term
     * @param sortOptions
     * @return
     * @throws SQLException
     * @throws OXException
     */
    public <O> List<Contact> select(Connection connection, Table table, int contextID, int folderID, int[] objectIDs, long minLastModified, 
    		ContactField[] fields, SearchTerm<O> term, SortOptions sortOptions) throws SQLException, OXException {
        /*
         * construct query string
         */
    	SearchTermAdapter adapter = null != term ? new SearchTermAdapter(term, getCharset(sortOptions)) : null;
        StringBuilder stringBuilder = new StringBuilder();
//      stringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" USE INDEX (cid) WHERE ")
//      stringBuilder.append("SELECT SQL_NO_CACHE ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
        stringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (Integer.MIN_VALUE != folderID) {
        	stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        }
        if (Long.MIN_VALUE != minLastModified) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append(">?");
        }
        if (null != objectIDs && 0 < objectIDs.length) {
        	stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
        	if (1 == objectIDs.length) {
        		stringBuilder.append('=').append(objectIDs[0]);
        	} else {
	        	stringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
        	}
        }
        if (null != adapter) {
        	stringBuilder.append(" AND ").append(adapter.getClause());	
        }
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)) {
        	stringBuilder.append(' ').append(Tools.getOrderClause(sortOptions));
        	if (0 < sortOptions.getLimit()) {
            	stringBuilder.append(' ').append(Tools.getLimitClause(sortOptions));
        	}
        }
        stringBuilder.append(';');
        /*
         * prepare statement
         */
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        ResultSet resultSet = null;
        List<Contact> contacts = new ArrayList<Contact>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(parameterIndex++, contextID);
            if (Integer.MIN_VALUE != folderID) {
            	stmt.setInt(parameterIndex++, folderID);
            }
            if (Long.MIN_VALUE != minLastModified) {
                stmt.setLong(parameterIndex++, minLastModified);
            }
            if (null != adapter) {
            	adapter.setParameters(stmt, parameterIndex);            	
            }
            /*
             * execute and read out results
             */
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                contacts.add(Mappers.CONTACT.fromResultSet(resultSet, fields));
            }
            return contacts; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public List<Contact> select(Connection connection, Table table, int contextID, ContactSearchObject contactSearch, 
    		ContactField[] fields, SortOptions sortOptions) throws SQLException, OXException {
        /*
         * construct query string
         */
        SearchAdapter adapter = new ContactSearchAdapter(contactSearch, contextID, fields, getCharset(sortOptions));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(adapter.getClause());
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)) {
            stringBuilder.append(' ').append(Tools.getOrderClause(sortOptions));
            if (0 < sortOptions.getLimit()) {
                stringBuilder.append(' ').append(Tools.getLimitClause(sortOptions));
            }
        }
        stringBuilder.append(';');
        /*
         * prepare statement
         */
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        ResultSet resultSet = null;
        List<Contact> contacts = new ArrayList<Contact>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
           	adapter.setParameters(stmt, parameterIndex);            	
            /*
             * execute and read out results
             */
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                contacts.add(Mappers.CONTACT.fromResultSet(resultSet, fields));
            }
            return contacts; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    /**
     * Selects the members of a distribution list from the database.
     * 
     * @param connection
     * @param table
     * @param contextID
     * @param objectID
     * @param fields
     * @return
     * @throws SQLException
     * @throws OXException
     */
    public DistListMember[] select(final Connection connection, final Table table, final int contextID, final int objectID, 
    		final DistListMemberField[] fields) throws SQLException, OXException {
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

    /**
     * Selects members of distribution lists from the database.
     * 
     * @param connection
     * @param table
     * @param contextID
     * @param objectIDs
     * @param fields
     * @return
     * @throws SQLException
     * @throws OXException
     */
    public Map<Integer, List<DistListMember>> select(final Connection connection, final Table table, final int contextID, 
    		final int[] objectIDs, final DistListMemberField[] fields) throws SQLException, OXException {
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
                final int parentContactObjectID = resultSet.getInt(
                		Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel());
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

    /**
     * Selects members of distribution lists from the database.
     * 
     * @param connection
     * @param table
     * @param contextID
     * @param entryIDs
     * @param fields
     * @return
     * @throws SQLException
     * @throws OXException
     */
    public List<DistListMember> select(Connection connection, Table table, int contextID, int referencedObjectID, int referencedFolderID, DistListMemberField[] fields) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Mappers.DISTLIST.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTACT_ID).getColumnLabel()).append("=? AND ( ")
            //FIXME: the previous implementation didn't write the contact folder ID, so we need to extend the possible folder IDs here 
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTACT_FOLDER_ID).getColumnLabel()).append(" IS NULL OR ")
        	.append(Mappers.DISTLIST.get(DistListMemberField.CONTACT_FOLDER_ID).getColumnLabel()).append("=0 OR ")
        	.append(Mappers.DISTLIST.get(DistListMemberField.CONTACT_FOLDER_ID).getColumnLabel()).append("=? );");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DistListMember> members = new ArrayList<DistListMember>();
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, referencedObjectID);
            stmt.setInt(3, referencedFolderID);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
            	members.add(Mappers.DISTLIST.fromResultSet(resultSet, fields));
            }
            return members; 
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public int insert(final Connection connection, final Table table, final Contact contact, final ContactField[] fields) 
    		throws SQLException, OXException {        
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
    
    public int insert(final Connection connection, final Table table, final DistListMember member, final DistListMemberField[] fields) 
    		throws SQLException, OXException {
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
    
    public int insert(final Connection connection, final Table table, final DistListMember[] members, final DistListMemberField[] fields) 
    		throws SQLException, OXException {
        int rowCount = 0;
        for (final DistListMember member : members) {
            rowCount += this.insert(connection, table, member, fields);            
        }
        return rowCount;
    }    

    public int insertFrom(Connection connection, Table from, Table to, int contextID, int folderID, int[] objectIDs) throws SQLException, OXException {
        return insertFrom(connection, from, to, contextID, folderID, objectIDs, Long.MIN_VALUE);
    }
    
    public int insertFrom(Connection connection, Table from, Table to, int contextID, int folderID, int[] objectIDs, long minLastModified) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("REPLACE INTO ").append(to).append(" SELECT * FROM ").append(from).append(" WHERE ");
        if (from.isDistListTable()) {
            stringBuilder.append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=?");
            if (null != objectIDs && 0 < objectIDs.length) {
                stringBuilder.append(" AND ").append(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel());
                if (1 == objectIDs.length) {
                    stringBuilder.append('=').append(objectIDs[0]);
                } else {
                    stringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
                }
                stringBuilder.append(';');
            }            
        } else {
            stringBuilder.append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
            if (Integer.MIN_VALUE != folderID) {
                stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
            }
            if (null != objectIDs && 0 < objectIDs.length) {
                stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
                if (1 == objectIDs.length) {
                    stringBuilder.append('=').append(objectIDs[0]);
                } else {
                    stringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
                }
            }
            if (Long.MIN_VALUE == minLastModified) {
                stringBuilder.append(';');
            } else {
                stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
            }             
        }
        /*
         * prepare statement
         */        
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(parameterIndex++, contextID);
            if (Integer.MIN_VALUE != folderID && false == from.isDistListTable()) {
                stmt.setInt(parameterIndex++, folderID);
            }
            if (Long.MIN_VALUE != minLastModified && false == from.isDistListTable()) {
                stmt.setLong(parameterIndex++, minLastModified);
            }
            /*
             * execute 
             */
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public int insertFrom(final Connection connection, final Table from, final Table to, final int contextID, final int objectID, 
        final long minLastModified) throws SQLException, OXException {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("REPLACE INTO ").append(to).append(" SELECT * FROM ").append(from).append(" WHERE ");
    if (from.isDistListTable()) {
        stringBuilder.append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel()).append("=?");
    } else {
        stringBuilder.append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
    }
    if (Long.MIN_VALUE == minLastModified) {
        stringBuilder.append(';');
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

    public int insertFrom(final Connection connection, final Table from, final Table to, final int contextID, final int objectID) 
    		throws SQLException, OXException {
        return this.insertFrom(connection, from, to, contextID, objectID, Long.MIN_VALUE);
    }
            
    public int update(Connection connection, Table table, int contextID, int objectID, long minLastModified, Contact contact, ContactField[] fields) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(table).append(" SET ").append(Mappers.CONTACT.getAssignments(fields)).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(';');
        } else {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
        }             
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Mappers.CONTACT.setParameters(stmt, contact, fields);
            stmt.setInt(1 + fields.length, contextID);
            stmt.setInt(2 + fields.length, objectID);
            if (Long.MIN_VALUE != minLastModified) {
                stmt.setLong(3 + fields.length, minLastModified);
            }
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int update(Connection connection, Table table, int contextID, int folderID, int[] objectIDs, Contact template, ContactField[] fields, long minLastModified) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(table).append(" SET ").append(Mappers.CONTACT.getAssignments(fields)).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (Integer.MIN_VALUE != folderID) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        }
        if (null != objectIDs && 0 < objectIDs.length) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
            if (1 == objectIDs.length) {
                stringBuilder.append('=').append(objectIDs[0]);
            } else {
                stringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
            }
        }
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(';');
        } else {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
        }
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Mappers.CONTACT.setParameters(stmt, template, fields);
            int parameterIndex = 1 + fields.length;
            stmt.setInt(parameterIndex++, contextID);
            if (Integer.MIN_VALUE != folderID) {
                stmt.setInt(parameterIndex++, folderID);
            }
            if (Long.MIN_VALUE != minLastModified) {
                stmt.setLong(parameterIndex++, minLastModified);
            }
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int updateMember(Connection connection, Table table, int contextID, DistListMember member, DistListMemberField[] fields) throws SQLException, OXException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(table).append(" SET ").append(Mappers.DISTLIST.getAssignments(fields)).append(" WHERE ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTACT_ID).getColumnLabel()).append("=?;"); 
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            Mappers.DISTLIST.setParameters(stmt, member, fields);
            stmt.setInt(1 + fields.length, contextID);
            stmt.setInt(2 + fields.length, member.getEntryID());
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    public int delete(Connection connection, Table table, int contextID, int folderID, int[] objectIDs, long minLastModified) 
        throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (Integer.MIN_VALUE != folderID) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        }
        if (Long.MIN_VALUE != minLastModified) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append(">?");
        }
        if (null != objectIDs && 0 < objectIDs.length) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
            if (1 == objectIDs.length) {
                stringBuilder.append('=').append(objectIDs[0]);
            } else {
                stringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
            }
        }
        if (Long.MIN_VALUE == minLastModified) {
            stringBuilder.append(';');
        } else {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
        }
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(parameterIndex++, contextID);
            if (Integer.MIN_VALUE != folderID) {
                stmt.setInt(parameterIndex++, folderID);
            }
            if (Long.MIN_VALUE != minLastModified) {
                stmt.setLong(parameterIndex++, minLastModified);
            }
            /*
             * execute and read out results
             */
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public int delete(Connection connection, Table table, int contextID, int folderID, int[] objectIDs) throws SQLException, OXException {
        return delete(connection, table, contextID, folderID, objectIDs, Long.MIN_VALUE);
    }
    
    public int deleteSingle(final Connection connection, final Table table, final int contextID, final int objectID, final long minLastModified) 
        throws SQLException, OXException {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("DELETE FROM ").append(table).append(" WHERE ")
        .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
        .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
    if (Long.MIN_VALUE == minLastModified) {
        stringBuilder.append(';');
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

    public int deleteSingle(final Connection connection, final Table table, final int contextID, final int objectID) 
    		throws SQLException, OXException {
        return this.deleteSingle(connection, table, contextID, objectID, Long.MIN_VALUE);
    } 
    
    private static String getCharset(final SortOptions sortOptions) {
    	if (null != sortOptions) {
  			final SuperCollator collator = SuperCollator.get(sortOptions.getCollation());
			if (null != collator && false == SuperCollator.DEFAULT.equals(collator)) {
				final String charset = collator.getSqlCharset();
				if (null != charset && false == charset.equals(SuperCollator.DEFAULT.getSqlCharset())) {
					return charset;
				}
			}
    	}
		return null; // no charset
    }
    
    private static ResultSet logExecuteQuery(final PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            LOG.debug("executeQuery: " + stmt.toString());
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
