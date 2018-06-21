/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
