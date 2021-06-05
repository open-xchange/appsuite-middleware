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

package com.openexchange.icap;

/**
 * {@link ICAPMethod} - Defines the allowed request ICAP methods as described in
 * <a href="https://tools.ietf.org/html/rfc3507#section-4.3.2">RFC-3507, Section 4.3.2</a>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum ICAPMethod {

    /**
     * <p>The Request Modification method.</p>
     * <p>The ICAP client sends an HTTP request to an ICAP server. The ICAP server
     * returns a modified version of the request, an HTTP response, or (if the client
     * indicates it supports 204 responses) an indication that no modification is
     * required.</p>
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.8">RFC-3507, Section 4.8</a>
     */
    REQMOD,
    /**
     * <p>The Response Modification method.</p>
     * <p>The ICAP client sends an origin server's HTTP response to an ICAP server, and
     * (if available) the original client request that caused that response. Similar to
     * {@link #REQMOD}, the response from the ICAP server can be an adapted HTTP
     * response, an error, or a 204 response code indicating that no adaptation is required.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.9">RFC-3507, Section 4.9</a>
     */
    RESPMOD,
    /**
     * <p>The Options method.</p>
     * <p>The ICAP "OPTIONS" method is used by the ICAP client to retrieve
     * configuration information from the ICAP server. In this method, the
     * ICAP client sends a request addressed to a specific ICAP resource and
     * receives back a response with options that are specific to the
     * service named by the URI. All OPTIONS requests MAY also return
     * options that are global to the server (i.e., apply to all services).</p>
     * 
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.10">RFC-3507, Section 4.10</a>
     */
    OPTIONS;
}
