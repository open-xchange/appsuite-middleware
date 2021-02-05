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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static com.openexchange.java.Autoboxing.i;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.pool.TestUser;

/**
 * {@link Bug41794Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug41794Test extends AbstractAJAXSession {

    private AJAXClient client3;
    private CalendarTestManager ctm2;
    private CalendarTestManager ctm3;
    private int groupParticipant;
    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        TestUser user3 = testContext.acquireUser();
        client3 = new AJAXClient(user3);
        groupParticipant = i(testContext.acquireGroup(Optional.of(Collections.singletonList(user3.getUserId())))); //TODO null check
        catm = new CalendarTestManager(getClient());
        ctm2 = new CalendarTestManager(getClient(1));
        ctm3 = new CalendarTestManager(client3);

        appointment = new Appointment();
        appointment.setTitle(this.getClass().getSimpleName());
        appointment.setStartDate(D("01.11.2015 08:00"));
        appointment.setEndDate(D("01.11.2015 09:00"));
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);

        UserParticipant up = new UserParticipant(getClient().getValues().getUserId());
        GroupParticipant gp = getGroupParticipant(groupParticipant);
        appointment.setParticipants(new Participant[] { up, gp });
    }

    @Override
    public TestConfig getTestConfig() {
        return TestConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug41794() throws Exception {
        catm.insert(appointment);

        appointment.setParentFolderID(getClient(1).getValues().getPrivateAppointmentFolder());
        ctm2.delete(appointment);

        assertNull("Did not expect appointment for user 2", ctm2.get(getClient(1).getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), false));

        Appointment loadedAppointment = ctm3.get(client3.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertNotNull(loadedAppointment);
        loadedAppointment.setAlarm(15);
        loadedAppointment.setLastModified(new Date(Long.MAX_VALUE));
        loadedAppointment.setIgnoreConflicts(true);
        ctm3.confirm(loadedAppointment, Appointment.ACCEPT, "message");
        ctm3.update(loadedAppointment);

        assertNull("Did not expect appointment for user 2", ctm2.get(getClient(1).getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), false));
    }

    private GroupParticipant getGroupParticipant(int groupParticipantId) {
        GroupParticipant gpart = new GroupParticipant(groupParticipantId);
        return gpart;
    }

}
