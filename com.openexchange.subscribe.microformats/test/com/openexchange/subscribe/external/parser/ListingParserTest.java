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

package com.openexchange.subscribe.external.parser;

import java.util.List;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.external.ExternalSubscriptionSource;
import com.openexchange.subscribe.microformats.parser.HTMLMicroformatParserFactory;


/**
 * {@link ListingParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ListingParserTest extends TestCase {

    private static final String LISTING1 =
        "<html>\n\n"+
        "   <head>\n"+
        "        <title>Subscription Sources</title>\n"+
        "    </head>\n"+
        "    <body>\n"+
        "        <h1>Available Subscription Sources</h1>\n"+
        "        \n"+
        "        <div class=\"ox_subscriptionSource\">\n"+
        "            <h3 class=\"ox_displayName\">Rally Tasks</h3>"+
        "            <img src=\"http://localhost/icon.png\" class=\"ox_icon\"></img>\n"+
        "            <span class=\"ox_sourceId\">com.openexchange.subscribe.tasks.rally</span>\n"+
        "            <span class=\"ox_module\">tasks</span>\n"+
        "            <a href=\"http://localhost/rally.php\" class=\"ox_link\">Rally Tasks</a>\n"+
        "        </div>\n"+
        "\n"+
        "        <div class=\"ox_subscriptionSource\">\n"+
        "            <h3 class=\"ox_displayName\">Flicker</h3>"+
        "            <img src=\"http://localhost/icon2.png\" class=\"ox_icon\"></img>\n"+
        "            <span class=\"ox_sourceId\">com.openexchange.subscribe.infostore.flicker</span>\n"+
        "            <span class=\"ox_module\">infostore</span>\n"+
        "            <a href=\"http://localhost/flicker/oxmf.html\" class=\"ox_link\">Flicker</a>\n"+
        "        </div>\n"+
        "    \n"+
        "    </body>\n"+
        "</html>";



    public void testParseCompleteListing() throws OXException {
        final List<ExternalSubscriptionSource> externalSources = new ListingParser(new HTMLMicroformatParserFactory()).parse(LISTING1);

        assertNotNull(externalSources);
        assertEquals(2, externalSources.size());

        boolean foundA = false, foundB = false;

        for (final ExternalSubscriptionSource source : externalSources) {
            if("com.openexchange.subscribe.tasks.rally".equals(source.getId())) {
                assertEquals("http://localhost/icon.png", source.getIcon());
                assertEquals(FolderObject.TASK, source.getFolderModule());
                assertEquals("Rally Tasks", source.getDisplayName());
                assertEquals("http://localhost/rally.php", source.getExternalAddress());
                foundA = true;
            } else if ("com.openexchange.subscribe.infostore.flicker".equals(source.getId())) {
                assertEquals("http://localhost/icon2.png", source.getIcon());
                assertEquals(FolderObject.INFOSTORE, source.getFolderModule());
                assertEquals("Flicker", source.getDisplayName());
                assertEquals("http://localhost/flicker/oxmf.html", source.getExternalAddress());
                foundB = true;
            } else {
                fail("Don't know subscription source id "+source.getId());
            }
        }

        assertTrue(foundA);
        assertTrue(foundB);
    }


}
