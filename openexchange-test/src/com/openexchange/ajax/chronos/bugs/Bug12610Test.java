
package com.openexchange.ajax.chronos.bugs;

import java.util.Calendar;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.RRuleFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * 
 * {@link Bug12610Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug12610Test extends AbstractChronosTest {

    public Bug12610Test() {
        super();
    }

    @Test
    public void testChangeYearlySeries() throws Throwable {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug12610Test", folderId);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 3);
        event.setRrule(RRuleFactory.getFrequencyWithUntilLimit(RecurringFrequency.YEARLY, DateTimeUtil.getZuluDateTime(cal.getTimeInMillis())));
        
        EventData createEvent = eventManager.createEvent(event, true);
        cal.add(Calendar.YEAR, 2);
        createEvent.setRrule(RRuleFactory.getFrequencyWithUntilLimit(RecurringFrequency.YEARLY, DateTimeUtil.getZuluDateTime(cal.getTimeInMillis())));
        eventManager.updateEvent(createEvent, false, false);
    }
}
