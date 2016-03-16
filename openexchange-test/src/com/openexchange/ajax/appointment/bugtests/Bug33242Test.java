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
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.group.actions.SearchRequest;
import com.openexchange.ajax.group.actions.SearchResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug33242Test}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class Bug33242Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;

    private CalendarTestManager ctm2;

    private Appointment series;

    private Appointment single;

    private AJAXClient client1;

    private AJAXClient client2;

    private String groupParticipant;


    private Appointment exception;

    public Bug33242Test(String name) {
        super(name);
    }

    /*
     * 1.) User A creates series with group (all users), daily, occurs 5 times.
     * 2.) User B (member of group) deletes single occurrence.
     */

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = new AJAXClient(User.User3);

        ctm = new CalendarTestManager(client1);
        ctm2 = new CalendarTestManager(client2);

        groupParticipant = AJAXConfig.getProperty(AJAXConfig.Property.GROUP_PARTICIPANT);

        prepareSeries();
        prepareSingle();
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

        UserParticipant userPart = new UserParticipant(client1.getValues().getUserId());
        GroupParticipant groupPart = getGroupParticipant(groupParticipant);

        series.setParticipants(new Participant[] { userPart, groupPart });
        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        series.setIgnoreConflicts(true);

        ctm.insert(series);

        exception = ctm.createIdentifyingCopy(series);
        exception.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        exception.setNote("Hello World");
        exception.setRecurrencePosition(2);

        ctm.setFailOnError(true);
        ctm2.setFailOnError(true);
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

        UserParticipant userPart = new UserParticipant(client1.getValues().getUserId());
        GroupParticipant groupPart = getGroupParticipant(groupParticipant);

        single.setParticipants(new Participant[] { userPart, groupPart });
        single.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        single.setIgnoreConflicts(true);

        ctm.insert(single);

        ctm.setFailOnError(true);
        ctm2.setFailOnError(true);
    }

    /**
     * @return
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private GroupParticipant getGroupParticipant(String groupParticipant) throws OXException, IOException, JSONException {
        SearchResponse response = getClient().execute(new SearchRequest(groupParticipant));
        Group[] group = response.getGroups();
        final int groupParticipantId = group[0].getIdentifier();
        GroupParticipant gpart = new GroupParticipant(groupParticipantId);
        return gpart;
    }

    @Test
    public void testDeleteByCreator() throws Exception {
        exception.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());

        // This should fail if not possible
        ctm.delete(exception);

        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = ctm.get(series);
        series.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = ctm2.get(series);
        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getDeleteException());
        assertNotNull(groupMemberAppointment.getDeleteException());
        assertSame(creatorAppointment.getDeleteException().length, 1);
        assertSame(groupMemberAppointment.getDeleteException().length, 1);
        assertNull(creatorAppointment.getChangeException());
        assertNull(groupMemberAppointment.getChangeException());
    }

    @Test
    public void testDeleteByGroupMember() throws Exception {
        exception.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());

        // This should fail if not possible
        ctm2.delete(exception);

        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = ctm.get(series);
        series.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = ctm2.get(series);
        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getChangeException());
        assertNotNull(groupMemberAppointment.getChangeException());
        assertSame(creatorAppointment.getChangeException().length, 1);
        assertSame(groupMemberAppointment.getChangeException().length, 1);
        assertNull(creatorAppointment.getDeleteException());
        assertNull(groupMemberAppointment.getDeleteException());

        List<Appointment> checkAppointment =  ctm.getChangeExceptions(client1.getValues().getPrivateAppointmentFolder(), series.getObjectID(), Appointment.ALL_COLUMNS);
        assertNotNull(checkAppointment);
        assertSame(checkAppointment.size(), 1);
        boolean found = false;
        for (Participant p : checkAppointment.get(0).getParticipants()) {
            if (p.getIdentifier() == client1.getValues().getUserId()) {
                found = true;
            }
        }
        assertTrue("The creator is missing in the Participant list, but should be present",found);
    }


    @Test
    public void testDeleteByCreaterWithUpdate() throws Exception {
        exception.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        ctm.update(exception);

        // This should fail if not possible
        ctm.delete(exception);

        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = ctm.get(series);
        series.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = ctm2.get(series);
        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getDeleteException());
        assertNotNull(groupMemberAppointment.getDeleteException());
        assertSame(creatorAppointment.getDeleteException().length, 1);
        assertSame(groupMemberAppointment.getDeleteException().length, 1);
        assertNull(creatorAppointment.getChangeException());
        assertNull(groupMemberAppointment.getChangeException());
    }

    @Test
    public void testDeleteByGroupMemberWithUpdate() throws Exception {
        exception.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        ctm2.update(exception);

        // This should fail if not possible
        ctm2.delete(exception);

        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        Appointment creatorAppointment = ctm.get(series);
        series.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        Appointment groupMemberAppointment = ctm2.get(series);
        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        assertNotNull(creatorAppointment.getChangeException());
        assertNotNull(groupMemberAppointment.getChangeException());
        assertSame(creatorAppointment.getChangeException().length, 1);
        assertSame(groupMemberAppointment.getChangeException().length, 1);
        assertNull(creatorAppointment.getDeleteException());
        assertNull(groupMemberAppointment.getDeleteException());

        Appointment copy = ctm.createIdentifyingCopy(series);
        copy.setRecurrencePosition(2);
        Appointment checkAppointment =  ctm.get(copy);
        assertNotNull(checkAppointment);

        boolean found = false;
        for (UserParticipant up : checkAppointment.getUsers()) {
            if (up.getIdentifier() == client1.getValues().getUserId()) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testDeleteByGroupMemberSingle() throws Exception {
        single.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        // This should fail if not possible
        ctm2.delete(single);
    }

    @Test
    public void testDeleteByCreaterSingle() throws Exception {
        single.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        // This should fail if not possible
        ctm.delete(single);
    }

    @Override
    public void tearDown() throws Exception {
        series.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        exception.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        single.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        ctm.cleanUp();
        ctm2.cleanUp();
        super.tearDown();
    }

}
