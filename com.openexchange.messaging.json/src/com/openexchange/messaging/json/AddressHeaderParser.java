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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;


/**
 * {@link AddressHeaderParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddressHeaderParser implements MessagingHeaderParser {

    private static final Set<String> WHITELIST = new HashSet<String>(Arrays.asList(
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
        "Resent-Bcc"));

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

        if(JSONArray.class.isInstance(value)) {
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
        if(JSONObject.class.isInstance(value)) {
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
