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
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;


/**
 * {@link Bug53073Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.4
 */
public class Bug53073Test extends AbstractAJAXSession {
    

    private AJAXClient client;
    private AJAXClient client2;
    private CalendarTestManager catm2;
    private FolderTestManager ftm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        client = getClient();
        client2 = getClient2();
        catm2 = new CalendarTestManager(client2);
        ftm2 = new FolderTestManager(client2);
    }

    @Test
    public void testBug53073() throws Exception {
        FolderObject sharedFolder = ftm.generatePrivateFolder("Bug 53073 Shared Folder " + System.currentTimeMillis(), FolderObject.CALENDAR, client.getValues().getPrivateAppointmentFolder(), client.getValues().getUserId());
        OCLPermission permissions = new OCLPermission();
        permissions.setEntity(client2.getValues().getUserId());
        permissions.setGroupPermission(false);
        permissions.setFolderAdmin(false);
        permissions.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        sharedFolder.getPermissions().add(permissions);
        ftm.insertFolderOnServer(sharedFolder);
        
        FolderObject privateFolder = ftm2.generatePrivateFolder("Bug 53073 Private Folder " + System.currentTimeMillis(), FolderObject.CALENDAR, client2.getValues().getPrivateAppointmentFolder(), client2.getValues().getUserId());
        ftm2.insertFolderOnServer(privateFolder);
        
        Appointment app = new Appointment();
        app.setTitle("Bug53073");
        app.setStartDate(D("01.05.2017 08:00"));
        app.setEndDate(D("01.05.2017 08:00"));
        app.setPrivateFlag(true);
        app.setIgnoreConflicts(true);
        app.setParentFolderID(sharedFolder.getObjectID());
        catm.insert(app);
        
        Appointment update = catm2.createIdentifyingCopy(app);
        update.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        System.out.println("Shared: " + sharedFolder.getObjectID() + ", " + privateFolder.getObjectID() + " -> " + client2.getValues().getPrivateAppointmentFolder());
        //Appointment llll = catm2.get(sharedFolder.getObjectID(), app.getObjectID());
        catm2.update(privateFolder.getObjectID(), update);
        assertTrue("Expected error.", catm2.getLastResponse().hasError());
        assertTrue("Wrong exception", catm2.getLastResponse().getException().similarTo(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.create()));
        System.out.println(catm2.getLastException().getMessage());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            catm2.cleanUp();
            ftm2.cleanUp();
        } finally {
            super.tearDown();
        }
    }
}
