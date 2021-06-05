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

package com.openexchange.messaging.generic.internet;

import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.generic.Utility;

/**
 * {@link MimeStringMessagingHeader} - A string header whose value is possibly RFC 2047 style encoded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeStringMessagingHeader implements MessagingHeader {

    private final String name;

    private final String value;

    /**
     * Initializes a new {@link MimeStringMessagingHeader}.
     *
     * @param name The header name
     * @param value The possibly RFC 2047 style encoded header value
     */
    public MimeStringMessagingHeader(final String name, final String value) {
        super();
        this.name = name;
        this.value = Utility.decodeMultiEncodedHeader(value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.PLAIN;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append(name).append('=').append(value).toString();
    }

}
