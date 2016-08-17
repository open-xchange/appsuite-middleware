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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.html.bugtests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;


/**
 * {@link Bug19428Test}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug19428Test extends AbstractSanitizing {
    @Test
    public void testGetConformHtml() {

        String content = "<html>\n" +
            "<head>\n" +
            "        \n" +
            "        <!-- \n" +
            "            Diese Internet-Praesenz wird mit INQUIRE gepflegt.\n" +
            "            INQUIRE ist ein Produkt von STAGEx aus Paderborn.\n" +
            "            Weitere Informationen erhalten Sie unter http://www.stage-x.de.\n" +
            "        -->\n" +
            "      \n" +
            "    <title>BVMW- Bundesverband mittelst&auml;ndische Wirtschaft</title>\n" +
            "    <link rel=\"stylesheet\" href=\"https://bvmw.stage-x.de/styles/stageNeu.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://www.stage-x.de/styles/newsletter.css\">\n" +
            "\n" +
            "</head>\n" +
            "<body bgcolor=\"#ffffff\" link=\"#C30000\" vlink=\"#C30000\" alink=\"#C30000\">\n" +
            "    <center>\n" +
            "    <table style=\"border: 2px solid #C6C4C5;\" cellpadding=\"0\" cellspacing=\"0\" width=\"700\">\n" +
            "        <tr>\n" +
            "            <td colspan=\"3\">\n" +
            "                 <img style=\"margin-bottom: 20px;\" src=\"https://bvmw.stage-x.de/vorlagen/images/head.jpg\" border=0 />\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td width=\"10\">\n" +
            "                &nbsp;\n" +
            "            </td>\n" +
            "\n" +
            "            <td>\n" +
            "                <br />\n" +
            "        <div class=\"h1\">\n" +
            "    <h1 class=\"red\">\n" +
            "        <center>Business Lunch</center>\n" +
            "    </h1>\n" +
            "</div><div class=\"hr\">\n" +
            "  <hr />\n" +
            "<div><h2 style=\"font: bold 14px/14px arial,helvetica,sans-serif; color: #000000; margin: 10px 0 10px 0; padding: 0;\">Udo Wiemann l\u00e4dt ein zum</h2><br /><center><table border=0 cellspacing=0 cellpadding=0 width=690><tr><td style=\"padding-right: 0px;\" valign=top><img width=\"140\" src=\"https://bvmw.stage-x.de/uploads/_newsletter/Foto Udo NL.jpg\" class=\"withBorder\" /></td>\n" +
            "<td valign=top><p class=\"text\" style=\"text-align: justify;\"><b>Business Lunch</b><br />\n" +
            "am Mittwoch, 15. Juni 2011<br />\n" +
            "von 12:00 Uhr bis 14:00 Uhr<br />\n" +
            "<br />\n" +
            "<br />\n" +
            "<br />\n" +
            "<b>Veranstaltungsort</b><br />\n" +
            "Driburg Therme GmbH<br />\n" +
            "Georg-Nave-Stra\u00dfe 24<br />\n" +
            "33014 Bad Driburg</p></td>\n" +
            "</tr></table></center><div style=\"margin-top: 20px;\">&nbsp;</div><div class=\"h1\">\n" +
            "    <h1 class=\"original\">\n" +
            "        Pers\u00f6nliche Einladung zum Business Lunch am 15 .Juni 2011 in das Thermalbad \u201eDriburg Therme\u201c\n" +
            "    </h1>\n" +
            "</div><font>Sehr geehrter Herr Thiet,</font><div class=\"text\">\n" +
            "    <p class=\"original\">\n" +
            "        in regelm\u00e4\u00dfigen Abst\u00e4nden treffen wir uns in einem kleinen Kreis von 12 Unternehmerinnen und Unternehmern zu einem Strategiegespr\u00e4ch, in dem ein aktuelles Thema aufgegriffen und diskutiert wird.<br />\n" +
            "<br />\n" +
            "Unser aktuelles Thema:<br />\n" +
            "<br />\n" +
            "<b>Werbung - effektiv, modern und strategisch</b><br />\n" +
            "- Die Qual der Wahl: Twitter, Facebook & Co. -  oder eher klassisch? -<br />\n" +
            "<br />\n" +
            "Diskutieren Sie mit uns die zuk\u00fcnftigen Herausforderungen und M\u00f6glichkeiten, die Unternehmen im harten Wettbewerb um Absatzm\u00e4rkte bew\u00e4ltigen m\u00fcssen. Als ein mit entscheidendes Kriterium wird die Positionierung des Unternehmens im Markt und die Darstellung der Dienstleistung und/oder der anzubietenden Produkte angesehen. Welche Werbebotschaften erreichen welche Kundengruppen und welche Rolle spielen die neuen Kommunikationsformen?<br />\n" +
            "<br />\n" +
            "Diese und andere Fragen m\u00f6chten wir mit Ihnen bei einem excellenten Men\u00fc im Ambiente des Thermalbades diskutieren.<br />\n" +
            "\n" +
            "    </p>\n" +
            "</div><div style=\"margin-left:0px;\"><p class=\"text\">Ich freue mich auf Sie!<br> Ihr</p>\n" +
            "<img style=\"margin-top:-15px; margin-bottom:-15px;\" border=0 width=160 src=https://bvmw.stage-x.de/vorlagen/images/SignatureWiemann.jpg />\n" +
            "<p class=\"text\">Udo Wiemann<br>BVMW Kreisgesch\u00e4ftsf\u00fchrer<br>Paderborn&#8226;H\u00f6xter&#8226;G\u00fctersloh</p></div><span><b>Die Teilnahme an dieser Veranstaltung ist f\u00fcr Sie kostenfrei.</b></span><div style=\"margin-top: 30px;\">&nbsp;</div><a href=\"https://bvmw.stage-x.de/versions/eventmanagement/anmeldung/v1.0/anmeldung.php?ref=02e253a6d618f5bb63be3a33904bc3e23cec07e9ba5f5bb252d13f5f431e4bbb\" title=\"Zur Eventanmeldung\" target=\"_blank\"><center>Zur Online - Anmeldung</center><br />\n" +
            "</a><div style=\"margin-top: 30px;\">&nbsp;</div>\n" +
            "            </td>\n" +
            "            <td width=\"10\">\n" +
            "                &nbsp;\n" +
            "            </td>\n" +
            "\n" +
            "        </tr>\n" +
            "        <tr style=\"background-color: #C6C4C5;\">\n" +
            "            <td style=\"color: black; text-align: center; font-family: arial, helvetica, sans-serif; font-size: 12px;\" colspan=\"3\">\n" +
            "                Bundesverband mittelst&auml;ndische Wirtschaft | Driburger Str. 42 | 33100 Paderborn | <a style=\"font-family: arial, helvetica,sans-serif; font-weight: normal; font-size: 12px; color: black;\" href=\"http://www.bvmw.de/?id=769\">www.bvmw.de</a><br />\n" +
            "                Telefon: (05251) 6860367 | Telefax: (05251) 6862328 | E-Mail: <a style=\"font-family: arial, helvetica,sans-serif; font-weight: normal; font-size: 12px; color: black;\" href=\"mailto:erika.schumacher@bvmw.de\">erika.schumacher@bvmw.de</a>\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "    </table>\n" +
            "    <font class=\"abmeldetext\" style=\"width: 150px; padding: 10px 0 0 10px;\">Der Newsletter wurde von uns an <span class=\"linktext\">h.thiet@mahlmann.biz</span> gesendet. Um sich abzumelden, klicken Sie bitte <a href=\"https://bvmw.stage-x.de/wb_newsletter.php?e=22111|ap1,1971,0\" target=_blank><span class=\"linktext\">hier</span></a></font>\n" +
            "    </center>\n" +
            "</body>";

        String expectedConformHtml = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n\n" +
            "        \n" +
            "        <!-- \n" +
            "            Diese Internet-Praesenz wird mit INQUIRE gepflegt.\n" +
            "            INQUIRE ist ein Produkt von STAGEx aus Paderborn.\n" +
            "            Weitere Informationen erhalten Sie unter http://www.stage-x.de.\n" +
            "        -->\n" +
            "      \n" +
            "    <title>BVMW- Bundesverband mittelst&#228;ndische Wirtschaft</title>\n" +
            "    <link rel=\"stylesheet\" href=\"https://bvmw.stage-x.de/styles/stageNeu.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://www.stage-x.de/styles/newsletter.css\">\n" +
            "\n" +
            "\n" +
            "</head><body bgcolor=\"#ffffff\" link=\"#C30000\" vlink=\"#C30000\" alink=\"#C30000\">\n" +
            "\n" +
            "    <center>\n" +
            "    <table style=\"border: 2px solid #C6C4C5;\" cellpadding=\"0\" cellspacing=\"0\" width=\"700\">\n" +
            "        <tbody><tr>\n" +
            "            <td colspan=\"3\">\n" +
            "                 <img style=\"margin-bottom: 20px;\" src=\"https://bvmw.stage-x.de/vorlagen/images/head.jpg\" border=\"0\">\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td width=\"10\">\n" +
            "                &#160;\n" +
            "            </td>\n" +
            "\n" +
            "            <td>\n" +
            "                <br>\n" +
            "        <div class=\"h1\">\n" +
            "    <h1 class=\"red\">\n" +
            "        <center>Business Lunch</center>\n" +
            "    </h1>\n" +
            "</div><div class=\"hr\">\n" +
            "  <hr>\n" +
            "<div><h2 style=\"font: bold 14px/14px arial,helvetica,sans-serif; color: #000000; margin: 10px 0 10px 0; padding: 0;\">Udo Wiemann l&#228;dt ein zum</h2><br><center><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"690\"><tbody><tr><td style=\"padding-right: 0px;\" valign=\"top\"><img width=\"140\" src=\"https://bvmw.stage-x.de/uploads/_newsletter/Foto Udo NL.jpg\" class=\"withBorder\"></td>\n" +
            "<td valign=\"top\"><p class=\"text\" style=\"text-align: justify;\"><b>Business Lunch</b><br>\n" +
            "am Mittwoch, 15. Juni 2011<br>\n" +
            "von 12:00 Uhr bis 14:00 Uhr<br>\n" +
            "<br>\n" +
            "<br>\n" +
            "<br>\n" +
            "<b>Veranstaltungsort</b><br>\n" +
            "Driburg Therme GmbH<br>\n" +
            "Georg-Nave-Stra&#223;e 24<br>\n" +
            "33014 Bad Driburg</p></td>\n" +
            "</tr></tbody></table></center><div style=\"margin-top: 20px;\">&#160;</div><div class=\"h1\">\n" +
            "    <h1 class=\"original\">\n" +
            "        Pers&#246;nliche Einladung zum Business Lunch am 15 .Juni 2011 in das Thermalbad &#8222;Driburg Therme&#8220;\n" +
            "    </h1>\n" +
            "</div><font>Sehr geehrter Herr Thiet,</font><div class=\"text\">\n" +
            "    <p class=\"original\">\n" +
            "        in regelm&#228;&#223;igen Abst&#228;nden treffen wir uns in einem kleinen Kreis von 12 Unternehmerinnen und Unternehmern zu einem Strategiegespr&#228;ch, in dem ein aktuelles Thema aufgegriffen und diskutiert wird.<br>\n" +
            "<br>\n" +
            "Unser aktuelles Thema:<br>\n" +
            "<br>\n" +
            "<b>Werbung - effektiv, modern und strategisch</b><br>\n" +
            "- Die Qual der Wahl: Twitter, Facebook &#38; Co. -  oder eher klassisch? -<br>\n" +
            "<br>\n" +
            "Diskutieren Sie mit uns die zuk&#252;nftigen Herausforderungen und M&#246;glichkeiten, die Unternehmen im harten Wettbewerb um Absatzm&#228;rkte bew&#228;ltigen m&#252;ssen. Als ein mit entscheidendes Kriterium wird die Positionierung des Unternehmens im Markt und die Darstellung der Dienstleistung und/oder der anzubietenden Produkte angesehen. Welche Werbebotschaften erreichen welche Kundengruppen und welche Rolle spielen die neuen Kommunikationsformen?<br>\n" +
            "<br>\n" +
            "Diese und andere Fragen m&#246;chten wir mit Ihnen bei einem excellenten Men&#252; im Ambiente des Thermalbades diskutieren.<br>\n" +
            "\n" +
            "    </p>\n" +
            "</div><div style=\"margin-left:0px;\"><p class=\"text\">Ich freue mich auf Sie!<br> Ihr</p>\n" +
            "<img style=\"margin-top:-15px; margin-bottom:-15px;\" border=\"0\" width=\"160\" src=\"https://bvmw.stage-x.de/vorlagen/images/SignatureWiemann.jpg\">\n" +
            "<p class=\"text\">Udo Wiemann<br>BVMW Kreisgesch&#228;ftsf&#252;hrer<br>Paderborn&#8226;H&#246;xter&#8226;G&#252;tersloh</p></div><span><b>Die Teilnahme an dieser Veranstaltung ist f&#252;r Sie kostenfrei.</b></span><div style=\"margin-top: 30px;\">&#160;</div><a href=\"https://bvmw.stage-x.de/versions/eventmanagement/anmeldung/v1.0/anmeldung.php?ref=02e253a6d618f5bb63be3a33904bc3e23cec07e9ba5f5bb252d13f5f431e4bbb\" title=\"Zur Eventanmeldung\" target=\"_blank\"><center>Zur Online - Anmeldung</center><br>\n" +
            "</a><div style=\"margin-top: 30px;\">&#160;</div>\n" +
            "            </div></div></td>\n" +
            "            <td width=\"10\">\n" +
            "                &#160;\n" +
            "            </td>\n" +
            "\n" +
            "        </tr>\n" +
            "        <tr style=\"background-color: #C6C4C5;\">\n" +
            "            <td style=\"color: black; text-align: center; font-family: arial, helvetica, sans-serif; font-size: 12px;\" colspan=\"3\">\n" +
            "                Bundesverband mittelst&#228;ndische Wirtschaft | Driburger Str. 42 | 33100 Paderborn | <a style=\"font-family: arial, helvetica,sans-serif; font-weight: normal; font-size: 12px; color: black;\" href=\"http://www.bvmw.de/?id=769\">www.bvmw.de</a><br>\n" +
            "                Telefon: (05251) 6860367 | Telefax: (05251) 6862328 | E-Mail: <a style=\"font-family: arial, helvetica,sans-serif; font-weight: normal; font-size: 12px; color: black;\" href=\"mailto:erika.schumacher@bvmw.de\">erika.schumacher@bvmw.de</a>\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "    </tbody></table>\n" +
            "    <font class=\"abmeldetext\" style=\"width: 150px; padding: 10px 0 0 10px;\">Der Newsletter wurde von uns an <span class=\"linktext\">h.thiet@mahlmann.biz</span> gesendet. Um sich abzumelden, klicken Sie bitte <a href=\"https://bvmw.stage-x.de/wb_newsletter.php?e=22111|ap1,1971,0\" target=\"_blank\"><span class=\"linktext\">hier</span></a></font>\n" +
            "    </center>\n" +
            "</body></html>\n ";

        String ret = getHtmlService().getConformHTML(content, "UTF-8");
        assertEquals("Unexpected return value", expectedConformHtml, ret);
    }
}
