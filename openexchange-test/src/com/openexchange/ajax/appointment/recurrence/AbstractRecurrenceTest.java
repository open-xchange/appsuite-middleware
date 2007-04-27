package com.openexchange.ajax.appointment.recurrence;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbstractRecurrenceTest extends AppointmentTest {
	
	protected static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
	
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
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
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
		assertNotNull("occurrence is null", occurrence);
		assertEquals("position is not equals", expectedPosition, occurrence.getPosition());
		assertEqualsAndNotNull("start date is not equals at position: " + expectedPosition, expectedStartDate, occurrence.getStartDate());
		assertEqualsAndNotNull("end date is not equals at position: " + expectedPosition, expectedEndDate, occurrence.getEndDate());
	}
}

