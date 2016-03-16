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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.generic.internet.MimeContentType;


/**
 * {@link ContentDispositionParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContentDispositionParserTest extends TestCase {
    public void testParseComplex() throws JSONException, OXException {
        final ContentDispositionParser parser = new ContentDispositionParser();

        assertTrue(parser.handles("content-disposition", null));

        final JSONObject jsonCDisp = new JSONObject();
        jsonCDisp.put("type", "attachment");

        final JSONObject params = new JSONObject();
        params.put("filename", "foo.dat");

        jsonCDisp.put("params", params);

        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

        parser.parseAndAdd(headers, "content-disposition", jsonCDisp);

        assertTrue(!headers.isEmpty());

        final Collection<MessagingHeader> collection = headers.get(MimeContentType.getContentTypeName());
        assertNotNull(collection);
        assertEquals(1, collection.size());

        final ContentDisposition cDisp = (ContentDisposition) collection.iterator().next();

        assertEquals("attachment", cDisp.getDisposition());

        assertEquals("foo.dat", cDisp.getFilenameParameter());
    }


    public void testParseBasic() throws OXException, JSONException {
        final ContentDispositionParser parser = new ContentDispositionParser();

        final String stringCDisp = "attachment;filename=foo.dat";

        final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

        parser.parseAndAdd(headers, "content-disposition", stringCDisp);

        assertTrue(!headers.isEmpty());

        final Collection<MessagingHeader> collection = headers.get(MimeContentType.getContentTypeName());
        assertNotNull(collection);
        assertEquals(1, collection.size());

        final ContentDisposition cDisp = (ContentDisposition) collection.iterator().next();

        assertEquals("attachment", cDisp.getDisposition());

        assertEquals("foo.dat", cDisp.getFilenameParameter());

    }


}
