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
 * {@link Bug21055Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21055Test extends AbstractSanitizing {

     @Test
     public void testSplitBlockQuotes() {
        String htmlContent = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
            " <head>" +
            "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>" +
            " </head>" +
            " <body>" +
            "  <p style=\"margin: 0pt;\">" +
            "   <span>" +
            "    <span></span>" +
            "    Test," +
            "   </span>" +
            "  </p>" +
            "  <p style=\"margin: 0pt;\">" +
            "   <span>&#160;</span>" +
            "  </p>" +
            "  <div style=\"margin: 5px 0px;\">" +
            "   <br/>" +
            "   On December 21, 2011 at 12:59 PM &#34;Foo Bar&#34; &#60;foo@bar.invalid&#62; wrote:" +
            "   <br/>" +
            "   <br/>" +
            "   <div style=\"position: relative;\">" +
            "    <blockquote style=\"margin-left: 0px; padding-left: 10px; border-left: 1px solid blue;\">" +
            "     Test in quote" +
            "     <div>&#160;</div>" +
            "     <div>foo</div>" +
            "     <div>bar</div>" +
            "    </blockquote>" +
            "   </div>" +
            "  </div>" +
            "  <p style=\"margin: 0px;\">&#160;</p>" +
            "  <p style=\"margin: 0px;\">some reply&#160;</p>" +
            "  <div style=\"margin: 5px 0px;\">" +
            "   <div style=\"position: relative;\">" +
            "    <blockquote style=\"margin-left: 0px; padding-left: 10px; border-left: 1px solid blue;\">" +
            "     <div>more quotes</div>" +
            "     <div>even more quotes</div>" +
            "     <div>&#160;</div>" +
            "     <div>moar quoting</div>" +
            "    </blockquote>" +
            "    <br/>" +
            "    &#160;" +
            "   </div>" +
            "  </div>" +
            "  <p style=\"margin: 0px; \"></p>" +
            " </body>" +
            "</html>";

        String actual = getHtmlService().html2text(htmlContent, false);

        String expected = "Test,\r\n \r\n\r\n\r\n" +
            "On December 21, 2011 at 12:59 PM \"Foo Bar\" <foo@bar.invalid> wrote:\r\n\r\n\r\n" +
            "> Test in quote\r\n" +
            ">   \r\n" +
            ">  foo\r\n" +
            ">  bar\r\n" +
            "> \r\n\r\n \r\n" +
            "some reply \r\n\r\n" +
            ">  more quotes\r\n" +
            ">  even more quotes\r\n" +
            ">   \r\n" +
            ">  moar quoting\r\n" +
            "> \r\n \r\n";

        assertEquals(expected, actual);
    }
}
