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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentAJAXRequestFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link AppointmentAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public abstract class AppointmentAction implements AJAXActionService {

    private static final AJAXRequestResult RESULT_JSON_NULL = new AJAXRequestResult(JSONObject.NULL, "json");

    public static final int[] COLUMNS_ALL_ALIAS = new int[] { 1, 20, 207, 206, 2 };

    public static final int[] COLUMNS_LIST_ALIAS = new int[] {
        1, 20, 207, 206, 2, 200, 201, 202, 203, 209, 221, 401, 402, 102, 400, 101, 220, 215, 100 };

    public static final String RECURRENCE_MASTER = "recurrence_master";

    protected static final int DAY_MILLIS = 24 * 60 * 60 * 1000;

    protected final static int[] _appointmentFields = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION,
        CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
        CalendarObject.RECURRENCE_CALCULATOR, CalendarObject.RECURRENCE_ID, CalendarObject.RECURRENCE_POSITION,
        CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.DELETE_EXCEPTIONS,
        Appointment.CHANGE_EXCEPTIONS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.TIMEZONE, Appointment.ORGANIZER, Appointment.ORGANIZER_ID, Appointment.PRINCIPAL, Appointment.PRINCIPAL_ID,
        Appointment.UID, Appointment.SEQUENCE, Appointment.CONFIRMATIONS, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT,
        Appointment.NUMBER_OF_ATTACHMENTS };

    private static final Pattern PATTERN_SPLIT = Pattern.compile(" *, *");

    protected static String[] split(final String csv) {
        return PATTERN_SPLIT.split(csv, 0);
    }

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractAppointmentAction}.
     */
    protected AppointmentAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the {@link AppointmentSqlFactoryService} instance.
     *
     * @return The service
     */
    protected AppointmentSqlFactoryService getService() {
        return services.getService(AppointmentSqlFactoryService.class);
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        if (!session.getUserPermissionBits().hasCalendar()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("calendar");
        }
        try {
            final AppointmentAJAXRequest ar = AppointmentAJAXRequestFactory.createAppointmentAJAXRequest(requestData, session);
            boolean performNew = true;
            return performNew ? performNew(ar) : perform(ar);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs specified appointment request.
     *
     * @param req The appointment request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(AppointmentAJAXRequest req) throws OXException, JSONException;

    protected AJAXRequestResult performNew(AppointmentAJAXRequest req) throws OXException, JSONException {
        return perform(req);
    }

    /**
     * Gets the result filled with JSON <code>NULL</code>.
     *
     * @return The result with JSON <code>NULL</code>.
     */
    protected static AJAXRequestResult getJSONNullResult() {
        return RESULT_JSON_NULL;
    }

    protected void anonymize(final Appointment anonymized) {
        // TODO: Solve dependency problem and use AnonymizingIterator#anonymize instead
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

    protected boolean shouldAnonymize(Appointment cdao, int uid) {
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

    protected Date getDateByFieldId(final int field, final Appointment appointmentObj, final TimeZone timeZone) {
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

    protected void compareStartDateForList(final LinkedList<Appointment> appointmentList, final Appointment appointmentObj, final int limit) {
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

    protected void convertExternalToInternalUsersIfPossible(final CalendarObject appointmentObj, final Context ctx, final org.slf4j.Logger log) {
        final Participant[] participants = appointmentObj.getParticipants();
        if (participants == null) {
            return;
        }

        final UserService us = getService(UserService.class);

        for (int pos = 0; pos < participants.length; pos++) {
            final Participant part = participants[pos];
            if (part.getType() == Participant.EXTERNAL_USER) {
                User foundUser;
                try {
                    foundUser = us.searchUser(part.getEmailAddress(), ctx);
                    if (foundUser == null) {
                        continue;
                    }
                    participants[pos] = new UserParticipant(foundUser.getId());
                } catch (final OXException e) {
                    log.debug("Couldn't resolve E-Mail address to an internal user: {}", part.getEmailAddress(), e); // ...and continue doing this for the remaining users
                }
            }
        }

        appointmentObj.setParticipants(participants);
    }

    protected void checkAndAddAppointment(final List<Appointment> appointmentList, final Appointment appointmentObj, final Date betweenStart, final Date betweenEnd, final CalendarCollectionService calColl) {
        if (appointmentObj.getFullTime() && betweenStart != null && betweenEnd != null) {
            if (calColl.inBetween(
                appointmentObj.getStartDate().getTime(),
                appointmentObj.getEndDate().getTime(),
                betweenStart.getTime(),
                betweenEnd.getTime())) {
                appointmentList.add(appointmentObj);
            }
        } else {
            appointmentList.add(appointmentObj);
        }
    }

    protected void checkAndAddAppointmentAsNewOrModified(final CollectionDelta<Appointment> appointmentList, final Appointment appointmentObj, final Date betweenStart, final Date betweenEnd, final CalendarCollectionService calColl) {
        if (appointmentObj.getFullTime() && betweenStart != null && betweenEnd != null) {
            if (calColl.inBetween(
                appointmentObj.getStartDate().getTime(),
                appointmentObj.getEndDate().getTime(),
                betweenStart.getTime(),
                betweenEnd.getTime())) {
                appointmentList.addNewOrModified(appointmentObj);
            }
        } else {
            appointmentList.addNewOrModified(appointmentObj);
        }
    }

}
