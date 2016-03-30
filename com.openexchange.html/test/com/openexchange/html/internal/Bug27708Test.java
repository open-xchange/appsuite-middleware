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

package com.openexchange.html.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;


/**
 * {@link Bug27708Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug27708Test extends TestCase {

    private static final int NUM_THREADS = 20;
    private static final int NUM_RUNS = 100;
    private static final Pattern uidPattern = Pattern.compile(
        "\\b[A-F0-9]{8}(?:-[A-F0-9]{4}){3}-[A-Z0-9]{12}\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Initializes a new {@link Bug27708Test}.
     */
    public Bug27708Test() {
        super();
    }

    public void testThreadSafety() throws Exception {
        Thread[] threads = new Thread[NUM_THREADS];
        CheckHtmlCleaner[] runnables = new CheckHtmlCleaner[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            runnables[i] = new CheckHtmlCleaner();
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
            if (false == runnables[i].errors.isEmpty()) {
                throw runnables[i].errors.get(0);
            }
        }
    }

    private static final class CheckHtmlCleaner implements Runnable {

        boolean onlyDetectForeignMarkers = false;
        List<AssertionError> errors = new ArrayList<AssertionError>();

        @Override
        public void run() {
            for (int i = 0; i < NUM_RUNS; i++) {
                String marker = UUID.randomUUID().toString();
                String html =
                      "<html>\n"
                    + " <head>\n"
                    + "  <style type=\"text/css\">.mceResizeHandle {position: absolute;border: 1px solid black;background: #FFF;width: 5px;height: 5px;z-index: 10000}.mceResizeHandle:hover {background: #000}img[data-mce-selected] {outline: 1px solid black}img.mceClonedResizable, table.mceClonedResizable {position: absolute;outline: 1px dashed black;opacity: .5;z-index: 10000}\n"
                    + "</style>\n"
                    + " </head>\n"
                    + " <body style=\"\">\n"
                    + "  <div>\n"
                    + "   wurst\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   gurke\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   hund\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   " + marker +"\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   autobahn\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   suppe\n"
                    + "  </div> \n"
                    + "  <div>\n"
                    + "   &nbsp;\n"
                    + "  </div>\n"
                    + " </body>\n"
                    + "</html>"
                ;

                try {
                    String cleanedHtml = HtmlServiceImpl.validateWithHtmlCleaner(html);
                    assertNotNull(cleanedHtml);
                    Matcher matcher = uidPattern.matcher(cleanedHtml);
                    if (onlyDetectForeignMarkers) {
                        if (matcher.find()) {
                            assertEquals("Cleaned HTML contains foreign marker", marker, matcher.group());
                        }
                    } else {
                        assertTrue("Cleaned HTML contains no marker", matcher.find());
                        assertEquals("Cleaned HTML contains foreign marker", marker, matcher.group());
                        assertTrue("Cleaned HTML appears to be too short", cleanedHtml.length() > 600);
                        assertTrue("Cleaned HTML appears to be too long", cleanedHtml.length() < 900);
                    }
                } catch (AssertionError e) {
                    errors.add(e);
                    break;
                } catch (RuntimeException e) {
                    // we want to find assertion errors
                    continue;
                }
            }
        }
    }

}
