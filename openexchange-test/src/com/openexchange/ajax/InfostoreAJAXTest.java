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

import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.TestInit;

public class InfostoreAJAXTest extends AbstractAJAXTest {

    protected static final int[] virtualFolders = { FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    public static final String INFOSTORE_FOLDER = "infostore.folder";

    protected int folderId;

    protected List<String> clean = new ArrayList<String>();

    protected String hostName = null;

    protected String sessionId;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.sessionId = getClient().getSession().getId();
        final int userId = getClient().getValues().getUserId();
        this.folderId = createFolderForTest(userId);

        com.openexchange.file.storage.File createdFile = createFileOnServer(folderId, "test knowledge", "text/javascript");
        clean.add(createdFile.getId());

        com.openexchange.file.storage.File createdFile2 = createFileOnServer(folderId, "test url", "text/javascript");
        clean.add(createdFile2.getId());
    }

    private int createFolderForTest(final int userId) throws JSONException, OXException, IOException {
        final int parent = getClient().getValues().getPrivateInfostoreFolder();
        FolderObject folder = new FolderObject("NewInfostoreFolder" + System.currentTimeMillis(), parent, Module.INFOSTORE.getFolderConstant(), FolderObject.PUBLIC, userId);
        return ftm.insertFolderOnServer(folder).getObjectID();
    }

    // Methods from the specification

    public Response all(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
        return all(webConv, hostname, sessionId, folderId, columns, -1, null);
    }

    public Response all(final WebConversation webConv, final String protocol, final String hostname, final String sessionId, final int folderId, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "all", hostname, protocol);
        url.append("&folder=");
        url.append(folderId);
        url.append("&columns=");
        for (final int col : columns) {
            url.append(col);
            url.append(',');
        }
        url.deleteCharAt(url.length() - 1);

        if (sort != -1) {
            url.append("&sort=");
            url.append(sort);
        }

        if (order != null) {
            url.append("&order=");
            url.append(order);
        }

        return gT(webConv, url.toString());
    }

    public Response all(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
        return all(webConv, null, hostname, sessionId, folderId, columns, sort, order);
    }

    public Response list(final WebConversation webConv, final String hostname, final String sessionId, final int[] columns, final String[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "list", hostname);
        url.append("&columns=");
        for (final int col : columns) {
            url.append(col);
            url.append(',');
        }
        url.deleteCharAt(url.length() - 1);

        final StringBuffer data = new StringBuffer("[");
        if (ids.length > 0) {
            for (final String[] tuple : ids) {
                data.append("{folder:\"");
                data.append(tuple[0]);
                data.append("\", id:\"");
                data.append(tuple[1]);
                data.append("\"},");
            }
            data.deleteCharAt(data.length() - 1);
        }
        data.append(']');

        return putT(webConv, url.toString(), data.toString());
    }

    public Response updates(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final long timestamp) throws MalformedURLException, JSONException, IOException, SAXException {
        return updates(webConv, hostname, sessionId, folderId, columns, timestamp, -1, null, null);
    }

    public Response updates(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final long timestamp, final String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
        return updates(webConv, hostname, sessionId, folderId, columns, timestamp, -1, null, ignore);
    }

    public Response updates(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns, final long timestamp, final int sort, final String order, final String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "updates", hostname);
        url.append("&folder=");
        url.append(folderId);
        url.append("&columns=");
        for (final int col : columns) {
            url.append(col);
            url.append(',');
        }
        url.deleteCharAt(url.length() - 1);

        url.append("&timestamp=");
        url.append(timestamp);

        if (sort != -1) {
            url.append("&sort=");
            url.append(sort);
        }

        if (order != null) {
            url.append("&order=");
            url.append(order);
        }

        if (ignore != null) {
            url.append("&ignore=");
            url.append(ignore);
        }

        return gT(webConv, url.toString());
    }

    public Response versions(final WebConversation webConv, final String hostname, final String sessionId, final String objectId, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "versions", hostname);
        url.append("&id=");
        url.append(objectId);
        url.append("&columns=");
        for (final int col : columns) {
            url.append(col);
            url.append(',');
        }
        url.deleteCharAt(url.length() - 1);

        if (sort != -1) {
            url.append("&sort=");
            url.append(sort);
        }

        if (order != null) {
            url.append("&order=");
            url.append(order);
        }

        return gT(webConv, url.toString());
    }

    private JSONObject toJSONArgs(final Map<String, String> modified) throws JSONException {
        final JSONObject obj = new JSONObject();
        for (final String attr : modified.keySet()) {
            obj.put(attr, modified.get(attr));
        }
        return obj;
    }

    public UpdateInfostoreResponse update(File data, Field[] fields, final long timestamp) throws MalformedURLException, IOException, JSONException, OXException {
        UpdateInfostoreRequest updateRequest = new UpdateInfostoreRequest(data, fields, new Date(timestamp));
        UpdateInfostoreResponse updateResponse = getClient().execute(updateRequest);

        return updateResponse;
    }

    public Response update(final WebConversation webConv, final String hostname, final String sessionId, final String id, final long timestamp, final Map<String, String> modified, final java.io.File upload, final String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
        final StringBuffer url = getUrl(sessionId, "update", hostname);
        url.append("&id=");
        url.append(id);

        url.append("&timestamp=");
        url.append(timestamp);

        final PostMethodWebRequest req = new PostMethodWebRequest(url.toString(), true);

        final JSONObject obj = toJSONArgs(modified);

        req.setParameter("json", obj.toString());

        if (upload != null) {
            req.selectFile("file", upload, contentType);
        }
        final WebResponse resp = webConv.getResource(req);
        final JSONObject res = extractFromCallback(resp.getText());
        return Response.parse(res.toString());
    }

    public String saveAs(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int attached, final int module, final int attachment, final Map<String, String> fields) throws MalformedURLException, IOException, SAXException, JSONException {
        final StringBuffer url = getUrl(sessionId, "saveAs", hostname);
        url.append("&folder=");
        url.append(folderId);
        url.append("&attached=");
        url.append(attached);
        url.append("&module=");
        url.append(module);
        url.append("&attachment=");
        url.append(attachment);
        final JSONObject obj = toJSONArgs(fields);

        final PutMethodWebRequest m = new PutMethodWebRequest(url.toString(), new ByteArrayInputStream(obj.toString().getBytes()), "text/javascript");

        final WebResponse resp = webConv.getResponse(m);
        final Response res = Response.parse(resp.getText());
        if (res.hasError()) {
            throw new JSONException(res.getErrorMessage());
        }
        return res.getData().toString();
    }

    public int[] detach(final WebConversation webConv, final String hostname, final String sessionId, final long timestamp, final String objectId, final int[] versions) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "detach", hostname);
        url.append("&timestamp=");
        url.append(timestamp);
        url.append("&id=");
        url.append(objectId);

        final StringBuffer data = new StringBuffer("[");

        if (versions.length > 0) {
            for (final int id : versions) {
                data.append(id);
                data.append(',');
            }
            data.deleteCharAt(data.length() - 1);
        }

        data.append(']');

        final String content = putS(webConv, url.toString(), data.toString());
        JSONArray arr = null;
        try {
            final JSONObject response = new JSONObject(content);
            arr = response.getJSONArray("data");
            if (!response.has("error")) {
                assertNotNull(response.opt(ResponseFields.TIMESTAMP)); // FIXME!
            }
        } catch (final JSONException x) {
            final Response res = Response.parse(content);
            if (res.hasError()) {
                throw new IOException(res.getErrorMessage());
            }
        }
        final int[] notDeleted = new int[arr.length()];

        for (int i = 0; i < arr.length(); i++) {
            notDeleted[i] = arr.getInt(i);
        }
        return notDeleted;
    }

    public Response revert(final WebConversation webConv, final String hostname, final String sessionId, final long timestamp, final String objectId) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "revert", hostname);
        url.append("&timestamp=");
        url.append(timestamp);
        url.append("&id=");
        url.append(objectId);

        return gT(webConv, url.toString());
    }

    public InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final String id) throws HttpException, IOException {
        return document(webConv, hostname, sessionId, id, -1, null);
    }

    public InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final String id, final String contentType) throws HttpException, IOException {
        return document(webConv, hostname, sessionId, id, -1, contentType);
    }

    public InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final String id, final int version) throws HttpException, IOException {
        return document(webConv, hostname, sessionId, id, version, null);
    }

    public InputStream document(final WebConversation webConv, final String hostname, final String sessionId, final String id, final int version, final String contentType) throws HttpException, IOException {

        final GetMethodWebRequest m = documentRequest(sessionId, hostname, id, version, contentType);
        final WebResponse resp = webConv.getResource(m);

        return resp.getInputStream();
    }

    public GetMethodWebRequest documentRequest(final String sessionId, final String hostname, final String id, final int version, String contentType) {
        final StringBuffer url = getUrl(sessionId, "document", hostname);
        url.append("&id=" + id);
        if (version != -1) {
            url.append("&version=" + version);
        }

        if (null != contentType) {
            contentType = contentType.replaceAll("/", "%2F");
            url.append("&content_type=");
            url.append(contentType);
        }
        return new GetMethodWebRequest(url.toString());
    }

    public String copy(final WebConversation webConv, final String hostname, final String sessionId, final String id, String folder, final long timestamp, final Map<String, String> modified, final java.io.File upload, final String contentType) throws JSONException, IOException {
        final StringBuffer url = getUrl(sessionId, "copy", hostname);
        url.append("&id=");
        url.append(id);
        url.append("&folder=");
        url.append(folder);

        url.append("&timestamp=");
        url.append(timestamp);

        final PostMethodWebRequest req = new PostMethodWebRequest(url.toString(), true);

        final JSONObject obj = new JSONObject();
        for (final String attr : modified.keySet()) {
            obj.put(attr, modified.get(attr));
        }

        req.setParameter("json", obj.toString());

        if (upload != null) {
            req.selectFile("file", upload, contentType);
        }
        final WebResponse resp = webConv.getResource(req);
        final JSONObject res = extractFromCallback(resp.getText());
        return Response.parse(res.toString()).getData().toString();
    }

    public String copy(final WebConversation webConv, final String hostname, final String sessionId, final String id, String folder, final long timestamp, final Map<String, String> modified) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "copy", hostname);
        url.append("&id=");
        url.append(id);
        url.append("&folder=");
        url.append(folder);

        url.append("&timestamp=");
        url.append(timestamp);
        final JSONObject obj = new JSONObject();
        for (final String attr : modified.keySet()) {
            obj.put(attr, modified.get(attr));
        }

        final Response res = putT(webConv, url.toString(), obj.toString());
        if (res.hasError()) {
            throw new JSONException(res.getErrorMessage());
        }
        return res.getData().toString();
    }

    public Response lock(final WebConversation webConv, final String hostname, final String sessionId, final String id, final long timeDiff) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "lock", hostname);
        url.append("&id=");
        url.append(id);
        if (timeDiff > 0) {
            url.append("&diff=");
            url.append(timeDiff);
        }

        return gT(webConv, url.toString());
    }

    public Response lock(final WebConversation webConv, final String hostname, final String sessionId, final String id) throws MalformedURLException, JSONException, IOException, SAXException {
        return lock(webConv, hostname, sessionId, id, -1);
    }

    public Response unlock(final WebConversation webConv, final String hostname, final String sessionId, final String id) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "unlock", hostname);
        url.append("&id=");
        url.append(id);

        return gT(webConv, url.toString());
    }

    public Response search(final WebConversation webConv, final String hostname, final String sessionId, final String query, final int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
        return search(webConv, hostname, sessionId, query, columns, -1, -1, null, -1, -1);
    }

    public Response search(final WebConversation webConv, final String hostname, final String sessionId, final String query, final int[] columns, final int folderId) throws JSONException, IOException, SAXException {
        return search(webConv, hostname, sessionId, query, columns, folderId, -1, null, -1, -1);
    }

    public Response search(final WebConversation webConv, final String hostname, final String sessionId, final String query, final int[] columns, final int folderId, final int sort, final String order, final int start, final int end) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "search", hostname);
        url.append("&columns=");
        for (final int c : columns) {
            url.append(c);
            url.append(',');
        }
        url.setLength(url.length() - 1);
        if (folderId != -1) {
            url.append("&folder=");
            url.append(folderId);
        }

        if (sort != -1) {
            url.append("&sort=");
            url.append(sort);

            url.append("&order=");
            url.append(order);

            if (start != -1) {
                url.append("&start=");
                url.append(start);
            }

            if (end != -1) {
                url.append("&end=");
                url.append(end);
            }
        }
        final JSONObject queryObject = new JSONObject();
        queryObject.put("pattern", query);
        return putT(webConv, url.toString(), queryObject.toString());
    }

    public Response search(final WebConversation webConv, final String hostname, final String sessionId, final String query, final int[] columns, final int folderId, final int sort, final String order, final int limit) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "search", hostname);
        url.append("&columns=");
        for (final int c : columns) {
            url.append(c);
            url.append(',');
        }
        url.setLength(url.length() - 1);
        if (folderId != -1) {
            url.append("&folder=");
            url.append(folderId);
        }

        if (sort != -1) {
            url.append("&sort=");
            url.append(sort);

            url.append("&order=");
            url.append(order);

            url.append("&limit=");
            url.append(limit);
        }
        final JSONObject queryObject = new JSONObject();
        queryObject.put("pattern", query);
        return putT(webConv, url.toString(), queryObject.toString());
    }

    protected StringBuffer getUrl(final String sessionId, final String action, final String hostname) {
        return getUrl(sessionId, action, hostname, null);
    }

    protected StringBuffer getUrl(final String sessionId, final String action, final String hostname, final String protocol) {
        final StringBuffer url = new StringBuffer((protocol != null) ? protocol : "http");
        url.append("://");
        url.append((hostname == null) ? getHostName() : hostname);
        url.append("/ajax/infostore?session=");
        url.append(sessionId);
        url.append("&action=");
        url.append(action);
        return url;
    }

    @Override
    public String getHostName() {
        if (null == hostName) {
            return super.getHostName();
        }
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public File createFileOnServer(int folderId, String fileName, String mimeType) throws Exception {
        long now = System.currentTimeMillis();
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(folderId));
        file.setTitle(fileName + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        file.setFileMIMEType(mimeType);
        itm.newAction(file, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
        return file;
    }

    public File createFile(int folderId, String fileName) throws Exception {
        long now = System.currentTimeMillis();
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(folderId));
        file.setTitle(fileName + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        return file;
    }
}
