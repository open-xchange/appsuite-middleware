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
import java.util.Date;
import org.json.JSONException;
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
 * {@link Bug41794Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug41794Test extends AbstractAJAXSession {

    private AJAXClient client2;
    private AJAXClient client3;
    private CalendarTestManager ctm1;
    private CalendarTestManager ctm2;
    private CalendarTestManager ctm3;
    private String groupParticipant;
    private Appointment appointment;

    public Bug41794Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
        client3 = new AJAXClient(User.User3);
        groupParticipant = AJAXConfig.getProperty(AJAXConfig.Property.GROUP_PARTICIPANT);
        ctm1 = new CalendarTestManager(client);
        ctm2 = new CalendarTestManager(client2);
        ctm3 = new CalendarTestManager(client3);

        appointment = new Appointment();
        appointment.setTitle(this.getClass().getSimpleName());
        appointment.setStartDate(D("01.11.2015 08:00"));
        appointment.setEndDate(D("01.11.2015 09:00"));
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);

        UserParticipant up = new UserParticipant(client.getValues().getUserId());
        GroupParticipant gp = getGroupParticipant(groupParticipant);
        appointment.setParticipants(new Participant[] { up, gp });
    }

    public void testBug41794() throws Exception {
        ctm1.insert(appointment);
        
        appointment.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        ctm2.delete(appointment);

        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        Appointment loadedAppointment = ctm1.get(appointment);
        for (UserParticipant up : loadedAppointment.getUsers()) {
            if (up.getIdentifier() == client2.getValues().getUserId()) {
                fail("Did not expect participant.");
            }
        }

        loadedAppointment = ctm3.get(client3.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        loadedAppointment.setAlarm(15);
        loadedAppointment.setLastModified(new Date(Long.MAX_VALUE));
        loadedAppointment.setIgnoreConflicts(true);
        ctm3.confirm(loadedAppointment, Appointment.ACCEPT, "message");
        ctm3.update(loadedAppointment);

        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        loadedAppointment = ctm1.get(appointment);
        for (UserParticipant up : loadedAppointment.getUsers()) {
            if (up.getIdentifier() == client2.getValues().getUserId()) {
                fail("Did not expect participant: " + up.getIdentifier());
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        ctm1.cleanUp();
        ctm2.cleanUp();
        ctm3.cleanUp();
        super.tearDown();
    }

    private GroupParticipant getGroupParticipant(String groupParticipant) throws OXException, IOException, JSONException {
        SearchResponse response = getClient().execute(new SearchRequest(groupParticipant));
        Group[] group = response.getGroups();
        final int groupParticipantId = group[0].getIdentifier();
        GroupParticipant gpart = new GroupParticipant(groupParticipantId);
        return gpart;
    }

}
