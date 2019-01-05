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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.webdav.protocol.WebdavPath;

/**
 * {@link MoveTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class MoveTest extends CalDAVTest {

    private CalendarTestManager manager2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager2 = new CalendarTestManager(new AJAXClient(testContext.acquireUser()));
        manager2.setFailOnError(true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != this.manager2) {
                this.manager2.cleanUp();
                if (null != manager2.getClient()) {
                    manager2.getClient().logout();
                }
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testMoveAsAttendeeOnClient() throws Exception {
        /*
         * create subfolder & fetch sync token for later synchronization
         */
        FolderObject subfolder = createFolder(randomUID());
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment as user 2 on server
         */
        String uid = randomUID();
        String summary = "serie";
        String location = "test";
        Date start = TimeTools.D("next sunday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        List<Participant> participants = new ArrayList<Participant>();
        participants.add(new UserParticipant(getClient().getValues().getUserId()));
        participants.add(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.setParticipants(participants);
        appointment.setParentFolderID(manager2.getPrivateFolder());
        appointment.setIgnoreConflicts(true);
        manager2.insert(appointment);
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendee);
        /*
         * move appointment to subfolder on client
         */
        MoveMethod move = null;
        String targetHref = "/caldav/" + encodeFolderID(String.valueOf(subfolder.getObjectID())) + '/' + new WebdavPath(iCalResource.getHref()).name();
        try {
            move = new MoveMethod(getBaseUri() + iCalResource.getHref(), getBaseUri() + targetHref, false);
            Assert.assertEquals("response code wrong", StatusCodes.SC_CREATED, getWebDAVClient().executeMethod(move));
        } finally {
            release(move);
        }
        /*
         * verify appointment was moved properly
         */
        assertNull(getAppointment(getDefaultFolderID(), appointment.getUid()));
        assertNotNull(getAppointment(String.valueOf(subfolder.getObjectID()), appointment.getUid()));
    }

}
