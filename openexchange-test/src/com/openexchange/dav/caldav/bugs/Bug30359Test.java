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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.junit.Test;
import com.openexchange.dav.Config;
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
        String uri = getBaseUri() + Config.getPathPrefix() + "/caldav/" + encodeFolderID(getDefaultFolderID()) + "/";
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

        final String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE C:calendar-multiget [<!ENTITY foo SYSTEM \"" + path + "\">]><C:calendar-multiget xmlns:D=\"DAV:\" xmlns:C=\"urn:ietf:params:xml:ns:caldav\"><D:prop><D:getetag/><C:calendar-d" + "ata/></D:prop><D:href>/caldav/" + getDefaultFolderID() + "/&foo;</D:href></C:calendar-multiget>";
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
