
package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.testing.httpclient.models.EventData;

public class Bug11250Test extends AbstractChronosTest {

    @Test
    public void testBug11250() throws Exception {
        Calendar start = DateTimeUtil.getUTCCalendar();
        Date from = start.getTime();
        start.add(Calendar.DAY_OF_MONTH, 1);
        long start1 = start.getTimeInMillis();
        start.add(Calendar.DAY_OF_MONTH, 1);
        long start2 = start.getTimeInMillis();
        start.add(Calendar.DAY_OF_MONTH, 1);
        long start3 = start.getTimeInMillis();

        EventData event = EventFactory.createSingleEvent(getCalendaruser(), "testBug11250_1", DateTimeUtil.getDateTime(start1), DateTimeUtil.getDateTime(start1 + TimeUnit.HOURS.toMillis(1)));
        event.setFolder(folderId);
        EventData event1 = eventManager.createEvent(event);

        event = EventFactory.createSingleEvent(getCalendaruser(), "testBug11250_2", DateTimeUtil.getDateTime(start3), DateTimeUtil.getDateTime(start3 + TimeUnit.HOURS.toMillis(1)));
        event.setFolder(folderId);
        EventData event3 = eventManager.createEvent(event);

        event = EventFactory.createSeriesEvent(getCalendaruser(), "testBug11250_3", DateTimeUtil.getDateTime(start2), DateTimeUtil.getDateTime(start2 + TimeUnit.HOURS.toMillis(1)), 2);
        event.setFolder(folderId);
        EventData event2 = eventManager.createEvent(event, true);

        SortOrder sortOrder = SortOrder.getSortOrder(EventField.START_DATE, Order.ASC);
        List<EventData> allEvents = eventManager.getAllEvents(from, new Date(start3 + TimeUnit.DAYS.toMillis(1)), true, folderId, sortOrder);

        assertEquals("Event array size not equals", 4, allEvents.size());

        assertEquals("appointment id at position 1 not equals", event1.getSummary(), allEvents.get(0).getSummary());
        assertEquals("appointment id at position 2 not equals", event2.getSummary(), allEvents.get(1).getSummary());
        assertEquals("appointment id at position 3 not equals", event3.getSummary(), allEvents.get(2).getSummary());
        assertEquals("appointment id at position 4 not equals", event2.getSummary(), allEvents.get(3).getSummary());
    }
}
