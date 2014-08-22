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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.copycom.access.CopyComAccess;
import com.openexchange.file.storage.copycom.http.client.methods.HttpMove;
import com.openexchange.file.storage.copycom.rest.folder.RestFolder;
import com.openexchange.session.Session;

/**
 * {@link CopyComFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CopyComFolderAccess extends AbstractCopyComResourceAccess implements FileStorageFolderAccess {

    private final CopyComAccountAccess accountAccess;
    private final int userId;
    private final String accountDisplayName;

    /**
     * Initializes a new {@link CopyComFolderAccess}.
     */
    public CopyComFolderAccess(final CopyComAccess boxAccess, final FileStorageAccount account, final Session session, final CopyComAccountAccess accountAccess) throws OXException {
        super(boxAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
    }

    private boolean hasSubfolders(String copyComFolderId, DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
        HttpRequestBase request = null;
        try {
            List<NameValuePair> qparams = initiateQueryString();
            HttpGet method = new HttpGet(buildUri(copyComFolderId+"/files", qparams));
            request = method;

            JSONObject jResponse = handleHttpResponse(httpClient.execute(method), JSONObject.class);
            JSONArray jData = jResponse.getJSONArray("data");
            int length = jData.length();
            for (int i = 0; i < length; i++) {
                JSONObject jItem = jData.getJSONObject(i);
                if (isFolder(jItem)) {
                    return true;
                }
            }
            return false;
        } finally {
            reset(request);
        }
    }

    protected CopyComFolder parseFolder(String copyComFolderId, RestFolder restFolder, DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
        return new CopyComFolder(userId).parseDirEntry(restFolder, rootFolderId, accountDisplayName, hasSubfolders(copyComFolderId, httpClient));
    }

    protected CopyComFolder parseFolder(String copyComFolderId, JSONObject jFolder, DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
        return new CopyComFolder(userId).parseDirEntry(jFolder, rootFolderId, accountDisplayName, hasSubfolders(copyComFolderId, httpClient));
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        return perform(new CopyComClosure<Boolean>() {

            @Override
            protected Boolean doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpGet method = new HttpGet(buildUri(toCopyComFolderId(folderId), initiateQueryString()));
                    request = method;

                    HttpResponse response = httpClient.execute(method);
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
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        return perform(new CopyComClosure<FileStorageFolder>() {

            @Override
            protected FileStorageFolder doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toCopyComFolderId(folderId);
                    HttpGet method = new HttpGet(buildUri(fid, initiateQueryString()));
                    request = method;

                    RestFolder restFolder = handleHttpResponse(httpClient.execute(method), RestFolder.class);
                    return parseFolder(fid, restFolder, httpClient);
                } finally {
                    if (null != request) {
                        request.releaseConnection();
                    }
                }
            }
        });
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        return perform(new CopyComClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toCopyComFolderId(parentIdentifier);
                    List<FileStorageFolder> folders = new LinkedList<FileStorageFolder>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("offset", Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair("limit", Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;

                        JSONObject jResponse = handleHttpResponse(httpClient.execute(method), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            if (isFolder(jItem)) {
                                folders.add(parseFolder(fid, jItem, httpClient));
                            }
                        }
                        reset(request);
                        request = null;

                        offset += limit;
                    } while (resultsFound == limit);

                    return folders.toArray(new FileStorageFolder[folders.size()]);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(FileStorageFolder.ROOT_FULLNAME);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        return perform(new CopyComClosure<String>() {

            @Override
            protected String doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpMove method = new HttpMove(buildUri(toCopyComFolderId(toCreate.getParentId()), null));
                    request = method;
                    method.setHeader("Authorization", "Bearer " + copyComAccess.getAccessToken());
                    method.setHeader("Content-Type", "application/json");
                    method.setEntity(asHttpEntity(new JSONObject(2).put("name", toCreate.getName())));

                    JSONObject jResponse = handleHttpResponse(httpClient.execute(method), JSONObject.class);
                    return jResponse.getString("id");
                } finally {
                    reset(request);
                }

            }
        });
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // Neither support for subscription nor permissions
        return identifier;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId, final String newName) throws OXException {
        return perform(new CopyComClosure<String>() {

            @Override
            protected String doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    {
                        HttpMove method = new HttpMove(buildUri(toCopyComFolderId(folderId), null));
                        request = method;
                        method.setHeader("Authorization", "Bearer " + copyComAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("destination", toCopyComFolderId(newParentId))));

                        handleHttpResponse(httpClient.execute(method), Void.class);
                        reset(request);
                        request = null;
                    }

                    if (null != newName) {
                        HttpPut method = new HttpPut(buildUri(toCopyComFolderId(folderId), null));
                        request = method;
                        method.setHeader("Authorization", "Bearer " + copyComAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("name", newName)));

                        handleHttpResponse(httpClient.execute(method), Void.class);
                        reset(request);
                        request = null;
                    }

                    return folderId;
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(folderId, e);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        return perform(new CopyComClosure<String>() {

            @Override
            protected String doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpPut method = new HttpPut(buildUri(toCopyComFolderId(folderId), null));
                    request = method;
                    method.setHeader("Authorization", "Bearer " + copyComAccess.getAccessToken());
                    method.setHeader("Content-Type", "application/json");
                    method.setEntity(asHttpEntity(new JSONObject(2).put("name", newName)));

                    handleHttpResponse(httpClient.execute(method), Void.class);
                    reset(request);
                    request = null;

                    return folderId;
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(folderId, e);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(final String folderId, boolean hardDelete) throws OXException {
        return perform(new CopyComClosure<String>() {

            @Override
            protected String doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    HttpDelete method = new HttpDelete(buildUri(toCopyComFolderId(folderId), initiateQueryString()));
                    request = method;

                    handleHttpResponse(httpClient.execute(method), Void.class);
                    reset(request);
                    request = null;

                    return folderId;
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(folderId, e);
                } finally {
                    reset(request);
                }
            }
        });
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(final String folderId, boolean hardDelete) throws OXException {
        perform(new CopyComClosure<Void>() {

            @Override
            protected Void doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpRequestBase request = null;
                try {
                    String fid = toCopyComFolderId(folderId);
                    List<String> list = new LinkedList<String>();

                    int limit = 100;
                    int offset = 0;
                    int resultsFound;

                    do {
                        List<NameValuePair> qparams = initiateQueryString();
                        qparams.add(new BasicNameValuePair("offset", Integer.toString(offset)));
                        qparams.add(new BasicNameValuePair("limit", Integer.toString(limit)));
                        HttpGet method = new HttpGet(buildUri(fid+"/files", qparams));
                        request = method;

                        JSONObject jResponse = handleHttpResponse(httpClient.execute(method), JSONObject.class);
                        JSONArray jData = jResponse.getJSONArray("data");
                        int length = jData.length();
                        resultsFound = length;
                        for (int i = 0; i < length; i++) {
                            JSONObject jItem = jData.getJSONObject(i);
                            list.add(jItem.getString("id"));
                        }

                        reset(request);
                        request = null;

                        offset += limit;
                    } while (resultsFound == limit);

                    for (String id : list) {
                        HttpDelete method = new HttpDelete(buildUri(id, initiateQueryString()));
                        request = method;

                        handleHttpResponse(httpClient.execute(method), Void.class);
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
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        return perform(new CopyComClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {

                List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();

                String fid = folderId;
                FileStorageFolder f = getFolder(fid);
                list.add(f);

                while (!FileStorageFolder.ROOT_FULLNAME.equals(fid)) {
                    fid = f.getParentId();
                    f = getFolder(fid);
                    list.add(f);
                }

                return list.toArray(new FileStorageFolder[list.size()]);
            }
        });
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return Type.STORAGE.getUnlimited();
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        final Quota[] ret = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            ret[i] = types[i].getUnlimited();
        }
        return ret;
    }

}
