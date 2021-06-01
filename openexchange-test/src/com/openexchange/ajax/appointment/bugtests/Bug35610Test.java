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

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug35610Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug35610Test extends AbstractAJAXSession {

    private CalendarTestManager ctm2;

    private Appointment app;

    private int nextYear;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ctm2 = new CalendarTestManager(testUser2.getAjaxClient());

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        app = new Appointment();
        app.setTitle("Bug 35610 Test");
        app.setStartDate(D("26.11." + nextYear + " 08:00"));
        app.setEndDate(D("26.11." + nextYear + " 09:00"));
        app.setRecurrenceType(Appointment.DAILY);
        app.setInterval(1);
        app.setOccurrence(5);
        UserParticipant user1 = new UserParticipant(getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        app.setParticipants(new Participant[] { user1, user2 });
        app.setUsers(new UserParticipant[] { user1, user2 });
        app.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        app.setIgnoreConflicts(true);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testTimeChange() throws Exception {
        catm.insert(app);
        ctm2.confirm(ctm2.getPrivateFolder(), app.getObjectID(), app.getLastModified(), Appointment.ACCEPT, "yay");

        Appointment exception = catm.createIdentifyingCopy(app);
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.removeInterval();
        exception.removeOccurrence();
        exception.setRecurrencePosition(3);
        exception.setStartDate(D("28.11." + nextYear + " 12:00"));
        exception.setEndDate(D("28.11." + nextYear + " 13:00"));
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setIgnoreConflicts(true);

        catm.update(exception);

        Appointment loadedException = catm.get(exception);
        for (UserParticipant up : loadedException.getUsers()) {
            if (up.getIdentifier() == getClient().getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.ACCEPT, up.getConfirm());
            }
            if (up.getIdentifier() == testUser2.getAjaxClient().getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.NONE, up.getConfirm());
            }
        }
    }

    @Test
    public void testNoTimeChange() throws Exception {
        catm.insert(app);
        ctm2.confirm(ctm2.getPrivateFolder(), app.getObjectID(), app.getLastModified(), Appointment.ACCEPT, "yay");

        Appointment exception = catm.createIdentifyingCopy(app);
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.removeInterval();
        exception.removeOccurrence();
        exception.setRecurrencePosition(3);
        exception.setStartDate(D("28.11." + nextYear + " 08:00"));
        exception.setEndDate(D("28.11." + nextYear + " 09:00"));
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setIgnoreConflicts(true);

        catm.update(exception);

        Appointment loadedException = catm.get(exception);
        for (UserParticipant up : loadedException.getUsers()) {
            if (up.getIdentifier() == getClient().getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.ACCEPT, up.getConfirm());
            }
            if (up.getIdentifier() == testUser2.getAjaxClient().getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.ACCEPT, up.getConfirm());
            }
        }
    }

}
