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
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.servlet.AjaxException;

public class FolderTest extends AbstractAJAXTest {

	private String sessionId;

	public FolderTest(String name) {
		super(name);
	}

	public static final String FOLDER_URL = "/ajax/folders";

	private static final String URL_ENCODED_COMMA = "%2C";

	private static String getCommaSeperatedIntegers(int[] intArray) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < intArray.length - 1; i++) {
			sb.append(intArray[i]);
			sb.append(',');
		}
		sb.append(intArray[intArray.length - 1]);
		return sb.toString();
	}

	private static int[] parsePermissionBits(int bits) {
		int[] retval = new int[5];
		for (int i = retval.length - 1; i >= 0; i--) {
			int exponent = (i * 7); // Number of bits to be shifted
			retval[i] = bits >> exponent;
			bits -= (retval[i] << exponent);
			if (retval[i] == Folder.MAX_PERMISSION)
				retval[i] = OCLPermission.ADMIN_PERMISSION;
			else if (i < (retval.length - 1))
				retval[i] = mapping_01[retval[i]];
			else
				retval[i] = retval[i];
		}
		return retval;
	}

	private static final int[] mapping_01 = { 0, 2, 4, -1, 8 };

	public static final int getUserId(final WebConversation conversation, final String hostname,
			final String entityArg, final String password) throws IOException, SAXException, JSONException,
			AjaxException, ConfigurationException {
		final String sessionId = LoginTest.getSessionId(conversation, hostname, entityArg, password);
		return ConfigTools.getUserId(conversation, hostname, sessionId);
	}

	public static List<FolderObject> getRootFolders(final WebConversation conversation, final String hostname,
			final String sessionId, final boolean printOutput) throws MalformedURLException, IOException, SAXException,
			JSONException, OXException {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
		req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ROOT);
		String columns = FolderObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + ","
				+ FolderObject.SUBFOLDERS;
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
			final String sessionId, final String parentIdentifier, final boolean printOutput)
			throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		return getSubfolders(conversation, hostname, sessionId, parentIdentifier, false, false);
	}

	public static List<FolderObject> getSubfolders(final WebConversation conversation, final String hostname,
			final String sessionId, final String parentIdentifier, final boolean printOutput, boolean ignoreMailfolder)
			throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
		req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		req.setParameter("parent", parentIdentifier);
		String columns = FolderObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + ","
				+ FolderObject.SUBFOLDERS + "," + FolderObject.STANDARD_FOLDER + "," + FolderObject.CREATED_BY;
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
			subfolder.setModule(FolderParser.getModuleFromString(arr.getString(1),
					subfolder.containsObjectID() ? subfolder.getObjectID() : -1));
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
			final String sessionId, final String folderIdentifier, Calendar timestamp, final boolean printOutput)
			throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
		req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		req.setParameter(AJAXServlet.PARAMETER_ID, folderIdentifier);
		req.setParameter(AJAXServlet.PARAMETER_COLUMNS, getCommaSeperatedIntegers(new int[] { FolderObject.OBJECT_ID,
				FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS }));
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
		if (!jsonFolder.isNull("created_by"))
			fo.setCreatedBy(jsonFolder.getInt("created_by"));

		if (!jsonFolder.isNull("creation_date"))
			fo.setCreationDate(new Date(jsonFolder.getLong("creation_date")));
		fo.setFolderName(jsonFolder.getString("title"));

		if (!jsonFolder.isNull("module"))
			fo.setModule(FolderParser.getModuleFromString(jsonFolder.getString("module"), fo.containsObjectID() ? fo
					.getObjectID() : -1));

		if (jsonFolder.has(FolderFields.PERMISSIONS) && !jsonFolder.isNull(FolderFields.PERMISSIONS)) {
			JSONArray jsonArr = jsonFolder.getJSONArray(FolderFields.PERMISSIONS);
			OCLPermission[] perms = new OCLPermission[jsonArr.length()];
			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject elem = jsonArr.getJSONObject(i);
				int entity;
				entity = elem.getInt(FolderFields.ENTITY);
				OCLPermission oclPerm = new OCLPermission();
				oclPerm.setEntity(entity);
				if (fo.containsObjectID()) {
					oclPerm.setFuid(fo.getObjectID());
				}
				int[] permissionBits = parsePermissionBits(elem.getInt(FolderFields.BITS));
				if (!oclPerm.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2],
						permissionBits[3]))
					throw new OXException("Invalid permission values: fp=" + permissionBits[0] + " orp="
							+ permissionBits[1] + " owp=" + permissionBits[2] + " odp=" + permissionBits[3]);
				oclPerm.setFolderAdmin(permissionBits[4] > 0 ? true : false);
				oclPerm.setGroupPermission(elem.getBoolean(FolderFields.GROUP));
				perms[i] = oclPerm;
			}
			fo.setPermissionsAsArray(perms);
		}

		if (respObj.has("timestamp") && !respObj.isNull("timestamp"))
			timestamp.setTimeInMillis(respObj.getLong("timestamp"));
		if (printOutput)
			System.out.println(respObj.toString());
		return fo;
	}

	public static int insertFolder(final WebConversation conversation, final String hostname, final String sessionId,
			final int entityId, final boolean isGroup, final int[] permsArr, final boolean isAdmin,
			final int parentFolderId, final String folderName, final String moduleStr, final int type,
			final int sharedForUserId, final int[] sharedPermsArr, final boolean sharedIsAdmin,
			final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException,
			OXException {
		JSONObject jsonFolder = new JSONObject();
		jsonFolder.put("title", folderName);
		JSONArray perms = new JSONArray();
		JSONObject jsonPermission = new JSONObject();
		jsonPermission.put("entity", entityId);
		jsonPermission.put("group", isGroup);
		jsonPermission.put("bits", createPermissionBits(permsArr[0], permsArr[1], permsArr[2], permsArr[3], isAdmin));
		perms.put(jsonPermission);
		if (sharedForUserId != -1) {
			jsonPermission = new JSONObject();
			jsonPermission.put("entity", sharedForUserId);
			jsonPermission.put("group", false);
			jsonPermission.put("bits", createPermissionBits(sharedPermsArr[0], sharedPermsArr[1], sharedPermsArr[2],
					sharedPermsArr[3], sharedIsAdmin));
			perms.put(jsonPermission);
		}
		jsonFolder.put("permissions", perms);
		jsonFolder.put("module", moduleStr);
		jsonFolder.put("type", type);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(FolderFields.FOLDER_ID, String.valueOf(parentFolderId));
		byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(),
				bais, "text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (!respObj.has("data") || respObj.has("error"))
			throw new OXException("Folder Insert failed"
					+ (respObj.has("error") ? (": " + respObj.getString("error")) : ""));
		return respObj.getInt("data");
	}

	public static int insertFolder(final WebConversation conversation, final String hostname, final String sessionId,
			final int entityId, final boolean isGroup, final int parentFolderId, final String folderName,
			final String moduleStr, final int type, final int sharedForUserId, final boolean printOutput)
			throws JSONException, MalformedURLException, IOException, SAXException, OXException {
		JSONObject jsonFolder = new JSONObject();
		jsonFolder.put("title", folderName);
		JSONArray perms = new JSONArray();
		JSONObject jsonPermission = new JSONObject();
		jsonPermission.put("entity", entityId);
		jsonPermission.put("group", isGroup);
		jsonPermission.put("bits", createPermissionBits(8, 8, 8, 8, true));
		perms.put(jsonPermission);
		if (sharedForUserId != -1) {
			jsonPermission = new JSONObject();
			jsonPermission.put("entity", sharedForUserId);
			jsonPermission.put("group", false);
			jsonPermission.put("bits", createPermissionBits(OCLPermission.CREATE_OBJECTS_IN_FOLDER, 4, 0, 0, false));
			perms.put(jsonPermission);
		}
		jsonFolder.put("permissions", perms);
		jsonFolder.put("module", moduleStr);
		jsonFolder.put("type", type);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(FolderFields.FOLDER_ID, String.valueOf(parentFolderId));
		byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(),
				bais, "text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (!respObj.has("data") || respObj.has("error"))
			throw new OXException("Folder Insert failed"
					+ (respObj.has("error") ? (": " + respObj.getString("error")) : ""));
		return respObj.getInt("data");
	}

	public static boolean renameFolder(final WebConversation conversation, final String hostname,
			final String sessionId, final int folderId, final String folderName, final String moduleStr,
			final int type, final long timestamp, final boolean printOutput) throws JSONException,
			MalformedURLException, IOException, SAXException {
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
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(),
				bais, "text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (respObj.has("error"))
			return false;
		return true;
	}

	public static boolean updateFolder(final WebConversation conversation, final String hostname,
			final String sessionId, final String entityArg, final String secondEntityArg, final int folderId,
			final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException,
			SAXException {
		final String entity = entityArg.indexOf('@') == -1 ? entityArg : entityArg.substring(0, entityArg.indexOf('@'));
		final String secondEntity;
		if (secondEntityArg == null) {
			secondEntity = null;
		} else {
			secondEntity = secondEntityArg.indexOf('@') == -1 ? secondEntityArg : secondEntityArg.substring(0,
					secondEntityArg.indexOf('@'));
		}
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
		jsonPermission.put("bits", createPermissionBits(4, 0, 0, 0, false));
		perms.put(jsonPermission);
		jsonFolder.put("permissions", perms);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(folderId));
		urlParam.setParameter("timestamp", String.valueOf(timestamp));
		byte[] bytes = jsonFolder.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(),
				bais, "text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (respObj.has("error"))
			return false;
		return true;
	}

	public static boolean moveFolder(final WebConversation conversation, final String hostname, final String sessionId,
			final String folderId, final String tgtFolderId, final long timestamp, final boolean printOutput)
			throws JSONException, MalformedURLException, IOException, SAXException {
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
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(),
				bais, "text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (respObj.has("error"))
			return false;
		return true;
	}

	public static int[] deleteFolders(final WebConversation conversation, final String hostname,
			final String sessionId, final int[] folderIds, final long timestamp, final boolean printOutput)
			throws JSONException, IOException, SAXException {
		JSONArray deleteIds = new JSONArray(Arrays.toString(folderIds));
		byte[] bytes = deleteIds.toString().getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		URLParameter urlParam = new URLParameter();
		urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		urlParam.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp));
		final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname + FOLDER_URL + urlParam.getURLParameters(),
				bais, "text/javascript; charset=UTF-8");
		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (respObj.has("error"))
			throw new JSONException("JSON Response object contains an error: " + respObj.getString("error"));
		JSONArray arr = respObj.getJSONArray("data");
		int[] retval = new int[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			retval[i] = arr.getInt(i);
		}
		return retval;
	}

	// public static boolean deleteTestFolders(final WebConversation
	// conversation, final String hostname,
	// final String sessionId, final int[] folderIds, final boolean printOutput)
	// throws JSONException,
	// IOException, SAXException {
	// String deleteIds = Arrays.toString(folderIds);
	// deleteIds = deleteIds.substring(1, deleteIds.length() -
	// 1).replaceAll("\\s+", "");
	//
	// byte[] bytes = "".getBytes("UTF-8");
	// ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	//
	// URLParameter urlParam = new URLParameter();
	// urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, "removetestfolders");
	// urlParam.setParameter("del_ids", deleteIds);
	// urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
	//
	// final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname +
	// FOLDER_URL + urlParam.getURLParameters(),
	// bais, "text/javascript; charset=UTF-8");
	//
	// final WebResponse resp = conversation.getResponse(req);
	//
	// JSONObject respObj = new JSONObject(resp.getText());
	// if (printOutput)
	// System.out.println(respObj.toString());
	// if (respObj.has("error"))
	// throw new JSONException("JSON Response object contains an error: " +
	// respObj.getString("error"));
	// String retval = respObj.getString("data");
	// return retval.equalsIgnoreCase("ok");
	// }

	private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };

	public static int createPermissionBits(int fp, int orp, int owp, int odp, boolean adminFlag) {
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
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""
				+ FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
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
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""
				+ FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
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
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""
				+ FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
		for (Iterator iter = subfolders.iterator(); iter.hasNext();) {
			FolderObject subfolder = (FolderObject) iter.next();
			if (subfolder.getModule() == FolderObject.CONTACT && subfolder.isDefaultFolder()) {
				return subfolder;
			}
		}
		throw new OXException("No Standard Contact Folder found!");
	}

	public static FolderObject getStandardInfostoreFolder(final WebConversation conversation, final String hostname,
			final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""
				+ FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
		for (Iterator iter = subfolders.iterator(); iter.hasNext();) {
			FolderObject subfolder = (FolderObject) iter.next();
			if (subfolder.getModule() == FolderObject.INFOSTORE && subfolder.isDefaultFolder()) {
				return subfolder;
			}
		}
		throw new OXException("No Standard Infostore Folder found!");
	}

	/*
	 * public static void testGetMailInboxStatic(final WebConversation
	 * conversation, final String hostname, final String sessionId) { try {
	 * printTestStart("testGetMailInbox"); List<FolderObject> l =
	 * getSubfolders(conversation, hostname, sessionId, "" +
	 * FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true); FolderObject
	 * defaultIMAPFolder = null; for (int i = 0; i < l.size(); i++) {
	 * FolderObject fo = l.get(i); if (fo.containsFullName() &&
	 * fo.getFullName().equals(MailFolderObject.DEFAULT_IMAP_FOLDER_ID)) {
	 * defaultIMAPFolder = fo; break; } } assertTrue(defaultIMAPFolder != null &&
	 * defaultIMAPFolder.hasSubfolders()); l = getSubfolders(conversation,
	 * hostname, sessionId, defaultIMAPFolder.getFullName(), true); assertTrue(l !=
	 * null && l.size() > 0); FolderObject inboxFolder = null; for (int i = 0; i <
	 * l.size() && (inboxFolder == null); i++) { FolderObject fo = l.get(i); if
	 * (fo.getFullName().endsWith("INBOX")) { inboxFolder = fo; } }
	 * assertTrue(inboxFolder != null); Calendar cal =
	 * GregorianCalendar.getInstance(); getFolder(conversation, hostname,
	 * sessionId, inboxFolder.getFullName(), cal, true);
	 * printTestEnd("testGetMailInbox"); } catch (Exception e) {
	 * e.printStackTrace(); fail(e.getMessage()); } }
	 */

	public static FolderObject getMyInfostoreFolder(final WebConversation conversation, final String hostname,
			final String sessionId, final int loginId) throws MalformedURLException, IOException, SAXException,
			JSONException, OXException {
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
		l = getSubfolders(conversation, hostname, sessionId, "" + infostore.getObjectID(), false);
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			FolderObject f = (FolderObject) iter.next();
			if (f.getObjectID() == FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID) {
				userStore = f;
				break;
			}
		}
		// System.out.println("MyInfostore");
		l = getSubfolders(conversation, hostname, sessionId, "" + userStore.getObjectID(), false);
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			FolderObject f = (FolderObject) iter.next();
			if (f.containsDefaultFolder() && f.isDefaultFolder() && f.getCreator() == loginId) {
				return f;
			}
		}
		throw new OXException("MyInfostore folder not found!");
	}

	public static void printTestStart(String testName) {
		System.out.println("\n\n\n--------------------------------" + testName + "--------------------------------");
	}

	public static void printTestEnd(String testName) {
		System.out.println("--------------------------------" + testName + "--------------------------------");
	}

	public void setUp() throws Exception {
		sessionId = getSessionId();
	}

	public void tearDown() throws Exception {
		logout();
	}

	public void testUnknownAction() {
		try {
			printTestStart("testUnknownAction");
			final WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostName() + FOLDER_URL);
			req.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
			req.setParameter(AJAXServlet.PARAMETER_ACTION, "unknown");
			final WebResponse resp = getWebConversation().getResponse(req);
			JSONObject respObj = new JSONObject(resp.getText());
			System.out.println("Response-Object: " + respObj.toString());
			assertTrue(respObj.has("error")
					&& respObj.getString("error").indexOf("Action \"unknown\" NOT supported via GET on /ajax/folders") != -1);
			printTestEnd("testUnknownAction");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testGetUserId() {
		try {
			int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			System.out.println(new StringBuilder(100).append("ID of user \"").append(getLogin()).append("\": ").append(
					userId));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
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

	public void testDeleteFolder() {
		try {
			printTestStart("testDeleteFolder");
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			int parent = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "DeleteMeImmediately", "calendar", FolderObject.PUBLIC, -1,
					true);
			assertFalse(parent == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + parent, cal, true);

			int child01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, parent,
					"DeleteMeImmediatelyChild01", "calendar", FolderObject.PUBLIC, -1, true);
			assertFalse(child01 == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + child01, cal, true);

			int child02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, parent,
					"DeleteMeImmediatelyChild02", "calendar", FolderObject.PUBLIC, -1, true);
			assertFalse(child02 == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + child02, cal, true);

			int[] failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { parent },
					cal.getTimeInMillis(), true);
			assertTrue((failedIds == null || failedIds.length == 0));
			printTestEnd("testDeleteFolder");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testFailDeleteFolder() {
		try {
			printTestStart("testFailDeleteFolder");
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			final int secId = getUserId(getWebConversation(), getHostName(), getSeconduser(), getPassword());
			int parent = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, new int[] {
					8, 8, 8, 8 }, true, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "DeleteMeImmediately", "calendar",
					FolderObject.PUBLIC, secId, new int[] { 8, 8, 8, 8 }, false, true);
			assertFalse(parent == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + parent, cal, true);

			int child01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, new int[] {
					8, 8, 8, 8 }, true, parent, "DeleteMeImmediatelyChild01", "calendar", FolderObject.PUBLIC, secId,
					new int[] { 8, 8, 8, 8 }, false, true);
			assertFalse(child01 == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + child01, cal, true);

			int child02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, new int[] {
					8, 8, 8, 8 }, true, parent, "DeleteMeImmediatelyChild02", "calendar", FolderObject.PUBLIC, secId,
					new int[] { 8, 8, 8, 8 }, false, true);
			assertFalse(child02 == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + child02, cal, true);

			int subchild01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					new int[] { 8, 8, 8, 8 }, false, child01, "NonDeleteableSubChild01", "calendar",
					FolderObject.PUBLIC, secId, new int[] { 8, 8, 8, 8 }, true, true);
			assertFalse(subchild01 == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + subchild01, cal, true);

			Exception exc = null;
			try {
				int[] failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(),
						new int[] { parent }, cal.getTimeInMillis(), true);
			} catch (JSONException e) {
				exc = e;
			}

			byte[] bytes = "ok".getBytes("UTF-8");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			URLParameter urlParam = new URLParameter();
			urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, "removetestfolders");
			urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
			urlParam.setParameter("del_ids", parent + "," + child01 + "," + child02 + "," + subchild01);
			final WebRequest req = new PutMethodWebRequest(PROTOCOL + getHostName() + FOLDER_URL
					+ urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
			final WebResponse resp = getWebConversation().getResponse(req);
			System.out.println(resp.toString());

			assertTrue(exc != null);

			printTestEnd("testFailDeleteFolder");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testCheckFolderPermissions() {
		try {
			printTestStart("testCheckFolderPermissions");
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			int fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "CheckMyPermissions", "calendar", FolderObject.PUBLIC, -1,
					true);
			Calendar cal = GregorianCalendar.getInstance();
			FolderObject fo = getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			System.out.println("-------------------------------- START FolderObject --------------------------------");
			System.out.println(fo.toString());
			System.out.println("-------------------------------- END FolderObject ----------------------------------");
			updateFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), getSeconduser(), fuid, cal
					.getTimeInMillis(), true);
			fo = getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			System.out.println("-------------------------------- START FolderObject --------------------------------");
			System.out.println(fo.toString());
			System.out.println("-------------------------------- END FolderObject ----------------------------------");
			deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal
					.getTimeInMillis(), true);
			printTestEnd("testCheckFolderPermissions");
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
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "NewPrivateFolder", "calendar", FolderObject.PRIVATE, -1,
					true);
			assertFalse(fuid == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			updated = renameFolder(getWebConversation(), getHostName(), getSessionId(), fuid,
					"ChangedPrivateFolderName", "calendar", FolderObject.PRIVATE, cal.getTimeInMillis(), true);
			assertTrue(updated);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal
					.getTimeInMillis(), true);
			assertFalse((failedIds != null && failedIds.length > 0));
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "NewPublicFolder", "calendar", FolderObject.PRIVATE, -1, true);
			assertFalse(fuid == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal
					.getTimeInMillis(), true);
			assertFalse((failedIds != null && failedIds.length > 0));
			fuid = -1;
			FolderObject myInfostore = getMyInfostoreFolder(getWebConversation(), getHostName(), getSessionId(), userId);
			System.out.println("MyINfostore Folder: " + myInfostore.toString());
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, myInfostore
					.getObjectID(), "NewInfostoreFolder", "infostore", FolderObject.PUBLIC, -1, true);
			assertFalse(fuid == -1);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			updated = renameFolder(getWebConversation(), getHostName(), getSessionId(), fuid,
					"ChangedInfostoreFolderName", "infostore", FolderObject.PUBLIC, cal.getTimeInMillis(), true);
			assertTrue(updated);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal
					.getTimeInMillis(), true);
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
					getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, false);
					deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal
							.getTimeInMillis(), false);
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
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "ChangeMyPermissions", "calendar", FolderObject.PRIVATE, -1,
					true);
			assertFalse(fuid == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			updated = updateFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), getSeconduser(),
					fuid, cal.getTimeInMillis(), true);
			assertTrue(updated);
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal
					.getTimeInMillis(), true);
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
					getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, false);
					deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal
							.getTimeInMillis(), false);
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
			 * Create a shared folder with login as creator and define share
			 * right for second user
			 */
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			final int secId = getUserId(getWebConversation(), getHostName(), getSeconduser(), getPassword());
			fuid01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "SharedFolder01", "calendar", FolderObject.PRIVATE, secId,
					true);
			assertFalse(fuid01 == -1);
			fuid02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "SharedFolder02", "calendar", FolderObject.PRIVATE, secId,
					true);
			assertFalse(fuid02 == -1);
			/*
			 * Connect with second user and verify that folder is visible
			 * beneath system shared folder
			 */
			anotherSessionId = LoginTest.getSessionId(getWebConversation(), getHostName(), getSeconduser(),
					getPassword());
			boolean found01 = false;
			boolean found02 = false;
			List<FolderObject> l = getSubfolders(getWebConversation(), getHostName(), anotherSessionId, ""
					+ FolderObject.SYSTEM_SHARED_FOLDER_ID, true);
			assertFalse(l == null || l.size() == 0);
			Next: for (Iterator iter = l.iterator(); iter.hasNext();) {
				FolderObject virtualFO = (FolderObject) iter.next();
				List<FolderObject> subList = getSubfolders(getWebConversation(), getHostName(), anotherSessionId,
						virtualFO.getFullName(), true);
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
			assertTrue(found02);
			String sesID = LoginTest.getSessionId(getWebConversation(), getHostName(), getLogin(), getPassword());

			deleteFolders(getWebConversation(), getHostName(), sesID, new int[] { fuid01, fuid02 }, System
					.currentTimeMillis(), false);
			// deleteTestFolders(getWebConversation(), getHostName(), sesID, new
			// int[] { fuid01, fuid02 }, false);

			printTestEnd("testSharedFolder");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
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
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "NewPrivateFolder", "calendar", FolderObject.PRIVATE, -1,
					true);
			DecimalFormat df = new DecimalFormat("00");
			subfuids = new int[3];
			for (int i = 0; i < subfuids.length; i++) {
				subfuids[i] = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, fuid,
						"NewPrivateSubFolder" + df.format((i + 1)), "calendar", FolderObject.PRIVATE, -1, true);
			}
			/*
			 * Get subfolder list
			 */
			List<FolderObject> l = getSubfolders(getWebConversation(), getHostName(), getSessionId(), "" + fuid, true);
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
					getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
					int[] failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(),
							new int[] { fuid }, cal.getTimeInMillis(), true);
					if (failedIds != null && failedIds.length > 0) {
						if (subfuids != null) {
							for (int i = 0; i < subfuids.length; i++) {
								if (subfuids[i] > 0) {
									/*
									 * Call getFolder to receive a valid
									 * timestamp for deletion
									 */
									getFolder(getWebConversation(), getHostName(), getSessionId(), "" + subfuids[i],
											cal, true);
									deleteFolders(getWebConversation(), getHostName(), getSessionId(),
											new int[] { subfuids[i] }, cal.getTimeInMillis(), true);
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

	public void testMoveFolder() {
		int parent01 = -1;
		int parent02 = -1;
		int moveFuid = -1;
		int[] failedIds = null;
		boolean moved = false;
		try {
			printTestStart("testMoveFolder");
			final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
			parent01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Parent01", "calendar", FolderObject.PRIVATE, -1, true);
			assertFalse(parent01 == -1);
			parent02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false,
					FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Parent02", "calendar", FolderObject.PRIVATE, -1, true);
			assertFalse(parent02 == -1);
			moveFuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, parent01,
					"MoveMe", "calendar", FolderObject.PRIVATE, -1, true);
			assertFalse(moveFuid == -1);
			Calendar cal = GregorianCalendar.getInstance();
			getFolder(getWebConversation(), getHostName(), getSessionId(), "" + moveFuid, cal, true);
			moved = moveFolder(getWebConversation(), getHostName(), getSessionId(), "" + moveFuid, "" + parent02, cal
					.getTimeInMillis(), true);
			assertTrue(moved);
			FolderObject movedFolderObj = null;
			movedFolderObj = getFolder(getWebConversation(), getHostName(), getSessionId(), "" + moveFuid, cal, true);
			assertTrue(movedFolderObj.containsParentFolderID() ? movedFolderObj.getParentFolderID() == parent02 : true);
			failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { parent01,
					parent02 }, cal.getTimeInMillis(), true);
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
			List<FolderObject> l = getSubfolders(getWebConversation(), getHostName(), getSessionId(), ""
					+ FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true);
			FolderObject defaultIMAPFolder = null;
			for (int i = 0; i < l.size(); i++) {
				FolderObject fo = l.get(i);
				if (fo.containsFullName() && fo.getFullName().equals(MailFolder.DEFAULT_FOLDER_ID)) {
					defaultIMAPFolder = fo;
					break;
				}
			}
			assertTrue(defaultIMAPFolder != null && defaultIMAPFolder.hasSubfolders());
			l = getSubfolders(getWebConversation(), getHostName(), getSessionId(), defaultIMAPFolder.getFullName(),
					true);
			assertTrue(l != null && l.size() > 0);
			FolderObject inboxFolder = null;
			for (int i = 0; i < l.size() && (inboxFolder == null); i++) {
				FolderObject fo = l.get(i);
				if (fo.getFullName().endsWith("INBOX")) {
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

	public void testFolderNamesShouldBeEqualRegardlessOfRequestMethod() {
		try {
			List<FolderObject> rootFolders = getRootFolders(getWebConversation(), getHostName(), getSessionId(), true);
			for (FolderObject rootFolder : rootFolders) {
				FolderObject individuallyLoaded = getFolder(getWebConversation(), getHostName(), getSessionId(), ""
						+ rootFolder.getObjectID(), Calendar.getInstance(), true);
				assertEquals("Foldernames differ : " + rootFolder.getFolderName() + " != "
						+ individuallyLoaded.getFolderName(), rootFolder.getFolderName(), individuallyLoaded
						.getFolderName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
