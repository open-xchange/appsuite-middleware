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
import com.openexchange.html.AssertionHelper;

/**
 * {@link Bug26153Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug26153Test extends AbstractSanitizing {

     @Test
     public void testDocumentize() {
        String htmlContent = "<p>my html document without html, or body tags</p>";
        String actual = getHtmlService().documentizeContent(htmlContent, "UTF-8");
        StringBuilder expectedBuilder = new StringBuilder();
        expectedBuilder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        expectedBuilder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        expectedBuilder.append("<head>\n");
        expectedBuilder.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
        expectedBuilder.append("</head>\n");
        expectedBuilder.append("<body>\n");
        expectedBuilder.append("<p>my html document without html, or body tags</p></body>\n");
        expectedBuilder.append("</html>\n");
        String expected = expectedBuilder.toString();

        AssertionHelper.assertTag("!DOCTYPE", actual, false);
        AssertionHelper.assertTag("<html", actual, true);
        AssertionHelper.assertTag("<body>", actual, true);
        AssertionHelper.assertTag("<meta", actual, false);

        assertEquals("Unexpected output", expected, actual);
    }

}
