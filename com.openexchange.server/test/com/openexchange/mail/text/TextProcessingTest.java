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

package com.openexchange.mail.text;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


/**
 * {@link TextProcessingTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextProcessingTest {
    /**
     * Initializes a new {@link TextProcessingTest}.
     */
    public TextProcessingTest() {
        super();
    }

         @Test
     public void testForBug31157() {
        // Keep trailing white-space
        final String s = "line1\n\n-- \nMy signature";

        final String processed = TextProcessing.performLineFolding(s, 76);

        final String[] lines = processed.split("\r?\n");
        final String delim = lines[lines.length - 2];

        assertEquals("Unexpected signature deliminiter line", "-- ", delim);
    }

}
