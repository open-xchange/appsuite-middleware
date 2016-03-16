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

package com.openexchange.ajax.appointment;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.UpdatesRequest;
import com.openexchange.ajax.appointment.action.AppointmentUpdatesResponse;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;


/**
 * US 1601 on server side (TA1698)
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class SharedFoldersShowOwnersPrivateAppointmentsAsBlocks extends ManagedAppointmentTest {

    /**
     *
     */
    private static final int[] COLUMNS = new int[]{Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.TITLE, Appointment.START_DATE, Appointment.END_DATE};
    private AJAXClient client1;
    private AJAXClient client2;
    private FolderObject sharedFolder;
    private Date start;
    private Date end;
    private Date startRange;
    private Date endRange;
    private String privateAppointmentTitle;
    private String publicAppointmentTitle;
    private int privateAppointmentID;
    private int publicAppointmentID;
    private Appointment privateAppointment;
    private Appointment publicAppointment;

    public SharedFoldersShowOwnersPrivateAppointmentsAsBlocks(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        User user2 = AJAXClient.User.User2;
        client2 = new AJAXClient(user2);
        UserValues values = client1.getValues();
        int module = Module.CALENDAR.getFolderConstant();
        sharedFolder = folderManager.generateSharedFolder("us1601_shared_"+(new Date().getTime()), module, values.getPrivateAppointmentFolder(), new int[]{values.getUserId(), client2.getValues().getUserId()});
        folderManager.insertFolderOnServer(sharedFolder);

        start = D("13.01.2010 07:00");
        end = D("13.01.2010 07:30");
        startRange = D("12.01.2010 07:00");
        endRange = D("14.01.2010 07:30");

        privateAppointmentTitle = "This should be private";
        privateAppointment = new Appointment();
        privateAppointment.setParentFolderID(sharedFolder.getObjectID());
        privateAppointment.setTitle(privateAppointmentTitle);
        privateAppointment.setStartDate(start);
        privateAppointment.setEndDate(end);
        privateAppointment.setPrivateFlag(true);
        calendarManager.insert(privateAppointment);
        privateAppointmentID = privateAppointment.getObjectID();

        publicAppointmentTitle = "This should be public";
        publicAppointment = new Appointment();
        publicAppointment.setParentFolderID(sharedFolder.getObjectID());
        publicAppointment.setTitle(publicAppointmentTitle);
        publicAppointment.setStartDate(start);
        publicAppointment.setEndDate(end);
        publicAppointment.setPrivateFlag(false);
        calendarManager.insert(publicAppointment);
        publicAppointmentID = publicAppointment.getObjectID();
    }

    public void testShouldFindABlockForAPrivateAppointmentViaAll() throws Exception{
        CommonAllResponse response = client2.execute(new AllRequest(sharedFolder.getObjectID(), COLUMNS, startRange, endRange, TimeZone.getDefault(), true, true));
        int namePos = response.getColumnPos(Appointment.TITLE);
        Object[][] objects = response.getArray();
        assertEquals("Should find two elements, a private and a public one", 2, objects.length);

        Object[] app1 = objects[0];
        Object[] app2 = objects[1];

        assertTrue("One of the two should be the public one", app1[namePos].equals(publicAppointmentTitle) || app2[namePos].equals(publicAppointmentTitle));
        assertFalse("None of the two should be the private title", app1[namePos].equals(privateAppointmentTitle) || app2[namePos].equals(privateAppointmentTitle));
    }

    public void testShouldFindABlockForAPrivateAppointmentViaList() throws Exception{
        CommonListResponse response = client2.execute(new ListRequest(ListIDs.l(new int[]{sharedFolder.getObjectID(),publicAppointmentID},new int[]{sharedFolder.getObjectID(),privateAppointmentID}), COLUMNS, true));
        int namePos = response.getColumnPos(Appointment.TITLE);
        Object[][] objects = response.getArray();
        assertEquals("Should find two elements, a private and a public one", 2, objects.length);

        Object[] app1 = objects[0];
        Object[] app2 = objects[1];

        assertTrue("One of the two should be the public one", app1[namePos].equals(publicAppointmentTitle) || app2[namePos].equals(publicAppointmentTitle));
        assertFalse("None of the two should be the private title", app1[namePos].equals(privateAppointmentTitle) || app2[namePos].equals(privateAppointmentTitle));
    }

    public void testShouldFindABlockForAPrivateAppointmentViaUpdates() throws Exception{
        AppointmentUpdatesResponse response = client2.execute(new UpdatesRequest(sharedFolder.getObjectID(),COLUMNS,new Date(privateAppointment.getLastModified().getTime() - 1),true, true));
        int namePos = response.getColumnPos(Appointment.TITLE);
        Object[][] objects = response.getArray();
        assertEquals("Should find two elements, a private and a public one", 2, objects.length);

        Object[] app1 = objects[0];
        Object[] app2 = objects[1];

        assertTrue("One of the two should be the public one", app1[namePos].equals(publicAppointmentTitle) || app2[namePos].equals(publicAppointmentTitle));
        assertFalse("None of the two should be the private title", app1[namePos].equals(privateAppointmentTitle) || app2[namePos].equals(privateAppointmentTitle));
    }

    public void testShouldNotAnonymizeOwnPrivateAppointments() throws OXException, IOException, SAXException, JSONException{
        CommonListResponse response = client1.execute(new ListRequest(ListIDs.l(new int[]{sharedFolder.getObjectID(),publicAppointmentID},new int[]{sharedFolder.getObjectID(),privateAppointmentID}), COLUMNS, true));
        int namePos = response.getColumnPos(Appointment.TITLE);
        Object[][] objects = response.getArray();
        assertEquals("Should find two elements, a private and a public one", 2, objects.length);

        Object[] app1 = objects[0];
        Object[] app2 = objects[1];

        assertTrue("One of the two should be the public one", app1[namePos].equals(publicAppointmentTitle) || app2[namePos].equals(publicAppointmentTitle));
        assertTrue("One should be the _unchanged_ private one", app1[namePos].equals(privateAppointmentTitle) || app2[namePos].equals(privateAppointmentTitle));
    }


    public void testShouldNotAllowToGetFullPrivateAppointmentsForNonOwner() throws Exception, Exception, Exception, Exception{ //this is actually a bug that has been around for some time
        GetResponse response = client2.execute(new GetRequest(privateAppointment));
        Appointment expected = response.getAppointment(timeZone);
        assertFalse("Title should be anonymized" , privateAppointmentTitle.equals(expected.getTitle()));
    }

    public void testShouldStillAllowToGetFullPrivateAppointmentsForOwner() throws Exception, Exception, Exception, Exception{ //this is actually a bug that has been around for some time
        GetResponse response = client1.execute(new GetRequest(privateAppointment));
        Appointment expected = response.getAppointment(timeZone);
        assertTrue("Title should not be anonymized" , privateAppointmentTitle.equals(expected.getTitle()));
    }


    public void testShouldShowRecurrences() throws Exception{
    }

}
