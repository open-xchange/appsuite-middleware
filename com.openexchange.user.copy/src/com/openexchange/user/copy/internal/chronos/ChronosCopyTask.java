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

package com.openexchange.user.copy.internal.chronos;

import static com.openexchange.chronos.common.CalendarUtils.getEventsByUID;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
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
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;

/**
 * {@link ChronosCopyTask}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ChronosCopyTask implements CopyUserTaskService {

    private final ServiceLookup services;

    private CalendarStorage srcCalendarStorage;
    private CalendarStorage dstCalendarStorage;
      //Preparation for 7.10.1, when calendar availabilities become relevant
//    private CalendarAvailabilityStorage srcAvailabilityStorage;
//    private CalendarAvailabilityStorage dstAvailabilityStorage;

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

    public ChronosCopyTask(ServiceLookup services) {
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
    public void done(Map<String, ObjectMapping<?>> copied, boolean failed) {
        // nothing to do
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
        //initialize storages
        initSourceCalendarStorage(srcCtx, srcCon);
        initDestinationCalendarStorage(dstCtx, dstCon);

        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();
        final Set<Integer> sourceFolderIds = folderMapping.getSourceKeys();
        //CalendarAccounts
        List<CalendarAccount> srcAccountList = loadSourceCalendarAccounts(srcUsrId);
        insertDestinationCalendarAccounts(srcAccountList, dstUsrId);
        //Events
        List<Event> srcEventList = loadSourceEvents(srcUsrId, sourceFolderIds);
        //sourceAlarms
        Map<String, List<Alarm>> alarmByEvent = loadSourceAlarms(srcEventList, srcUsrId);
        Map<String, Event> dstEventMapping = exchangeEventIds(srcEventList, dstCtx, dstUsrId, srcUsrId);
        insertDestinationEvents(new ArrayList<>(dstEventMapping.values()));
        //Attendees
        Map<String, List<Attendee>> eventAttendeeMapping = loadSourceAttendees(convertEventIds(dstEventMapping.keySet()));
        Map<String, List<Attendee>> attendees = exchangeAttendeesIds(folderMapping, eventAttendeeMapping, dstEventMapping, dstUsrId, srcUsrId);
        insertDestinationAttendees(attendees);
        //Alarms
        Map<String, Map<Integer, List<Alarm>>> alarmByEventByUser = exchangeAlarmIds(alarmByEvent, dstEventMapping, srcUsrId, dstUsrId);
        insertDestinationAlarms(new ArrayList<>(dstEventMapping.values()), alarmByEventByUser, attendees);
        //AlarmTrigger
        insertDestinationAlarmTriggers(alarmByEventByUser, new ArrayList<>(dstEventMapping.values()));
        //PerUserProperties
        List<FolderProperties> properties = loadSourceFolderProperties(srcCon, srcCtx.getContextId(), srcUsrId, sourceFolderIds);
        exchangePropertyIds(properties, dstCtx.getContextId(), dstUsrId, folderMapping);
        insertDestinationFolderProperties(dstCon, properties);
        //Preparation for 7.10.1, when calendar availabilities become relevant
//        initSourceAvailabilityStorage(srcCtx, srcCon);
//        initDestinationAvailabilityStorage(dstCtx, dstCon);
//        List<Available> availabilities = loadSourceAvailabilities(srcUsrId);
//        availabilities = exchangeAvailabilityIds(availabilities, dstUsrId);
//        insertDestinationAvailabilities(availabilities);

        final IntegerMapping mapping = new IntegerMapping();
        for (Entry<String, Event> eventMapping : dstEventMapping.entrySet()) {
            mapping.addMapping(Integer.parseInt(eventMapping.getKey()), Integer.parseInt(eventMapping.getValue().getId()));
        }
        return mapping;
    }

    private List<CalendarAccount> loadSourceCalendarAccounts(int srcUsrId) throws OXException {
        return srcCalendarStorage.getAccountStorage().loadAccounts(srcUsrId);
    }

    private List<Event> loadSourceEvents(int srcUsrId, Set<Integer> sourceFolderIds) throws OXException {
        CompositeSearchTerm folderIdsTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (Integer folderId : sourceFolderIds) {
            folderIdsTerm.addSearchTerm(CalendarUtils.getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.EQUALS, folderId));
        }
        SearchTerm<?> searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(CalendarUtils.getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, Integer.valueOf(srcUsrId)))
            .addSearchTerm(folderIdsTerm);

        return srcCalendarStorage.getEventStorage().searchEvents(searchTerm, null, null);
    }

    private Map<String, List<Attendee>> loadSourceAttendees(String[] dstEventList) throws OXException {
        if (0 < dstEventList.length) {
            return srcCalendarStorage.getAttendeeStorage().loadAttendees(dstEventList);
        }
        return Collections.emptyMap();
    }

    private Map<String, List<Alarm>> loadSourceAlarms(List<Event> eventList, int srcUserId) throws OXException {
        if (!eventList.isEmpty()) {
            return srcCalendarStorage.getAlarmStorage().loadAlarms(eventList, srcUserId);
        }
        return Collections.emptyMap();
    }

    private List<FolderProperties> loadSourceFolderProperties(final Connection readCon, int contextId, int userId, Set<Integer> folders) throws OXException {
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

    private Map<String, Event> exchangeEventIds(List<Event> srcEventList, Context dstCtx, int dstUsrId, int srcUsrId) throws OXException {
        Map<String, Event> dstEventList = new LinkedHashMap<>(srcEventList.size());
        for (Entry<String, List<Event>> entry : getEventsByUID(srcEventList, false).entrySet()) {
            List<Event> eventGroup = sortSeriesMasterFirst(entry.getValue());
            String srcSeriesMasterId = "";
            String dstSeriesMasterId = "";
            if (1 <= eventGroup.size() && null != eventGroup.get(0).getSeriesId()) {
                srcSeriesMasterId = eventGroup.get(0).getId();
            }
            for (Event srcEvent : eventGroup) {
                String srcEventId = srcEvent.getId();
                String dstEventId = dstCalendarStorage.getEventStorage().nextId();
                if (srcEventId.equals(srcSeriesMasterId)) {
                    dstSeriesMasterId = dstEventId;
                }
                CalendarUser calendarUser = createCalendarUser(dstCtx, dstUsrId, srcEvent.getCalendarUser(), srcUsrId);
                Organizer organizer = srcEvent.getOrganizer();
                if (organizer.getEntity() == srcUsrId) {
                    organizer.setEntity(dstUsrId);
                    organizer.setUri(calendarUser.getUri());
                } else {
                    organizer.setUri(organizer.getEMail());
                    organizer.setEntity(0);
                }
                srcEvent.setId(dstEventId);
                if (Strings.isNotEmpty(srcEvent.getSeriesId()) && srcEvent.getSeriesId().equals(srcSeriesMasterId) && Strings.isNotEmpty(dstSeriesMasterId)) {
                    srcEvent.setSeriesId(dstSeriesMasterId);
                }
                srcEvent.setCalendarUser(calendarUser);
                srcEvent.setCreatedBy(calendarUser);
                srcEvent.setModifiedBy(calendarUser);
                srcEvent.setOrganizer(organizer);
                srcEvent.setFolderId(srcEvent.getFolderId());
                srcEvent.setRecurrenceId(srcEvent.getRecurrenceId());
                srcEvent.setColor(srcEvent.getColor());
                srcEvent.setTransp(srcEvent.getTransp());
                srcEvent.setDeleteExceptionDates(srcEvent.getDeleteExceptionDates());
                srcEvent.setChangeExceptionDates(srcEvent.getChangeExceptionDates());
                srcEvent.setCategories(srcEvent.getCategories());
                srcEvent.setFilename(srcEvent.getFilename());
                srcEvent.setExtendedProperties(srcEvent.getExtendedProperties());
                dstEventList.put(srcEventId, srcEvent);
            }
        }
        return dstEventList;
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
                } else if (CalendarUtils.isExternalUser(attendee)) {
                    attendee.setFolderId(null);
                }
                attendees.add(attendee);
            }
            eventMapping.get(srcEventId).setAttendees(attendees);
            dstAttendees.put(eventMapping.get(srcEventId).getId(), attendees);
        }
        return dstAttendees;
    }

    private Map<String, Map<Integer, List<Alarm>>> exchangeAlarmIds(Map<String, List<Alarm>> alarmByEvent, Map<String, Event> dstEventMapping, int srcUsrId, int dstUsrId) throws OXException {
        Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId = new HashMap<>(alarmByEvent.size());
        Map<Integer, List<Alarm>> alarmsByUser = null;
        List<Alarm> alarmList;
        for (Entry<String,  List<Alarm>> alarmsPerEvent : alarmByEvent.entrySet()) {
                alarmsByUser = new HashMap<>(1);
                alarmList = new ArrayList<>(alarmsPerEvent.getValue().size());
                for (Alarm alarm : alarmsPerEvent.getValue()) {
                    alarm.setId(dstCalendarStorage.getAlarmStorage().nextId());
                    alarm.setTimestamp(System.currentTimeMillis());
                    //add to list
                    alarmList.add(alarm);
                }
                //add to inner map
                alarmsByUser.put(dstUsrId, alarmList);
                alarmsByUserByEventId.put(dstEventMapping.get(alarmsPerEvent.getKey()).getId(), alarmsByUser);
        }
        return alarmsByUserByEventId;
    }

    private List<FolderProperties> exchangePropertyIds(List<FolderProperties> properties, int dstCtxId, int dstUsrId, ObjectMapping<FolderObject> folderMapping) {
        for (FolderProperties folderProperties : properties) {
           folderProperties.setContextId(dstCtxId);
           folderProperties.setUserId(dstUsrId);
           folderProperties.setFolderId(Integer.parseInt(getDestinationFolder(folderMapping, folderProperties.getFolderId())));
        }
        return properties;
    }

    private void insertDestinationCalendarAccounts(List<CalendarAccount> srcAccountList, int dstUserId) throws OXException {
        for (CalendarAccount srcCalendarAccount : srcAccountList) {
            int destAccountId;
            if (CalendarAccount.DEFAULT_ACCOUNT.getProviderId().equals(srcCalendarAccount.getProviderId())) {
                destAccountId = CalendarAccount.DEFAULT_ACCOUNT.getAccountId();
            } else {
                destAccountId = dstCalendarStorage.getAccountStorage().nextId();
            }
            dstCalendarStorage.getAccountStorage().insertAccount(new DefaultCalendarAccount(
                srcCalendarAccount.getProviderId(),
                destAccountId,
                dstUserId,
                srcCalendarAccount.getInternalConfiguration(),
                srcCalendarAccount.getUserConfiguration(),
                srcCalendarAccount.getLastModified()));
        }
    }

    private void insertDestinationEvents(List<Event> dstEventList) throws OXException {
        dstCalendarStorage.getEventStorage().insertEvents(dstEventList);
    }

    private void insertDestinationAttendees(Map<String, List<Attendee>> eventAttendeeMapping) throws OXException {
        dstCalendarStorage.getAttendeeStorage().insertAttendees(eventAttendeeMapping);
    }

    private void insertDestinationAlarms(List<Event> dstEvents, Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId, Map<String, List<Attendee>> attendees) throws OXException {
        for (Event event : dstEvents) {
            if (null != alarmsByUserByEventId.get(event.getId())) {
                for (Entry<Integer, List<Alarm>> entry : alarmsByUserByEventId.get(event.getId()).entrySet()) {
                    for (Attendee attendee : attendees.get(event.getId())) {
                        if (attendee.getEntity() == entry.getKey()) {
                            event.setFolderId(attendee.getFolderId());
                        }
                    }
                    Map<Integer, List<Alarm>> map = new LinkedHashMap<>(alarmsByUserByEventId.get(event.getId()).entrySet().size());
                    map.put(entry.getKey(), entry.getValue());
                    dstCalendarStorage.getAlarmStorage().insertAlarms(event, map);
                }
            }
        }
    }

    private void insertDestinationAlarmTriggers(Map<String, Map<Integer, List<Alarm>>> alarmByEventByUser, List<Event> eventList) throws OXException {
        if (!alarmByEventByUser.isEmpty()) {
            dstCalendarStorage.getAlarmTriggerStorage().insertTriggers(alarmByEventByUser, eventList);
        }
    }

    private void insertDestinationFolderProperties(final Connection writeCon, List<FolderProperties> properties) throws OXException {
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
                // Execute & close
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void initSourceCalendarStorage(Context srcCtx, final Connection readCon) throws OXException {
        this.srcCalendarStorage = createCalendarStorage(srcCtx, DefaultCalendarAccount.DEFAULT_ACCOUNT.getAccountId(), readCon, readCon);
    }

    private void initDestinationCalendarStorage(Context dstCtx, final Connection writeCon) throws OXException {
        this.dstCalendarStorage = createCalendarStorage(dstCtx, DefaultCalendarAccount.DEFAULT_ACCOUNT.getAccountId(), writeCon, writeCon);
    }

    private CalendarUser createCalendarUser(Context dstCtx, int dstUsrId, CalendarUser srcCalendarUser, int srcUsrId) {
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

    private CalendarStorage createCalendarStorage(Context ctx, int accountId, final Connection readCon, final Connection writeCon) throws OXException {
        SimpleDBProvider simpleDBProvider = new SimpleDBProvider(readCon, writeCon);
        return services.getService(CalendarStorageFactory.class).create(ctx, accountId, getEntityResolver(ctx), simpleDBProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

    private String getDestinationFolder(ObjectMapping<FolderObject> folderMapping, int srcFolder) {
        return String.valueOf(folderMapping.getDestination(folderMapping.getSource(srcFolder)).getObjectID());
    }

    private String[] convertEventIds(Set<String> set) {
        return set.toArray(new String[set.size()]);
    }

    private EntityResolver getEntityResolver(Context ctx) throws OXException {
        return services.getService(CalendarUtilities.class).getEntityResolver(ctx.getContextId());
    }

    private static class FolderProperties {

        private int contextId;
        private int folderId;
        private int userId;
        private String propertyName;
        private String propertyValue;

        public FolderProperties() {
            super();
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

    /**
     * Preparation for 7.10.1, when calendar availabilities become relevant
     *
    private List<Available> loadSourceAvailabilities(int userId) throws OXException {
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

    private CalendarAvailabilityStorage createCalendarAvailabilityStorage(Context ctx, final Connection readCon, final Connection writeCon) throws OXException {
        SimpleDBProvider simpleDBProvider = new SimpleDBProvider(readCon, writeCon);
        return services.getService(CalendarAvailabilityStorageFactory.class).create(ctx, simpleDBProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

    private void initSourceAvailabilityStorage(Context srcCtx, final Connection readCon) throws OXException {
        if (null != srcAvailabilityStorage) {
            this.srcAvailabilityStorage = null;
        }
        this.srcAvailabilityStorage = createCalendarAvailabilityStorage(srcCtx, readCon, null);
    }

    private void initDestinationAvailabilityStorage(Context dstCtx, final Connection writeCon) throws OXException {
        if (null != dstAvailabilityStorage) {
            this.dstAvailabilityStorage = null;
        }
        this.dstAvailabilityStorage = createCalendarAvailabilityStorage(dstCtx, null, writeCon);
    }
    */

}
