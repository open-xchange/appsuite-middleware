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

package com.openexchange.html.bugtests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;


/**
 * {@link Bug21532Test}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug21532Test extends AbstractSanitizing {
     @Test
     public void testConvertConditionalCommentsWihoutWhitespaces() throws Exception {
        String content = trimLines(getHtmlService().getConformHTML("</head><body><![if !supportLists]><p>You should see this</p><![endif]></body></html>", "UTF-8"));

        String expected = "<!doctype html>\n" +
            "<html>\n"
          + "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "</head>\n"
            + "<body>\n"
            + "<p>You should see this</p>\n"
            + "</body>\n"
            + "</html>";

        assertEquals("Unexpected return value", expected, trimLines(content));
    }
}
