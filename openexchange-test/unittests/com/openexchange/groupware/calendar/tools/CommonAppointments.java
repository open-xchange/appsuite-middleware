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

package com.openexchange.groupware.calendar.tools;

import com.openexchange.exception.OXException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestFolderToolkit;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CommonAppointments {

    private CalendarSql calendar = null;
    private int privateFolder;
    private final Context ctx;
    private final long FUTURE = System.currentTimeMillis()+24*3600000;
    private Session session;

    public CommonAppointments(final Context ctx, final String user) {
        this.ctx = ctx;
        switchUser( user );
    }

	/**
	 *
	 * Returns the folder ID of user's private calendar folder
	 *
	 * @return The folder ID of user's private calendar folder
	 */
	public int getPrivateFolder() {
		return privateFolder;
	}

    public CalendarDataObject buildRecurringAppointment() {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("recurring");
        cdao.setParentFolderID(privateFolder);
        cdao.setIgnoreConflicts(true);
        //CalendarTest.fillDatesInDao(cdao);
        cdao.setStartDate(new Date(100));
        cdao.setEndDate(new Date(36000100));
        cdao.setRecurrenceType(CalendarObject.MONTHLY);
        cdao.setOccurrence(5);
        cdao.setDayInMonth(3);
        cdao.setInterval(2);
        cdao.setDays(CalendarObject.TUESDAY);

        cdao.setContext(ctx);
        return cdao;
    }

    public void copyRecurringInformation(CalendarDataObject source, CalendarDataObject target) {
        Set<Integer> recurrenceFields = new HashSet<Integer>() {

            {
                add(CalendarObject.RECURRENCE_TYPE);
                add(CalendarObject.INTERVAL);
                add(CalendarObject.DAYS);
                add(CalendarObject.DAY_IN_MONTH);
                add(CalendarObject.MONTH);
                add(CalendarObject.RECURRENCE_COUNT);
                add(CalendarObject.UNTIL);
            }
        };

        for (int recurrenceField : recurrenceFields) {
            if (source.contains(recurrenceField)) {
                if (recurrenceField == CalendarObject.UNTIL && target.contains(CalendarObject.RECURRENCE_COUNT)) {
                    continue;
                }
                if (recurrenceField == CalendarObject.RECURRENCE_COUNT && target.contains(CalendarObject.UNTIL)) {
                    continue;
                }
                target.set(recurrenceField, source.get(recurrenceField));
            }
        }
    }

    public CalendarDataObject buildBasicAppointment(final Date start, final Date end) {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("basic");
        cdao.setParentFolderID(privateFolder);
        cdao.setIgnoreConflicts(true);
        //CalendarTest.fillDatesInDao(cdao);
        cdao.setStartDate(start);
        cdao.setEndDate(end);
        cdao.setContext(ctx);
        cdao.setTimezone("utc");
        return cdao;
    }

    public void removeAll(final String user, final List<CalendarDataObject> clean) throws SQLException, OXException {
        switchUser( user );
        for(final CalendarDataObject cdao : clean) {
            try {
                calendar.deleteAppointmentObject(cdao,cdao.getParentFolderID(),new Date(Long.MAX_VALUE));
            } catch (final Throwable t) {
                t.printStackTrace(); // Drastic but apparently neccessary...
            }
        }
    }

    public CalendarDataObject buildAppointmentWithUserParticipants(final String...usernames) {
        return buildAppointmentWithParticipants(usernames, new String[0], new String[0]);
    }

    public CalendarDataObject buildAppointmentWithResourceParticipants(final String...resources) {
        return buildAppointmentWithParticipants(new String[0], resources, new String[0]);
    }

    public CalendarDataObject buildAppointmentWithGroupParticipants(final String...groups) {
        return buildAppointmentWithParticipants(new String[0], new String[0], groups);
    }

    public CalendarDataObject buildAppointmentWithParticipants(final String[] users, final String[] resources, final String[] groups) {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("with participants");
        cdao.setParentFolderID(privateFolder);
        cdao.setIgnoreConflicts(true);

        final long FIVE_DAYS = 5L*24L*3600000L;
        final long THREE_HOURS = 3L*3600000L;

        cdao.setStartDate(new Date(FUTURE + FIVE_DAYS));
        cdao.setEndDate(new Date(FUTURE + FIVE_DAYS + THREE_HOURS));
        cdao.setContext(ctx);

        final TestContextToolkit tools = new TestContextToolkit();

        final List<Participant> participants = new ArrayList<Participant>(users.length+resources.length+groups.length);
        final List<UserParticipant> userParticipants = tools.users(ctx, users);
        participants.addAll(userParticipants);
        participants.addAll( tools.resources(ctx, resources) );
        participants.addAll( tools.groups(ctx, groups) );

        cdao.setParticipants(participants);
        cdao.setUsers(userParticipants);
        cdao.setContainsResources(resources.length > 0);

        return cdao;
    }

    public void switchUser(final String user) {
        final TestContextToolkit tools = new TestContextToolkit();
        final int userId = tools.resolveUser(user,ctx);
        privateFolder = new TestFolderToolkit().getStandardFolder(userId, ctx);
        session = tools.getSessionForUser(user, ctx);
        calendar = new CalendarSql(session);
    }

    public CalendarDataObject[] move(final CalendarDataObject cdao, int sourceFolderId) throws OXException {
        CalendarDataObject[] conflicts = null;
        if(cdao.containsObjectID()) {
            conflicts = calendar.updateAppointmentObject(cdao, sourceFolderId, new Date(Long.MAX_VALUE));
        } else {
            conflicts = calendar.insertAppointmentObject(cdao);
        }
        if(conflicts == null) {
            return null;
        }
        for (final CalendarDataObject conflict : conflicts) {
            conflict.setContext(cdao.getContext());
        }
        return conflicts;
    }

    public CalendarDataObject[] save(final CalendarDataObject cdao) throws OXException {
        CalendarDataObject[] conflicts = null;
        if(cdao.containsObjectID()) {
            conflicts = calendar.updateAppointmentObject(cdao, cdao.getParentFolderID(), new Date(Long.MAX_VALUE));
        } else {
            conflicts = calendar.insertAppointmentObject(cdao);
        }
        if(conflicts == null) {
            return null;
        }
        for (final CalendarDataObject conflict : conflicts) {
            conflict.setContext(cdao.getContext());
        }
        return conflicts;
    }

    public void delete(final CalendarDataObject cdao) throws OXException, SQLException {
        calendar.deleteAppointmentObject(cdao, cdao.getParentFolderID(), new Date(Long.MAX_VALUE));
    }

    public CalendarDataObject reload(final CalendarDataObject which) throws SQLException, OXException {
        return calendar.getObjectById(which.getObjectID(), which.getParentFolderID());
    }

    public Session getSession() {
        return session;
    }

    public void deleteAll(final Context ctx) throws SQLException, OXException {
        Statement stmt = null;
        Connection writeCon = null;
        try {
            writeCon = DBPool.pickupWriteable(ctx);
            stmt = writeCon.createStatement();
            for(final String tablename : new String[]{"dateExternal", "prg_dates", "prg_dates_members", "prg_date_rights"}) {
                stmt.executeUpdate("DELETE FROM "+tablename+" WHERE cid = "+ctx.getContextId());
            }
        } finally {
            if(stmt != null) {
                stmt.close();
            }
            if(writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }

    }

    public List<Appointment> getPrivateAppointments() throws OXException {
       return getAppointmentsInFolder(privateFolder);

    }

    public List<Appointment> getAppointmentsInFolder(final int folderId) throws OXException {
        return getAppointmentsInFolder(folderId, new int[]{CalendarDataObject.OBJECT_ID});
    }
    
    public CalendarDataObject getObjectById(int objectId, int folderId) throws OXException, SQLException {
        return calendar.getObjectById(objectId, folderId);
    }

    public List<Appointment> getAppointmentsInFolder(final int folderId, int[] columns) throws OXException {
        final List<Appointment> cdao = new ArrayList<Appointment>();
        try {
            final SearchIterator<Appointment> iterator = calendar.getAppointmentsBetweenInFolder(folderId, columns, new Date(0), new Date(Long.MAX_VALUE), CalendarDataObject.OBJECT_ID, Order.ASCENDING);
            while(iterator.hasNext()) {
                cdao.add(iterator.next());
            }
            return cdao;
        } catch (final OXException e) {
            throw e;
        } catch (final SQLException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    public List<Appointment> getModifiedInFolder(final int folderId, final long since) throws OXException {
        final List<Appointment> cdao = new ArrayList<Appointment>();
        try {

            final SearchIterator<Appointment> iterator = calendar.getModifiedAppointmentsInFolder(folderId, new Date(0), new Date(Long.MAX_VALUE),new int[]{CalendarDataObject.OBJECT_ID}, new Date(since));
            while(iterator.hasNext()) {
                cdao.add(iterator.next());
            }
            return cdao;
        } catch (final OXException e) {
            if (e.getPrefix().equals("APP")) {
                throw e;
            }
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    public List<Appointment> getDeletedInFolder(final int folderId, final long since) throws OXException {
        final List<Appointment> cdao = new ArrayList<Appointment>();
        try {

            final SearchIterator<Appointment> iterator = calendar.getDeletedAppointmentsInFolder(folderId, new int[]{CalendarDataObject.OBJECT_ID}, new Date(since));
            while(iterator.hasNext()) {
                cdao.add(iterator.next());
            }
            return cdao;
        } catch (final OXException e) {
            if (e.getPrefix().equals("APP")) {
                throw e;
            }
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    public static Date D(final String date) {
        return TimeTools.D(date);
    }

    public static Date recalculate(final Date date, final TimeZone from, final TimeZone to) {
        final Calendar fromCal = new GregorianCalendar();
        fromCal.setTimeZone(from);
        fromCal.setTime(date);

        final Calendar toCal = new GregorianCalendar();
        toCal.setTimeZone(to);
        toCal.set(fromCal.get(Calendar.YEAR), fromCal.get(Calendar.MONTH), fromCal.get(Calendar.DATE), fromCal.get(Calendar.HOUR_OF_DAY), fromCal.get(Calendar.MINUTE), fromCal.get(Calendar.SECOND));
        toCal.set(Calendar.MILLISECOND, 0);

        return toCal.getTime();
    }

    public static String dateString(final long time, final TimeZone tz) {
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        format.setTimeZone(tz);
        return format.format(new Date(time));
    }

    public CalendarDataObject createIdentifyingCopy(final CalendarDataObject appointment) {
        final CalendarDataObject copy = new CalendarDataObject();
        copy.setObjectID(appointment.getObjectID());
        copy.setContext(appointment.getContext());
        copy.setParentFolderID(appointment.getParentFolderID());
        return copy;
    }

    public AppointmentSQLInterface getCurrentAppointmentSQLInterface() {
        return calendar;
    }

    public CalendarDataObject load(final int objectId, final int inFolder) throws OXException, SQLException {
        return calendar.getObjectById(objectId, inFolder);
    }

    public void confirm(int objectId, int userId, int status, String message) throws OXException {
        calendar.setUserConfirmation(objectId, getPrivateFolder(), userId, status, message);
    }
}
