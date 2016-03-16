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

package com.openexchange.calendar.api.itip;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link CalendarITipIntegrationUtility}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarITipIntegrationUtility implements ITipIntegrationUtility {

    private final AppointmentSqlFactoryService factory;

    private final CalendarCollectionService calendarCollection;

    private final ContextService contexts;

    public CalendarITipIntegrationUtility(final AppointmentSqlFactory factory, final CalendarCollectionService calendarCollection, final ContextService contexts) {
        this.factory = factory;
        this.contexts = contexts;
        this.calendarCollection = calendarCollection;
    }

    // TODO: Find a better way
    @Override
    public List<Appointment> getConflicts(final CalendarDataObject appointment, final Session session) throws OXException {
    	if (appointment == null) {
    		return Collections.emptyList();
    	}
        final AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
        final List<Appointment> conflicts = new ArrayList<Appointment>();
        boolean checkedUser = false;
        if (appointment.getParticipants() != null) {
            for (final Participant participant : appointment.getParticipants()) {
                if (!UserParticipant.class.isInstance(participant)) {
                    continue;
                }
                final UserParticipant userParticipant = (UserParticipant) participant;
                checkedUser = checkedUser || userParticipant.getIdentifier() == session.getUserId();
                final SearchIterator<Appointment> freeBusyInformation = appointments.getFreeBusyInformation(
                    userParticipant.getIdentifier(),
                    Participant.USER,
                    appointment.getStartDate(),
                    appointment.getEndDate());
                while (freeBusyInformation.hasNext()) {
                    final Appointment next = freeBusyInformation.next();
                    next.setParticipants(new Participant[] { userParticipant });
                    conflicts.add(next);
                }

            }
        }
        if (!checkedUser) {
            final SearchIterator<Appointment> freeBusyInformation = appointments.getFreeBusyInformation(
                session.getUserId(),
                Participant.USER,
                appointment.getStartDate(),
                appointment.getEndDate());
            while (freeBusyInformation.hasNext()) {
                final Appointment next = freeBusyInformation.next();
                next.setParticipants(new Participant[] { new UserParticipant(session.getUserId()) });
                conflicts.add(next);
            }
        }
        return conflicts;
    }

    private static final Set<Integer> EXEMPT = new HashSet<Integer>(Arrays.asList(
        Appointment.RECURRENCE_START,
        Appointment.ALARM,
        Appointment.RECURRENCE_DATE_POSITION,
        Appointment.DAYS,
        Appointment.DAY_IN_MONTH,
        Appointment.MONTH,
        Appointment.INTERVAL,
        Appointment.UNTIL,
        Appointment.NOTIFICATION,
        Appointment.RECURRENCE_COUNT,
//        Appointment.NUMBER_OF_LINKS,
        Appointment.LAST_MODIFIED_UTC));

    private static final int[] EXCEPTION_FIELDS = new int[Appointment.ALL_COLUMNS.length - EXEMPT.size()];

    static {
        int i = 0;
        for (final int col : Appointment.ALL_COLUMNS) {
            if (!EXEMPT.contains(col)) { // FIXME: Broken fields, fix underlying calendar
                EXCEPTION_FIELDS[i++] = col;
            }
        }
    }

    @Override
    public List<Appointment> getExceptions(final Appointment original, final Session session) throws OXException {
        final CalendarDataObject[] changeExceptionsByRecurrence = calendarCollection.getChangeExceptionsByRecurrence(
            original.getObjectID(),
            EXCEPTION_FIELDS,
            session);
        final List<Appointment> appointments = new ArrayList<Appointment>(changeExceptionsByRecurrence.length);
        for (final CalendarDataObject calendarDataObject : changeExceptionsByRecurrence) {
            appointments.add(calendarDataObject);
        }
        return appointments;
    }

    @Override
    public CalendarDataObject resolveUid(final String uid, final Session session) throws OXException {
        final AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
        final int resolved = appointments.resolveUid(uid);
        if (resolved == 0) {
            return null;
        }
        try {
            return appointments.getObjectById(resolved);
        } catch (final SQLException e) {
        	throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        }
    }

    @Override
    public Appointment reloadAppointment(final Appointment appointment, final Session session) throws OXException {
        try {
            return factory.createAppointmentSql(session).getObjectById(appointment.getObjectID(), appointment.getParentFolderID());
        } catch (final SQLException e) {
        	throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
        }
    }

	@Override
    public Appointment loadAppointment(final Appointment appointment, final Session session) throws OXException {
	       try {
	        	if (appointment.getObjectID() <= 0) {
	                final AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
	            	appointment.setObjectID(appointments.resolveUid(appointment.getUid()));
	        	}
	            return factory.createAppointmentSql(session).getObjectById(appointment.getObjectID());
	        } catch (final SQLException e) {
	        	throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
	        }
	    }


    @Override
    public void createAppointment(final CalendarDataObject appointment, final Session session) throws OXException {
        final Context ctx = contexts.getContext(session.getContextId());
        appointment.setContext(ctx);
        appointment.setIgnoreConflicts(true);
        factory.createAppointmentSql(session).insertAppointmentObject(appointment);
    }

    @Override
    public void updateAppointment(final CalendarDataObject appointment, final Session session, final Date clientLastModified) throws OXException {
        final Context ctx = contexts.getContext(session.getContextId());
        appointment.setContext(ctx);
        appointment.setIgnoreConflicts(true);
        final AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
        boolean checkPermissions = false;
        if (appointment.getParentFolderID() <= 0) {
        	try {
            	int folder = appointments.getFolder(appointment.getObjectID());
            	if (folder != 0) {
            		appointment.setParentFolderID(folder);
            	} else {
            		folder = getPrincipalsFolderId(appointments.getObjectById(appointment.getObjectID()), session);
            		if (folder != 0) {
            			appointment.setParentFolderID(folder);
                		checkPermissions = true;
            		} else {
            			appointment.setParentFolderID(getPrivateCalendarFolderId(session));
            		}
            	}
        	} catch (final OXException x) {
        		// IGNORE
        	} catch (final SQLException e) {
        		// IGNORE
        	}
        }
        appointments.updateAppointmentObject(appointment, appointment.getParentFolderID(), clientLastModified, checkPermissions);
    }

    @Override
    public int getFolderIdForUser(final int appId, final int userId, final int contextId) throws OXException {
    	if (appId <= 0) {
    		return 0;
    	}
    	final Session mockSession = new ITipSession(userId, contextId);
    	final AppointmentSQLInterface appointments = factory.createAppointmentSql(mockSession);
    	return appointments.getFolder(appId);
    }

    private int getPrincipalsFolderId(final CalendarDataObject appointment, final Session session) throws OXException {

    	if (appointment.getPrincipalId() <= 0) {
    		return 0;
    	}
    	return getFolderIdForUser(appointment.getObjectID(), appointment.getPrincipalId(), session.getContextId());
	}

	@Override
    public void changeConfirmationForExternalParticipant(final Appointment appointment, final ConfirmationChange change, final Session session) throws OXException {
        final AppointmentSQLInterface appointments = factory.createAppointmentSql(session);

        appointments.setExternalConfirmation(appointment.getObjectID(), appointment.getParentFolderID(), change.getIdentifier(), change.getNewStatus(), change.getNewMessage());
    }

    @Override
    public void deleteAppointment(final Appointment appointment, final Session session, final Date clientLastModified) throws OXException {
        final CalendarDataObject toDelete = new CalendarDataObject();
        toDelete.setObjectID(appointment.getObjectID());
        toDelete.setParentFolderID(appointment.getParentFolderID());
        if (appointment.containsRecurrencePosition()) {
            toDelete.setRecurrencePosition(appointment.getRecurrencePosition());
        } else if (appointment.containsRecurrenceDatePosition()) {
            toDelete.setRecurrenceDatePosition(new Date(startOfTheDay(appointment.getRecurrenceDatePosition())));
        }
        final Context ctx = contexts.getContext(session.getContextId());
        toDelete.setContext(ctx);
        try {
			factory.createAppointmentSql(session).deleteAppointmentObject(toDelete, toDelete.getParentFolderID(), clientLastModified);
		} catch (final SQLException e) {
        	throw OXCalendarExceptionCodes.SQL_ERROR.create(e);
		}
    }

    private long startOfTheDay(final Date recurrenceDatePosition) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(recurrenceDatePosition);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public int getPrivateCalendarFolderId(final Session session) throws OXException {
        final Context ctx = contexts.getContext(session.getContextId());
        final OXFolderAccess acc = new OXFolderAccess(ctx);
        return acc.getDefaultFolderID(session.getUserId(), FolderObject.CALENDAR);
    }





}
