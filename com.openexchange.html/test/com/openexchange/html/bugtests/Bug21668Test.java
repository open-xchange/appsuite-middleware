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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.AssertionHelper;

/**
 * {@link Bug21668Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21668Test extends AbstractSanitizing {

     @Test
     public void testPlainTextQuoting() {
        Queue<String> quotedText = new LinkedList<String>();
        quotedText.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent nisl diam, egestas");
        quotedText.add(" vulputate bibendum at, ornare pulvinar odio.");
        quotedText.add(" Ut vulputate purus purus, ac congue ipsum mattis vel. Suspendisse potenti.");
        quotedText.add(" Duis nec nunc lectus. Suspendisse maximus fermentum egestas. Nulla sed mauris");
        quotedText.add(" nec lorem blandit finibus quis vel velit. Cras lacinia non neque sed pellentesque.");

        StringBuilder htmlContentBuilder = new StringBuilder();
        htmlContentBuilder.append("<p style=\"margin: 0;\"><span><span>Foo bar</span></span></p>");
        htmlContentBuilder.append("<div style=\"margin: 5px 0px 5px 0px;\">");
        htmlContentBuilder.append("<br/>On April 1, 2015 at 11:20 AM &#34;Foo Bar&#34; &#60;foo@bar.invalid&#62; wrote:");
        htmlContentBuilder.append("<br/>");
        htmlContentBuilder.append("<br/> ");
        htmlContentBuilder.append("<div style=\"position: relative;\">");
        htmlContentBuilder.append("<blockquote style=\"margin-left: 0px; padding-left: 10px; border-left: solid 1px blue;\">");

        StringBuilder expectedBuilder = new StringBuilder();
        expectedBuilder.append("Foo bar\r\n\r\n\r\n");
        expectedBuilder.append("On April 1, 2015 at 11:20 AM \"Foo Bar\" <foo@bar.invalid> wrote:\r\n\r\n\r\n");

        Iterator<String> iterator = quotedText.iterator();
        while (iterator.hasNext()) {
            String q = iterator.next();
            htmlContentBuilder.append(q).append("<br/> ");
            expectedBuilder.append("> ").append(q).append("\r\n");
        }
        htmlContentBuilder.setLength(htmlContentBuilder.length() - "<br/>".length() - 1);
        htmlContentBuilder.append("</blockquote>");
        htmlContentBuilder.append("</div>");
        htmlContentBuilder.append("</div>");
        htmlContentBuilder.append("<p style=\"margin: 0px;\">&#160;</p>");
        htmlContentBuilder.append("<p style=\"margin: 0px;\">Morbi hendrerit aliquet laoreet. Nulla semper, diam ac ullamcorper rhoncus, felis lectus ullamcorper turpis, tincidunt euismod massa nibh sit amet dui. Nunc tristique ante a ex sollicitudin pulvinar.</p>");
        htmlContentBuilder.append("<p style=\"margin: 0px;\">&#160;</p> ");
        htmlContentBuilder.append("<p style=\"margin: 0px;\">Cras aliquet laoreet ligula at laoreet. Sed facilisis viverra elit nec lobortis. Quisque et faucibus purus. Fusce efficitur est vel mi sodales gravida. Aenean hendrerit rutrum condimentum. </p>");
        htmlContentBuilder.append("<p style=\"margin: 0px;\">&#160;</p>");

        expectedBuilder.append("> \r\n\r\n \r\n");
        expectedBuilder.append("Morbi hendrerit aliquet laoreet. Nulla semper, diam ac ullamcorper rhoncus, felis lectus ullamcorper turpis, tincidunt euismod massa nibh sit amet dui. Nunc tristique ante a ex sollicitudin pulvinar.\r\n \r\n");
        expectedBuilder.append("Cras aliquet laoreet ligula at laoreet. Sed facilisis viverra elit nec lobortis. Quisque et faucibus purus. Fusce efficitur est vel mi sodales gravida. Aenean hendrerit rutrum condimentum.\r\n \r\n");

        String actual = getHtmlService().html2text(htmlContentBuilder.toString(), false);
        String expected = expectedBuilder.toString();
        String[] quotedLines = expected.split("\r\n");

        AssertionHelper.assertBlockingQuote(quotedText, quotedLines);
        assertEquals("Unexpected value", expected, actual);
    }
}
