
package com.openexchange.ajax.appointment.bugtests;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.UpdatesRequest;
import com.openexchange.ajax.appointment.action.AppointmentUpdatesResponse;
import com.openexchange.ajax.contact.action.ContactUpdatesResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.TaskUpdatesResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */

public class Bug16292Test extends AbstractAJAXSession {

    private AJAXClient client;

    private int appointmentFolder;

    private int taskFolder;

    private int contactFolder;

    private TimeZone tz;

    private Calendar calendar;

    private Appointment appointment;

    private Task task;

    private Contact contact;

    public Bug16292Test(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        appointmentFolder = client.getValues().getPrivateAppointmentFolder();
        taskFolder = client.getValues().getPrivateTaskFolder();
        contactFolder = client.getValues().getPrivateContactFolder();
        tz = client.getValues().getTimeZone();
        calendar = TimeTools.createCalendar(tz);

        appointment = createAppointment();
        task = createTask();
        contact = createContact();
    }

    public void testAppointmentAtGet() throws Exception {
        final GetRequest appointmentGetReq = new GetRequest(appointment, false);
        final GetResponse appointmentGetResp = client.execute(appointmentGetReq);
        final JSONObject appointmentJSON = (JSONObject) appointmentGetResp.getData();
        assertTrue("Appointment: Number of attachments is null after GetRequest.", (appointmentJSON.get("number_of_attachments") != null));
    }

    public void testTaskAtGet() throws Exception {
        final com.openexchange.ajax.task.actions.GetRequest taskGetReq = new com.openexchange.ajax.task.actions.GetRequest(task, tz);
        final com.openexchange.ajax.task.actions.GetResponse taskGetResp = client.execute(taskGetReq);
        final JSONObject taskJSON = (JSONObject) taskGetResp.getData();
        assertTrue("Task: Number of attachments is null after GetRequest.", (taskJSON.get("number_of_attachments") != null));
    }

    public void testContactAtGet() throws Exception {
        final com.openexchange.ajax.contact.action.GetRequest contactGetReq = new com.openexchange.ajax.contact.action.GetRequest(
            contact,
            tz);
        final com.openexchange.ajax.contact.action.GetResponse contactGetResp = client.execute(contactGetReq);
        final JSONObject contactJSON = (JSONObject) contactGetResp.getData();
        assertTrue("Contact: Number of attachments is null after GetRequest.", (contactJSON.get("number_of_attachments") != null));
    }

    public void testAppointmentAtList() throws Exception {
        final int[] appFields = { Appointment.NUMBER_OF_ATTACHMENTS, Appointment.TITLE, Appointment.OBJECT_ID };
        final ListRequest appointmentListReq = new ListRequest(
            new ListIDs(appointment.getParentFolderID(), appointment.getObjectID()),
            appFields,
            false);
        final CommonListResponse appointmentListResp = client.execute(appointmentListReq);

        assertNOANotNull(
            appointmentListResp,
            "Appointment",
            "ListRequest",
            Appointment.OBJECT_ID,
            Appointment.NUMBER_OF_ATTACHMENTS,
            appointment.getObjectID());
    }

    public void testTaskAtList() throws Exception {
        final int[][] ids = { { taskFolder, task.getObjectID() } };
        final int[] taskFields = { Task.OBJECT_ID, Task.TITLE, Task.NUMBER_OF_ATTACHMENTS };
        final com.openexchange.ajax.task.actions.ListRequest taskListReq = new com.openexchange.ajax.task.actions.ListRequest(
            ids,
            taskFields,
            false);
        final CommonListResponse taskListResp = client.execute(taskListReq);

        assertNOANotNull(taskListResp, "Task", "ListRequest", Task.OBJECT_ID, Task.NUMBER_OF_ATTACHMENTS, task.getObjectID());
    }

    public void testContactAtList() throws Exception {
        final int[] conFields = { Contact.OBJECT_ID, Contact.TITLE, Contact.NUMBER_OF_ATTACHMENTS };
        final com.openexchange.ajax.contact.action.ListRequest contactListReq = new com.openexchange.ajax.contact.action.ListRequest(
            new ListIDs(contactFolder, contact.getObjectID()),
            conFields);
        final CommonListResponse contactListResp = client.execute(contactListReq);

        assertNOANotNull(contactListResp, "Contact", "ListRequest", Contact.OBJECT_ID, Contact.NUMBER_OF_ATTACHMENTS, contact.getObjectID());
    }

    public void testAppointmentAtAll() throws Exception {
        final int[] appFields = { Appointment.NUMBER_OF_ATTACHMENTS, Appointment.TITLE, Appointment.OBJECT_ID };
        final AllRequest appointmentAllReq = new AllRequest(
            appointmentFolder,
            appFields,
            appointment.getStartDate(),
            appointment.getEndDate(),
            tz);
        final CommonAllResponse appointmentAllResp = client.execute(appointmentAllReq);

        assertNOANotNull(
            appointmentAllResp,
            "Appointment",
            "AllRequest",
            Appointment.OBJECT_ID,
            Appointment.NUMBER_OF_ATTACHMENTS,
            appointment.getObjectID());
    }

    public void testTaskAtAll() throws Exception {
        final int[] taskFields = { Task.OBJECT_ID, Task.NUMBER_OF_ATTACHMENTS, Task.TITLE };
        final com.openexchange.ajax.task.actions.AllRequest taskAllReq = new com.openexchange.ajax.task.actions.AllRequest(
            taskFolder,
            taskFields,
            Task.START_DATE,
            Order.ASCENDING);
        final CommonAllResponse taskAllResp = client.execute(taskAllReq);

        assertNOANotNull(taskAllResp, "Task", "AllRequest", Task.OBJECT_ID, Task.NUMBER_OF_ATTACHMENTS, task.getObjectID());
    }

    public void testContactAtAll() throws Exception {
        final int[] contactFields = { Contact.OBJECT_ID, Contact.NUMBER_OF_ATTACHMENTS, Contact.TITLE };
        final com.openexchange.ajax.contact.action.AllRequest contactAllReq = new com.openexchange.ajax.contact.action.AllRequest(
            contactFolder,
            contactFields);
        final CommonAllResponse contactAllResp = client.execute(contactAllReq);

        assertNOANotNull(contactAllResp, "Contact", "AllRequest", Contact.OBJECT_ID, Contact.NUMBER_OF_ATTACHMENTS, contact.getObjectID());
    }

    public void testAppointmentAtUpdates() throws Exception {
        final int[] appFields = { Appointment.NUMBER_OF_ATTACHMENTS, Appointment.TITLE, Appointment.OBJECT_ID };
        final UpdatesRequest appointmentUpdatesRequest = new UpdatesRequest(appointmentFolder, appFields, new Date(
            appointment.getLastModified().getTime() - 1), false);
        final AppointmentUpdatesResponse appointmentUpdatesResp = client.execute(appointmentUpdatesRequest);

        assertNOANotNull(
            appointmentUpdatesResp,
            "Appointment",
            "UpdatesRequest",
            Appointment.OBJECT_ID,
            Appointment.NUMBER_OF_ATTACHMENTS,
            appointment.getObjectID());
    }

    public void testTaskAtUpdates() throws Exception {
        final int[] taskFields = { Task.OBJECT_ID, Task.NUMBER_OF_ATTACHMENTS, Task.TITLE };
        final com.openexchange.ajax.task.actions.UpdatesRequest taskUpdatesReq = new com.openexchange.ajax.task.actions.UpdatesRequest(
            taskFolder,
            taskFields,
            Task.START_DATE,
            Order.ASCENDING,
            new Date(task.getLastModified().getTime() - 1));
        final TaskUpdatesResponse taskUpdatesResp = client.execute(taskUpdatesReq);

        assertNOANotNull(taskUpdatesResp, "Task", "UpdatesRequest", Task.OBJECT_ID, Task.NUMBER_OF_ATTACHMENTS, task.getObjectID());
    }

    public void testContactAtUpdates() throws Exception {
        final int[] contactFields = { Contact.OBJECT_ID, Contact.NUMBER_OF_ATTACHMENTS, Contact.TITLE };
        final com.openexchange.ajax.contact.action.UpdatesRequest contactUpdatesReq = new com.openexchange.ajax.contact.action.UpdatesRequest(
            contactFolder,
            contactFields,
            Contact.OBJECT_ID,
            Order.DESCENDING,
            new Date(contact.getLastModified().getTime() - 1));
        final ContactUpdatesResponse contactUpdatesResp = client.execute(contactUpdatesReq);

        assertNOANotNull(
            contactUpdatesResp,
            "Contact",
            "UpdatesRequest",
            Contact.OBJECT_ID,
            Contact.NUMBER_OF_ATTACHMENTS,
            contact.getObjectID());
    }

    private void assertNOANotNull(final AbstractColumnsResponse resp, final String type, final String reqType, final int objIdColumn, final int noaColumn, final int objId) {
        final Iterator<Object> it = resp.iterator(objIdColumn);
        int i = 0;
        while (it.hasNext()) {
            final int actual = (Integer) it.next();
            if (actual == appointment.getObjectID()) {
                assertNotNull(type + ": Number of attachments is null after " + reqType, resp.getValue(i, noaColumn));
            }
            i++;
        }
    }

    @Override
    public void tearDown() throws Exception {
        // Delete Appointment
        client.execute(new DeleteRequest(appointment, false));

        // Delete Task
        client.execute(new com.openexchange.ajax.task.actions.DeleteRequest(task, false));

        // Delete Contact
        client.execute(new com.openexchange.ajax.contact.action.DeleteRequest(contact, false));

        super.tearDown();
    }

    private Appointment createAppointment() throws OXException, IOException, SAXException, JSONException {
        final Calendar cal = (Calendar) calendar.clone();
        final Appointment appointmentObj = new Appointment();

        appointmentObj.setTitle("testBug16292");
        cal.add(Calendar.DAY_OF_MONTH, 1);
        appointmentObj.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(cal.getTime());
        appointmentObj.setAlarm(15);

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolder);
        appointmentObj.setRecurrenceType(Appointment.NO_RECURRENCE);
        appointmentObj.setIgnoreConflicts(true);

        final AppointmentInsertResponse insReq = client.execute(new InsertRequest(appointmentObj, tz, false));
        insReq.fillAppointment(appointmentObj);

        return appointmentObj;
    }

    private Task createTask() throws OXException, IOException, SAXException, JSONException {
        final Calendar cal = (Calendar) calendar.clone();
        final Task taskObj = new Task();

        taskObj.setTitle("testBug16292");
        taskObj.setParentFolderID(taskFolder);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        taskObj.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        taskObj.setEndDate(cal.getTime());

        taskObj.setRecurrenceType(Task.NO_RECURRENCE);
        taskObj.setPercentComplete(75);
        taskObj.setStatus(Task.IN_PROGRESS);

        final com.openexchange.ajax.task.actions.InsertRequest insReq = new com.openexchange.ajax.task.actions.InsertRequest(
            taskObj,
            tz,
            false);
        final InsertResponse insResp = client.execute(insReq);
        insResp.fillTask(taskObj);

        return taskObj;
    }

    private Contact createContact() throws OXException, IOException, SAXException, JSONException {
        final Contact contactObj = new Contact();

        contactObj.setSurName("Meier");
        contactObj.setGivenName("Herbert");
        contactObj.setStreetBusiness("Franz-Meier Weg 17");
        contactObj.setCityBusiness("Test Stadt");
        contactObj.setStateBusiness("NRW");
        contactObj.setCountryBusiness("Deutschland");
        contactObj.setTelephoneBusiness1("+49112233445566");
        contactObj.setCompany("Internal Test AG");
        contactObj.setEmail1("hebert.meier@open-xchange.com");
        contactObj.setParentFolderID(contactFolder);

        final com.openexchange.ajax.contact.action.InsertRequest insReq = new com.openexchange.ajax.contact.action.InsertRequest(
            contactObj,
            false);
        final com.openexchange.ajax.contact.action.InsertResponse insResp = client.execute(insReq);
        insResp.fillObject(contactObj);

        return contactObj;
    }
}
