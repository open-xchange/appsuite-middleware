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
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.HtmlSanitizeOptions;
import com.openexchange.html.HtmlSanitizeOptions.ParserPreference;

/**
 * {@link Bug56582VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug56582VulTest extends AbstractSanitizing {

    public Bug56582VulTest() {
        super();
    }

    @Test
    public void testCorruptCss() throws Exception {
        String content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "\n" +
            "</head>\n" +
            "<body>\n" +
            "<style>\n" +
            ".a {\n" +
            "        font-family: </styl/**/e>;\n" +
            "        font-family: </sty/**/le>;\n" +
            "        font-family: </s/*data*/tyle>;\n" +
            "} \n" +
            ".<iframe/onload=alert(document[\"cookie\"])> { } \n" +
            "</style>\n" +
            "\n" +
            "    <p>hello</p>\n" +
            "</body>\n" +
            "</html>";

        {
            HtmlSanitizeOptions.Builder options = HtmlSanitizeOptions.builder();
            options.setOptConfigName(null);
            options.setDropExternalImages(false);
            options.setModified(null);
            options.setCssPrefix(null);
            options.setParserPreference(ParserPreference.JERICHO);

            String sanitized = getHtmlService().sanitize(content, options.build()).getContent();

            assertTrue("Unexpected content: " + sanitized, sanitized.indexOf("onload") < 0);
        }

        {
            HtmlSanitizeOptions.Builder options = HtmlSanitizeOptions.builder();
            options.setOptConfigName(null);
            options.setDropExternalImages(false);
            options.setModified(null);
            options.setCssPrefix(null);
            options.setParserPreference(ParserPreference.JSOUP);

            String sanitized = getHtmlService().sanitize(content, options.build()).getContent();

            assertTrue("Unexpected content: " + sanitized, sanitized.indexOf("onload") < 0);
        }
    }

}
