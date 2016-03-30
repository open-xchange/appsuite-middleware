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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.junit.Test;
import com.openexchange.dav.caldav.CalDAVTest;

/**
 * {@link Bug30359Test}
 *
 * Local file inclusion/path traversal via WebDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug30359Test extends CalDAVTest {

	@Test
    public void testExternalEntities() throws Exception {
        String uri = getBaseUri() + "/caldav/" + getDefaultFolderID() + "/";
        EntityEnclosingMethod m = new EntityEnclosingMethod(uri) {

            @Override
            public String getName() {
                return "REPORT";
            }
        };

        String secret = "my-password-is-" + UUID.randomUUID().toString();
        File file = File.createTempFile("Bug30359Test", ".txt");
        file.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(secret);
        writer.close();
        String path = file.toURI().toURL().toExternalForm();

        final String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE C:calendar-multiget [<!ENTITY foo SYSTEM \"" + path +
            "\">]><C:calendar-multiget xmlns:D=\"DAV:\" xmlns:C=\"urn:ietf:params:xml:ns:caldav\"><D:prop><D:getetag/><C:calendar-d" +
            "ata/></D:prop><D:href>/caldav/" + getDefaultFolderID() + "/&foo;</D:href></C:calendar-multiget>";
        m.setRequestEntity(new RequestEntity() {

            @Override
            public void writeRequest(OutputStream arg0) throws IOException {
                arg0.write(body.getBytes("UTF-8"));
            }

            @Override
            public boolean isRepeatable() {
                return true;
            }

            @Override
            public String getContentType() {
                return "text/xml; charst=\"utf-8\"";
            }

            @Override
            public long getContentLength() {
                return body.length();
            }
        });

        int status = getWebDAVClient().executeMethod(m);
        assertEquals(207, status);
        String response = m.getResponseBodyAsString();
        assertNotNull(response);
        assertFalse(response, response.contains(secret));
    }

}
