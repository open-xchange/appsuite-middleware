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

package com.openexchange.groupware;

import java.sql.Connection;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.iterator.SearchIterator;


public class AppointmentDeleteNoCommit extends TestCase {

    int cols[] = new int[] { Appointment.START_DATE, Appointment.END_DATE, Appointment.TITLE, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.USERS, Appointment.FULL_TIME };
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    public static final String TIMEZONE = "Europe/Berlin";
 // Override these in setup
    private static int userid = 11; // bishoph
    public final static int contextid = 1;

    private static boolean init = false;

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        final EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        AppointmentDeleteNoCommit.userid = getUserId();
        ContextStorage.start();
    }

    @Override
	protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }

    private static Properties getAJAXProperties() {
        final Properties properties = AjaxInit.getAJAXProperties();
        return properties;
    }

    private static int resolveUser(final String u) throws Exception {
        final UserStorage uStorage = UserStorage.getInstance();
        return uStorage.getUserId(u, getContext());
    }

    public static int getUserId() throws Exception {
        if (!init) {
            Init.startServer();
            init = true;
        }
        final String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        return resolveUser(user);
    }

    public static Context getContext() {
        return new ContextImpl(contextid);
    }

    void deleteAllAppointments() throws Exception  {
        final Connection readcon = DBPool.pickup(getContext());
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "deleteAllApps");
        final CalendarSql csql = new CalendarSql(so);
        final SearchIterator<Appointment> si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        while (si.hasNext()) {
            final Appointment cdao = si.next();
            CalendarTest.testDelete(cdao);
        }
        si.close();
        DBPool.push(context, readcon);
    }

    public static int getPrivateFolder(final int userid) throws Exception {
        int privatefolder = 0;
        final Context context = getContext();
        final Connection readcon = DBPool.pickup(context);
        privatefolder = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        DBPool.push(context, readcon);
        return privatefolder;
    }

    /*
     when i open a multi participant appt and add one resource to the appt, the
     following error is thrown:
    */
    public void testDeleteAll() throws Exception {

        // Clean up appointments
        deleteAllAppointments();

    }

}
