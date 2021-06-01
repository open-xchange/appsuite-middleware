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
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosConflictDataRaw;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link Bug15585Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15585Test extends AbstractChronosTest {

    public Bug15585Test() {
        super();
    }

    @Test
    public void testConflictTitle() throws Throwable {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug15585Test", folderId);
        EventData storedEvent = eventManager.createEvent(event, true);

        ChronosCalendarResultResponse response = defaultUserApi.getChronosApi().createEvent(folderId, event, Boolean.TRUE, null, Boolean.FALSE, null, null, null, Boolean.FALSE, null);
        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        CalendarResult data = response.getData();
        assertNotNull(data.getConflicts());
        ChronosConflictDataRaw matchingConflict = null;
        for (ChronosConflictDataRaw c : data.getConflicts()) {
            if (null != c.getEvent() && storedEvent.getId().equals(c.getEvent().getId())) {
                matchingConflict = c;
                break;
            }
        }
        assertNotNull(matchingConflict);
        assertNotNull(matchingConflict.getEvent());
        assertNotNull(matchingConflict.getEvent().getSummary());
    }
}
