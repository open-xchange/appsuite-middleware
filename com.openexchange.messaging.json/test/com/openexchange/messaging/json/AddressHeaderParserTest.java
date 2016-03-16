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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingHeader;


/**
 * {@link AddressHeaderParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddressHeaderParserTest extends TestCase {
    public void testResponsible() {
        final AddressHeaderParser parser = new AddressHeaderParser();

        final List<String> headerNames = Arrays.asList(
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

        for (final String headerName : headerNames) {
            assertTrue(parser.handles(headerName, null));
        }
    }

    public void testParseComplex() throws JSONException, OXException {
        final JSONObject object = new JSONObject("{address : 'clark.kent@dailyplanet.com', personal : 'Clark Kent'}");

        final AddressHeaderParser parser = new AddressHeaderParser();

        final HashMap<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        parser.parseAndAdd(headers, "From", object);

        assertTrue(headers.containsKey("From"));

        final Collection<MessagingHeader> parsed = headers.get("From");
        assertNotNull(parsed);

        assertEquals(1, parsed.size());

        final MessagingAddressHeader header = (MessagingAddressHeader) parsed.iterator().next();

        assertEquals("Clark Kent", header.getPersonal());
        assertEquals("clark.kent@dailyplanet.com", header.getAddress());
    }

    public void testParseSimple() throws OXException, JSONException {

        final AddressHeaderParser parser = new AddressHeaderParser();

        final HashMap<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        parser.parseAndAdd(headers, "From", "Clark Kent <clark.kent@dailyplanet.com>");

        assertTrue(headers.containsKey("From"));

        final Collection<MessagingHeader> parsed = headers.get("From");
        assertNotNull(parsed);

        assertEquals(1, parsed.size());

        final MessagingAddressHeader header = (MessagingAddressHeader) parsed.iterator().next();

        assertEquals("Clark Kent", header.getPersonal());
        assertEquals("clark.kent@dailyplanet.com", header.getAddress());
    }

    public void testParseList() throws OXException, JSONException {
        final JSONObject object = new JSONObject("{address : 'clark.kent@dailyplanet.com', personal : 'Clark Kent'}");

        final JSONArray array = new JSONArray("['Lois Lane <lois.lane@dailyplanet.com>']");
        array.put(object);

        final AddressHeaderParser parser = new AddressHeaderParser();

        final HashMap<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
        parser.parseAndAdd(headers, "From", array);

        assertTrue(headers.containsKey("From"));

        final Collection<MessagingHeader> parsed = headers.get("From");
        assertNotNull(parsed);

        assertEquals(2, parsed.size());

        final Iterator<MessagingHeader> iterator = parsed.iterator();
        MessagingAddressHeader header = (MessagingAddressHeader) iterator.next();

        assertEquals("Lois Lane", header.getPersonal());
        assertEquals("lois.lane@dailyplanet.com", header.getAddress());

        header = (MessagingAddressHeader) iterator.next();

        assertEquals("Clark Kent", header.getPersonal());
        assertEquals("clark.kent@dailyplanet.com", header.getAddress());

    }
}
