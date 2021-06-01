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
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.RRuleFactory.RRuleBuilder;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug15074Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug15074Test extends AbstractChronosTest {

    /**
     * Initializes a new {@link Bug15074Test}.
     */
    public Bug15074Test() {
        super();
    }

    @Test
    public void testBug() throws Exception {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug15074Test", folderId);
        Calendar start = Calendar.getInstance();
        start.set(2007, Calendar.DECEMBER, 7, 0, 0, 0);
        event.setStartDate(DateTimeUtil.getDateTime(start));

        Calendar end = Calendar.getInstance();
        end.set(2007, Calendar.DECEMBER, 8, 0, 0, 0);
        event.setEndDate(DateTimeUtil.getDateTime(end));

        String rrule = RRuleBuilder.create().addFrequency(RecurringFrequency.YEARLY).addInterval(1).addByMonth(Calendar.DECEMBER).addByMonthDay(7).build();
        event.setRrule(rrule);
        EventData createEvent = eventManager.createEvent(event, true);

        Calendar cal = Calendar.getInstance();
        Date from = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date until = cal.getTime();
        List<EventData> allEvents = eventManager.getAllEvents(from, until, true, folderId);
        assertEquals(1, allEvents.size());
        assertEquals(createEvent.getId(), allEvents.get(0).getId());
    }

}
