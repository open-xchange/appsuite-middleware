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
package com.openexchange.groupware.calendar.tools;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;

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

    public CalendarDataObject buildRecurringAppointment() {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("recurring");
        cdao.setParentFolderID(privateFolder);
        cdao.setIgnoreConflicts(true);
        //CalendarTest.fillDatesInDao(cdao);
        cdao.setStartDate(new Date(100));
        cdao.setEndDate(new Date(36000100));
        cdao.setRecurrenceType(CalendarObject.MONTHLY);
        cdao.setRecurrenceCount(5);
        cdao.setDayInMonth(3);
        cdao.setInterval(2);
        cdao.setDays(CalendarObject.TUESDAY);
        
        cdao.setContext(ctx);
        return cdao;
    }

    public void removeAll(final String user, final List<CalendarDataObject> clean) throws SQLException, OXException {
        switchUser( user );
        for(final CalendarDataObject cdao : clean) {
            calendar.deleteAppointmentObject(cdao,privateFolder,new Date(Long.MAX_VALUE));
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

        final long FIVE_DAYS = 5l*24l*3600000l;
        final long THREE_HOURS = 3l*3600000l;

        cdao.setStartDate(new Date(FUTURE + FIVE_DAYS));
        cdao.setEndDate(new Date(FUTURE + FIVE_DAYS + THREE_HOURS));
        cdao.setContext(ctx);

        final CalendarContextToolkit tools = new CalendarContextToolkit();

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
        final CalendarContextToolkit tools = new CalendarContextToolkit();
        final int userId = tools.resolveUser(user,ctx);
        privateFolder = new CalendarFolderToolkit().getStandardFolder(userId, ctx);
        session = tools.getSessionForUser(user, ctx);
        calendar = new CalendarSql(session);
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

    public CalendarDataObject reload(final CalendarDataObject which) throws SQLException, OXException {
        return calendar.getObjectById(which.getObjectID(), which.getParentFolderID());
    }

    public Session getSession() {
        return session;
    }

    public void deleteAll(final Context ctx) throws SQLException, DBPoolingException {
        Statement stmt = null;
        Connection writeCon = null;
        try {
            writeCon = DBPool.pickupWriteable(ctx);
            stmt = writeCon.createStatement();
            for(final String tablename : new String[]{"prg_dates", "prg_dates_members", "prg_date_rights"}) {
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
}
