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

package com.openexchange.filestore.sproxyd.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.sproxyd.SproxydExceptionCode;
import com.openexchange.java.util.UUIDs;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.server.ServiceLookup;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.function.CheckedConsumer;

/**
 * {@link SproxydClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydClient {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydClient.class);

    /** The HTTP status code for a locked storage object */
    private static final int SC_LOCKED = 423;

    private final ServiceLookup services;
    private final EndpointPool endpointPool;
    private final String prefix;
    private final String filestoreID;


    /**
     * Initializes a new {@link SproxydClient}.
     *
     * @param services The service lookup
     * @param endpointPool The Endpoint pool
     * @param prefix The prefix to use
     * @param filestoreID The filestore ID
     */
    public SproxydClient(ServiceLookup services, EndpointPool endpointPool, String prefix, String filestoreID) {
        super();
        this.services = services;
        this.endpointPool = endpointPool;
        this.prefix = prefix;
        this.filestoreID = filestoreID;
    }

    /**
     * Stores a new object.
     *
     * @param data The content to store
     * @param length The content length
     * @return The new identifier of the stored object
     * @throws OXException If storing a new object fails
     */
    public UUID put(InputStream data, long length) throws OXException {
        Callable<UUID> putDataCallable = new Callable<UUID>() {

            @Override
            public UUID call() throws Exception {
                UUID id = UUID.randomUUID();
                HttpResponse response = null;
                HttpPut request = null;
                Endpoint endpoint = getEndpoint();
                try {
                    request = new HttpPut(endpoint.getObjectUrl(id));
                    request.setEntity(new InputStreamEntity(data, length));
                    response = getHttpClient().execute(request);
                    int status = response.getStatusLine().getStatusCode();
                    if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_CREATED == status) {
                        return id;
                    }
                    if (SC_LOCKED == status) {
                        throw SproxydExceptionCode.LOCKED.create(UUIDs.getUnformattedString(id), response.getStatusLine());
                    }
                    throw SproxydExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
                } catch (IOException e) {
                    throw handleCommunicationError(endpoint, e);
                } finally {
                    Utils.close(request, response);
                }
            }
        };

        return executeWithRetryOnLockedStatusCode(putDataCallable);
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
     * @throws OXException If stored object's input stream cannot be returned
     */
    public InputStream get(UUID id, long rangeStart, long rangeEnd) throws OXException {
        Callable<InputStream> getDataCallable = new Callable<InputStream>() {

            @Override
            public InputStream call() throws Exception {
                HttpGet get = null;
                HttpResponse response = null;
                Endpoint endpoint = getEndpoint();
                try {
                    get = new HttpGet(endpoint.getObjectUrl(id));
                    if (0 < rangeStart || 0 < rangeEnd) {
                        get.addHeader("Range", "bytes=" + rangeStart + "-" + rangeEnd);
                    }
                    response = getHttpClient().execute(get);
                    int status = response.getStatusLine().getStatusCode();
                    if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_PARTIAL_CONTENT == status) {
                        InputStream content = HttpClients.createHttpResponseStreamFor(response);
                        response = null;
                        return content;
                    }
                    if (HttpServletResponse.SC_NOT_FOUND == status) {
                        throw FileStorageCodes.FILE_NOT_FOUND.create(UUIDs.getUnformattedString(id));
                    }
                    if (SC_LOCKED == status) {
                        throw SproxydExceptionCode.LOCKED.create(UUIDs.getUnformattedString(id), response.getStatusLine());
                    }
                    throw SproxydExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
                } catch (IOException e) {
                    throw handleCommunicationError(endpoint, e);
                } finally {
                    Utils.close(get, response);
                }
            }
        };

        return executeWithRetryOnLockedStatusCode(getDataCallable);
    }

    /**
     * Deletes a stored object.
     *
     * @param id The identifier of the object to delete
     * @return <code>true</code> if the object was deleted successfully, <code>false</code> if it was not found
     * @throws OXException If deletion fails
     */
    public boolean delete(UUID id) throws OXException {
        Callable<Boolean> deleteDataCallable = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                HttpDelete delete = null;
                HttpResponse response = null;
                Endpoint endpoint = getEndpoint();
                try {
                    delete = new HttpDelete(endpoint.getObjectUrl(id));
                    response = getHttpClient().execute(delete);
                    int status = response.getStatusLine().getStatusCode();
                    if (HttpServletResponse.SC_OK == status) {
                        return Boolean.TRUE;
                    }
                    if (HttpServletResponse.SC_NOT_FOUND == status) {
                        return Boolean.FALSE;
                    }
                    if (SC_LOCKED == status) {
                        throw SproxydExceptionCode.LOCKED.create(UUIDs.getUnformattedString(id), response.getStatusLine());
                    }
                    throw SproxydExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
                } catch (IOException e) {
                    throw handleCommunicationError(endpoint, e);
                } finally {
                    Utils.close(delete, response);
                }
            }
        };

        return executeWithRetryOnLockedStatusCode(deleteDataCallable).booleanValue();
    }

    /**
     * Deletes multiple stored objects.
     *
     * @param ids The identifier of the objects to delete
     */
    public void delete(Collection<UUID> ids) throws OXException {
        if (ids != null) {
            for (UUID id : ids) {
                delete(id);
            }
        }
    }

    /**
     * Gets an end-point from the pool.
     *
     * @return the end-point
     * @throws OXException If no end-point is available (i.e. all are blacklisted due to connection timeouts).
     */
    Endpoint getEndpoint() throws OXException {
        Endpoint endpoint = endpointPool.get(prefix);
        if (endpoint == null) {
            throw SproxydExceptionCode.STORAGE_UNAVAILABLE.create();
        }
        return endpoint;
    }

    /**
     * Handles communication errors. If the end-point is not available it is blacklisted.
     *
     * @param endpoint The end-point for which the exception occurred.
     * @param e The exception
     * @return An OXException to re-throw
     * @throws OXException
     */
    OXException handleCommunicationError(Endpoint endpoint, IOException e) throws OXException {
        if (org.apache.http.conn.ConnectionPoolTimeoutException.class.isInstance(e)) {
            // Waiting for an available connection in HttpClient pool expired.
            // Avoid additional stress for that pool by preventing unavailable check, which might in turn wait for a lease.
            return FileStorageCodes.IOERROR.create(e, e.getMessage());
        }

        if (Utils.endpointUnavailable(endpoint.getBaseUrl(), getHttpClient())) {
            LOG.warn("Sproxyd endpoint is unavailable: {}", endpoint);
            endpointPool.blacklist(endpoint.getBaseUrl());
        }

        return FileStorageCodes.IOERROR.create(e, e.getMessage());
    }

    /**
     * Gets the HTTP client to use to communicate with Sproxyd end-point.
     *
     * @return The HTTP client
     * @throws OXException If HTTP client cannot be returned
     */
    HttpClient getHttpClient() throws OXException {
        return services.getServiceSafe(HttpClientService.class).getHttpClient(filestoreID);
    }

    // ----------------------------------------------- Failsafe retry stuff ----------------------------------------------------------------

    /**
     * Executes given callable with retry behavior on '<code>HTTP/1.1 423 Locked</code>' response.
     *
     * @param <V> The type of the return value
     * @param callable The callable to execute
     * @return The return value
     * @throws OXException If a non-retriable exception occurs or retry attempts are expired
     */
    private static <V> V executeWithRetryOnLockedStatusCode(Callable<V> callable) throws OXException {
        try {
            return Failsafe.with(RETRY_POLICY_LOCKED).onRetry(ON_RETRY_LISTENER_LOCKED).get(callable);
        } catch (FailsafeException e) {
            // Checked exception occurred
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }

            Throwable t = cause == null ? e : cause;
            throw SproxydExceptionCode.UNEXPECTED_ERROR.create(t, t.getMessage());
        } catch (RuntimeException e) {
            // Unchecked exception occurred
            throw SproxydExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final RetryPolicy RETRY_POLICY_LOCKED = new RetryPolicy()
        .withMaxRetries(3).withBackoff(1, 10, TimeUnit.SECONDS).withJitter(0.25f)
        .retryOn(t -> OXException.class.isInstance(t) && SproxydExceptionCode.LOCKED.equals((OXException) t));

    private static final CheckedConsumer<Throwable> ON_RETRY_LISTENER_LOCKED = new CheckedConsumer<Throwable>() {

        @Override
        public void accept(Throwable t) throws Exception {
            LOG.debug("Storage object is locked. Most likely because it is concurrently write-wise accessed.", t);
        }
    };

}
