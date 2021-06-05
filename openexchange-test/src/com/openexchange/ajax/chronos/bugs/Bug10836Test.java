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

import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractSecondUserChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventsResponse;

/**
 * Checks if the calendar has a vulnerability in the list request.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public final class Bug10836Test extends AbstractSecondUserChronosTest {

    /**
     * Default constructor.
     *
     * @param name Name of the test.
     */
    public Bug10836Test() {
        super();
    }

    /**
     * Creates a private appointment with user A and tries to read it with user
     * B through a list request.
     *
     * @throws Throwable if some exception occurs.
     */
    @Test
    public void testVulnerability() throws Throwable {

        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug10836Test", folderId);
        EventData createEvent = eventManager.createEvent(event);
        List<EventId> body = new ArrayList<>();
        EventId id = new EventId();
        id.setFolder(folderId);
        id.setId(createEvent.getId());
        body.add(id);
        EventsResponse response = userApi2.getChronosApi().getEventList(body, null, null);
        assertNotNull("Missing error.", response.getError());

        id.setFolder(defaultFolderId2);
        response = userApi2.getChronosApi().getEventList(body, null, null);
        assertNotNull("Missing error.", response.getError());
    }
}
