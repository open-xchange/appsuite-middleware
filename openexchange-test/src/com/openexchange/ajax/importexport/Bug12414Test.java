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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.tools.RandomString;

/**
 * Checks if truncation information is properly handled by importer.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12414Test extends AbstractAJAXSession {

    AJAXClient client;

    int folderId;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateContactFolder();
    }

    @Test
    public void testTruncation() throws OXException, IOException, JSONException {
        final VCardImportRequest request = new VCardImportRequest(folderId, new ByteArrayInputStream(vCard.getBytes(com.openexchange.java.Charsets.UTF_8)), false);
        final VCardImportResponse importR = client.execute(request);
        assertEquals("Missing import response.", 1, importR.size());
        final Response response = importR.get(0);
        assertFalse("No error occurs.", response.hasError());
    }

    public static final String vCard = "BEGIN:VCARD\n" + "VERSION:2.1\n" + "FN:" + RandomString.generateChars(321) + '\n' + "END:VCARD\n";
}
