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
import com.openexchange.html.HtmlService;


/**
 * {@link Bug17195Test}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug17195Test extends AbstractSanitizing {

     @Test
     public void testContainsSigns() throws Exception {
        HtmlService htmlService = getHtmlService();

        String url = "https://ox6.local.com/ox6/#m=3Dinfostore&f=485D112&i=4D89719";

        String expected = "<!doctype html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "</head>\n" +
            "<body>\n" +
            "https://ox6.local.com/ox6/#m=3Dinfostore&amp;f=485D112&amp;i=4D89719\n" +
            "</body>\n" +
            "</html>";

        String result = trimLines(htmlService.getConformHTML(url, "UTF-8"));

        assertEquals("Unexpected return value", expected, result);
    }

}
