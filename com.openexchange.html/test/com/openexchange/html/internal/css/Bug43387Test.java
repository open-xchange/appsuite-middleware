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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import com.openexchange.java.StringBuilderStringer;
import com.openexchange.java.Stringer;


/**
 * Simple unit tests for {@link CSSMatcher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.1
 */
public class Bug43387Test {

     @Test
     public void testBug43387() {
        final Stringer cssBuffer = new StringBuilderStringer(new StringBuilder(256));
        String css = "position: absolute;border: 1px solid black;background: #FFF;width: 5px;height: 5px;z-index: 10000";

        Map<String, Set<String>> styleMap = new HashMap<String, Set<String>>();
        Set<String> values = new HashSet<String>();
        /*
         * background
         */
        values.add("Nc");
        values.add("scroll");
        values.add("fixed");
        values.add("transparent");
        values.add("top");
        values.add("bottom");
        values.add("center");
        values.add("left");
        values.add("right");
        values.add("repeat");
        values.add("repeat-x");
        values.add("repeat-y");
        values.add("no-repeat");
        styleMap.put("background", values);
        values = new HashSet<String>();
        /*
         * background-image
         */
        values.add("d"); // delete
        styleMap.put("background-image", values);

        boolean checkCSSElements = CSSMatcher.checkCSS(cssBuffer.append(css), styleMap, "test", false, true);
        assertFalse(checkCSSElements);
    }

}