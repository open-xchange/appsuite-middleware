package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import com.openexchange.data.conversion.ical.*;

public class AbstractICalTest extends AbstractAJAXTest {
	
	protected static final String IMPORT_URL = "/ajax/import";
	
	protected static final String EXPORT_URL = "/ajax/export";
	
	protected Date startTime = null;
	
	protected Date endTime = null;
	
	protected int appointmentFolderId = -1;
	
	protected int taskFolderId = -1;
	
	protected int userId = -1;
	
	protected String emailaddress = null;
	
	protected TimeZone timeZone = null;

    protected Context ctx;

    private static final Log LOG = LogFactory.getLog(AbstractICalTest.class);
	
	public AbstractICalTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		final FolderObject appointmentFolderObj = FolderTest
				.getStandardCalendarFolder(getWebConversation(),
				getHostName(), getSessionId());
		appointmentFolderId = appointmentFolderObj.getObjectID();
		
		final FolderObject taskFolderObj = FolderTest
				.getStandardTaskFolder(getWebConversation(),
				getHostName(), getSessionId());
		taskFolderId = taskFolderObj.getObjectID();
		
		userId = appointmentFolderObj.getCreatedBy();
		
		timeZone = ConfigTools.getTimeZone(getWebConversation(),
				getHostName(), getSessionId());
		
		LOG.debug(new StringBuilder().append("use timezone: ").append(
				timeZone).toString());
		
		final ContactObject contactObj = ContactTest.loadUser(getWebConversation(), userId, FolderObject.SYSTEM_LDAP_FOLDER_ID, getHostName(), getSessionId());
		emailaddress = contactObj.getEmail1();
		
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(timeZone);
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		startTime = c.getTime();
		// startTime.setTime(sta + timeZone.getOffset(startTime.getTime()));
		endTime = new Date(startTime.getTime() + 3600000);

        ctx = null; // FIXME
    }
	
	public static ImportResult[] importICal(final WebConversation webCon, final AppointmentObject[] appointmentObj, final int folderId, final TimeZone timeZone, final String emailaddress, final String host, final String session) throws Exception, TestException {
		return importICal(webCon, appointmentObj, null, folderId, -1, timeZone, emailaddress, host, session);
	}
	
	public static ImportResult[] importICal(final WebConversation webCon, final Task[] taskObj, final int folderId, final TimeZone timeZone, final String emailaddress, final String host, final String session) throws Exception, TestException {
		return importICal(webCon, null, taskObj, -1, folderId, timeZone, emailaddress, host, session);
	}
	
	public static ImportResult[] importICal(final WebConversation webCon, final AppointmentObject[] appointmentObj, final Task[] taskObj, final int appointmentFolderId, final int taskFolderId, final TimeZone timeZone, final String emailaddress, String host, final String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		ICalEmitter emitter = new ICal4JEmitter();
		ICalSession icalSession = emitter.createSession();
		if (appointmentObj != null) {
			for (int a = 0; a < appointmentObj.length; a++) {
				emitter.writeAppointment(icalSession, appointmentObj[a], null, new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
			}
		}
		
		if (taskObj != null) {
			for (int a = 0; a < taskObj.length; a++) {
				emitter.writeTask(icalSession, taskObj[a], null, new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
			}
		}
		
		emitter.writeSession(icalSession, byteArrayOutputStream);
		
		return importICal(webCon, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), appointmentFolderId, taskFolderId, timeZone, emailaddress, host, session);
	}
		
	public static ImportResult[] importICal(final WebConversation webCon, final ByteArrayInputStream byteArrayInputStream, final int appointmentFolderId, final int taskFolderId, final TimeZone timeZone, final String emailaddress, String host, final String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, Format.ICAL.getConstantName());
		if (appointmentFolderId != -1) {
			parameter.setParameter("folder", appointmentFolderId);
		}
		
		if (taskFolderId != -1) {
			parameter.setParameter("folder", taskFolderId);
		}
		
		final WebRequest req = new PostMethodWebRequest(host + "/ajax/import" + parameter.getURLParameters());
		
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "ical-test.ics", byteArrayInputStream, Format.ICAL.getMimeType());
		
		final WebResponse resp = webCon.getResource(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final JSONObject response = extractFromCallback( resp.getText() );
		
		final JSONArray jsonArray = response.getJSONArray("data");
		
		assertNotNull("json array in response is null", jsonArray);
		
		final ImportResult[] importResult = new ImportResult[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			final JSONObject jsonObj = jsonArray.getJSONObject(a);
			
			if (jsonObj.has("error")) {
				importResult[a] = new ImportResult();
				importResult[a].setException(new OXException( jsonObj.getString("error")));
			} else {
                final String objectId = jsonObj.getString("id");
				final String folder = jsonObj.getString("folder_id");
				final long timestamp = jsonObj.getLong("last_modified");
			
				importResult[a] = new ImportResult(objectId, folder, timestamp);
			} 
		}
		
		return importResult;
	}
	
	public AppointmentObject[] exportAppointment(final WebConversation webCon, final int inFolder, final String mailaddress, final TimeZone timeZone, String host, final String session, Context ctx) throws Exception, TestException {
		host = appendPrefix(host);
		
		final String contentType = "text/calendar";
		
		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter("folder", appointmentFolderId);
		parameter.setParameter("action", Format.ICAL.getConstantName());
		
		final WebRequest req = new GetMethodWebRequest(host + "/ajax/export" + parameter.getURLParameters());
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resp.getText().getBytes());

		List<CalendarDataObject> exportData = null;
		try {
            ICalParser parser = new ICal4JParser();
            List<ConversionError> errors = new ArrayList<ConversionError>();
            List<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
            exportData = parser.parseAppointments(byteArrayInputStream, timeZone, ctx, errors, warnings);
        } catch (final Exception exc) {
            exc.printStackTrace();
            System.out.println("error: " + exc);
            exportData = new ArrayList<CalendarDataObject>();
        }
		
		return exportData.toArray(new AppointmentObject[exportData.size()]);
	}
	
	public Task[] exportTask(final WebConversation webCon, final int inFolder, final String mailaddress, final TimeZone timeZone, String host, final String session, Context ctx) throws Exception, TestException {
		host = appendPrefix(host);
		
		final String contentType = "text/calendar";
		
		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter("action", Format.ICAL.getConstantName());
		parameter.setParameter("folder", taskFolderId);
		parameter.setParameter("type", Types.TASK);
		
		final WebRequest req = new GetMethodWebRequest(host + EXPORT_URL + parameter.getURLParameters());
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resp.getText().getBytes());
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, mailaddress);
		
		List<Task> exportData = new ArrayList<Task>();
		
		try {
			ICalParser parser = new ICal4JParser();
            List<ConversionError> errors = new ArrayList<ConversionError>();
            List<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
            exportData = parser.parseTasks(byteArrayInputStream, timeZone, ctx, errors, warnings);
		} catch (final Exception exc) {
			System.out.println("error: " + exc);
		}
		
		return exportData.toArray(new Task[exportData.size()]);
	}
}