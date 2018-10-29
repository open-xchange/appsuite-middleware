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

package com.openexchange.ajax.chronos;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;

/**
 * {@link AbstractAttendeeTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class AbstractAttendeeTest extends AbstractChronosTest {

    protected String folderId2;
    protected UserApi user2;
    protected EventManager eventManager2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ApiClient client = generateApiClient(testUser2);
        rememberClient(client);
        EnhancedApiClient enhancedClient = generateEnhancedClient(testUser2);
        rememberClient(enhancedClient);

        user2 = new UserApi(client, enhancedClient, testUser2, false);
        folderId2 = getDefaultFolder(user2.getSession(), client);
        eventManager2 = new EventManager(user2, getDefaultFolder(user2.getSession(), client));
    }

    @Override
    public void tearDown() throws Exception {
        eventManager2.cleanUp();
        super.tearDown();
    }

    protected EventData updateAlarms(String eventId, long timestamp, List<Alarm> body, String recurrenceId) throws Exception {
        ChronosCalendarResultResponse calendarResult = user2.getChronosApi().updateAlarms(user2.getSession(), folderId2, eventId, timestamp, body, recurrenceId, false, null);
        List<EventData> updates = calendarResult.getData().getUpdated();
        assertTrue(updates.size() == 1);
        return updates.get(0);
    }

    public List<Attendee> addAdditionalAttendee(EventData expectedEventData) {
        ArrayList<Attendee> atts = new ArrayList<>(2);
        atts.addAll(expectedEventData.getAttendees());
        Attendee attendee2 = AttendeeFactory.createIndividual(user2.getCalUser());
        attendee2.setPartStat("ACCEPTED");
        atts.add(attendee2);
        return atts;
    }

    protected AttendeeAndAlarm createAttendeeAndAlarm(EventData updatedEvent, int attendeeId) {
        AttendeeAndAlarm body = new AttendeeAndAlarm();
        for (Attendee attendee : updatedEvent.getAttendees()) {
            if (attendee.getEntity() == attendeeId) {
                attendee.setPartStat("TENTATIVE");
                attendee.setMember(null);
                body.attendee(attendee);
            }
        }
        body.addAlarmsItem(AlarmFactory.createAlarm("-PT20M", RelatedEnum.START));
        return body;
    }

}
