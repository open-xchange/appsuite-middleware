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

package com.openexchange.user.copy.internal.chronos.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.chronos.storage.CalendarAvailabilityStorageFactory;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;

/**
 * {@link ChronosCalendarCopyTask}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ChronosCalendarCopyTask implements CopyUserTaskService {

    private final ServiceLookup services;

    private CalendarStorage srcCalendarStorage;
    private CalendarStorage dstCalendarStorage;

    private CalendarAvailabilityStorage srcAvailabilityStorage;
    private CalendarAvailabilityStorage dstAvailabilityStorage;

    private static final String SELECT_ALARM_TRIGGER =
          "SELECT "
        + "cid, account, alarm, user, eventId, folder, triggerDate, action, recurrence, floatingTimezone, relatedTime, pushed "
        + "FROM calendar_alarm_trigger "
        + "WHERE cid = ? "
        + "AND account = ? "
        + "AND user = ? "
        + "AND folder IN (#IDS#)";

    private static final String INSERT_ALARM_TRIGGER =
          "INSERT INTO calendar_alarm_trigger "
        + "(cid, account, alarm, user, eventId, folder, triggerDate, action, recurrence, floatingTimezone, relatedTime, pushed) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_USER_FOLDER_PROPERTIES =
          "SELECT "
        + "fuid, name, value "
        + "FROM oxfolder_user_property "
        + "WHERE cid = ? "
        + "AND userid = ? "
        + "AND fuid IN (#IDS#)";

    private static final String INSERT_USER_FOLDER_PROPERTIES =
          "INSERT INTO oxfolder_user_property "
        + "(cid, fuid, userid, name, value) "
        + "VALUES (?,?,?,?,?)";

    public ChronosCalendarCopyTask(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName()
        };
    }

    @Override
    public String getObjectName() {
        return com.openexchange.chronos.Event.class.getName();
    }

    @Override
    public ObjectMapping<?> copyUser(Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcUsrId = copyTools.getSourceUserId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();
        Context srcCtx = copyTools.getSourceContext();
        Context dstCtx = copyTools.getDestinationContext();
        //init storages
        setSourceCalendarStorage(srcCtx, srcCon);
        setDestinationCalendarStorage(dstCtx, dstCon);
        setSourceAvailabilityStorage(srcCtx, srcCon);
        setDestinationAvailabilityStorage(dstCtx, dstCon);

        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();
        final Set<Integer> sourceFolderIds = folderMapping.getSourceKeys();
        //CalendarAccounts
        List<CalendarAccount> srcAccountList = loadSourceCalendarAccounts(srcUsrId);
        insertDestinationCalendarAccounts(srcAccountList, dstUsrId);
        //Events
        List<Event> srcEventList = searchSourceEvents(srcUsrId, sourceFolderIds);
        //sourceAlarms
        Map<String, Map<Integer, List<Alarm>>> alarmByEvent = loadSourceAlarms(srcEventList);
        Map<String, Event> dstEventMapping = exchangeEventIds(srcEventList, dstCtx, dstUsrId, srcUsrId);
        insertDestinationEvents(new ArrayList<>(dstEventMapping.values()));
        //Attendees
        Map<String, List<Attendee>> eventAttendeeMapping = loadSourceAttendees(convertEventIds(dstEventMapping.keySet()));
        Map<String, List<Attendee>> attendees = exchangeAttendeesIds(folderMapping, eventAttendeeMapping, dstEventMapping, dstUsrId, srcUsrId);
        insertDestinationAttendees(attendees);
        //Alarms
        Map<String, Map<Integer, List<Alarm>>> alarmByEventByUser = exchangeAlarmIds(alarmByEvent, dstEventMapping, dstUsrId);
        insertAlarms(alarmByEventByUser);
        //AlarmTrigger
        List<AlarmTrigger> alarmTrigger = loadAlarmTriggers(copyTools.getSourceConnection(), srcCtx.getContextId(), srcUsrId, sourceFolderIds);
        exchangeAlarmTriggerIds(alarmTrigger, dstEventMapping, dstUsrId, alarmByEventByUser);
        insertAlarmTriggers(copyTools.getDestinationConnection(), alarmTrigger, dstCtx.getContextId());
        //PerUserProperties
        List<FolderProperties> properties = loadFolderProperties(srcCon, srcCtx.getContextId(), srcUsrId, sourceFolderIds);
        exchangePropertyIds(properties, dstCtx.getContextId(), dstUsrId, folderMapping);
        insertFolderproperties(dstCon, properties);
        //CalendarAvailabilities
        List<Available> availabilities = getSourceAvailabilities(srcUsrId);
        availabilities = exchangeAvailabilityIds(availabilities, dstUsrId);
        insertDestinationAvailabilities(availabilities);

        return null;
    }

    private List<FolderProperties> exchangePropertyIds(List<FolderProperties> properties, int dstCtxId, int dstUsrId, ObjectMapping<FolderObject> folderMapping) {
        for (FolderProperties folderProperties : properties) {
           folderProperties.setContextId(dstCtxId);
           folderProperties.setUserId(dstUsrId);
           folderProperties.setFolderId(Integer.parseInt(getDestinationFolder(folderMapping, folderProperties.getFolderId())));
        }
        return properties;
    }

    private void insertFolderproperties(final Connection writeCon, List<FolderProperties> properties) throws OXException {
        PreparedStatement stmt = null;
        try {
            for (FolderProperties folderProperties : properties) {
             // New entry
                stmt = writeCon.prepareStatement(INSERT_USER_FOLDER_PROPERTIES);
                stmt.setInt(1, folderProperties.getContextId());
                stmt.setInt(2, folderProperties.getFolderId());
                stmt.setInt(3, folderProperties.getUserId());
                stmt.setString(4, folderProperties.getPropertyName());
                stmt.setString(5, folderProperties.getPropertyValue());

                // Execute
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private List<FolderProperties> loadFolderProperties(final Connection readCon, int contextId, int userId, Set<Integer> folders) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String selectStatement = CopyTools.replaceIdsInQuery("#IDS#", SELECT_USER_FOLDER_PROPERTIES, folders);
        try {
            // Prepare statement
            stmt = readCon.prepareStatement(selectStatement);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);

            // Execute & convert result
            rs = stmt.executeQuery();
            List<FolderProperties> properties = new ArrayList<>();
            while (rs.next()) {
                FolderProperties property = new FolderProperties();
                property.setFolderId(rs.getInt(1));
                property.setPropertyName(rs.getString(2));
                property.setPropertyValue(rs.getString(3));
                properties.add(property);
            }
            return properties;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private List<AlarmTrigger> exchangeAlarmTriggerIds(List<AlarmTrigger> triggers, Map<String, Event> dstEventMapping, int dstUsrId, Map<String, Map<Integer, List<Alarm>>> alarmByEvent) {
        for (AlarmTrigger alarmTrigger : triggers) {
            String triggerEventId = alarmTrigger.getEventId();
            String dstEventId = dstEventMapping.get(triggerEventId).getId();
            List<Alarm> alarmList = alarmByEvent.get(dstEventId).get(dstUsrId);
            alarmTrigger.setAlarm(alarmList.get(0).getId());
            alarmTrigger.setEventId(dstEventId);
            alarmTrigger.setUserId(dstUsrId);
        }
        return triggers;
    }

    private List<AlarmTrigger> loadAlarmTriggers(final Connection readCon, int contextId, int userId, Set<Integer> folders) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String selectStatement = CopyTools.replaceIdsInQuery("#IDS#", SELECT_ALARM_TRIGGER, folders);
        try {
            stmt = readCon.prepareStatement(selectStatement);
            stmt.setInt(1, contextId);
            stmt.setInt(2, 0);
            stmt.setInt(3, userId);
            // Execute
            rs = stmt.executeQuery();
            List<AlarmTrigger> triggerList = new ArrayList<>();
            while (rs.next()) {
                final AlarmTrigger trigger = new AlarmTrigger();
                trigger.setAlarm(rs.getInt(3));
                trigger.setUserId(rs.getInt(4));
                trigger.setEventId(rs.getString(5));
                trigger.setFolder(rs.getString(6));
                trigger.setTime(rs.getLong(7));//triggerDate?
                trigger.setAction(rs.getString(8));
//                trigger.setRecurrenceId(new DefaultRecurrenceId(rs.getString(7)));
//                trigger.setTimezone(new TimeZone(rs.getString(8)));//TimeZone
                trigger.setRelatedTime(rs.getLong(11));
                trigger.setPushed(rs.getBoolean(12));
                triggerList.add(trigger);
            }
            return triggerList;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void insertAlarmTriggers(final Connection dstCon, List<AlarmTrigger> alarmTrigger, int contextId) throws OXException {
        PreparedStatement stmt = null;
        try {
            for (AlarmTrigger trigger : alarmTrigger) {
                // New entry
                stmt = dstCon.prepareStatement(INSERT_ALARM_TRIGGER);
                stmt.setInt(1, contextId);
                stmt.setInt(2, 0);
                stmt.setInt(3, trigger.getAlarm()); //TODO alarm mapping
                stmt.setInt(4, trigger.getUserId());
                stmt.setString(5, trigger.getEventId()); //TODO eventId mapping
                stmt.setString(6, trigger.getFolder());
                stmt.setLong(7, trigger.getTime());
                stmt.setString(8, trigger.getAction());
                stmt.setString(9, "" /**trigger.getRecurrenceId().toString()*/); //TODO
                stmt.setString(10, "" /**trigger.getTimezone().toString()*/); //TODO
                stmt.setLong(11, trigger.getRelatedTime());
                stmt.setBoolean(12, trigger.isPushed());
                // Execute
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private List<Available> getSourceAvailabilities(int userId) throws OXException {
        return srcAvailabilityStorage.loadAvailable(userId);
    }

    private List<Available> exchangeAvailabilityIds(List<Available> availabilityList, int dstUsrId) throws OXException {
        for (Available available : availabilityList) {
            available.setId(dstAvailabilityStorage.nextAvailableId());
            available.setCalendarUser(dstUsrId);
        }
        return availabilityList;
    }

    private void insertDestinationAvailabilities(List<Available> availabilityList) throws OXException {
        dstAvailabilityStorage.insertAvailable(availabilityList);
    }


    private String getDestinationFolder(ObjectMapping<FolderObject> folderMapping, int srcFolder) {
        return String.valueOf(folderMapping.getDestination(folderMapping.getSource(srcFolder)).getObjectID());
    }

    private Map<String, List<Attendee>> exchangeAttendeesIds(ObjectMapping<FolderObject> folderMapping, Map<String, List<Attendee>> eventAttendeeMapping, Map<String, Event> eventMapping, int dstUsrId, int srcUsrId) {
        Map<String, List<Attendee>> dstAttendees = new HashMap<>(eventAttendeeMapping.size());
        for (Map.Entry<String, List<Attendee>> entry : eventAttendeeMapping.entrySet()) {
            String srcEventId = entry.getKey();
            List<Attendee> attendees = new ArrayList<>(entry.getValue().size());
            for (Attendee attendee : entry.getValue()) {
                //setters for attendee values
                if (attendee.getEntity() == srcUsrId) {
                    attendee.setFolderId(getDestinationFolder(folderMapping, Integer.parseInt(attendee.getFolderId())));
                    attendee.setEntity(dstUsrId);
                    attendees.add(attendee);
                }
            }
            dstAttendees.put(eventMapping.get(srcEventId).getId(), attendees);
        }
        return dstAttendees;
    }

    private void insertDestinationAttendees(Map<String, List<Attendee>> eventAttendeeMapping) throws OXException {
        dstCalendarStorage.getAttendeeStorage().insertAttendees(eventAttendeeMapping);
    }

    private String[] convertEventIds(Set<String> set) {
        return set.toArray(new String[set.size()]);
    }

    @Override
    public void done(Map<String, ObjectMapping<?>> copied, boolean failed) {
        // TODO Auto-generated method stub
    }

    private List<Event> searchSourceEvents(int srcUsrId, Set<Integer> sourceFolderIds) throws OXException {
        CompositeSearchTerm folderIdsTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (Integer folderId : sourceFolderIds) {
            folderIdsTerm.addSearchTerm(CalendarUtils.getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.EQUALS, folderId));
        }
        SearchTerm<?> searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(CalendarUtils.getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, Integer.valueOf(srcUsrId)))
            .addSearchTerm(folderIdsTerm);

        return srcCalendarStorage.getEventStorage().searchEvents(searchTerm, null, null);
    }

    private CalendarUser getCalendarUser(Context dstCtx, int dstUsrId, CalendarUser srcCalendarUser, int srcUsrId) {
        CalendarUser calendarUser = new CalendarUser();
        calendarUser.setEntity(dstUsrId);
        calendarUser.setUri(ResourceId.forUser(dstCtx.getContextId(), dstUsrId));
        if (srcCalendarUser.getEntity() == srcUsrId) {
            if (Strings.isNotEmpty(srcCalendarUser.getEMail())) {
                calendarUser.setEMail(srcCalendarUser.getEMail());
            }
            if (Strings.isNotEmpty(srcCalendarUser.getCn())) {
                calendarUser.setCn(srcCalendarUser.getCn());
            }
        }
        return calendarUser;
    }

    private Map<String, Event> exchangeEventIds(List<Event> srcEventList, Context dstCtx, int dstUsrId, int srcUsrId) throws OXException {
        Map<String, Event> dstEventList = new HashMap<>(srcEventList.size());
        for (Event srcEvent : srcEventList) {
            String srcEventId = srcEvent.getId();
            String dstEventId = dstCalendarStorage.getEventStorage().nextId();
            CalendarUser calendarUser = getCalendarUser(dstCtx, dstUsrId, srcEvent.getCalendarUser(), srcUsrId);
            Organizer organizer = srcEvent.getOrganizer();
            if (organizer.getEntity() == srcUsrId) {
                organizer.setEntity(dstUsrId);
                organizer.setUri(calendarUser.getUri());
            } else {
                organizer.setUri(organizer.getEMail());
                organizer.setEntity(0);
            }
            srcEvent.setId(dstEventId);
            srcEvent.setCalendarUser(calendarUser);
            srcEvent.setCreatedBy(calendarUser);
            srcEvent.setModifiedBy(calendarUser);
            srcEvent.setOrganizer(organizer);
            dstEventList.put(srcEventId, srcEvent);
        }
        return dstEventList;
    }

    private Map<String, Map<Integer, List<Alarm>>> loadSourceAlarms(List<Event> eventList) throws OXException {
        return srcCalendarStorage.getAlarmStorage().loadAlarms(eventList);
    }

    private Map<String, Map<Integer, List<Alarm>>> exchangeAlarmIds(Map<String, Map<Integer, List<Alarm>>> alarms, Map<String, Event> dstEventMapping, int dstUsrId) throws OXException {
        Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId = new HashMap<>(alarms.size());
        Map<Integer, List<Alarm>> alarmsByUser = null;
        List<Alarm> alarmList;
        for (Entry<String, Map<Integer, List<Alarm>>> entry : alarms.entrySet()) {
            for (Entry<Integer, List<Alarm>> alarmsPerUser : entry.getValue().entrySet()) {
                alarmsByUser = new HashMap<>(1);
                alarmList = new ArrayList<>(entry.getValue().size());
                for (Alarm alarm : alarmsPerUser.getValue()) {
                    alarm.setId(dstCalendarStorage.getAlarmStorage().nextId());
                    if (false == alarm.containsUid() || null == alarm.getUid()) {
                        alarm.setUid(UUID.randomUUID().toString());
                    }
                    //add to list
                    alarmList.add(alarm);
                }
                //add to inner map
                alarmsByUser.put(dstUsrId, alarmList);
            }
            //add to outer map
            alarmsByUserByEventId.put(dstEventMapping.get(entry.getKey()).getId(), alarmsByUser);
        }
        return alarmsByUserByEventId;
    }

    private void insertAlarms(Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws OXException {
        dstCalendarStorage.getAlarmStorage().insertAlarms(alarmsByUserByEventId);
    }

    private void insertDestinationCalendarAccounts(List<CalendarAccount> srcAccountList, int dstUserId) throws OXException {
        for (CalendarAccount srcCalendarAccount : srcAccountList) {
            int destAccountId;
            if (CalendarAccount.DEFAULT_ACCOUNT.getProviderId().equals(srcCalendarAccount.getProviderId())) {
                destAccountId = CalendarAccount.DEFAULT_ACCOUNT.getAccountId();
            } else {
                destAccountId = dstCalendarStorage.getAccountStorage().nextId();
            }
            //TODO check if null/empty
            dstCalendarStorage.getAccountStorage().insertAccount(new DefaultCalendarAccount(
                srcCalendarAccount.getProviderId(),
                destAccountId,
                dstUserId,
                srcCalendarAccount.isEnabled(),
                srcCalendarAccount.getInternalConfiguration(),
                srcCalendarAccount.getUserConfiguration(),
                srcCalendarAccount.getLastModified()));
        }
    }

    private Map<String, List<Attendee>> loadSourceAttendees(String[] destEventList) throws OXException {
        return srcCalendarStorage.getAttendeeStorage().loadAttendees(destEventList);
    }

    private void insertDestinationEvents(List<Event> dstEventList) throws OXException {
        dstCalendarStorage.getEventStorage().insertEvents(dstEventList);
    }

    private List<CalendarAccount> loadSourceCalendarAccounts(int srcUsrId) throws OXException {
        return srcCalendarStorage.getAccountStorage().loadAccounts(srcUsrId);
    }

    private CalendarStorage getCalendarStorage(Context ctx, int accountId, final Connection readCon, final Connection writeCon) throws OXException {
        SimpleDBProvider simpleDBProvider = new SimpleDBProvider(readCon, writeCon);
        return services.getService(CalendarStorageFactory.class).create(ctx, accountId, null, simpleDBProvider, DBTransactionPolicy.NORMAL_TRANSACTIONS);
    }

    private CalendarAvailabilityStorage getCalendarAvailabilityStorage(Context ctx, final Connection readCon, final Connection writeCon) throws OXException {
        SimpleDBProvider simpleDBProvider = new SimpleDBProvider(readCon, writeCon);
        return services.getService(CalendarAvailabilityStorageFactory.class).create(ctx, simpleDBProvider, DBTransactionPolicy.NORMAL_TRANSACTIONS);
    }

    private void setSourceAvailabilityStorage(Context srcCtx, final Connection readCon) throws OXException {
        if (null != srcAvailabilityStorage) {
            this.srcAvailabilityStorage = null;
        }
        this.srcAvailabilityStorage = getCalendarAvailabilityStorage(srcCtx, readCon, null);
    }

    private void setDestinationAvailabilityStorage(Context dstCtx, final Connection readCon) throws OXException {
        if (null != dstAvailabilityStorage) {
            this.dstAvailabilityStorage = null;
        }
        this.dstAvailabilityStorage = getCalendarAvailabilityStorage(dstCtx, readCon, null);
    }

    private void setSourceCalendarStorage(Context srcCtx, final Connection readCon) throws OXException {
        if (null != srcCalendarStorage) {
            this.srcCalendarStorage = null;
        }
        this.srcCalendarStorage = getCalendarStorage(srcCtx, 0, readCon, null);
    }

    private void setDestinationCalendarStorage(Context dstCtx, final Connection writeCon) throws OXException {
        if (null != dstCalendarStorage) {
            this.dstCalendarStorage = null;
        }
        this.dstCalendarStorage = getCalendarStorage(dstCtx, 0, null, writeCon);
    }

    private class FolderProperties {

        private int contextId;
        private int folderId;
        private int userId;
        private String propertyName;
        private String propertyValue;

        public FolderProperties() {

        }

        public int getContextId() {
            return contextId;
        }

        public void setContextId(int contextId) {
            this.contextId = contextId;
        }

        public int getFolderId() {
            return folderId;
        }

        public void setFolderId(int folderId) {
            this.folderId = folderId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyValue() {
            return propertyValue;
        }

        public void setPropertyValue(String propertyValue) {
            this.propertyValue = propertyValue;
        }

    }

}
