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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;

/**
 * {@link AllAliasTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AllAliasTest extends ContactTest {

    /**
     * Initializes a new {@link AllAliasTest}.
     *
     * @param name
     */
    public AllAliasTest() {
        super();
    }


    @Test
    public void testAllAlias() throws Exception {
        AJAXClient client = getClient();
        final AllRequest allAliasRequest = new AllRequest(client.getValues().getPrivateContactFolder(), "all");
        final CommonAllResponse allAliasResponse = client.execute(allAliasRequest);
        final Object[][] aliasContacts = allAliasResponse.getArray();

        final AllRequest allRequest = new AllRequest(client.getValues().getPrivateContactFolder(), new int[] { 20, 1, 5, 2, 602 });
        final CommonAllResponse allResponse = client.execute(allRequest);
        final Object[][] contacts = allResponse.getArray();

        assertEquals("Arrays' sizes are not equal.", aliasContacts.length, contacts.length);
        for (int i = 0; i < aliasContacts.length; i++) {
            final Object[] o1 = aliasContacts[i];
            final Object[] o2 = contacts[i];
            assertEquals("Objects' sizes are not equal.", o1.length, o2.length);
            for (int j = 0; j < o1.length; j++) {
                if ((o1[j] != null || o2[j] != null)) {
                    if (!(o1[j] instanceof JSONArray) && !(o2[j] instanceof JSONArray)) {
                        assertEquals("Array[" + i + "][" + j + "] not equal.", o1[j], o2[j]);
                    } else {
                        compareArrays((JSONArray) o1[j], (JSONArray) o2[j]);
                    }
                }
            }
        }
    }

    private void compareArrays(final JSONArray o1, final JSONArray o2) throws Exception {
        if (o1.length() != o2.length()) {
            fail("Arrays' sizes are not equal.");
        }
        for (int i = 0; i < o1.length(); i++) {
            if ((o1.get(i) != null || o2.get(i) != null)) {
                if (!(o1.get(i) instanceof JSONArray) && !(o2.get(i) instanceof JSONArray)) {
                    assertEquals("Array[" + i + "] not equal.", o1.get(i).toString(), o2.get(i).toString());
                } else {
                    compareArrays((JSONArray) o1.get(i), (JSONArray) o2.get(i));
                }
            }
        }
    }

}
