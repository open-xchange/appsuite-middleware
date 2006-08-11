package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.api.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.URLParameter;

public class FolderTest extends AbstractAJAXTest {
	
	private static final String FOLDER_URL = "/ajax/folders";
	
	public static List<FolderObject> getRootFolders(final WebConversation conversation, final String hostname,
		final String sessionId, final boolean printOutput) throws MalformedURLException, IOException, SAXException,
		JSONException, OXException {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
		req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ROOT);
		String columns = FolderObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + "," + FolderObject.SUBFOLDERS;
		req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columns);
		final WebResponse resp = conversation.getResponse(req);
		final List<FolderObject> folders = new ArrayList<FolderObject>();
		JSONObject respObj = new JSONObject(resp.getText());
		JSONArray arr = respObj.getJSONArray("data");
		if (printOutput)
			System.out.println("data: " + arr.toString());
		for (int i = 0; i < arr.length(); i++) {
			JSONArray nestedArr = arr.getJSONArray(i);
			FolderObject rootFolder = new FolderObject();
			rootFolder.setObjectID(nestedArr.getInt(0));
			rootFolder.setModule(FolderParser.getModuleFromString(nestedArr.getString(1), nestedArr.getInt(0)));
			rootFolder.setFolderName(nestedArr.getString(2));
			rootFolder.setSubfolderFlag(nestedArr.getBoolean(3));
			folders.add(rootFolder);
		}
		return folders;
	}
	
	public static List<FolderObject> getSubfolders(final WebConversation conversation, final String hostname,
		final String sessionId, final String parentIdentifier, final boolean printOutput) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		return getSubfolders(conversation, hostname, sessionId, parentIdentifier, false, false);
	}
	
	public static List<FolderObject> getSubfolders(final WebConversation conversation, final String hostname,
		final String sessionId, final String parentIdentifier, final boolean printOutput, boolean ignoreMailfolder) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
		req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		req.setParameter("parent", parentIdentifier);
		String columns = FolderObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + "," + FolderObject.SUBFOLDERS + "," + FolderObject.STANDARD_FOLDER + "," + FolderObject.CREATED_BY;
		req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columns);
		
		if (ignoreMailfolder) {
			req.setParameter(AJAXServlet.PARAMETER_IGNORE, "mailfolder");
		}

		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (respObj.has("error") && !respObj.isNull("error")) {
			throw new OXException("Error occured: " + respObj.getString("error"));
		}
		if (!respObj.has("data") || respObj.isNull("data")) {
			throw new OXException("Error occured: Missing key \"data\"");
		}
		JSONArray data = respObj.getJSONArray("data");
		final List<FolderObject> folders = new ArrayList<FolderObject>();
		for (int i = 0; i < data.length(); i++) {
			JSONArray arr = data.getJSONArray(i);
			FolderObject subfolder = new FolderObject();
			try {
				subfolder.setObjectID(arr.getInt(0));
			} catch (JSONException exc) {
				subfolder.removeObjectID();
				subfolder.setFullName(arr.getString(0));
			}
			subfolder.setModule(FolderParser.getModuleFromString(arr.getString(1), subfolder.containsObjectID() ? subfolder.getObjectID() : -1));
			subfolder.setFolderName(arr.getString(2));
			subfolder.setSubfolderFlag(arr.getBoolean(3));
			subfolder.setDefaultFolder(arr.getBoolean(4));
			if (!arr.isNull(5))
            subfolder.setCreatedBy(arr.getInt(5));
			folders.add(subfolder);
		}
		return folders;
	}
	
	public static FolderObject getFolder(final WebConversation conversation, final String hostname,
		final String sessionId, final String folderIdentifier, Calendar timestamp, final boolean printOutput) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
		req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		req.setParameter(AJAXServlet.PARAMETER_ID, folderIdentifier);
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (respObj.has("error") && !respObj.isNull("error")) {
			throw new OXException("Error occured: " + respObj.getString("error"));
		}
		if (!respObj.has("data") || respObj.isNull("data")) {
			throw new OXException("Error occured: Missing key \"data\"");
		}
		JSONObject jsonFolder = respObj.getJSONObject("data");
		FolderObject fo = new FolderObject();
		try {
			fo.setObjectID(jsonFolder.getInt("id"));
		} catch (JSONException exc) {
			fo.removeObjectID();
			fo.setFullName(jsonFolder.getString("id"));
		}
		if(!jsonFolder.isNull("created_by"))
		fo.setCreatedBy(jsonFolder.getInt("created_by"));
		
		if(!jsonFolder.isNull("creation_date"))
		fo.setCreationDate(new Date(jsonFolder.getLong("creation_date")));
		fo.setFolderName(jsonFolder.getString("title"));
		
		if(!jsonFolder.isNull("module"))
		fo.setModule(FolderParser.getModuleFromString(jsonFolder.getString("module"), fo.containsObjectID() ? fo.getObjectID() : -1));
		if (fo.getModule() != FolderObject.MAIL && (!respObj.has("timestamp") || respObj.isNull("timestamp"))) {
			throw new OXException("Error occured: Missing key \"timestamp\"");
		}
		if (respObj.has("timestamp"))
			timestamp.setTimeInMillis(respObj.getLong("timestamp"));
		if (printOutput)
			System.out.println(respObj.toString());
		return fo;
	}
	
	public static int insertFolder(final WebConversation conversation, final String hostname, final String sessionId,
		final String entity, final boolean isGroup, final int parentFolderId, final String folderName,
		final String moduleStr, final int type, final String sharedForUser, final boolean printOutput) throws JSONException, MalformedURLException, IOException,
		SAXException, OXException {
		JSONObject jsonFolder = new JSONObject();
		jsonFolder.put("title", folderName);
		JSONArray perms = new JSONArray();
		JSONObject jsonPermission = new JSONObject();
		jsonPermission.put("entity", entity);
		jsonPermission.put("group", isGroup);
		jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
		perms.put(jsonPermission);
		if (sharedForUser != null && sharedForUser.length() > 0) {
			jsonPermission = new JSONObject();
			jsonPermission.put("entity", sharedForUser);
			jsonPermission.put("group", false);
			jsonPermission.put("bits", createPermissionBits(8, 4, 0, 0, false));
			perms.put(jsonPermission);
		}
		jsonFolder.put("permissions", perms);
		jsonFolder.put("module", moduleStr);
		jsonFolder.put("type", type);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter("folder", String.valueOf(parentFolderId));
		byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(), bais,
			"text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (!respObj.has("data") || respObj.has("error"))
			throw new OXException("Folder Insert failed" + (respObj.has("error") ? (": " + respObj.getString("error")) : ""));
		return respObj.getInt("data");
	}
	
	public static boolean renameFolder(final WebConversation conversation, final String hostname, final String sessionId,
		final int folderId, final String folderName,
		final String moduleStr, final int type, final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException {
		JSONObject jsonFolder = new JSONObject();
		jsonFolder.put("id", folderId);
		jsonFolder.put("title", folderName);
		jsonFolder.put("module", moduleStr);
		jsonFolder.put("type", type);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(folderId));
		urlParam.setParameter("timestamp", String.valueOf(timestamp));
		byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(), bais,
			"text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		if (resp.getText() == null || resp.getText().length() == 0)
			return true;
		else {
			JSONObject respObj = new JSONObject(resp.getText());
			if (printOutput)
				System.out.println(respObj.toString());
			return false;
		}
	}
	
	public static boolean updateFolder(final WebConversation conversation, final String hostname, final String sessionId,
		final String entity, final String secondEntity, final int folderId, final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException {
		JSONObject jsonFolder = new JSONObject();
		jsonFolder.put("id", folderId);
		JSONArray perms = new JSONArray();
		JSONObject jsonPermission = new JSONObject();
		jsonPermission.put("entity", entity);
		jsonPermission.put("group", false);
		jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
		perms.put(jsonPermission);
		jsonPermission = new JSONObject();
		jsonPermission.put("entity", secondEntity);
		jsonPermission.put("group", false);
		jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
		perms.put(jsonPermission);
		jsonFolder.put("permissions", perms);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(folderId));
		urlParam.setParameter("timestamp", String.valueOf(timestamp));
		byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(), bais,
			"text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		if (resp.getText() == null || resp.getText().length() == 0)
			return true;
		else {
			JSONObject respObj = new JSONObject(resp.getText());
			if (printOutput)
				System.out.println(respObj.toString());
			return false;
		}
	}
	
	public static boolean moveFolder(final WebConversation conversation, final String hostname, final String sessionId,
			final String folderId, final String tgtFolderId, final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException {
		JSONObject jsonFolder = new JSONObject();
		jsonFolder.put("id", folderId);
		jsonFolder.put("folder_id", tgtFolderId);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(folderId));
		urlParam.setParameter("timestamp", String.valueOf(timestamp));
		byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(), bais,
				"text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		if (resp.getText() == null || resp.getText().length() == 0)
			return true;
		else {
			JSONObject respObj = new JSONObject(resp.getText());
			if (printOutput)
				System.out.println(respObj.toString());
			return false;
		}
	}
	
	public static int[] deleteFolders(final WebConversation conversation, final String hostname, final String sessionId, final int[] folderIds, final long timestamp, final boolean printOutput) throws JSONException, IOException, SAXException {
		JSONArray deleteIds = new JSONArray(Arrays.toString(folderIds));
		byte[] bytes = deleteIds.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp));
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(), bais,
			"text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		JSONArray arr = respObj.getJSONArray("data");
		int[] retval = new int[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			retval[i] = arr.getInt(i);
		}
		return retval;
	}
	
	private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };
	
	private static int createPermissionBits(int fp, int orp, int owp, int odp, boolean adminFlag) {
		int[] perms = new int[5];
		perms[0] = fp;
		perms[1] = orp;
		perms[2] = owp;
		perms[3] = odp;
		perms[4] = adminFlag ? 1 : 0;
		return createPermissionBits(perms);
	}
	
	private static int createPermissionBits(int[] permission) {
		int retval = 0;
		boolean first = true;
		for (int i = permission.length - 1; i >= 0; i--) {
			int exponent = (i * 7); // Number of bits to be shifted
			if (first) {
				retval += permission[i] << exponent;
				first = false;
			} else {
				if (permission[i] == OCLPermission.ADMIN_PERMISSION) {
					retval += Folder.MAX_PERMISSION << exponent;
				} else {
					retval += mapping[permission[i]] << exponent;
				}
			}
		}
		return retval;
	}
	
	public static FolderObject getStandardTaskFolder(final WebConversation conversation, final String hostname,
		final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""+FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
		for (Iterator iter = subfolders.iterator(); iter.hasNext();) {
			FolderObject subfolder = (FolderObject) iter.next();
			if (subfolder.getModule() == FolderObject.TASK && subfolder.isDefaultFolder()) {
				return subfolder;
			}
		}
		throw new OXException("No Standard Task Folder found!");
	}
	
	public static FolderObject getStandardCalendarFolder(final WebConversation conversation, final String hostname,
		final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""+FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
		for (Iterator iter = subfolders.iterator(); iter.hasNext();) {
			FolderObject subfolder = (FolderObject) iter.next();
			if (subfolder.getModule() == FolderObject.CALENDAR && subfolder.isDefaultFolder()) {
				return subfolder;
			}
		}
		throw new OXException("No Standard Calendar Folder found!");
	}
	
	public static FolderObject getStandardContactFolder(final WebConversation conversation, final String hostname,
		final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""+FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
		for (Iterator iter = subfolders.iterator(); iter.hasNext();) {
			FolderObject subfolder = (FolderObject) iter.next();
			if (subfolder.getModule() == FolderObject.CONTACT && subfolder.isDefaultFolder()) {
				return subfolder;
			}
		}
		throw new OXException("No Standard Contact Folder found!");
	}
	
	public static FolderObject getMyInfostoreFolder(final WebConversation conversation, final String hostname,
		final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		FolderObject infostore = null;
		List<FolderObject> l = getRootFolders(conversation, hostname, sessionId, false);
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			FolderObject rf = (FolderObject) iter.next();
			if (rf.getObjectID() == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
				infostore = rf;
				break;
			}
		}
		FolderObject userStore = null;
		l = getSubfolders(conversation, hostname, sessionId, ""+infostore.getObjectID(), false);
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			FolderObject f = (FolderObject) iter.next();
			if (f.getObjectID() == FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID) {
				userStore = f;
				break;
			}
		}
		System.out.println("MyInfostore");
		l = getSubfolders(conversation, hostname, sessionId, ""+userStore.getObjectID(), false);
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			FolderObject f = (FolderObject) iter.next();
			if (f.containsDefaultFolder() && f.isDefaultFolder()) {
				return f;
			}
		}
		throw new OXException("MyInfostore folder not found!");
	}
	
	public static void printTestStart(String testName) {
		System.out.println("\n\n\n--------------------------------"+testName+"--------------------------------");
	}
	
	public static void printTestEnd(String testName) {
		System.out.println("--------------------------------"+testName+"--------------------------------");
	}
	
	public void testGetRootFolders() {
		try {
			printTestStart("testGetRootFolders");
			int[] assumedIds = { 1, 2, 3, 9 };
			List<FolderObject> l = getRootFolders(getWebConversation(), getHostName(), getSessionId(), true);
			assertFalse(l == null || l.size() == 0);
			int i = 0;
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				FolderObject rf = (FolderObject) iter.next();
				assertTrue(rf.getObjectID() == assumedIds[i]);
				i++;
			}
			printTestEnd("testGetRootFolders");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testInsertRenameFolder() {
		int fuid = -1;
		int[] failedIds = null;
		boolean updated = false;
		try {
			printTestStart("testInsertRenameFolder");
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
				FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "NewPrivateFolder", "calendar", FolderObject.PRIVATE, null, true);
			assertFalse(fuid == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
			updated = renameFolder(getWebConversation(), getHostName(), getSessionId(), fuid, "ChangedPrivateFolderName", "calendar", FolderObject.PRIVATE, cal.getTimeInMillis(), true);
			assertTrue(updated);
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
			assertFalse((failedIds != null && failedIds.length > 0));
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
				FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "NewPublicFolder", "calendar", FolderObject.PRIVATE, null, true);
			assertFalse(fuid == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
			assertFalse((failedIds != null && failedIds.length > 0));
			fuid = -1;
			FolderObject myInfostore = getMyInfostoreFolder(getWebConversation(), getHostName(), getSessionId());
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
				myInfostore.getObjectID(), "NewInfostoreFolder", "infostore", FolderObject.PUBLIC, null, true);
			assertFalse(fuid == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
			updated = renameFolder(getWebConversation(), getHostName(), getSessionId(), fuid, "ChangedInfostoreFolderName", "infostore", FolderObject.PUBLIC, cal.getTimeInMillis(), true);
			assertTrue(updated);
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
			assertFalse((failedIds != null && failedIds.length > 0));
			fuid = -1;
			printTestEnd("testInsertRenameFolder");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			try {
			if (fuid != -1) {
					Calendar cal = GregorianCalendar.getInstance();
					/*
					 * Call getFolder to receive a valid timestamp for deletion
					 */
					getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, false);
					deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), false);
			}
			} catch (Exception e) {
				e.printStackTrace();
		}
	}
	}
	
	public void testInsertUpdateFolder() {
		int fuid = -1;
		int[] failedIds = null;
		boolean updated = false;
		try {
			printTestStart("testInsertUpdateFolder");
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
				FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "ChangeMyPermissions", "calendar", FolderObject.PRIVATE, null, true);
			assertFalse(fuid == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
			updated = updateFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), getSeconduser(), fuid, cal.getTimeInMillis(), true);
			assertTrue(updated);
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
			assertFalse((failedIds != null && failedIds.length > 0));
			fuid = -1;
			printTestEnd("testInsertUpdateFolder");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			try {
			if (fuid != -1) {
					Calendar cal = GregorianCalendar.getInstance();
					/*
					 * Call getFolder to receive a valid timestamp for deletion
					 */
					getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, false);
					deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), false);
			}
			} catch (Exception e) {
				e.printStackTrace();
		}
	}
	}
	
	public void testSharedFolder() {
		int fuid01 = -1;
		int fuid02 = -1;
		String anotherSessionId = null;
		try {
			printTestStart("testSharedFolder");
			/*
			 * Create a shared folder with login as creator and define share right for second user
			 */
			fuid01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
				FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "SharedFolder01", "calendar", FolderObject.PRIVATE,
					getSeconduser(), true);
			assertFalse(fuid01 == -1);
			fuid02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
				FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "SharedFolder02", "calendar", FolderObject.PRIVATE,
					getSeconduser(), true);
			assertFalse(fuid02 == -1);
			/*
			 * Connect with second user and verify that folder is visible beneath system shared folder
			 */
			anotherSessionId = LoginTest.getSessionId(getWebConversation(), getHostName(), getSeconduser(), getPassword());
			boolean found01 = false;
			boolean found02 = false;
			List<FolderObject> l = getSubfolders(getWebConversation(), getHostName(), anotherSessionId, ""+FolderObject.SYSTEM_SHARED_FOLDER_ID, true);
			assertFalse(l == null || l.size() == 0);
			Next: for (Iterator iter = l.iterator(); iter.hasNext();) {
				FolderObject virtualFO = (FolderObject) iter.next();
				List<FolderObject> subList = getSubfolders(getWebConversation(), getHostName(), anotherSessionId, virtualFO.getFullName(), true);
				for (Iterator iterator = subList.iterator(); iterator.hasNext();) {
					FolderObject sharedFolder = (FolderObject) iterator.next();
					if (sharedFolder.getObjectID() == fuid01) {
						found01 = true;
						if (found01 && found02)
							break Next;
					}
					if (sharedFolder.getObjectID() == fuid02) {
						found02 = true;
						if (found01 && found02)
							break Next;
					}
				}
			}
			assertTrue(found01);
			printTestEnd("testSharedFolder");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			try {
				if (fuid01 > -1) {
					Calendar cal = GregorianCalendar.getInstance();
					/*
					 * Call getFolder to receive a valid timestamp for deletion
					 */
					getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid01, cal, false);
					deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid01 }, cal.getTimeInMillis(), false);
				}
				if (fuid02 > -1) {
					Calendar cal = GregorianCalendar.getInstance();
					/*
					 * Call getFolder to receive a valid timestamp for deletion
					 */
					getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid02, cal, false);
					deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid02 }, cal.getTimeInMillis(), false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void testGetSubfolder() {
		int fuid = -1;
		int[] subfuids = null;
		try {
			printTestStart("testGetSubfolder");
			/*
			 * Create a temp folder with subfolders
			 */
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
				FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "NewPrivateFolder", "calendar", FolderObject.PRIVATE, null, true);
			DecimalFormat df = new DecimalFormat("00");
			subfuids = new int[3];
			for (int i = 0; i < subfuids.length; i++) {
				subfuids[i] = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
					fuid, "NewPrivateSubFolder" + df.format((i + 1)), "calendar", FolderObject.PRIVATE, null, true);
			}
			/*
			 * Get subfolder list
			 */
			List<FolderObject> l = getSubfolders(getWebConversation(), getHostName(), getSessionId(), ""+fuid, true);
			assertFalse(l == null || l.size() == 0);
			int i = 0;
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				FolderObject subFolder = (FolderObject) iter.next();
				assertTrue(subFolder.getObjectID() == subfuids[i]);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fuid != -1) {
					Calendar cal = GregorianCalendar.getInstance();
					/*
					 * Call getFolder to receive a valid timestamp for deletion
					 */
					getFolder(getWebConversation(), getHostName(), getSessionId(), ""+fuid, cal, true);
					int[] failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
					if (failedIds != null && failedIds.length > 0) {
						if (subfuids != null) {
							for (int i = 0; i < subfuids.length; i++) {
								if (subfuids[i] > 0) {
									/*
									 * Call getFolder to receive a valid timestamp for deletion
									 */
									getFolder(getWebConversation(), getHostName(), getSessionId(), ""+subfuids[i], cal, true);
									deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { subfuids[i] }, cal.getTimeInMillis(), true);
								}
							}
						}
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			printTestEnd("testGetSubfolder");
		}
	}
	
	public void notestMoveFolder() {
		int parent01 = -1;
		int parent02 = -1;
		int moveFuid = -1;
		int[] failedIds = null;
		boolean moved = false;
		try {
			printTestStart("testMoveFolder");
			parent01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Parent01", "calendar", FolderObject.PRIVATE, null, true);
			assertFalse(parent01 == -1);
			parent02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Parent02", "calendar", FolderObject.PRIVATE, null, true);
			assertFalse(parent02 == -1);
			moveFuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), false,
					parent01, "MoveMe", "calendar", FolderObject.PRIVATE, null, true);
			assertFalse(moveFuid == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+moveFuid, cal, true);
			moved = moveFolder(getWebConversation(), getHostName(), getSessionId(), ""+moveFuid, ""+parent02, cal.getTimeInMillis(), true);
			assertTrue(moved);
			getFolder(getWebConversation(), getHostName(), getSessionId(), ""+moveFuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { parent01, parent02, moveFuid }, cal.getTimeInMillis(), true);
			assertFalse((failedIds != null && failedIds.length > 0));
			printTestEnd("testMoveFolder");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetMailInbox() {
		try {
			printTestStart("testGetMailInbox");
			List<FolderObject> l = getSubfolders(getWebConversation(), getHostName(), getSessionId(), ""+FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true);
			FolderObject defaultIMAPFolder = null;
			for (int i = 0; i < l.size(); i++) {
				FolderObject fo = l.get(i);
				if (fo.containsFullName() && fo.getFullName().equals(FolderObject.DEFAULT_IMAP_FOLDER)) {
					defaultIMAPFolder = fo;
					break;
				}
			}
			assertTrue(defaultIMAPFolder != null && defaultIMAPFolder.hasSubfolders());
			l = getSubfolders(getWebConversation(), getHostName(), getSessionId(), defaultIMAPFolder.getFullName(), true);
			assertTrue(l != null && l.size() > 0);
			FolderObject inboxFolder = null;
			for (int i = 0; i < l.size() && (inboxFolder == null); i++) {
				FolderObject fo = l.get(i);
				if (fo.getFullName().equals("INBOX")) {
					inboxFolder = fo;
				}
			}
			assertTrue(inboxFolder != null);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), inboxFolder.getFullName(), cal, true);
			printTestEnd("testGetMailInbox");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}
