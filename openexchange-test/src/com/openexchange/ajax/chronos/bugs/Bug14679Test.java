
package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.EventFactory.Weekday;
import com.openexchange.ajax.chronos.factory.RRuleFactory.RRuleBuilder;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug14679Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug14679Test extends AbstractChronosTest {

    public Bug14679Test() {
        super();
    }

    @Test
    public void testBug() throws Exception {

        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug14679Test", folderId);
        Calendar start = Calendar.getInstance();
        start.set(2009, 9, 19, 12, 30, 0);
        event.setStartDate(DateTimeUtil.getDateTime(start));
        Calendar end = Calendar.getInstance();
        end.set(2009, 9, 19, 13, 30, 0);
        event.setEndDate(DateTimeUtil.getDateTime(end));

        Calendar rruleCal = Calendar.getInstance();
        rruleCal.set(2009, 11, 30, 0, 0, 0);
        String rrule = RRuleBuilder.create().addFrequency(RecurringFrequency.WEEKLY).addByDay(Weekday.MO, Weekday.WE).addInterval(1).addUntil(DateTimeUtil.getZuluDateTime(rruleCal.getTimeInMillis())).build();
        event.setRrule(rrule); // "FREQ=WEEKLY;BYDAY=MO,WE;INTERVAL=1;UNTIL=20091230T000000Z"
        EventData createEvent = eventManager.createEvent(event);

        String rrule2 = RRuleBuilder.create().addFrequency(RecurringFrequency.WEEKLY).addByDay(Weekday.MO, Weekday.WE).addInterval(2).addUntil(DateTimeUtil.getZuluDateTime(rruleCal.getTimeInMillis())).build();
        createEvent.setRrule(rrule2); // "FREQ=WEEKLY;BYDAY=MO,WE;INTERVAL=2;UNTIL=20091230T000000Z"
        eventManager.updateEvent(createEvent, false, false);

        Calendar from = Calendar.getInstance();
        from.set(2009, 10, 1, 0, 0, 0);

        Calendar until = Calendar.getInstance();
        until.set(2009, 11, 1, 0, 0, 0);

        List<EventData> allEvents = eventManager.getAllEvents(from.getTime(), until.getTime(), true, folderId);
        assertEquals("Wrong amount of occurrences.", 5, allEvents.size());
    }

}
