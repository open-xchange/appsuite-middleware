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

package com.openexchange.login.internal.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;


/**
 * {@link CompositeLoginFormatterTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CompositeLoginFormatterTest {
    /**
     * Initializes a new {@link CompositeLoginFormatterTest}.
     */
    public CompositeLoginFormatterTest() {
        super();
    }

         @Test
     public void testLoginFormat() {
        final CompositeLoginFormatter cp = new CompositeLoginFormatter("$u - $c - $s - $agent $client end", null);
        List<LoginFormatter> loginFormatters = cp.getLoginFormatters();

        assertEquals("Unexpected size", 10, loginFormatters.size());

        assertTrue("Unexpected formatter", TokenFormatter.USER.equals(loginFormatters.get(0)));
        assertTrue("Unexpected formatter", TokenFormatter.CONTEXT.equals(loginFormatters.get(2)));
        assertTrue("Unexpected formatter", TokenFormatter.SESSION.equals(loginFormatters.get(4)));
        assertTrue("Unexpected formatter", TokenFormatter.AGENT.equals(loginFormatters.get(6)));
        assertTrue("Unexpected formatter", TokenFormatter.CLIENT.equals(loginFormatters.get(8)));

        assertEquals("Unexpected formatter", " - ", loginFormatters.get(1).toString());
        assertEquals("Unexpected formatter", " - ", loginFormatters.get(3).toString());
        assertEquals("Unexpected formatter", " - ", loginFormatters.get(5).toString());
        assertEquals("Unexpected formatter", " ", loginFormatters.get(7).toString());
        assertEquals("Unexpected formatter", " end", loginFormatters.get(9).toString());
    }

}
