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

package com.openexchange.chronos.storage.rdb.legacy;

import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.tools.arrays.Collections.put;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.AttendeeMapper;
import com.openexchange.chronos.storage.rdb.RdbStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAttendeeStorage extends RdbStorage implements AttendeeStorage {

    private final EntityResolver entityResolver;

    /**
     * Initializes a new {@link RdbAttendeeStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbAttendeeStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.entityResolver = entityResolver;
    }

    @Override
    public void insertAttendees(String objectID, List<Attendee> attendees) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void insertAttendees(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void updateAttendee(String objectID, Attendee attendee) throws OXException {
        updateAttendees(objectID, Collections.singletonList(attendee));
    }

    @Override
    public void updateAttendees(String objectID, List<Attendee> attendees) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void insertAttendeeTombstone(String objectID, Attendee attendee) throws OXException {
        insertAttendeeTombstones(objectID, Collections.singletonList(attendee));
    }

    @Override
    public void insertAttendeeTombstones(String objectID, List<Attendee> attendees) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void insertAttendeeTombstones(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public List<Attendee> loadAttendees(String eventId) throws OXException {
        return loadAttendees(new String[] { eventId }).get(eventId);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds) throws OXException {
        return loadAttendees(eventIds, null);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds, Boolean internal) throws OXException {
        return loadAttendees(eventIds, internal, false);
    }

    @Override
    public Map<String, Integer> loadAttendeeCounts(String[] eventIds, Boolean internal) throws OXException {
        Map<String, Integer> attendeeCountsByEventId = new HashMap<String, Integer>(eventIds.length);
        for (Entry<String, List<Attendee>> entry : loadAttendees(eventIds, internal).entrySet()) {
            attendeeCountsByEventId.put(entry.getKey(), I(entry.getValue().size()));
        }
        return attendeeCountsByEventId;
    }

    @Override
    public Map<String, Attendee> loadAttendee(String[] eventIds, Attendee attendee, AttendeeField[] fields) throws OXException {
        Map<String, Attendee> attendeePerEventId = new HashMap<String, Attendee>(eventIds.length);
        for (Entry<String, List<Attendee>> entry : loadAttendees(eventIds, null).entrySet()) {
            Attendee matchingAttendee = find(entry.getValue(), attendee);
            if (null != matchingAttendee) {
                attendeePerEventId.put(entry.getKey(), matchingAttendee);
            }
        }
        return attendeePerEventId;
    }

    @Override
    public Map<String, List<Attendee>> loadAttendeeTombstones(String[] eventIds) throws OXException {
        return loadAttendees(eventIds, null, true);
    }

    @Override
    public void deleteAttendees(String objectID, List<Attendee> attendees) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteAttendees(String objectID) throws OXException {
        deleteAttendees(Collections.singletonList(objectID));
    }

    @Override
    public void deleteAttendees(List<String> objectIDs) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public boolean deleteAllAttendees() throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    private Map<String, List<Attendee>> loadAttendees(String[] eventIds, Boolean internal, boolean tombstones) throws OXException {
        if (null == eventIds || 0 == eventIds.length) {
            return java.util.Collections.emptyMap();
        }
        Map<String, List<Attendee>> attendeesById = new HashMap<String, List<Attendee>>(eventIds.length);
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            /*
             * select raw attendee data & pre-fetch referenced internal entities
             */
            Map<String, List<Attendee>> userAttendeeData;
            Map<String, List<Attendee>> internalAttendeeData;
            Map<String, List<Attendee>> externalAttendeeData;
            if (false == Boolean.FALSE.equals(internal)) {
                userAttendeeData = selectUserAttendeeData(connection, context.getContextId(), eventIds, tombstones);
                internalAttendeeData = selectInternalAttendeeData(connection, context.getContextId(), eventIds, tombstones);
                prefetchEntities(internalAttendeeData.values(), userAttendeeData.values());
            } else {
                userAttendeeData = null;
                internalAttendeeData = null;
            }
            if (false == tombstones || false == Boolean.TRUE.equals(internal)) {
                externalAttendeeData = selectExternalAttendeeData(connection, context.getContextId(), eventIds);
            } else {
                externalAttendeeData = null;
            }
            /*
             * generate resulting attendee lists per event identifier
             */
            for (String eventId : eventIds) {
                attendeesById.put(eventId, getAttendees(eventId, tombstones,
                    null != internalAttendeeData ? internalAttendeeData.get(eventId) : null,
                    null != userAttendeeData ? userAttendeeData.get(eventId) : null,
                    null != externalAttendeeData ? externalAttendeeData.get(eventId) : null))
                ;
            }
            return attendeesById;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Constructs the final attendee list for an event by pre-processing and merging the supplied lists of loaded attendees.
     *
     * @param eventId The identifier of the corresponding event
     * @param tombstones <code>true</code> if attendee tombstones are read, <code>false</code>, otherwise
     * @param internalAttendees The internal attendees as loaded from the storage
     * @param userAttendees The user attendees as loaded from the storage
     * @param externalAttendees The external attendees as loaded from the storage
     * @return The merged list of attendees
     */
    private List<Attendee> getAttendees(String eventId, boolean tombstones, List<Attendee> internalAttendees, List<Attendee> userAttendees, List<Attendee> externalAttendees) throws OXException {
        List<Attendee> attendees = new ArrayList<Attendee>();
        /*
         * add user attendees individually if listed in internal attendees or when reading tombstone data, or as member of a group if not
         */
        if (null != userAttendees) {
            for (Attendee userAttendee : userAttendees) {
                try {
                    userAttendee = entityResolver.applyEntityData(sanitizeCUType(eventId, userAttendee));
                } catch (OXException e) {
                    if ("CAL-4034".equals(e.getErrorCode())) {
                        /*
                         * invalid calendar user; possibly a no longer existing user - add as external attendee as fallback if possible
                         */
                        Attendee externalAttendee = CalendarUtils.asExternal(userAttendee, AttendeeMapper.getInstance().getMappedFields());
                        if (null == externalAttendee) {
                            externalAttendee = CalendarUtils.asExternal(find(internalAttendees, userAttendee.getEntity()), AttendeeMapper.getInstance().getMappedFields());
                        }
                        if (null != externalAttendee) {
                            attendees.add(entityResolver.applyEntityData(externalAttendee));
                            String message = "Falling back to external attendee representation for non-existent user " + userAttendee;
                            addInvalidDataWarning(eventId, EventField.ATTENDEES, ProblemSeverity.MINOR, message, e);
                        } else {
                            addInvalidDataWarning(eventId, EventField.ATTENDEES, ProblemSeverity.NORMAL, "Skipping non-existent user " + userAttendee, e);
                        }
                        continue;
                    }
                    throw e;
                }
                if (tombstones || null != find(internalAttendees, userAttendee.getEntity())) {
                    /*
                     * attendee tombstone or individual user attendee
                     */
                    attendees.add(userAttendee);
                } else {
                    int[] groupIDs = findGroupIDs(internalAttendees, userAttendee.getEntity());
                    if (null != groupIDs && 0 < groupIDs.length) {
                        /*
                         * suitable group membership(s) found, apply in "member" property
                         */
                        List<String> groupMemberships = new ArrayList<String>(groupIDs.length);
                        for (int groupID : groupIDs) {
                            groupMemberships.add(ResourceId.forGroup(context.getContextId(), groupID));
                        }
                        userAttendee.setMember(groupMemberships);
                        attendees.add(userAttendee);
                    } else if (isIgnoreOrphanedUserAttendees()) {
                        /*
                         * no suitable entry in prg_date_rights (anymore), skip attendee
                         */
                        addInvalidDataWarning(eventId, EventField.ATTENDEES, ProblemSeverity.TRIVIAL, "Skipping orphaned user attendee " + userAttendee, null);
                    } else {
                        /*
                         * no suitable entry in prg_date_rights (anymore), take over as individual user attendee
                         */
                        attendees.add(userAttendee);
                        addInvalidDataWarning(eventId, EventField.ATTENDEES, ProblemSeverity.TRIVIAL, "Preserving orphaned user attendee " + userAttendee, null);
                    }
                }
            }
        }
        /*
         * add other internal, non-user attendees as well as external attendees as-is
         */
        if (null != internalAttendees) {
            for (Attendee internalAttendee : internalAttendees) {
                if (false == CalendarUserType.INDIVIDUAL.equals(internalAttendee.getCuType())) {
                    if (CalendarUtils.contains(attendees, internalAttendee.getEntity())) {
                        /*
                         * duplicate calendar user; just skip
                         */
                        addInvalidDataWarning(eventId, EventField.ATTENDEES, ProblemSeverity.TRIVIAL, "Skipping duplicate " + internalAttendee, null);
                        continue;
                    }
                    try {
                        attendees.add(entityResolver.applyEntityData(sanitizeCUType(eventId, internalAttendee)));
                    } catch (OXException e) {
                        if ("CAL-4034".equals(e.getErrorCode())) {
                            /*
                             * invalid calendar user; possibly a no longer existing group or resource - skip
                             */
                            addInvalidDataWarning(eventId, EventField.ATTENDEES, ProblemSeverity.MINOR, "Skipping non-existent " + internalAttendee, e);
                            continue;
                        }
                        throw e;
                    }
                }
            }
        }
        if (null != externalAttendees) {
            for (Attendee externalAttendee : externalAttendees) {
                attendees.add(entityResolver.applyEntityData(externalAttendee));
            }
        }
        return attendees;
    }

    private Attendee sanitizeCUType(String eventId, Attendee internalAttendee) throws OXException {
        int entity = internalAttendee.getEntity();
        CalendarUserType cuType = internalAttendee.getCuType();
        try {
            /*
             * check entity existence by preparing a corresponding internal attendee
             */
            if (CalendarUserType.GROUP.equals(cuType)) {
                entityResolver.prepareGroupAttendee(entity);
            } else if (CalendarUserType.RESOURCE.equals(cuType) || CalendarUserType.ROOM.equals(cuType)) {
                entityResolver.prepareResourceAttendee(entity);
            } else {
                entityResolver.prepareUserAttendee(entity);
            }
            return internalAttendee;
        } catch (OXException e) {
            if ("CAL-4034".equals(e.getErrorCode())) {
                /*
                 * Invalid calendar user; possibly a "user" attendee added as resource - recover if possible
                 */
                CalendarUserType probedCUType = entityResolver.probeCUType(entity);
                if (null != probedCUType && false == probedCUType.equals(cuType)) {
                    String message = "Auto-correcting stored calendar user type for " + internalAttendee + " to \"" + probedCUType + '"';
                    addInvalidDataWarning(eventId, EventField.ATTENDEES, ProblemSeverity.TRIVIAL, message, e);
                    internalAttendee.setCuType(probedCUType);
                    return internalAttendee;
                }
            }
            throw e;
        }
    }

    private int[] findGroupIDs(List<Attendee> internalAttendees, int member) throws OXException {
        Set<Integer> groupIDs = new HashSet<Integer>();
        for (Attendee groupAttendee : filter(internalAttendees, Boolean.TRUE, CalendarUserType.GROUP)) {
            try {
                int[] members = entityResolver.getGroupMembers(groupAttendee.getEntity());
                if (com.openexchange.tools.arrays.Arrays.contains(members, member)) {
                    groupIDs.add(I(groupAttendee.getEntity()));
                }
            } catch (OXException e) {
                if ("CAL-4034".equals(e.getErrorCode())) {
                    /*
                     * Invalid calendar user; possibly a no longer existing group - ignore silently
                     */
                    continue;
                }
                throw e;
            }
        }
        return I2i(groupIDs);
    }

    private void prefetchEntities(Collection<List<Attendee>> internalAttendees, Collection<List<Attendee>> userAttendees) {
        if (null != internalAttendees && null != userAttendees) {
            List<Attendee> attendees = new ArrayList<Attendee>();
            for (List<Attendee> attendeeList : internalAttendees) {
                attendees.addAll(attendeeList);
            }
            for (List<Attendee> attendeeList : userAttendees) {
                attendees.addAll(attendeeList);
            }
            entityResolver.prefetch(attendees);
        }
    }

    private static Map<String, List<Attendee>> selectInternalAttendeeData(Connection connection, int contextID, String objectIDs[], boolean tombstones) throws SQLException {
        if (null == objectIDs || 0 == objectIDs.length) {
            return java.util.Collections.emptyMap();
        }
        Map<String, List<Attendee>> attendeesByObjectId = new HashMap<String, List<Attendee>>(objectIDs.length);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT object_id,id,type,ma,dn FROM ")
            .append(tombstones ? "del_date_rights" : "prg_date_rights")
            .append(" WHERE cid=? AND object_id")
        ;
        if (1 == objectIDs.length) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (").append(getParameters(objectIDs.length)).append(')');
        }
        stringBuilder.append(" AND type IN (1,2,3);");
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (String objectID : objectIDs) {
                stmt.setInt(parameterIndex++, asInt(objectID));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attendee attendee = new Attendee();
                    attendee.setEntity(resultSet.getInt("id"));
                    attendee.setCuType(Appointment2Event.getCalendarUserType(resultSet.getInt("type")));
                    attendee.setUri(Appointment2Event.getURI(resultSet.getString("ma")));
                    attendee.setCn(resultSet.getString("dn"));
                    put(attendeesByObjectId, asString(resultSet.getInt("object_id")), attendee);
                }
            }
        }
        return attendeesByObjectId;
    }

    private static Map<String, List<Attendee>> selectUserAttendeeData(Connection connection, int contextID, String objectIDs[], boolean tombstones) throws SQLException, OXException {
        if (null == objectIDs || 0 == objectIDs.length) {
            return java.util.Collections.emptyMap();
        }
        Map<String, List<Attendee>> attendeesByObjectId = new HashMap<String, List<Attendee>>(objectIDs.length);
        InternalAttendeeMapper mapper = InternalAttendeeMapper.getInstance();
        AttendeeField[] mappedFields = mapper.getMappedFields();
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT object_id,").append(mapper.getColumns(mappedFields))
            .append(" FROM ").append(tombstones ? "del_dates_members" : "prg_dates_members")
            .append(" WHERE cid=? AND object_id")
        ;
        if (1 == objectIDs.length) {
            stringBuilder.append("=?;");
        } else {
            stringBuilder.append(" IN (").append(getParameters(objectIDs.length)).append(");");
        }
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (String objectID : objectIDs) {
                stmt.setInt(parameterIndex++, asInt(objectID));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    put(attendeesByObjectId, asString(resultSet.getInt("object_id")), mapper.fromResultSet(resultSet, mappedFields));
                }
            }
        }
        return attendeesByObjectId;
    }

    private static Map<String, List<Attendee>> selectExternalAttendeeData(Connection connection, int contextID, String objectIDs[]) throws SQLException {
        if (null == objectIDs || 0 == objectIDs.length) {
            return java.util.Collections.emptyMap();
        }
        Map<String, List<Attendee>> attendeesByObjectId = new HashMap<String, List<Attendee>>(objectIDs.length);
        String sql;
        if (1 == objectIDs.length) {
            sql = "SELECT objectId,mailAddress,displayName,confirm,reason FROM dateExternal WHERE cid=? AND objectId=?;";
        } else {
            sql = new StringBuilder()
                .append("SELECT objectId,mailAddress,displayName,confirm,reason FROM dateExternal ")
                .append("WHERE cid=? AND objectId IN (").append(getParameters(objectIDs.length)).append(");")
            .toString();
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (String objectID : objectIDs) {
                stmt.setInt(parameterIndex++, asInt(objectID));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attendee attendee = new Attendee();
                    attendee.setCuType(CalendarUserType.INDIVIDUAL);
                    attendee.setUri(Appointment2Event.getURI(resultSet.getString("mailAddress")));
                    attendee.setCn(resultSet.getString("displayName"));
                    attendee.setPartStat(Appointment2Event.getParticipationStatus(resultSet.getInt("confirm")));
                    attendee.setComment(resultSet.getString("reason"));
                    put(attendeesByObjectId, asString(resultSet.getInt("objectId")), attendee);
                }
            }
        }
        return attendeesByObjectId;
    }

    /**
     * Gets a value indicating whether <i>orphaned</i> individual user attendees that do have a record in the
     * <code>prg_dates_members</code> table, but there's no matching entry in the <code>prg_date_rights</code> table should be skipped
     * during read operations or not.
     * <p/>
     * This systematically happens for user attendees that originally were added as member of a group, and afterwards this group was
     * deleted or these users were removed from the group.
     *
     * @return <code>true</code> if the such orphaned attendees should be ignored and skipped, <code>false</code>, if they should be
     *         preserved as individual attendees
     */
    private static boolean isIgnoreOrphanedUserAttendees() {
        return false;
    }

}
