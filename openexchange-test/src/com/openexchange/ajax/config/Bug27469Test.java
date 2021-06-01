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

package com.openexchange.ajax.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * {@link Bug27469Test} Test setting of the default sender address to one of the available aliases
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class Bug27469Test extends AbstractAJAXSession {

    /**
     * Initializes a new {@link Bug27469Test}.
     * 
     * @param name
     */
    public Bug27469Test() {
        super();
    }

    @Test
    public void testSetAliases() throws Exception {
        GetRequest request = new GetRequest(Tree.MailAddresses);
        GetResponse response = getClient().execute(request);
        JSONArray allAddresses = (JSONArray) response.getData();
        int numberOfAddresses = allAddresses.length();
        assertTrue(numberOfAddresses > 1);
        for (int i = 0; i < numberOfAddresses; i++) {
            String newAddress = allAddresses.getString(i);
            setSendAddress(newAddress);
            String sendAddress = getSendAddress();
            assertEquals("The sendAddress wasn't updated", newAddress, sendAddress);
        }
    }

    private void setSendAddress(String newAddress) throws Exception {
        SetRequest setRequest = new SetRequest(Tree.SendAddress, newAddress);
        SetResponse setResponse = getClient().execute(setRequest);
        assertFalse(setResponse.hasError());
    }

    private String getSendAddress() throws Exception {
        GetRequest request = new GetRequest(Tree.SendAddress);
        GetResponse response = getClient().execute(request);
        String sendAddress = response.getString();
        return sendAddress;
    }
}
