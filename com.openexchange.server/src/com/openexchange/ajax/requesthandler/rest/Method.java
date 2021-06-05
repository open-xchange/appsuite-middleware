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

package com.openexchange.ajax.requesthandler.rest;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * An enumeration for HTTP methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Method {

    /**
     * Use <code>GET</code> requests to retrieve resource representation/information only - and not to modify it in any way
     * <p>
     * As GET requests do not change the state of the resource, these are said to be safe methods. Additionally, GET APIs should be
     * idempotent, which means that making multiple identical requests must produce the same result every time until another API
     * (<code>POST</code> or <code>PUT</code>) has changed the state of the resource on the server.
     */
    GET,
    /**
     * Use <code>PUT</code> primarily to update existing resource (if the resource does not exist then API may decide to create a new
     * resource or not). If a new resource has been created by the PUT API, the origin server MUST inform the user agent via the HTTP
     * response code 201 (Created) response and if an existing resource is modified, either the <code>200 (OK)</code> or
     * <code>204 (No Content)</code> response codes SHOULD be sent to indicate successful completion of the request.
     */
    PUT,
    /**
     * Use <code>POST</code> to create new subordinate resources, e.g. a file is subordinate to a directory containing it or a row is
     * subordinate to a database table. Talking strictly in terms of REST, POST methods are used to create a new resource into the
     * collection of resources.
     */
    POST,
    /**
     * As the name applies, <code>DELETE</code>s are used to delete resources (identified by the Request-URI).
     * <p>
     * A successful response of <code>DELETE</code> requests SHOULD be HTTP response code <code>200 (OK)</code> if the response includes an
     * entity describing the status, <code>202 (Accepted)</code> if the action has been queued, or 204 (No Content) if the action has been
     * performed but the response does not include an entity.
     * <p>
     * <code>DELETE</code> operations are idempotent. If you <code>DELETE</code> a resource, it is removed from the collection of resource.
     * Repeatedly calling <code>DELETE</code> on that resource will not change the outcome.
     */
    DELETE,
    /**
     * HTTP <code>PATCH</code> requests are to make partial update on a resource. If you see <code>PUT</code> requests also modify a
     * resource entity so to make more clear - <code>PATCH</code> method is the correct choice for partially updating an existing resource
     * and <code>PUT</code> should only be used if you are replacing a resource in its entirety.
     */
    PATCH;

    private static final Map<String, Method> MAP;

    static {
        final Method[] values = Method.values();
        final Map<String, Method> m = new HashMap<String, Method>(values.length);
        for (final Method method : values) {
            m.put(method.name(), method);
        }
        MAP = ImmutableMap.copyOf(m);
    }

    /**
     * Gets the method appropriate for specified HTTP Servlet request.
     *
     * @param req The HTTP Servlet request
     * @return The appropriate method or <code>null</code>
     */
    public static Method valueOf(final HttpServletRequest req) {
        String method = req.getMethod();
        return null == method ? null : MAP.get(Strings.toUpperCase(method));
    }
}
