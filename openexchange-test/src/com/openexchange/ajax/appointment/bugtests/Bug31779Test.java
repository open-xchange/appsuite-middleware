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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug31779Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug31779Test extends AbstractAJAXSession {

    private int nextYear;

    private Appointment appointment;

    private CalendarTestManager ctm2;

    public Bug31779Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ctm2 = new CalendarTestManager(getClient2());

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        appointment = new Appointment();
        appointment.setTitle("Bug 31779 appointment.");
        appointment.setStartDate(D("01.04." + nextYear + " 08:00"));
        appointment.setEndDate(D("01.04." + nextYear + " 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        UserParticipant user1 = new UserParticipant(getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(getClient2().getValues().getUserId());
        appointment.setParticipants(new Participant[] { user1, user2 });
        appointment.setUsers(new UserParticipant[] { user1, user2 });

        catm.insert(appointment);
    }

    /**
     * Tests the bug as written in the report: If the creator deletes an exception, the whole exception should disappear.
     * 
     * @throws Exception
     */
    @Test
    public void testBug31779() throws Exception {
        Appointment exception = ctm2.createIdentifyingCopy(appointment);
        exception.setParentFolderID(getClient2().getValues().getPrivateAppointmentFolder());
        exception.setNote("Hello World");
        exception.setRecurrencePosition(2);
        ctm2.update(exception);
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.delete(catm.createIdentifyingCopy(exception));
        exception.setParentFolderID(getClient2().getValues().getPrivateAppointmentFolder());
        Appointment loadedException = ctm2.get(exception);
        assertNull("No object expected.", loadedException);
        assertTrue("Error expected.", ctm2.getLastResponse().hasError());
        assertTrue("No object expected.", ctm2.getLastResponse().getErrorMessage().contains("Object not found"));
    }

    /**
     * Tests the case, that a participant deletes an exception: Only the participant should be removed.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteByparticipant() throws Exception {
        Appointment exception = ctm2.createIdentifyingCopy(appointment);
        exception.setParentFolderID(getClient2().getValues().getPrivateAppointmentFolder());
        exception.setNote("Hello World");
        exception.setRecurrencePosition(2);
        ctm2.update(exception);
        ctm2.delete(ctm2.createIdentifyingCopy(exception));
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        Appointment loadedException = catm.get(exception);
        assertNotNull("Object expected.", loadedException);
        assertEquals("Wrong creator.", getClient().getValues().getUserId(), loadedException.getCreatedBy());
        assertEquals("Wrong changer.", getClient2().getValues().getUserId(), loadedException.getModifiedBy());
    }

    @After
    public void tearDown() throws Exception {
        try {
            ctm2.cleanUp();
        } finally {
            super.tearDown();
        }
    }

}
