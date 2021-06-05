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

package com.openexchange.java;

import static org.junit.Assert.assertTrue;
import org.junit.Test;


/**
 * {@link StringsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StringsTest {
    
    private static final String REPLACE_TEST = "/ajax/image/mail/picture?folder=default0%2FINBOX%2FDrafts&amp;id=45&amp;uid=34c8e615-fa54-45c2-973b-37b032af8bcd";
    private static final String REPLACE_RESULT = "/ajax/image/mail/picture?folder=default0%2FINBOX%2FDrafts&id=45&uid=34c8e615-fa54-45c2-973b-37b032af8bcd";

    /**
     * Initializes a new {@link StringsTest}.
     */
    public StringsTest() {
        super();
    }

    @Test
    public final void testSplitByTokensOrQuotedStrings() {
        String str = "This is  a string that \"will \\\"be\" highlighted when your \"super-'mega'-duper\" 'regular expr\\'ession' matches something 'really \"heavy\" cool'.";

        String[] tokens = Strings.splitByTokensOrQuotedStrings(str);

        String[] expecteds = new String[] { "This", "is", "a", "string", "that", "\"will \\\"be\"", "highlighted", "when", "your", "\"super-'mega'-duper\"", "'regular expr\\'ession'", "matches", "something", "'really \"heavy\" cool'." };
        org.junit.Assert.assertArrayEquals("Unexpected array content", expecteds, tokens);
    }
    
    @Test
    public final void testReplaceSequenceWith_Char() {
        String replaceSequenceWith = Strings.replaceSequenceWith(REPLACE_TEST, "&amp;", '&');
        assertTrue("Strings do not match", replaceSequenceWith.equals(REPLACE_RESULT));
    }
    
    @Test
    public final void testReplaceSequenceWith_String() {
        String replaceSequenceWith = Strings.replaceSequenceWith(REPLACE_TEST, "&amp;", "&");
        assertTrue("Strings do not match", replaceSequenceWith.equals(REPLACE_RESULT));
    }

}
