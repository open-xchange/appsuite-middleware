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
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.testing.httpclient.models.EventData;


/**
 * {@link Bug64836Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class Bug64836Test extends AbstractChronosTest {
    
    public Bug64836Test() {
        super();
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        eventManager.setIgnoreConflicts(true);        
    }
    
    @Test
    public void testBug() throws Exception {
        EventData series = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "Bug 64836 Test", 2, defaultFolderId);
        EventData createdSeries = eventManager.createEvent(series);
        TimeZone timeZone = TimeZone.getTimeZone(createdSeries.getStartDate().getTzid());
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> events = eventManager.getAllEvents(createdSeries.getFolder(), from, until, true);
        events = getEventsByUid(events, createdSeries.getUid());
        assertEquals(2, events.size());

        String exceptionSummary = createdSeries.getSummary() + " first";
        for (EventData occurrence : events) {
            EventData exception = new EventData();
            exception.setSummary(exceptionSummary);
            exception.setFolder(occurrence.getFolder());
            exception.setId(occurrence.getId());
            exception.setRecurrenceId(occurrence.getRecurrenceId());
            eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), true);
            exceptionSummary = createdSeries.getSummary() + " second";
        }
        
        eventManager.getEvent(defaultFolderId, createdSeries.getId());
    }
    
}
