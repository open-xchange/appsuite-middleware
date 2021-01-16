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

package org.json;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * {@link JSONInputStreamTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class JSONInputStreamTest {

    /**
     * Initializes a new {@link JSONInputStreamTest}.
     */
    public JSONInputStreamTest() {
        super();
    }

    @Test
    public void test_InputStreamEntity() throws IOException, JSONException {
        JSONObject json = new JSONObject()
            .putSafe("username", "testing")
            .putSafe("password", "foo\\*bar")
            .putSafe("clientip", "127.0.0.1")
            .putSafe("callingsystem", "webmail");

        String jsonBodyString = json.toString();
        assertThat(jsonBodyString, is(equalTo("{\"username\":\"testing\",\"password\":\"foo\\\\*bar\",\"clientip\":\"127.0.0.1\",\"callingsystem\":\"webmail\"}")));
        int contentLength = jsonBodyString.length();

        Reader reader = new InputStreamReader(new JSONInputStream(json, "UTF-8"), StandardCharsets.UTF_8);
        int intValueOfChar;
        StringBuilder targetString = new StringBuilder(contentLength);
        while ((intValueOfChar = reader.read()) != -1) {
            targetString.append((char) intValueOfChar);
        }
        reader.close();

        JSONObject newJson = new JSONObject(targetString.toString());
        assertThat(targetString.toString(), is(equalTo("{\"username\":\"testing\",\"password\":\"foo\\\\*bar\",\"clientip\":\"127.0.0.1\",\"callingsystem\":\"webmail\"}")));
        assertThat(targetString.length(), is(equalTo(contentLength)));
        assertThat(newJson, is(equalTo(json)));
    }

    @Test
    public void test_InputStreamEntity2() throws IOException, JSONException {
        String quote = String.valueOf(Character.toChars(34));
        JSONObject json = new JSONObject()
            .putSafe("username", "testing")
            .putSafe("password", "foo"+quote+"bar")
            .putSafe("clientip", "127.0.0.1")
            .putSafe("callingsystem", "webmail");

        String jsonBodyString = json.toString();
        assertThat(jsonBodyString, is(equalTo("{\"username\":\"testing\",\"password\":\"foo\\\"bar\",\"clientip\":\"127.0.0.1\",\"callingsystem\":\"webmail\"}")));
        int contentLength = jsonBodyString.length();

        Reader reader = new InputStreamReader(new JSONInputStream(json, "UTF-8"), StandardCharsets.UTF_8);
        int intValueOfChar;
        StringBuilder targetString = new StringBuilder(contentLength);
        while ((intValueOfChar = reader.read()) != -1) {
            targetString.append((char) intValueOfChar);
        }
        reader.close();

        JSONObject newJson = new JSONObject(targetString.toString());
        assertThat(targetString.toString(), is(equalTo("{\"username\":\"testing\",\"password\":\"foo\\\"bar\",\"clientip\":\"127.0.0.1\",\"callingsystem\":\"webmail\"}")));
        assertThat(targetString.length(), is(equalTo(contentLength)));
        assertThat(newJson, is(equalTo(json)));
    }

}
