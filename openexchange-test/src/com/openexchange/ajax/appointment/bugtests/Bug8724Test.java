
package com.openexchange.ajax.appointment.bugtests;

import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.framework.ListIDInt;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

public class Bug8724Test extends AppointmentTest {

    private final static int[] _appointmentFields = { DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY, FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION, CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.TIMEZONE
    };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug8724Test.class);

    /**
     * This test checks if the list action return an object not found exception
     * if one id is requested trhat doesn't exist
     */
    @Test
    public void testBug8724_I() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testBug8724_I");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        final Appointment appointmentListObject = new Appointment();
        appointmentListObject.setObjectID(objectId + 1000);
        appointmentListObject.setParentFolderID(appointmentFolderId);

        catm.list(new ListIDs(appointmentFolderId, objectId + 1000), _appointmentFields);

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
    }

    /**
     * This test checks if the list action return an object not found exception
     * if one id is requested trhat doesn't exist
     */
    @Test
    public void testBug8724_II() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testBug8724_II");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        final Appointment appointmentListObject1 = new Appointment();
        appointmentListObject1.setObjectID(objectId + 1000);
        appointmentListObject1.setParentFolderID(appointmentFolderId);

        final Appointment appointmentListObject2 = new Appointment();
        appointmentListObject2.setObjectID(objectId + 1001);
        appointmentListObject2.setParentFolderID(appointmentFolderId);

        ListIDs foldersAndIds = new ListIDs(appointmentFolderId, objectId + 1000);
        foldersAndIds.add(new ListIDInt(appointmentFolderId, objectId + 1001));
        catm.list(foldersAndIds, _appointmentFields);

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
    }
}
