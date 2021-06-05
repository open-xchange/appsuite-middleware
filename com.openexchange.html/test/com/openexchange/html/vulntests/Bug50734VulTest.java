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
 * {@link Bug50734VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug50734VulTest extends AbstractSanitizing {

    public Bug50734VulTest() {
        super();
    }

    @Test
    public void testStartTagSanitizing() {
        String content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head><body>\n" +
            "<form><isindex formaction=\"javascript&colon;confirm(1)\"\n" +
            "<img src=`a` onerror=alert(1)&NewLine; &#34;&#62;<h1/onmouseover='\\u0061lert(2)'>\n" +
            "<iframe/src=\"jAvAsCrIpT:alert(document.domain)\">\n" +
            "\n" +
            "</body></html>";
        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "isindex");
    }

    @Test
    public void testStartTagSanitizing2() {
        String content = "Hello there 3 \n" +
            "\n" +
            "<!-- --!>\n" +
            "<input type=hidden style=`x:expression(alert(/ /))`>\n" +
            "<! XSS=\"><img src=xx:x onerror=alert(1)//\"> ";
        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "onerror");
    }

}
