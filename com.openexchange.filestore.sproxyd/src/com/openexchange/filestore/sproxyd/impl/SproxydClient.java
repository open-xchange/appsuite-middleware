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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
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

/**
 * {@link SproxydClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydClient.class);

    private final EndpointPool endpoints;
    private final HttpClient httpClient;
    private final String prefix;

    /**
     * Initializes a new {@link SproxydClient}.
     *
     * @param sproxydConfig The sproxyd config
     * @param prefix The prefix to use
     */
    public SproxydClient(SproxydConfig sproxydConfig, String prefix) {
        super();
        this.endpoints = sproxydConfig.getEndpointPool();
        this.httpClient = sproxydConfig.getHttpClient();
        this.prefix = prefix;
    }

    /**
     * Stores a new object.
     *
     * @param data The content to store
     * @param length The content length
     * @return The new identifier of the stored object
     */
    public UUID put(InputStream data, long length) throws OXException {
        UUID id = UUID.randomUUID();
        HttpResponse response = null;
        HttpPut request = null;
        Endpoint endpoint = getEndpoint();
        try {
            request = new HttpPut(endpoint.getObjectUrl(id));
            request.setEntity(new InputStreamEntity(data, length));
            response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_CREATED == status) {
                return id;
            }
            throw SproxydExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
        } catch (IOException e) {
            throw handleCommunicationError(endpoint, e);
        } finally {
            Utils.close(request, response);
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
        HttpGet get = null;
        HttpResponse response = null;
        Endpoint endpoint = getEndpoint();
        try {
            get = new HttpGet(endpoint.getObjectUrl(id));
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
            throw SproxydExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
        } catch (IOException e) {
            throw handleCommunicationError(endpoint, e);
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
        HttpDelete delete = null;
        HttpResponse response = null;
        Endpoint endpoint = getEndpoint();
        try {
            delete = new HttpDelete(endpoint.getObjectUrl(id));
            response = httpClient.execute(delete);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status) {
                return true;
            }
            if (HttpServletResponse.SC_NOT_FOUND == status) {
                return false;
            }
            throw SproxydExceptionCode.UNEXPECTED_ERROR.create(response.getStatusLine());
        } catch (IOException e) {
            throw handleCommunicationError(endpoint, e);
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
     * Gets an endpoint from the pool.
     *
     * @return the endpoint
     * @throws OXException If no endpoint is available (i.e. all are blacklisted due to connection timeouts).
     */
    private Endpoint getEndpoint() throws OXException {
        Endpoint endpoint = endpoints.get(prefix);
        if (endpoint == null) {
            throw SproxydExceptionCode.STORAGE_UNAVAILABLE.create();
        }
        return endpoint;
    }

    /**
     * Handles communication errors. If the endpoint is not available it is blacklisted.
     *
     * @param endpoint The endpoint for which the exception occurred.
     * @param e The exception
     * @return An OXException to re-throw
     */
    private OXException handleCommunicationError(Endpoint endpoint, IOException e) {
        if (Utils.endpointUnavailable(endpoint.getBaseUrl(), httpClient)) {
            LOG.warn("Sproxyd endpoint is unavailable: " + endpoint);
            endpoints.blacklist(endpoint.getBaseUrl());
        }

        return FileStorageCodes.IOERROR.create(e, e.getMessage());
    }

}
