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
import com.openexchange.ajax.chronos.factory.RRuleFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * 
 * {@link Bug13501Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug13501Test extends AbstractChronosTest {

    public Bug13501Test() {
        super();
    }

    @Test
    public void testBug13501() throws Exception {
        Date startSearch = new Date(1246233600000L); // 29.06.2009 00:00:00
        Date endSearch = new Date(1249257600000L); // 03.08.2009 00:00:00

        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 15);
        start.set(Calendar.MONTH, Calendar.JUNE);
        start.set(Calendar.YEAR, 2009);
        start.set(Calendar.HOUR_OF_DAY, 8);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(start.getTimeInMillis() + 3600000);

        EventData series = EventFactory.createSeriesEvent(getCalendaruser(), "Bug13501Test", DateTimeUtil.getDateTime(start), DateTimeUtil.getDateTime(end), 5);
        series.setFolder(folderId);
        EventData createEvent = eventManager.createEvent(series, true);

        createEvent.setRrule(RRuleFactory.getFrequencyWithOccurenceLimit(RecurringFrequency.WEEKLY, 5));

        eventManager.updateEvent(createEvent, false, false);

        List<EventData> allEvents = eventManager.getAllEvents(startSearch, endSearch, true, folderId);
        assertEquals(3, allEvents.size());
    }

}
