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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.openexchange.java.Strings;

/**
 * {@link AbstractFileNameToolsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractFileNameToolsTest {

    /**
     * Initializes a new {@link AbstractFileNameToolsTest}.
     */
    protected AbstractFileNameToolsTest() {
        super();
    }

    /**
     * Checks if specified string "survives" sanitization through {@link FileNameTools#sanitizeFilename(String) sanitizeFilename()}.
     *
     * @param string The string to check
     * @param checkNormalized <code>true</code> to also check if sanitization maintains noralized form, otherwise <code>false</code>
     */
    protected static void checkSanitizing(String string, boolean checkNormalized) {
        String sanitizedString = FileNameTools.sanitizeFilename(string);
        assertFalse("Sanitized characters in " + sanitizedString, sanitizedString.indexOf('_') >= 0);
        if (checkNormalized) {
            assertTrue("Unexpected string after sanitizing", Strings.equalsNormalized(string, sanitizedString));
        }
    }

}
