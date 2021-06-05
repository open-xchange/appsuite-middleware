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

package com.openexchange.messaging.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.StringMessageHeader;

public class InvertedHeaderParser implements MessagingHeaderParser {

    @Override
    public boolean handles(final String key, final Object value) {
        return true;
    }

    @Override
    public void parseAndAdd(final Map<String, Collection<MessagingHeader>> headers, final String key, final Object value) {
        final StringMessageHeader header = new StringMessageHeader(key, new StringBuilder((String)value).reverse().toString());
        headers.put(key, Arrays.asList((MessagingHeader)header));
    }

    @Override
    public int getRanking() {
        return 2;
    }

}
