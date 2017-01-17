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
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.folder.actions.VersionsRequest;
import com.openexchange.ajax.folder.actions.VersionsResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest.ListItem;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.json.FileMetadataFieldParser;
import com.openexchange.file.storage.meta.FileFieldSet;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.TestInit;
import com.openexchange.test.TestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class InfostoreTestManager implements TestManager {

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

    public Set<String> getCreatedEntitiesIds() {
        Set<String> createdIds = new HashSet<>(createdEntities.size());
        for (File file : createdEntities) {
            createdIds.add(file.getId());
        }
        return createdIds;
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    @Override
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }

    @Override
    public void cleanUp() {
        List<String> objectIDs = new ArrayList<String>(createdEntities.size());
        List<String> folderIDs = new ArrayList<String>(createdEntities.size());
        for (File metadata : createdEntities) {
            objectIDs.add(metadata.getId());
            folderIDs.add(metadata.getFolderId());
        }
        try {
            deleteAction(objectIDs, folderIDs, new Date(Long.MAX_VALUE), Boolean.TRUE);
        } catch (Exception e) {
        }
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

    public void newAction(File data) throws OXException, IOException, JSONException {
        NewInfostoreRequest newRequest = new NewInfostoreRequest(data);
        newRequest.setFailOnError(getFailOnError());
        NewInfostoreResponse newResponse = getClient().execute(newRequest);
        lastResponse = newResponse;
        if (!lastResponse.hasError()) {
            data.setId(newResponse.getID());
            createdEntities.add(data);
        }
    }

    /*
     * The following is not beautiful, but the request/response framework
     * doesn't seem to offer a solution to do POST requests containing files.
     */
    public void newAction(File data, java.io.File upload) throws OXException, IOException, JSONException {
        NewInfostoreRequest newRequest = new NewInfostoreRequest(data, upload);
        newRequest.setFailOnError(false);
        NewInfostoreResponse newResponse = getClient().execute(newRequest);
        lastResponse = newResponse;
        if (!lastResponse.hasError()) {
            data.setId(newResponse.getID());
            createdEntities.add(data);
        }
    }

    public void copyAction(String id, String folderId, File data) throws OXException, IOException, JSONException {
        CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(id, folderId, data);
        copyRequest.setFailOnError(getFailOnError());
        CopyInfostoreResponse copyResponse = getClient().execute(copyRequest);
        lastResponse = copyResponse;
        if (!lastResponse.hasError()) {
            data.setId(copyResponse.getID());
            createdEntities.add(data);
        }
    }

    public void copyAction(String id, String folderId, File data, java.io.File file) throws OXException, IOException, JSONException {
        CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(id, folderId, data);
        copyRequest.setFailOnError(getFailOnError());
        CopyInfostoreResponse copyResponse = getClient().execute(copyRequest);
        lastResponse = copyResponse;
        if (!lastResponse.hasError()) {
            data.setId(copyResponse.getID());
            createdEntities.add(data);
        }
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

    public void deleteAction(String id, String folder, Date timestamp) throws OXException, IOException, JSONException {
        deleteAction(Arrays.asList(id), Arrays.asList(folder), timestamp);
    }

    public void deleteAction(File data) throws OXException, IOException, JSONException {
        deleteAction(data.getId(), data.getFolderId(), data.getLastModified());
    }

    public File getAction(String id) throws OXException, JSONException, IOException {
        return getAction(id, -1);
    }

    public File getAction(String id, int version) throws OXException, JSONException, IOException {
        int[] columns = new int[] { Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL, Metadata.VERSION, Metadata.COLOR_LABEL };
        GetInfostoreRequest getRequest = new GetInfostoreRequest(id, version, columns);
        getRequest.setFailOnError(getFailOnError());
        GetInfostoreResponse getResponse = getClient().execute(getRequest);
        lastResponse = getResponse;
        return getResponse.getDocumentMetadata();
    }

    public List<File> getAll(int folderId) throws OXException, JSONException, IOException {
        int[] columns = new int[] { Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL };
        return getAll(folderId, columns);
    }

    public List<File> getAll(int folderId, int[] columns) throws OXException, JSONException, IOException {
        return getAll(folderId, columns, Metadata.ID, Order.DESCENDING);
    }

    public List<File> getAll(int folderId, int[] columns, int sort, Order order) throws OXException, JSONException, IOException {
        AllInfostoreRequest allRequest = new AllInfostoreRequest(folderId, columns, sort, order);
        AbstractColumnsResponse response = getClient().execute(allRequest);
        lastResponse = response;

        return createResponse(response.getResponse(), columns);
    }

    protected List<File> createResponse(Response response, int[] columns) throws JSONException {
        List<File> files = new ArrayList<>();

        FileFieldSet fileFieldSet = new FileFieldSet();

        JSONObject json = ResponseWriter.getJSON(response);
        JSONArray filesJSON = (JSONArray) json.get("data");
        for (int i = 0; i < filesJSON.length(); i++) {
            DefaultFile metadata = new DefaultFile();

            int columncount = 0;
            for (int column : columns) {
                Field field = Field.get(column);
                if (null != field) {
                    Object orig = ((JSONArray) filesJSON.get(i)).get(columncount);
                    Object converted;
                    try {
                        converted = FileMetadataFieldParser.convert(field, orig);
                        field.doSwitch(fileFieldSet, metadata, converted);
                    } catch (OXException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    columncount++;
                }
            }

            files.add(metadata);
        }
        return files;
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
        if (!lastResponse.hasError()) {
            return response.getData().toString();
        }
        return "";
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

    public List<File> list(final List<ListItem> items, final int[] cols) throws MalformedURLException, IOException, JSONException, OXException {
        ListInfostoreRequest request = new ListInfostoreRequest(items, cols, failOnError);
        ListInfostoreResponse response = getClient().execute(request);
        lastResponse = response;
        return createResponse(response.getResponse(), cols);
    }

    public List<File> list(String[][] infostore_ids, int[] cols) throws MalformedURLException, IOException, JSONException, OXException {
        List<ListItem> items = new ArrayList<>();
        for (String[] infostoreId : infostore_ids) {
            for (int i = 0; i < infostoreId.length; i++) {
                String folderId = infostoreId[i];
                String id = infostoreId[++i];
                items.add(new ListItem(folderId, id));
            }
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

    public List<File> search(final String query, final int folderId) throws MalformedURLException, JSONException, IOException, OXException {
        return search(query, folderId, -1, null, -1);
    }

    public List<File> search(final String query, final int folderId, final int sort, final Order order, final int limit) throws MalformedURLException, JSONException, IOException, OXException {
        return search(query, folderId, sort, order, limit, -1, -1);
    }

    public List<File> search(final String query, final int folderId, final int sort, final Order order, final int limit, final int start, final int end) throws MalformedURLException, JSONException, IOException, OXException {
        int[] columns = new int[] { Metadata.TITLE, Metadata.ID, Metadata.DESCRIPTION, Metadata.LAST_MODIFIED_UTC, Metadata.CATEGORIES };

        SearchInfostoreRequest request = new SearchInfostoreRequest(folderId, query, columns, sort, order, limit, start, end, false);
        SearchInfostoreResponse response = getClient().execute(request);
        lastResponse = response;

        List<File> found = new ArrayList<>();
        JSONArray foundFiles = (JSONArray) response.getData();
        if (foundFiles == null) {
            return found;
        }
        //FIXME MS more intelligent parsing based on columns
        for (int i = 0; i < foundFiles.length(); i++) {
            JSONArray jsonFile = foundFiles.getJSONArray(i);
            DefaultFile file = new DefaultFile();
            file.setTitle(jsonFile.get(0).toString());
            file.setId(jsonFile.get(1).toString());
            file.setDescription(jsonFile.get(2).toString());
            file.setLastModified(new Date((Long) jsonFile.get(3)));

            found.add(file);
        }
        return found;
    }

    @Override
    public boolean doesFailOnError() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasLastException() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Throwable getLastException() {
        // TODO Auto-generated method stub
        return null;
    }

    public File createFileOnServer(int folderId, String fileName, String mimeType) throws Exception {
        File createFile = InfostoreTestManager.createFile(folderId, fileName, mimeType);
        newAction(createFile, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
        return createFile;
    }

    public static File createFile(int folderId, String fileName, String mimeType) throws Exception {
        //        long now = System.currentTimeMillis();
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(folderId));
        file.setTitle(fileName);
        file.setFileName(file.getTitle());
        file.setDescription(fileName + " description");
        file.setFileMIMEType(mimeType);
        return file;
    }
}
