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
import com.openexchange.html.AssertionHelper;

/**
 * {@link Bug26153Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug26153Test extends AbstractSanitizing {

    @Test
    public void testDocumentize() {
        String htmlContent = "<p>my html document without html, or body tags</p>";
        String actual = getHtmlService().documentizeContent(htmlContent, "UTF-8");
        StringBuilder expectedBuilder = new StringBuilder();
        expectedBuilder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        expectedBuilder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        expectedBuilder.append("<head>\n");
        expectedBuilder.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
        expectedBuilder.append("</head>\n");
        expectedBuilder.append("<body>\n");
        expectedBuilder.append("<p>my html document without html, or body tags</p></body>\n");
        expectedBuilder.append("</html>\n");
        String expected = expectedBuilder.toString();

        AssertionHelper.assertTag("!DOCTYPE", actual, false);
        AssertionHelper.assertTag("<html", actual, true);
        AssertionHelper.assertTag("<body>", actual, true);
        AssertionHelper.assertTag("<meta", actual, false);

        assertEquals("Unexpected output", expected, actual);
    }

}
