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
 * {@link Bug29892Test}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug29892Test extends AbstractSanitizing {
     @Test
     public void testIgnoreHrefBaseTag() {
        String content = getHtmlService().checkBaseTag("<html>\n" +
            "<head>" +
            "<base href=\"https://bugs.open-xchange.com/\" /> \n" +
            "<style type=\"text/css\">\n" +
            "table {\n" +
            "font-size: 9pt;\n" +
            "}\n" +
            "</style>\n" +
            "</head></html>", false);

        assertEquals("Base tag", "<html>\n" +
            "<head> \n" +
            "<style type=\"text/css\">\n" +
            "table {\n" +
            "font-size: 9pt;\n" +
            "}\n" +
            "</style>\n" +
            "</head></html>",content);
    }
}
