package com.openexchange.ajax.importexport;

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
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
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
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class AbstractVCardTest extends AbstractAJAXTest {
	
	protected static final String IMPORT_URL = "/ajax/import";
	
	protected static final String EXPORT_URL = "/ajax/export";
	
	protected int contactFolderId = -1;
	
	protected int userId = -1;
	
	protected String emailaddress = null;
	
	protected TimeZone timeZone = null;
	
	private static final Log LOG = LogFactory.getLog(AbstractVCardTest.class);
	
	public AbstractVCardTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		final FolderObject contactFolderObject = FolderTest
				.getStandardContactFolder(getWebConversation(),
				getHostName(), getSessionId());
		
		contactFolderId = contactFolderObject.getObjectID();
		
		userId = contactFolderObject.getCreatedBy();
		
		timeZone = ConfigTools.getTimeZone(getWebConversation(),
				getHostName(), getSessionId());
		
		LOG.debug(new StringBuilder().append("use timezone: ").append(
				timeZone).toString());
		
		ContactObject contactObj = ContactTest.loadUser(getWebConversation(), userId, FolderObject.SYSTEM_LDAP_FOLDER_ID, getHostName(), getSessionId());
		emailaddress = contactObj.getEmail1();
	}
	
	public static ImportResult[] importVCard(WebConversation webCon, ContactObject[] contactObj, int folderId, TimeZone timeZone, String emailaddress, String host, String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		final VersitDefinition contactDef = Versit.getDefinition("text/vcard");
		final VersitDefinition.Writer versitWriter = contactDef.getWriter(byteArrayOutputStream, "UTF-8");
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, emailaddress);
		
		
		if (contactObj != null) {
			for (int a = 0; a < contactObj.length; a++) {
				VersitObject versitObject = oxContainerConverter.convertContact(contactObj[a], "3.0");
				contactDef.write(versitWriter, versitObject);
			}
		}
		
		byteArrayOutputStream.flush();
		versitWriter.flush();
		oxContainerConverter.close();
				
		return importVCard(webCon, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), folderId, timeZone, emailaddress, host, session);
	}
	
	public static ImportResult[] importVCard(WebConversation webCon, ByteArrayInputStream byteArrayInputStream, int folderId, TimeZone timeZone, String emailaddress, String host, String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, Format.VCARD.getConstantName());
		parameter.setParameter("folder", folderId);
		
		WebRequest req = new PostMethodWebRequest(host + "/ajax/import" + parameter.getURLParameters());
		
		((PostMethodWebRequest)req).setMimeEncoded(true);
		req.selectFile("file", "vcard-test.vcf", byteArrayInputStream, Format.VCARD.getMimeType());
		
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
				importResult[a].setException(new OXException("server error"));
			} else {
				String objectId = jsonObj.getString("id");
				String folder = jsonObj.getString("folder_id");
				long timestamp = jsonObj.getLong("last_modified");
				
				importResult[a] = new ImportResult(objectId, folder, timestamp);
			}
		}
		
		return importResult;
	}
	
	public ContactObject[] exportContact(WebConversation webCon, int inFolder, String mailaddress, TimeZone timeZone, String host, String session) throws Exception, TestException {
		host = appendPrefix(host);
		
		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter("folder", inFolder);
		parameter.setParameter("action", Format.VCARD.getConstantName());
		 
		WebRequest req = new GetMethodWebRequest(host + "/ajax/export" + parameter.getURLParameters());
		WebResponse resp = webCon.getResponse(req);
		 
		assertEquals(200, resp.getResponseCode());

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resp.getText().getBytes());
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, mailaddress);
		 
		final List<ContactObject> exportData = new ArrayList<ContactObject>();
		 
		try {
			final VersitDefinition def = Versit.getDefinition("text/vcard");
			final VersitDefinition.Reader versitReader = def.getReader(byteArrayInputStream, "UTF-8");
			VersitObject versitObject = def.parse(versitReader);
			while (versitObject != null) {
				final ContactObject contactObj = oxContainerConverter.convertContact(versitObject);
				contactObj.setParentFolderID(contactFolderId);
				exportData.add(contactObj);
	 
				versitObject = def.parse(versitReader);
			}
		} catch (Exception exc) {
			throw new Exception(exc);
		}
		 
		return exportData.toArray(new ContactObject[exportData.size()]);
	}
}