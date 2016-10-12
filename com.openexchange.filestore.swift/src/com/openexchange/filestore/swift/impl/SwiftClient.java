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

package com.openexchange.filestore.swift.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.swift.SwiftExceptionCode;
import com.openexchange.filestore.swift.impl.token.Token;
import com.openexchange.filestore.swift.impl.token.TokenStorage;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;

/**
 * {@link SwiftClient}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class SwiftClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SwiftClient.class);

    /**
     * The delimiter character to separate the prefix from the keys
     */
    public static final char DELIMITER = '/';

    private final String userName;
    private final AuthInfo authValue;
    private final EndpointPool endpoints;
    private final HttpClient httpClient;
    private final String prefix;
    private final TokenStorage tokenStorage;

    /**
     * Initializes a new {@link SwiftClient}.
     *
     * @param swiftConfig The Swift configuration
     * @param prefix The prefix to use
     * @param tokenStorage The token storage
     */
    public SwiftClient(SwiftConfig swiftConfig, String prefix, TokenStorage tokenStorage) {
        super();
        this.userName = swiftConfig.getUserName();
        this.authValue = swiftConfig.getAuthInfo();
        this.endpoints = swiftConfig.getEndpointPool();
        this.httpClient = swiftConfig.getHttpClient();
        this.tokenStorage = tokenStorage;
        this.prefix = prefix;
    }

    /**
     * Gets the prefix
     *
     * @return The prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Deletes associated container
     *
     * @throws OXException If container deletion fails
     */
    public void deleteContainer() throws OXException {
        Endpoint endpoint = getEndpoint();

        int limit = 100;
        String marker = null;

        List<String> objects;
        int numResults;
        do {
            objects = listContainerFiles(marker, limit, endpoint, 0, false);
            numResults = objects.size();
            if (numResults > 0) {
                for (String name : objects) {
                    delete(name, endpoint, 0, false);
                }
                marker = objects.get(numResults - 1);
            }
        } while (numResults >= limit);
    }

    private List<String> listContainerFiles(String marker, int limit, Endpoint endpoint, int retryCount, boolean withNewToken) throws OXException {
        HttpGet get = null;
        HttpResponse response = null;
        try {
            Token token = endpoint.getToken();

            Map<String, String> parameters = Utils.mapFor("format", "json", "prefix", prefix + DELIMITER, "delimiter", Character.toString(DELIMITER), "limit", Integer.toString(limit));
            if (null != marker) {
                parameters.put("marker", marker);
            }
            List<NameValuePair> queryString = Utils.toQueryString(parameters);
            get = new HttpGet(Utils.buildUri(endpoint.getContainerUri(), queryString));
            get.setHeader(new BasicHeader("X-Auth-Token", endpoint.getToken().getId()));

            response = httpClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status) {
                // Successful
                JSONArray jResponse = new JSONArray(new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));
                int length = jResponse.length();
                List<String> ids = new ArrayList<String>(length);
                for (int i = length, c = 0; i-- > 0;) {
                    ids.add(jResponse.getJSONObject(c++).getString("name"));
                }
            }
            if (HttpServletResponse.SC_NO_CONTENT == status) {
                // Successful, but empty
                return Collections.emptyList();
            }
            if (HttpServletResponse.SC_NOT_FOUND == status) {
                // Such a container does not exist
                return Collections.emptyList();
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                if (withNewToken) {
                    // Already invoked with a newly acquired token
                    String reasonPhrase = response.getStatusLine().getReasonPhrase();
                    throw Strings.isEmpty(reasonPhrase) ? SwiftExceptionCode.UNAUTHORIZED_SIMPLE.create() : SwiftExceptionCode.UNAUTHORIZED.create(reasonPhrase);
                }
                // Token expired intermittently
                acquireNewTokenFor(token, endpoint);
                Utils.close(get, response);
                return listContainerFiles(marker, limit, endpoint, retryCount, true);
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
                return listContainerFiles(marker, limit, endpoint, nextRetry, false);
            }

            throw error;
        } catch (JSONException e) {
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Utils.close(get, response);
        }
    }

    /**
     * Creates associated container if absent
     *
     * @throws OXException If container creation fails
     */
    public void createContainerIfAbsent() throws OXException {
        Endpoint endpoint = getEndpoint();

        if (false == existsContainer(endpoint, 0, false)) {
            createContainer(endpoint, 0, false);
        }
    }

    private boolean existsContainer(Endpoint endpoint, int retryCount, boolean withNewToken) throws OXException {
        HttpGet get = null;
        HttpResponse response = null;
        try {
            Token token = endpoint.getToken();
            get = new HttpGet(endpoint.getContainerUri());
            get.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            response = httpClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_NO_CONTENT == status) {
                // Does already exist
                return true;
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                if (withNewToken) {
                    // Already invoked with a newly acquired token
                    String reasonPhrase = response.getStatusLine().getReasonPhrase();
                    throw Strings.isEmpty(reasonPhrase) ? SwiftExceptionCode.UNAUTHORIZED_SIMPLE.create() : SwiftExceptionCode.UNAUTHORIZED.create(reasonPhrase);
                }
                // Token expired intermittently
                acquireNewTokenFor(token, endpoint);
                Utils.close(get, response);
                return existsContainer(endpoint, retryCount, true);
            }
            if (HttpServletResponse.SC_NOT_FOUND != status) {
                throw SwiftExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
            }

            // 404 -> Does not exist, yet
            return false;
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
                return existsContainer(endpoint, nextRetry, false);
            }

            throw error;
        } finally {
            Utils.close(get, response);
        }
    }

    private boolean createContainer(Endpoint endpoint, int retryCount, boolean withNewToken) throws OXException {
        HttpPut put = null;
        HttpResponse response = null;
        try {
            Token token = endpoint.getToken();
            put = new HttpPut(endpoint.getContainerUri());
            put.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            response = httpClient.execute(put);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_CREATED == status || HttpServletResponse.SC_ACCEPTED == status) {
                // Does already exist
                return true;
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                if (withNewToken) {
                    // Already invoked with a newly acquired token
                    String reasonPhrase = response.getStatusLine().getReasonPhrase();
                    throw Strings.isEmpty(reasonPhrase) ? SwiftExceptionCode.UNAUTHORIZED_SIMPLE.create() : SwiftExceptionCode.UNAUTHORIZED.create(reasonPhrase);
                }
                // Token expired intermittently
                acquireNewTokenFor(token, endpoint);
                Utils.close(put, response);
                return createContainer(endpoint, retryCount, true);
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
                return createContainer(endpoint, nextRetry, false);
            }

            throw error;
        } finally {
            Utils.close(put, response);
        }
    }

    /**
     * Stores a new object.
     *
     * @param data The content to store
     * @param length The content length
     * @return The new identifier of the stored object
     */
    public UUID put(InputStream data, long length) throws OXException {
        if (null == data) {
            return null;
        }

        Endpoint endpoint = getEndpoint();
        return put(data, length, endpoint, 0, false);
    }

    private UUID put(InputStream data, long length, Endpoint endpoint, int retryCount, boolean withNewToken) throws OXException {
        HttpResponse response = null;
        HttpPut put = null;
        try {
            Token token = endpoint.getToken();
            UUID id = UUID.randomUUID();
            put = new HttpPut(endpoint.getObjectUri(prefix, id));
            put.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            put.setEntity(new InputStreamEntity(data, length));
            response = httpClient.execute(put);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_CREATED == status) {
                return id;
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                if (withNewToken) {
                    // Already invoked with a newly acquired token
                    String reasonPhrase = response.getStatusLine().getReasonPhrase();
                    throw Strings.isEmpty(reasonPhrase) ? SwiftExceptionCode.UNAUTHORIZED_SIMPLE.create() : SwiftExceptionCode.UNAUTHORIZED.create(reasonPhrase);
                }
                // Token expired intermittently
                acquireNewTokenFor(token, endpoint);
                Utils.close(put, response);
                return put(data, length, endpoint, retryCount, true);
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
                return put(data, length, endpoint, nextRetry, false);
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
        if (null == id) {
            throw FileStorageCodes.FILE_NOT_FOUND.create("null");
        }

        Endpoint endpoint = getEndpoint();
        return get(id, rangeStart, rangeEnd, endpoint, 0, false);
    }

    private InputStream get(UUID id, long rangeStart, long rangeEnd, Endpoint endpoint, int retryCount, boolean withNewToken) throws OXException {
        HttpGet get = null;
        HttpResponse response = null;
        try {
            Token token = endpoint.getToken();
            get = new HttpGet(endpoint.getObjectUri(prefix, id));
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
                if (withNewToken) {
                    // Already invoked with a newly acquired token
                    String reasonPhrase = response.getStatusLine().getReasonPhrase();
                    throw Strings.isEmpty(reasonPhrase) ? SwiftExceptionCode.UNAUTHORIZED_SIMPLE.create() : SwiftExceptionCode.UNAUTHORIZED.create(reasonPhrase);
                }
                // Token expired intermittently
                acquireNewTokenFor(token, endpoint);
                Utils.close(get, response);
                return get(id, rangeStart, rangeEnd, endpoint, retryCount, true);
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
                return get(id, rangeStart, rangeEnd, endpoint, nextRetry, false);
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
        if (null == id) {
            return false;
        }

        Endpoint endpoint = getEndpoint();
        return delete(Utils.addPrefix(prefix, id), endpoint, 0, false);
    }

    private boolean delete(String id, Endpoint endpoint, int retryCount, boolean withNewToken) throws OXException {
        HttpDelete delete = null;
        HttpResponse response = null;
        try {
            Token token = endpoint.getToken();
            delete = new HttpDelete(endpoint.getObjectUri(id));
            delete.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            response = httpClient.execute(delete);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_NO_CONTENT == status) {
                return true;
            }
            if (HttpServletResponse.SC_NOT_FOUND == status) {
                return false;
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                if (withNewToken) {
                    // Already invoked with a newly acquired token
                    String reasonPhrase = response.getStatusLine().getReasonPhrase();
                    throw Strings.isEmpty(reasonPhrase) ? SwiftExceptionCode.UNAUTHORIZED_SIMPLE.create() : SwiftExceptionCode.UNAUTHORIZED.create(reasonPhrase);
                }
                // Token expired intermittently
                acquireNewTokenFor(token, endpoint);
                Utils.close(delete, response);
                delete(id, endpoint, retryCount, true);
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
                return delete(id, endpoint, nextRetry, false);
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
        if (null == ids || ids.isEmpty()) {
            return;
        }

        Endpoint endpoint = getEndpoint();
        for (UUID id : ids) {
            delete(Utils.addPrefix(prefix, id), endpoint, 0, false);
        }
    }

    /**
     * Gets an end-point from the pool.
     *
     * @return The end-point
     * @throws OXException If no end-point is available (i.e. all are blacklisted due to connection timeouts).
     */
    private Endpoint getEndpoint() throws OXException {
        // Grab next end-point to use
        Endpoint endpoint = endpoints.get(prefix);
        if (endpoint == null) {
            throw SwiftExceptionCode.STORAGE_UNAVAILABLE.create();
        }

        // Check the token associated with it
        getOrAcquireNewTokenIfExpiredFor(endpoint);

        // Return...
        return endpoint;
    }

    /**
     * Handles communication errors. If the end-point is not available it is blacklisted.
     *
     * @param endpoint The end-point for which the exception occurred
     * @param e The exception
     * @return An OXException to re-throw or <code>null</code> to retry with another available end-point
     */
    private OXException handleCommunicationError(Endpoint endpoint, IOException e) {
        Boolean unavailable = Utils.endpointUnavailable(endpoint, httpClient);
        if (null != unavailable && unavailable.booleanValue()) {
            LOG.warn("Swift end-point is unavailable: {}", endpoint);
            boolean anyAvailable = endpoints.blacklist(endpoint);
            if (anyAvailable) {
                // Signal retry
                return null;
            }
        }

        return FileStorageCodes.IOERROR.create(e, e.getMessage());
    }

    private Token getOrAcquireNewTokenIfExpiredFor(Endpoint endpoint) throws OXException {
        Token token = endpoint.getToken();
        return null == token || token.isExpired() ? acquireNewTokenFor(token, endpoint) : token;
    }

    private Token acquireNewTokenFor(Token expiredToken, Endpoint endpoint) throws OXException {
        Object lock = endpoint.getLock();
        synchronized (lock) {
            // Check if already newly acquired
            {
                Token current = endpoint.getToken();
                if (expiredToken != current) {
                    // Another thread acquired a new token in the meantime
                    return current;
                }
            }

            // Invalidate token
            endpoint.invalidateToken();

            // Check possible valid one held in storage
            String id = endpoint.getId();
            {
                Token storedToken = tokenStorage.get(id);
                if (null != storedToken && !storedToken.isExpired() && (!storedToken.getId().equals(null == expiredToken ? null : expiredToken.getId()))) {
                    return applyToken(storedToken, endpoint);
                }
            }

            // Try to acquire lock
            if (false == tokenStorage.lock(id)) {
                // Failed to get lock; await concurrent token acquisition to complete (using exponential back-off)
                {
                    LOG.info("Awaiting new token for Swift object store \"{}\"", endpoint.getBaseUri());
                    int retry = 1;
                    do {
                        if (retry > 1) {
                            LOG.info("Still waiting for new token for Swift object store \"{}\"", endpoint.getBaseUri());
                        }
                        long nanosToWait = TimeUnit.NANOSECONDS.convert((retry++ * 100) + ((long) (Math.random() * 100)), TimeUnit.MILLISECONDS);
                        LockSupport.parkNanos(nanosToWait);
                    } while (tokenStorage.isLocked(id));
                }

                // Grab new token and check its validity
                Token newToken = tokenStorage.get(id);
                if (null == newToken || newToken.isExpired()) {
                    // Overall retry...
                    return acquireNewTokenFor(expiredToken, endpoint);
                }

                // Accept & set the new token
                return applyToken(newToken, endpoint);
            }

            // Acquired lock...
            LOG.info("Acquiring new token for Swift object store \"{}\"...", endpoint.getBaseUri());
            try {
                // Request new token
                Token newToken = doAcquireNewToken();
                LOG.info("Acquired new token for Swift object store \"{}\"", endpoint.getBaseUri());

                // Store it
                tokenStorage.store(newToken, id);

                // Apply it
                return applyToken(newToken, endpoint);
            } finally {
                tokenStorage.unlock(id);
            }
        }
    }

    private Token applyToken(Token token, Endpoint endpoint) {
        endpoint.setToken(token);
        return token;
    }

    private Token doAcquireNewToken() throws OXException {
        return doAcquireNewToken(userName, authValue, httpClient).getToken();
    }

    /**
     * Acquires a new token according to specified arguments.
     *
     * @param userName The associated user name
     * @param authValue The auth value to use
     * @param httpClient The optional HTTP client
     * @return The newly acquired token
     * @throws OXException If token cannot be acquired
     */
    public static TokenAndResponse doAcquireNewToken(String userName, AuthInfo authValue, HttpClient httpClient) throws OXException {
        // Get a new one
        HttpPost post = null;
        HttpResponse response = null;
        try {
            {
                String identityEndPoint = authValue.getIdentityUrl();
                JSONObject jAuthData = new JSONObject(2);
                switch (authValue.getType()) {
                    case PASSWORD_V2: {
                        if (Strings.isEmpty(identityEndPoint)) {
                            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("identityUrl");
                        }

                        String tenantName = authValue.getTenantName();
                        if (Strings.isEmpty(tenantName)) {
                            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("tenantName");
                        }

                        post = new HttpPost(identityEndPoint);
                        jAuthData = new JSONObject(4).put("tenantName", tenantName).put("passwordCredentials", new JSONObject(3).put("username", userName).put("password", authValue.getValue()));
                        break;
                    }
                    case PASSWORD_V3: {
                        if (Strings.isEmpty(identityEndPoint)) {
                            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("identityUrl");
                        }

                        String tenantName = authValue.getTenantName();
                        if (Strings.isEmpty(tenantName)) {
                            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("tenantName");
                        }

                        String domain = authValue.getDomain();
                        if (Strings.isEmpty(domain)) {
                            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("domain");
                        }

                        post = new HttpPost(identityEndPoint);

                        // Create the "identity" object
                        JSONObject jUser = new JSONObject(4).put("name", userName).put("domain", new JSONObject(2).put("id", domain)).put("password", authValue.getValue());
                        JSONObject jIdentity = new JSONObject(4).put("methods", new JSONArray(1).put("password")).put("password", new JSONObject(2).put("user", jUser));

                        // Create the "scope" object
                        JSONObject jProject = new JSONObject(4).put("name", tenantName).put("domain", new JSONObject(2).put("id", "default"));
                        JSONObject jScope = new JSONObject(2).put("project", jProject);

                        jAuthData = new JSONObject(4).put("identity", jIdentity).put("scope", jScope);
                        break;
                    }
                    case RACKSPACE_API_KEY:
                        post = new HttpPost(Strings.isEmpty(identityEndPoint) ? "https://identity.api.rackspacecloud.com/v2.0/tokens" : identityEndPoint);
                        jAuthData = new JSONObject(2).put("RAX-KSKEY:apiKeyCredentials", new JSONObject(3).put("username", userName).put("apiKey", authValue.getValue()));
                        break;
                    default:
                        throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unsupported auth type: " + authValue.getType().getId());
                }
                JSONObject jRequestBody = new JSONObject(2).put("auth", jAuthData);
                post.setEntity(new StringEntity(jRequestBody.toString(), ContentType.APPLICATION_JSON));
            }

            response = httpClient.execute(post);

            StatusLine statusLine = response.getStatusLine();
            int status = statusLine.getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_CREATED == status) {
                JSONObject jResponse = new JSONObject(new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));
                return new TokenAndResponse(authValue.getType().getParser().parseTokenFrom(jResponse, response), jResponse);
            }

            String reasonPhrase = statusLine.getReasonPhrase();
            LOG.warn("Authentication failed with status code {} ({})", Integer.valueOf(status), null == reasonPhrase ? "<no-reason>" : reasonPhrase);
            throw SwiftExceptionCode.AUTH_FAILED.create();
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Utils.close(post, response);
        }
    }

}
