
package com.openexchange.webdav.xml.appointment.recurrence;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import com.openexchange.webdav.xml.AppointmentTest;

public class AbstractRecurrenceTest extends AppointmentTest {

    protected static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

    protected SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractRecurrenceTest.class);

    public AbstractRecurrenceTest() {
        super();
        simpleDateFormatUTC.setTimeZone(timeZoneUTC);
    }
}
