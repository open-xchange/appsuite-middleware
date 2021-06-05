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

package com.openexchange.html.vulntests;

import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.AssertionHelper;


/**
 * {@link Bug22284VulTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug22284VulTest extends AbstractSanitizing {
     @Test
     public void testSanitize() {
        String content = "<HTML><HEAD><STYLE " +
            "id=\"styletagforeditor\">body{background-color:rgb(255,255,255);direction:ltr;font-family:times " +
            "new " +
            "roman;font-size:12pt;line-height:1.2;padding-top:0.787in;padding-right:0.787in;padding-bottom:0.787i " +
            "n;padding-left:0.787in;margin:0in;} " +
            "p{margin-top:0pt;margin-bottom:0pt;}</STYLE><STYLE " +
            "id=\"styletagtwoforeditor\" type=\"text/css\">table { font-size: 12pt } " +
            "table p, li p { margin : 0px; }</STYLE></HEAD><BODY><P><IMG  " +
            "align=\"bottom\" alt=\"onerror=``alert(1)\"  " +
            "src=\"http://localhost\"></P></BODY></HTML>";

         AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "onerror=``alert(1)");
    }
}
