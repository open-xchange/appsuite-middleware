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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.messaging.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    private static final Set<String> WHITELIST = new HashSet<String>(Arrays.asList(
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
        "Resent-Bcc".toLowerCase(Locale.US)));

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
            if(entry.getKey().equalsIgnoreCase(KnownHeader.FROM.toString())) {
                return object;
            }
            addresses.put(object);
        }
        return addresses;
    }

    private MessagingAddressHeader toMessagingAddress(final MessagingHeader address) throws OXException {
        if(MessagingAddressHeader.class.isInstance(address)) {
            return (MessagingAddressHeader) address;
        }
        return MimeAddressMessagingHeader.valueOfRFC822(address.getName(), address.getValue());
    }

}
