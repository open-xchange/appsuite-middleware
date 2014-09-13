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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.file.storage.onedrive;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.onedrive.access.OneDriveAccess;
import com.openexchange.file.storage.onedrive.http.client.methods.HttpCopy;
import com.openexchange.file.storage.onedrive.http.client.methods.HttpMove;
import com.openexchange.file.storage.onedrive.rest.file.RestFile;
import com.openexchange.file.storage.onedrive.rest.file.RestFileResponse;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link OneDriveFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OneDriveFileAccess extends AbstractOneDriveResourceAccess implements ThumbnailAware {

    private static final String QUERY_PARAM_LIMIT = OneDriveConstants.QUERY_PARAM_LIMIT;
    private static final String QUERY_PARAM_OFFSET = OneDriveConstants.QUERY_PARAM_OFFSET;

    private final OneDriveAccountAccess accountAccess;
    final int userId;

    /**
     * Initializes a new {@link OneDriveFileAccess}.
     */
    public OneDriveFileAccess(OneDriveAccess oneDriveAccess, FileStorageAccount account, Session session, OneDriveAccountAccess accountAccess) {
        super(oneDriveAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
    }

    @Override
    public void startTransaction() throws OXException {
        // Nope
    }

    @Override
    public void commit() throws OXException {
        // Nope
    }

    @Override
    public void rollback() throws OXException {
        // Nope
    }

    @Override
    public void finish() throws OXException {
        // Nope
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // Nope
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nope
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nope
    }

    @Override
    public boolean exists(final String folderId, final String id, final String version) throws OXException {
        return perform(new OneDriveClosure<Boolean>() {

            @Override
            protected Boolean doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;

                    HttpResponse response = execute(method, httpClient);
                    return Boolean.valueOf(200 == response.getStatusLine().getStatusCode());
                } catch (HttpResponseException e) {
                    if (404 == e.getStatusCode()) {
                        return Boolean.FALSE;
                    }
                    throw e;
                } finally {
                    reset(request);
                }

            }

        }).booleanValue();
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        return perform(new OneDriveClosure<File>() {

            @Override
            protected File doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;

                    RestFile restFile = handleHttpResponse(execute(method, httpClient), RestFile.class);
                    return new OneDriveFile(folderId, id, userId, getRootFolderId()).parseOneDriveFile(restFile);
                } finally {
                    if (null != request) {
                        request.releaseConnection();
                    }
                }
            }
        });
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        saveFileMetadata(file, sequenceNumber, null);
    }

    @Override
    public void saveFileMetadata(final File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
            perform(new OneDriveClosure<Void>() {

                @Override
                protected Void doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                    HttpRequestBase request = null;
                    try {
                        HttpPost method = new HttpPost(buildUri(file.getId(), null));
                        request = method;
                        method.setHeader("Authorization", "Bearer " + oneDriveAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("name", file.getFileName())));

                        handleHttpResponse(execute(method, httpClient), Void.class);
                        return null;
                    } catch (HttpResponseException e) {
                        throw handleHttpResponseError(file.getId(), e);
                    } finally {
                        reset(request);
                    }
                }
            });
        }
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, final File update, final InputStream newFile, final List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw OneDriveExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
        }

        return perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String newFileId;
                    {
                        HttpCopy method = new HttpCopy(buildUri(source.getId(), null));
                        request = method;
                        method.setHeader("Authorization", "Bearer " + oneDriveAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("destination", toOneDriveFolderId(destFolder))));

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        newFileId = jResponse.getString("id");
                        reset(request);
                        request = null;
                    }

                    String fileName = null;
                    if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
                        HttpPost method = new HttpPost(buildUri(newFileId, null));
                        request = method;
                        method.setHeader("Authorization", "Bearer " + oneDriveAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        fileName = update.getFileName();
                        method.setEntity(asHttpEntity(new JSONObject(2).put("name", fileName)));

                        handleHttpResponse(execute(method, httpClient), Void.class);
                        reset(request);
                        request = null;
                    }

                    if (null != newFile) {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("overwrite", "true"));

                        HttpPost method = new HttpPost(buildUri(newFileId, qparams));
                        request = method;

                        //MimeTypeMap map = Services.getService(MimeTypeMap.class);
                        //String contentType = map.getContentType(fileName);

                        MultipartEntity multipartEntity = new MultipartEntity();
                        multipartEntity.addPart(new FormBodyPart("file", new InputStreamBody(newFile, "application/octet-stream", fileName)));
                        method.setEntity(multipartEntity);

                        handleHttpResponse(execute(method, httpClient), Void.class);
                        reset(request);
                        request = null;
                    }

                    return new IDTuple(destFolder, newFileId);
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(source.getId(), e);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public IDTuple move(final IDTuple source, final String destFolder, long sequenceNumber, final File update, final List<File.Field> modifiedFields) throws OXException {
        return perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String newFileId;
                    {
                        HttpMove method = new HttpMove(buildUri(source.getId(), null));
                        request = method;
                        method.setHeader("Authorization", "Bearer " + oneDriveAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("destination", toOneDriveFolderId(destFolder))));

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        newFileId = jResponse.getString("id");
                        reset(request);
                        request = null;
                    }

                    String fileName = null;
                    if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
                        HttpPost method = new HttpPost(buildUri(newFileId, initiateQueryString()));
                        request = method;
                        fileName = update.getFileName();
                        method.setEntity(asHttpEntity(new JSONObject(2).put("name", fileName)));

                        handleHttpResponse(execute(method, httpClient), Void.class);
                        reset(request);
                        request = null;
                    }

                    return new IDTuple(destFolder, newFileId);
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(source.getId(), e);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        return perform(new OneDriveClosure<InputStream>() {

            @Override
            protected InputStream doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                boolean error = true;
                try {
                    HttpGet method = new HttpGet(buildUri(id + "/content", initiateQueryString()));
                    request = method;

                    HttpResponse httpResponse = execute(method, httpClient);
                    InputStream content = httpResponse.getEntity().getContent();
                    error = false;
                    return content;
                } finally {
                    if (error) {
                        reset(request);
                    }
                }
            }

        });
    }

    @Override
    public InputStream getThumbnailStream(String folderId, final String id, String version) throws OXException {
        return perform(new OneDriveClosure<InputStream>() {

            @Override
            protected InputStream doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                boolean error = true;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;

                    RestFileResponse restResponse = handleHttpResponse(execute(method, httpClient), RestFileResponse.class);
                    RestFile restFile = restResponse.getData().get(0);

                    String thumbnailUrl = (String) restFile.getAdditionalProperties().get("picture");
                    if (null == thumbnailUrl) {
                        return null;
                    }

                    reset(request);
                    request = null;
                    method = new HttpGet(thumbnailUrl);
                    request = method;

                    HttpResponse httpResponse = execute(method, httpClient);
                    InputStream content = httpResponse.getEntity().getContent();
                    error = false;
                    return content;
                } finally {
                    if (error) {
                        reset(request);
                    }
                }
            }

        });
    }

    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        final String id = file.getId();
        final String oneDriveFolderId = toOneDriveFolderId(file.getFolderId());
        perform(new OneDriveClosure<Void>() {

            @Override
            protected Void doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    if (isEmpty(id) || !exists(null, id, CURRENT_VERSION)) {
                        HttpPut method = new HttpPut(buildUri(oneDriveFolderId + "/files/" + file.getFileName(), initiateQueryString()));
                        request = method;

                        HttpEntity entity = new InputStreamEntity(data, -1);
                        method.setEntity(entity);

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        file.setId(jResponse.getString("id"));

                        reset(request);
                        request = null;
                    } else {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("overwrite", "true"));
                        HttpPut method = new HttpPut(buildUri(oneDriveFolderId + "/files/" + file.getFileName(), qparams));
                        request = method;

                        HttpEntity entity = new InputStreamEntity(data, -1);
                        method.setEntity(entity);

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        file.setId(jResponse.getString("id"));

                        reset(request);
                        request = null;
                    }

                    return null;
                } finally {
                    reset(request);
                    Streams.close(data);
                }
            }
        });
    }

    @Override
    public void removeDocument(final String folderId, long sequenceNumber) throws OXException {
        perform(new OneDriveClosure<Void>() {

            @Override
            protected Void doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toOneDriveFolderId(folderId);
                    List<String> ids = new LinkedList<String>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_OFFSET, Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_LIMIT, Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFile(jItem)) {
                                ids.add(jItem.getString("id"));
                            }
                        }
                        reset(request);
                        request = null;

                        offset += limit;
                    } while (resultsFound == limit);

                    for (String id : ids) {
                        HttpDelete method = new HttpDelete(buildUri(id, initiateQueryString()));
                        request = method;

                        handleHttpResponse(execute(method, httpClient), STATUS_CODE_POLICY_IGNORE_NOT_FOUND, Void.class);

                        reset(request);
                        request = null;
                    }

                    return null;
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, long sequenceNumber, final boolean hardDelete) throws OXException {
        return perform(new OneDriveClosure<List<IDTuple>>() {

            @Override
            protected List<IDTuple> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    for (IDTuple idTuple : ids) {
                        HttpDelete method = new HttpDelete(buildUri(idTuple.getId(), initiateQueryString()));
                        request = method;

                        handleHttpResponse(execute(method, httpClient), STATUS_CODE_POLICY_IGNORE_NOT_FOUND, Void.class);

                        reset(request);
                        request = null;
                    }

                    return Collections.emptyList();
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public String[] removeVersion(String folderId, final String id, String[] versions) throws OXException {
        /*
         * No versioning support
         */
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw OneDriveExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        return perform(new OneDriveClosure<String[]>() {

            @Override
            protected String[] doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpDelete method = new HttpDelete(buildUri(id, initiateQueryString()));
                    request = method;

                    handleHttpResponse(execute(method, httpClient), STATUS_CODE_POLICY_IGNORE_NOT_FOUND, Void.class);

                    return new String[0];
                } finally {
                    reset(request);
                }
            }

        });
    }

    @Override
    public void unlock(String folderId, String id) throws OXException {
        // Nope
    }

    @Override
    public void lock(String folderId, String id, long diff) throws OXException {
        // Nope
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toOneDriveFolderId(folderId);
                    List<File> files = new LinkedList<File>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_OFFSET, Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_LIMIT, Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFile(jItem)) {
                                files.add(new OneDriveFile(folderId, jItem.getString("id"), userId, getRootFolderId()).parseOneDriveFile(jItem));
                            }
                        }
                        reset(request);
                        request = null;

                        offset += limit;
                    } while (resultsFound == limit);

                    return new FileTimedResult(files);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toOneDriveFolderId(folderId);
                    List<File> files = new LinkedList<File>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_OFFSET, Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_LIMIT, Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFile(jItem)) {
                                files.add(new OneDriveFile(folderId, jItem.getString("id"), userId, getRootFolderId()).parseOneDriveFile(jItem));
                            }
                        }

                        reset(request);
                        request = null;

                        offset += limit;
                    } while (resultsFound == limit);

                    // Sort collection if needed
                    sort(files, sort, order);

                    return new FileTimedResult(files);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;

                    RestFileResponse restResponse = handleHttpResponse(execute(method, httpClient), RestFileResponse.class);
                    RestFile restFile = restResponse.getData().get(0);
                    return new FileTimedResult(Collections.<File> singletonList(new OneDriveFile(folderId, id, userId, getRootFolderId()).parseOneDriveFile(restFile)));
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        return getVersions(folderId, id);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;

                    RestFileResponse restResponse = handleHttpResponse(execute(method, httpClient), RestFileResponse.class);
                    RestFile restFile = restResponse.getData().get(0);
                    return new FileTimedResult(Collections.<File> singletonList(new OneDriveFile(folderId, id, userId, getRootFolderId()).parseOneDriveFile(restFile)));
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, List<Field> fields) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    List<File> files = new LinkedList<File>();

                    for (IDTuple id : ids) {
                        HttpGet method = new HttpGet(buildUri(id.getId(), initiateQueryString()));
                        request = method;

                        RestFileResponse restResponse = handleHttpResponse(execute(method, httpClient), RestFileResponse.class);
                        List<RestFile> data = restResponse.getData();
                        if (!data.isEmpty()) {
                            RestFile restFile = data.get(0);
                            files.add(new OneDriveFile(id.getFolder(), id.getId(), userId, getRootFolderId()).parseOneDriveFile(restFile));
                        }
                        reset(request);
                        request = null;
                    }

                    return new FileTimedResult(files);
                } finally {
                    reset(request);
                }
            }
        });
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public SearchIterator<File> search(List<String> folderIds, SearchTerm<?> searchTerm, List<Field> fields, Field sort, final SortDirection order, int start, int end) throws OXException {
        if (FileNameTerm.class.isInstance(searchTerm) && (null == folderIds || 1 == folderIds.size())) {
            String pattern = ((FileNameTerm) searchTerm).getPattern();
            return search(pattern, fields, null != folderIds && 1 == folderIds.size() ? folderIds.get(0) : null, sort, order, start, end);
        }
        throw FileStorageExceptionCodes.SEARCH_TERM_NOT_SUPPORTED.create(searchTerm.getClass().getSimpleName());
    }

    @Override
    public SearchIterator<File> search(final String pattern, List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return perform(new OneDriveClosure<SearchIterator<File>>() {

            @Override
            protected SearchIterator<File> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    List<File> files = new LinkedList<File>();
                    String fid = null == folderId ? null : toOneDriveFolderId(folderId);
                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        if (null != pattern) {
                            qparams.add(new BasicNameValuePair("q", pattern));
                        }
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_OFFSET, Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair(QUERY_PARAM_LIMIT, Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri("me/skydrive/search", qparams));
                        request = method;

                        JSONObject jResponse = handleHttpResponse(execute(method, httpClient), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFile(jItem)) {
                                if (null != fid) {
                                    if (fid.equals(jItem.optString("parent_id", null))) {
                                        files.add(new OneDriveFile(folderId, jItem.getString("id"), userId, getRootFolderId()).parseOneDriveFile(jItem));
                                    }
                                } else {
                                    files.add(new OneDriveFile(folderId, jItem.getString("id"), userId, getRootFolderId()).parseOneDriveFile(jItem));
                                }
                            }
                        }

                        reset(request);
                        request = null;

                        offset += limit;
                    } while (resultsFound == limit);

                    // Sort collection
                    sort(files, sort, order);
                    if ((start != NOT_SET) && (end != NOT_SET)) {
                        final int size = files.size();
                        if ((start) > size) {
                            /*
                             * Return empty iterator if start is out of range
                             */
                            return SearchIteratorAdapter.emptyIterator();
                        }
                        /*
                         * Reset end index if out of range
                         */
                        int toIndex = end;
                        if (toIndex >= size) {
                            toIndex = size;
                        }
                        files = files.subList(start, toIndex);
                    }

                    return new SearchIteratorAdapter<File>(files.iterator(), files.size());
                } finally {
                    reset(request);
                }
            }

        });
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    /**
     * Sorts the supplied list of files if needed.
     *
     * @param files The files to sort
     * @param sort The sort order, or <code>null</code> if not specified
     * @param order The sort direction
     */
    protected static void sort(List<File> files, Field sort, SortDirection order) {
        if (null != sort && 1 < files.size()) {
            Collections.sort(files, order.comparatorBy(sort));
        }
    }

}
