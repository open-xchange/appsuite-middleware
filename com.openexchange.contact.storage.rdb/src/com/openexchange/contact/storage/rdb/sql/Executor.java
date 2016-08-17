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

package com.openexchange.contact.storage.rdb.sql;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.fields.Fields;
import com.openexchange.contact.storage.rdb.internal.DistListMember;
import com.openexchange.contact.storage.rdb.internal.Tools;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.search.AutocompleteAdapter;
import com.openexchange.contact.storage.rdb.search.ContactSearchAdapter;
import com.openexchange.contact.storage.rdb.search.FulltextAutocompleteAdapter;
import com.openexchange.contact.storage.rdb.search.SearchAdapter;
import com.openexchange.contact.storage.rdb.search.SearchTermAdapter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.search.SearchTerm;


/**
 * {@link Executor} - Constructs and performs the actual database statements.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Executor {

    /*-
     *
     * Maybe a feasible way to include distribution lists
     * ==================================================
     *
     *
     * SELECT field65 as email1, field66 as email2, field67 as email3, '' as email4 FROM prg_contacts
     *    WHERE prg_contacts.cid=1337 AND (field65 LIKE '%nonexistent1@nowhere.com%' OR field66 LIKE '%nonexistent1@nowhere.com%' OR field67 LIKE '%nonexistent1@nowhere.com%')
     *
     * UNION
     *
     * SELECT '' as email1, '' as email2, '' as email3, field04 as email4 FROM prg_dlist
     *    WHERE prg_dlist.cid=1337 AND field04 LIKE '%nonexistent1@nowhere.com%';
     *
     */

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Executor.class);

    /**
     * Initializes a new {@link Executor}.
     */
    public Executor() {
        super();
    }

    /**
     * Gets the number of records in a context.
     *
     * @param connection The db connection to use
     * @param table The database table to query
     * @param contextID The context ID
     * @return The number of records
     * @throws SQLException
     * @throws OXException
     */
    public long count(Connection connection, Table table, int contextID) throws SQLException, OXException {
        StringBuilder allocator = new StringBuilder(128);
        allocator.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?;");
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(allocator.toString());
            stmt.setInt(1, contextID);
            resultSet = logExecuteQuery(stmt);
            return resultSet.next() ? resultSet.getLong(1) : 0;
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    /**
     *  Gets the number of records in a folder.
     *
     * @param connection The db connection to use
     * @param table The database table to query
     * @param contextID The context ID
     * @param folderID The folder ID
     * @return The number of records
     * @throws SQLException
     * @throws OXException
     */
    public int count(Connection connection, Table table, int contextID, int userID, int folderID, boolean canReadAll) throws SQLException, OXException {
        StringBuilder allocator = new StringBuilder(128);
        if (canReadAll) {
            String pflag = Mappers.CONTACT.get(ContactField.PRIVATE_FLAG).getColumnLabel();
            allocator.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
                .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
                .append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=? AND ")
                .append("((").append(pflag).append(" IS NULL OR ").append(pflag).append("=0) OR (").append(Mappers.CONTACT.get(ContactField.CREATED_BY).getColumnLabel()).append("=?));");
        } else {
            allocator.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
                .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
                .append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=? AND ")
                .append(Mappers.CONTACT.get(ContactField.CREATED_BY).getColumnLabel()).append("=?;");
        }

        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(allocator.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, folderID);
            stmt.setInt(3, userID);
            resultSet = logExecuteQuery(stmt);
            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            return count;
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
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
        StringBuilder stringBuilder = new StringBuilder(256);
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
            return new ContactReader(contextID, connection, resultSet).readContact(fields, false);
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public Contact selectSingleGuestContact(Connection connection, Table table, int contextID, int userID, ContactField[] fields)
        throws SQLException, OXException {
    StringBuilder stringBuilder = new StringBuilder(256);
    stringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
        .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
        .append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=? AND ")
        .append(Mappers.CONTACT.get(ContactField.INTERNAL_USERID).getColumnLabel()).append("=?;");
    PreparedStatement stmt = null;
    ResultSet resultSet = null;
    try {
        stmt = connection.prepareStatement(stringBuilder.toString());
        stmt.setInt(1, contextID);
        stmt.setInt(2, FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID);
        stmt.setInt(3, userID);
        resultSet = logExecuteQuery(stmt);
            return new ContactReader(contextID, connection, resultSet).readContact(fields, false);
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
        ContactField[] fields, SearchTerm<O> term, SortOptions sortOptions, int forUser) throws SQLException, OXException {
        /*
         * construct query string
         */
    	SearchTermAdapter adapter = null != term ? new SearchTermAdapter(term, getCharset(sortOptions)) : null;
        StringBuilder StringBuilder = new StringBuilder(1024);
        StringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields, table.getName() + ".")).append(",object_use_count.value").append(" FROM ").append(table)
            .append(" LEFT JOIN ").append(Table.OBJECT_USE_COUNT).append(" ON ").append(table.getName()).append(".cid=").append(Table.OBJECT_USE_COUNT)
            .append(".cid AND ").append(forUser).append("=").append(Table.OBJECT_USE_COUNT).append(".user AND ")
            .append(table.getName()).append(".fid=").append(Table.OBJECT_USE_COUNT).append(".folder AND ")
            .append(table).append(".intfield01=").append(Table.OBJECT_USE_COUNT).append(".object ")
            .append(" WHERE ").append(table.getName()).append(".").append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (Integer.MIN_VALUE != folderID) {
        	StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        }
        if (null != objectIDs && 0 < objectIDs.length) {
        	StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
        	if (1 == objectIDs.length) {
        		StringBuilder.append('=').append(objectIDs[0]);
        	} else {
	        	StringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
        	}
        }
        if (Long.MIN_VALUE != minLastModified) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append(">?");
        }
        if (null != adapter) {
        	StringBuilder.append(" AND ").append(adapter.getClause());
        }
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)) {
        	StringBuilder.append(' ').append(Tools.getOrderClause(sortOptions));
        	if (0 < sortOptions.getLimit()) {
            	StringBuilder.append(' ').append(Tools.getLimitClause(sortOptions));
        	}
        }
        StringBuilder.append(';');
        /*
         * prepare statement
         */
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(StringBuilder.toString());
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
            return new ContactReader(contextID, connection, resultSet).readContacts(fields, false);
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
        StringBuilder StringBuilder = new StringBuilder(1024);
        StringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (Integer.MIN_VALUE != folderID) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        }
        if (null != objectIDs && 0 < objectIDs.length) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
            if (1 == objectIDs.length) {
                StringBuilder.append('=').append(objectIDs[0]);
            } else {
                StringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
            }
        }
        if (Long.MIN_VALUE != minLastModified) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append(">?");
        }
        if (null != adapter) {
            StringBuilder.append(" AND ").append(adapter.getClause());
        }
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)) {
            StringBuilder.append(' ').append(Tools.getOrderClause(sortOptions));
            if (0 < sortOptions.getLimit()) {
                StringBuilder.append(' ').append(Tools.getLimitClause(sortOptions));
            }
        }
        StringBuilder.append(';');
        /*
         * prepare statement
         */
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(StringBuilder.toString());
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
            return new ContactReader(contextID, connection, resultSet).readContacts(fields, false);
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    /**
     * Select contacts whose month/day portion of the date field falls between the supplied period. This does only work for the
     * 'birthday'- and 'anniversary' fields.
     *
     * @param connection The connection to use
     * @param contextID The context ID
     * @param folderIDs The folder IDs, or <code>null</code> if there's no restriction on folders
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @param fields The contact fields to select
     * @param sortOptions The sort options to apply
     * @param dateField One of <code>ContactField.ANNIVERSARY</code> or <code>ContactField.BIRTHDAY</code>
     * @return The found contacts
     * @throws SQLException
     * @throws OXException
     */
    public List<Contact> selectByAnnualDate(Connection connection, int contextID, int[] folderIDs, Date from, Date until,
        ContactField[] fields, SortOptions sortOptions, ContactField dateField) throws SQLException, OXException {
        /*
         * construct query string
         */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ").append(Mappers.CONTACT.getColumns(fields)).append(" FROM ").append(Table.CONTACTS)
            .append(" WHERE ").append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (null != folderIDs && 0 < folderIDs.length) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel());
            if (1 == folderIDs.length) {
                stringBuilder.append('=').append(folderIDs[0]);
            } else {
                stringBuilder.append(" IN (").append(Tools.toCSV(folderIDs)).append(')');
            }
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(from);
        int fromYear = calendar.get(Calendar.YEAR);
        calendar.setTime(until);
        int untilYear = calendar.get(Calendar.YEAR);
        String columnLabel = Mappers.CONTACT.get(dateField).getColumnLabel();
        stringBuilder.append(" AND (DATE_FORMAT(").append(columnLabel).append(",'%m-%d %T')>=DATE_FORMAT(?,'%m-%d %T') ")
            .append(untilYear == fromYear ? "AND" : "OR")
            .append(" DATE_FORMAT(").append(columnLabel).append(",'%m-%d %T')<DATE_FORMAT(?,'%m-%d %T'))");
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
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(parameterIndex++, contextID);
            stmt.setTimestamp(parameterIndex++, new Timestamp(from.getTime()));
            stmt.setTimestamp(parameterIndex++, new Timestamp(until.getTime()));
            /*
             * execute and read out results
             */
            resultSet = logExecuteQuery(stmt);
            return new ContactReader(contextID, connection, resultSet).readContacts(fields, false);
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    /**
     * Select contacts by the supplied auto-complete query.
     *
     * @param connection The connection to use
     * @param contextID The context ID
     * @param folderIDs The folder IDs, or <code>null</code> if there's no restriction on folders
     * @param query The query, as supplied by the client
     * @param parameters The {@link AutocompleteParameters}
     * @param fields The contact fields to select
     * @param sortOptions The sort options to apply
     * @return The found contacts
     * @throws SQLException
     * @throws OXException
     */
    public List<Contact> selectByAutoComplete(Connection connection, int contextID, int[] folderIDs, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws SQLException, OXException {
        /*
         * construct query string
         */
        SearchAdapter adapter;
        if (FulltextAutocompleteAdapter.hasFulltextIndex(connection, contextID)) {
            adapter = new FulltextAutocompleteAdapter(query, parameters, folderIDs,contextID, fields, getCharset(sortOptions));
        } else {
            adapter = new AutocompleteAdapter(query, parameters, folderIDs,contextID, fields, getCharset(sortOptions));
        }
        StringBuilder stringBuilder = adapter.getClause();
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)) {
            stringBuilder.append(' ').append(Tools.getOrderClause(sortOptions, true));
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
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            adapter.setParameters(stmt, parameterIndex);
            /*
             * execute and read out results
             */
            resultSet = logExecuteQuery(stmt);
            return new ContactReader(contextID, connection, resultSet).readContacts(fields, true);
        } finally {
            closeSQLStuff(resultSet, stmt);
        }
    }

    public List<Contact> select(Connection connection, Table table, int contextID, ContactSearchObject contactSearch,
        ContactField[] fields, SortOptions sortOptions, int forUser) throws SQLException, OXException {
        /*
         * construct query string
         */
        SearchAdapter adapter = new ContactSearchAdapter(contactSearch, contextID, fields, getCharset(sortOptions), forUser);
        StringBuilder stringBuilder = adapter.getClause();
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
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
           	adapter.setParameters(stmt, parameterIndex);
            /*
             * execute and read out results
             */
            resultSet = logExecuteQuery(stmt);
            return new ContactReader(contextID, connection, resultSet).readContacts(fields, false);
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
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(Mappers.DISTLIST.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel()).append("=? ")
            .append("ORDER BY CONCAT_WS('',").append(Mappers.DISTLIST.get(DistListMemberField.DISPLAY_NAME).getColumnLabel()).append(',')
            .append(Mappers.DISTLIST.get(DistListMemberField.MAIL).getColumnLabel()).append(") ASC;")
        ;
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
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(Mappers.DISTLIST.getColumns(fields)).append(" FROM ").append(table).append(" WHERE ")
            .append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel()).append(" IN (")
            .append(Tools.toCSV(objectIDs)).append(") ORDER BY CONCAT_WS('',")
            .append(Mappers.DISTLIST.get(DistListMemberField.DISPLAY_NAME).getColumnLabel()).append(',')
            .append(Mappers.DISTLIST.get(DistListMemberField.MAIL).getColumnLabel()).append(") ASC;")
        ;
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
        member.setUuid(UUID.randomUUID());
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

    public int insertFrom(Connection connection, Table from, Table to, int contextID, int folderID, int[] objectIDs, long maxLastModified) throws SQLException, OXException {
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
            if (Long.MIN_VALUE == maxLastModified) {
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
            if (Long.MIN_VALUE != maxLastModified && false == from.isDistListTable()) {
                stmt.setLong(parameterIndex++, maxLastModified);
            }
            /*
             * execute
             */
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Inserts or replaces an existing row in the 'to' table with the values read from the 'from' table, plus updates the supplied fields
     * to the values read from the contact update.
     *
     * @param connection A write connection to the database
     * @param from The table to read the data from
     * @param to The table to insert the data into
     * @param contextID The context ID
     * @param folderID The folder ID, or <code>Integer.MIN_VALUE</code> if not relevant
     * @param objectIDs The object IDs, or <code>null</code> if not relevant
     * @param maxLastModified The inclusive maximum modification time to consider, or <code>Long.MIN_VALUE</code> if not used
     * @param update The contact to get the updated field values from
     * @param updatedFields The fields that should not be copied from the other table but updated
     * @return The number of affected rows
     * @throws SQLException
     * @throws OXException
     */
    public int replaceToDeletedContactsAndUpdate(Connection connection,int contextID, int folderID, int[] objectIDs, long maxLastModified,
        Contact update, ContactField[] updatedFields) throws SQLException, OXException {
        if (null == updatedFields || 0 == updatedFields.length) {
            throw new IllegalArgumentException("need some updated fields");
        }
        if (Integer.MIN_VALUE == folderID && (null == objectIDs || 0 == objectIDs.length)) {
            throw new UnsupportedOperationException("need either a folder id or object ids");
        }
        /*
         * determine which fields to copy over
         */
        EnumSet<ContactField> copiedFieldsSet = EnumSet.copyOf(Fields.DEL_CONTACT_DATABASE);
        copiedFieldsSet.removeAll(Arrays.asList(updatedFields));
        ContactField[] copiedFields = copiedFieldsSet.toArray(new ContactField[copiedFieldsSet.size()]);
        /*
         * build statement
         */
        StringBuilder StringBuilder = new StringBuilder();
        StringBuilder.append("REPLACE INTO ").append(Table.DELETED_CONTACTS).append(" (")
            .append(Mappers.CONTACT.getColumns(copiedFields)).append(',')
            .append(Mappers.CONTACT.getColumns(updatedFields)).append(") SELECT ");
        for (ContactField copiedField : copiedFields) {
            StringBuilder.append(Table.CONTACTS).append('.').append(Mappers.CONTACT.get(copiedField).getColumnLabel()).append(',');
        }
        StringBuilder.append(Tools.getParameters(updatedFields.length)).append(" FROM ").append(Table.CONTACTS).append(" WHERE ");
        StringBuilder.append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (Integer.MIN_VALUE != folderID) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        }
        if (null != objectIDs && 0 < objectIDs.length) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
            if (1 == objectIDs.length) {
                StringBuilder.append('=').append(objectIDs[0]);
            } else {
                StringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
            }
        }
        if (Long.MIN_VALUE != maxLastModified) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?");
        }
        StringBuilder.append(';');
        /*
         * prepare statement
         */
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        try {
            stmt = connection.prepareStatement(StringBuilder.toString());
            for (ContactField updatedField : updatedFields) {
                Mappers.CONTACT.get(updatedField).set(stmt, parameterIndex++, update);
            }
            stmt.setInt(parameterIndex++, contextID);
            if (Integer.MIN_VALUE != folderID) {
                stmt.setInt(parameterIndex++, folderID);
            }
            if (Long.MIN_VALUE != maxLastModified) {
                stmt.setLong(parameterIndex++, maxLastModified);
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
        final long maxLastModified) throws SQLException, OXException {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("REPLACE INTO ").append(to).append(" SELECT * FROM ").append(from).append(" WHERE ");
    if (from.isDistListTable()) {
        stringBuilder.append(Mappers.DISTLIST.get(DistListMemberField.CONTEXT_ID).getColumnLabel()).append("=? AND ")
            .append(Mappers.DISTLIST.get(DistListMemberField.PARENT_CONTACT_ID).getColumnLabel()).append("=?");
    } else {
        stringBuilder.append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
    }
    if (Long.MIN_VALUE == maxLastModified) {
        stringBuilder.append(';');
    } else {
        stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
    }
    PreparedStatement stmt = null;
    try {
        stmt = connection.prepareStatement(stringBuilder.toString());
        stmt.setInt(1, contextID);
        stmt.setInt(2, objectID);
        if (Long.MIN_VALUE != maxLastModified) {
            stmt.setLong(3, maxLastModified);
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

    /**
     * Inserts or replaces a contact in the supplied table. If an old row in the table has the same value as a new row for a PRIMARY KEY
     * or a UNIQUE index, the old row is deleted before the new row is inserted.
     *
     * @param connection A writable connection
     * @param table The table to use
     * @param contextID The context ID
     * @param objectID The object ID
     * @param maxLastModified The inclusive maximum modification time to consider, or <code>Long.MIN_VALUE</code> if not used
     * @param contact The contact to insert or replace
     * @param fields The affected fields
     * @return The number of affected rows
     * @throws SQLException
     * @throws OXException
     */
    public int replace(Connection connection, Table table, int contextID, int objectID, long maxLastModified, Contact contact,
        ContactField[] fields) throws SQLException, OXException {
        StringBuilder StringBuilder = new StringBuilder();
        StringBuilder.append("REAPLCE INTO ").append(table).append(" SET ").append(Mappers.CONTACT.getAssignments(fields))
            .append(" WHERE ").append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel())
            .append("=? AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
        if (Long.MIN_VALUE != maxLastModified) {
            StringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?");
        }
        StringBuilder.append(';');
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(StringBuilder.toString());
            Mappers.CONTACT.setParameters(stmt, contact, fields);
            stmt.setInt(1 + fields.length, contextID);
            stmt.setInt(2 + fields.length, objectID);
            if (Long.MIN_VALUE != maxLastModified) {
                stmt.setLong(3 + fields.length, maxLastModified);
            }
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public int update(Connection connection, Table table, int contextID, int objectID, long maxLastModified, Contact contact, ContactField[] fields) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(table).append(" SET ").append(Mappers.CONTACT.getAssignments(fields)).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
        if (Long.MIN_VALUE == maxLastModified) {
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
            if (Long.MIN_VALUE != maxLastModified) {
                stmt.setLong(3 + fields.length, maxLastModified);
            }
            return logExecuteUpdate(stmt);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public int update(Connection connection, Table table, int contextID, int folderID, int[] objectIDs, Contact template, ContactField[] fields, long maxLastModified) throws SQLException, OXException {
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
        if (Long.MIN_VALUE == maxLastModified) {
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
            if (Long.MIN_VALUE != maxLastModified) {
                stmt.setLong(parameterIndex++, maxLastModified);
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

    private void deleteFromObjectUseCountTable(Connection connection, int contextID, int folderID, int[] objectIDs) throws OXException, SQLException {
        if (null != objectIDs && objectIDs.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM object_use_count WHERE cid=?");
            if (Integer.MIN_VALUE != folderID) {
                sb.append(" AND folder=?");
            }
            sb.append(" AND object");
            if (objectIDs.length == 1) {
                sb.append("=").append(objectIDs[0]);
            } else {
                sb.append(" IN (").append(Tools.toCSV(objectIDs)).append(")");
            }
            sb.append(";");
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement(sb.toString());
                stmt.setInt(1, contextID);
                if (Integer.MIN_VALUE != folderID) {
                    stmt.setInt(2, folderID);
                }
                logExecuteUpdate(stmt);
                //} catch (SQLException e) {
                //    LOG.warn("Could not delete contacts from object_use_count table: {}", e.getMessage());
            } finally {
                closeSQLStuff(stmt);
            }
        }
    }

    private void deleteSingleFromObjectUseCountTable(Connection connection, int contextID, int objectID) throws OXException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM object_use_count WHERE cid=?");
        if (Integer.MIN_VALUE != objectID) {
            sb.append(" AND object=?");
        }
        sb.append(";");
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sb.toString());
            stmt.setInt(1, contextID);
            if (Integer.MIN_VALUE != objectID) {
                stmt.setInt(2, objectID);
            }
            logExecuteUpdate(stmt);
            //} catch (SQLException e) {
            //    LOG.warn("Could not delete contact from object_use_count table: {}", e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public int delete(Connection connection, Table table, int contextID, int folderID, int[] objectIDs, long maxLastModified)
        throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=?");
        if (Integer.MIN_VALUE != folderID) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel()).append("=?");
        }
        if (Long.MIN_VALUE != maxLastModified) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?");
        }
        if (null != objectIDs && 0 < objectIDs.length) {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel());
            if (1 == objectIDs.length) {
                stringBuilder.append('=').append(objectIDs[0]);
            } else {
                stringBuilder.append(" IN (").append(Tools.toCSV(objectIDs)).append(')');
            }
        }
        stringBuilder.append(';');
        PreparedStatement stmt = null;
        int parameterIndex = 1;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(parameterIndex++, contextID);
            if (Integer.MIN_VALUE != folderID) {
                stmt.setInt(parameterIndex++, folderID);
            }
            if (Long.MIN_VALUE != maxLastModified) {
                stmt.setLong(parameterIndex++, maxLastModified);
            }
            /*
             * execute and read out results
             */
            int result = logExecuteUpdate(stmt);
            deleteFromObjectUseCountTable(connection, contextID, folderID, objectIDs);
            return result;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public int delete(Connection connection, Table table, int contextID, int folderID, int[] objectIDs) throws SQLException, OXException {
        return delete(connection, table, contextID, folderID, objectIDs, Long.MIN_VALUE);
    }

    public int deleteSingle(Connection connection, Table table, int contextID, int objectID, long maxLastModified) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM ").append(table).append(" WHERE ")
            .append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=? AND ")
            .append(Mappers.CONTACT.get(ContactField.OBJECT_ID).getColumnLabel()).append("=?");
        if (Long.MIN_VALUE == maxLastModified) {
            stringBuilder.append(';');
        } else {
            stringBuilder.append(" AND ").append(Mappers.CONTACT.get(ContactField.LAST_MODIFIED).getColumnLabel()).append("<=?;");
        }
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            if (Long.MIN_VALUE != maxLastModified) {
                stmt.setLong(3, maxLastModified);
            }
            int result = logExecuteUpdate(stmt);
            deleteSingleFromObjectUseCountTable(connection, contextID, objectID);
            return result;
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
        try {
            if (false == LOG.isDebugEnabled()) {
                return stmt.executeQuery();
            }
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", stmt.toString(), (System.currentTimeMillis() - start));
            return resultSet;
        } catch (SQLException e) {
            LOG.warn("Error executing \"{}\": {}", stmt, e.getMessage());
            throw e;
        }
    }

    private static int logExecuteUpdate(final PreparedStatement stmt) throws SQLException {
        try {
            if (false == LOG.isDebugEnabled()) {
                return stmt.executeUpdate();
            }
            long start = System.currentTimeMillis();
            final int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), rowCount, (System.currentTimeMillis() - start));
            return rowCount;
        } catch (SQLException e) {
            LOG.warn("Error executing \"{}\": {}", stmt, e.getMessage());
            throw e;
        }
    }

}
