
package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug10733Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug10733Test extends AbstractChronosTest {

    @Test
    public void testBug10733() throws Exception {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("testBug10733");
        stringBuffer.append(" - ");
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars

        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), stringBuffer.toString(), folderId);
        ChronosCalendarResultResponse response = chronosApi.createEvent(getSessionId(), folderId, event, Boolean.FALSE, null, Boolean.FALSE, null, null, null, null, null);
        assertNotNull(response.getError());
        assertEquals("Unexpected error message", "CAL-5070", response.getCode());
    }
}
