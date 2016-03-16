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

package com.openexchange.groupware.calendar.calendarsqltests;

import com.openexchange.exception.OXException;
import static com.openexchange.groupware.calendar.tools.CalendarAssertions.assertUserParticipants;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.setuptools.TestContextToolkit;


public class Bug10154Test extends CalendarSqlTest {
 // Bug 10154

    public void testShouldKeepParticipantsInSharedFolder() throws OXException, SQLException {
        folders.sharePrivateFolder(session, ctx, secondUserId);
        try {
            appointments.switchUser(secondUser);
            CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user);
            appointment.setParentFolderID(folders.getStandardFolder(userId, ctx));

            appointments.save(appointment);

            appointment = appointments.reload(appointment);

            appointments.switchUser(user);

            final ArrayList<Participant> participants = new ArrayList<Participant>(java.util.Arrays.asList(appointment.getParticipants()));

            final TestContextToolkit tk = new TestContextToolkit();

            final UserParticipant participant = new UserParticipant(tk.resolveUser(participant1));
            participants.add(participant);

            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setObjectID(appointment.getObjectID());
            cdao.setParentFolderID(appointment.getParentFolderID());
            cdao.setContext(appointment.getContext());
            cdao.setParticipants(participants);

            appointments.save(cdao);

            appointments.switchUser(secondUser);

            cdao = new CalendarDataObject();
            cdao.setStartDate(appointment.getStartDate());
            cdao.setEndDate(new Date(appointment.getEndDate().getTime() + 36000000));
            cdao.setObjectID(appointment.getObjectID());
            cdao.setParentFolderID(appointment.getParentFolderID());
            cdao.setContext(appointment.getContext());

            appointments.save(cdao);

            appointments.switchUser(user);
            appointment = appointments.reload(appointment);

            assertUserParticipants(appointment, user, participant1);

        } finally {
            // Unshare
            folders.unsharePrivateFolder(session, ctx);
        }
    }
}
