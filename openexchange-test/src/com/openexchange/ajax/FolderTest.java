/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax;

import static com.openexchange.folderstorage.Permissions.createPermissionBits;
import static com.openexchange.folderstorage.Permissions.parsePermissionBits;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

public class FolderTest extends AbstractAJAXTest {

    private String sessionId;

    public FolderTest(final String name) {
        super(name);
    }

    public static final String FOLDER_URL = "/ajax/folders";

    private static String getCommaSeperatedIntegers(final int[] intArray) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < intArray.length - 1; i++) {
            sb.append(intArray[i]);
            sb.append(',');
        }
        sb.append(intArray[intArray.length - 1]);
        return sb.toString();
    }

    /**
     * @deprecated use {@link ConfigTools#getUserId(WebConversation, String, String)}.
     */
    @Deprecated
    public static final int getUserId(final WebConversation conversation, final String hostname, final String entityArg, final String password) throws IOException, SAXException, JSONException, OXException, OXException {
        final WebConversation conversation2 = new WebConversation(); // Can't reuse conversations for different sessions!
        final String sessionId = LoginTest.getSessionId(conversation2, hostname, entityArg, password);
        return ConfigTools.getUserId(conversation2, hostname, sessionId);
    }

    public static List<FolderObject> getRootFolders(final WebConversation conversation, final String hostname, final String sessionId, final boolean printOutput) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        return getRootFolders(conversation, null, hostname, sessionId, printOutput);
    }

    public static List<FolderObject> getRootFolders(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final boolean printOutput) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        return getRootFolders(conversation, protocol, hostname, sessionId);
    }

    public static List<FolderObject> getRootFolders(final WebConversation conversation, final String protocol, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        final WebRequest req = new GetMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ROOT);
        final String columns = FolderObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + "," + FolderObject.SUBFOLDERS;
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columns);
        final WebResponse resp = conversation.getResponse(req);
        final List<FolderObject> folders = new ArrayList<FolderObject>();
        final JSONObject respObj = new JSONObject(resp.getText());
        final JSONArray arr = respObj.getJSONArray("data");
        for (int i = 0; i < arr.length(); i++) {
            final JSONArray nestedArr = arr.getJSONArray(i);
            final FolderObject rootFolder = new FolderObject();
            rootFolder.setObjectID(nestedArr.getInt(0));
            rootFolder.setModule(FolderParser.getModuleFromString(nestedArr.getString(1), nestedArr.getInt(0)));
            rootFolder.setFolderName(nestedArr.getString(2));
            rootFolder.setSubfolderFlag(nestedArr.getBoolean(3));
            folders.add(rootFolder);
        }
        return folders;
    }

    public static List<FolderObject> getSubfolders(final WebConversation conversation, final String hostname, final String sessionId, final String parentIdentifier, final boolean printOutput) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        return getSubfolders(conversation, null, hostname, sessionId, parentIdentifier);
    }

    public static List<FolderObject> getSubfolders(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final String parentIdentifier) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        return getSubfolders(conversation, protocol, hostname, sessionId, parentIdentifier, false);
    }

    public static List<FolderObject> getSubfolders(final WebConversation conversation, final String hostname, final String sessionId, final String parentIdentifier, final boolean printOutput, final boolean ignoreMailfolder) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        return getSubfolders(conversation, null, hostname, sessionId, parentIdentifier, ignoreMailfolder);
    }

    public static List<FolderObject> getSubfolders(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final String parentIdentifier, final boolean ignoreMailfolder) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        final AJAXClient client = new AJAXClient(new AJAXSession(conversation, hostname, sessionId), false);
        client.setProtocol(protocol);
        client.setHostname(hostname);
        return FolderTools.getSubFolders(client, parentIdentifier, ignoreMailfolder);
    }

    public static FolderObject getFolder(final WebConversation conversation, final String hostname, final String sessionId, final String folderIdentifier, final Calendar timestamp, final boolean printOutput) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        return getFolder(conversation, null, hostname, sessionId, folderIdentifier, timestamp);
    }

    public static FolderObject getFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final String folderIdentifier, final Calendar timestamp) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        final WebRequest req = new GetMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
        req.setParameter(AJAXServlet.PARAMETER_ID, folderIdentifier);
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, getCommaSeperatedIntegers(new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS }));
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (respObj.has("error") && !respObj.isNull("error")) {
            throw OXException.general("Error occured: " + respObj.getString("error"));
        }
        if (!respObj.has("data") || respObj.isNull("data")) {
            throw OXException.general("Error occured: Missing key \"data\"");
        }
        final JSONObject jsonFolder = respObj.getJSONObject("data");
        final FolderObject fo = new FolderObject();
        try {
            fo.setObjectID(jsonFolder.getInt("id"));
        } catch (final JSONException exc) {
            fo.removeObjectID();
            fo.setFullName(jsonFolder.getString("id"));
        }
        if (!jsonFolder.isNull("created_by")) {
            fo.setCreatedBy(jsonFolder.getInt("created_by"));
        }

        if (!jsonFolder.isNull("creation_date")) {
            fo.setCreationDate(new Date(jsonFolder.getLong("creation_date")));
        }
        fo.setFolderName(jsonFolder.getString("title"));

        if (!jsonFolder.isNull("module")) {
            fo.setModule(FolderParser.getModuleFromString(jsonFolder.getString("module"), fo.containsObjectID() ? fo.getObjectID() : -1));
        }

        if (jsonFolder.has(FolderFields.PERMISSIONS) && !jsonFolder.isNull(FolderFields.PERMISSIONS)) {
            final JSONArray jsonArr = jsonFolder.getJSONArray(FolderFields.PERMISSIONS);
            final OCLPermission[] perms = new OCLPermission[jsonArr.length()];
            for (int i = 0; i < jsonArr.length(); i++) {
                final JSONObject elem = jsonArr.getJSONObject(i);
                int entity;
                entity = elem.getInt(FolderFields.ENTITY);
                final OCLPermission oclPerm = new OCLPermission();
                oclPerm.setEntity(entity);
                if (fo.containsObjectID()) {
                    oclPerm.setFuid(fo.getObjectID());
                }
                final int[] permissionBits = parsePermissionBits(elem.getInt(FolderFields.BITS));
                if (!oclPerm.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2], permissionBits[3])) {
                    throw OXException.general("Invalid permission values: fp=" + permissionBits[0] + " orp=" + permissionBits[1] + " owp=" + permissionBits[2] + " odp=" + permissionBits[3]);
                }
                oclPerm.setFolderAdmin(permissionBits[4] > 0 ? true : false);
                oclPerm.setGroupPermission(elem.getBoolean(FolderFields.GROUP));
                perms[i] = oclPerm;
            }
            fo.setPermissionsAsArray(perms);
        }

        if (respObj.has("timestamp") && !respObj.isNull("timestamp")) {
            timestamp.setTimeInMillis(respObj.getLong("timestamp"));
        }
        return fo;
    }

    public static int insertFolder(final WebConversation conversation, final String hostname, final String sessionId, final int entityId, final boolean isGroup, final int[] permsArr, final boolean isAdmin, final int parentFolderId, final String folderName, final String moduleStr, final int type, final int sharedForUserId, final int[] sharedPermsArr, final boolean sharedIsAdmin, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException, OXException {
        return insertFolder(conversation, null, hostname, sessionId, entityId, isGroup, permsArr, isAdmin, parentFolderId, folderName, moduleStr, type, sharedForUserId, sharedPermsArr, sharedIsAdmin);
    }

    public static int insertFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final int entityId, final boolean isGroup, final int[] permsArr, final boolean isAdmin, final int parentFolderId, final String folderName, final String moduleStr, final int type, final int sharedForUserId, final int[] sharedPermsArr, final boolean sharedIsAdmin) throws JSONException, MalformedURLException, IOException, SAXException, OXException {
        final JSONObject jsonFolder = new JSONObject();
        jsonFolder.put("title", folderName);
        final JSONArray perms = new JSONArray();
        JSONObject jsonPermission = new JSONObject();
        jsonPermission.put("entity", entityId);
        jsonPermission.put("group", isGroup);
        jsonPermission.put("bits", createPermissionBits(permsArr[0], permsArr[1], permsArr[2], permsArr[3], isAdmin));
        perms.put(jsonPermission);
        if (sharedForUserId != -1) {
            jsonPermission = new JSONObject();
            jsonPermission.put("entity", sharedForUserId);
            jsonPermission.put("group", false);
            jsonPermission.put("bits", createPermissionBits(sharedPermsArr[0], sharedPermsArr[1], sharedPermsArr[2], sharedPermsArr[3], sharedIsAdmin));
            perms.put(jsonPermission);
        }
        jsonFolder.put("permissions", perms);
        jsonFolder.put("module", moduleStr);
        jsonFolder.put("type", type);
        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(FolderFields.FOLDER_ID, Integer.toString(parentFolderId));
        final byte[] bytes = jsonFolder.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest req = new PutMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL + urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (!respObj.has("data") || respObj.has("error")) {
            throw OXException.general("Folder Insert failed" + (respObj.has("error") ? (": " + respObj.getString("error")) : ""));
        }
        return respObj.getInt("data");
    }

    public static int insertFolder(final WebConversation conversation, final String hostname, final String sessionId, final int entityId, final boolean isGroup, final int parentFolderId, final String folderName, final String moduleStr, final int type, final int sharedForUserId, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException, OXException {
        return insertFolder(conversation, null, hostname, sessionId, entityId, isGroup, parentFolderId, folderName, moduleStr, type, sharedForUserId);
    }

    public static int insertFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final int entityId, final boolean isGroup, final int parentFolderId, final String folderName, final String moduleStr, final int type, final int sharedForUserId) throws JSONException, MalformedURLException, IOException, SAXException, OXException {
        final JSONObject jsonFolder = new JSONObject();
        jsonFolder.put("title", folderName);
        final JSONArray perms = new JSONArray();
        JSONObject jsonPermission = new JSONObject();
        jsonPermission.put("entity", entityId);
        jsonPermission.put("group", isGroup);
        jsonPermission.put("bits", createPermissionBits(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS, true));
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
        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(FolderFields.FOLDER_ID, Integer.toString(parentFolderId));
        final byte[] bytes = jsonFolder.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest req = new PutMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL + urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (!respObj.has("data") || respObj.has("error")) {
            throw OXException.general("Folder Insert failed" + (respObj.has("error") ? (": " + respObj.getString("error")) : ""));
        }
        return respObj.getInt("data");
    }

    public static boolean renameFolder(final WebConversation conversation, final String hostname, final String sessionId, final int folderId, final String folderName, final String moduleStr, final int type, final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException {
        return renameFolder(conversation, null, hostname, sessionId, folderId, folderName, moduleStr, type, timestamp);
    }

    public static boolean renameFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final int folderId, final String folderName, final String moduleStr, final int type, final long timestamp) throws JSONException, MalformedURLException, IOException, SAXException {
        final JSONObject jsonFolder = new JSONObject();
        jsonFolder.put("title", folderName);
        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(AJAXServlet.PARAMETER_ID, Integer.toString(folderId));
        urlParam.setParameter("timestamp", String.valueOf(timestamp));
        urlParam.setParameter(FolderFields.TREE, Integer.toString(1)); //TODO need to get this out of the rensponse
        final byte[] bytes = jsonFolder.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest req = new PutMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL + urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
        final WebResponse resp = conversation.getResponse(req);
        final String text = resp.getText();
        try {
            final JSONObject respObj = new JSONObject(text);
            if (respObj.has("error")) {
                return false;
            }
            return true;
        } catch (JSONException e) {
            // Response is no valid JSON
            throw new JSONException("HTTP response cannot be parsed to JSON object:\n" + text, e);
        }
    }

    public static boolean updateFolder(final WebConversation conversation, final String hostname, final String sessionId, final String entityArg, final String secondEntityArg, final int folderId, final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException {
        return updateFolder(conversation, null, hostname, sessionId, entityArg, secondEntityArg, folderId, timestamp);
    }

    public static boolean updateFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final String entityArg, final String secondEntityArg, final int folderId, final long timestamp) throws JSONException, MalformedURLException, IOException, SAXException {
        return updateFolder(conversation, protocol, hostname, sessionId, entityArg, secondEntityArg, folderId, timestamp, 0);
    }

    public static boolean updateFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final String entityArg, final String secondEntityArg, final int folderId, final long timestamp, final int permissions) throws JSONException, MalformedURLException, IOException, SAXException {
        final String entity = entityArg.indexOf('@') == -1 ? entityArg : entityArg.substring(0, entityArg.indexOf('@'));
        final String secondEntity;
        if (secondEntityArg == null) {
            secondEntity = null;
        } else {
            secondEntity = secondEntityArg.indexOf('@') == -1 ? secondEntityArg : secondEntityArg.substring(0, secondEntityArg.indexOf('@'));
        }
        final JSONObject jsonFolder = new JSONObject();
        jsonFolder.put("id", folderId);
        final JSONArray perms = new JSONArray();
        JSONObject jsonPermission = new JSONObject();
        jsonPermission.put("entity", entity);
        jsonPermission.put("group", false);
        jsonPermission.put("bits", (0 == permissions) ? createPermissionBits(8, 8, 8, 8, true) : permissions);
        perms.put(jsonPermission);
        if (null != secondEntity) {
            jsonPermission = new JSONObject();
            jsonPermission.put("entity", secondEntity);
            jsonPermission.put("group", false);
            jsonPermission.put("bits", createPermissionBits(4, 0, 0, 0, false));
            perms.put(jsonPermission);
        }
        jsonFolder.put("permissions", perms);
        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(AJAXServlet.PARAMETER_ID, Integer.toString(folderId));
        urlParam.setParameter("timestamp", String.valueOf(timestamp));
        final byte[] bytes = jsonFolder.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest req = new PutMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL + urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (respObj.has("error")) {
            return false;
        }
        return true;
    }

    public static boolean moveFolder(final WebConversation conversation, final String hostname, final String sessionId, final String folderId, final String tgtFolderId, final long timestamp, final boolean printOutput) throws JSONException, MalformedURLException, IOException, SAXException {
        return moveFolder(conversation, null, hostname, sessionId, folderId, tgtFolderId, timestamp);
    }

    public static boolean moveFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final String folderId, final String tgtFolderId, final long timestamp) throws JSONException, MalformedURLException, IOException, SAXException {
        final JSONObject jsonFolder = new JSONObject();
        jsonFolder.put("id", folderId);
        jsonFolder.put("folder_id", tgtFolderId);
        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(folderId));
        urlParam.setParameter("timestamp", String.valueOf(timestamp));
        final byte[] bytes = jsonFolder.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        final ByteArrayInputStream bais = new UnsynchronizedByteArrayInputStream(bytes);
        final WebRequest req = new PutMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL + urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (respObj.has("error")) {
            return false;
        }
        return true;
    }

    public static int[] deleteFolders(final WebConversation conversation, final String hostname, final String sessionId, final int[] folderIds, final long timestamp, final boolean printOutput) throws JSONException, IOException, SAXException {
        return deleteFolders(conversation, null, hostname, sessionId, folderIds, timestamp);
    }

    public static int[] deleteFolders(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final int[] folderIds, final long timestamp) throws JSONException, IOException, SAXException {
        final JSONArray deleteIds = new JSONArray(Arrays.toString(folderIds));
        final byte[] bytes = deleteIds.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        final ByteArrayInputStream bais = new UnsynchronizedByteArrayInputStream(bytes);
        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp));
        final WebRequest req = new PutMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL + urlParam.getURLParameters(), bais, "text/javascript; charset=UTF-8");
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (respObj.has("error")) {
            throw new JSONException("JSON Response object contains an error: " + respObj.getString("error"));
        }
        final JSONArray arr = respObj.getJSONArray("data");
        final int[] retval = new int[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            retval[i] = arr.getInt(i);
        }
        return retval;
    }

    public static int[] clearFolder(final WebConversation conversation, final String hostname, final String sessionId, final int[] folderIds, final long timestamp) throws JSONException, IOException, SAXException {
        return clearFolder(conversation, null, hostname, sessionId, folderIds, timestamp);
    }

    public static int[] clearFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final int[] folderIds, final long timestamp) throws JSONException, IOException, SAXException {
        final JSONArray clearIds = new JSONArray(Arrays.toString(folderIds));

        final URLParameter urlParam = new URLParameter();
        urlParam.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_CLEAR);
        urlParam.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        urlParam.setParameter(AJAXServlet.PARAMETER_TIMESTAMP, Long.toString(timestamp));

        final WebRequest req = new PutMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + FOLDER_URL + urlParam.getURLParameters(), new UnsynchronizedByteArrayInputStream(clearIds.toString().getBytes(com.openexchange.java.Charsets.UTF_8)), "text/javascript; charset=UTF-8");

        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (respObj.has("error")) {
            throw new JSONException("JSON Response object contains an error: " + respObj.getString("error"));
        }
        final JSONArray arr = respObj.getJSONArray("data");
        final int[] retval = new int[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            retval[i] = arr.getInt(i);
        }
        return retval;
    }

    public static FolderObject getStandardFolder(final int module, final String protocol, final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, OXException, OXException, IOException, SAXException, JSONException {
        final List<FolderObject> subfolders = getSubfolders(conversation, protocol, hostname, sessionId, Integer.toString(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), true);
        if (null != subfolders && 0 < subfolders.size()) {
            for (final FolderObject subfolder : subfolders) {
                if (module == subfolder.getModule() && subfolder.isDefaultFolder()) {
                    return subfolder;
                }
            }
        }
        throw OXException.general(String.format("No standard folder for module '%d' found", module));
    }

    public static FolderObject getStandardTaskFolder(final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        return getStandardTaskFolder(conversation, null, hostname, sessionId);
    }

    public static FolderObject getStandardTaskFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        final List<FolderObject> subfolders = getSubfolders(conversation, protocol, hostname, sessionId, "" + FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true);
        for (Object element : subfolders) {
            final FolderObject subfolder = (FolderObject) element;
            if (subfolder.getModule() == FolderObject.TASK && subfolder.isDefaultFolder()) {
                return subfolder;
            }
        }
        throw OXException.general("No Standard Task Folder found!");
    }

    public static FolderObject getStandardCalendarFolder(final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        return getStandardCalendarFolder(conversation, null, hostname, sessionId);
    }

    public static FolderObject getStandardCalendarFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        final List<FolderObject> subfolders = getSubfolders(conversation, protocol, hostname, sessionId, "" + FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true);
        for (FolderObject subfolder : subfolders) {
            if (subfolder.getModule() == FolderObject.CALENDAR && subfolder.isDefaultFolder()) {
                return subfolder;
            }
        }
        throw OXException.general("No Standard Calendar Folder found!");
    }

    public static FolderObject getStandardContactFolder(final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        return getStandardContactFolder(conversation, null, hostname, sessionId);
    }

    public static FolderObject getStandardContactFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        final List<FolderObject> subfolders = getSubfolders(conversation, protocol, hostname, sessionId, FolderStorage.PRIVATE_ID, true);
        for (final FolderObject subfolder : subfolders) {
            if (subfolder.getModule() == FolderObject.CONTACT && subfolder.isDefaultFolder()) {
                return subfolder;
            }
        }
        throw OXException.general("No Standard Contact Folder found!");
    }

    public static FolderObject getStandardInfostoreFolder(final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        return getStandardInfostoreFolder(conversation, null, hostname, sessionId);
    }

    public static FolderObject getStandardInfostoreFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException {
        final List<FolderObject> subfolders = getSubfolders(conversation, protocol, hostname, sessionId, "" + FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true);
        for (Object element : subfolders) {
            final FolderObject subfolder = (FolderObject) element;
            if (subfolder.getModule() == FolderObject.INFOSTORE && subfolder.isDefaultFolder()) {
                return subfolder;
            }
        }
        throw OXException.general("No Standard Infostore Folder found!");
    }

    public static FolderObject getMyInfostoreFolder(final WebConversation conversation, final String hostname, final String sessionId, final int loginId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException, OXException {
        return getMyInfostoreFolder(conversation, null, hostname, sessionId, loginId);
    }

    public static FolderObject getMyInfostoreFolder(final WebConversation conversation, final String protocol, final String hostname, final String sessionId, final int loginId) throws MalformedURLException, IOException, SAXException, JSONException, OXException, OXException, OXException {
        FolderObject infostore = null;
        List<FolderObject> l = getRootFolders(conversation, protocol, hostname, sessionId, false);
        for (FolderObject rf : l) {
            if (rf.getObjectID() == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                infostore = rf;
                break;
            }
        }
        if (null == infostore) {
            throw new TestException("System infostore folder not found!");
        }
        FolderObject userStore = null;
        l = getSubfolders(conversation, protocol, hostname, sessionId, Integer.toString(infostore.getObjectID()));
        for (FolderObject f : l) {
            if (f.getObjectID() == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
                userStore = f;
                break;
            }
        }
        if (null == userStore) {
            throw new TestException("System user store folder not found!");
        }
        l = getSubfolders(conversation, protocol, hostname, sessionId, Integer.toString(userStore.getObjectID()));
        for (FolderObject f : l) {
            if (f.containsDefaultFolder() && f.isDefaultFolder() && f.getCreator() == loginId) {
                return f;
            }
        }
        throw new TestException("Private infostore folder not found!");
    }

    @Override
    public void setUp() throws Exception {
        sessionId = getSessionId();
    }

    @Override
    public void tearDown() throws Exception {
        logout();
        super.tearDown();

    }

    public void testGetUserId() throws OXException, OXException, IOException, SAXException, JSONException {
        getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
    }

    public void testGetRootFolders() throws OXException, IOException, SAXException, JSONException, OXException {
        final int[] assumedIds = { 1, 2, 3, 9 };
        final List<FolderObject> l = getRootFolders(getWebConversation(), getHostName(), getSessionId(), true);
        assertFalse(l == null || l.size() == 0);
        int i = 0;
        for (Object element : l) {
            final FolderObject rf = (FolderObject) element;
            assertTrue(rf.getObjectID() == assumedIds[i]);
            i++;
        }
    }

    public void testDeleteFolder() throws OXException, JSONException, IOException, SAXException, OXException, OXException {
        final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
        final int parent = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "DeleteMeImmediately" + System.currentTimeMillis(), "calendar", FolderObject.PUBLIC, -1, true);
        assertFalse(parent == -1);
        final Calendar cal = GregorianCalendar.getInstance();
        getFolder(getWebConversation(), getHostName(), getSessionId(), "" + parent, cal, true);

        final int child01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, parent, "DeleteMeImmediatelyChild01" + System.currentTimeMillis(), "calendar", FolderObject.PUBLIC, -1, true);
        assertFalse(child01 == -1);
        getFolder(getWebConversation(), getHostName(), getSessionId(), "" + child01, cal, true);

        final int child02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, parent, "DeleteMeImmediatelyChild02" + System.currentTimeMillis(), "calendar", FolderObject.PUBLIC, -1, true);
        assertFalse(child02 == -1);
        getFolder(getWebConversation(), getHostName(), getSessionId(), "" + child02, cal, true);

        final int[] failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { parent }, cal.getTimeInMillis(), true);
        assertTrue((failedIds == null || failedIds.length == 0));
    }

    public void testCheckFolderPermissions() throws OXException, OXException, IOException, SAXException, JSONException, OXException {
        final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
        final int fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "CheckMyPermissions", "calendar", FolderObject.PUBLIC, -1, true);
        final Calendar cal = GregorianCalendar.getInstance();
        getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
        updateFolder(getWebConversation(), getHostName(), getSessionId(), getLogin(), getSeconduser(), fuid, cal.getTimeInMillis(), true);
        getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
        deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
    }

    public void testInsertRenameFolder() throws OXException, OXException, IOException, SAXException, JSONException, OXException, OXException {
        int fuid = -1;
        int[] failedIds = null;
        boolean updated = false;
        try {
            final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
            fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "NewPrivateFolder" + System.currentTimeMillis(), "calendar", FolderObject.PRIVATE, -1, true);
            assertFalse(fuid == -1);
            final Calendar cal = GregorianCalendar.getInstance();
            getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
            updated = renameFolder(getWebConversation(), getHostName(), getSessionId(), fuid, "ChangedPrivateFolderName" + System.currentTimeMillis(), "calendar", FolderObject.PRIVATE, cal.getTimeInMillis(), true);
            assertTrue(updated);
            getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
            failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
            assertFalse((failedIds != null && failedIds.length > 0));
            fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "NewPublicFolder" + System.currentTimeMillis(), "calendar", FolderObject.PRIVATE, -1, true);
            assertFalse(fuid == -1);
            getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
            failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
            assertFalse((failedIds != null && failedIds.length > 0));
            fuid = -1;
            final FolderObject myInfostore = getMyInfostoreFolder(getWebConversation(), getHostName(), getSessionId(), userId);
            fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, myInfostore.getObjectID(), "NewInfostoreFolder" + System.currentTimeMillis(), "infostore", FolderObject.PUBLIC, -1, true);
            assertFalse(fuid == -1);
            getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
            updated = renameFolder(getWebConversation(), getHostName(), getSessionId(), fuid, "ChangedInfostoreFolderName" + System.currentTimeMillis(), "infostore", FolderObject.PUBLIC, cal.getTimeInMillis(), true);
            assertTrue(updated);
            getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
            failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
            assertFalse((failedIds != null && failedIds.length > 0));
            fuid = -1;
        } finally {
            try {
                if (fuid != -1) {
                    final Calendar cal = GregorianCalendar.getInstance();
                    /*
                     * Call getFolder to receive a valid timestamp for deletion
                     */
                    getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, false);
                    deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), false);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void testGetSubfolder() throws OXException, OXException, IOException, SAXException, JSONException, OXException {
        int fuid = -1;
        int[] subfuids = null;
        try {
            /*
             * Create a temp folder with subfolders
             */
            final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
            fuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "NewPrivateFolder" + System.currentTimeMillis(), "calendar", FolderObject.PRIVATE, -1, true);
            final DecimalFormat df = new DecimalFormat("00");
            subfuids = new int[3];
            for (int i = 0; i < subfuids.length; i++) {
                subfuids[i] = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, fuid, "NewPrivateSubFolder" + String.valueOf(System.currentTimeMillis()) + "_" + df.format((i + 1)), "calendar", FolderObject.PRIVATE, -1, true);
            }
            /*
             * Get subfolder list
             */
            final List<FolderObject> l = getSubfolders(getWebConversation(), getHostName(), getSessionId(), "" + fuid, true);
            assertFalse(l == null || l.size() == 0);
            int i = 0;
            for (Object element : l) {
                final FolderObject subFolder = (FolderObject) element;
                assertTrue(subFolder.getObjectID() == subfuids[i]);
                i++;
            }
        } finally {
            if (fuid != -1) {
                final Calendar cal = GregorianCalendar.getInstance();
                /*
                 * Call getFolder to receive a valid timestamp for deletion
                 */
                getFolder(getWebConversation(), getHostName(), getSessionId(), "" + fuid, cal, true);
                final int[] failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { fuid }, cal.getTimeInMillis(), true);
                if (failedIds != null && failedIds.length > 0) {
                    if (subfuids != null) {
                        for (int subfuid : subfuids) {
                            if (subfuid > 0) {
                                /*
                                 * Call getFolder to receive a valid timestamp for deletion
                                 */
                                getFolder(getWebConversation(), getHostName(), getSessionId(), "" + subfuid, cal, true);
                                deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { subfuid }, cal.getTimeInMillis(), true);
                            }
                        }
                    }
                }
            }
        }
    }

    public void testMoveFolder() throws OXException, OXException, IOException, SAXException, JSONException, OXException {
        int parent01 = -1;
        int parent02 = -1;
        int moveFuid = -1;
        int[] failedIds = null;
        boolean moved = false;
        final int userId = getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
        parent01 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Parent01" + System.currentTimeMillis(), "calendar", FolderObject.PRIVATE, -1, true);
        assertFalse(parent01 == -1);
        parent02 = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Parent02" + System.currentTimeMillis(), "calendar", FolderObject.PRIVATE, -1, true);
        assertFalse(parent02 == -1);
        moveFuid = insertFolder(getWebConversation(), getHostName(), getSessionId(), userId, false, parent01, "MoveMe" + System.currentTimeMillis(), "calendar", FolderObject.PRIVATE, -1, true);
        assertFalse(moveFuid == -1);
        final Calendar cal = GregorianCalendar.getInstance();
        getFolder(getWebConversation(), getHostName(), getSessionId(), "" + moveFuid, cal, true);
        moved = moveFolder(getWebConversation(), getHostName(), getSessionId(), "" + moveFuid, "" + parent02, cal.getTimeInMillis(), true);
        assertTrue(moved);
        FolderObject movedFolderObj = null;
        movedFolderObj = getFolder(getWebConversation(), getHostName(), getSessionId(), "" + moveFuid, cal, true);
        assertTrue(movedFolderObj.containsParentFolderID() ? movedFolderObj.getParentFolderID() == parent02 : true);
        failedIds = deleteFolders(getWebConversation(), getHostName(), getSessionId(), new int[] { parent01, parent02 }, cal.getTimeInMillis(), true);
        assertFalse((failedIds != null && failedIds.length > 0));
    }

    public void testFolderNamesShouldBeEqualRegardlessOfRequestMethod() {
        try {
            final List<FolderObject> rootFolders = getRootFolders(getWebConversation(), getHostName(), getSessionId(), true);
            for (final FolderObject rootFolder : rootFolders) {
                final FolderObject individuallyLoaded = getFolder(getWebConversation(), getHostName(), getSessionId(), "" + rootFolder.getObjectID(), Calendar.getInstance(), true);
                assertEquals("Foldernames differ : " + rootFolder.getFolderName() + " != " + individuallyLoaded.getFolderName(), rootFolder.getFolderName(), individuallyLoaded.getFolderName());
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    // Node 2652

    public void testLastModifiedUTCInGet() throws JSONException, OXException, IOException, SAXException {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);
        // Load an existing folder
        final GetRequest getRequest = new GetRequest(EnumAPI.OX_OLD, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, new int[] { FolderObject.LAST_MODIFIED_UTC });
        final GetResponse response = Executor.execute(client, getRequest);
        assertTrue(((JSONObject) response.getData()).has("last_modified_utc"));
    }

    // Node 2652

    public void testLastModifiedUTCInList() throws JSONException, IOException, SAXException, OXException {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);
        // List known folder
        final ListRequest listRequest = new ListRequest(EnumAPI.OX_OLD, "" + FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, new int[] { FolderObject.LAST_MODIFIED_UTC }, false);
        final ListResponse listResponse = client.execute(listRequest);
        final JSONArray arr = (JSONArray) listResponse.getData();
        final int size = arr.length();
        assertTrue(size > 0);
        for (int i = 0; i < size; i++) {
            final JSONArray row = arr.optJSONArray(i);
            assertNotNull(row);
            assertTrue(row.length() == 1);
            assertNotNull(row.get(0));
        }
    }

    // Node 2652

    public void testLastModifiedUTCInUpdates() throws JSONException, OXException, IOException, SAXException {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);
        // List known folder
        final UpdatesRequest updatesRequest = new UpdatesRequest(EnumAPI.OX_OLD, new int[] { FolderObject.LAST_MODIFIED_UTC }, -1, null, new Date(0));
        // final AbstractAJAXResponse response = Executor.execute(client, updatesRequest);
        //
        // final JSONArray arr = (JSONArray) response.getData();
        // final int size = arr.length();
        // assertTrue(size > 0);
        // for (int i = 0; i < size; i++) {
        // final JSONArray row = arr.optJSONArray(i);
        // assertNotNull(row);
        // assertTrue(row.length() == 1);
        // assertNotNull(row.get(0));
        // }
        final FolderUpdatesResponse response = Executor.execute(client, updatesRequest);

        final JSONArray arr = (JSONArray) response.getData();
        final int size = arr.length();
        assertTrue(size > 0);
        for (int i = 0; i < size; i++) {
            final JSONArray row = arr.optJSONArray(i);
            assertNotNull(row);
            assertTrue(row.length() == 1);
            assertNotNull(row.get(0));
        }
    }
}
