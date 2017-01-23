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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link CalendarTestManagerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarTestManagerTest extends AbstractAJAXSession {

    private FolderObject testFolder;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testFolder = ftm.generatePublicFolder("Calendar Manager Tests " + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(testFolder);
    }

    protected Appointment generateAppointment() {
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(testFolder.getObjectID());
        appointment.setTitle(this.getClass().getCanonicalName());
        appointment.setStartDate(new Date());
        appointment.setEndDate(new Date());
        return appointment;
    }

    @Test
    public void testCreate() throws Exception {
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);

        assertNotNull(appointment.getLastModified());
        assertExists(appointment);
    }

    @Test
    public void testRemove() throws Exception {
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);

        assertExists(appointment);

        catm.delete(appointment);

        assertDoesNotExist(appointment);
    }

    @Test
    public void testGet() throws Exception {
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);

        Appointment reload = catm.get(appointment.getParentFolderID(), appointment.getObjectID());

        assertEquals(appointment.getObjectID(), reload.getObjectID());
        assertEquals(appointment.getTitle(), reload.getTitle());

        reload = catm.get(appointment);

        assertEquals(appointment.getObjectID(), reload.getObjectID());
        assertEquals(appointment.getTitle(), reload.getTitle());
    }

    @Test
    public void testUpdate() throws Exception {
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);

        Appointment update = catm.createIdentifyingCopy(appointment);

        assertEquals(update.getObjectID(), appointment.getObjectID());
        assertEquals(update.getParentFolderID(), appointment.getParentFolderID());
        assertEquals(update.getLastModified(), appointment.getLastModified());
        assertNotSame(appointment, update);

        update.setStartDate(new Date(23000));
        update.setEndDate(new Date(25000));
        update.setIgnoreConflicts(true);

        catm.update(update);

        Appointment reload = catm.get(appointment);

        assertEquals(23000, reload.getStartDate().getTime());
        assertEquals(25000, reload.getEndDate().getTime());
    }

    @Test
    public void testUpdates() throws Exception {
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);

        Date beforeUpdate = appointment.getLastModified(); // TODO use global timestamp from ALL request
        Appointment update = catm.createIdentifyingCopy(appointment);
        String updatedTitle = this.getClass().getCanonicalName() + "2";
        update.setTitle(updatedTitle);
        update.setIgnoreConflicts(true);

        assertEquals(update.getObjectID(), appointment.getObjectID());
        assertEquals(update.getParentFolderID(), appointment.getParentFolderID());
        assertEquals(update.getLastModified(), appointment.getLastModified());
        assertNotSame(appointment, update);

        catm.update(update);

        List<Appointment> updates = catm.updates(appointment.getParentFolderID(), beforeUpdate, true);

        assertEquals("Should have one new update", 1, updates.size());
        assertEquals("Should contain the updated title", updatedTitle, updates.get(0).getTitle());
    }

    @Test
    public void testGetAllInFolder() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(testFolder.getObjectID());
        appointment.setTitle(this.getClass().getCanonicalName());
        appointment.setStartDate(D("12/02/1999 10:00"));
        appointment.setEndDate(D("12/02/1999 12:00"));
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);

        Appointment[] appointments = catm.all(appointment.getParentFolderID(), D("01/01/1999 00:00"), D("01/03/1999 00:00"));

        assertNotNull(appointments);
        assertInList(appointments, appointment);
    }

    private void assertInList(Appointment[] appointments, Appointment appointment) {
        for (Appointment appointmentObject : appointments) {
            if (appointmentObject.getObjectID() == appointment.getObjectID()) {
                return;
            }
        }
        fail("Could not find appointment");

    }

    public void assertExists(Appointment appointment) {
        GetRequest get = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID(), false);
        try {
            GetResponse response = getClient().execute(get);
            assertFalse(response.hasError());
        } catch (OXException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        } catch (JSONException e) {
            fail(e.toString());
        }
    }

    public void assertDoesNotExist(Appointment appointment) {
        GetRequest get = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID(), false);
        try {
            GetResponse response = getClient().execute(get);
            assertTrue(response.hasError());
            assertTrue(response.getResponse().getErrorMessage().contains("not found.")); // Brittle
        } catch (OXException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        } catch (JSONException e) {
            fail(e.toString());
        }
    }

}
