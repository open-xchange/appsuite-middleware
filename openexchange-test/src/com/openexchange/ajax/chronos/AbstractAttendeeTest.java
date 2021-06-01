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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.test.common.test.TestClassConfig;
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
        ApiClient client2 = testUser2.getApiClient();
        user2 = new UserApi(client2, getEnhancedApiClient2(), testUser2);
        folderId2 = getDefaultFolder(client2);
        eventManager2 = new EventManager(user2, getDefaultFolder(client2));
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).useEnhancedApiClients().build();
    }

    protected EventData updateAlarms(String eventId, long timestamp, List<Alarm> body, String recurrenceId) throws Exception {
        ChronosCalendarResultResponse calendarResult = user2.getChronosApi().updateAlarms(folderId2, eventId, L(timestamp), body, recurrenceId, Boolean.FALSE, null, null, null, null, null, null);
        List<EventData> updates = calendarResult.getData().getUpdated();
        assertTrue(updates.size() == 1);
        return updates.get(0);
    }

    public List<Attendee> addAdditionalAttendee(EventData expectedEventData) {
        ArrayList<Attendee> atts = new ArrayList<>(2);
        atts.addAll(expectedEventData.getAttendees());
        Attendee attendee2 = AttendeeFactory.createIndividual(I(user2.getCalUser().intValue()));
        attendee2.setPartStat("ACCEPTED");
        atts.add(attendee2);
        return atts;
    }

    protected AttendeeAndAlarm createAttendeeAndAlarm(EventData updatedEvent, int attendeeId) {
        AttendeeAndAlarm body = new AttendeeAndAlarm();
        for (Attendee attendee : updatedEvent.getAttendees()) {
            if (attendee.getEntity() != null && attendee.getEntity().intValue() == attendeeId) {
                attendee.setPartStat("TENTATIVE");
                attendee.setMember(null);
                body.attendee(attendee);
            }
        }
        body.addAlarmsItem(AlarmFactory.createAlarm("-PT20M", RelatedEnum.START));
        return body;
    }

}
