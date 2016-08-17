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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.onedrive.access.OneDriveOAuthAccess;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;

/**
 * {@link AbstractOneDriveResourceAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOneDriveResourceAccess {

    /**
     * The Jackson object mapper instance.
     */
    private static final ObjectMapper MAPPER;
    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER = objectMapper;
    }

    /**
     * Gets the object mapper.
     *
     * @return The object mapper
     */
    protected static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    protected static <T> T parseIntoObject(InputStream inputStream, Class<T> clazz) throws OXException {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jp = jsonFactory.createParser(inputStream);
            return getObjectMapper().readValue(jp, clazz);
        } catch (JsonGenerationException | JsonMappingException | JsonParseException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------------------------------------------- //

    /** The status code policy to obey */
    public static interface StatusCodePolicy {

        /**
         * Examines given status line
         *
         * @param httpResponse The HTTP response
         * @throws OXException If an Open-Xchange error is yielded from status
         */
        void handleStatusCode(HttpResponse httpResponse) throws OXException;
    }

    /** The default status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_DEFAULT = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws OXException {
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                if (404 == statusCode) {
                    throw FileStorageExceptionCodes.NOT_FOUND.create(OneDriveConstants.ID, statusCode);
                }
                String reason;
                try {
                    JSONObject jsonObject = new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = jsonObject.getJSONObject("error").getString("message");
                } catch (Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw FileStorageExceptionCodes.PROTOCOL_ERROR.create("HTTP", statusCode + " " + reason);
            }
        }
    };

    /** The status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> while ignoring <code>404</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_IGNORE_NOT_FOUND = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws OXException {
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if ((statusCode < 200 || statusCode >= 300) && statusCode != 404) {
                String reason;
                try {
                    JSONObject jsonObject = new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = jsonObject.getJSONObject("error").getString("message");
                } catch (Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw FileStorageExceptionCodes.PROTOCOL_ERROR.create("HTTP", statusCode + " " + reason);
            }
        }
    };

    // -------------------------------------------------------------------------------------------------------------- //

    /**
     * The OneDrive base URL: <code>"https://apis.live.net/v5.0/"</code>
     */
    protected static final String URL_API_BASE = "https://apis.live.net/v5.0/";

    /** The type constants for a folder */
    private static final Set<String> TYPES_FOLDER = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(OneDriveConstants.TYPE_FOLDER, OneDriveConstants.TYPE_ALBUM)));

    /**
     * Checks if specified JSON item is a folder
     *
     * @param jItem The JSON item to check
     * @return <code>true</code> if folder; otherwise <code>false</code>
     */
    protected static boolean isFolder(JSONObject jItem) {
        return TYPES_FOLDER.contains(jItem.optString("type", null));
    }

    /**
     * Checks if specified JSON item is a file
     *
     * @param jItem The JSON item to check
     * @return <code>true</code> if file; otherwise <code>false</code>
     */
    protected static boolean isFile(JSONObject jItem) {
        return false == isFolder(jItem);
    }

    protected final OneDriveOAuthAccess oneDriveAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected String rootFolderIdentifier;

    /**
     * Initializes a new {@link AbstractOneDriveResourceAccess}.
     */
    protected AbstractOneDriveResourceAccess(OneDriveOAuthAccess oneDriveAccess, FileStorageAccount account, Session session) {
        super();
        this.oneDriveAccess = oneDriveAccess;
        this.account = account;
        this.session = session;
    }

    /**
     * Gets the root folder identifier
     *
     * @return The root folder identifier
     * @throws OXException If root folder cannot be returned
     */
    public String getRootFolderId() throws OXException {
        String rootFolderId = rootFolderIdentifier;
        if (null == rootFolderId) {
            String key = "com.openexchange.file.storage.onedrive.rootFolderId";
            rootFolderId = (String) session.getParameter(key);
            if (null == rootFolderId) {
                HttpRequestBase request = null;
                try {
                    int keepOn = 1;
                    while (keepOn > 0) {
                        DefaultHttpClient httpClient = (DefaultHttpClient) oneDriveAccess.getClient().client;
                        HttpGet method = new HttpGet(buildUri("/me/skydrive", initiateQueryString()));
                        request = method;
                        // HttpClients.setRequestTimeout(3500, method);

                        HttpResponse httpResponse = httpClient.execute(method);
                        if (SC_UNAUTHORIZED == httpResponse.getStatusLine().getStatusCode()) {
                            if (keepOn > 1) {
                                throw FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(account.getId(), OneDriveConstants.ID, httpResponse.getStatusLine());
                            }

                            reset(request);
                            request = null;
                            keepOn = 2;

                            oneDriveAccess.initialise();
                        } else {
                            JSONObject jResponse = handleHttpResponse(httpResponse, JSONObject.class);
                            rootFolderId = jResponse.optString("id", null);
                            keepOn = 0;
                        }
                    }
                } catch (HttpResponseException e) {
                    throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()), e.getMessage());
                } catch (IOException e) {
                    throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
                } finally {
                    reset(request);
                }
                session.setParameter(key, rootFolderId);
            }
            this.rootFolderIdentifier = rootFolderId;
        }
        return rootFolderId;
    }

    /**
     * Executes specified HTTP method/request using given HTTP client instance.
     *
     * @param method The method/request to execute
     * @param httpClient The HTTP client to use
     * @return The HTTP response
     * @throws ClientProtocolException If client protocol error occurs
     * @throws IOException If an I/O error occurs
     */
    protected HttpResponse execute(HttpRequestBase method, DefaultHttpClient httpClient) throws ClientProtocolException, IOException {
        //long st = System.currentTimeMillis();
        HttpResponse httpResponse = httpClient.execute(method);
        //long dur = System.currentTimeMillis() - st;
        //System.out.println("----------------------------------------------");
        //System.out.println("Executing " + method.getMethod() + " for " + method.getURI().getPath() + " took " + dur + "msec");
        //new Throwable().printStackTrace(System.out);
        //System.out.println("----------------------------------------------");
        return httpResponse;
    }

    /**
     * Resets given HTTP request
     *
     * @param request The HTTP request
     */
    public static void reset(HttpRequestBase request) {
        if (null != request) {
            try {
                request.reset();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Turns specified JSON value into an appropriate HTTP entity.
     *
     * @param jValue The JSON value
     * @return The HTTP entity
     * @throws JSONException If a JSON error occurs
     * @throws IOException If an I/O error occurs
     */
    protected InputStreamEntity asHttpEntity(JSONValue jValue) throws JSONException, IOException {
        if (null == jValue) {
            return null;
        }

        ByteArrayOutputStream bStream = Streams.newByteArrayOutputStream(1024);
        OutputStreamWriter osw = new OutputStreamWriter(bStream, Charsets.UTF_8);
        jValue.write(osw);
        osw.flush();
        return new InputStreamEntity(Streams.asInputStream(bStream), bStream.size(), ContentType.APPLICATION_JSON);
    }

    /**
     * Initiates the query string parameters
     *
     * @return The query string parameters
     */
    protected List<NameValuePair> initiateQueryString() {
        List<NameValuePair> qparams = new LinkedList<NameValuePair>();
        qparams.add(new BasicNameValuePair("access_token", oneDriveAccess.getOAuthAccount().getToken()));
        return qparams;
    }

    /**
     * Builds the URI from given arguments
     *
     * @param resourceId The resource identifier
     * @param queryString The query string parameters
     * @return The built URI string
     * @throws IllegalArgumentException If the given string violates RFC 2396
     */
    public static URI buildUri(String resourceId, List<NameValuePair> queryString) {
        try {
            return new URI("https", null, "apis.live.net", -1, "/v5.0/" + resourceId, null == queryString ? null : URLEncodedUtils.format(queryString, "UTF-8"), null);
        } catch (URISyntaxException x) {
            IllegalArgumentException y = new IllegalArgumentException();
            y.initCause(x);
            throw y;
        }
    }

    /**
     * Handles given HTTP response while expecting <code>200 (Ok)</code> status code.
     *
     * @param httpResponse The HTTP response
     * @param clazz The class of the result object
     * @return The result object
     * @throws OXException If an Open-Xchange error occurs
     * @throws ClientProtocolException If a client protocol error occurs
     * @throws IOException If an I/O error occurs
     */
    public static <R> R handleHttpResponse(HttpResponse httpResponse, Class<R> clazz) throws OXException, ClientProtocolException, IOException {
        return handleHttpResponse(httpResponse, STATUS_CODE_POLICY_DEFAULT, clazz);
    }

    /**
     * Handles given HTTP response while expecting given status code.
     *
     * @param httpResponse The HTTP response
     * @param policy The status code policy to obey
     * @param clazz The class of the result object
     * @return The result object
     * @throws OXException If an Open-Xchange error occurs
     * @throws ClientProtocolException If a client protocol error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If content stream cannot be created
     */
    protected static <R> R handleHttpResponse(HttpResponse httpResponse, StatusCodePolicy policy, Class<R> clazz) throws OXException, ClientProtocolException, IOException {
        policy.handleStatusCode(httpResponse);

        // OK, continue
        if (Void.class.equals(clazz)) {
            return null;
        }
        if (JSONObject.class.equals(clazz)) {
            try {
                return (R) new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
            } catch (JSONException e) {
                throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
        return parseIntoObject(httpResponse.getEntity().getContent(), clazz);
    }

    /**
     * Performs given closure.
     *
     * @param closure The closure to perform
     * @param httpClient The client to use
     * @return The return value
     * @throws OXException If performing closure fails
     */
    protected <R> R perform(OneDriveClosure<R> closure) throws OXException {
        return closure.perform(this, (DefaultHttpClient) oneDriveAccess.getClient().client, session);
    }

    /**
     * Handles authentication error.
     *
     * @param e The authentication error
     * @param session The associated session
     * @throws OXException If authentication error could not be handled
     */
    protected void handleAuthError(HttpResponseException e, Session session) throws OXException {
        try {
            oneDriveAccess.initialise();
        } catch (OXException oxe) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractOneDriveResourceAccess.class);
            logger.warn("Could not re-initialize Microsoft OneDrive access", oxe);

            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, OneDriveConstants.ID, e.getMessage());
        }
    }

    /** Status code (401) indicating that the request requires HTTP authentication. */
    private static final int SC_UNAUTHORIZED = 401;

    /** Status code (404) indicating that the requested resource is not available. */
    private static final int SC_NOT_FOUND = 404;

    /**
     * Handles given HTTP response error.
     *
     * @param identifier The optional identifier for associated Microsoft OneDrive resource
     * @param e The HTTP error
     * @return The resulting exception
     */
    protected OXException handleHttpResponseError(String identifier, String accountId, HttpResponseException e) {
        if (null != identifier && SC_NOT_FOUND == e.getStatusCode()) {
            return FileStorageExceptionCodes.NOT_FOUND.create(e, OneDriveConstants.ID, identifier);
        }
        if (null != accountId && SC_UNAUTHORIZED == e.getStatusCode()) {
            return FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(accountId, OneDriveConstants.ID, SC_UNAUTHORIZED);
        }
        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()), e.getMessage());
    }

    /**
     * Gets the OneDrive folder identifier from given file storage folder identifier
     *
     * @param folderId The file storage folder identifier
     * @return The appropriate OneDrive folder identifier
     * @throws OXException If operation fails
     */
    protected String toOneDriveFolderId(String folderId) throws OXException {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? getRootFolderId() : folderId;
    }

    /**
     * Gets the file storage folder identifier from given OneDrive folder identifier
     *
     * @param oneDriveId The OneDrive folder identifier
     * @return The appropriate file storage folder identifier
     * @throws OXException If operation fails
     */
    protected String toFileStorageFolderId(String oneDriveId) throws OXException {
        return getRootFolderId().equals(oneDriveId) ? FileStorageFolder.ROOT_FULLNAME : oneDriveId;
    }

}
