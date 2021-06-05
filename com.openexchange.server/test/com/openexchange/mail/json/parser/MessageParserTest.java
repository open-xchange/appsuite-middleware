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

package com.openexchange.mail.json.parser;

import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.junit.Assert;


/**
 * {@link MessageParserTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessageParserTest {

    /**
     * Initializes a new {@link MessageParserTest}.
     */
    public MessageParserTest() {
        super();
    }

     @Test
     public void testForBug35683() {
        try {
            JSONObject jMail = new JSONObject().put("from", new JSONArray().put(new JSONArray().put("Jane Doe").put("jane@barfoo.de")));

            InternetAddress[] addresses = MessageParser.parseAddressKey("from", jMail, true);

            Assert.assertNotNull(addresses);
            Assert.assertEquals("Unexpected length", 1, addresses.length);
            Assert.assertEquals("Unexpected address representation", "Jane Doe <jane@barfoo.de>", addresses[0].toString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
