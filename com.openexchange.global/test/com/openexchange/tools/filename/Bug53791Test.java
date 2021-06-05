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

package com.openexchange.tools.filename;

import org.junit.Test;

/**
 * {@link BugFileNameToolsTest}
 *
 * Lost support for umlauts in file names with certain browsers on macOS
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug53791Test extends AbstractFileNameToolsTest {

    @Test
    public void testDecomposedString() {
        String[] strings = new String[] {
            "Du\u0308ru\u0308m.txt",
            "D\u00fcr\u00fcm.txt",
            "Jos\u00E9.txt",
            "Jos\u0065\u0301.txt",
            "\u00C5ngstrom.txt",
            "\u0041\u030Angstrom.txt"
        };
        for (String string : strings) {
            checkSanitizing(string, true);
        }
    }

}
