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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.api2.OXException;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link CalendarITipIntegrationUtility}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarITipIntegrationUtility implements ITipIntegrationUtility {

    private AppointmentSqlFactoryService factory;

    private CalendarCollectionService calendarCollection;

    private ContextService contexts;

    public CalendarITipIntegrationUtility(AppointmentSqlFactory factory, CalendarCollectionService calendarCollection, ContextService contexts) {
        this.factory = factory;
        this.contexts = contexts;
        this.calendarCollection = calendarCollection;
    }

    // TODO: Find a better way
    public List<Appointment> getConflicts(CalendarDataObject appointment, Session session) throws AbstractOXException {
    	if (appointment == null) {
    		return Collections.emptyList();
    	}
        AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
        List<Appointment> conflicts = new ArrayList<Appointment>();
        boolean checkedUser = false;
        if (appointment.getParticipants() != null) {
            for (Participant participant : appointment.getParticipants()) {
                if (!UserParticipant.class.isInstance(participant)) {
                    continue;
                }
                UserParticipant userParticipant = (UserParticipant) participant;
                checkedUser = checkedUser || userParticipant.getIdentifier() == session.getUserId();
                SearchIterator<Appointment> freeBusyInformation = appointments.getFreeBusyInformation(
                    userParticipant.getIdentifier(),
                    Participant.USER,
                    appointment.getStartDate(),
                    appointment.getEndDate());
                while (freeBusyInformation.hasNext()) {
                    Appointment next = freeBusyInformation.next();
                    next.setParticipants(new Participant[] { userParticipant });
                    conflicts.add(next);
                }

            }
        }
        if (!checkedUser) {
            SearchIterator<Appointment> freeBusyInformation = appointments.getFreeBusyInformation(
                session.getUserId(),
                Participant.USER,
                appointment.getStartDate(),
                appointment.getEndDate());
            while (freeBusyInformation.hasNext()) {
                Appointment next = freeBusyInformation.next();
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
        Appointment.NUMBER_OF_LINKS,
        Appointment.LAST_MODIFIED_UTC));

    private static final int[] EXCEPTION_FIELDS = new int[Appointment.ALL_COLUMNS.length - EXEMPT.size()];

    static {
        int i = 0;
        for (int col : Appointment.ALL_COLUMNS) {
            if (!EXEMPT.contains(col)) { // FIXME: Broken fields, fix underlying calendar
                EXCEPTION_FIELDS[i++] = col;
            }
        }
    }

    public List<Appointment> getExceptions(Appointment original, Session session) throws AbstractOXException {
        CalendarDataObject[] changeExceptionsByRecurrence = calendarCollection.getChangeExceptionsByRecurrence(
            original.getObjectID(),
            EXCEPTION_FIELDS,
            session);
        List<Appointment> appointments = new ArrayList<Appointment>(changeExceptionsByRecurrence.length);
        for (CalendarDataObject calendarDataObject : changeExceptionsByRecurrence) {
            appointments.add(calendarDataObject);
        }
        return appointments;
    }

    public CalendarDataObject resolveUid(String uid, Session session) throws AbstractOXException {
        AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
        int resolved = appointments.resolveUid(uid);
        if (resolved == 0) {
            return null;
        }
        try {
            return appointments.getObjectById(resolved);
        } catch (SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.SQL_ERROR, e.getMessage(), e);
        }
    }
    
    public Appointment reloadAppointment(Appointment appointment, Session session) throws AbstractOXException {
        try {
            return factory.createAppointmentSql(session).getObjectById(appointment.getObjectID(), appointment.getParentFolderID());
        } catch (SQLException e) {
            throw new OXCalendarException(OXCalendarException.Code.SQL_ERROR, e);
        }
    }

    public void createAppointment(CalendarDataObject appointment, Session session) throws AbstractOXException {
        Context ctx = contexts.getContext(session.getContextId());
        appointment.setContext(ctx);
        appointment.setIgnoreConflicts(true);
        factory.createAppointmentSql(session).insertAppointmentObject(appointment);
    }

    public void updateAppointment(CalendarDataObject appointment, Session session, Date clientLastModified) throws AbstractOXException {
        Context ctx = contexts.getContext(session.getContextId());
        appointment.setContext(ctx);
        appointment.setIgnoreConflicts(true);
        AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
        boolean checkPermissions = false;
        if (appointment.getParentFolderID() <= 0) {
        	try {
            	int folder = appointments.getFolder(appointment.getObjectID());
            	if (folder != 0) {
            		appointment.setParentFolderID(folder);
            	} else {
            		folder = getPrincipalsFolderId(appointments.getObjectById(appointment.getObjectID()), session);
            		appointment.setParentFolderID(folder);
            		checkPermissions = true;
            	}
        	} catch (AbstractOXException x) {
        		// IGNORE
        	} catch (SQLException e) {
        		// IGNORE
        	}
        }
        appointments.updateAppointmentObject(appointment, appointment.getParentFolderID(), clientLastModified, checkPermissions);
    }
    
    public int getFolderIdForUser(int appId, int userId, int contextId) throws AbstractOXException {
    	if (appId <= 0) {
    		return 0;
    	}
    	Session mockSession = new ITipSession(userId, contextId);
    	AppointmentSQLInterface appointments = factory.createAppointmentSql(mockSession);
    	return appointments.getFolder(appId);
    }
    
    private int getPrincipalsFolderId(CalendarDataObject appointment, Session session) throws AbstractOXException {
    	
    	if (appointment.getPrincipalId() <= 0) {
    		return 0;
    	}
    	return getFolderIdForUser(appointment.getObjectID(), appointment.getPrincipalId(), session.getContextId());
	}

	public void changeConfirmationForExternalParticipant(Appointment appointment, ConfirmationChange change, Session session) throws AbstractOXException {
        AppointmentSQLInterface appointments = factory.createAppointmentSql(session);
        
        appointments.setExternalConfirmation(appointment.getObjectID(), appointment.getParentFolderID(), change.getIdentifier(), change.getNewStatus(), change.getNewMessage());
    }

    public void deleteAppointment(Appointment appointment, Session session, Date clientLastModified) throws AbstractOXException {
        CalendarDataObject toDelete = new CalendarDataObject();
        toDelete.setObjectID(appointment.getObjectID());
        toDelete.setParentFolderID(appointment.getParentFolderID());
        if (appointment.containsRecurrencePosition()) {
            toDelete.setRecurrencePosition(appointment.getRecurrencePosition());
        } else if (appointment.containsRecurrenceDatePosition()) {
            toDelete.setRecurrenceDatePosition(new Date(startOfTheDay(appointment.getRecurrenceDatePosition())));
        }
        Context ctx = contexts.getContext(session.getContextId());
        toDelete.setContext(ctx);
        factory.createAppointmentSql(session).deleteAppointmentObject(toDelete, toDelete.getParentFolderID(), clientLastModified);
    }
    
    private long startOfTheDay(Date recurrenceDatePosition) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(recurrenceDatePosition);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    public int getPrivateCalendarFolderId(Session session) throws AbstractOXException {
        Context ctx = contexts.getContext(session.getContextId());
        final OXFolderAccess acc = new OXFolderAccess(ctx);
        return acc.getDefaultFolder(session.getUserId(), FolderObject.CALENDAR).getObjectID();
    }


}
