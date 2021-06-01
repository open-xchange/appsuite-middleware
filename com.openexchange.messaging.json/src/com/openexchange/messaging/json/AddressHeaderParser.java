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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;


/**
 * {@link AddressHeaderParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddressHeaderParser implements MessagingHeaderParser {

    private static final Set<String> WHITELIST = ImmutableSet.of(
        "From",
        "To",
        "Cc",
        "Bcc",
        "Reply-To",
        "Resent-Reply-To",
        "Disposition-Notification-To",
        "Resent-From",
        "Sender",
        "Resent-Sender",
        "Resent-To",
        "Resent-Cc",
        "Resent-Bcc");

    @Override
    public int getRanking() {
        return 1;
    }

    @Override
    public boolean handles(final String key, final Object value) {
        return WHITELIST.contains(key);
    }

    @Override
    public void parseAndAdd(final Map<String, Collection<MessagingHeader>> headers, final String key, final Object value) throws JSONException, OXException {
        final ArrayList<MessagingHeader> list = new ArrayList<MessagingHeader>();

        if (JSONArray.class.isInstance(value)) {
            final JSONArray arr = (JSONArray) value;
            for(int i = 0, size = arr.length(); i < size; i++) {
                parse(key, arr.get(i), list);
            }
        } else {
            parse(key, value, list);
        }

        headers.put(key, list);
    }

    private void parse(final String key, final Object value, final ArrayList<MessagingHeader> list) throws OXException {
        if (JSONObject.class.isInstance(value)) {
            list.add(parseObject(key, (JSONObject) value));
        } else if (String.class.isInstance(value)) {
            list.add(parseString(key, (String) value));
        }
    }

    private MessagingHeader parseString(final String key, final String value) throws OXException {
        return MimeAddressMessagingHeader.parseRFC822(key, value).iterator().next();
    }

    private MessagingHeader parseObject(final String key, final JSONObject value) {
        final String address = value.optString("address");
        final String personal = value.optString("personal");
        return MimeAddressMessagingHeader.valueOfPlain(key, personal, address);
    }

}
