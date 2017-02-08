
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertFalse;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.groupware.container.Appointment;

public class MultipleTest extends AppointmentTest {

    @Test
    public void testMultipleInsert() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testMultipleInsert");
        appointmentObj.setIgnoreConflicts(true);

        final InsertRequest insertRequest1 = new InsertRequest(appointmentObj, timeZone, true);
        final InsertRequest insertRequest2 = new InsertRequest(appointmentObj, timeZone, true);
        final InsertRequest insertRequest3 = new InsertRequest(appointmentObj, timeZone, true);

        final MultipleRequest multipleInsertRequest = MultipleRequest.create(new AJAXRequest[] { insertRequest1, insertRequest2, insertRequest3 });
        final MultipleResponse multipleInsertResponse = (MultipleResponse) Executor.execute(getClient(), multipleInsertRequest);

        assertFalse("first insert request has errors: ", multipleInsertResponse.getResponse(0).hasError());
        assertFalse("second insert request has errors: ", multipleInsertResponse.getResponse(1).hasError());
        assertFalse("third insert request has errors: ", multipleInsertResponse.getResponse(2).hasError());

        final int objectId1 = ((CommonInsertResponse) multipleInsertResponse.getResponse(0)).getId();
        final int objectId2 = ((CommonInsertResponse) multipleInsertResponse.getResponse(1)).getId();
        final int objectId3 = ((CommonInsertResponse) multipleInsertResponse.getResponse(2)).getId();

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId3);
        final Date modified = loadAppointment.getLastModified();

        final DeleteRequest deleteRequest1 = new DeleteRequest(objectId1, appointmentFolderId, modified);
        final DeleteRequest deleteRequest2 = new DeleteRequest(objectId2, appointmentFolderId, modified);
        final DeleteRequest deleteRequest3 = new DeleteRequest(objectId3, appointmentFolderId, modified);

        MultipleRequest.create(new AJAXRequest[] { deleteRequest1, deleteRequest2, deleteRequest3 });
        final MultipleResponse multipleDeleteResponse = (MultipleResponse) Executor.execute(getClient(), multipleInsertRequest);

        assertFalse("first delete request has errors: ", multipleDeleteResponse.getResponse(0).hasError());
        assertFalse("second delete request has errors: ", multipleDeleteResponse.getResponse(1).hasError());
        assertFalse("third delete request has errors: ", multipleDeleteResponse.getResponse(2).hasError());
    }
}
