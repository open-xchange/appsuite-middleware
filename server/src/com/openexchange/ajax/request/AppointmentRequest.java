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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.parser.CalendarParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AppointmentRequest} - Processes appointment requests.
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class AppointmentRequest {

    private static final int DAY_MILLIS = 24 * 60 * 60 * 1000;

    private static final Pattern PATTERN_SPLIT = Pattern.compile(",");

    private static String[] split(final String csv) {
        return PATTERN_SPLIT.split(csv, 0);
    }

    public static final String RECURRENCE_MASTER = "recurrence_master";

    private final static int[] _appointmentFields = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, AppointmentObject.LOCATION,
        CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
        CalendarObject.RECURRENCE_CALCULATOR, CalendarObject.RECURRENCE_ID, CalendarObject.RECURRENCE_POSITION,
        CalendarObject.PARTICIPANTS, CalendarObject.USERS, AppointmentObject.SHOWN_AS, AppointmentObject.DELETE_EXCEPTIONS,
        AppointmentObject.CHANGE_EXCEPTIONS, AppointmentObject.FULL_TIME, AppointmentObject.COLOR_LABEL, AppointmentObject.TIMEZONE };

    private final ServerSession session;

    private final Context ctx;

    private final User user;

    private Date timestamp;

    private final TimeZone timeZone;

    private AppointmentSqlFactoryService appointmentFactory;

    private static final Log LOG = LogFactory.getLog(AppointmentRequest.class);

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

        timeZone = TimeZone.getTimeZone(sTimeZone);
        if (LOG.isDebugEnabled()) {
            LOG.debug("use timezone string: " + sTimeZone);
            LOG.debug("use user timezone: " + timeZone);
        }
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public JSONValue action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, OXConflictException, OXException, JSONException, SearchIteratorException, AjaxException, OXJSONException {
        if (!session.getUserConfiguration().hasCalendar()) {
            throw new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "calendar");
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
        } else {
            throw new AjaxException(AjaxException.Code.UnknownAction, action);
        }
    }

    public JSONObject actionNew(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXConflictException, OXException, AjaxException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final CalendarDataObject appointmentObj = new CalendarDataObject();
        appointmentObj.setContext(ctx);

        final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
        appointmentParser.parse(appointmentObj, jData);

        if (!appointmentObj.containsParentFolderID()) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, AJAXServlet.PARAMETER_FOLDERID);
        }

        final AppointmentSQLInterface appointmentSql = appointmentFactory.createAppointmentSql(session);
        final AppointmentObject[] conflicts = appointmentSql.insertAppointmentObject(appointmentObj);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts == null) {
            jsonResponseObj.put(AppointmentFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
        } else {
            final JSONArray jsonConflictArray = new JSONArray();
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone);
            for (int a = 0; a < conflicts.length; a++) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflicts[a], jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }

            jsonResponseObj.put("conflicts", jsonConflictArray);
        }

        return jsonResponseObj;
    }

    public JSONObject actionUpdate(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXConflictException, OXException, OXJSONException, AjaxException {
        final int objectId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);

        final CalendarDataObject appointmentObj = new CalendarDataObject();
        appointmentObj.setContext(ctx);

        final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
        appointmentParser.parse(appointmentObj, jData);

        appointmentObj.setObjectID(objectId);

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        final AppointmentObject[] conflicts = appointmentsql.updateAppointmentObject(appointmentObj, inFolder, timestamp);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts == null) {
            jsonResponseObj.put(AppointmentFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
        } else {
            final JSONArray jsonConflictArray = new JSONArray();
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone);
            for (int a = 0; a < conflicts.length; a++) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflicts[a], jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }

            jsonResponseObj.put("conflicts", jsonConflictArray);
        }

        return jsonResponseObj;
    }

    public JSONArray actionUpdates(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final Date requestedTimestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        timestamp = new Date(requestedTimestamp.getTime());
        final Date startUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date endUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_END);
        final Date start = startUTC == null ? null : applyTimeZone2Date(startUTC.getTime());
        final Date end = endUTC == null ? null : applyTimeZone2Date(endUTC.getTime());
        final String ignore = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_IGNORE);

        final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);

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

        final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone);
        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        SearchIterator<AppointmentObject> it = null;
        Date lastModified = null;
        try {
            if (!bIgnoreModified) {
                if (showAppointmentInAllFolders) {
                    it = appointmentsql.getModifiedAppointmentsBetween(
                        session.getUserId(),
                        start,
                        end,
                        _appointmentFields,
                        requestedTimestamp,
                        0,
                        null);
                } else {
                    if (start == null || end == null) {
                        it = appointmentsql.getModifiedAppointmentsInFolder(folderId, _appointmentFields, requestedTimestamp, true);
                    } else {
                        it = appointmentsql.getModifiedAppointmentsInFolder(
                            folderId,
                            start,
                            end,
                            _appointmentFields,
                            requestedTimestamp,
                            true);
                    }
                }

                while (it.hasNext()) {
                    final AppointmentObject appointmentObj = it.next();
                    boolean written = false;
                    if (appointmentObj.getRecurrenceType() != CalendarObject.NONE && appointmentObj.getRecurrencePosition() == 0) {
                        if (bRecurrenceMaster) {
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateFirstRecurring(appointmentObj);
                                written = true;
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence " + appointmentObj.getObjectID() + ':' + ctx.getContextId(), e);
                            }
                            if (recuResults != null && recuResults.size() != 1) {
                                LOG.warn("cannot load first recurring appointment from appointment object: " + +appointmentObj.getRecurrenceType() + " / " + appointmentObj.getObjectID() + "\n\n\n");
                            } else if (recuResults != null) {
                                appointmentObj.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                                appointmentObj.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                                appointmentWriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
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
                                    recuResults = recColl.calculateRecurring(
                                        appointmentObj,
                                        start.getTime(),
                                        end.getTime(),
                                        0);
                                    written = true;
                                }
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence " + appointmentObj.getObjectID() + ':' + ctx.getContextId(), e);
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
                    final AppointmentObject appointmentObj = it.next();

                    jsonResponseArray.put(appointmentObj.getObjectID());

                    lastModified = appointmentObj.getLastModified();

                    if (timestamp.getTime() < lastModified.getTime()) {
                        timestamp = lastModified;
                    }
                }
            }

            return jsonResponseArray;
        } catch (final SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e, new Object[0]);
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionDelete(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
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
        } catch (final SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e, new Object[0]);
        }

        return new JSONArray();
    }

    public JSONArray actionList(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
        timestamp = new Date(0);

        Date lastModified = null;

        SearchIterator<AppointmentObject> it = null;

        final HashMap<Integer, ArrayList<Integer>> recurrencePositionMap = new HashMap<Integer, ArrayList<Integer>>();

        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final JSONArray jData = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);

        final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);

        final HashMap<Integer, Integer> objectIdMap = new HashMap<Integer, Integer>();
        for (int a = 0; a < jData.length(); a++) {
            final JSONObject jObject = jData.getJSONObject(a);
            final int objectId = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_ID);
            final int folderId = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_FOLDERID);

            objectIdMap.put(Integer.valueOf(objectId), Integer.valueOf(folderId));

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
                ArrayList<Integer> recurrencePosList = null;
                if (recurrencePositionMap.containsKey(Integer.valueOf(objectId))) {
                    recurrencePosList = recurrencePositionMap.get(Integer.valueOf(objectId));
                } else {
                    recurrencePosList = new ArrayList<Integer>();
                }
                recurrencePosList.add(Integer.valueOf(recurrencePosition));
                recurrencePositionMap.put(Integer.valueOf(objectId), recurrencePosList);
            }
        }

        final int size = objectIdMap.size();
        final int[][] objectIdAndFolderId = new int[size][2];

        final Iterator<Map.Entry<Integer, Integer>> iterator = objectIdMap.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<Integer, Integer> entry = iterator.next();
            objectIdAndFolderId[i][0] = entry.getKey().intValue();
            objectIdAndFolderId[i][1] = entry.getValue().intValue();
        }

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        it = appointmentsql.getObjectsById(objectIdAndFolderId, _appointmentFields);
        try {
            int counter = 0;
            final JSONArray jsonResponseArray = new JSONArray();
            while (it.hasNext()) {
                final AppointmentObject appointment = it.next();
                if (null == appointment) {
                    continue;
                }
                final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone);

                final Date startDate = appointment.getStartDate();
                final Date endDate = appointment.getEndDate();

                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {
                    if (bRecurrenceMaster) {
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateFirstRecurring(appointment);
                        } catch (final OXException e) {
                            LOG.error("Can not calculate recurrence " + appointment.getObjectID() + ":" + ctx.getContextId(), e);
                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        }

                        if (recuResults != null && recuResults.size() == 1) {
                            appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                            appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        } else {
                            LOG.warn("cannot load first recurring appointment from appointment object: " + +appointment.getRecurrenceType() + " / " + appointment.getObjectID() + "\n\n\n");
                        }
                    } else {
                        // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                        // appointment.calculateRecurrence();
                        if (recurrencePositionMap.containsKey(Integer.valueOf(appointment.getObjectID()))) {
                            final ArrayList<Integer> recurrencePosList = recurrencePositionMap.get(Integer.valueOf(appointment.getObjectID()));

                            for (int a = 0; a < recurrencePosList.size(); a++) {
                                appointment.setStartDate(startDate);
                                appointment.setEndDate(endDate);
                                final RecurringResultsInterface recuResults = recColl.calculateRecurring(
                                    appointment,
                                    0,
                                    0,
                                    recurrencePosList.get(a).intValue());
                                if (recuResults.size() > 0) {
                                    final RecurringResultInterface result = recuResults.getRecurringResult(0);
                                    appointment.setStartDate(new Date(result.getStart()));
                                    appointment.setEndDate(new Date(result.getEnd()));
                                    appointment.setRecurrencePosition(result.getPosition());
                                } else {
                                    throw new OXObjectNotFoundException("no recurrence appointment found at pos: " + counter);
                                }

                                appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                            }
                        } else {
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateFirstRecurring(appointment);
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence " + appointment.getObjectID() + ":" + ctx.getContextId(), e);
                                appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                            }
                            if (recuResults != null && recuResults.size() > 0) {
                                final RecurringResultInterface result = recuResults.getRecurringResult(0);
                                appointment.setStartDate(new Date(result.getStart()));
                                appointment.setEndDate(new Date(result.getEnd()));
                                appointment.setRecurrencePosition(result.getPosition());
                            } else if (recuResults != null) {
                                throw new OXObjectNotFoundException("no recurrence appointment found at pos: " + counter);
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
            LOG.error(e.getMessage(), e);
            throw e;
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionAll(final JSONObject jsonObj) throws SearchIteratorException, OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
        timestamp = new Date(0);

        SearchIterator<AppointmentObject> it = null;

        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final Date startUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date endUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_END);
        final Date start = applyTimeZone2Date(startUTC.getTime());
        final Date end = applyTimeZone2Date(endUTC.getTime());
        final int folderId = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);

        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final boolean listOrder;
        if (orderBy == CalendarObject.START_DATE || orderBy == CalendarObject.END_DATE) {
            listOrder = true;
        } else {
            listOrder = false;
        }

        final List<DateOrderObject> objectList = new ArrayList<DateOrderObject>();

        final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);

        final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);

        final int leftHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.LEFT_HAND_LIMIT);
        final int rightHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.RIGHT_HAND_LIMIT);

        boolean showAppointmentInAllFolders = false;

        if (folderId == 0) {
            showAppointmentInAllFolders = true;
        }

        final JSONArray jsonResponseArray = new JSONArray();
        try {
            final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
            CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            if (showAppointmentInAllFolders) {
                it = appointmentsql.getAppointmentsBetween(user.getId(), start, end, _appointmentFields, orderBy, orderDir);
            } else {
                it = appointmentsql.getAppointmentsBetweenInFolder(folderId, _appointmentFields, start, end, orderBy, orderDir);
            }
            Date lastModified = new Date(0);
            while (it.hasNext()) {
                final AppointmentObject appointment = it.next();
                final AppointmentWriter writer = new AppointmentWriter(timeZone);
                boolean written = false;
                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {
                    if (bRecurrenceMaster) {
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateFirstRecurring(appointment);
                            written = true;
                        } catch (final OXException e) {
                            LOG.error("Can not calculate recurrence " + appointment.getObjectID() + ':' + ctx.getContextId(), e);
                        }
                        if (recuResults != null && recuResults.size() == 1) {
                            appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                            appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                            writer.writeArray(appointment, columns, startUTC, endUTC, jsonResponseArray);
                        } else {
                            LOG.warn("cannot load first recurring appointment from appointment object: " + +appointment.getRecurrenceType() + " / " + appointment.getObjectID() + "\n\n\n");
                        }
                    } else {
                        // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                        // appointment.calculateRecurrence();
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateRecurring(appointment, start.getTime(), end.getTime(), 0);
                            written = true;
                        } catch (final OXException e) {
                            LOG.error("Can not calculate recurrence " + appointment.getObjectID() + ':' + ctx.getContextId(), e);
                        }
                        if (recuResults != null) {
                            for (int a = 0; a < recuResults.size(); a++) {
                                final RecurringResultInterface result = recuResults.getRecurringResult(a);
                                appointment.setStartDate(new Date(result.getStart()));
                                appointment.setEndDate(new Date(result.getEnd()));
                                appointment.setRecurrencePosition(result.getPosition());

                                // add to order list
                                if (listOrder) {
                                    final DateOrderObject dateOrderObject = new DateOrderObject(getDateByFieldId(
                                        orderBy,
                                        appointment,
                                        timeZone), appointment.clone());
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
                        final DateOrderObject dateOrderObject = new DateOrderObject(
                            getDateByFieldId(orderBy, appointment, timeZone),
                            appointment.clone());
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
                final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone);
                final DateOrderObject[] dateOrderObjectArray = objectList.toArray(new DateOrderObject[objectList.size()]);
                Arrays.sort(dateOrderObjectArray);

                boolean asc;

                if (orderDir == null) {
                    asc = true;
                } else {
                    if (orderDir.equalsIgnoreCase("desc")) {
                        asc = false;
                    } else {
                        asc = true;
                    }
                }

                if (asc) {
                    for (int a = 0; a < dateOrderObjectArray.length; a++) {
                        final AppointmentObject appointmentObj = (AppointmentObject) dateOrderObjectArray[a].getObject();
                        appointmentwriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                    }
                } else {
                    for (int a = dateOrderObjectArray.length - 1; a >= 0; a--) {
                        final AppointmentObject appointmentObj = (AppointmentObject) dateOrderObjectArray[a].getObject();
                        appointmentwriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                    }
                }
            }

            return jsonResponseArray;
        } catch (final SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e, new Object[0]);
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONObject actionGet(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXObjectNotFoundException, OXException, OXJSONException, AjaxException {
        timestamp = null;
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final int recurrencePosition = DataParser.parseInt(jsonObj, CalendarFields.RECURRENCE_POSITION);

        final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
        CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        try {
            final AppointmentObject appointmentobject = appointmentsql.getObjectById(id, inFolder);
            final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone);

            final JSONObject jsonResponseObj = new JSONObject();

            if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && recurrencePosition > 0) {
                // Commented this because this is done in CalendarOperation.loadAppointment():207 that calls extractRecurringInformation()
                // appointmentobject.calculateRecurrence();
                final RecurringResultsInterface recuResults = recColl.calculateRecurring(
                    appointmentobject,
                    0,
                    0,
                    recurrencePosition,
                    recColl.MAXTC,
                    true);
                if (recuResults.size() == 0) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(new StringBuilder(32).append("No occurrence at position ").append(recurrencePosition));
                    }
                    throw new OXCalendarException(OXCalendarException.Code.UNKNOWN_RECURRENCE_POSITION, Integer.valueOf(recurrencePosition));
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
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e, new Object[0]);
        }
    }

    public JSONObject actionConfirm(final JSONObject jsonObj) throws OXException, AjaxException, OXJSONException, JSONException {
        int objectId = DataParser.checkInt(jsonObj, DataFields.ID);
        int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        int userId = user.getId();
        if (jData.has(AJAXServlet.PARAMETER_ID)) {
            userId = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
        }
        String confirmMessage = DataParser.checkString(jData, CalendarFields.CONFIRM_MESSAGE);
        int confirmStatus = DataParser.checkInt(jData, CalendarFields.CONFIRMATION);

        final AppointmentSQLInterface appointmentSql = appointmentFactory.createAppointmentSql(session);
        timestamp = appointmentSql.setUserConfirmation(objectId, folderId, userId, confirmStatus, confirmMessage);

        return new JSONObject();
    }

    public JSONArray actionHas(final JSONObject jsonObj) throws JSONException, OXException, OXJSONException, AjaxException {
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

    public JSONArray actionSearch(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, SearchIteratorException, OXConflictException, OXException, OXJSONException, AjaxException {
        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);

        timestamp = new Date(0);

        final Date lastModified = null;

        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final AppointmentSearchObject searchObj = new AppointmentSearchObject();
        if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
            searchObj.setFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
        }

        if (jData.has(SearchFields.PATTERN)) {
            searchObj.setPattern(DataParser.parseString(jData, SearchFields.PATTERN));
        }

        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);

        int limit = 0;
        boolean hasLimit = false;
        if (jsonObj.has("limit")) {
            limit = DataParser.checkInt(jsonObj, "limit");
            hasLimit = true;
        }

        final Date start = DataParser.parseTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
        final Date end = DataParser.parseTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);

        final Date startUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date endUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_END);

        searchObj.setFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
        searchObj.setTitle(DataParser.parseString(jData, AppointmentFields.TITLE));
        searchObj.setSearchInNote(DataParser.parseBoolean(jData, "searchinnote"));
        searchObj.setCatgories(DataParser.parseString(jData, AppointmentFields.CATEGORIES));
        searchObj.setSubfolderSearch(DataParser.parseBoolean(jData, "subfoldersearch"));
        searchObj.setAllFolders(DataParser.parseBoolean(jData, "allfolders"));

        if (start != null && end != null) {
            searchObj.setRange(new Date[] { start, end });
        }
        // searchObj.setRange(DataParser.parseJSONDateArray(jData, "daterange"));

        if (jData.has(CalendarFields.PARTICIPANTS)) {
            final Participants participants = new Participants();
            searchObj.setParticipants(CalendarParser.parseParticipants(jData, participants));
        }

        final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<AppointmentObject> it = null;
        try {
            final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
            CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);

            if (searchObj.getFolder() > 0 && searchObj.getPattern() != null) {
                it = appointmentsql.searchAppointments(searchObj.getPattern(), searchObj.getFolder(), orderBy, orderDir, _appointmentFields);
            } else {
                it = appointmentsql.getAppointmentsByExtendedSearch(searchObj, orderBy, orderDir, _appointmentFields);
            }

            int counter = 0;

            /*-
             * TODO: Enable if limit is supported
             * 
            List<CalendarDataObject> recurrenceAppointmentList = null;
            List<CalendarDataObject> appointmentSortedList = null;

            if (hasLimit) {
                recurrenceAppointmentList = new ArrayList<CalendarDataObject>();
                appointmentSortedList = new ArrayList<CalendarDataObject>();
            }
             */

            final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone);
            while (it.hasNext()) {
                final AppointmentObject appointment = it.next();

                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {
                    if (start != null && end != null) {
                        if (bRecurrenceMaster) {
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateFirstRecurring(appointment);
                            } catch (final OXException x) {
                                LOG.error("Can not calculate recurrence " + appointment.getObjectID() + ":" + ctx.getContextId(), x);
                                appointmentwriter.writeArray(appointment, columns, startUTC, endUTC, jsonResponseArray);
                            }
                            if (recuResults != null && recuResults.size() == 1) {
                                appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                                appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                                appointmentwriter.writeArray(appointment, columns, startUTC, endUTC, jsonResponseArray);
                            } else {
                                LOG.warn("cannot load first recurring appointment from appointment object: " + +appointment.getRecurrenceType() + " / " + appointment.getObjectID() + "\n\n\n");
                            }
                        } else {
                            // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                            // appointment.calculateRecurrence();
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateRecurring(
                                    appointment,
                                    start.getTime(),
                                    end.getTime(),
                                    0);
                            } catch (final OXException x) {
                                LOG.error("Can not calculate recurrence " + appointment.getObjectID() + ":" + ctx.getContextId(), x);
                                if (appointment.getFullTime()) {
                                    if (recColl.inBetween(
                                        appointment.getStartDate().getTime(),
                                        appointment.getEndDate().getTime(),
                                        startUTC.getTime(),
                                        endUTC.getTime())) {
                                        appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                                        counter++;
                                    }
                                } else {
                                    appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                                    counter++;
                                }
                            }
                            if (recuResults != null && recuResults.size() > 0) {
                                final RecurringResultInterface result = recuResults.getRecurringResult(0);
                                appointment.setStartDate(new Date(result.getStart()));
                                appointment.setEndDate(new Date(result.getEnd()));
                                appointment.setRecurrencePosition(result.getPosition());

                                if (appointment.getFullTime()) {
                                    if (recColl.inBetween(
                                        appointment.getStartDate().getTime(),
                                        appointment.getEndDate().getTime(),
                                        startUTC.getTime(),
                                        endUTC.getTime())) {
                                        appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                                        counter++;
                                    }
                                } else {
                                    appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                                    counter++;
                                }
                            }
                        }
                    } else {
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateFirstRecurring(appointment);
                        } catch (final OXException x) {
                            LOG.error("Can not calculate recurrence " + appointment.getObjectID() + ":" + ctx.getContextId(), x);
                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        }
                        if (recuResults != null && recuResults.size() != 1) {
                            LOG.warn("cannot load first recurring appointment from appointment object: " + +appointment.getRecurrenceType() + " / " + appointment.getObjectID() + "\n\n\n");
                        } else if (recuResults != null) {
                            appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                            appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        }
                    }
                } else {
                    if (appointment.getFullTime() && (startUTC != null && endUTC != null)) {
                        if (recColl.inBetween(
                            appointment.getStartDate().getTime(),
                            appointment.getEndDate().getTime(),
                            startUTC.getTime(),
                            endUTC.getTime())) {
                            appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                            counter++;
                        }
                    } else {
                        appointmentwriter.writeArray(appointment, columns, jsonResponseArray);
                        counter++;
                    }
                }

                if (timestamp.before(appointment.getLastModified())) {
                    timestamp = appointment.getLastModified();
                }

                if (hasLimit && counter >= limit) {
                    break;
                }
            }

            return jsonResponseArray;
        } catch (final SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e, new Object[0]);
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public JSONArray actionNewAppointmentsSearch(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, SearchIteratorException, OXConflictException, OXException, OXJSONException, AjaxException {
        final String[] sColumns = split(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);

        final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
        final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);

        final Date startUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date endUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_END);

        int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);

        if (orderBy == 0) {
            orderBy = CalendarObject.START_DATE;
        }

        String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
        if (orderDir == null) {
            orderDir = "asc";
        }

        final int limit = DataParser.checkInt(jsonObj, "limit");

        timestamp = new Date(0);

        final Date lastModified = null;

        final AppointmentSearchObject searchObj = new AppointmentSearchObject();
        searchObj.setRange(new Date[] { start, end });

        final LinkedList<AppointmentObject> appointmentList = new LinkedList<AppointmentObject>();

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<AppointmentObject> searchIterator = null;
        try {
            final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
            CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            searchIterator = appointmentsql.getAppointmentsByExtendedSearch(searchObj, orderBy, orderDir, _appointmentFields);

            final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone);

            while (searchIterator.hasNext()) {
                final AppointmentObject appointmentobject = searchIterator.next();
                boolean processed = false;
                if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && appointmentobject.getRecurrencePosition() == 0) {
                    // Commented this because this is done in CalendarOperation.next():726 that calls extractRecurringInformation()
                    // appointmentobject.calculateRecurrence();
                    RecurringResultsInterface recuResults = null;
                    try {
                        recuResults = recColl.calculateRecurring(appointmentobject, start.getTime(), end.getTime(), 0);
                        processed = true;
                    } catch (final OXException x) {
                        LOG.error("Can not calculate recurrence " + appointmentobject.getObjectID() + ":" + ctx.getContextId(), x);
                    }
                    if (recuResults != null && recuResults.size() > 0) {
                        final RecurringResultInterface result = recuResults.getRecurringResult(0);
                        appointmentobject.setStartDate(new Date(result.getStart()));
                        appointmentobject.setEndDate(new Date(result.getEnd()));
                        appointmentobject.setRecurrencePosition(result.getPosition());

                        if (appointmentobject.getFullTime()) {
                            if (recColl.inBetween(
                                appointmentobject.getStartDate().getTime(),
                                appointmentobject.getEndDate().getTime(),
                                startUTC.getTime(),
                                endUTC.getTime())) {
                                compareStartDateForList(appointmentList, appointmentobject, limit);
                            }
                        } else {
                            compareStartDateForList(appointmentList, appointmentobject, limit);
                        }
                    }
                }
                if (!processed) {
                    if (appointmentobject.getFullTime() && (startUTC != null && endUTC != null)) {
                        if (recColl.inBetween(
                            appointmentobject.getStartDate().getTime(),
                            appointmentobject.getEndDate().getTime(),
                            startUTC.getTime(),
                            endUTC.getTime())) {
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
                final AppointmentObject appointmentObj = appointmentList.get(a);
                if (appointmentObj.getFullTime()) {
                    appointmentwriter.writeArray(appointmentObj, columns, startUTC, endUTC, jsonResponseArray);
                } else {
                    appointmentwriter.writeArray(appointmentObj, columns, jsonResponseArray);
                }
            }

            return jsonResponseArray;
        } catch (final SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, e, new Object[0]);
        } finally {
            if (searchIterator != null) {
                searchIterator.close();
            }
        }
    }

    public JSONArray actionFreeBusy(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXMandatoryFieldException, OXException, OXJSONException, AjaxException {
        final int userId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int type = DataParser.checkInt(jsonObj, "type");
        final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
        final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);

        timestamp = new Date(0);

        SearchIterator<AppointmentObject> it = null;

        final JSONArray jsonResponseArray = new JSONArray();

        try {
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone);

            final AppointmentSQLInterface appointmentsql = appointmentFactory.createAppointmentSql(session);
            it = appointmentsql.getFreeBusyInformation(userId, type, start, end);
            while (it.hasNext()) {
                final AppointmentObject appointmentObj = it.next();
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

    public JSONObject actionCopy(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, OXObjectNotFoundException, OXException, OXJSONException, AjaxException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final boolean ignoreConflicts = DataParser.checkBoolean(jsonObj, AppointmentFields.IGNORE_CONFLICTS);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);

        final AppointmentSQLInterface appointmentSql = appointmentFactory.createAppointmentSql(session);

        timestamp = new Date(0);

        // final JSONObject jsonResponseObject = new JSONObject();
        CalendarDataObject appointmentObj = null;
        try {
            appointmentObj = appointmentSql.getObjectById(id, inFolder);
        } catch (final SQLException exc) {
            throw new OXCalendarException(OXCalendarException.Code.CALENDAR_SQL_ERROR, exc, new Object[0]);
        }

        appointmentObj.removeObjectID();
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(ignoreConflicts);
        final AppointmentObject[] conflicts = appointmentSql.insertAppointmentObject(appointmentObj);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts != null) {
            final JSONArray jsonConflictArray = new JSONArray();
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone);
            for (int a = 0; a < conflicts.length; a++) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflicts[a], jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }
        } else {
            jsonResponseObj.put(AppointmentFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
        }

        return jsonResponseObj;
    }

    private void compareStartDateForList(final LinkedList<AppointmentObject> appointmentList, final AppointmentObject appointmentObj, final int limit) {
        if (limit > 0) {
            boolean found = false;

            for (int a = 0; a < appointmentList.size(); a++) {
                final AppointmentObject compareAppointment = appointmentList.get(a);
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

    private Date getDateByFieldId(final int field, final AppointmentObject appointmentObj, final TimeZone timeZone) {
        final Date date = null;
        if (field == CalendarObject.START_DATE) {
            return appointmentObj.getStartDate();
        } else if (field == CalendarObject.END_DATE) {
            return appointmentObj.getEndDate();
        }

        if (date == null) {
            return null;
        }

        if (appointmentObj.getFullTime()) {
            return date;
        }
        final int offset = timeZone.getOffset(date.getTime());
        return new Date(date.getTime() + offset);
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
}
