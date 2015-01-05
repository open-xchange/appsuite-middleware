/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.html;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * {@link Bug21584Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21584Test extends AbstractSanitizing {

    @Test
    public void testConvertHTMLBlockQuotesToTextQuotes() {
        String htmlContent = "<html><head></head><body bgcolor=\"#FFFFFF\"><div>Foobar<br><br>(mobile)</div><div><br>On February 14, 2012 at 11:56 AM \"Foo Bar\" &lt;<a href=\"mailto:foo@bar.invalid\">foo@bar.invalid</a>&gt; wrote:<br><br></div><div></div><blockquote type=\"cite\"><div><meta http-equiv=\"Content-Type\" content=\"text/html; charset=us-ascii\"><meta name=\"Generator\" content=\"Microsoft Word 12 (filtered medium)\"><style><!--" +
            "/* Font Definitions */" +
            "@font-face" +
            "{font-family:Calibri;" +
            "panose-1:2 15 5 2 2 2 4 3 2 4;}" +
            "/* Style Definitions */" +
            "p.MsoNormal, li.MsoNormal, div.MsoNormal" +
            "{margin:0in;" +
            "margin-bottom:.0001pt;" +
            "font-size:11.0pt;" +
            "font-family:\"Calibri\",\"sans-serif\";}" +
            "a:link, span.MsoHyperlink" +
            "{mso-style-priority:99;" +
            "color:blue;" +
            "text-decoration:underline;}" +
            "a:visited, span.MsoHyperlinkFollowed" +
            "{mso-style-priority:99;" +
            "color:purple;" +
            "text-decoration:underline;}" +
            "span.EmailStyle17" +
            "{mso-style-type:personal-compose;" +
            "font-family:\"Calibri\",\"sans-serif\";" +
            "color:windowtext;}" +
            ".MsoChpDefault" +
            "{mso-style-type:export-only;}" +
            "@page WordSection1" +
            "{size:8.5in 11.0in;" +
            "margin:1.0in 1.0in 1.0in 1.0in;}" +
            "div.WordSection1" +
            "{page:WordSection1;}" +
            "--></style><!--[if gte mso 9]><xml>" +
            "<o:shapedefaults v:ext=\"edit\" spidmax=\"1026\" />" +
            "</xml><![endif]--><!--[if gte mso 9]><xml>" +
            "<o:shapelayout v:ext=\"edit\">" +
            "<o:idmap v:ext=\"edit\" data=\"1\" />" +
            "</o:shapelayout></xml><![endif]--><div class=\"WordSection1\"><p class=\"MsoNormal\">Lorem ipsum dolor sit amet, consectetur adipiscing elit.<o:p></o:p></p><p class=\"MsoNormal\"><o:p>&nbsp;</o:p></p><p class=\"MsoNormal\">Cheers<o:p></o:p></p><p class=\"MsoNormal\"><o:p>&nbsp;</o:p></p><p class=\"MsoNormal\">Foo bar<o:p></o:p></p><p class=\"MsoNormal\"><o:p>&nbsp;</o:p></p></div></div></blockquote></body></html>";

        String actual = getHtmlService().html2text(htmlContent, false);
        String expected = "Foobar\r\n\r\n" +
            "(mobile)\r\n\r\n" +
            "On February 14, 2012 at 11:56 AM \"Foo Bar\" <foo@bar.invalid> wrote:\r\n\r\n\r\n" +
            "> \r\n" +
            ">     Lorem ipsum dolor sit amet, consectetur adipiscing elit.\r\n" +
            "> \r\n" +
            ">      \r\n" +
            "> \r\n" +
            ">     Cheers\r\n" +
            "> \r\n" +
            ">      \r\n" +
            "> \r\n" +
            ">     Foo bar\r\n" +
            "> \r\n" +
            ">      \r\n" +
            "> \r\n";

        assertEquals("The HTML <blockquote> tag is not converted correctly", expected, actual);
    }
}
