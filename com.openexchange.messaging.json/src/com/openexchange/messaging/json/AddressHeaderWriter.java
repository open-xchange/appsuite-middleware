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

import java.util.Collection;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingHeader.KnownHeader;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AddressHeaderWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddressHeaderWriter implements MessagingHeaderWriter {

    private static final Set<String> WHITELIST = ImmutableSet.of(
        "From".toLowerCase(Locale.US),
        "To".toLowerCase(Locale.US),
        "Cc".toLowerCase(Locale.US),
        "Bcc".toLowerCase(Locale.US),
        "Reply-To".toLowerCase(Locale.US),
        "Resent-Reply-To".toLowerCase(Locale.US),
        "Disposition-Notification-To".toLowerCase(Locale.US),
        "Resent-From".toLowerCase(Locale.US),
        "Sender".toLowerCase(Locale.US),
        "Resent-Sender".toLowerCase(Locale.US),
        "Resent-To".toLowerCase(Locale.US),
        "Resent-Cc".toLowerCase(Locale.US),
        "Resent-Bcc".toLowerCase(Locale.US));

    @Override
    public int getRanking() {
        return 1;
    }

    @Override
    public boolean handles(final Entry<String, Collection<MessagingHeader>> entry) {
        final String name = entry.getKey();
        if (null == name) {
            return false;
        }
        return WHITELIST.contains(name.toLowerCase(Locale.US));
    }

    @Override
    public String writeKey(final Entry<String, Collection<MessagingHeader>> entry) throws JSONException, OXException {
        return entry.getKey();
    }

    @Override
    public Object writeValue(final Entry<String, Collection<MessagingHeader>> entry, final ServerSession session) throws JSONException, OXException {
        final JSONArray addresses = new JSONArray();
        for (final MessagingHeader address : entry.getValue()) {
            final JSONObject object = new JSONObject();
            final MessagingAddressHeader addr = toMessagingAddress(address);
            object.put("personal", addr.getPersonal());
            object.put("address", addr.getAddress());
            if (entry.getKey().equalsIgnoreCase(KnownHeader.FROM.toString())) {
                return object;
            }
            addresses.put(object);
        }
        return addresses;
    }

    private MessagingAddressHeader toMessagingAddress(final MessagingHeader address) throws OXException {
        if (MessagingAddressHeader.class.isInstance(address)) {
            return (MessagingAddressHeader) address;
        }
        return MimeAddressMessagingHeader.valueOfRFC822(address.getName(), address.getValue());
    }

}
