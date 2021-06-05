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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug51480VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug51480VulTest extends AbstractSanitizing {

    public Bug51480VulTest() {
        super();
    }

    @Test
    public void testCorruptHtml() {
        String content = "<hr>\n" +
            "Start\n" +
            "<! foo=\"[[[x]]\"><x foo=\"]foo>supa\"> [D] <% hej <input autofocus onfocus=\"alert(document.domain)//%><script>alert(5)</script>\" name=\"test\"><body>test<s>test<s>test</s></s>\n" +
            "Stop";

        try {
            getHtmlService().sanitize(content, null, false, null, null);
            fail("HTML should be recognized as corrupt");
        } catch (OXException e) {
            assertTrue("Unexpected error", e.equalsCode(4, "HTM"));
        }
    }

}
