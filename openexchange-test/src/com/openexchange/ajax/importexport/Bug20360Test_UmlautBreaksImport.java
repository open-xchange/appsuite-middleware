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
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

public class Bug20360Test_UmlautBreaksImport extends AbstractManagedContactTest {

    private final String vcard = "BEGIN:VCARD\n" + "VERSION:3.0\n" + "N;CHARSET=UTF-8:T\u00e4st;\u00dcser\n" + "FN;CHARSET=UTF-8:Str\u00e4to\n" + "EMAIL;TYPE=PREF,INTERNET:schneider@str\u00e4to.de\n" + "EMAIL:schneider@strato.de\n" + "END:VCARD\n";

    @Test
    public void testUmlaut() throws IOException, JSONException, OXException {
        VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vcard.getBytes("UTF-8")));
        VCardImportResponse importResponse = getClient().execute(importRequest);

        JSONArray data = (JSONArray) importResponse.getData();
        JSONObject jsonObject = data.getJSONObject(0);
        int objID = jsonObject.getInt("id");

        Contact actual = cotm.getAction(folderID, objID);

        assertTrue(actual.containsEmail1());
        assertTrue(actual.containsEmail2());
        assertEquals("schneider@str\u00e4to.de", actual.getEmail1());
        assertEquals("schneider@strato.de", actual.getEmail2());

    }

}
