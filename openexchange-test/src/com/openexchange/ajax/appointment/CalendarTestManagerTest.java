/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
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

    @Override
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
    public void testCreate() {
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);

        catm.insert(appointment);

        assertNotNull(appointment.getLastModified());
        assertExists(appointment);
    }

    @Test
    public void testRemove() {
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
    public void testUpdates() {
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
    public void testGetAllInFolder() {
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
