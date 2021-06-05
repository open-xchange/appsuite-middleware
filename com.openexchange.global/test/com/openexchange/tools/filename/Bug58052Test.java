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

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * {@link Bug58052Test}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class Bug58052Test extends AbstractFileNameToolsTest {

    @Test
    public void testLeaveAllowedUnicodeChars() {
        String fileName = "\u2460ab\u2461cd\u2462.zip";

        String sanitizedString = FileNameTools.sanitizeFilename(fileName);

        assertTrue("Characters wrongly sanitized " + sanitizedString, sanitizedString.contains("\u2460"));
        assertTrue("Characters wrongly sanitized " + sanitizedString, sanitizedString.contains("\u2461"));
        assertTrue("Characters wrongly sanitized " + sanitizedString, sanitizedString.contains("\u2462"));
        assertTrue("Unexpected string after sanitizing", fileName.equalsIgnoreCase(sanitizedString));
    }
}
