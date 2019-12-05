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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link AttendeeDescriptionTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.3
 */
public class AttendeeDescriptionTest extends AbstractDescriptionTest {

    private List<Attendee> attendeeIndividualWithUri;
    private List<Attendee> attendeeIndividualWithUriAndStatus;
    private List<Attendee> attendeeIndividualWithCn;
    private List<Attendee> attendeeGroup;
    private List<Attendee> attendeeResource;
    private List<Attendee> multipeAttendees;
    private String[] uriSorted;
    private ArrayList<Attendee> multipeAttendeesWithExternal;
    private String[] uriSortedWithExternal;

    private static final String[] ADD_MESSAGE = new String[3];
    static {
        ADD_MESSAGE[0] = "has been invited to the appointment.";
        ADD_MESSAGE[1] = "has been invited to the appointment.";
        ADD_MESSAGE[2] = "has been reserved for the appointment.";
    }

    private static final String[] DELETE_MESSAGE = new String[3];
    static {
        DELETE_MESSAGE[0] = "has been removed from the appointment.";
        DELETE_MESSAGE[1] = "has been removed from the appointment.";
        DELETE_MESSAGE[2] = "is no longer reserved for the appointment.";
    }

    private static final String UPDATE_MESSAGE = " the appointment.";

    public AttendeeDescriptionTest() {
        super(EventField.ATTENDEES, "", () -> {
            return new AttendeeDescriber();
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        attendeeIndividualWithUri = new ArrayList<Attendee>();
        Attendee a = new Attendee();
        a.setUri("testuri");
        a.setCuType(CalendarUserType.INDIVIDUAL);
        attendeeIndividualWithUri.add(a);

        attendeeIndividualWithUriAndStatus = new ArrayList<Attendee>();
        Attendee a1 = new Attendee();
        a1.setUri("testuri");
        a1.setCuType(CalendarUserType.INDIVIDUAL);
        a1.setPartStat(ParticipationStatus.ACCEPTED);
        attendeeIndividualWithUriAndStatus.add(a1);

        attendeeIndividualWithCn = new ArrayList<Attendee>();
        Attendee b = new Attendee();
        b.setCn("testName");
        b.setCuType(CalendarUserType.INDIVIDUAL);
        attendeeIndividualWithCn.add(b);

        attendeeGroup = new ArrayList<Attendee>();
        Attendee c = new Attendee();
        c.setCuType(CalendarUserType.GROUP);
        c.setUri("GroupOfUsers");
        attendeeGroup.add(c);

        attendeeResource = new ArrayList<Attendee>();
        Attendee d = new Attendee();
        d.setCuType(CalendarUserType.RESOURCE);
        d.setUri("Resource");
        attendeeResource.add(d);

        multipeAttendees = new ArrayList<Attendee>();
        Attendee e1 = new Attendee();
        e1.setCuType(CalendarUserType.INDIVIDUAL);
        e1.setCn("Anton");
        e1.setEntity(1);
        multipeAttendees.add(e1);
        Attendee e2 = new Attendee();
        e2.setCuType(CalendarUserType.INDIVIDUAL);
        e2.setCn("Caesar");
        e2.setEntity(1);
        multipeAttendees.add(e2);
        Attendee e3 = new Attendee();
        e3.setCuType(CalendarUserType.INDIVIDUAL);
        e3.setCn("Berta");
        e3.setEntity(1);
        multipeAttendees.add(e3);
        uriSorted = new String[] { "Anton", "Berta", "Caesar" };

        multipeAttendeesWithExternal = new ArrayList<Attendee>();
        multipeAttendeesWithExternal.add(e1);
        multipeAttendeesWithExternal.add(e2);
        Attendee e4 = new Attendee();
        e4.setCuType(CalendarUserType.INDIVIDUAL);
        e4.setUri("Berta");
        multipeAttendeesWithExternal.add(e4);
        uriSortedWithExternal = new String[] { "Anton", "Caesar", "Berta" };
    }

    @Test
    public void testAttendees_AddNewIndividualWithUri_DescriptionAvailable() {
        setAttendees(null, attendeeIndividualWithUri);
        descriptionMessage = ADD_MESSAGE[0];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, attendeeIndividualWithUri.get(0).getUri());
    }

    @Test
    public void testAttendees_AddNewIndividualWithUriAndStatus_DescriptionAvailable() {
        setAttendees(null, attendeeIndividualWithUriAndStatus);
        descriptionMessage = ADD_MESSAGE[0];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, attendeeIndividualWithUriAndStatus.get(0).getUri());
    }

    @Test
    public void testAttendees_AddNewIndividualWithCn_DescriptionAvailable() {
        setAttendees(null, attendeeIndividualWithCn);
        descriptionMessage = ADD_MESSAGE[0];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, attendeeIndividualWithCn.get(0).getCn());
    }

    @Test
    public void testAttendees_AddNewGroup_DescriptionAvailable() {
        setAttendees(null, attendeeGroup);
        descriptionMessage = ADD_MESSAGE[1];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, "group " + attendeeGroup.get(0).getUri());
    }

    @Test
    public void testAttendees_AddNewRessource_DescriptionAvailable() {
        setAttendees(null, attendeeResource);
        descriptionMessage = ADD_MESSAGE[2];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, "resource " + attendeeResource.get(0).getUri());
    }

    @Test
    public void testAttendees_AddMultipleAttendees_DescriptionAvailable() {
        setAttendees(null, multipeAttendees);
        descriptionMessage = ADD_MESSAGE[0];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        assertThat("Not matching size", I(description.getSentences().size()), is((I(multipeAttendees.size()))));

        for (int i = 0; i < multipeAttendees.size(); i++) {
            assertTrue(getMessage(description, i).endsWith(descriptionMessage));
            assertTrue(getMessage(description, i).contains(uriSorted[i]));
        }
    }

    @Test
    public void testAttendees_AddMultipleAttendeesWithExternals_DescriptionAvailable() {
        setAttendees(null, multipeAttendeesWithExternal);
        descriptionMessage = ADD_MESSAGE[0];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        assertThat("Not matching size", I(description.getSentences().size()), is((I(multipeAttendeesWithExternal.size()))));

        for (int i = 0; i < multipeAttendeesWithExternal.size(); i++) {
            assertTrue(getMessage(description, i).endsWith(descriptionMessage));
            assertTrue(getMessage(description, i).contains(uriSortedWithExternal[i]));
        }
    }

    @Test
    public void testAttendees_UpdateAttendee_DescriptionAvailable() {
        setAttendees(attendeeIndividualWithUri, attendeeIndividualWithUriAndStatus);
        descriptionMessage = UPDATE_MESSAGE;

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, attendeeIndividualWithUriAndStatus.get(0).getUri());
        checkMessageEnd(description, attendeeIndividualWithUriAndStatus.get(0).getPartStat().toString().toLowerCase());
    }

    @Test
    public void testAttendees_RemoveIndividualWithUri_DescriptionAvailable() {
        setAttendees(attendeeIndividualWithUri, null);
        descriptionMessage = DELETE_MESSAGE[0];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, attendeeIndividualWithUri.get(0).getUri());
    }

    @Test
    public void testAttendees_RemoveIndividualWithCn_DescriptionAvailable() {
        setAttendees(attendeeIndividualWithCn, null);
        descriptionMessage = DELETE_MESSAGE[0];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, attendeeIndividualWithCn.get(0).getCn());
    }

    @Test
    public void testAttendees_RemoveGroup_DescriptionAvailable() {
        setAttendees(attendeeGroup, null);
        descriptionMessage = DELETE_MESSAGE[1];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, "group " + attendeeGroup.get(0).getUri());
    }

    @Test
    public void testAttendees_RemoveRessource_DescriptionAvailable() {
        setAttendees(attendeeResource, null);
        descriptionMessage = DELETE_MESSAGE[2];

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, "resource " + attendeeResource.get(0).getUri());
    }

    @Test
    public void testAttendees_NullValue_DescriptionUnavailable() {
        PowerMockito.when(eventUpdate.getAttendeeUpdates()).thenReturn(null);

        Description description = describer.describe(eventUpdate);
        assertThat(description, nullValue());
    }

    @Test
    public void testAttendees_UpdateEmpty_DescriptionUnavailable() {
        setAttendees(attendeeIndividualWithUri, attendeeIndividualWithUri);

        Description description = describer.describe(eventUpdate);
        assertThat(description, nullValue());
    }

    // -------------------- HELPERS --------------------

    private void setAttendees(List<Attendee> originalAttendees, List<Attendee> updatedAttendees) {
        PowerMockito.when(eventUpdate.getAttendeeUpdates()).thenReturn(CalendarUtils.getAttendeeUpdates(originalAttendees, updatedAttendees));
        PowerMockito.when(original.getAttendees()).thenReturn(originalAttendees);
        PowerMockito.when(updated.getAttendees()).thenReturn(updatedAttendees);
    }
}
