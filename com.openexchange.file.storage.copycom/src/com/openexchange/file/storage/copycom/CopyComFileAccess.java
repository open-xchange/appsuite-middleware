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

package com.openexchange.file.storage.copycom;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
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
import com.openexchange.file.storage.copycom.access.CopyComAccess;
import com.openexchange.file.storage.copycom.http.client.methods.HttpCopy;
import com.openexchange.file.storage.copycom.http.client.methods.HttpMove;
import com.openexchange.file.storage.copycom.osgi.Services;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.FileKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link CopyComFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CopyComFileAccess extends AbstractCopyComResourceAccess implements ThumbnailAware {

    private final CopyComAccountAccess accountAccess;
    final int userId;

    /**
     * Initializes a new {@link CopyComFileAccess}.
     */
    public CopyComFileAccess(CopyComAccess boxAccess, FileStorageAccount account, Session session, CopyComAccountAccess accountAccess) throws OXException {
        super(boxAccess, account, session);
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
        return perform(new CopyComClosure<Boolean>() {

            @Override
            protected Boolean doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri("meta/" + id + "/@activity", null));
                    request = method;
                    access.sign(request);

                    HttpResponse response = access.getHttpClient().execute(method);
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
        return perform(new CopyComClosure<File>() {

            @Override
            protected File doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri("meta/" + id + "/@activity", null));
                    request = method;
                    access.sign(request);

                    com.copy.api.File restFile = handleHttpResponse(access.getHttpClient().execute(method), com.copy.api.File.class);
                    return new CopyComFile(folderId, id, userId, rootFolderId).parseCopyComFile(restFile);
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
            perform(new CopyComClosure<Void>() {

                @Override
                protected Void doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                    HttpRequestBase request = null;
                    try {
                        List<NameValuePair> qparams = new LinkedList<NameValuePair>();
                        qparams.add(new BasicNameValuePair("name", file.getFileName()));
                        qparams.add(new BasicNameValuePair("overwrite", "true"));

                        HttpPut method = new HttpPut(buildUri("files/" + file.getId(), qparams));
                        request = method;
                        access.sign(request);

                        handleHttpResponse(access.getHttpClient().execute(method), Void.class);
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
            throw CopyComExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
        }

        return perform(new CopyComClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String newFileId;
                    {
                        HttpCopy method = new HttpCopy(buildUri(source.getId(), null));
                        request = method;
                        access.sign(request);
                        method.setHeader("Authorization", "Bearer " + copyComAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("destination", toCopyComFolderId(destFolder))));

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
                        newFileId = jResponse.getString("id");
                        reset(request);
                        request = null;
                    }

                    String fileName = null;
                    if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
                        HttpPost method = new HttpPost(buildUri(newFileId, null));
                        request = method;
                        access.sign(request);
                        method.setHeader("Authorization", "Bearer " + copyComAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        fileName = update.getFileName();
                        method.setEntity(asHttpEntity(new JSONObject(2).put("name", fileName)));

                        handleHttpResponse(access.getHttpClient().execute(method), Void.class);
                        reset(request);
                        request = null;
                    }

                    if (null != newFile) {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("overwrite", "true"));

                        HttpPost method = new HttpPost(buildUri(newFileId, qparams));
                        request = method;
                        access.sign(request);

                        //MimeTypeMap map = Services.getService(MimeTypeMap.class);
                        //String contentType = map.getContentType(fileName);

                        MultipartEntity multipartEntity = new MultipartEntity();
                        multipartEntity.addPart(new FormBodyPart("file", new InputStreamBody(newFile, "application/octet-stream", fileName)));
                        method.setEntity(multipartEntity);

                        handleHttpResponse(access.getHttpClient().execute(method), Void.class);
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
        return perform(new CopyComClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String newFileId;
                    {
                        HttpMove method = new HttpMove(buildUri(source.getId(), null));
                        request = method;
                        access.sign(request);
                        method.setHeader("Authorization", "Bearer " + copyComAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("destination", toCopyComFolderId(destFolder))));

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
                        newFileId = jResponse.getString("id");
                        reset(request);
                        request = null;
                    }

                    String fileName = null;
                    if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
                        HttpPost method = new HttpPost(buildUri(newFileId, initiateQueryString()));
                        request = method;
                        access.sign(request);
                        fileName = update.getFileName();
                        method.setEntity(asHttpEntity(new JSONObject(2).put("name", fileName)));

                        handleHttpResponse(access.getHttpClient().execute(method), Void.class);
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
        return perform(new CopyComClosure<InputStream>() {

            @Override
            protected InputStream doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                boolean error = true;
                try {
                    HttpGet method = new HttpGet(buildUri(id + "/content", initiateQueryString()));
                    request = method;
                    access.sign(request);

                    HttpResponse httpResponse = access.getHttpClient().execute(method);
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
        return perform(new CopyComClosure<InputStream>() {

            @Override
            protected InputStream doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                boolean error = true;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;
                    access.sign(request);

                    RestFileResponse restResponse = handleHttpResponse(access.getHttpClient().execute(method), RestFileResponse.class);
                    RestFile restFile = restResponse.getData().get(0);

                    String thumbnailUrl = (String) restFile.getAdditionalProperties().get("picture");
                    if (null == thumbnailUrl) {
                        return null;
                    }

                    reset(request);
                    request = null;
                    method = new HttpGet(thumbnailUrl);
                    request = method;
                    access.sign(request);

                    HttpResponse httpResponse = access.getHttpClient().execute(method);
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
        final String copyComFolderId = toCopyComFolderId(file.getFolderId());
        perform(new CopyComClosure<Void>() {

            @Override
            protected Void doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    if (isEmpty(id) || !exists(null, id, CURRENT_VERSION)) {
                        HttpPost method = new HttpPost(buildUri("files/" + copyComFolderId, null));
                        request = method;
                        access.sign(request);

                        MimeTypeMap map = Services.getService(MimeTypeMap.class);
                        String contentType = map.getContentType(file.getFileName());

                        MultipartEntity multipartEntity = new MultipartEntity();
                        java.io.File theFile = null;
                        if (data instanceof FileKnowingInputStream) {
                            FileKnowingInputStream stream = (FileKnowingInputStream) data;
                            theFile = stream.getFile();
                            Streams.close(data);
                        }
                        if (null == theFile) {
                            multipartEntity.addPart(new FormBodyPart("file", new InputStreamBody(data, contentType, file.getFileName())));
                        } else {
                            multipartEntity.addPart(new FormBodyPart("file", new FileBody(theFile, contentType)));
                        }
                        method.setEntity(multipartEntity);

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
                        JSONArray jObjects = jResponse.getJSONArray("objects");
                        file.setId(jObjects.getJSONObject(0).getString("id"));

                        reset(request);
                        request = null;
                    } else {
                        HttpPost method = new HttpPost(buildUri("files/" + copyComFolderId, null));
                        request = method;
                        access.sign(request);

                        MimeTypeMap map = Services.getService(MimeTypeMap.class);
                        String contentType = map.getContentType(file.getFileName());

                        MultipartEntity multipartEntity = new MultipartEntity();
                        java.io.File theFile = null;
                        if (data instanceof FileKnowingInputStream) {
                            FileKnowingInputStream stream = (FileKnowingInputStream) data;
                            theFile = stream.getFile();
                            Streams.close(data);
                        }
                        if (null == theFile) {
                            multipartEntity.addPart(new FormBodyPart("file", new InputStreamBody(data, contentType, file.getFileName())));
                        } else {
                            multipartEntity.addPart(new FormBodyPart("file", new FileBody(theFile, contentType)));
                        }
                        method.setEntity(multipartEntity);

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
                        JSONArray jObjects = jResponse.getJSONArray("objects");
                        file.setId(jObjects.getJSONObject(0).getString("id"));

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
    public void removeDocument(final String folderId, long sequenceNumber) throws OXException {
        perform(new CopyComClosure<Void>() {

            @Override
            protected Void doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toCopyComFolderId(folderId);
                    List<String> ids = new LinkedList<String>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("offset", Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair("limit", Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;
                        access.sign(request);

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
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
                        access.sign(request);
                        HttpResponse httpResponse = access.getHttpClient().execute(method);
                        StatusLine statusLine = httpResponse.getStatusLine();
                        int statusCode = statusLine.getStatusCode();
                        if (200 != statusCode && 404 != statusCode) {
                            throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
                        }
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
        return perform(new CopyComClosure<List<IDTuple>>() {

            @Override
            protected List<IDTuple> doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    for (IDTuple idTuple : ids) {
                        HttpDelete method = new HttpDelete(buildUri(idTuple.getId(), initiateQueryString()));
                        request = method;
                        access.sign(request);
                        HttpResponse httpResponse = access.getHttpClient().execute(method);
                        StatusLine statusLine = httpResponse.getStatusLine();
                        int statusCode = statusLine.getStatusCode();
                        if (200 != statusCode && 404 != statusCode) {
                            throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
                        }
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
                throw CopyComExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        return perform(new CopyComClosure<String[]>() {

            @Override
            protected String[] doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpDelete method = new HttpDelete(buildUri(id, initiateQueryString()));
                    request = method;
                    access.sign(request);
                    HttpResponse httpResponse = access.getHttpClient().execute(method);
                    StatusLine statusLine = httpResponse.getStatusLine();
                    int statusCode = statusLine.getStatusCode();
                    if (200 != statusCode && 404 != statusCode) {
                        throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
                    }

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
        return perform(new CopyComClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toCopyComFolderId(folderId);
                    List<File> files = new LinkedList<File>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("offset", Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair("limit", Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;
                        access.sign(request);

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFile(jItem)) {
                                files.add(new CopyComFile(folderId, jItem.getString("id"), userId, rootFolderId).parseCopyComFile(jItem));
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
        return perform(new CopyComClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toCopyComFolderId(folderId);
                    List<File> files = new LinkedList<File>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("offset", Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair("limit", Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;
                        access.sign(request);

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFile(jItem)) {
                                files.add(new CopyComFile(folderId, jItem.getString("id"), userId, rootFolderId).parseCopyComFile(jItem));
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
        return perform(new CopyComClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;
                    access.sign(request);

                    RestFileResponse restResponse = handleHttpResponse(access.getHttpClient().execute(method), RestFileResponse.class);
                    RestFile restFile = restResponse.getData().get(0);
                    return new FileTimedResult(Collections.<File> singletonList(new CopyComFile(folderId, id, userId, rootFolderId).parseCopyComFile(restFile)));
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
        return perform(new CopyComClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));
                    request = method;
                    access.sign(request);

                    RestFileResponse restResponse = handleHttpResponse(access.getHttpClient().execute(method), RestFileResponse.class);
                    RestFile restFile = restResponse.getData().get(0);
                    return new FileTimedResult(Collections.<File> singletonList(new CopyComFile(folderId, id, userId, rootFolderId).parseCopyComFile(restFile)));
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, List<Field> fields) throws OXException {
        return perform(new CopyComClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    List<File> files = new LinkedList<File>();

                    for (IDTuple id : ids) {
                        HttpGet method = new HttpGet(buildUri(id.getId(), initiateQueryString()));
                        request = method;
                        access.sign(request);

                        RestFileResponse restResponse = handleHttpResponse(access.getHttpClient().execute(method), RestFileResponse.class);
                        RestFile restFile = restResponse.getData().get(0);
                        files.add(new CopyComFile(id.getFolder(), id.getId(), userId, rootFolderId).parseCopyComFile(restFile));
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
        return perform(new CopyComClosure<SearchIterator<File>>() {

            @Override
            protected SearchIterator<File> doPerform(CopyComAccess access) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    List<File> files = new LinkedList<File>();
                    String fid = null == folderId ? null : toCopyComFolderId(folderId);
                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        if (null != pattern) {
                            qparams.add(new BasicNameValuePair("q", pattern));
                        }
                        qparams.add(new BasicNameValuePair("offset", Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair("limit", Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri("me/skydrive/search", qparams));
                        request = method;
                        access.sign(request);

                        JSONObject jResponse = handleHttpResponse(access.getHttpClient().execute(method), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFile(jItem)) {
                                if (null != fid) {
                                    if (fid.equals(jItem.optString("parent_id", null))) {
                                        files.add(new CopyComFile(folderId, jItem.getString("id"), userId, rootFolderId).parseCopyComFile(jItem));
                                    }
                                } else {
                                    files.add(new CopyComFile(folderId, jItem.getString("id"), userId, rootFolderId).parseCopyComFile(jItem));
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
