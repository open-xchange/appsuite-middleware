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

package com.openexchange.ajax.chronos.bugs;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.itip.ITipUtil;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link Bug68699Test}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class Bug68699Test extends AbstractChronosTest {

    private String summary;
    private ApiClient apiClient2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        apiClient2 = testUser2.getApiClient();
        summary = "Bug68699Test" + UUID.randomUUID();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).useEnhancedApiClients().build();
    }

    @Test(expected = AssertionError.class)
    public void testBug68699() throws Exception {
        try {
            /*
             * Create event with no location set
             */
            Attendee attendee = AttendeeFactory.createIndividual(I(testUser2.getUserId()));
            EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), summary, folderId);
            event.setFolder(folderId);
            event.setAttendees(Collections.singletonList(attendee));
            event.setLocation(null);
            event = eventManager.createEvent(event, true);

            /*
             * Update event with empty location
             */
            EventData deltaEvent = new EventData();
            deltaEvent.setId(event.getId());
            deltaEvent.setFolder(event.getFolder());
            deltaEvent.setLocation("");
            deltaEvent.setAttendees(event.getAttendees());
            deltaEvent.setAttachments(null); // Suppress empty list generated by client

            eventManager.updateEvent(deltaEvent);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        /*
         * Expect that there is no mail and util throws AssertionError
         */
        ITipUtil.receiveNotification(apiClient2, testUser.getLogin(), "Appointment changed: " + summary);
    }
}
