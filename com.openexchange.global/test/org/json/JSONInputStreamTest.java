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
