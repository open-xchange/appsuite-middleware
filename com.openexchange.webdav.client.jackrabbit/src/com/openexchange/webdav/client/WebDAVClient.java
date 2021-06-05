/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.webdav.client;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.webdav.client.functions.ErrorAwareFunction;

/**
 * {@link WebDAVClient}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public interface WebDAVClient {

    /** Constant for <i>depth</i> in the {@link #propFind} method to retrieve properties of the targeted resource only */
    static final int DEPTH_0 = 0;

    /** Constant for <i>depth</i> in the {@link #propFind} method to retrieve properties of the targeted resource only and all child resources */
    static final int DEPTH_1 = 1;

    /** Constant for <i>depth</i> in the {@link #propFind} method to retrieve properties of the targeted resource only and all child resources, recursively */
    static final int DEPTH_INFINITY = Integer.MAX_VALUE;

    /**
     * Gets the report from the given url
     *
     * @param <T> The result type
     * @param href The url to the
     * @param body The body
     * @param handler The response handler
     * @return The parsed result
     * @throws WebDAVClientException
     */
    <T> T report(String href, WebDAVXmlBody body, ErrorAwareFunction<Document, T> handler, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Performs a WebDAV search, if supported by the WebDAV server
     *
     * @see "Web Distributed Authoring and Versioning (WebDAV) SEARCH" - rfc5323
     *
     * @param <T> The  result type
     * @param href The url
     * @param body The request body containing the search definition
     * @param handler The response handler
     * @return The parsed result
     * @throws WebDAVClientException
     */
    <T> T search(String href, WebDAVXmlBody body, ErrorAwareFunction<Document, T> handler, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Issues a <code>PROPFIND</code> request at a WebDAV resource and makes the successfully looked up resource properties of the
     * resulting multistatus response available via corresponding WebDAV resources.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param depth The <i>depth</i> to request; one of {@link #DEPTH_0}, {@link #DEPTH_1} or {@link #DEPTH_INFINITY}
     * @param props The names of the properties to request
     * @param headers Additional headers to use for the request
     * @return The successfully looked up resource properties of the resulting multistatus response as WebDAV resources
     */
    List<WebDAVResource> propFind(String href, int depth, Set<QName> props, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Deletes an existing WebDAV resource.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param headers Additional headers to use for the request
     */
    void delete(String href, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Gets a value indicating whether a specific WebDAV address exists or not, meking use of an appropriate <code>HEAD</code> request.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param headers Additional headers to use for the request
     * @return <code>true</code> if the resource exists, <code>false</code>, otherwise
     */
    boolean exists(String href, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Gets the contents of a WebDAV resource.
     *
     * @param href The href address of the collection to create
     * @param headers Additional headers to use for the request
     * @return The resource body
     */
    InputStream get(String href, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Creates a new collection resource using the <code>MKCOL</code> request method.
     *
     * @param href The href address of the collection to create
     * @param headers Additional headers to use for the request
     */
    void mkCol(String href, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Moves a WebDAV resource to a specific destination.
     * <p/>
     * Note that an existing resource at the destination will be overridden unless the <code>Overwrite</code> header is not set to
     * <code>F</code> explicitly.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param destinationHref The destination href address to move the resource to
     * @param headers Additional headers to use for the request
     */
    void move(String href, String destinationHref, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Copies a WebDAV resource to a specific destination.
     * <p/>
     * Note that an existing resource at the destination will be overridden unless the <code>Overwrite</code> header is not set to
     * <code>F</code> explicitly.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param destinationHref The destination href address to copy the resource to
     * @param headers Additional headers to use for the request
     */
    void copy(String href, String destinationHref, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Issues a <code>PROPPATCH</code> request against a WebDAV resource.
     * <p/>
     * The values of the properties to set can be supplied as {@link String}, {@link Date} or {@link Element} for complex property values.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param propsToSet A map holding the properties to set
     * @param propsToRemove The names of the properties to remove
     * @param headers Additional headers to use for the request
     */
    void propPatch(String href, Map<QName, Object> propsToSet, Set<QName> propsToRemove, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Performs a <code>PUT</code> request to create or update the body of a WebDAV resource.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param content The content to upload
     * @param contentType The content type, or <code>null</code> if unknown
     * @param contentLength The content length, or <code>-1</code> if unknown
     * @param headers Additional headers to use for the request
     */
    void put(String href, InputStream content, String contentType, long contentLength, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Locks a WebDAV resource using the <code>LOCK</code> request method.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param timeout The time until the lock is requested to expire, or <code>-1</code> for an infinite timeout
     * @param headers Additional headers to use for the request
     * @return The lock token identifying the lock
     */
    String lock(String href, long timeout, Map<String, String> headers) throws WebDAVClientException;

    /**
     * Unlocks a previously locked WebDAV resource using the <code>UNLOCK</code> request method.
     *
     * @param href The href address of the targeted WebDAV resource
     * @param lockToken The token identifying the lock to release
     * @param headers Additional headers to use for the request
     */
    void unlock(String href, String lockToken, Map<String, String> headers) throws WebDAVClientException;

}
