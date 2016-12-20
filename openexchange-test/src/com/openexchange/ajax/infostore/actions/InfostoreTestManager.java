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

package com.openexchange.ajax.infostore.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.actions.VersionsRequest;
import com.openexchange.ajax.folder.actions.VersionsResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest.ListItem;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.UUIDs;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class InfostoreTestManager {

    private final Set<File> createdEntities;

    private AJAXClient client;

    private boolean failOnError;

    private AbstractAJAXResponse lastResponse;

    public InfostoreTestManager() {
        createdEntities = new HashSet<File>();
    }

    public InfostoreTestManager(AJAXClient client) {
        this();
        setClient(client);
    }

    public Set<File> getCreatedEntities() {
        return this.createdEntities;
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean getFailOnError() {
        return failOnError;
    }

    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }

    public void cleanUp() throws OXException, IOException, SAXException, JSONException {
        List<String> objectIDs = new ArrayList<String>(createdEntities.size());
        List<String> folderIDs = new ArrayList<String>(createdEntities.size());
        for (File metadata : createdEntities) {
            objectIDs.add(metadata.getId());
            folderIDs.add(metadata.getFolderId());
        }
        deleteAction(objectIDs, folderIDs, new Date(Long.MAX_VALUE), Boolean.TRUE);
        createdEntities.clear();
    }

    public void removeFromCreatedEntities(Collection<String> ids) {
        for (String id : ids) {
            for (File data : new HashSet<File>(createdEntities)) {
                if (data.getId() == id) {
                    createdEntities.remove(data);
                }
            }
        }
    }

    public void newAction(File data) throws OXException, IOException, SAXException, JSONException {
        NewInfostoreRequest newRequest = new NewInfostoreRequest(data);
        newRequest.setFailOnError(getFailOnError());
        NewInfostoreResponse newResponse = getClient().execute(newRequest);
        lastResponse = newResponse;
        data.setId(newResponse.getID());
        createdEntities.add(data);
    }

    /*
     * The following is not beautiful, but the request/response framework
     * doesn't seem to offer a solution to do POST requests containing files.
     */
    public void newAction(File data, java.io.File upload) throws OXException, IOException, SAXException, JSONException {
        NewInfostoreRequest newRequest = new NewInfostoreRequest(data, upload);
        newRequest.setFailOnError(getFailOnError());
        NewInfostoreResponse newResponse = getClient().execute(newRequest);
        lastResponse = newResponse;
        data.setId(newResponse.getID());
        createdEntities.add(data);
    }

    public String copyAction(String id, String folderId, File data) throws OXException, IOException, JSONException {
        CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(id, folderId, data);
        copyRequest.setFailOnError(getFailOnError());
        CopyInfostoreResponse copyResponse = getClient().execute(copyRequest);
        lastResponse = copyResponse;
        return copyResponse.getID();
    }

    public Object getConfigAction(String name) throws OXException, IOException, JSONException {
        GetInfostoreConfigRequest req = new GetInfostoreConfigRequest(name);
        req.setFailOnError(getFailOnError());
        AbstractAJAXResponse resp = getClient().execute(req);
        lastResponse = resp;
        return resp.getResponse().getData();
    }

    public void updateAction(File data, Field[] fields, Date timestamp) throws OXException, IOException, JSONException {
        UpdateInfostoreRequest updateRequest = new UpdateInfostoreRequest(data, fields, timestamp);
        updateRequest.setFailOnError(getFailOnError());
        UpdateInfostoreResponse updateResponse = getClient().execute(updateRequest);
        lastResponse = updateResponse;
    }

    public void updateAction(File data, java.io.File file, Field[] fields, Date timestamp) throws OXException, IOException, JSONException {
        UpdateInfostoreRequest updateRequest = new UpdateInfostoreRequest(data, fields, file, timestamp);
        updateRequest.setFailOnError(getFailOnError());
        UpdateInfostoreResponse updateResponse = getClient().execute(updateRequest);
        lastResponse = updateResponse;
    }

    public void deleteAction(List<String> ids, List<String> folders, Date timestamp) throws OXException, IOException, JSONException {
        deleteAction(ids, folders, timestamp, null);
    }

    public void deleteAction(List<String> ids, List<String> folders, Date timestamp, Boolean hardDelete) throws OXException, IOException, JSONException {
        DeleteInfostoreRequest deleteRequest = new DeleteInfostoreRequest(ids, folders, timestamp);
        deleteRequest.setHardDelete(hardDelete);
        deleteRequest.setFailOnError(getFailOnError());
        DeleteInfostoreResponse deleteResponse = getClient().execute(deleteRequest);
        lastResponse = deleteResponse;
        removeFromCreatedEntities(ids);
    }

    public void deleteAction(String id, String folder, Date timestamp) throws OXException, IOException, SAXException, JSONException {
        deleteAction(Arrays.asList(id), Arrays.asList(folder), timestamp);
    }

    public void deleteAction(File data) throws OXException, IOException, SAXException, JSONException {
        deleteAction(data.getId(), data.getFolderId(), data.getLastModified());
    }

    public File getAction(String id) throws OXException, JSONException, IOException {
        GetInfostoreRequest getRequest = new GetInfostoreRequest(id);
        getRequest.setFailOnError(getFailOnError());
        GetInfostoreResponse getResponse = getClient().execute(getRequest);
        lastResponse = getResponse;
        return getResponse.getDocumentMetadata();
    }

    public List<File> getAll(int folderId) throws OXException, JSONException, IOException {
        int[] columns = new int[] { Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL };
        AllInfostoreRequest allRequest = new AllInfostoreRequest(folderId, columns, Metadata.ID, Order.DESCENDING);
        AbstractColumnsResponse response = getClient().execute(allRequest);
        lastResponse = response;

        List<File> found = new ArrayList<>();
        for (Object[] doc : response.getArray()) {
            File file = new DefaultFile();
            int docId = Integer.parseInt((String) doc[response.getColumnPos(Metadata.ID)]);
            file.setId(Integer.toString(docId));
            String title = (String) doc[response.getColumnPos(Metadata.TITLE)];
            file.setTitle(title);
            String description = (String) doc[response.getColumnPos(Metadata.DESCRIPTION)];
            file.setDescription(description);
            String url = (String) doc[response.getColumnPos(Metadata.URL)];
            file.setURL(url);

            file.setTitle(Integer.toString(folderId));
            found.add(file);
        }
        return found;
    }

    public List<File> versions(final String objectId, final int[] cols) throws MalformedURLException, JSONException, IOException, OXException {
        VersionsRequest request = new VersionsRequest(objectId, cols);
        VersionsResponse response = getClient().execute(request);
        lastResponse = response;
        return response.getVersions();
    }

    public List<File> versions(final String objectId, final int[] cols, int sort, Order order) throws MalformedURLException, JSONException, IOException, OXException {
        VersionsRequest request = new VersionsRequest(objectId, cols, sort, order);
        VersionsResponse response = getClient().execute(request);
        lastResponse = response;
        return response.getVersions();
    }

    public String saveAs(final int folderId, final int attached, final int module, final int attachment, final Map<String, String> fields) throws MalformedURLException, IOException, JSONException, OXException {
        SaveAsRequest request = new SaveAsRequest(Integer.toString(folderId), attached, module, attachment, fields);
        SaveAsResponse response = getClient().execute(request);
        lastResponse = response;
        return response.getData().toString();
    }

    public void revert(final String id) throws MalformedURLException, IOException, JSONException, OXException {
        RevertRequest request = new RevertRequest(id);
        RevertResponse response = getClient().execute(request);
        lastResponse = response;
    }

    public InputStream document(final String folderId, String id, String version) throws MalformedURLException, IOException, JSONException, OXException {
        return document(folderId, id, version, null);
    }

    public InputStream document(final String folderId, String id, String version, String mimeType) throws MalformedURLException, IOException, JSONException, OXException {
        GetDocumentRequest request = new GetDocumentRequest(folderId, id, version, mimeType);
        GetDocumentResponse response = getClient().execute(request);
        lastResponse = response;
        return response.getContent();
    }

    public Object[][] list(final List<ListItem> items, final int[] cols) throws MalformedURLException, IOException, JSONException, OXException {
        ListInfostoreRequest request = new ListInfostoreRequest(items, cols, failOnError);
        ListInfostoreResponse response = getClient().execute(request);
        lastResponse = response;
        return response.getArray();
    }

    public Object[][] list(String[][] infostore_ids, int[] cols) throws MalformedURLException, IOException, JSONException, OXException {
        List<ListItem> items = new ArrayList<>();
        for (String[] infostoreId : infostore_ids) {
            for (int i = 0; i < infostoreId.length; i++)
                items.add(new ListItem(infostoreId[i], infostoreId[i++]));
        }
        return list(items, cols);
    }

    public void lock(String id) throws OXException, IOException, JSONException {
        LockRequest request = new LockRequest(id);
        LockResponse response = getClient().execute(request);
        lastResponse = response;
    }

    public void unlock(String id) throws OXException, IOException, JSONException {
        UnlockRequest request = new UnlockRequest(id);
        UnlockResponse response = getClient().execute(request);
        lastResponse = response;
    }

    public File newDocument(int folderId) {
        return this.newDocument(folderId, null);
    }

    public File newDocument(int folderId, List<FileStorageObjectPermission> objectPermissions) {
        File doc = new DefaultFile();
        doc.setTitle(UUIDs.getUnformattedString(UUID.randomUUID()));
        doc.setDescription("Infostore Item Description");
        doc.setFileMIMEType("image/png");
        doc.setFolderId(String.valueOf(folderId));
        if (objectPermissions != null) {
            doc.setObjectPermissions(objectPermissions);
        }
        doc.setFileName("contact_image.png");
        return doc;
    }

    public List<File> search(final String query, final int[] columns, final int folderId, final int sort, final Order order) throws MalformedURLException, JSONException, IOException, SAXException, OXException {
        SearchInfostoreRequest request = new SearchInfostoreRequest(folderId, query, columns, sort, order);
        SearchInfostoreResponse response = getClient().execute(request);
        lastResponse = response;
        JSONArray foundFiles = (JSONArray) response.getData();

        List<File> found = new ArrayList<>();
        for (Object object : foundFiles) {
            File file = new DefaultFile();

            //            TODO
            //            file.

            found.add(file);
        }
        return found;
    }

}
