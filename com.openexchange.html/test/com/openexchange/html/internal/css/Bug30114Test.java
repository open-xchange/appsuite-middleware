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

package com.openexchange.html.internal.css;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;


/**
 * Simple unit tests for {@link CSSMatcher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public class Bug30114Test {

    @Test
    public void testBug30114() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "body * { font-family:Tahoma, Geneva, sans-serif; font-size:13px; }\n" +
            "a { color:#000; text-decoration:underline; }\n" +
            ".trenner { text-align:center; }";

        boolean checkCSSElements = CSSMatcher.checkCSS(cssBuffer.append(css), null, "test", false, true);
        assertFalse(checkCSSElements);

        final String saneCss = cssBuffer.toString();
        final String[] lines = saneCss.split("\r?\n");

        String line = "#test * { font-family:Tahoma, Geneva, sans-serif; font-size:13px; }";
        assertTrue("Expected to start with: " + line + ", but was " + lines[0].trim().replaceAll(" +", " "), lines[0].trim().replaceAll(" +", " ").equals(line));

        line = "#test a { color:#000; text-decoration:underline; }";
        assertTrue("Expected to start with: " + line + ", but was " + lines[1].trim().replaceAll(" +", " "), lines[1].trim().replaceAll(" +", " ").equals(line));

        line = "#test .test-trenner { text-align:center; }";
        assertTrue("Expected to start with: " + line + ", but was " + lines[2].trim().replaceAll(" +", " "), lines[2].trim().replaceAll(" +", " ").equals(line));

    }


    @Test
    public void testBug30114_2() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "* { font-family:Tahoma, Geneva, sans-serif; font-size:13px; }\n" +
            "a { color:#000; text-decoration:underline; }\n" +
            ".trenner { text-align:center; }";

        boolean checkCSSElements = CSSMatcher.checkCSS(cssBuffer.append(css), null, "test", false, true);
        assertFalse(checkCSSElements);

        final String saneCss = cssBuffer.toString();
        final String[] lines = saneCss.split("\r?\n");

        String line = "#test * { font-family:Tahoma, Geneva, sans-serif; font-size:13px; }";
        assertTrue("Expected to start with: " + line + ", but was " + lines[0].trim().replaceAll(" +", " "), lines[0].trim().replaceAll(" +", " ").equals(line));

        line = "#test a { color:#000; text-decoration:underline; }";
        assertTrue("Expected to start with: " + line + ", but was " + lines[1].trim().replaceAll(" +", " "), lines[1].trim().replaceAll(" +", " ").equals(line));

        line = "#test .test-trenner { text-align:center; }";
        assertTrue("Expected to start with: " + line + ", but was " + lines[2].trim().replaceAll(" +", " "), lines[2].trim().replaceAll(" +", " ").equals(line));

    }

}