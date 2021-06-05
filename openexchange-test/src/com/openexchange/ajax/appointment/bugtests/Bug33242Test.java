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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug33242Test}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class Bug33242Test extends AbstractAJAXSession {


    private CalendarTestManager catm2;

    private Appointment series;

    private Appointment single;

    private int groupParticipant;

    private Appointment exception;

    public Bug33242Test() {
        super();
    }

    /*
     * 1.) User A creates series with group (all users), daily, occurs 5 times.
     * 2.) User B (member of group) deletes single occurrence.
     */

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        catm2 = new CalendarTestManager(testUser2.getAjaxClient());

        groupParticipant = i(testContext.acquireGroup(Optional.of(Collections.singletonList(I(testUser2.getUserId()))))); //TODO null check

        prepareSeries();
        prepareSingle();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    /**
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private void prepareSeries() throws OXException, IOException, JSONException {
        int nextYear = Calendar.getInstance().get(Calendar.YEAR);
        series = new Appointment();
        series.setTitle("Bug 33242 Test");
        series.setStartDate(D("01.08." + nextYear + " 18:00"));
        series.setEndDate(D("01.08." + nextYear + " 19:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setOccurrence(5);
        series.setInterval(1);

        UserParticipant userPart = new UserParticipant(getClient().getValues().getUserId());
        GroupParticipant groupPart = getGroupParticipant(groupParticipant);

        series.setParticipants(new Participant[] { userPart, groupPart });
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        series.setIgnoreConflicts(true);

        catm.insert(series);

        exception = catm.createIdentifyingCopy(series);
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        exception.setNote("Hello World");
        exception.setRecurrencePosition(2);

        catm.setFailOnError(true);
        catm2.setFailOnError(true);
    }

    /**
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private void prepareSingle() throws OXException, IOException, JSONException {
        int nextYear = Calendar.getInstance().get(Calendar.YEAR);
        single = new Appointment();
        single.setTitle("Bug 33242 Test Single");
        single.setStartDate(D("06.08." + nextYear + " 18:00"));
        single.setEndDate(D("06.08." + nextYear + " 19:00"));

        UserParticipant userPart = new UserParticipant(getClient().getValues().getUserId());
        GroupParticipant groupPart = getGroupParticipant(groupParticipant);

        single.setParticipants(new Participant[] { userPart, groupPart });
        single.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        single.setIgnoreConflicts(true);

        catm.insert(single);

        catm.setFailOnError(true);
        catm2.setFailOnError(true);
    }

    /**
     * @return
     */
    private GroupParticipant getGroupParticipant(int groupParticipant) {
        GroupParticipant gpart = new GroupParticipant(groupParticipant);
        return gpart;
    }

    @Test
    public void testDeleteByCreator() throws Exception {
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());

        // This should fail if not possible
        catm.delete(exception);

        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = catm.get(series);
        series.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = catm2.get(series);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getDeleteException());
        assertNotNull(groupMemberAppointment.getDeleteException());
        assertEquals(creatorAppointment.getDeleteException().length, 1);
        assertEquals(groupMemberAppointment.getDeleteException().length, 1);
        assertNull(creatorAppointment.getChangeException());
        assertNull(groupMemberAppointment.getChangeException());
    }

    @Test
    public void testDeleteByGroupMember() throws Exception {
        exception.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());

        // This should fail if not possible
        catm2.delete(exception);

        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = catm.get(series);
        series.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = catm2.get(series);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getChangeException());
        assertNotNull(groupMemberAppointment.getDeleteException());
        assertEquals(creatorAppointment.getChangeException().length, 1);
        assertEquals(groupMemberAppointment.getDeleteException().length, 1);
        assertNull(creatorAppointment.getDeleteException());
        assertNull(groupMemberAppointment.getChangeException());

        List<Appointment> checkAppointment = catm.getChangeExceptions(getClient().getValues().getPrivateAppointmentFolder(), series.getObjectID(), Appointment.ALL_COLUMNS);
        assertNotNull(checkAppointment);
        assertEquals(checkAppointment.size(), 1);
        boolean found = false;
        for (Participant p : checkAppointment.get(0).getParticipants()) {
            if (p.getIdentifier() == getClient().getValues().getUserId()) {
                found = true;
            }
        }
        assertTrue("The creator is missing in the Participant list, but should be present", found);
    }

    @Test
    public void testDeleteByCreaterWithUpdate() throws Exception {
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.update(exception);

        // This should fail if not possible
        catm.delete(exception);

        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = catm.get(series);
        series.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = catm2.get(series);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getDeleteException());
        assertNotNull(groupMemberAppointment.getDeleteException());
        assertEquals(creatorAppointment.getDeleteException().length, 1);
        assertEquals(groupMemberAppointment.getDeleteException().length, 1);
        assertNull(creatorAppointment.getChangeException());
        assertNull(groupMemberAppointment.getChangeException());
    }

    @Test
    public void testDeleteByGroupMemberWithUpdate() throws Exception {
        exception.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        exception.removeNote(); // cannot change note as attendee
        catm2.update(exception);

        // This should fail if not possible
        catm2.delete(exception);

        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = catm.get(series);
        series.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = catm2.get(series);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getChangeException());
        assertNotNull(groupMemberAppointment.getDeleteException());
        assertEquals(creatorAppointment.getChangeException().length, 1);
        assertEquals(groupMemberAppointment.getDeleteException().length, 1);
        assertNull(creatorAppointment.getDeleteException());
        assertNull(groupMemberAppointment.getChangeException());

        Appointment copy = catm.createIdentifyingCopy(series);
        copy.setRecurrencePosition(2);
        Appointment checkAppointment = catm.get(copy);
        assertNotNull(checkAppointment);

        boolean found = false;
        for (UserParticipant up : checkAppointment.getUsers()) {
            if (up.getIdentifier() == getClient().getValues().getUserId()) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testDeleteByGroupMemberSingle() throws Exception {
        single.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        // This should fail if not possible
        catm2.delete(single);
    }

    @Test
    public void testDeleteByCreaterSingle() throws Exception {
        single.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        // This should fail if not possible
        catm.delete(single);
    }

}
