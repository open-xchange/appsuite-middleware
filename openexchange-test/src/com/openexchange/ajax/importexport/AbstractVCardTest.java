package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.formats.Format;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class AbstractVCardTest extends AbstractAJAXTest {

	protected static final String IMPORT_URL = "/ajax/import";

	protected static final String EXPORT_URL = "/ajax/export";

	protected int contactFolderId = -1;

	protected int userId = -1;

	protected String emailaddress = null;

	protected TimeZone timeZone = null;

	private static final Log LOG = LogFactory.getLog(AbstractVCardTest.class);

	public AbstractVCardTest(final String name) {
		super(name);
	}

	@Override
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

		final Contact contactObj = ContactTest.loadUser(getWebConversation(), userId, FolderObject.SYSTEM_LDAP_FOLDER_ID, getHostName(), getSessionId());
		emailaddress = contactObj.getEmail1();
	}

	public static ImportResult[] importVCard(final WebConversation webCon, final Contact[] contactObj, final int folderId, final TimeZone timeZone, final String emailaddress, String host, final String session) throws Exception, OXException {
		host = appendPrefix(host);

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		final VersitDefinition contactDef = Versit.getDefinition("text/vcard");
		final VersitDefinition.Writer versitWriter = contactDef.getWriter(byteArrayOutputStream, "UTF-8");
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, emailaddress);


		if (contactObj != null) {
			for (int a = 0; a < contactObj.length; a++) {
				final VersitObject versitObject = oxContainerConverter.convertContact(contactObj[a], "3.0");
				contactDef.write(versitWriter, versitObject);
			}
		}

		byteArrayOutputStream.flush();
		versitWriter.flush();
		oxContainerConverter.close();

		return importVCard(webCon, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), folderId, timeZone, emailaddress, host, session);
	}

	public static ImportResult[] importVCard(final WebConversation webCon, final ByteArrayInputStream byteArrayInputStream, final int folderId, final TimeZone timeZone, final String emailaddress, String host, final String session) throws Exception, OXException {
		host = appendPrefix(host);

		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, Format.VCARD.getConstantName());
		parameter.setParameter("folder", folderId);

		final WebRequest req = new PostMethodWebRequest(host + "/ajax/import" + parameter.getURLParameters(), true);

		req.selectFile("file", "vcard-test.vcf", byteArrayInputStream, Format.VCARD.getMimeType());

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
				importResult[a].setException(new OXException(6666,jsonObj.getString("error")));
			} else {
				final String objectId = jsonObj.getString("id");
				final String folder = jsonObj.getString("folder_id");
				final long timestamp = jsonObj.getLong("last_modified");

				importResult[a] = new ImportResult(objectId, folder, timestamp);
			}
		}

		return importResult;
	}

	public Contact[] exportContact(final WebConversation webCon, final int inFolder, final String mailaddress, final TimeZone timeZone, String host, final String session) throws Exception, OXException {
		host = appendPrefix(host);

		final URLParameter parameter = new URLParameter(true);
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter("folder", inFolder);
		parameter.setParameter("action", Format.VCARD.getConstantName());

		final WebRequest req = new GetMethodWebRequest(host + "/ajax/export" + parameter.getURLParameters());
		final WebResponse resp = webCon.getResponse(req);

		assertEquals(200, resp.getResponseCode());

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resp.getText().getBytes());
		final OXContainerConverter oxContainerConverter = new OXContainerConverter(timeZone, mailaddress);

		final List<Contact> exportData = new ArrayList<Contact>();

		try {
			final VersitDefinition def = Versit.getDefinition("text/vcard");
			final VersitDefinition.Reader versitReader = def.getReader(byteArrayInputStream, "UTF-8");
			VersitObject versitObject = def.parse(versitReader);
			while (versitObject != null) {
				final Contact contactObj = oxContainerConverter.convertContact(versitObject);
				contactObj.setParentFolderID(contactFolderId);
				exportData.add(contactObj);

				versitObject = def.parse(versitReader);
			}
		} catch (final Exception exc) {
			throw new Exception(exc);
		}

		return exportData.toArray(new Contact[exportData.size()]);
	}
}
