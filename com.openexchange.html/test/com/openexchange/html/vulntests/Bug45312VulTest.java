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
 * {@link Bug45312VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug45312VulTest extends AbstractSanitizing {

    public Bug45312VulTest() {
        super();
    }

     @Test
     public void testScriptTagSanitizing() {
        String content = "\n" +
            "\n" +
            "13 - <SCRIPT SRC=//ha.ckers.org/.j>\n" +
            "\n" +
            "14 - <BODY onload!#$%&()*~+-_.,:;?@[/|\\]^`=alert(\"samp\")>\n" +
            "\n" +
            "15 - <BODY BACKGROUND=\"javascript:alert('element')\">\n" +
            "\n" +
            "16 - <BODY ONLOAD=alert('fast')>";

        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "<script", "ha.ckers.org", "onload", "javascript");
    }
}
