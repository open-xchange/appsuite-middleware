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

package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.importexport.Format;

/**
 * Tests the ICAL imports and exports by using the servlets.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ICalImportExportServletTest extends AbstractImportExportServletTest {

    @Test
    public void testIcalMessage() throws Exception {
        final InputStream is = new ByteArrayInputStream("BEGIN:VCALENDAR".getBytes());
        final WebConversation webconv = getClient().getSession().getConversation();
        final Format format = Format.ICAL;
        final int folderId = createFolder("ical-empty-file-" + UUID.randomUUID().toString(), FolderObject.CONTACT);
        try {
            final WebRequest req = new PostMethodWebRequest(getCSVColumnUrl(IMPORT_SERVLET, folderId, format), true);
            req.selectFile("file", "empty.ics", is, format.getMimeType());
            final WebResponse webRes = webconv.getResource(req);
            final JSONObject response = extractFromCallback(webRes.getText());
            Assert.assertEquals("Must contain error ", "I_E-1100", response.optString("code"));
        } finally {
            removeFolder(folderId);
        }
    }

}
