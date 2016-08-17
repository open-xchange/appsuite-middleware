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

import java.util.Date;
import java.util.Iterator;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * {@link Bug38079Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.3
 */
public class Bug48149Test extends AbstractAJAXSession {

    private AJAXClient client2;
    private AJAXClient client3;
    private CalendarTestManager ctm;
    private CalendarTestManager ctm2;
    private CalendarTestManager ctm3;
    private FolderTestManager ftm1;
    private FolderObject sharedFolder1;
    private FolderTestManager ftm2;
    private Appointment app1;
    private Appointment app2;

    public Bug48149Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
        client3 = new AJAXClient(User.User3);
        ctm = new CalendarTestManager(client);
        ctm2 = new CalendarTestManager(client2);
        ctm3 = new CalendarTestManager(client3);
        ftm1 = new FolderTestManager(client);
        ftm2 = new FolderTestManager(client2);
        
        // Remove all permissions
        FolderObject privateFolder1 = ftm1.getFolderFromServer(client.getValues().getPrivateAppointmentFolder());
        Iterator<OCLPermission> i = privateFolder1.getPermissions().iterator();
        while (i.hasNext()) {
            OCLPermission permission = i.next();
            if (permission.getEntity() != client.getValues().getUserId()) {
                i.remove();
            }
        }
        privateFolder1.setLastModified(new Date(Long.MAX_VALUE));
        ftm1.updateFolderOnServer(privateFolder1);

        FolderObject privateFolder2 = ftm2.getFolderFromServer(client2.getValues().getPrivateAppointmentFolder());
        i = privateFolder2.getPermissions().iterator();
        while (i.hasNext()) {
            OCLPermission permission = i.next();
            if (permission.getEntity() != client2.getValues().getUserId()) {
                i.remove();
            }
        }
        privateFolder2.setLastModified(new Date(Long.MAX_VALUE));
        ftm2.updateFolderOnServer(privateFolder1);
        
        // Add new shared folder.
        sharedFolder1 = ftm1.generateSharedFolder("Shared Folder"+System.currentTimeMillis(), FolderObject.CALENDAR, client.getValues().getPrivateAppointmentFolder(), client.getValues().getUserId(), client3.getValues().getUserId());
        ftm1.insertFolderOnServer(sharedFolder1);
        
        // Appointments not visible for user 3.
        app1 = new Appointment();
        app1.setTitle("app1");
        app1.setStartDate(TimeTools.D("07.08.2016 08:00"));
        app1.setEndDate(TimeTools.D("07.08.2016 09:00"));
        app1.setIgnoreConflicts(true);
        app1.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        ctm.insert(app1);
        
        app2 = new Appointment();
        app2.setTitle("app1");
        app2.setStartDate(TimeTools.D("07.08.2016 08:00"));
        app2.setEndDate(TimeTools.D("07.08.2016 09:00"));
        app2.setIgnoreConflicts(true);
        app2.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        ctm.insert(app2);
    }

    public void testLoadAppointmentFromUserWithShared() throws Exception {
        ctm3.get(sharedFolder1.getObjectID(), app1.getObjectID());
    }

    public void testLoadAppointmentFromUserWithoutAnyShares() throws Exception {
        try {
            ctm3.get(sharedFolder1.getObjectID(), app2.getObjectID());
        } catch (Exception e) {
            // ignore
        }
        assertTrue("Expected error.", ctm3.getLastResponse().hasError());
        assertTrue("Excpected something with permissions...", ctm3.getLastResponse().getErrorMessage().contains("ermission"));
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        ctm2.cleanUp();
        ctm3.cleanUp();
        ftm1.cleanUp();
        ftm2.cleanUp();
        super.tearDown();
    }

}
