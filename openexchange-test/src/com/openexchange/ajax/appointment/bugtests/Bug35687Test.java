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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import java.util.List;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * {@link Bug35687Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug35687Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private FolderTestManager ftm;
    private AJAXClient client2;
    private FolderObject folder;
    private Appointment app;

    public Bug35687Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client2 = new AJAXClient(User.User2);

        ctm = new CalendarTestManager(client);
        ftm = new FolderTestManager(client);
        folder = ftm.generateSharedFolder("Bug35687Folder" + System.currentTimeMillis(), FolderObject.CALENDAR, client.getValues().getPrivateAppointmentFolder(), client.getValues().getUserId(), client2.getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);

        ctm.setClient(client2);

        int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        app = new Appointment();
        app.setTitle("Bug 35687 Test");
        app.setStartDate(D("16.12." + nextYear + " 08:00"));
        app.setEndDate(D("16.12." + nextYear + " 09:00"));
        app.setParentFolderID(folder.getObjectID());
        app.setAlarm(15);
        app.setIgnoreConflicts(true);

        app = ctm.insert(app);
        System.out.println("hello");
    }

    public void testBug35687() throws Exception {
        Appointment loaded = ctm.get(app);
        assertEquals("Wrong alarm value", 15, loaded.getAlarm());

        List<Appointment> listAppointment = ctm.list(new ListIDs(folder.getObjectID(), app.getObjectID()), new int[] { Appointment.ALARM });
        assertTrue("Missing alarm value for list request.", listAppointment.get(0).containsAlarm());
        assertEquals("Wrong alarm value for list request.", 15, listAppointment.get(0).getAlarm());
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        ftm.cleanUp();
        super.tearDown();
    }

}
