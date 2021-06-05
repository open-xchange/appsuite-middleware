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

package com.openexchange.password.mechanism.impl.mech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.exception.OXException;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.stock.StockPasswordMechs;

/**
 * {@link PasswordMechTests}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
@RunWith(Parameterized.class)
public class PasswordMechTests {

    private static final String PASSWORD = "secret";

    private PasswordMech passwordMech;

    @Parameters(name = "{index}: {0}")
    public static Iterable<? extends Object> data() {
        return Arrays.asList(StockPasswordMechs.BCRYPT.getPasswordMech(), StockPasswordMechs.CRYPT.getPasswordMech(), StockPasswordMechs.SHA1.getPasswordMech(), StockPasswordMechs.SHA256.getPasswordMech(), StockPasswordMechs.SHA512.getPasswordMech());
    }

    public PasswordMechTests(PasswordMech passwordMech) {
        this.passwordMech = passwordMech;
    }

    @Test
    public void testEncode_withPassword_handleProperly() throws OXException {
        PasswordDetails encode = this.passwordMech.encode(PASSWORD);

        assertEquals(PASSWORD, encode.getPlainPassword());
        assertEquals(this.passwordMech.getIdentifier(), encode.getPasswordMech());
        assertNotNull(encode.getEncodedPassword());

        boolean check = this.passwordMech.check(PASSWORD, encode.getEncodedPassword(), encode.getSalt());
        assertTrue(check);
    }

    @Test
    public void testEncode_passwordNull_handleProperly() throws OXException {
        PasswordDetails encode = this.passwordMech.encode(null);

        assertNull(encode.getPlainPassword());
        assertNull(encode.getEncodedPassword());
        assertEquals(this.passwordMech.getIdentifier(), encode.getPasswordMech());

        boolean check = this.passwordMech.check(null, encode.getEncodedPassword(), encode.getSalt());
        assertTrue(check);
    }

    @Test
    public void testEncode_passwordEmpty_handleProperly() throws OXException {
        PasswordDetails encode = this.passwordMech.encode("");

        assertEquals("", encode.getPlainPassword());
        assertNull(encode.getEncodedPassword());
        assertEquals(this.passwordMech.getIdentifier(), encode.getPasswordMech());

        boolean check = this.passwordMech.check("", encode.getEncodedPassword(), encode.getSalt());
        assertTrue(check);
    }

}
