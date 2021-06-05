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

package com.openexchange.tools.servlet.http;

import javax.servlet.http.Cookie;

/**
 * Internal data object for transferring cookies from requests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.8.0
 */
public final class AuthCookie implements com.openexchange.authentication.Cookie {

    private final String name, value;

    public AuthCookie(final Cookie cookie) {
        super();
        this.name = cookie.getName();
        this.value = cookie.getValue();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32).append('[');
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        if (value != null) {
            builder.append("value=").append(value);
        }
        builder.append(']');
        return builder.toString();
    }

}
