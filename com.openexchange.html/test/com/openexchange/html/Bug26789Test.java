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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import static org.junit.Assert.assertNotNull;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.osgi.HTMLServiceActivator;

/**
 * Verifies that parsing HTML does not fail due to some special HTML content.
 * TODO Actually we do not have any HTML content for that parsing fails. The fix for bug 26789 introduces writing down the HTML content to
 * files if parsing fails. Content entered below does not fail in the HTML parser. If we found failing HTML content from the written tmp
 * files add the content here and enable test in test suite.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug26789Test {

    private HtmlService service;

    public Bug26789Test() {
        super();
    }

    @Before
    public void setUp() {
        Object[] maps = HTMLServiceActivator.getDefaultHTMLEntityMaps();

        @SuppressWarnings("unchecked")
        final Map<String, Character> htmlEntityMap = (Map<String, Character>) maps[1];
        @SuppressWarnings("unchecked")
        final Map<Character, String> htmlCharMap = (Map<Character, String>) maps[0];

        htmlEntityMap.put("apos", Character.valueOf('\''));

        service = new HtmlServiceImpl(htmlCharMap, htmlEntityMap);
    }

    @After
    public void tearDown() {
        service = null;
    }

    @Test
    public void testForNullPointerException() {
        String content = content1;
        String test = service.getConformHTML(content, "UTF-8");
        assertNotNull(test);
        test = service.htmlFormat(content);
        assertNotNull(test);

        content = content2;
        test = service.getConformHTML(content, "UTF-8");
        assertNotNull(test);
        test = service.htmlFormat(content);
        assertNotNull(test);
    }

    private static final String content1 =
          "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
        + "\n"
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>\n"
        + "    <meta content=\"text/html;charset=UTF-8\" http-equiv=\"Content-Type\" />\n"
        + " <style type=\"text/css\">div.OutlookMessageHeader, table.moz-email-headers-table, blockquote #_t{background-image:url('[###########################################################################3]');}</style></head><body style=\"\">\n"
        + "\n"
        + " \n"
        + " \n"
        + " \n"
        + "  <div>\n"
        + "  meldete euch doch auch an, ihr erhaltet so jede Woche ein tolles Hauskreisthema:\n"
        + "</div> \n"
        + "  <blockquote type=\"cite\" style=\"position: relative; margin-left: 0px; padding-left: 10px; border-left: solid 1px blue;\">"
        + "   ---------- Urspr&#252;ngliche Nachricht ----------\n"
        + "   <br />Von: Kai Renz &#60;kontakt@internet-neukunden.de&#62;\n"
        + "   <br />An: Andre &#60;andre@butscher.de&#62;\n"
        + "   <br />Datum: 1. Juni 2013 um 00:09\n"
        + "   <br />Betreff: Das 9. Hauskreis-Thema ! \n"
        + "   <br />\n"
        + "  <br /> \n"
        + "   <p>Hallo Andre,&#160;</p> \n"
        + "   <p>hier ist das 9. Hauskreis-Thema &#34;Die Frau am Jakobsbrunnen&#34;:&#160;<br /><a href=\"https://app.getresponse.com/click.html?x=a62b&lc=BM4jl&mc=BZ&s=[..]\" target=\"_blank\">Hier klicken und das 9. Thema herunterladen: &#34;Die Frau am Jakobsbrunnen&#34;</a>&#160;</p> \n"
        + "   <p>K&#246;nnen Sie mir bitte einen Gefallen tun und diese Email auch an andere Hauskreis-Leiter weiterleiten: &#160;<br /><a href=\"https://app.getresponse.com/click.html?x=a62b&#38;lc=BM4j0&#38;mc=BZ&#38;s=rJF85&#38;y=v&#38;\" target=\"_blank\">Hier f&#252;r diesen Newsletter anmelden:&#160;http://www.hauskreisthemen.de</a>&#160;<br /><br />Lieben Gru&#223;&#160;<br />Kai Renz&#160;<br /><br />P.S.:&#160;<a href=\"https://app.getresponse.com/click.html?x=a62b&lc=BM4jO&mc=BZ&s=[..]\" target=\"_blank\">Hier auf Facebook: https://www.facebook.com/hauskreis</a>&#160;</p>\n"
        + "   <p>&#160;</p> \n"
        + "   <p>Wenn Sie keine weiteren Hauskreis-Themen mehr m&#246;chten, k&#246;nnen Sie sich&#160;<br />am Ende &#252;ber den &#34;unsubscripe&#34; Link selbst aus diesem Newsletter austragen.&#160;<br />--------------------------------------------------------------------------------------------&#160; </p>\n"
        + "   <br /> \n"
        + "    \n"
        + "     \n"
        + "      \n"
        + "       \n"
        + "      \n"
        + "     \n"
        + "   <table style=\"border-collapse: separate; width: 600px;\u00b8\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\"><tbody><tr><td style=\"font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 10px 0 10px 0;\" valign=\"middle\"> \n"
        + "        \n"
        + "         \n"
        + "          \n"
        + "         \n"
        + "          \n"
        + "         \n"
        + "       <table style=\"border-collapse: separate; width: 101px;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"right\"><tbody><tr><td style=\"font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 0; font-size: 10px; line-height: 18px; text-align: left;\" align=\"right\" valign=\"top\" width=\"101\">&#160;</td></tr></tbody></table> \n"
        + "        \n"
        + "         \n"
        + "          \n"
        + "           \n"
        + "          \n"
        + "        \n"
        + "       <table style=\"border-collapse: separate; width: 499px;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td style=\"font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 7px 0 0 0; color: #939598; font-size: 10px;\" align=\"left\" valign=\"middle\" width=\"499\" height=\"18\"><span style=\"color: #939598;\"><br /><br />Kai Renz, Keltenstrasse 6, Leinfelden-Echterdingen, Baden-W&#252;rttemberg 70771, Germany<br /><br /></span></td></tr></tbody></table> \n"
        + "        \n"
        + "         \n"
        + "          \n"
        + "           \n"
        + "          \n"
        + "         \n"
        + "       <table style=\"border-collapse: separate; width: 499px;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td style=\"font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 7px 0 0 0; color: #939598; font-size: 10px;\" align=\"left\" valign=\"middle\" width=\"499\" height=\"18\"><span style=\"color: #939598;\">You may <a href=\"https://app.getresponse.com/me.html?x=a62b&m=fRSu&s=rJF85&y=l&#[..]\"><span style=\"color: #0985f5;\">unsubscribe</span></a> or <a href=\"https://app.getresponse.com/me.html?x=a62b&m=fRSu&s=rJF85&y=l&#[..]\"><span style=\"color: #0985f5;\">change your contact details</span></a> at any time.</span></td></tr></tbody></table> </td></tr></tbody></table> \n"
        + "   <img src=\"https://app.getresponse.com/open.html?x=a62b&m=fRSu&mc=BZ&s=rJF[..]\" border=\"0\" width=\"0\" height=\"0\" />\n"
        + "  </blockquote> \n"
        + "  <div>\n"
        + "  <br />&#160;\n"
        + "  </div>\n"
        + " \n"
        + "</body></html>\n";

    private static final String content2 =
          "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
        + "\r\n"
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>\r\n"
        + "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\" />\r\n"
        + " <style type=\"text/css\">v\\:* {  }\r\n"
        + " o\\:* {  }\r\n"
        + " w\\:* {  }\r\n"
        + " .shape {  }\r\n"
        + " @font-face {  font-family: Calibri; }\r\n"
        + " @font-face {  font-family: Tahoma; }\r\n"
        + " @page WordSection1 { margin: 70.85pt 70.85pt 2.0cm 70.85pt; }\r\n"
        + " P.MsoNormal {  MARGIN: 0cm 0cm 0pt; FONT-FAMILY: \"Times New Roman\",\"serif\"; FONT-SIZE: 12pt;}\r\n"
        + " LI.MsoNormal {  MARGIN: 0cm 0cm 0pt; FONT-FAMILY: \"Times New Roman\",\"serif\"; FONT-SIZE: 12pt;}\r\n"
        + " DIV.MsoNormal {  MARGIN: 0cm 0cm 0pt; FONT-FAMILY: \"Times New Roman\",\"serif\"; FONT-SIZE: 12pt;}\r\n"
        + " A:link {  COLOR: blue; TEXT-DECORATION: underline; }\r\n"
        + " SPAN.MsoHyperlink {  COLOR: blue; TEXT-DECORATION: underline; }\r\n"
        + " A:visited {  COLOR: purple; TEXT-DECORATION: underline; }\r\n"
        + " SPAN.MsoHyperlinkFollowed {  COLOR: purple; TEXT-DECORATION: underline; }\r\n"
        + " P {  FONT-FAMILY: \"Times New Roman\",\"serif\"; MARGIN-LEFT: 0cm; FONT-SIZE: 12pt; MARGIN-RIGHT: 0cm;   }\r\n"
        + " P.MsoAcetate {  MARGIN: 0cm 0cm 0pt; FONT-FAMILY: \"Tahoma\",\"sans-serif\"; FONT-SIZE: 8pt;  }\r\n";
}
