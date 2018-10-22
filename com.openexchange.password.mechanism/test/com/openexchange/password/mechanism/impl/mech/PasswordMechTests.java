/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.password.mechanism.stock.StockPasswordMechs;

/**
 * {@link PasswordMechTests}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
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

        boolean check = this.passwordMech.check(PASSWORD, encode.getEncodedPassword(), null);
        assertTrue(check);
    }

    @Test
    public void testEncode_passwordNull_handleProperly() throws OXException {
        PasswordDetails encode = this.passwordMech.encode(null);

        assertNull(encode.getPlainPassword());
        assertNull(encode.getEncodedPassword());
        assertEquals(this.passwordMech.getIdentifier(), encode.getPasswordMech());

        boolean check = this.passwordMech.check(null, encode.getEncodedPassword(), null);
        assertTrue(check);
    }

    @Test
    public void testEncode_passwordEmpty_handleProperly() throws OXException {
        PasswordDetails encode = this.passwordMech.encode("");

        assertEquals("", encode.getPlainPassword());
        assertNull(encode.getEncodedPassword());
        assertEquals(this.passwordMech.getIdentifier(), encode.getPasswordMech());

        boolean check = this.passwordMech.check("", encode.getEncodedPassword(), null);
        assertTrue(check);
    }

}
