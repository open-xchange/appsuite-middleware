
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertFalse;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.groupware.container.Contact;

public class MultipleTest extends ContactTest {

    @Test
    public void testMultipleInsert() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("testMultipleInsert");
        contactObj.setParentFolderID(contactFolderId);

        final InsertRequest insertRequest1 = new InsertRequest(contactObj, true);
        final InsertRequest insertRequest2 = new InsertRequest(contactObj, true);
        final InsertRequest insertRequest3 = new InsertRequest(contactObj, true);

        final MultipleRequest<InsertResponse> multipleInsertRequest = MultipleRequest.create(new InsertRequest[] { insertRequest1, insertRequest2, insertRequest3 });
        final MultipleResponse<?> multipleInsertResponse = Executor.execute(getClient(), multipleInsertRequest);

        assertFalse("first insert request has errors: ", multipleInsertResponse.getResponse(0).hasError());
        assertFalse("second insert request has errors: ", multipleInsertResponse.getResponse(1).hasError());
        assertFalse("third insert request has errors: ", multipleInsertResponse.getResponse(2).hasError());

        final int objectId1 = ((InsertResponse) multipleInsertResponse.getResponse(0)).getId();
        final int objectId2 = ((InsertResponse) multipleInsertResponse.getResponse(1)).getId();
        final int objectId3 = ((InsertResponse) multipleInsertResponse.getResponse(2)).getId();

        // final ContactObject loadContact = Appointment(getWebConversation(), objectId3, appointmentFolderId, timeZone, getHostName(),
        // getSessionId());
        final Date modified = null; // loadAppointment.getLastModified();

        final DeleteRequest deleteRequest1 = new DeleteRequest(contactFolderId, objectId1, modified);
        final DeleteRequest deleteRequest2 = new DeleteRequest(contactFolderId, objectId2, modified);
        final DeleteRequest deleteRequest3 = new DeleteRequest(contactFolderId, objectId3, modified);

        MultipleRequest.create(new AJAXRequest[] { deleteRequest1, deleteRequest2, deleteRequest3 });
        final MultipleResponse<?> multipleDeleteResponse = Executor.execute(getClient(), multipleInsertRequest);

        assertFalse("first delete request has errors: ", multipleDeleteResponse.getResponse(0).hasError());
        assertFalse("second delete request has errors: ", multipleDeleteResponse.getResponse(1).hasError());
        assertFalse("third delete request has errors: ", multipleDeleteResponse.getResponse(2).hasError());
    }
}
