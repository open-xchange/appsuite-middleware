
package com.openexchange.ajax.appointment.recurrence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.test.OXTestToolkit;

public class AbstractRecurrenceTest extends AppointmentTest {

    protected static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

    protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    protected SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    protected final static int[] _fields = { DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY, FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION, CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, Appointment.SHOWN_AS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.RECURRENCE_POSITION, Appointment.TIMEZONE};

    @Before
    public void setUp() throws Exception {
        super.setUp();
        simpleDateFormatUTC.setTimeZone(timeZoneUTC);
        simpleDateFormat.setTimeZone(timeZone);
    }

    protected Occurrence getOccurrenceByPosition(final Occurrence[] occurrenceArray, final int position) {
        for (int a = 0; a < occurrenceArray.length; a++) {
            if (occurrenceArray[a].getPosition() == position) {
                return occurrenceArray[a];
            }
        }
        return null;
    }

    public static void assertOccurrence(final int expectedPosition, final Date expectedStartDate, final Date expectedEndDate, final Occurrence occurrence) throws Exception {
        assertOccurrence(expectedPosition, expectedStartDate, expectedEndDate, occurrence, timeZoneUTC);
    }

    public static void assertOccurrence(final int expectedPosition, final Date expectedStartDate, final Date expectedEndDate, final Occurrence occurrence, final TimeZone timeZone) throws Exception {
        assertNotNull("occurrence is null", occurrence);
        assertEquals("position is not equals", expectedPosition, occurrence.getPosition());
        OXTestToolkit.assertEqualsAndNotNull("start date is not equals at position: " + expectedPosition, addOffsetToDate(expectedStartDate, timeZone), addOffsetToDate(occurrence.getStartDate(), timeZone));
        OXTestToolkit.assertEqualsAndNotNull("end date is not equals at position: " + expectedPosition, addOffsetToDate(expectedEndDate, timeZone), addOffsetToDate(occurrence.getEndDate(), timeZone));
    }

    public static Date addOffsetToDate(final Date value, final TimeZone timeZone) throws JSONException {
        final int offset = timeZone.getOffset(value.getTime());
        return new Date(value.getTime() + offset);
    }
}
