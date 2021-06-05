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

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.mail.actions.ListRequest;

/**
 * {@link ListAliasTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ListAliasTest extends AbstractMailTest {

    /**
     * Initializes a new {@link ListAliasTest}.
     *
     * @param name
     */
    public ListAliasTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testListAlias() throws Exception {
        final String[][] folderAndIds = getFolderAndIDs(getClient().getValues().getInboxFolder());

        final ListRequest aliasRequest = new ListRequest(folderAndIds, "list");
        final CommonListResponse aliasResponse = getClient().execute(aliasRequest);
        final Object[][] aliasMails = aliasResponse.getArray();

        final ListRequest request = new ListRequest(folderAndIds, new int[] { 600, 601, 614, 602, 611, 603, 612, 607, 652, 610, 608, 102 });
        final CommonListResponse response = getClient().execute(request);
        final Object[][] mails = response.getArray();

        assertEquals("Arrays' sizes are not equal.", aliasMails.length, mails.length);
        for (int i = 0; i < aliasMails.length; i++) {
            final Object[] o1 = aliasMails[i];
            final Object[] o2 = mails[i];
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
