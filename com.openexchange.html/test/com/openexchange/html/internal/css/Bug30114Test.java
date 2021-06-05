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