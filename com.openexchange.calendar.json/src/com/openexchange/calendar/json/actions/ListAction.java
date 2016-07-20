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

package com.openexchange.calendar.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.PUT, name = "list", description = "Get a list of appointments.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for appointments are defined in Common object data, Detailed task and appointment data and Detailed appointment data. The alias \"list\" uses the predefined columnset [1, 20, 207, 206, 2, 200, 201, 202, 203, 209, 221, 401, 402, 102, 400, 101, 220, 215, 100]."),
    @Parameter(name = "recurrence_master", description = "Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.")
}, requestBody = "An array with full object IDs (folder, id and optionally either recurrence_position or recurrence_date_position) of requested appointments.",
    responseDescription = "Response with timestamp: An array with appointment data. Each array element describes one appointment and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
@OAuthAction(AppointmentActionFactory.OAUTH_READ_SCOPE)
public final class ListAction extends ChronosAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(ListAction.class);

    /**
     * Initializes a new {@link ListAction}.
     *
     * @param services
     */
    public ListAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        Date timestamp = new Date(0);

        Date lastModified = null;

        SearchIterator<Appointment> it = null;

        final TIntObjectMap<TIntList> recurrencePositionMap = new TIntObjectHashMap<TIntList>();
        final JSONArray jData = req.getData();
        if (null == jData) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        final boolean bRecurrenceMaster = req.parseBoolean(RECURRENCE_MASTER);

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
                TIntList recurrencePosList = null;
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

        final AppointmentSqlFactoryService factoryService = getService();
        if (null == factoryService) {
            throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
        }
        final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(req.getSession());
        final CalendarCollectionService recColl = getService(CalendarCollectionService.class);
        it = appointmentsql.getObjectsById(objectIdAndFolderId, _appointmentFields);
        final List<Appointment> appointmentList = new ArrayList<Appointment>(16);
        try {
            int counter = 0;
            while (it.hasNext()) {
                Appointment appointment = it.next();
                if (null == appointment) {
                    continue;
                }

                final Date startDate = appointment.getStartDate();
                final Date endDate = appointment.getEndDate();

                if (appointment.getRecurrenceType() != CalendarObject.NONE && appointment.getRecurrencePosition() == 0) {
                    if (bRecurrenceMaster) {
                        RecurringResultsInterface recuResults = null;
                        try {
                            recuResults = recColl.calculateFirstRecurring(appointment);
                        } catch (final OXException e) {
                            LOG.error("Can not calculate recurrence {}:{}", appointment.getObjectID(), req.getSession().getContextId(), e);
                            appointmentList.add(appointment);
                        }

                        if (recuResults != null && recuResults.size() == 1) {
                            appointment.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                            appointment.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                            appointmentList.add(appointment);
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
                                appointment = appointment.clone();
                                appointment.setStartDate(startDate);
                                appointment.setEndDate(endDate);
                                final RecurringResultsInterface recuResults = recColl.calculateRecurring(
                                    appointment,
                                    0,
                                    0,
                                    recurrencePosList.get(a));
                                if (recuResults.size() > 0) {
                                    final RecurringResultInterface result = recuResults.getRecurringResult(0);
                                    appointment.setStartDate(new Date(result.getStart()));
                                    appointment.setEndDate(new Date(result.getEnd()));
                                    appointment.setRecurrencePosition(result.getPosition());
                                } else {
                                    throw OXException.notFound("no recurrence appointment found at pos: " + counter);
                                }

                                appointmentList.add(appointment);
                            }
                        } else {
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateFirstRecurring(appointment);
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence {}:{}", appointment.getObjectID(), req.getSession().getContextId(), e);
                                appointmentList.add(appointment);
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

                            appointmentList.add(appointment);
                        }
                    }
                } else {
                    appointmentList.add(appointment);
                }

                lastModified = appointment.getLastModified();

                if (timestamp.getTime() < lastModified.getTime()) {
                    timestamp = lastModified;
                }

                counter++;
            }

            return new AJAXRequestResult(appointmentList, timestamp, "appointment");
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

    private static final String[] REQUIRED_PARAMETERS = {
        CalendarParameters.PARAMETER_FIELDS
    };

    @Override
    protected AJAXRequestResult perform(CalendarService calendarService, AppointmentAJAXRequest request) throws OXException, JSONException {
        CalendarSession calendarSession = initSession(request, REQUIRED_PARAMETERS);
        List<EventID> requestedIDs = parseRequestedIDs(request);
        List<UserizedEvent> events = calendarService.getEvents(calendarSession, requestedIDs);
        return getAppointmentResultWithTimestamp(events);
    }

}
