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
import org.junit.Test;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.groupware.search.Order;

/**
 * {@link AllAliasTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AllAliasTest extends AbstractMailTest {

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
        final AllRequest allAliasRequest = new AllRequest(getClient().getValues().getInboxFolder(), "all", 0, Order.ASCENDING, true);
        final AllResponse allAliasResponse = getClient().execute(allAliasRequest);
        final Object[][] aliasMails = allAliasResponse.getArray();

        final AllRequest allRequest = new AllRequest(getClient().getValues().getInboxFolder(), new int[] { 600, 601 }, 0, Order.ASCENDING, true);
        final AllResponse allResponse = getClient().execute(allRequest);
        final Object[][] mails = allResponse.getArray();

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
