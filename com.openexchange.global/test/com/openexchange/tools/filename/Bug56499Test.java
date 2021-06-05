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
 * {@link Bug56499Test}
 *
 * Lost support for '&#x3010;' (LEFT BLACK LENTICULAR BRACKET, U+3010) and '&#x3011;' (RIGHT BLACK LENTICULAR BRACKET, U+3011) in file names
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class Bug56499Test extends AbstractFileNameToolsTest {

    @Test
    public void testDecomposedString() {
        checkSanitizing("\u3010\u30c9\u30e9\u30d5\u30c8\u7248\u3011\u57fa\u76e4\u6280\u8853\u306e\u9ad8\u5ea6\u5316-20171011.zip", true);
    }

}
