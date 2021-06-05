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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test;

import java.util.List;
import org.json.JSONArray;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified.SimplifiedHeaderTest;

/**
 * {@link TestCommandUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class TestCommandUtil {

    /**
     * Tests if the specified {@link JSONArray} contains all headers of the specified {@link SimplifiedHeaderTest}
     * 
     * @param headerTest The {@link SimplifiedHeaderTest}
     * @param headers The {@link JSONArray} with the headers
     * @return <code>true</code> if the specified {@link JSONArray} contains all headers of the specified {@link SimplifiedHeaderTest}
     *         <code>false</code> otherwise
     */
    public static boolean isSimplified(SimplifiedHeaderTest headerTest, JSONArray headers) {
        List<String> simplifiedHeaders = headerTest.getHeaderNames();
        if (simplifiedHeaders.size() != headers.length()) {
            return false;
        }
        for (Object header : headers) {
            if (!simplifiedHeaders.contains(header)) {
                return false;
            }
        }
        return true;
    }
}
