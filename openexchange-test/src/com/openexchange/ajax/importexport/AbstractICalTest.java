package com.openexchange.ajax.importexport;

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
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class AbstractICalTest extends AbstractAJAXTest {
	
	protected static final String IMPORTEXPORT_URL = "/ajax/importexport";
	
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
		taskFolderId = appointmentFolderObj.getObjectID();

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
		startTime.setTime(timeZone.getOffset(startTime.getTime()));
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
		
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		
		if (appointmentFolderId != -1) {
			parameter.setParameter("appointmentfolder", appointmentFolderId);
		}
		
		if (appointmentFolderId != -1) {
			parameter.setParameter("taskfolder", taskFolderId);
		}
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + IMPORTEXPORT_URL
				+ parameter.getURLParameters(), byteArrayInputStream, "text/calendar");
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(200, resp.getResponseCode());
		
		final Response response = Response.parse(resp.getText());
		
		// parse jsonarray
		
		JSONArray jsonArray = (JSONArray) response.getData();
		
		assertNotNull("json array in response is null", jsonArray);
		
		ImportResult[] importResult = new ImportResult[jsonArray.length()];
		for (int a = 0; a < jsonArray.length(); a++) {
			JSONObject jsonObj = jsonArray.getJSONObject(a);
			
			String objectId = jsonObj.getString("object_id");
			String folder = jsonObj.getString("folder");
			long timestamp = jsonObj.getLong("timestamp");
			
			importResult[a] = new ImportResult(objectId, folder, timestamp);
		}
		
		return importResult;
	}
}