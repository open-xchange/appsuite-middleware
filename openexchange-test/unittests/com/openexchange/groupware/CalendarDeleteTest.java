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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import com.openexchange.calendar.CalendarAdministration;
import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.test.AjaxInit;
import junit.framework.TestCase;


public class CalendarDeleteTest extends TestCase {

    private static int contextid = 1;
    private static int userid = 11;
    private static int groupid = 62;
    private static int resourceid = 2;
    private static int deleteuserid = 11;
    private Context context;

    private static boolean init = false;

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();

        final TestConfig config = new TestConfig();
        contextid = ContextStorage.getInstance().getContextId(config.getContextName());

        final EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        context = new ContextImpl(contextid);
        userid = CalendarTest.getUserId();
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        deleteuserid = resolveUser(user2);

        try {
            final CalendarTest ct = new CalendarTest();
            CalendarTest.dontDelete();
            ct.setUp();
            ct.testWholeDayWithDB();
            ct.testMultiSpanWholeDay();
            ct.testInsertAndLabel();
            ct.testNoAlarm();
            ct.testInsertAndAlarm();
            //ct.testInsertMoveAndDeleteAppointments();
            ct.testConfirmation();
            ct.testInsertUpdateAlarm();
            ct.testAlarmAndUpdate();

            final AppointmentBugTests abt = new AppointmentBugTests();
            abt.testBug4467();
            //abt.testBug4497();
            abt.testBug4276();
            abt.testBug4766();
            abt.testBug5010();
            abt.testBug5012();
            //abt.testBug5144();

        } catch (final Throwable ex) {
            throw new Exception(ex.getMessage(), ex);
        } finally {
            CalendarTest.doDelete();
        }

    }

    private Properties getAJAXProperties() {
        final Properties properties = AjaxInit.getAJAXProperties();
        return properties;
    }

    private int resolveUser(final String user) throws Exception {
        final UserStorage uStorage = UserStorage.getInstance();
        return uStorage.getUserId(user, context);
    }

    @Override
	protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }

    public void testDeleteUserData() throws Throwable {
        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);
        final SessionObject so = SessionObjectWrapper.createSessionObject(deleteuserid, context.getContextId(), "deleteAllUserApps");

        final DeleteEvent delEvent = new DeleteEvent(this, so.getUserId(), DeleteEvent.TYPE_USER, ContextStorage.getInstance().getContext(so.getContextId()));

        final CalendarAdministration ca = new CalendarAdministration();
        ca.deletePerformed(delEvent, readcon, writecon);

        final Statement stmt = readcon.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_dates_members pdm WHERE pd.intfield01 = pdm.object_id AND pdm.member_uid = "+deleteuserid+" AND pd.cid = "+contextid);
        assertTrue("Test that no appointment exists for user "+deleteuserid, rs.next() != true);
        rs.close();

        final ResultSet rs2 = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd WHERE pd.created_from = "+deleteuserid+" AND pd.cid = "+contextid);
        assertTrue("Test that no cerated_from exists for user "+deleteuserid, rs2.next() != true);
        rs2.close();

        final ResultSet rs3 = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd WHERE pd.changed_from = "+deleteuserid+" AND pd.cid = "+contextid);
        assertTrue("Test that no changed_from exists for user "+deleteuserid, rs3.next() != true);
        rs3.close();

        final ResultSet rs4 = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_date_rights pdr WHERE pd.intfield01 = pdr.object_id AND pdr.id = "+deleteuserid+" AND pdr.type = "+Participant.USER+" AND pd.cid = "+contextid);
        assertTrue("Test that no user_right entry exists for user "+deleteuserid, rs4.next() != true);
        rs4.close();

        stmt.close();

        DBPool.push(context, readcon);
        DBPool.pushWrite(context, writecon);
    }

     public void testDeleteGroup() throws Throwable {
        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);
        final SessionObject so = SessionObjectWrapper.createSessionObject(deleteuserid, context.getContextId(), "deleteAllUserApps");

        final DeleteEvent delEvent = new DeleteEvent(this, groupid, DeleteEvent.TYPE_GROUP, ContextStorage.getInstance().getContext(so.getContextId()));

        final CalendarAdministration ca = new CalendarAdministration();
        ca.deletePerformed(delEvent, readcon, writecon);

        final Statement stmt = readcon.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_date_rights pdr WHERE pd.intfield01 = pdr.object_id AND pdr.id = "+groupid+" AND pdr.type = "+Participant.GROUP+" AND pd.cid = "+contextid);
        assertTrue("Test that no user_right entry exists for group "+deleteuserid, rs.next() != true);
        rs.close();

        stmt.close();

        DBPool.push(context, readcon);
        DBPool.pushWrite(context, writecon);
    }

    public void testDeleteResource() throws Throwable {
        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);
        final SessionObject so = SessionObjectWrapper.createSessionObject(deleteuserid, context.getContextId(), "deleteAllUserApps");

        final DeleteEvent delEvent = new DeleteEvent(this, resourceid, DeleteEvent.TYPE_RESOURCE, ContextStorage.getInstance().getContext(so.getContextId()));

        final CalendarAdministration ca = new CalendarAdministration();
        ca.deletePerformed(delEvent, readcon, writecon);

        final Statement stmt = readcon.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_date_rights pdr WHERE pd.intfield01 = pdr.object_id AND pdr.id = "+resourceid+" AND pdr.type = "+Participant.RESOURCE+" AND pd.cid = "+contextid);
        assertTrue("Test that no user_right entry exists for resource "+deleteuserid, rs.next() != true);
        rs.close();

        stmt.close();

        DBPool.push(context, readcon);
        DBPool.pushWrite(context, writecon);
    }


}
