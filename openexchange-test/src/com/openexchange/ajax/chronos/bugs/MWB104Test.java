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

package com.openexchange.ajax.chronos.bugs;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.EnhancedApiClient;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link MWB104Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class MWB104Test extends AbstractChronosTest {

    private UserApi userApi2;
    private String defaultFolderId2;
    private EventManager eventManager2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ApiClient apiClient2 = generateApiClient(testUser2);
        rememberClient(apiClient2);
        EnhancedApiClient enhancedApiClient2 = generateEnhancedClient(testUser2);
        rememberClient(enhancedApiClient2);
        userApi2 = new UserApi(apiClient2, enhancedApiClient2, testUser2, true);
        defaultFolderId2 = getDefaultFolder(userApi2.getSession(), userApi2.getFoldersApi());
        eventManager2 = new EventManager(userApi2, defaultFolderId2);
    }

    @Test
    public void testResetPartstat() throws Exception {
        EventData eventData = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "MWB-104 Test", folderId);
        Integer secondUserId = I(getClient2().getValues().getUserId());
        Attendee attendee = AttendeeFactory.createIndividual(secondUserId);
        eventData.addAttendeesItem(attendee);
        EventData createdEvent = eventManager.createEvent(eventData, true);

        AttendeeAndAlarm attendeeAndAlarm = new AttendeeAndAlarm();
        attendee.setPartStat("ACCEPTED");
        attendee.setComment("Comment");
        attendeeAndAlarm.setAttendee(attendee);
        eventManager2.setLastTimeStamp(eventManager.getLastTimeStamp());
        eventManager2.updateAttendee(createdEvent.getId(), attendeeAndAlarm, false);

        for (Attendee a : eventManager.getEvent(createdEvent.getFolder(), createdEvent.getId()).getAttendees()) {
            if (a.getEntity() == secondUserId) {
                assertEquals("Expected correct partstat", "ACCEPTED", a.getPartStat());
                assertEquals("Expected correct comment.", "Comment", a.getComment());
            }
        }

        createdEvent.setStartDate(DateTimeUtil.incrementDateTimeData(createdEvent.getStartDate(), 3600000));
        createdEvent.setEndDate(DateTimeUtil.incrementDateTimeData(createdEvent.getEndDate(), 3600000));
        createdEvent.setAttendees(null);

        eventManager.setLastTimeStamp(eventManager2.getLastTimeStamp());
        eventManager.updateEvent(createdEvent, false, false);

        for (Attendee a : eventManager.getEvent(createdEvent.getFolder(), createdEvent.getId()).getAttendees()) {
            if (a.getEntity() == secondUserId) {
                assertEquals("Expected correct partstat", "NEEDS-ACTION", a.getPartStat());
                assertEquals("Expected correct comment.", "Comment", a.getComment());
            }
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        eventManager2.cleanUp();
        super.tearDown();
    }
}
