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
import java.util.LinkedList;
import java.util.Queue;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.AssertionHelper;

/**
 * {@link Bug21584Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21584Test extends AbstractSanitizing {

     @Test
     public void testConvertHTMLBlockQuotesToTextQuotes() {
        Queue<String> quotedText = new LinkedList<String>();
        quotedText.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        quotedText.add("Cheers");
        quotedText.add("Foo bar");

        StringBuilder htmlContentBuilder = new StringBuilder();
        htmlContentBuilder.append("<html><head></head><body bgcolor=\"#FFFFFF\"><div>Foobar<br><br>(mobile)</div><div><br>On February 14, 2012 at 11:56 AM \"Foo Bar\" &lt;<a href=\"mailto:foo@bar.invalid\">foo@bar.invalid</a>&gt; wrote:<br><br></div><div></div><blockquote type=\"cite\"><div><meta http-equiv=\"Content-Type\" content=\"text/html; charset=us-ascii\"><meta name=\"Generator\" content=\"Microsoft Word 12 (filtered medium)\"><style><!--");
        htmlContentBuilder.append("/* Font Definitions */");
        htmlContentBuilder.append("@font-face");
        htmlContentBuilder.append("{font-family:Calibri;");
        htmlContentBuilder.append("panose-1:2 15 5 2 2 2 4 3 2 4;}");
        htmlContentBuilder.append("/* Style Definitions */");
        htmlContentBuilder.append("p.MsoNormal, li.MsoNormal, div.MsoNormal");
        htmlContentBuilder.append("{margin:0in;");
        htmlContentBuilder.append("margin-bottom:.0001pt;");
        htmlContentBuilder.append("font-size:11.0pt;");
        htmlContentBuilder.append("font-family:\"Calibri\",\"sans-serif\";}");
        htmlContentBuilder.append("a:link, span.MsoHyperlink");
        htmlContentBuilder.append("{mso-style-priority:99;");
        htmlContentBuilder.append("color:blue;");
        htmlContentBuilder.append("text-decoration:underline;}");
        htmlContentBuilder.append("a:visited, span.MsoHyperlinkFollowed");
        htmlContentBuilder.append("{mso-style-priority:99;");
        htmlContentBuilder.append("color:purple;");
        htmlContentBuilder.append("text-decoration:underline;}");
        htmlContentBuilder.append("span.EmailStyle17");
        htmlContentBuilder.append("{mso-style-type:personal-compose;");
        htmlContentBuilder.append("font-family:\"Calibri\",\"sans-serif\";");
        htmlContentBuilder.append("color:windowtext;}");
        htmlContentBuilder.append(".MsoChpDefault");
        htmlContentBuilder.append("{mso-style-type:export-only;}");
        htmlContentBuilder.append("@page WordSection1");
        htmlContentBuilder.append("{size:8.5in 11.0in;");
        htmlContentBuilder.append("margin:1.0in 1.0in 1.0in 1.0in;}");
        htmlContentBuilder.append("div.WordSection1");
        htmlContentBuilder.append("{page:WordSection1;}");
        htmlContentBuilder.append("--></style><!--[if gte mso 9]><xml>");
        htmlContentBuilder.append("<o:shapedefaults v:ext=\"edit\" spidmax=\"1026\" />");
        htmlContentBuilder.append("</xml><![endif]--><!--[if gte mso 9]><xml>");
        htmlContentBuilder.append("<o:shapelayout v:ext=\"edit\">");
        htmlContentBuilder.append("<o:idmap v:ext=\"edit\" data=\"1\" />");
        htmlContentBuilder.append("</o:shapelayout></xml><![endif]--><div class=\"WordSection1\">");
        htmlContentBuilder.append("<p class=\"MsoNormal\">Lorem ipsum dolor sit amet, consectetur adipiscing elit.<o:p></o:p></p>");
        htmlContentBuilder.append("<p class=\"MsoNormal\"><o:p>&nbsp;</o:p></p>");
        htmlContentBuilder.append("<p class=\"MsoNormal\">Cheers<o:p></o:p></p>");
        htmlContentBuilder.append("<p class=\"MsoNormal\"><o:p>&nbsp;</o:p></p>");
        htmlContentBuilder.append("<p class=\"MsoNormal\">Foo bar<o:p></o:p></p>");
        htmlContentBuilder.append("<p class=\"MsoNormal\"><o:p>&nbsp;</o:p></p></div></div></blockquote></body></html>");
        String htmlContent = htmlContentBuilder.toString();

        String actual = getHtmlService().html2text(htmlContent, false);
        StringBuilder expectedBuilder = new StringBuilder();
        expectedBuilder.append("Foobar\r\n\r\n");
        expectedBuilder.append("(mobile)\r\n\r\n");
        expectedBuilder.append("On February 14, 2012 at 11:56 AM \"Foo Bar\" <foo@bar.invalid> wrote:\r\n\r\n\r\n");
        expectedBuilder.append("> \r\n");
        expectedBuilder.append(">     Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\n");
        expectedBuilder.append("> \r\n");
        expectedBuilder.append(">      \r\n");
        expectedBuilder.append("> \r\n");
        expectedBuilder.append(">     Cheers\r\n");
        expectedBuilder.append("> \r\n");
        expectedBuilder.append(">      \r\n");
        expectedBuilder.append("> \r\n");
        expectedBuilder.append(">     Foo bar\r\n");
        expectedBuilder.append("> \r\n");
        expectedBuilder.append(">      \r\n");
        expectedBuilder.append("> \r\n");

        String expected = expectedBuilder.toString();
        String[] lines = expected.split("\r\n");

        AssertionHelper.assertBlockingQuote(quotedText, lines);
        assertEquals("Unexpected value", expected, actual);
    }
}
