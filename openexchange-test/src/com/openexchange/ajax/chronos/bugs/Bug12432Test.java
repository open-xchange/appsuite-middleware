
package com.openexchange.ajax.chronos.bugs;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractSecondUserChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;

/**
 * 
 * {@link Bug12432Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class Bug12432Test extends AbstractSecondUserChronosTest {

    public Bug12432Test() {
        super();
    }

    @Test
    public void testFirstReservedThenFree() throws Throwable {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug12432Test", folderId);
        event.setTransp(TranspEnum.OPAQUE);
        eventManager.createEvent(event, true);
        event.setTransp(TranspEnum.TRANSPARENT);
        eventManager.createEvent(event, false);
    }

    @Test
    public void testFirstFreeThenReserved() throws Throwable {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug12432Test", folderId);
        event.setTransp(TranspEnum.TRANSPARENT);
        eventManager.createEvent(event, true);
        event.setTransp(TranspEnum.OPAQUE);
        eventManager.createEvent(event, false);
    }

    @Test
    public void testChangeFree() throws Throwable {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug12432Test", folderId);
        event.setTransp(TranspEnum.TRANSPARENT);
        EventData free = eventManager.createEvent(event, true);
        event.setTransp(TranspEnum.OPAQUE);
        eventManager.createEvent(event, false);

        free.setStartDate(DateTimeUtil.incrementDateTimeData(free.getStartDate(), -TimeUnit.MINUTES.toMillis(30)));
        free.setEndDate(DateTimeUtil.incrementDateTimeData(free.getEndDate(), TimeUnit.MINUTES.toMillis(30)));
        eventManager.updateEvent(free, false, true);
    }

    @Test
    public void testChangeReserved() throws Throwable {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug12432Test", folderId);
        event.setTransp(TranspEnum.TRANSPARENT);
        eventManager.createEvent(event, true);
        event.setTransp(TranspEnum.OPAQUE);
        EventData reserved = eventManager.createEvent(event, false);

        reserved.setStartDate(DateTimeUtil.incrementDateTimeData(reserved.getStartDate(), -TimeUnit.MINUTES.toMillis(30)));
        reserved.setEndDate(DateTimeUtil.incrementDateTimeData(reserved.getEndDate(), TimeUnit.MINUTES.toMillis(30)));
        eventManager.updateEvent(reserved, false, true);
    }

}
