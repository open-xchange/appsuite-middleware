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

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug13788Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug13788Test extends AbstractChronosTest {

    @Test
    public void testBug13788() throws Exception {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "dsd", folderId);
        long now = System.currentTimeMillis();
        DateTimeData start = DateTimeUtil.getDateTimeWithoutTimeInformation(now);
        DateTimeData end = DateTimeUtil.getDateTimeWithoutTimeInformation(now + TimeUnit.DAYS.toMillis(1));
        event.setStartDate(start);
        event.setEndDate(end);
        EventData createEvent = eventManager.createEvent(event, true);

        createEvent.setStartDate(end);
        createEvent.setEndDate(DateTimeUtil.getDateTimeWithoutTimeInformation(now + TimeUnit.DAYS.toMillis(2)));
        eventManager.updateEvent(createEvent, false, false);

        EventData getEvent = eventManager.getEvent(folderId, createEvent.getId());
        Date date = DateTimeUtil.parseAllDay(getEvent.getStartDate());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

}
