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

import static com.openexchange.ajax.folder.Create.ocl;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.server.impl.OCLPermission;

/**
 * Checks if a changed appointment in a shared folder looses all its participants.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug10154Test extends AbstractAJAXSession {

    /**
     * @param name test name.
     */
    public Bug10154Test(final String name) {
        super(name);
    }

    /**
     * A creates a shared folder and an appointment with participants. B changes
     * the participant in the folder and A verifies if its participants get lost.
     */
    public void testParticipantsLost() throws Throwable {
        final AJAXClient clientA = getClient();
        final int userIdA = clientA.getValues().getUserId();
        final AJAXClient clientB = new AJAXClient(User.User2);
        final FolderObject folder = Create.folder(
            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            "Folder to test bug 10154",
            FolderObject.CALENDAR,
            FolderObject.PRIVATE,
            ocl(userIdA, false, true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(clientB.getValues().getUserId(), false, false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION));
        {
            final CommonInsertResponse response = clientA.execute(
                new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
            response.fillObject(folder);
        }
        try {
            final TimeZone tzA = clientA.getValues().getTimeZone();
            final Appointment appointment = new Appointment();
            final List<Participant> onInsert = ParticipantTools.createParticipants(
                userIdA, clientB.getValues().getUserId());
            final Participant[] expected = onInsert.toArray(new Participant[onInsert.size()]);
            {
                appointment.setTitle("Test for bug 10154");
                appointment.setParentFolderID(folder.getObjectID());
                appointment.setStartDate(new Date(TimeTools.getHour(0, tzA)));
                appointment.setEndDate(new Date(TimeTools.getHour(1, tzA)));
                appointment.setParticipants(onInsert);
                appointment.setIgnoreConflicts(true);
                final InsertRequest request = new InsertRequest(appointment, tzA);
                final CommonInsertResponse response = clientA.execute(request);
                appointment.setLastModified(response.getTimestamp());
                appointment.setObjectID(response.getId());
            }
            final TimeZone tzB = clientB.getValues().getTimeZone();
            {
                final Appointment change = new Appointment();
                change.setObjectID(appointment.getObjectID());
                change.setParentFolderID(folder.getObjectID());
                change.setLastModified(appointment.getLastModified());
                change.setStartDate(new Date(TimeTools.getHour(1, tzB)));
                change.setEndDate(new Date(TimeTools.getHour(2, tzB)));
                change.setIgnoreConflicts(true);
                final UpdateRequest request = new UpdateRequest(change, tzB);
                final UpdateResponse response = clientB.execute(request);
                appointment.setLastModified(response.getTimestamp());
            }
            {
                final GetRequest request = new GetRequest(folder.getObjectID(), appointment.getObjectID());
                final GetResponse response = clientA.execute(request);
                final Appointment reload = response.getAppointment(tzA);
                final Participant[] participants = reload.getParticipants();
                assertEquals("Participants should not be changed.", expected.length, participants.length);
                final Comparator<Participant> comparator = new Comparator<Participant>() {
                    @Override
                    public int compare(final Participant o1, final Participant o2) {
                        return o1.getIdentifier() - o2.getIdentifier();
                    }};
                Arrays.sort(expected, comparator);
                Arrays.sort(participants, comparator);
                for (int i = 0; i < expected.length; i++) {
                    assertEquals("Participants should not be changed.", expected[i].getIdentifier(), participants[i].getIdentifier());
                }
            }
        } finally {
            clientA.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD,
                    folder.getObjectID(), folder.getLastModified()));
        }
    }
}
