package com.openexchange.ajax.appointment.recurrence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.test.OXTestToolkit;

public class AbstractRecurrenceTest extends AppointmentTest {
	
	protected static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
	
	protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	protected SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private static final Log LOG = LogFactory.getLog(AbstractRecurrenceTest.class);
	
	protected final static int[] _fields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		AppointmentObject.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL,
		AppointmentObject.RECURRENCE_POSITION,
		CalendarDataObject.TIMEZONE
	};
	
	public AbstractRecurrenceTest(String name) {
		super(name);
	}
	
	public void setUp() throws Exception {
		super.setUp();
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
		simpleDateFormat.setTimeZone(timeZone);
	}
	
	protected Occurrence getOccurrenceByPosition(Occurrence[] occurrenceArray, int position) {
		for (int a = 0; a < occurrenceArray.length; a++) {
			if (occurrenceArray[a].getPosition() == position) {
				return occurrenceArray[a];
			}
		}
		return null;
	}
	
	public static void assertOccurrence(int expectedPosition, Date expectedStartDate, Date expectedEndDate, Occurrence occurrence) throws Exception {
		assertOccurrence(expectedPosition, expectedStartDate, expectedEndDate, occurrence, timeZoneUTC);
	}
	
	public static void assertOccurrence(int expectedPosition, Date expectedStartDate, Date expectedEndDate, Occurrence occurrence, TimeZone timeZone) throws Exception {
		assertNotNull("occurrence is null", occurrence);
		assertEquals("position is not equals", expectedPosition, occurrence.getPosition());
		OXTestToolkit.assertEqualsAndNotNull("start date is not equals at position: " + expectedPosition, addOffsetToDate(expectedStartDate, timeZone), addOffsetToDate(occurrence.getStartDate(), timeZone));
		OXTestToolkit.assertEqualsAndNotNull("end date is not equals at position: " + expectedPosition, addOffsetToDate(expectedEndDate, timeZone), addOffsetToDate(occurrence.getEndDate(), timeZone));
	}
	
	public static Date addOffsetToDate(final Date value, final TimeZone timeZone) throws JSONException {
		final int offset = timeZone.getOffset(value.getTime());
		return new Date(value.getTime()+offset);
	}
}

