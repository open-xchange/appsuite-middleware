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

package com.openexchange.i18n.tools.replacement;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;

/**
 * {@link AuthorizationTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class AuthorizationTest {

    @Test
    public final void testDecodeBasicAuth() {
        Map<String, Credentials> headerToCredentials = new HashMap<String, Credentials>();
        headerToCredentials.put("Basic aGFsbG86MTIz", new Credentials("hallo", "123"));
        headerToCredentials.put("Basic MyQ1MyAgw7Z3QDIzIDJfN3ExXsKwYjpmc2VnZnNnZnNndmE=", new Credentials("3$53  \u00f6w@23 2_7q1^\u00b0b", "fsegfsgfsgva"));
        headerToCredentials.put("Basic dXNlcjU6ZW1wdHkxIA==", new Credentials("user5", "empty1 "));
        headerToCredentials.put("Basic dXNlcjU6IGVtcHR5Mg==", new Credentials("user5", " empty2"));
        for (Entry<String, Credentials> entry : headerToCredentials.entrySet()) {
            Credentials actual = Authorization.decode(entry.getKey());
            assertEquals(entry.getValue().getLogin(), actual.getLogin());
            assertEquals(entry.getValue().getPassword(), actual.getPassword());
        }
    }

}
