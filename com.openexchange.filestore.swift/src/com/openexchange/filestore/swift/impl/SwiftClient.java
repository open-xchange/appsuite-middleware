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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.filestore.swift.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.swift.SwiftExceptionCode;
import com.openexchange.java.Charsets;
import com.openexchange.java.util.UUIDs;

/**
 * {@link SwiftClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SwiftClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SwiftClient.class);

    private final String userName;
    private final AuthValue authValue;
    private final EndpointPool endpoints;
    private final HttpClient httpClient;
    private final String containerName;
    private final int contextId;
    private final int userId;
    private final AtomicReference<Token> tokenRef;

    /**
     * Initializes a new {@link SwiftClient}.
     *
     * @param swiftConfig The Swift configuration
     * @param contextId The context identifier
     * @param userId The user identifier
     * @throws OXException If initialization fails
     */
    public SwiftClient(SwiftConfig swiftConfig, int contextId, int userId) throws OXException {
        super();
        tokenRef = new AtomicReference<Token>();
        this.userName = swiftConfig.getUserName();
        this.authValue = swiftConfig.getAuthValue();
        this.endpoints = swiftConfig.getEndpointPool();
        this.httpClient = swiftConfig.getHttpClient();
        this.contextId = contextId;
        this.userId = userId;

        // E.g. "57462_ctx_5_user_store" or "57462_ctx_store"
        StringBuilder sb = new StringBuilder(32).append(contextId).append("_ctx");
        if (userId > 0) {
            sb.append(userId).append("_user");
        }
        sb.append("_store");
        containerName = sb.toString();

        // Get token
        acquireToken(null);
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Stores a new object.
     *
     * @param data The content to store
     * @param length The content length
     * @return The new identifier of the stored object
     */
    public UUID put(InputStream data, long length) throws OXException {
        return put(data, length, 0);
    }

    private UUID put(InputStream data, long length, int retryCount) throws OXException {
        Token token = acquireTokenIfExpired();

        HttpResponse response = null;
        HttpPut put = null;
        Endpoint endpoint = getEndpoint();
        try {
            UUID id = UUID.randomUUID();
            put = new HttpPut(endpoint.getObjectUrl(containerName, id));
            put.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            put.setEntity(new InputStreamEntity(data, length));
            response = httpClient.execute(put);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_CREATED == status) {
                return id;
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                throw SwiftExceptionCode.AUTH_FAILED.create();
            }
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
        } catch (IOException e) {
            OXException error = handleCommunicationError(endpoint, e);
            if (null == error) {
                // Retry... With exponential back-off
                int nextRetry = retryCount + 1;
                if (nextRetry > 5) {
                    // Give up...
                    throw FileStorageCodes.IOERROR.create(e, e.getMessage());
                }

                long nanosToWait = TimeUnit.NANOSECONDS.convert((nextRetry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
                LockSupport.parkNanos(nanosToWait);
                return put(data, length, nextRetry);
            }

            throw error;
        } finally {
            Utils.close(put, response);
        }
    }

    /**
     * Gets the input stream of a stored file.
     *
     * @param id The identifier of the file
     * @return The file's input stream
     */
    public InputStream get(UUID id) throws OXException {
        return get(id, 0, -1);
    }

    /**
     * Gets the input stream of a stored object.
     *
     * @param id The identifier of the object
     * @param rangeStart The (inclusive) start of the requested byte range, or a value equal or smaller <code>0</code> if not used
     * @param rangeEnd The (inclusive) end of the requested byte range, or a value equal or smaller <code>0</code> if not used
     * @return The object's input stream
     */
    public InputStream get(UUID id, long rangeStart, long rangeEnd) throws OXException {
        return get(id, rangeStart, rangeEnd, 0);
    }

    private InputStream get(UUID id, long rangeStart, long rangeEnd, int retryCount) throws OXException {
        Token token = acquireTokenIfExpired();

        HttpGet get = null;
        HttpResponse response = null;
        Endpoint endpoint = getEndpoint();
        try {
            get = new HttpGet(endpoint.getObjectUrl(containerName, id));
            get.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            if (0 < rangeStart || 0 < rangeEnd) {
                get.addHeader("Range", "bytes=" + rangeStart + "-" + rangeEnd);
            }
            response = httpClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_PARTIAL_CONTENT == status) {
                InputStream content = response.getEntity().getContent();
                response = null;
                get = null;
                return content;
            }
            if (HttpServletResponse.SC_NOT_FOUND == status) {
                throw FileStorageCodes.FILE_NOT_FOUND.create(UUIDs.getUnformattedString(id));
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                throw SwiftExceptionCode.AUTH_FAILED.create();
            }
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
        } catch (IOException e) {
            OXException error = handleCommunicationError(endpoint, e);
            if (null == error) {
                // Retry... With exponential back-off
                int nextRetry = retryCount + 1;
                if (nextRetry > 5) {
                    // Give up...
                    throw FileStorageCodes.IOERROR.create(e, e.getMessage());
                }

                long nanosToWait = TimeUnit.NANOSECONDS.convert((nextRetry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
                LockSupport.parkNanos(nanosToWait);
                return get(id, rangeStart, rangeEnd, nextRetry);
            }

            throw error;
        } finally {
            Utils.close(get, response);
        }
    }

    /**
     * Deletes a stored object.
     *
     * @param id The identifier of the object to delete
     * @return <code>true</code> if the object was deleted successfully, <code>false</code> if it was not found
     * @throws OXException
     */
    public boolean delete(UUID id) throws OXException {
        return delete(id, 0);
    }

    private boolean delete(UUID id, int retryCount) throws OXException {
        Token token = acquireTokenIfExpired();

        HttpDelete delete = null;
        HttpResponse response = null;
        Endpoint endpoint = getEndpoint();
        try {
            delete = new HttpDelete(endpoint.getObjectUrl(containerName, id));
            delete.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            response = httpClient.execute(delete);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status) {
                return true;
            }
            if (HttpServletResponse.SC_NOT_FOUND == status) {
                return false;
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                throw SwiftExceptionCode.AUTH_FAILED.create();
            }
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
        } catch (IOException e) {
            OXException error = handleCommunicationError(endpoint, e);
            if (null == error) {
                // Retry... With exponential back-off
                int nextRetry = retryCount + 1;
                if (nextRetry > 5) {
                    // Give up...
                    throw FileStorageCodes.IOERROR.create(e, e.getMessage());
                }

                long nanosToWait = TimeUnit.NANOSECONDS.convert((nextRetry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
                LockSupport.parkNanos(nanosToWait);
                return delete(id, nextRetry);
            }

            throw error;
        } finally {
            Utils.close(delete, response);
        }
    }

    /**
     * Deletes multiple stored objects.
     *
     * @param ids The identifier of the objects to delete
     */
    public void delete(Collection<UUID> ids) throws OXException {
        for (UUID id : ids) {
            delete(id);
        }
    }

    /**
     * Gets an end-point from the pool.
     *
     * @return The end-point
     * @throws OXException If no end-point is available (i.e. all are blacklisted due to connection timeouts).
     */
    private Endpoint getEndpoint() throws OXException {
        Endpoint endpoint = endpoints.get(contextId, userId);
        if (endpoint == null) {
            throw SwiftExceptionCode.STORAGE_UNAVAILABLE.create();
        }
        return endpoint;
    }

    /**
     * Handles communication errors. If the end-point is not available it is blacklisted.
     *
     * @param endpoint The end-point for which the exception occurred.
     * @param e The exception
     * @return An OXException to re-throw
     */
    private OXException handleCommunicationError(Endpoint endpoint, IOException e) {
        if (Utils.endpointUnavailable(endpoint.getBaseUrl(), httpClient)) {
            LOG.warn("Swift end-point is unavailable: {}", endpoint);
            boolean anyAvailable = endpoints.blacklist(endpoint.getBaseUrl());
            if (anyAvailable) {
                // Signal retry
                return null;
            }
        }

        return FileStorageCodes.IOERROR.create(e, e.getMessage());
    }

    private Token acquireTokenIfExpired() throws OXException {
        Token token = tokenRef.get();
        return token.isExpired() ? acquireToken(token) : token;
    }

    private synchronized Token acquireToken(Token token) throws OXException {
        // Check if already newly acquired
        {
            Token cand = tokenRef.get();
            if (token != cand) {
                return cand;
            }
        }

        // Get a new one
        HttpPost post = null;
        HttpResponse response = null;
        try {
            post = new HttpPost("https://identity.api.rackspacecloud.com/v2.0/tokens");

            {
                JSONObject jAuthData = new JSONObject(2);
                if (AuthValue.Type.PASSWORD.equals(authValue.getType())) {
                    jAuthData = new JSONObject(2).put("passwordCredentials", new JSONObject(3).put("username", userName).put("password", authValue.getValue()));
                } else {
                    jAuthData = new JSONObject(2).put("RAX-KSKEY:apiKeyCredentials", new JSONObject(3).put("username", userName).put("apiKey", authValue.getValue()));
                }
                JSONObject jRequestBody = new JSONObject(2).put("auth", jAuthData);
                post.setEntity(new StringEntity(jRequestBody.toString(), ContentType.APPLICATION_JSON));
            }

            response = httpClient.execute(post);

            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK != status) {
                throw SwiftExceptionCode.AUTH_FAILED.create();
            }

            JSONObject jResponse = new JSONObject(new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));
            JSONObject jAccess = jResponse.getJSONObject("access");
            JSONObject jToken = jAccess.getJSONObject("token");
            Token newToken = Token.parseFrom(jToken);
            tokenRef.set(newToken);
            return newToken;
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Utils.close(post, response);
        }
    }

}
