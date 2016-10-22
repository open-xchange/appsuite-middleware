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

package com.openexchange.calendar.json.actions;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject.Marker;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.groupware.search.Order;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@OAuthAction(AppointmentActionFactory.OAUTH_READ_SCOPE)
public final class UpdatesAction extends ChronosAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdatesAction.class);

    /**
     * Initializes a new {@link UpdatesAction}.
     *
     * @param services
     */
    public UpdatesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        final Date requestedTimestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        Date timestamp = new Date(requestedTimestamp.getTime());

        final Date startUTC = req.getDate(AJAXServlet.PARAMETER_START);
        final Date endUTC = req.getDate(AJAXServlet.PARAMETER_END);
        final Date start = startUTC == null ? null : req.applyTimeZone2Date(startUTC.getTime());
        final Date end = endUTC == null ? null : req.applyTimeZone2Date(endUTC.getTime());
        final String ignore = req.getParameter(AJAXServlet.PARAMETER_IGNORE);

        final boolean bRecurrenceMaster = Boolean.parseBoolean(req.getParameter(RECURRENCE_MASTER));
        final boolean showPrivates = Boolean.parseBoolean(req.getParameter(AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS));

        final int folderId = req.getFolderId();

        boolean showAppointmentInAllFolders = false;
        if (folderId == 0) {
            showAppointmentInAllFolders = true;
        }

        boolean bIgnoreDelete = false;
        boolean bIgnoreModified = false;
        if (null != ignore) {
            if (ignore.indexOf("deleted") >= 0) {
                bIgnoreDelete = true;
            }
            if (ignore.indexOf("changed") >= 0) {
                bIgnoreModified = true;
            }
        }

        if (bIgnoreModified && bIgnoreDelete) {
            // nothing requested
            return new AJAXRequestResult(new JSONArray(0), timestamp, "json");
        }

        if (!bIgnoreDelete && folderId == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_FOLDERID);
        }

        final ServerSession session = req.getSession();
        final AppointmentSqlFactoryService sqlFactoryService = getService();
        if (null == sqlFactoryService) {
            throw ServiceExceptionCode.serviceUnavailable(AppointmentSqlFactoryService.class);
        }
        final AppointmentSQLInterface appointmentsql = sqlFactoryService.createAppointmentSql(session);
        final CalendarCollectionService recColl = getService(CalendarCollectionService.class);
        SearchIterator<Appointment> it = null;
        Date lastModified = null;
        appointmentsql.setIncludePrivateAppointments(showPrivates);
        final CollectionDelta<Appointment> appointments = new CollectionDelta<Appointment>();
        try {
            if (!bIgnoreModified) {
                if (showAppointmentInAllFolders) {
                    if (start == null) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_START);
                    } else if (end == null) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_END);
                    }
                    it = appointmentsql.getModifiedAppointmentsBetween(session.getUserId(), start, end, _appointmentFields, requestedTimestamp, 0, Order.NO_ORDER);
                } else {
                    if (start == null || end == null) {
                        it = appointmentsql.getModifiedAppointmentsInFolder(folderId, _appointmentFields, requestedTimestamp);
                    } else {
                        it = appointmentsql.getModifiedAppointmentsInFolder(folderId, start, end, _appointmentFields, requestedTimestamp);
                    }
                }

                while (it.hasNext()) {
                    Appointment appointmentObj = it.next();
                    boolean written = false;
                    if (appointmentObj.getRecurrenceType() != CalendarObject.NONE && appointmentObj.getRecurrencePosition() == 0) {
                        if (bRecurrenceMaster) {
                            RecurringResultsInterface recuResults = null;
                            try {
                                recuResults = recColl.calculateFirstRecurring(appointmentObj);
                                written = true;
                            } catch (final OXException e) {
                                LOG.error("Can not calculate recurrence {}:{}", appointmentObj.getObjectID(), session.getContextId(), e);
                            }
                            if (recuResults != null && recuResults.size() != 1) {
                                LOG.warn("cannot load first recurring appointment from appointment object: {} / {}\n\n\n", +appointmentObj.getRecurrenceType(), appointmentObj.getObjectID());
                            } else if (recuResults != null) {
                                appointmentObj.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                                appointmentObj.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));

                                appointments.addNewOrModified(appointmentObj);
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
                                LOG.error("Can not calculate recurrence {}:{}", appointmentObj.getObjectID(), session.getContextId(), e);
                            }

                            if (recuResults != null) {
                                for (int a = 0; a < recuResults.size(); a++) {
                                    appointmentObj = appointmentObj.clone();
                                    final RecurringResultInterface result = recuResults.getRecurringResult(a);
                                    appointmentObj.setStartDate(new Date(result.getStart()));
                                    appointmentObj.setEndDate(new Date(result.getEnd()));
                                    appointmentObj.setRecurrencePosition(result.getPosition());

                                    if (startUTC == null || endUTC == null) {
                                        appointments.addNewOrModified(appointmentObj);
                                    } else {
                                        checkAndAddAppointmentAsNewOrModified(appointments, appointmentObj, startUTC, endUTC, recColl);
                                    }
                                }
                            }
                        }
                    }
                    if (!written) {
                        if (startUTC == null || endUTC == null) {
                            appointments.addNewOrModified(appointmentObj);
                        } else {
                            checkAndAddAppointmentAsNewOrModified(appointments, appointmentObj, startUTC, endUTC, recColl);
                        }
                    }

                    lastModified = appointmentObj.getLastModified();

                    if (timestamp.getTime() < lastModified.getTime()) {
                        timestamp = lastModified;
                    }
                }

                SearchIterators.close(it);
                it = null;
            }

            if (!bIgnoreDelete) {
                it = appointmentsql.getDeletedAppointmentsInFolder(folderId, _appointmentFields, requestedTimestamp);
                while (it.hasNext()) {
                    final Appointment appointmentObj = it.next();
                    appointmentObj.setMarker(Marker.ID_ONLY);
                    appointments.addDeleted(appointmentObj);

                    lastModified = appointmentObj.getLastModified();

                    if (timestamp.getTime() < lastModified.getTime()) {
                        timestamp = lastModified;
                    }
                }
            }

            return new AJAXRequestResult(appointments, timestamp, "appointment");
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        } finally {
            SearchIterators.close(it);
        }
    }

    private static final Set<String> REQUIRED_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(
        AJAXServlet.PARAMETER_COLUMNS, AJAXServlet.PARAMETER_TIMESTAMP
    );

    //    {action=updates, columns=1, start=1475043687797, end=1538122887797, showPrivate=true, recurrence_master=true, timestamp=1317198087797, ignore=deleted, sort=201, order=asc, timezone=UTC, session=f95184c1c11d4826b0ec25c86c6970d8}
    private static final Set<String> OPTIONAL_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(
        AJAXServlet.PARAMETER_START, AJAXServlet.PARAMETER_END,
        AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS, AJAXServlet.PARAMETER_RECURRENCE_MASTER,
        AJAXServlet.PARAMETER_TIMEZONE, AJAXServlet.PARAMETER_SORT, AJAXServlet.PARAMETER_ORDER
    );

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        if (false == session.contains(CalendarParameters.PARAMETER_RECURRENCE_MASTER)) {
            session.set(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.FALSE);
        }
        Date since = request.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        String ignore = request.getParameter(AJAXServlet.PARAMETER_IGNORE);
        boolean ignoreUpdated = null != ignore && ignore.contains("changed");
        boolean ignoreDeleted = null != ignore && ignore.contains("deleted");
        int folderID = request.getFolderId();
        List<UserizedEvent> newAndModifiedEvents;
        List<UserizedEvent> deletedEvents;
        if (0 < folderID) {
            if (false == ignoreUpdated) {
                newAndModifiedEvents = session.getCalendarService().getUpdatedEventsInFolder(session, folderID, since);
            } else {
                newAndModifiedEvents = null;
            }
            if (false == ignoreDeleted) {
                deletedEvents = session.getCalendarService().getDeletedEventsInFolder(session, folderID, since);
            } else {
                deletedEvents = null;
            }
        } else {
            if (false == session.contains(CalendarParameters.PARAMETER_RANGE_START)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_START);
            }
            if (false == session.contains(CalendarParameters.PARAMETER_RANGE_END)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_END);
            }
            if (false == ignoreUpdated) {
                newAndModifiedEvents = session.getCalendarService().getUpdatedEventsOfUser(session, since);
            } else {
                newAndModifiedEvents = null;
            }
            if (false == ignoreDeleted) {
                deletedEvents = session.getCalendarService().getDeletedEventsOfUser(session, since);
            } else {
                deletedEvents = null;
            }
        }
        AJAXRequestResult deltaResult = getAppointmentDeltaResultWithTimestamp(session, newAndModifiedEvents, deletedEvents);
        if (null == deltaResult.getTimestamp() || deltaResult.getTimestamp().before(since)) {
            deltaResult.setTimestamp(since);
        }
        return deltaResult;
    }

}
