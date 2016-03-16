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
 * {@link Bug24899Test}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug24899Test extends AbstractSanitizing {
    @Test
    public void testHonorBaseTag() {
        String content = "<html>\n" +
            "<head>\n" +
            " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
            " <title>Sofortige Benachrichtigung per E-Mail</title>\n" +
            " <base href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731\">\n" +
            " <style type=\"text/css\">\n" +
            "   img { border: none }\n" +
            " </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "Diese Nachricht wurde automatisch erzeugt und vom BSCW-System,\n" +
            "<a href=\"https://bscw.ilmenau.baw.de\">https://bscw.ilmenau.baw.de</a>\n" +
            "an Sie versandt.\n" +
            "\n" +
            "<h1>Sofortige Benachrichtigung per E-Mail</h1>\n" +
            "<H2>Aktivit\u00e4ten in <I>IT-Koordinatoren WSV</I></H2>\n" +
            "<dl><dt><a href=\"867920?op=inf&amp;back_object=107728\"><img alt=\"Mehr Information\" title=\"Mehr Information\" src=\"/bscw_resources/120420-1504-25392/icons/info.gif\" class=\"plain inlist\"  /></a> <a href=\"867920\"><img alt=\"Ordner\" title=\"Ordner\" src=\"/bscw_resources/120420-1504-25392/icons/s_folder.gif\" class=\"plain inlist\"  /></a><strong><a href=\"867920\">IT-Koordinatoren WSV</a></strong></dt><dd>\n" +
            "\n" +
            "<dl><dt><a href=\"17607708?op=inf&amp;back_object=107728\"><img alt=\"Mehr Information\" title=\"Mehr Information\" src=\"/bscw_resources/120420-1504-25392/icons/info.gif\" class=\"plain inlist\"  /></a> <a href=\"17607708\"><img alt=\"Ordner\" title=\"Ordner\" src=\"/bscw_resources/120420-1504-25392/icons/s_folder.gif\" class=\"plain inlist\"  /></a><strong><a href=\"17607708\">Besprechungen_2013</a></strong></dt><dd>\n" +
            "<img alt=\"neu\" title=\"neu\" src=\"/bscw_resources/120420-1504-25392/icons/e_new.gif\" class=\"plain inlist\"  /> erzeugt von <a href=\"221894?op=inf&amp;back_object=107728\"><em>Moebes</em></a>, 2013-01-23&nbsp;12:16\n" +
            "<dl><dt><a href=\"17607718?op=inf&amp;back_object=107728\"><img alt=\"Mehr Information\" title=\"Mehr Information\" src=\"/bscw_resources/120420-1504-25392/icons/info.gif\" class=\"plain inlist\"  /></a> <a href=\"17607718\"><img alt=\"Ordner\" title=\"Ordner\" src=\"/bscw_resources/120420-1504-25392/icons/s_folder.gif\" class=\"plain inlist\"  /></a><strong><a href=\"17607718\">01-2013</a></strong></dt><dd>\n" +
            "<img alt=\"neu\" title=\"neu\" src=\"/bscw_resources/120420-1504-25392/icons/e_new.gif\" class=\"plain inlist\"  /> erzeugt von <a href=\"221894?op=inf&amp;back_object=107728\"><em>Moebes</em></a>, 2013-01-23&nbsp;12:17\n" +
            "</dd></dl>\n" +
            "</dd></dl>\n" +
            "</dd></dl>\n" +
            "<hr />\n" +
            "<h5>Die Nachricht wurde erzeugt, weil Sie f\u00fcr das betreffende Objekt\n" +
            "und die aktuelle Aktivit\u00e4t eine sofortige Benachrichtigung per E-Mail\n" +
            "abonniert haben. Falls Sie weitere Nachrichten dieser Art vermeiden\n" +
            "wollen, \u00e4ndern Sie bitte die Ereignis-Einstellung des betreffenden\n" +
            "Ordners oder Objekts. Sie k\u00f6nnen die direkte Benachrichtigung auch\n" +
            "generell ausschalten unter <em>Optionen &gt; Ereignisse</em>\n" +
            "im oberen Men\u00fc der BSCW-Benutzerschnittstelle.</h5>\n" +
            "</body>\n" +
            "</html>";

        String expected = "<html>\n" +
            "<head>\n" +
            " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
            " <title>Sofortige Benachrichtigung per E-Mail</title>\n" +
            " \n" +
            " <style type=\"text/css\">\n" +
            "   img { border: none }\n" +
            " </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "Diese Nachricht wurde automatisch erzeugt und vom BSCW-System,\n" +
            "<a href=\"https://bscw.ilmenau.baw.de\">https://bscw.ilmenau.baw.de</a>\n" +
            "an Sie versandt.\n" +
            "\n" +
            "<h1>Sofortige Benachrichtigung per E-Mail</h1>\n" +
            "<H2>Aktivit\u00e4ten in <I>IT-Koordinatoren WSV</I></H2>\n" +
            "<dl><dt><a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/867920?op=inf&amp;back_object=107728\"\"><img alt=\"Mehr Information\" title=\"Mehr Information\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/info.gif\"\" class=\"plain inlist\"  /></a> <a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/867920\"\"><img alt=\"Ordner\" title=\"Ordner\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/s_folder.gif\"\" class=\"plain inlist\"  /></a><strong><a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/867920\"\">IT-Koordinatoren WSV</a></strong></dt><dd>\n" +
            "\n" +
            "<dl><dt><a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/17607708?op=inf&amp;back_object=107728\"\"><img alt=\"Mehr Information\" title=\"Mehr Information\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/info.gif\"\" class=\"plain inlist\"  /></a> <a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/17607708\"\"><img alt=\"Ordner\" title=\"Ordner\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/s_folder.gif\"\" class=\"plain inlist\"  /></a><strong><a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/17607708\"\">Besprechungen_2013</a></strong></dt><dd>\n" +
            "<img alt=\"neu\" title=\"neu\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/e_new.gif\"\" class=\"plain inlist\"  /> erzeugt von <a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/221894?op=inf&amp;back_object=107728\"\"><em>Moebes</em></a>, 2013-01-23&nbsp;12:16\n" +
            "<dl><dt><a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/17607718?op=inf&amp;back_object=107728\"\"><img alt=\"Mehr Information\" title=\"Mehr Information\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/info.gif\"\" class=\"plain inlist\"  /></a> <a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/17607718\"\"><img alt=\"Ordner\" title=\"Ordner\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/s_folder.gif\"\" class=\"plain inlist\"  /></a><strong><a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/17607718\"\">01-2013</a></strong></dt><dd>\n" +
            "<img alt=\"neu\" title=\"neu\" src=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/bscw_resources/120420-1504-25392/icons/e_new.gif\"\" class=\"plain inlist\"  /> erzeugt von <a href=\"https://bscw.ilmenau.baw.de/bscw/bscw.cgi/107731/221894?op=inf&amp;back_object=107728\"\"><em>Moebes</em></a>, 2013-01-23&nbsp;12:17\n" +
            "</dd></dl>\n" +
            "</dd></dl>\n" +
            "</dd></dl>\n" +
            "<hr />\n" +
            "<h5>Die Nachricht wurde erzeugt, weil Sie f\u00fcr das betreffende Objekt\n" +
            "und die aktuelle Aktivit\u00e4t eine sofortige Benachrichtigung per E-Mail\n" +
            "abonniert haben. Falls Sie weitere Nachrichten dieser Art vermeiden\n" +
            "wollen, \u00e4ndern Sie bitte die Ereignis-Einstellung des betreffenden\n" +
            "Ordners oder Objekts. Sie k\u00f6nnen die direkte Benachrichtigung auch\n" +
            "generell ausschalten unter <em>Optionen &gt; Ereignisse</em>\n" +
            "im oberen Men\u00fc der BSCW-Benutzerschnittstelle.</h5>\n" +
            "</body>\n" +
            "</html>";

        String ret = getHtmlService().checkBaseTag(content, false);

        assertEquals("Unexpected return value ", expected, ret);
    }
}
