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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.rest.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.java.Strings;

/**
 * A simple {@link Response} object for RESTful services.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Response {

    private Iterable<String> body;
    private int status = 200;
    private final Map<String, String> headers;
    private boolean disableCaching;

    /**
     * Initializes a new {@link Response}.
     */
    public Response() {
        super();
        headers = new HashMap<String, String>(8);
        disableCaching = true;
    }

    /**
     * The response body, just iterate and send it all.
     */
    public Iterable<String> getBody() {
        return body;
    }

    /**
     * Sets the response body.
     *
     * @param body The iterable body.
     */
    public void setBody(Iterable<String> body) {
        this.body = body;
    }

    /**
     * The status code
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status The status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Gets the response headers as an unmodifiable map.
     *
     * @return The response headers as an unmodifiable map
     */
    public Map<String, String> getHeaders() {
        return null == headers ? Collections.<String, String> emptyMap() : Collections.unmodifiableMap(headers);
    }

    /**
     * Gets the denoted header's value
     *
     * @return The header value or <code>null</code> if there is no such header
     */
    public String getHeader(String name) {
        return Strings.isEmpty(name) ? null : headers.get(name);
    }

    /**
     * Sets the response header.
     *
     * @param name The header name
     * @param value The header value
     */
    public void setHeader(String name, String value) {
        if (Strings.isEmpty(name)) {
            return;
        }
        headers.put(name, Strings.isEmpty(value) ? "" : value);
    }

    /**
     * Sets the response's <i>Content-Type</i> header;<br>
     * e.g. <code>application/json; charset=UTF-8</code>
     *
     * @param contentType The <i>Content-Type</i> value
     */
    public void setContentType(String contentType) {
        if (Strings.isEmpty(contentType)) {
            return;
        }
        headers.put("Content-Type", contentType);
    }

    /**
     * Sets the response's <i>Content-Disposition</i> header;<br>
     * e.g. <code>attachment; filename="readme.txt"</code>
     *
     * @param contentDisposition The <i>Content-Disposition</i> value
     */
    public void setContentDisposition(String contentDisposition) {
        if (Strings.isEmpty(contentDisposition)) {
            return;
        }
        headers.put("Content-Disposition", contentDisposition);
    }

    /**
     * Sets the response headers.
     *
     * @param headers The response headers
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    /**
     * Sets if the server response <i>shall not</i> be cached by a Proxy or a Browser; default is <code>true</code>.
     *
     * @param disableCaching The flag to set
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }

    /**
     * Checks if the server response <i>shall not</i> be cached by a Proxy or a Browser; default is <code>true</code>.
     * <p>
     * The HTTP response headers "Pragma", "Cache-Control" and "Expiry" are supposed to be modified appropriately.
     *
     * @return <code>true</code> to disable caching; otherwise <code>false</code>
     */
    public boolean isDisableCaching() {
        return disableCaching;
    }

}
