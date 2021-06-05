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

package com.openexchange.subscribe.dav;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * {@link HttpEntityMethod} - A generic HTTP method with a body.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class HttpEntityMethod extends HttpEntityEnclosingRequestBase {

    private final String methodName;

    /**
     * Initializes a new HTTP method with a body.
     *
     * @param methodName The method name
     * @param uri The URI to use
     */
    public HttpEntityMethod(String methodName, String uri) {
        super();
        this.methodName = methodName;
        setURI(URI.create(uri));
    }

    /**
     * Initializes a new HTTP method with a body.
     *
     * @param methodName The method name
     * @param uri The URI to use
     */
    public HttpEntityMethod(String methodName, URI uri) {
        super();
        this.methodName = methodName;
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return methodName;
    }

}
