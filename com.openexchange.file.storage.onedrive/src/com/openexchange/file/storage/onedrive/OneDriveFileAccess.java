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

package com.openexchange.file.storage.onedrive;

import static com.openexchange.file.storage.onedrive.OneDriveConstants.QUERY_PARAM_LIMIT;
import static com.openexchange.file.storage.onedrive.OneDriveConstants.QUERY_PARAM_OFFSET;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.onedrive.access.OneDriveOAuthAccess;
import com.openexchange.file.storage.onedrive.http.client.methods.HttpCopy;
import com.openexchange.file.storage.onedrive.http.client.methods.HttpMove;
import com.openexchange.file.storage.onedrive.rest.Image;
import com.openexchange.file.storage.onedrive.rest.file.RestFile;
import com.openexchange.file.storage.onedrive.rest.file.RestFileResponse;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Charsets;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link OneDriveFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OneDriveFileAccess extends AbstractOneDriveResourceAccess implements ThumbnailAware, FileStorageSequenceNumberProvider {

    private final OneDriveAccountAccess accountAccess;
    final int userId;

    /**
     * Initializes a new {@link OneDriveFileAccess}.
     *
     * @param oneDriveAccess The underlying One Drive access
     * @param account The underlying account
     * @param session The session The account access
     * @param accountAccess The account access
     */
    public OneDriveFileAccess(OneDriveOAuthAccess oneDriveAccess, FileStorageAccount account, Session session, OneDriveAccountAccess accountAccess) {
        super(oneDriveAccess, account, session);
        this.accountAccess = accountAccess;
        this.userId = session.getUserId();
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
        try {
            return null != getFileMetadata(folderId, id, version);
        } catch (OXException e) {
            if (FileStorageExceptionCodes.FILE_NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        if (CURRENT_VERSION != version) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(OneDriveConstants.ID);
        }
        return perform(new OneDriveClosure<File>() {

            @Override
            protected File doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpGet request = null;
                try {
                    request = new HttpGet(buildUri(id, initiateQueryString()));
                    RestFile restFile = handleHttpResponse(execute(request, httpClient), RestFile.class);
                    OneDriveFile file = new OneDriveFile(folderId, id, userId, getRootFolderId()).parseOneDriveFile(restFile);
                    if (false == file.getFolderId().equals(folderId)) {
                        throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
                    }
                    return file;
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, null);
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        return perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpPut request = null;
                try {
                    if (FileStorageFileAccess.NEW == file.getId()) {
                        /*
                         * create new, empty file ("touch")
                         */
                        return saveDocument(file, Streams.EMPTY_INPUT_STREAM, sequenceNumber, modifiedFields);
                    } else {
                        /*
                         * rename / description change
                         */
                        if (null != modifiedFields && false == modifiedFields.contains(Field.FILENAME) && false == modifiedFields.contains(Field.DESCRIPTION)) {
                            // no change
                            return new IDTuple(file.getFolderId(), file.getId());
                        }
                        request = new HttpPut(buildUri(file.getId(), initiateQueryString()));
                        JSONObject json = new JSONObject(2);
                        if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
                            json.put("name", file.getFileName());
                        }
                        if (null == modifiedFields || modifiedFields.contains(Field.DESCRIPTION)) {
                            json.put("description", file.getDescription());
                        }
                        request.setEntity(asHttpEntity(json));
                        JSONObject jResponse = handleHttpResponse(execute(request, httpClient), JSONObject.class);
                        file.setId(jResponse.getString("id"));
                        return new IDTuple(file.getFolderId(), file.getId());
                    }
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(file.getId(), account.getId(), e);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, final File update, final InputStream newFile, final List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(OneDriveConstants.ID);
        }
        if (destFolder.equals(source.getFolder()) && null != update && null != update.getFileName()) {
            /*
             * special handling to copy within the same folder
             */
            InputStream data = null;
            try {
                data = getDocument(source.getFolder(), source.getId(), CURRENT_VERSION);
                DefaultFile toCreate = new DefaultFile(update);
                toCreate.setId(NEW);
                toCreate.setFolderId(destFolder);
                return saveDocument(toCreate, data, UNDEFINED_SEQUENCE_NUMBER);
            } finally {
                Streams.close(data);
            }
        }
        /*
         * perform copy operation
         */
        IDTuple result = perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpCopy request = null;
                try {
                    request = new HttpCopy(buildUri(source.getId(), initiateQueryString()));
                    request.setEntity(asHttpEntity(new JSONObject(2).put("destination", toOneDriveFolderId(destFolder))));
                    JSONObject jResponse = handleHttpResponse(execute(request, httpClient), JSONObject.class);
                    return new IDTuple(destFolder, jResponse.getString("id"));
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(source.getId(), account.getId(), e);
                } finally {
                    reset(request);
                }
            }
        });
        /*
         * update additional metadata as needed
         */
        if (null != update) {
            DefaultFile toUpdate = new DefaultFile(update);
            toUpdate.setId(result.getId());
            toUpdate.setFolderId(result.getFolder());
            result = saveFileMetadata(update, UNDEFINED_SEQUENCE_NUMBER, modifiedFields);
        }
        return result;
    }

    @Override
    public IDTuple move(final IDTuple source, final String destFolder, long sequenceNumber, final File update, final List<File.Field> modifiedFields) throws OXException {
        /*
         * perform move operation
         */
        IDTuple result = perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpMove request = null;
                try {
                    request = new HttpMove(buildUri(source.getId(), initiateQueryString()));
                    request.setEntity(asHttpEntity(new JSONObject(1).put("destination", toOneDriveFolderId(destFolder))));
                    JSONObject jResponse = handleHttpResponse(execute(request, httpClient), JSONObject.class);
                    return new IDTuple(destFolder, jResponse.getString("id"));
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(source.getId(), account.getId(), e);
                } finally {
                    reset(request);
                }
            }
        });
        /*
         * update additional metadata as needed
         */
        if (null != update) {
            DefaultFile toUpdate = new DefaultFile(update);
            toUpdate.setId(result.getId());
            toUpdate.setFolderId(result.getFolder());
            result = saveFileMetadata(update, UNDEFINED_SEQUENCE_NUMBER, modifiedFields);
        }
        return result;
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        return perform(new OneDriveClosure<InputStream>() {

            @Override
            protected InputStream doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpGet request = null;
                boolean error = true;
                try {
                    request = new HttpGet(buildUri(id, initiateQueryString()));
                    RestFile restFile = handleHttpResponse(execute(request, httpClient), RestFile.class);
                    reset(request);

                    request = new HttpGet(buildUri(id + "/content", initiateQueryString()));
                    HttpResponse httpResponse = execute(request, httpClient);
                    InputStream content = httpResponse.getEntity().getContent();
                    error = false;
                    return new SizeKnowingInputStream(content, restFile.getSize());
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
                HttpGet request = null;
                boolean error = true;
                try {
                    /*
                     * discover thumbnail image url
                     */
                    request = new HttpGet(buildUri(id, initiateQueryString()));
                    RestFile restFile = handleHttpResponse(execute(request, httpClient), RestFile.class);
                    reset(request);
                    Image[] images = restFile.getImages();
                    if (null != images && 0 < images.length) {
                        for (String imageType : new String[] { "album", "thumbnail", "normal" }) {
                            for (Image image : images) {
                                if (imageType.equals(image.getType()) && null != image.getSource()) {
                                    /*
                                     * get & return this thumbnail stream
                                     */
                                    request = new HttpGet(image.getSource());
                                    HttpResponse httpResponse = execute(request, httpClient);
                                    InputStream content = httpResponse.getEntity().getContent();
                                    error = false;
                                    return content;
                                }
                            }
                        }
                    }
                    /*
                     * no thumbnail available
                     */
                    return null;
                } finally {
                    if (error) {
                        reset(request);
                    }
                }
            }
        });
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        return perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    List<NameValuePair> queryParameters = initiateQueryString();
                    queryParameters.add(new BasicNameValuePair("downsize_photo_uploads", "false"));
                    if (FileStorageFileAccess.NEW == file.getId()) {
                        /*
                         * upload new files via custom multipart/form-data to workaround potential filename encoding problems
                         * https://social.msdn.microsoft.com/Forums/onedrive/en-US/4e886074-f5fb-4848-b1ba-11bac7922b0a/
                         */
                        queryParameters.add(new BasicNameValuePair("overwrite", "ChooseNewName"));
                        HttpPost method = new HttpPost(buildUri(toOneDriveFolderId(file.getFolderId()) + "/files/", queryParameters));
                        request = method;
                        String mimeType = null != file.getFileMIMEType() ? file.getFileMIMEType() : MimeType2ExtMap.getContentType(file.getFileName());
                        final String boundary = UUIDs.getUnformattedStringFromRandom();
                        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, boundary, Charsets.UTF_8) {

                            @Override
                            public Header getContentType() {
                                return new BasicHeader(HTTP.CONTENT_TYPE, generateContentType(boundary, null));
                            }
                        };
                        multipartEntity.addPart("file", new InputStreamBody(data, mimeType, file.getFileName()));
                        method.setEntity(multipartEntity);
                    } else {
                        /*
                         * update existing files via PUT to reference the overwritten version by id
                         */
                        queryParameters.add(new BasicNameValuePair("overwrite", "true"));
                        HttpPut method = new HttpPut(buildUri(file.getId() + "/content/", queryParameters));
                        request = method;
                        method.setEntity(new InputStreamEntity(data, -1));
                    }
                    JSONObject uploadResponse = handleHttpResponse(execute(request, httpClient), JSONObject.class);
                    file.setId(uploadResponse.getString("id"));
                    reset(request);
                    /*
                     * update additionally changed metadata as needed
                     */
                    JSONObject updatedMetadata = new JSONObject(2);
                    if ((null == modifiedFields || modifiedFields.contains(Field.FILENAME)) && null != file.getFileName() && false == file.getFileName().equals(uploadResponse.getString("name"))) {
                        updatedMetadata.put("name", file.getFileName());
                    }
                    if ((null == modifiedFields || modifiedFields.contains(Field.DESCRIPTION)) && (null != file.getDescription() || null == file.getDescription() && uploadResponse.hasAndNotNull("description"))) {
                        updatedMetadata.put("description", file.getDescription());
                    }
                    if (0 < updatedMetadata.length()) {
                        HttpPut method = new HttpPut(buildUri(file.getId(), initiateQueryString()));
                        request = method;
                        method.setEntity(asHttpEntity(updatedMetadata));
                        JSONObject updateResponse = handleHttpResponse(execute(request, httpClient), JSONObject.class);
                        file.setId(updateResponse.getString("id"));
                    }
                    return new IDTuple(file.getFolderId(), file.getId());
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
                        HttpGet method = new HttpGet(buildUri(fid + "/files", qparams));
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
                        HttpGet method = new HttpGet(buildUri(fid + "/files", qparams));
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
                        HttpGet method = new HttpGet(buildUri(fid + "/files", qparams));
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
    public SearchIterator<File> search(final String pattern, List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(final String pattern, List<Field> fields, final String folderId, final boolean includeSubfolders, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final Map<String, Boolean> allowedFolders;
        if (null != folderId) {
            allowedFolders = new HashMap<String, Boolean>();
            allowedFolders.put(folderId, Boolean.TRUE);
        } else {
            allowedFolders = null;
        }
        return perform(new OneDriveClosure<SearchIterator<File>>() {

            @Override
            protected SearchIterator<File> doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    List<File> files = new LinkedList<File>();
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
                                OneDriveFile metadata = new OneDriveFile(jItem.getString("parent_id"), jItem.getString("id"), userId, getRootFolderId()).parseOneDriveFile(jItem);
                                if (null != allowedFolders) {
                                    Boolean allowed = allowedFolders.get(metadata.getFolderId());
                                    if (null == allowed) {
                                        allowed = Boolean.valueOf(includeSubfolders && isSubfolderOf(metadata.getFolderId(), folderId));
                                        allowedFolders.put(metadata.getFolderId(), allowed);
                                    }
                                    if (false == allowed.booleanValue()) {
                                        continue; // skip this file
                                    }
                                }
                                files.add(metadata);
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

    /**
     * Gets a value indicating whether a folder is a subfolder (at any level) of a parent folder.
     *
     * @param folderId The identifier of the folder to check
     * @param parentFolderId The identifier of the parent folder, or <code>null</code> to fall back to the root folder
     * @return <code>true</code> if the folder is a subfolder (at any level) of the parent folder, <code>false</code>, otherwise
     */
    private boolean isSubfolderOf(String folderId, String parentFolderId) throws OXException, IOException {
        String rootId = FileStorageFolder.ROOT_FULLNAME;
        String parentId = null != parentFolderId ? parentFolderId : rootId;
        String id = folderId;
        if (parentId.equals(rootId)) {
            return true;
        }
        if (id.equals(rootId) || id.equals(parentId)) {
            return false;
        }
        FileStorageFolderAccess folderAccess = getAccountAccess().getFolderAccess();
        do {
            FileStorageFolder folder = folderAccess.getFolder(id);
            id = folder.getParentId();
        } while (false == id.equals(parentId) && false == id.equals(rootId));
        return id.equals(parentId);
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        if (null == folderIds || 0 == folderIds.size()) {
            return Collections.emptyMap();
        }
        FileStorageFolderAccess folderAccess = getAccountAccess().getFolderAccess();
        Map<String, Long> sequenceNumbers = new HashMap<String, Long>(folderIds.size());
        for (String folderId : folderIds) {
            Date lastModifiedDate = folderAccess.getFolder(folderId).getLastModifiedDate();
            sequenceNumbers.put(folderId, null != lastModifiedDate ? Long.valueOf(lastModifiedDate.getTime()) : null);
        }
        return sequenceNumbers;
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
