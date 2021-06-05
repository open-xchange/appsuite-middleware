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

package com.openexchange.admin.rmi.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link URITools}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class URITools {

    private URITools() {
        super();
    }

    public static final URI changeHost(final URI uri, final String newHost) throws URISyntaxException {
        return new URI(uri.getScheme(), uri.getUserInfo(), newHost, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
    }

    public static final URI generateURI(final String protocol, final String host, final int port) throws URISyntaxException {
        return new URI(protocol, null, host, port, null, null, null);
    }

    public static final String getHost(final URI uri) {
        String retval = uri.getHost();
        if (null == retval || retval.length() == 0) {
            return retval;
        }
        if (retval.indexOf(':') > 0 && (retval.length() > 0 && retval.charAt(0) == '[') && retval.endsWith("]")) {
            retval = retval.substring(1, retval.length() -1);
        }
        return retval;
    }
}
