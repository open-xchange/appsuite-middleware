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

package com.openexchange.ajax.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.java.Strings;

/**
 * {@link RedeemTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RedeemTest extends AbstractLoginTest {

    public RedeemTest() {
        super();
    }

    @Test
    public void testRedeemRandom() throws Exception {
        createClient();
        String[] credentials = credentials(USER1);

        inModule("login");

        raw("login", "name", credentials[0], "password", credentials[1]);

        String random = rawResponse.optString("random");
        /*
         * RandomToken is disabled by default via US: 52869957 so only try to login if the randomToken is enabled on the server side
         */
        if (Strings.isNotEmpty(random)) {
            String session = rawResponse.getString("session");
            createClient();
            raw("redeem", "random", random);
            assertFalse(rawResponse.has("error"));
            assertEquals(session, rawResponse.get("session"));
        }

    }
}
