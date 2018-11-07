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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.Operand.Type;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SearchTerm.OperationPosition;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.tools.StringCollection;

/**
 * {@link SearchAdapter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SearchAdapter {

    private final int contextID;
    private final StringBuilder stringBuilder;
    private final List<Object> parameters;
    private final String charset;
    private final String prefixEvents;
    private final String prefixAttendees;

    private boolean usesAttendees;
    private boolean usesEvents;

    /**
     * Initializes a new {@link SearchAdapter}.
     *
     * @param The context identifier
     * @param charset The optional charset to use for string comparisons, or <code>null</code> if not specified
     * @param prefixEvents The prefix to use when inserting column operands for event fields
     * @param prefixAttendees The prefix to use when inserting column operands for internal attendee fields
     * @param prefixExternalAttendees The prefix to use when inserting column operands for external attendee fields
     */
    public SearchAdapter(int contextID, String charset, String prefixEvents, String prefixAttendees) throws OXException {
        super();
        this.contextID = contextID;
        this.charset = charset;
        this.prefixEvents = prefixEvents;
        this.prefixAttendees = prefixAttendees;
        this.parameters = new ArrayList<Object>();
        this.stringBuilder = new StringBuilder(256);
    }

    /**
     * Appends the supplied search term to the resulting SQL statement.
     *
     * @param term The search term to append
     * @return A self reference
     */
    public SearchAdapter append(SearchTerm<?> term) throws OXException {
        if (null != term) {
            if (SingleSearchTerm.class.isInstance(term)) {
                append((SingleSearchTerm) term);
            } else if (CompositeSearchTerm.class.isInstance(term)) {
                append((CompositeSearchTerm) term);
            } else {
                throw new IllegalArgumentException("Need either an 'SingleSearchTerm' or 'CompositeSearchTerm'.");
            }
        }
        return this;
    }

    /**
     * Appends the supplied search filters to the resulting SQL statement.
     *
     * @param filtere The filters to append
     * @return A self reference
     */
    public SearchAdapter append(List<SearchFilter> filters) throws OXException {
        if (null != filters && 0 < filters.size()) {
            if (0 == stringBuilder.length()) {
                stringBuilder.append("TRUE");
            }
            for (SearchFilter filter : filters) {
                append(filter);
            }
        }
        return this;
    }

    /**
     * Gets a value indicating whether properties of attendees are present in the search term or not.
     *
     * @return <code>true</code> if attendees are used, <code>false</code>, otherwise
     */
    public boolean usesAttendees() {
        return usesAttendees;
    }

    /**
     * Gets a value indicating whether properties of events are present in the search term or not.
     *
     * @return <code>true</code> if events are used, <code>false</code>, otherwise
     */
    public boolean usesEvents() {
        return usesEvents;
    }

    /**
     * Gets the constructed <code>WHERE</code>-clause for the search term to be used in the database statement, without the leading
     * <code>WHERE</code>.
     *
     * @return The search clause
     */
    public String getClause() {
        String clause = stringBuilder.toString().trim();
        return 0 < clause.length() ? clause : "TRUE";
    }

    /**
     * Inserts the constant parameters discovered in the search term into the supplied prepared statement.
     *
     * @param stmt The prepared statement to populate
     * @param parameterIndex The parameter index to begin with
     * @return The incremented parameter index
     */
    public int setParameters(PreparedStatement stmt, int parameterIndex) throws SQLException {
        for (Object parameter : parameters) {
            stmt.setObject(parameterIndex++, parameter);
        }
        return parameterIndex;
    }

    private void append(SearchFilter filter) throws OXException {
        List<String> fields = filter.getFields();
        if (null != fields && 0 < fields.size()) {
            for (String field : fields) {
                appendFieldFilter(field, filter.getQueries());
            }
        }
    }

    private void appendFieldFilter(String field, List<String> queries) throws OXException {
        switch (field) {
            case "subject":
                appendFieldFilter(EventField.SUMMARY, queries);
                break;
            case "location":
                appendFieldFilter(EventField.LOCATION, queries);
                break;
            case "description":
                appendFieldFilter(EventField.DESCRIPTION, queries);
                break;
            case "range":
                appendRangeFilter(queries);
                break;
            case "type":
                appendRecurringType(queries);
                break;
            case "status":
                appendStatus(queries);
                break;
            case "users":
                appendUsers(queries);
                break;
            case "participants":
                appendExternalParticipants(queries);
                break;
            case "attachment":
                appendAttachments(queries);
                break;
            default:
                throw new IllegalArgumentException("Unsupported filter field: " + field);
        }
    }

    private void appendAttachments(List<String> queries) throws OXException {
        for (String query : queries) {
            stringBuilder.append(" AND EXISTS (SELECT 1 FROM prg_attachment WHERE prg_attachment.cid=");
            appendConstantOperand(Integer.valueOf(contextID), Types.INTEGER);
            stringBuilder.append(" AND 0=").append(prefixEvents).append("account AND prg_attachment.attached=").append(prefixEvents).append("id AND prg_attachment.filename = ");
            appendConstantOperand(query, Types.VARCHAR);
            stringBuilder.append(')');
        }
    }

    private void appendUsers(List<String> queries) throws OXException {
        for (String query : queries) {
            stringBuilder.append(" AND EXISTS (SELECT 1 FROM calendar_attendee WHERE calendar_attendee.cid=");
            appendConstantOperand(Integer.valueOf(contextID), Types.INTEGER);
            stringBuilder.append(" AND calendar_attendee.account=").append(prefixEvents).append("account AND calendar_attendee.event=").append(prefixEvents).append("id AND calendar_attendee.entity=");
            appendConstantOperand(query, Types.INTEGER);
            stringBuilder.append(')');
        }
    }

    private void appendExternalParticipants(List<String> queries) throws OXException {
        for (String query : queries) {
            stringBuilder.append(" AND EXISTS (SELECT 1 FROM calendar_attendee WHERE calendar_attendee.cid=");
            appendConstantOperand(Integer.valueOf(contextID), Types.INTEGER);
            stringBuilder.append(" AND calendar_attendee.account=").append(prefixEvents).append("account AND calendar_attendee.event=").append(prefixEvents).append("id AND calendar_attendee.uri=");
            appendConstantOperand(query, Types.VARCHAR);
            stringBuilder.append(')');
        }
    }

    private void appendRangeFilter(List<String> queries) throws OXException {
        Date now = new Date();
        for (String query : queries) {
            Date from;
            Date until;
            switch (query) {
                case "one_month":
                    from = CalendarUtils.add(now, Calendar.MONTH, -1);
                    until = CalendarUtils.add(now, Calendar.MONTH, +1);
                    break;
                case "three_months":
                    from = CalendarUtils.add(now, Calendar.MONTH, -3);
                    until = CalendarUtils.add(now, Calendar.MONTH, +3);
                    break;
                case "one_year":
                    from = CalendarUtils.add(now, Calendar.YEAR, -1);
                    until = CalendarUtils.add(now, Calendar.YEAR, +1);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported filter query: " + query);
            }
            stringBuilder.append(" AND ").append(prefixEvents).append("rangeUntil>? AND ").append(prefixEvents).append("rangeFrom<?");
            parameters.add(Long.valueOf(from.getTime()));
            parameters.add(Long.valueOf(until.getTime()));
            usesEvents = true;
        }
    }

    private void appendRecurringType(List<String> queries) throws OXException {
        for (String query : queries) {
            stringBuilder.append(" AND ");
            switch (query) {
                case "series":
                    append(getSearchTerm(EventField.SERIES_ID, SingleOperation.GREATER_THAN, Integer.valueOf(0)));
                    break;
                case "single":
                    append(getSearchTerm(EventField.SERIES_ID, SingleOperation.ISNULL));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported filter query: " + query);
            }
        }
    }

    private void appendStatus(List<String> queries) throws OXException {
        if (2 > queries.size()) {
            throw new IllegalArgumentException("Unsupported status filter");
        }
        int entity;
        try {
            entity = Integer.parseInt(queries.get(0));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unsupported status filter", e);
        }
        List<Object> partStats = new ArrayList<Object>(queries.size() - 1);
        for (int i = 1; i < queries.size(); i++) {
            partStats.add(new ParticipationStatus(queries.get(i)));
        }
        DbMapping<? extends Object, Attendee> entityMapping = AttendeeMapper.getInstance().get(AttendeeField.ENTITY);
        DbMapping<? extends Object, Attendee> partStatMapping = AttendeeMapper.getInstance().get(AttendeeField.PARTSTAT);
        stringBuilder.append(" AND (").append(entityMapping.getColumnLabel(prefixAttendees)).append(" = ? AND ");
        parameters.add(Integer.valueOf(entity));
        if (1 == partStats.size()) {
            stringBuilder.append(partStatMapping.getColumnLabel(prefixAttendees)).append(" = ?");
            parameters.add(((ParticipationStatus) partStats.get(0)).getValue());
        } else {
            appendAsInClause(partStatMapping, prefixAttendees, partStats);
        }
        usesAttendees = true;
        stringBuilder.append(") ");
    }

    private void appendFieldFilter(EventField field, List<String> queries) throws OXException {
        if (null != queries && 0 < queries.size()) {
            for (String query : queries) {
                stringBuilder.append(" AND ");
                append(getSearchTerm(field, SingleOperation.EQUALS, query));
            }
        }
    }

    private void append(SingleSearchTerm term) throws OXException {
        /*
         * get relevant mapping for term
         */
        Map<String, DbMapping<? extends Object, ?>> mappings = getFirstColumnMappings(term);
        if (null == mappings) {
            throw new IllegalArgumentException("Term contains unmappable field.");
        }
        /*
         * append operation
         */
        Operation operation = term.getOperation();
        Operand<?>[] operands = term.getOperands();
        Iterator<Entry<String, DbMapping<? extends Object, ?>>> iterator = mappings.entrySet().iterator();
        Entry<String, DbMapping<? extends Object, ?>> firstMapping = iterator.next();
        if (false == iterator.hasNext()) {
            appendOperation(operation, operands, firstMapping.getValue(), firstMapping.getKey());
        } else {
            stringBuilder.append("(");
            appendOperation(operation, operands, firstMapping.getValue(), firstMapping.getKey());
            do {
                stringBuilder.append(" OR ");
                Entry<String, DbMapping<? extends Object, ?>> mapping = iterator.next();
                appendOperation(operation, operands, mapping.getValue(), mapping.getKey());
            } while (iterator.hasNext());
            stringBuilder.append(")");
        }
    }

    private void appendOperation(Operation operation, Operand<?>[] operands, DbMapping<? extends Object, ?> mapping, String prefix) throws OXException {
        stringBuilder.append('(');
        for (int i = 0; i < operands.length; i++) {
            if (OperationPosition.BEFORE.equals(operation.getSqlPosition())) {
                stringBuilder.append(operation.getSqlRepresentation());
            }
            if (Operand.Type.COLUMN.equals(operands[i].getType())) {
                Entry<String, DbMapping<? extends Object, ?>> entry = getMapping(operands[i].getValue());
                appendColumnOperand(entry.getValue(), entry.getKey());
            } else if (Operand.Type.CONSTANT.equals(operands[i].getType())) {
                appendConstantOperand(operands[i].getValue(), mapping.getSqlType());
            } else {
                throw new IllegalArgumentException("unknown type in operand: " + operands[i].getType());
            }
            if (OperationPosition.AFTER.equals(operation.getSqlPosition())) {
                stringBuilder.append(' ').append(operation.getSqlRepresentation());
            } else if (OperationPosition.BETWEEN.equals(operation.getSqlPosition()) && i != operands.length - 1) {
                //don't place an operator after the last operand here
                stringBuilder.append(' ').append(operation.getSqlRepresentation()).append(' ');
            }
        }
        stringBuilder.append(')');
    }

    private void append(CompositeSearchTerm term) throws OXException {
        stringBuilder.append(" ( ");
        if (false == appendAsInClause(term)) {
            Operation operation = term.getOperation();
            SearchTerm<?>[] terms = term.getOperands();
            if (OperationPosition.BEFORE.equals(operation.getSqlPosition())) {
                stringBuilder.append(operation.getSqlRepresentation());
            }
            for (int i = 0; i < terms.length; i++) {
                append(terms[i]);
                if (OperationPosition.AFTER.equals(operation.getSqlPosition())) {
                    stringBuilder.append(' ').append(operation.getSqlRepresentation());
                } else if (OperationPosition.BETWEEN.equals(operation.getSqlPosition()) && i != terms.length - 1) {
                    //don't place an operator after the last operand
                    stringBuilder.append(' ').append(operation.getSqlRepresentation()).append(' ');
                }
            }
        }
        stringBuilder.append(" ) ");
    }

    /**
     * Tries to interpret and append a composite term as <code>IN</code>-clause, so that a composite 'OR' term where each nested 'EQUALS'
     * operation targets the same column gets optimized to a suitable <code>column IN (value1,value2,...)</code>.
     *
     * @param compositeTerm The composite term
     * @return <code>true</code>, if the term was appended as 'IN' clause, <code>false</code>, otherwise
     */
    private boolean appendAsInClause(CompositeSearchTerm compositeTerm) throws OXException {
        /*
         * check if operation & operands are applicable
         */
        if (false == CompositeOperation.OR.equals(compositeTerm.getOperation())) {
            return false; // only 'OR' composite operations
        }
        if (null == compositeTerm.getOperands() || 2 > compositeTerm.getOperands().length) {
            return false; // at least 2 operands
        }
        List<Object> constantValues = new ArrayList<Object>();
        Object commonColumnValue = null;
        for (SearchTerm<?> term : compositeTerm.getOperands()) {
            if (false == SingleSearchTerm.class.isInstance(term) || false == SingleOperation.EQUALS.equals(term.getOperation())) {
                return false; // only nested single search terms with 'EQUALS' operations
            }
            Object columnValue = null;
            Object constantValue = null;
            for (Operand<?> operand : ((SingleSearchTerm) term).getOperands()) {
                if (Type.COLUMN.equals(operand.getType())) {
                    columnValue = operand.getValue();
                } else if (Type.CONSTANT.equals(operand.getType())) {
                    constantValue = operand.getValue();
                } else {
                    return false; // only 'COLUMN' = 'CONSTANT' operations
                }
            }
            if (null == columnValue || null == constantValue) {
                return false; // only 'COLUMN' = 'CONSTANT' operations
            }
            if (String.class.isInstance(constantValue) && StringCollection.containsWildcards((String) constantValue)) {
                return false; // no wildcard comparisons
            }
            if (null == commonColumnValue) {
                commonColumnValue = columnValue; // first column value
            } else if (false == commonColumnValue.equals(columnValue)) {
                return false; // only equal column value
            }
            constantValues.add(constantValue);
        }
        if (null == commonColumnValue || 2 > constantValues.size()) {
            return false;
        }
        /*
         * all checks passed, build IN clause
         */
        Iterator<Entry<String, DbMapping<? extends Object, ?>>> iterator = getMappings(commonColumnValue).entrySet().iterator();
        Entry<String, DbMapping<? extends Object, ?>> firstMapping = iterator.next();
        if (false == iterator.hasNext()) {
            appendAsInClause(firstMapping.getValue(), firstMapping.getKey(), constantValues);
        } else {
            stringBuilder.append("(");
            appendAsInClause(firstMapping.getValue(), firstMapping.getKey(), constantValues);
            do {
                stringBuilder.append(" OR ");
                Entry<String, DbMapping<? extends Object, ?>> mapping = iterator.next();
                appendAsInClause(mapping.getValue(), mapping.getKey(), constantValues);
            } while (iterator.hasNext());
            stringBuilder.append(")");
        }
        return true;
    }

    private void appendAsInClause(DbMapping<? extends Object, ?> mapping, String prefix, List<Object> constantValues) throws OXException {
        appendColumnOperand(mapping, prefix);
        stringBuilder.append(" IN (");
        appendConstantOperand(constantValues.get(0), mapping.getSqlType());
        for (int i = 1; i < constantValues.size(); i++) {
            stringBuilder.append(',');
            appendConstantOperand(constantValues.get(i), mapping.getSqlType());
        }
        stringBuilder.append(") ");
    }

    private void appendConstantOperand(Object value, int sqlType) throws OXException {
        if (String.class.isInstance(value)) {
            appendConstantOperand((String) value, sqlType);
            return;
        }
        if (Boolean.class.isInstance(value) && Types.INTEGER == sqlType) {
            parameters.add(Integer.valueOf(Boolean.TRUE.equals(value) ? 1 : 0));
        } else if (Long.class.isInstance(value) && Types.TIMESTAMP == sqlType) {
            parameters.add(new Timestamp(((Long) value).longValue()));
        } else if (Date.class.isInstance(value) && Types.TIMESTAMP == sqlType) {
            parameters.add(new Timestamp(((Date) value).getTime()));
        } else if (Date.class.isInstance(value) && Types.BIGINT == sqlType) {
            parameters.add(Long.valueOf(((Date) value).getTime()));
        } else if (ParticipationStatus.class.isInstance(value) && Types.VARCHAR == sqlType) {
            parameters.add(((ParticipationStatus) value).getValue());
        } else if (Transp.class.isInstance(value) && Types.VARCHAR == sqlType) {
            parameters.add(((Transp) value).getValue());
        } else {
            // default
            parameters.add(value);
        }
        stringBuilder.append('?');
    }

    private void appendConstantOperand(String value, int sqlType) throws OXException {
        if (Types.INTEGER == sqlType) {
            if ("true".equalsIgnoreCase(value)) {
                // special handling for "true" string
                parameters.add(Integer.valueOf(1));
            } else if ("false".equalsIgnoreCase(value)) {
                // special handling for "false" string
                parameters.add(Integer.valueOf(0));
            } else {
                // try to parse
                parameters.add(Integer.valueOf(value));
            }
        } else {
            if (StringCollection.containsWildcards(value)) {
                // use "LIKE" search
                parameters.add(StringCollection.prepareForSearch(value, false, true));
                int index = stringBuilder.lastIndexOf("=");
                stringBuilder.replace(index, index + 1, "LIKE");
            } else {
                // use "EQUALS" search
                parameters.add(value);
            }
        }
        stringBuilder.append('?');
    }

    private void appendColumnOperand(DbMapping<? extends Object, ?> mapping, String prefix) throws OXException {
        String columnLabel = null != prefix ? mapping.getColumnLabel(prefix) : mapping.getColumnLabel();
        if (null != charset && Types.VARCHAR == mapping.getSqlType()) {
            stringBuilder.append("CONVERT(").append(columnLabel).append(" USING ").append(charset).append(')');
        } else {
            stringBuilder.append(columnLabel);
        }
    }

    private Map<String, DbMapping<? extends Object, ?>> getFirstColumnMappings(SingleSearchTerm term) throws OXException {
        for (Operand<?> operand : term.getOperands()) {
            if (Operand.Type.COLUMN.equals(operand.getType())) {
                return getMappings(operand.getValue());
            }
        }
        return null;
    }

    private Map<String, DbMapping<? extends Object, ?>> getMappings(Object value) throws OXException {
        if (EventField.class.isInstance(value)) {
            DbMapping<? extends Object, ?> mapping = EventMapper.getInstance().opt((EventField) value);
            if (null == mapping) {
                throw new IllegalArgumentException("No mapping available for: " + value);
            }
            usesEvents = true;
            return Collections.<String, DbMapping<? extends Object, ?>> singletonMap(prefixEvents, mapping);
        }
        if (AttendeeField.class.isInstance(value)) {
            DbMapping<? extends Object, Attendee> attendeeMapping = AttendeeMapper.getInstance().opt((AttendeeField) value);
            if (null == attendeeMapping) {
                throw new IllegalArgumentException("No mapping available for: " + value);
            }
            usesAttendees = true;
            return Collections.<String, DbMapping<? extends Object, ?>> singletonMap(prefixAttendees, attendeeMapping);
        }
        throw new IllegalArgumentException("No mapping available for: " + value);
    }

    private Map.Entry<String, DbMapping<? extends Object, ?>> getMapping(Object value) throws OXException {
        Set<Entry<String, DbMapping<? extends Object, ?>>> entries = getMappings(value).entrySet();
        if (1 < entries.size()) {
            throw new IllegalArgumentException("Found multiple mappings for: " + value);
        }
        return entries.iterator().next();
    }

}
