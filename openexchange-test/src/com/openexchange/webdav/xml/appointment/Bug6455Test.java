
package com.openexchange.webdav.xml.appointment;

import static org.junit.Assert.fail;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;

public class Bug6455Test extends AppointmentTest {

    @Test
    public void testBug6455() throws Exception {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("testBug6455");
        stringBuffer.append(" - ");
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
        stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle(stringBuffer.toString());
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        int objectId = 0;

        try {
            objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), "APP-0072");
        }

        if (objectId > 0) {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
        }
    }
}
