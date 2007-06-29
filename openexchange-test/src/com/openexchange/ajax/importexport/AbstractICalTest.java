package com.openexchange.ajax.importexport;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.container.Response;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
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
	
	private static final Log LOG = LogFactory.getLog(AbstractICalTest.class);
	
	public AbstractICalTest(String name) {
		super(name);
	}
	
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
		
		ContactObject contactObj = ContactTest.loadUser(getWebConversation(), userId, FolderObject.SYSTEM_LDAP_FOLDER_ID, getHostName(), getSessionId());
		emailaddress = contactObj.getEmail1();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(timeZone);
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		startTime = c.getTime();
		// startTime.setTime(sta + timeZone.getOffset(startTime.getTime()));
		endTime = new Date(startTime.getTime() + 3600000);
	}
	
	public static ImportResult[] importICal(WebConversation webCon, AppointmentObject[] appointmentObj, int folderId, TimeZone timeZone, String emailaddress, String host, String session) throws Exception, TestException {
		return importICal(webCon, appointmentObj, null, folderId, -1, timeZone, emailaddress, host, session);
	}
	
	public static ImportResult[] importICal(WebConversation webCon, Task[] taskObj, int folderId, TimeZone timeZone, String emailaddress, String host, String session) throws Exception, TestException {
		return importICal(webCon, null, taskObj, -1, folderId, timeZone, emailaddress, host, session);
	}
	
	public static ImportResult[] importICal(WebConversation webCon, AppointmentObject[] appointmentObj, Task[] taskObj, int appointmentFolderId, int taskFolderId, TimeZone timeZone, String emailaddress, String host, String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
		VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
		final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
		versitDefinition.writeProperties(versitWriter, versitObjectContainer);
		final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");
		final VersitDefinition taskDef = versitDefinition.getChildDef("VTODO");
		OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, emailaddress);
		
		if (appointmentObj != null) {
			for (int a = 0; a < appointmentObj.length; a++) {
				VersitObject versitObject = oxContainerConverter.convertAppointment(appointmentObj[a]);
				eventDef.write(versitWriter, versitObject);
			}
		}
		
		if (taskObj != null) {
			for (int a = 0; a < taskObj.length; a++) {
				VersitObject versitObject = oxContainerConverter.convertTask(taskObj[a]);
				taskDef.write(versitWriter, versitObject);
			}
		}
		
		versitDefinition.writeEnd(versitWriter, versitObjectContainer);
		byteArrayOutputStream.flush();
		versitWriter.flush();
		oxContainerConverter.close();
		
		return importICal(webCon, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), appointmentFolderId, taskFolderId, timeZone, emailaddress, host, session);
	}
		
	public static ImportResult[] importICal(WebConversation webCon, ByteArrayInputStream byteArrayInputStream, int appointmentFolderId, int taskFolderId, TimeZone timeZone, String emailaddress, String host, String session) throws Exception, TestException {
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
		
		WebRequest req = new PostMethodWebRequest(host + "/ajax/import" + parameter.getURLParameters());
		
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "ical-test.ics", byteArrayInputStream, Format.ICAL.getMimeType());
		
		WebResponse resp = webCon.getResource(req);
		
		assertEquals(200, resp.getResponseCode());
		
		JSONObject response = extractFromCallback( resp.getText() );
		
		JSONArray jsonArray = response.getJSONArray("data");
		
		assertNotNull("json array in response is null", jsonArray);
		
		ImportResult[] importResult = new ImportResult[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			JSONObject jsonObj = jsonArray.getJSONObject(a);
			
			if (jsonObj.has("error")) {
				importResult[a] = new ImportResult();
				importResult[a].setException(new OXException( jsonObj.getString("error")));
			} else {
				String objectId = jsonObj.getString("id");
				String folder = jsonObj.getString("folder_id");
				long timestamp = jsonObj.getLong("last_modified");
			
				importResult[a] = new ImportResult(objectId, folder, timestamp);
			} 
		}
		
		return importResult;
	}
	
	public AppointmentObject[] exportAppointment(WebConversation webCon, int inFolder, String mailaddress, TimeZone timeZone, String host, String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		final String contentType = "text/calendar";
		
		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter("folder", appointmentFolderId);
		parameter.setParameter("action", Format.ICAL.getConstantName());
		
		WebRequest req = new GetMethodWebRequest(host + "/ajax/export" + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resp.getText().getBytes());
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, mailaddress);
		
		final List<AppointmentObject> exportData = new ArrayList<AppointmentObject>();
		
		try {
			final VersitDefinition def = Versit.getDefinition(contentType);
			final VersitDefinition.Reader versitReader = def.getReader(byteArrayInputStream, "UTF-8");
			final VersitObject rootVersitObject = def.parseBegin(versitReader);
			VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);
			while (versitObject != null) {
				final Property property = versitObject.getProperty("UID");
				
				if ("VEVENT".equals(versitObject.name)) {
					final AppointmentObject appointmentObj = oxContainerConverter.convertAppointment(versitObject);
					appointmentObj.setParentFolderID(appointmentFolderId);
					exportData.add(appointmentObj);
				} else {
					LOG.warn("invalid versit object: " + versitObject.name);
				}
				
				versitObject = def.parseChild(versitReader, rootVersitObject);
			}
		} catch (Exception exc) {
			System.out.println("error: " + exc);
		}
		
		return exportData.toArray(new AppointmentObject[exportData.size()]);
	}
	
	public Task[] exportTask(WebConversation webCon, int inFolder, String mailaddress, TimeZone timeZone, String host, String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		final String contentType = "text/calendar";
		
		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter("action", Format.ICAL.getConstantName());
		parameter.setParameter("folder", taskFolderId);
		parameter.setParameter("type", Types.TASK);
		
		WebRequest req = new GetMethodWebRequest(host + EXPORT_URL + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resp.getText().getBytes());
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, mailaddress);
		
		final List<Task> exportData = new ArrayList<Task>();
		
		try {
			final VersitDefinition def = Versit.getDefinition(contentType);
			final VersitDefinition.Reader versitReader = def.getReader(byteArrayInputStream, "UTF-8");
			final VersitObject rootVersitObject = def.parseBegin(versitReader);
			VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);
			while (versitObject != null) {
				final Property property = versitObject.getProperty("UID");
				
				if ("VTODO".equals(versitObject.name)) {
					final Task taskObj = oxContainerConverter.convertTask(versitObject);
					taskObj.setParentFolderID(taskFolderId);
					exportData.add(taskObj);
				} else {
					LOG.warn("invalid versit object: " + versitObject.name);
				}
				
				versitObject = def.parseChild(versitReader, rootVersitObject);
			}
		} catch (Exception exc) {
			System.out.println("error: " + exc);
		}
		
		return exportData.toArray(new Task[exportData.size()]);
	}
}