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

package com.openexchange.ajax.request;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.DateOrderObject;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.ParticipantParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link AppointmentRequest} - Processes appointment requests.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class AppointmentRequest extends CalendarRequest {

    private static final int DAY_MILLIS = 24 * 60 * 60 * 1000;

    private static final Pattern PATTERN_SPLIT = Pattern.compile(",");

    private static String[] split(final String csv) {
        return PATTERN_SPLIT.split(csv, 0);
    }

    public static final String RECURRENCE_MASTER = "recurrence_master";

    private final static int[] _appointmentFields = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION,
        CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
        CalendarObject.RECURRENCE_CALCULATOR, CalendarObject.RECURRENCE_ID, CalendarObject.RECURRENCE_POSITION,
        CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.DELETE_EXCEPTIONS,
        Appointment.CHANGE_EXCEPTIONS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.TIMEZONE, Appointment.ORGANIZER, Appointment.UID, Appointment.SEQUENCE, Appointment.CONFIRMATIONS, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT,
        Appointment.NUMBER_OF_ATTACHMENTS};

    private final Context ctx;

    private final User user;

    private final AppointmentSqlFactoryService appointmentFactory;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentRequest.class);

    /**
     * Initializes a new {@link AppointmentRequest}.
     *
     * @param session The session.
     */
    public AppointmentRequest(final ServerSession session) {
        this.session = session;
        ctx = session.getContext();
        user = session.getUser();
        final String sTimeZone = user.getTimeZone();
        appointmentFactory = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);

        timeZone = getTimeZone(sTimeZone);
        LOG.debug("use timezone string: {}", sTimeZone);
        LOG.debug("use user timezone: {}", timeZone);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public JSONValue action(final String action, final JSONObject jsonObject) throws JSONException, OXException {
        if (!session.getUserPermissionBits().hasCalendar()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("calendar");
        }
        if (AJAXServlet.ACTION_CONFIRM.equalsIgnoreCase(action)) {
            return actionConfirm(jsonObject);
        } else if (AJAXServlet.ACTION_NEW.equalsIgnoreCase(action)) {
            return actionNew(jsonObject);
        } else if (AJAXServlet.ACTION_DELETE.equalsIgnoreCase(action)) {
            return actionDelete(jsonObject);
        } else if (AJAXServlet.ACTION_UPDATE.equalsIgnoreCase(action)) {
            return actionUpdate(jsonObject);
        } else if (AJAXServlet.ACTION_UPDATES.equalsIgnoreCase(action)) {
            return actionUpdates(jsonObject);
        } else if (AJAXServlet.ACTION_LIST.equalsIgnoreCase(action)) {
            return actionList(jsonObject);
        } else if (AJAXServlet.ACTION_ALL.equalsIgnoreCase(action)) {
            return actionAll(jsonObject);
        } else if (AJAXServlet.ACTION_GET.equalsIgnoreCase(action)) {
            return actionGet(jsonObject);
        } else if (AJAXServlet.ACTION_SEARCH.equalsIgnoreCase(action)) {
            return actionSearch(jsonObject);
        } else if (AJAXServlet.ACTION_NEW_APPOINTMENTS.equalsIgnoreCase(action)) {
            return actionNewAppointmentsSearch(jsonObject);
        } else if (AJAXServlet.ACTION_HAS.equalsIgnoreCase(action)) {
            return actionHas(jsonObject);
        } else if (AJAXServlet.ACTION_FREEBUSY.equalsIgnoreCase(action)) {
            return actionFreeBusy(jsonObject);
        } else if (AJAXServlet.ACTION_COPY.equalsIgnoreCase(action)) {
            return actionCopy(jsonObject);
        } else if (AJAXServlet.ACTION_RESOLVE_UID.equalsIgnoreCase(action)) {
            return actionResolveUid(jsonObject);
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
        }
    }

    private JSONObject actionResolveUid(final JSONObject jsonObj) throws JSONException, OXException {
        final AppointmentSQLInterface appointmentSql = appointmentFactory.createAppointmentSql(session);
        final JSONObject json = new JSONObject();
        final String uid = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_UID);
        final int id = appointmentSql.resolveUid(uid);
        if (id == 0) {
            throw OXException.notFound("");
        }
        json.put("id", id);
        return json;
    }

    public JSONObject actionNew(final JSONObject jsonObj) throws JSONException, OXException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final CalendarDataObject appointmentObj = new CalendarDataObject();
        appointmentObj.setContext(ctx);

        final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
        appointmentParser.parse(appointmentObj, jData);

        if (!appointmentObj.containsParentFolderID()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_FOLDERID);
        }

        convertExternalToInternalUsersIfPossible(appointmentObj, ctx, LOG);

        final AppointmentSQLInterface appointmentSql = appointmentFactory.createAppointmentSql(session);
        final Appointment[] conflicts = appointmentSql.insertAppointmentObject(appointmentObj);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts == null) {
            jsonResponseObj.put(DataFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
        } else {
            final JSONArray jsonConflictArray = new JSONArray(conflicts.length);
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(session);
            for (int a = 0; a < conflicts.length; a++) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflicts[a], jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }

            jsonResponseObj.put("conflicts", jsonConflictArray);
        }

        return jsonResponseObj;
    }

    public JSONObject actionUpdate(final JSONObject jsonObj) throws JSONException, OXException {
        final int objectId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final CalendarDataObject appointmentObj = new CalendarDataObject();
        appointmentObj.setContext(ctx);

        final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
        appointmentParser.parse(appointmentObj, jData);

        convertExternalToInternalUsersIfPossible(appointmentObj, ctx, LOG);

        appointmentObj.setObjectID(objectId);

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        final Appointment[] conflicts = appointmentsql.updateAppointmentObject(appointmentObj, inFolder, timestamp);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts == null) {
            jsonResponseObj.put(DataFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
        } else {
            final JSONArray jsonConflictArray = new JSONArray(conflicts.length);
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(session);
            for (int a = 0; a < conflicts.length; a++) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflicts[a], jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }

            jsonResponseObj.put("conflicts", jsonConflictArray);
        }

        return jsonResponseObj;
    }

    public JSONArray actionUpdates(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final Date requestedTimestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        timestamp = new Date(requestedTimestamp.getTime());
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }
        final Date startUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date endUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_END);
        final Date start = startUTC == null ? null : applyTimeZone2Date(startUTC.getTime());
        final Date end = endUTC == null ? null : applyTimeZone2Date(endUTC.getTime());
        final String ignore = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_IGNORE);

        final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);
        final boolean showPrivates = DataParser.parseBoolean(jsonObj, AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS);
        final int folderId = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);

        boolean showAppointmentInAllFolders = false;

        if (folderId == 0) {
            showAppointmentInAllFolders = true;
        }

        boolean bIgnoreDelete = false;
        boolean bIgnoreModified = false;

        if (ignore != null && ignore.indexOf("deleted") != -1) {
            bIgnoreDelete = true;
        }

        if (ignore != null && ignore.indexOf("changed") != -1) {
            bIgnoreModified = true;
        }

        final JSONArray jsonResponseArray = new JSONArray();

        if (bIgnoreModified && bIgnoreDelete) {
            // nothing requested

            return jsonResponseArray;
        }

        final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(session);
        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        SearchIterator<Appointment> it = null;
        Date lastModified = null;
        appointmentsql.setIncludePrivateAppointments(showPrivates);
        try {
            if (!bIgnoreModified) {
                if (showAppointmentInAllFolders) {
                    it = appointmentsql.getModifiedAppointmentsBetween(session.getUserId(), start, end, _appointmentFields, requestedTimestamp, 0, Order.NO_ORDER);
                } else {
                    if (start == null || end == null) {
                        it = appointmentsql.getModifiedAppointmentsInFolder(folderId, _appointmentFields, requestedTimestamp);
                    } else {
                        it = appointmentsql.getModifiedAppointmentsInFolder(folderId, start, end, _appointmentFields, requestedTimestamp);
                    }
                }

                while (it.hasNext()) {
                    final Appointment appointmentObj = it.next();
                    boolean written = false;
                    if (appointmentObj.getRecurrenceType() != CalendarObject.NONE && appointmentObj.getRecurrencePosition() == 0) {
                        if (bRecurrenceMaster) {
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateFirstRecurring(appointmentObj);
                                written = true;
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence {}:{}", appointmentObj.getObjectID(), ctx.getContextId(), e);
                            }
                            if (recuResults != null && recuResults.size() != 1) {
                                LOG.warn("cannot load first recurring appointment from appointment object: {} / {}\n\n\n", appointmentObj.getRecurrenceType(), appointmentObj.getObjectID());
                            } else if (recuResults != null) {
                                appointmentObj.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                                appointmentObj.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                                appointmentWriter.writeArray(appointmentObj, columns, jsonResponseArray);
                            }
                        } else {
                            // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                            // appointmentObj.calculateRecurrence();

                            RecurringResultsInterface recuResults = null;
                            try {
                                if (start == null || end == null) {
                                    recuResults = recColl.calculateFirstRecurring(appointmentObj);
                                    written = true;
                                } else {
                                    recuResults = recColl.calculateRecurring(appointmentObj, start.getTime(), end.getTime(), 0);
                                    written = true;
                                }
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence {}:{}", appointmentObj.getObjectID(), ctx.getContextId(), e);
                            }

                            if (recuResults != null) {
                                for (int a = 0; a < recuResults.size(); a++) {
                                    final RecurringResultInterface result = recuResults.getRecurringResult(a);
                                    appointmentObj.setStartDate(new Date(result.getStart()));
                                    appointmentObj.setEndDate(new Date(result.getEnd()));
                                    appointmentObj.setRecurrencePosition(result.getPosition());

                                    if (startUTC == null || endUTC == null) {
                                        appointmentWriter.writeArray(appointmentObj, columns, jsonResponseArray);
                                    } else {
                                        appointmentWriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                                    }
                                }
                            }
                        }
                    }
                    if (!written) {
                        if (startUTC == null || endUTC == null) {
                            appointmentWriter.writeArray(appointmentObj, columns, jsonResponseArray);
                        } else {
                            appointmentWriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                        }
                    }

                    lastModified = appointmentObj.getLastModified();

                    if (timestamp.getTime() < lastModified.getTime()) {
                        timestamp = lastModified;
                    }
                }
            }

            if (!bIgnoreDelete) {
                it = appointmentsql.getDeletedAppointmentsInFolder(folderId, _appointmentFields, requestedTimestamp);
                while (it.hasNext()) {
                    final Appointment appointmentObj = it.next();

                    jsonResponseArray.put(appointmentObj.getObjectID());

                    lastModified = appointmentObj.getLastModified();

                    if (timestamp.getTime() < lastModified.getTime()) {
                        timestamp = lastModified;
                    }
                }
            }

            return jsonResponseArray;
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionDelete(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final CalendarDataObject appointmentObj = new CalendarDataObject();
        appointmentObj.setObjectID(DataParser.checkInt(jData, DataFields.ID));
        final int inFolder = DataParser.checkInt(jData, AJAXServlet.PARAMETER_INFOLDER);

        if (jData.has(CalendarFields.RECURRENCE_POSITION)) {
            appointmentObj.setRecurrencePosition(DataParser.checkInt(jData, CalendarFields.RECURRENCE_POSITION));
        } else if (jData.has(CalendarFields.RECURRENCE_DATE_POSITION)) {
            appointmentObj.setRecurrenceDatePosition(DataParser.checkDate(jData, CalendarFields.RECURRENCE_DATE_POSITION));

        }

        appointmentObj.setContext(ctx);

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);

        try {
            appointmentsql.deleteAppointmentObject(appointmentObj, inFolder, timestamp);
            if (appointmentObj.getLastModified() != null) {
                timestamp = appointmentObj.getLastModified();
            }
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        }

        return new JSONArray();
    }

    public JSONArray actionList(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

        Date lastModified = null;

        SearchIterator<Appointment> it = null;

        final TIntObjectMap<TIntArrayList> recurrencePositionMap = new TIntObjectHashMap<TIntArrayList>();

        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final JSONArray jData = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);

        final TIntIntMap objectIdMap = new TIntIntHashMap();
        for (int a = 0; a < jData.length(); a++) {
            JSONObject jObject = null;
            try {
                jObject = jData.getJSONObject(a);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, jData.toString());
            }

            final int objectId = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_ID);
            final int folderId = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_FOLDERID);

            objectIdMap.put(objectId, folderId);

            // for backward compatibility supporting both recurrence position parameters
            int tempRecurrencePosition = DataParser.parseInt(jObject, CalendarFields.RECURRENCE_POSITION);
            if (tempRecurrencePosition == 0) {
                tempRecurrencePosition = DataParser.parseInt(jObject, CalendarFields.OLD_RECURRENCE_POSITION);

                if (tempRecurrencePosition > 0) {
                    LOG.warn("found old recurrence position field in request");
                }
            }

            if (tempRecurrencePosition > 0) {
                final int recurrencePosition = tempRecurrencePosition;
                TIntArrayList recurrencePosList = null;
                if (recurrencePositionMap.containsKey(objectId)) {
                    recurrencePosList = recurrencePositionMap.get(objectId);
                } else {
                    recurrencePosList = new TIntArrayList();
                }
                recurrencePosList.add(recurrencePosition);
                recurrencePositionMap.put(objectId, recurrencePosList);
            }
        }

        final int size = objectIdMap.size();
        final int[][] objectIdAndFolderId = new int[size][2];
        {
            int i = 0;
            for (final int objectId : objectIdMap.keys()) {
                objectIdAndFolderId[i][0] = objectId;
                objectIdAndFolderId[i++][1] = objectIdMap.get(objectId);
            }
        }

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        it = appointmentsql.getObjectsById(objectIdAndFolderId, _appointmentFields);
        try {
            int counter = 0;
            final JSONArray jsonResponseArray = new JSONArray();
            while (it.hasNext()) {
                final Appointment appointment = it.next();
                if (null == appointment) {
                    continue;
                }
                final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone).setSession(session);

                final Date startDate = appointment.getStartDate();
                final Date endDate = appointment.getEndDate();

                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {
                    if (bRecurrenceMaster) {
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateFirstRecurring(appointment);
                        } catch (final OXException e) {
                            LOG.error("Can not calculate recurrence {}:{}", appointment.getObjectID(), ctx.getContextId(), e);
                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        }

                        if (recuResults != null && recuResults.size() == 1) {
                            appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                            appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        } else {
                            LOG.warn("cannot load first recurring appointment from appointment object: {} / {}\n\n\n", appointment.getRecurrenceType(), appointment.getObjectID());
                        }
                    } else {
                        // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                        // appointment.calculateRecurrence();
                        if (recurrencePositionMap.containsKey(appointment.getObjectID())) {
                            final TIntList recurrencePosList = recurrencePositionMap.get(appointment.getObjectID());

                            final int listSize = recurrencePosList.size();
                            for (int a = 0; a < listSize; a++) {
                                appointment.setStartDate(startDate);
                                appointment.setEndDate(endDate);
                                final RecurringResultsInterface recuResults = recColl.calculateRecurring(appointment, 0, 0, recurrencePosList.get(a));
                                if (recuResults.size() > 0) {
                                    final RecurringResultInterface result = recuResults.getRecurringResult(0);
                                    appointment.setStartDate(new Date(result.getStart()));
                                    appointment.setEndDate(new Date(result.getEnd()));
                                    appointment.setRecurrencePosition(result.getPosition());
                                } else {
                                    throw OXException.notFound("no recurrence appointment found at pos: " + counter);
                                }

                                appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                            }
                        } else {
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateFirstRecurring(appointment);
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence {}:{}", appointment.getObjectID(), ctx.getContextId(), e);
                                appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                            }
                            if (recuResults != null && recuResults.size() > 0) {
                                final RecurringResultInterface result = recuResults.getRecurringResult(0);
                                appointment.setStartDate(new Date(result.getStart()));
                                appointment.setEndDate(new Date(result.getEnd()));
                                appointment.setRecurrencePosition(result.getPosition());
                            } else if (recuResults != null) {
                                throw OXException.notFound("no recurrence appointment found at pos: " + counter);
                            }

                            if (appointment.getFullTime() && appointment.getStartDate().getTime() == appointment.getEndDate().getTime()) {
                                appointment.setEndDate(new Date(appointment.getStartDate().getTime() + DAY_MILLIS));
                            }

                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        }
                    }
                } else {
                    appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                }

                lastModified = appointment.getLastModified();

                if (timestamp.getTime() < lastModified.getTime()) {
                    timestamp = lastModified;
                }

                counter++;
            }

            return jsonResponseArray;
        } catch (final SearchIteratorException e) {
            throw e;
        } catch (final OXException e) {
            LOG.error("", e);
            throw e;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionAll(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

        SearchIterator<Appointment> it = null;

        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final Date startUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date endUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_END);
        final Date start = applyTimeZone2Date(startUTC.getTime());
        final Date end = applyTimeZone2Date(endUTC.getTime());
        final int folderId = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);

        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final boolean showPrivateAppointments = DataParser.parseBoolean(jsonObj, AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS);
        final boolean listOrder;
        if (orderBy == CalendarObject.START_DATE || orderBy == CalendarObject.END_DATE) {
            listOrder = true;
        } else {
            listOrder = false;
        }

        final List<DateOrderObject> objectList = new ArrayList<DateOrderObject>();

        final String orderDirString = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
        final Order orderDir = OrderFields.parse(orderDirString);

        final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);

        final int leftHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.LEFT_HAND_LIMIT);
        final int rightHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.RIGHT_HAND_LIMIT);

        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        boolean showAppointmentInAllFolders = false;

        if (folderId == 0) {
            showAppointmentInAllFolders = true;
        }

        final JSONArray jsonResponseArray = new JSONArray();
        try {
            final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
            final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            if (showAppointmentInAllFolders) {
                it = appointmentsql.getAppointmentsBetween(user.getId(), start, end, _appointmentFields, orderBy, orderDir);
            } else {
                final boolean old = appointmentsql.getIncludePrivateAppointments();
                appointmentsql.setIncludePrivateAppointments(showPrivateAppointments);
                it = appointmentsql.getAppointmentsBetweenInFolder(folderId, _appointmentFields, start, end, orderBy, orderDir);
                appointmentsql.setIncludePrivateAppointments(old);
            }
            Date lastModified = new Date(0);
            while (it.hasNext()) {
                final Appointment appointment = it.next();
                final AppointmentWriter writer = new AppointmentWriter(timeZone).setSession(session);
                boolean written = false;

                // Workaround to fill appointments with alarm times
                // TODO: Move me down into the right layer if there is time for some refactoring.
                if (com.openexchange.tools.arrays.Arrays.contains(columns, CalendarObject.ALARM)) {
                    if (!appointment.containsAlarm() && appointment.containsUserParticipants()) {
                        final OXFolderAccess ofa = new OXFolderAccess(ctx);

                        try {
                            final int folderType = ofa.getFolderType(appointment.getParentFolderID(), user.getId());
                            final int owner = ofa.getFolderOwner(appointment.getParentFolderID());

                            switch (folderType) {
                                case FolderObject.PRIVATE:
                                    for (final UserParticipant u : appointment.getUsers()) {
                                        if (u.getIdentifier() == user.getId() && u.getAlarmMinutes() > -1) {
                                            appointment.setAlarm(u.getAlarmMinutes());
                                            break;
                                        }
                                    }
                                    break;

                                case FolderObject.SHARED:
                                    for (final UserParticipant u : appointment.getUsers()) {
                                        if (u.getIdentifier() == owner && u.getAlarmMinutes() > -1) {
                                            appointment.setAlarm(u.getAlarmMinutes());
                                            break;
                                        }
                                    }
                                    break;
                            }
                        } catch (final OXException e) {
                            LOG.error("An error occurred during filling an appointment with alarm information.", e);
                        }
                    }
                }
                // End of workaround

                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {
                    if (bRecurrenceMaster) {
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateFirstRecurring(appointment);
                            written = true;
                        } catch (final OXException e) {
                            LOG.error("Can not calculate recurrence {}:{}", appointment.getObjectID(), ctx.getContextId(), e);
                        }
                        if (recuResults != null && recuResults.size() == 1) {
                            appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                            appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                            writer.writeArray(appointment, columns, jsonResponseArray);
                        } else {
                            LOG.warn("cannot load first recurring appointment from appointment object: {} / {}\n\n\n", appointment.getRecurrenceType(), appointment.getObjectID());
                        }
                    } else {
                        // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                        // appointment.calculateRecurrence();
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateRecurring(appointment, start.getTime(), end.getTime(), 0);
                            written = true;
                        } catch (final OXException e) {
                            LOG.error("Can not calculate recurrence {}:{}", appointment.getObjectID(), ctx.getContextId(), e);
                        }
                        if (recuResults != null) {
                            for (int a = 0; a < recuResults.size(); a++) {
                                final RecurringResultInterface result = recuResults.getRecurringResult(a);
                                appointment.setStartDate(new Date(result.getStart()));
                                appointment.setEndDate(new Date(result.getEnd()));
                                appointment.setRecurrencePosition(result.getPosition());

                                // add to order list
                                if (listOrder) {
                                    final DateOrderObject dateOrderObject = new DateOrderObject(getDateByFieldId(orderBy, appointment, timeZone), appointment.clone());
                                    objectList.add(dateOrderObject);
                                } else {
                                    writer.writeArray(appointment, columns, startUTC, endUTC, jsonResponseArray);
                                }
                            }
                        }
                    }
                }

                if (!written) {
                    // add to order list
                    if (listOrder) {
                        final DateOrderObject dateOrderObject = new DateOrderObject(getDateByFieldId(orderBy, appointment, timeZone), appointment.clone());
                        objectList.add(dateOrderObject);
                    } else {
                        writer.writeArray(appointment, columns, startUTC, endUTC, jsonResponseArray);
                    }
                }

                lastModified = appointment.getLastModified();

                if (timestamp.getTime() < lastModified.getTime()) {
                    timestamp = lastModified;
                }
            }

            if (listOrder && !objectList.isEmpty()) {
                final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone).setSession(session);
                final DateOrderObject[] dateOrderObjectArray = objectList.toArray(new DateOrderObject[objectList.size()]);
                Arrays.sort(dateOrderObjectArray);

                switch (orderDir) {
                    case ASCENDING:
                    case NO_ORDER:
                        for (int a = 0; a < dateOrderObjectArray.length; a++) {
                            final Appointment appointmentObj = (Appointment) dateOrderObjectArray[a].getObject();
                            appointmentwriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                        }
                        break;
                    case DESCENDING:
                        for (int a = dateOrderObjectArray.length - 1; a >= 0; a--) {
                            final Appointment appointmentObj = (Appointment) dateOrderObjectArray[a].getObject();
                            appointmentwriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                        }
                }
            }

            return jsonResponseArray;
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONObject actionGet(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = null;
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final int recurrencePosition = DataParser.parseInt(jsonObj, CalendarFields.RECURRENCE_POSITION);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        try {
            final Appointment appointmentobject = appointmentsql.getObjectById(id, inFolder);
            if (shouldAnonymize(appointmentobject, session.getUserId())) {
                anonymize(appointmentobject);
            }

            final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone).setSession(session);
            appointmentwriter.setSession(session);

            final JSONObject jsonResponseObj = new JSONObject();

            if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && recurrencePosition > 0) {
                // Commented this because this is done in CalendarOperation.loadAppointment():207 that calls extractRecurringInformation()
                // appointmentobject.calculateRecurrence();
                final RecurringResultsInterface recuResults = recColl.calculateRecurring(appointmentobject, 0, 0, recurrencePosition, CalendarCollectionService.MAX_OCCURRENCESE, true);
                if (recuResults.size() == 0) {
                    LOG.warn("No occurrence at position {}", recurrencePosition);
                    throw OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION.create(Integer.valueOf(recurrencePosition));
                }
                final RecurringResultInterface result = recuResults.getRecurringResult(0);
                appointmentobject.setStartDate(new Date(result.getStart()));
                appointmentobject.setEndDate(new Date(result.getEnd()));
                appointmentobject.setRecurrencePosition(result.getPosition());

                appointmentwriter.writeAppointment(appointmentobject, jsonResponseObj);
            } else {
                appointmentwriter.writeAppointment(appointmentobject, jsonResponseObj);
            }

            timestamp = appointmentobject.getLastModified();

            return jsonResponseObj;
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        }
    }

    public JSONObject actionConfirm(final JSONObject jsonObj) throws OXException, OXException, OXException, JSONException {
        final int objectId = DataParser.checkInt(jsonObj, DataFields.ID);
        final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        //DataParser.checkInt(jData, ParticipantsFields.CONFIRMATION);

        final ConfirmableParticipant participant = new ParticipantParser().parseConfirmation(true, jData);

        int userId = user.getId();
        if (jData.has(AJAXServlet.PARAMETER_ID)) {
            userId = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
        }
        final String confirmMessage = participant.getMessage();
        final int confirmStatus = participant.getConfirm();

        final AppointmentSQLInterface appointmentSql = appointmentFactory.createAppointmentSql(session);
        timestamp = null;
        if (participant.getType() == Participant.USER || participant.getType() == 0) {
            timestamp = appointmentSql.setUserConfirmation(objectId, folderId, userId, confirmStatus, confirmMessage);
        } else if (participant.getType() == Participant.EXTERNAL_USER) {
            timestamp = appointmentSql.setExternalConfirmation(objectId, folderId, participant.getEmailAddress(), confirmStatus, confirmMessage);
        } else {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AJAXServlet.PARAMETER_TYPE, jData.get(AJAXServlet.PARAMETER_TYPE));
        }

        return new JSONObject();
    }

    public JSONArray actionHas(final JSONObject jsonObj) throws JSONException, OXException, OXException, OXException {
        final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
        final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        final boolean[] bHas = appointmentsql.hasAppointmentsBetween(start, end);

        final JSONArray jsonResponseArray = new JSONArray();
        for (int a = 0; a < bHas.length; a++) {
            jsonResponseArray.put(bHas[a]);
        }

        return jsonResponseArray;
    }

    public JSONArray actionSearch(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        timestamp = new Date(0);

        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final AppointmentSearchObject searchObj = new AppointmentSearchObject();

        if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
            final int inFolder = DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER);
            searchObj.setFolderIDs(Collections.singleton(Integer.valueOf(inFolder)));
        }

        if (jData.has(SearchFields.PATTERN)) {
            searchObj.setQueries(Collections.singleton(DataParser.parseString(jData, SearchFields.PATTERN)));
        }

        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final String orderDirString = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
        final Order orderDir = OrderFields.parse(orderDirString);

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        final SearchIterator<Appointment> it = appointmentsql.searchAppointments(searchObj, orderBy, orderDir, _appointmentFields);

        final JSONArray jsonResponseArray = new JSONArray();

        try {
            final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone).setSession(session);

            while (it.hasNext()) {
                final Appointment appointment = it.next();

                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {

                    // If this is an recurring appointment, add the first occurrence to the result object
                    RecurringResultsInterface recuResults = null;
                    try {
                        recuResults = recColl.calculateFirstRecurring(appointment);
                    } catch (final OXException x) {
                        LOG.error("Can not calculate recurrence for appointment {} in context {}", appointment.getObjectID(), ctx.getContextId(), x);
                        appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                    }
                    if (recuResults != null && recuResults.size() != 1) {
                        LOG.warn("Can not load first recurring appointment from appointment object {}", appointment.getObjectID());
                        appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                    } else if (recuResults != null) {
                        appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                        appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                        appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                    }
                } else {
                    appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                }

                if (appointment.getLastModified() != null && timestamp.before(appointment.getLastModified())) {
                    timestamp = appointment.getLastModified();
                }
            }

            return jsonResponseArray;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionNewAppointmentsSearch(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);

        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
        final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);

        final Date startUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date endUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_END);

        int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);

        if (orderBy == 0) {
            orderBy = CalendarObject.START_DATE;
        }

        String orderDirString = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
        if (orderDirString == null) {
            orderDirString = "asc";
        }
        final Order orderDir = OrderFields.parse(orderDirString);

        final int limit = DataParser.checkInt(jsonObj, "limit");

        timestamp = new Date(0);

        final Date lastModified = null;

        final AppointmentSearchObject searchObj = new AppointmentSearchObject();
        searchObj.setMinimumEndDate(start);
        searchObj.setMaximumStartDate(end);

        final LinkedList<Appointment> appointmentList = new LinkedList<Appointment>();

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<Appointment> searchIterator = null;
        try {
            final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
            final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            searchIterator = appointmentsql.searchAppointments(searchObj, orderBy, orderDir, _appointmentFields);

            final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone).setSession(session);

            while (searchIterator.hasNext()) {
                final Appointment appointmentobject = searchIterator.next();
                boolean processed = false;
                if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && appointmentobject.getRecurrencePosition() == 0) {
                    // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                    // appointmentobject.calculateRecurrence();
                    RecurringResultsInterface recuResults = null;
                    try {
                        recuResults = recColl.calculateRecurring(appointmentobject, start.getTime(), end.getTime(), 0);
                        processed = true;
                    } catch (final OXException x) {
                        LOG.error("Can not calculate recurrence {}:{}", appointmentobject.getObjectID(), ctx.getContextId(), x);
                    }
                    if (recuResults != null && recuResults.size() > 0) {
                        final RecurringResultInterface result = recuResults.getRecurringResult(0);
                        appointmentobject.setStartDate(new Date(result.getStart()));
                        appointmentobject.setEndDate(new Date(result.getEnd()));
                        appointmentobject.setRecurrencePosition(result.getPosition());

                        if (appointmentobject.getFullTime()) {
                            if (recColl.inBetween(appointmentobject.getStartDate().getTime(), appointmentobject.getEndDate().getTime(), startUTC.getTime(), endUTC.getTime())) {
                                compareStartDateForList(appointmentList, appointmentobject, limit);
                            }
                        } else {
                            compareStartDateForList(appointmentList, appointmentobject, limit);
                        }
                    }
                }
                if (!processed) {
                    if (appointmentobject.getFullTime() && (startUTC != null && endUTC != null)) {
                        if (recColl.inBetween(appointmentobject.getStartDate().getTime(), appointmentobject.getEndDate().getTime(), startUTC.getTime(), endUTC.getTime())) {
                            compareStartDateForList(appointmentList, appointmentobject, limit);
                        }
                    } else {
                        compareStartDateForList(appointmentList, appointmentobject, limit);
                    }
                }

                if (timestamp.before(appointmentobject.getLastModified())) {
                    timestamp = appointmentobject.getLastModified();
                }
            }

            for (int a = 0; a < appointmentList.size(); a++) {
                final Appointment appointmentObj = appointmentList.get(a);
                if (appointmentObj.getFullTime()) {
                    appointmentwriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                } else {
                    appointmentwriter.writeArray(appointmentObj, columns, jsonResponseArray);
                }
            }

            return jsonResponseArray;
        } finally {
            if (searchIterator != null) {
                searchIterator.close();
            }
        }
    }

    public JSONArray actionFreeBusy(final JSONObject jsonObj) throws JSONException, OXException {
        final int userId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int type = DataParser.checkInt(jsonObj, "type");
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
        final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);

        timestamp = new Date(0);

        SearchIterator<Appointment> it = null;

        final JSONArray jsonResponseArray = new JSONArray();

        try {
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(session);

            final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
            it = appointmentsql.getFreeBusyInformation(userId, type, start, end);
            while (it.hasNext()) {
                final Appointment appointmentObj = it.next();
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(appointmentObj, jsonAppointmentObj);
                jsonResponseArray.put(jsonAppointmentObj);
                if (null != appointmentObj.getLastModified() && timestamp.before(appointmentObj.getLastModified())) {
                    timestamp = appointmentObj.getLastModified();
                }
            }
            return jsonResponseArray;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONObject actionCopy(final JSONObject jsonObj) throws JSONException, OXException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final boolean ignoreConflicts = DataParser.checkBoolean(jsonObj, AppointmentFields.IGNORE_CONFLICTS);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);
        final TimeZone timeZone;
        {
            final String timeZoneId = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? this.timeZone : getTimeZone(timeZoneId);
        }

        final AppointmentSQLInterface appointmentSql = appointmentFactory.createAppointmentSql(session);

        timestamp = new Date(0);

        // final JSONObject jsonResponseObject = new JSONObject();
        CalendarDataObject appointmentObj = null;
        try {
            appointmentObj = appointmentSql.getObjectById(id, inFolder);
        } catch (final SQLException exc) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(exc, new Object[0]);
        }

        appointmentObj.removeObjectID();
        appointmentObj.removeUid();
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(ignoreConflicts);
        final Appointment[] conflicts = appointmentSql.insertAppointmentObject(appointmentObj);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts != null) {
            final JSONArray jsonConflictArray = new JSONArray(conflicts.length);
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(session);
            for (int a = 0; a < conflicts.length; a++) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflicts[a], jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }
        } else {
            jsonResponseObj.put(DataFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
        }

        return jsonResponseObj;
    }

    private boolean shouldAnonymize(Appointment cdao, int uid) {
        if (!cdao.getPrivateFlag()) {
            return false;
        }

        if (cdao.getCreatedBy() == uid) {
            return false;
        }

        for (UserParticipant user : cdao.getUsers()) {
            if (user.getIdentifier() == uid) {
                return false;
            }
        }
        return true;
    }

    private void compareStartDateForList(final LinkedList<Appointment> appointmentList, final Appointment appointmentObj, final int limit) {
        if (limit > 0) {
            boolean found = false;

            for (int a = 0; a < appointmentList.size(); a++) {
                final Appointment compareAppointment = appointmentList.get(a);
                if (appointmentObj.getStartDate().getTime() < compareAppointment.getStartDate().getTime()) {
                    appointmentList.add(a, appointmentObj);
                    found = true;
                    break;
                }
            }

            if (!found) {
                appointmentList.addLast(appointmentObj);
            }

            if (appointmentList.size() > limit) {
                appointmentList.removeLast();
            }
        } else {
            appointmentList.add(appointmentObj);
        }
    }

    private Date getDateByFieldId(final int field, final Appointment appointmentObj, final TimeZone timeZone) {
        if (field == CalendarObject.START_DATE) {
            return appointmentObj.getStartDate();
        } else if (field == CalendarObject.END_DATE) {
            return appointmentObj.getEndDate();
        }
        return null;
    }

    /**
     * Creates a new <code>java.util.Date</code> instance with this request's time zone offset subtracted from specified UTC time.
     *
     * @param utcTime The UTC time
     * @return A new <code>java.util.Date</code> with time zone offset applied
     */
    private Date applyTimeZone2Date(final long utcTime) {
        return new Date(utcTime - timeZone.getOffset(utcTime));
    }

    private void anonymize(final Appointment anonymized) {
        //TODO: Solve dependency problem and use AnonymizingIterator#anonymize instead
        anonymized.setTitle("Private");
        anonymized.removeAlarm();
        anonymized.removeCategories();
        anonymized.removeConfirm();
        anonymized.removeConfirmMessage();
        anonymized.removeLabel();
        anonymized.removeLocation();
        anonymized.removeNote();
        anonymized.removeNotification();
        anonymized.removeParticipants();
        anonymized.removeShownAs();
        anonymized.removeUsers();
    }
}
