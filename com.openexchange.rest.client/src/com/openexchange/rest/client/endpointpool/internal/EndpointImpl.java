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

package com.openexchange.rest.client.endpointpool.internal;

import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.endpointpool.Endpoint;
import com.openexchange.rest.client.endpointpool.Path;


/**
 * {@link EndpointImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class EndpointImpl implements Endpoint {

    private final URI baseUri;
    private final int hash;

    /**
     * Initializes a new {@link EndpointImpl}.
     *
     * @param uri The base URI; e.g. <code>"https://my.service.invalid/v1/service"</code>
     * @throws IllegalArgumentException If specified URI is empty or illegal
     */
    public EndpointImpl(String uri) {
        super();
        if (Strings.isEmpty(uri)) {
            throw new IllegalArgumentException("uri must not be empty");
        }
        try {
            this.baseUri = new URI(uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("uri is illegal", e);
        }

        hash = 31 * 1 + ((baseUri == null) ? 0 : baseUri.hashCode());
    }

    /**
     * Initializes a new {@link EndpointImpl}.
     *
     * @param uri The base URI; e.g. <code>"https://my.service.invalid/v1/service"</code>
     * @throws IllegalArgumentException If specified URI is empty or illegal
     */
    public EndpointImpl(URI uri) {
        super();
        if (null == uri) {
            throw new IllegalArgumentException("uri must not be empty");
        }
        try {
            String sUri = uri.toString();
            this.baseUri = sUri.endsWith("/") ? new URI(sUri.substring(0, sUri.length() - 1)) : uri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("uri is illegal", e);
        }

        hash = 31 * 1 + ((baseUri == null) ? 0 : baseUri.hashCode());
    }

    @Override
    public String getBaseUri() {
        return baseUri.toString();
    }

    @Override
    public String getConcatenatedUri(Path path) {
        StringBuilder sb = new StringBuilder(baseUri.toString());
        for (String segment : path) {
            sb.append('/').append(segment);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EndpointImpl)) {
            return false;
        }
        EndpointImpl other = (EndpointImpl) obj;
        if (baseUri == null) {
            if (other.baseUri != null) {
                return false;
            }
        } else if (!baseUri.equals(other.baseUri)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getBaseUri();
    }

}
