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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.junit.Test;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.task.ManagedTaskTest;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link Bug8475Test_TaskAttendeeHandling}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class Bug8475Test_TaskAttendeeHandling extends ManagedTaskTest {

    private final String task =
        "BEGIN:VCALENDAR\n"
        + "VERSION:2.0\n"
        + "PRODID:-//Apple Computer\\, Inc//iCal 1.5//EN\n"
        + "BEGIN:VTODO\n"
        + "ORGANIZER:MAILTO:tobias.friedrich@open-xchange.com\n"
        + "ATTENDEE:MAILTO:tobias.prinz@open-xchange.com\n"
        + "DTSTART:20070608T080000Z\n"
        + "STATUS:COMPLETED\n"
        + "SUMMARY:Test todo\n"
        + "UID:8D4FFA7A-ABC0-11D7-8200-00306571349C-RID\n"
        + "DUE:20070618T080000Z\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    @Test
    public void testAttendeeNotFound() throws Exception {
        final ICalImportRequest request = new ICalImportRequest(folderID, new ByteArrayInputStream(task.toString().getBytes(com.openexchange.java.Charsets.UTF_8)), false);
        ICalImportResponse response = getClient().execute(request);
        assertEquals(1, response.getImports().length);

        String objectId = response.getImports()[0].getObjectId();
        Task task = ttm.getTaskFromServer(folderID, Integer.parseInt(objectId));
        assertNotNull(task);

        final Participant[] participants = task.getParticipants();
        assertEquals("One participant?", 1, participants.length);
        boolean found = false;
        for (final Participant p : participants) {
            if ("tobias.prinz@open-xchange.com".equals(p.getEmailAddress())) {
                found = true;
            }
        }
        assertTrue("The attendee tobias.prinz@open-xchange.com couldnt be found", found);
    }

    @Test
    public void testInternalAttendee() throws Exception {

        final String ical =
            "BEGIN:VCALENDAR\n"
            + "VERSION:2.0\n"
            + "PRODID:-//Apple Computer\\, Inc//iCal 1.5//EN\n"
            + "BEGIN:VTODO\n"
            + "ORGANIZER:MAILTO:tobias.friedrich@open-xchange.com\n"
            + "ATTENDEE:MAILTO:"
            + testUser.getLogin() + "\n"
            + "DTSTART:20070608T080000Z\n"
            + "STATUS:COMPLETED\n"
            + "SUMMARY:Test todo\n"
            + "UID:8D4FFA7A-ABC0-11D7-8200-00306571349C-RID\n"
            + "DUE:20070618T080000Z\n"
            + "END:VTODO\n" + "END:VCALENDAR";

        final ICalImportRequest request = new ICalImportRequest(folderID, new ByteArrayInputStream(ical.toString().getBytes(com.openexchange.java.Charsets.UTF_8)), false);
        ICalImportResponse response = getClient().execute(request);
        assertEquals(1, response.getImports().length);

        String objectId = response.getImports()[0].getObjectId();
        Task task = ttm.getTaskFromServer(folderID, Integer.parseInt(objectId));

        final Participant[] participants = task.getParticipants();
        assertEquals("One participant?", 1, participants.length);
        final Participant p = participants[0];

        assertEquals(1, task.getUsers().length);
        UserParticipant internalParticipant = task.getUsers()[0];
        assertNotNull(internalParticipant);
        assertEquals(task.getCreatedBy(), internalParticipant.getIdentifier());

    }

}
