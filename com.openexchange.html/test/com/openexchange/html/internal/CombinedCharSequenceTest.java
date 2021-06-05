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

package com.openexchange.html.internal;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.html.tools.CombinedCharSequence;

/**
 * {@link CombinedCharSequenceTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class CombinedCharSequenceTest {

    public CombinedCharSequenceTest() {
        super();
    }

     @Test
     public void testCombinedCharSequence() {
        String s1 = "Hello Pet";
        String s2 = "er Park";
        String s3 = "er How";

        StringBuilder sb = new StringBuilder(32);

        CombinedCharSequence cs = new CombinedCharSequence(s1, s2, s3);
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            sb.append(cs.charAt(i));
        }
        assertEquals("Hello Peter Parker How", sb.toString());

        sb.setLength(0);
        CharSequence subSequence = cs.subSequence(6, length - 4);
        length = subSequence.length();
        for (int i = 0; i < length; i++) {
            sb.append(subSequence.charAt(i));
        }
        assertEquals("Peter Parker", sb.toString());
    }

}
