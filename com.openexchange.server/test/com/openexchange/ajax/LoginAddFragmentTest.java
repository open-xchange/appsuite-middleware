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

package com.openexchange.ajax;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.ajax.login.LoginTools;

/**
 * {@link LoginAddFragmentTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SuppressWarnings("synthetic-access")
public class LoginAddFragmentTest {

    public void assertFragment(String original, String expected) {
        assertEquals(expected, new TestLogin().addFragmentParam(original, "session", "abcd"));
    }

    @Test
    public void testSimple() {
        assertFragment("http://www.open-xchange.com/index.html", "http://www.open-xchange.com/index.html#session=abcd");
    }

    @Test
    public void testEnhanceExistingFragment() {
        assertFragment("http://www.open-xchange.com/index.html#f=12&i=23", "http://www.open-xchange.com/index.html#f=12&i=23&session=abcd");
    }

    @Test
    public void testDelimitedByQuestionMark() {
        assertFragment("http://www.open-xchange.com/index.html#f=12&i=23?someParam=someValue", "http://www.open-xchange.com/index.html#f=12&i=23&session=abcd?someParam=someValue");
    }

    private static final class TestLogin extends LoginServlet {
        private static final long serialVersionUID = 6740060632253781537L;

        public String addFragmentParam(String url, String param, String value) {
            return LoginTools.addFragmentParameter(url, param, value);
        }
    }
}
