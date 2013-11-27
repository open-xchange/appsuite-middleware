package com.openexchange.webdav.xml.appointment.recurrence;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.webdav.xml.AppointmentTest;

public class AbstractRecurrenceTest extends AppointmentTest {

	protected static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

	protected SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private static final Log LOG = com.openexchange.log.Log.loggerFor(AbstractRecurrenceTest.class);

	public AbstractRecurrenceTest(final String name) {
		super(name);
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
	}
}

