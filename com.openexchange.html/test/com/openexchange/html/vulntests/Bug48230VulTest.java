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

package com.openexchange.html.vulntests;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.AssertionHelper;
import com.openexchange.java.Strings;

/**
 * {@link Bug48230VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug48230VulTest extends AbstractSanitizing {

    public Bug48230VulTest() {
        super();
    }

    @Test
    public void testDataUriSanitizing() {
        String content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head>"
            + "<body>"
            + "<a href=\"data:text/html;base64,PHNjcmlwdD5wcm9tcHQoZG9jdW1lbnQuY29va2llKTwvc2NyaXB0Pg==\">XSS</a>"
            + "</body></html>";
        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "data:text/html");

        content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head>"
            + "<body>"
            + "<a href=data::text%2fhtml;base64,PHNjcmlwdD5wcm9tcHQoZG9jdW1lbnQuY29va2llKTwvc2NyaXB0Pg==>xss</a>"
            + "</body></html>";
        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "data:text/html");

        content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head>"
            + "<body>"
            + "<img src=\"data:image/svg;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/Pg0KPCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4NCg0KPHN2ZyB2ZXJzaW9uPSIxLjEiIGJhc2VQcm9maWxlPSJmdWxsIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPg0KICAgPHBvbHlnb24gaWQ9InRyaWFuZ2xlIiBwb2ludHM9IjAsMCAwLDUwIDUwLDAiIGZpbGw9IiMwMDk5MDAiIHN0cm9rZT0iIzAwNDQwMCIvPg0KICAgPHNjcmlwdCB0eXBlPSJ0ZXh0L2phdmFzY3JpcHQiPg0KICAgICAgYWxlcnQoZG9jdW1lbnQuZG9tYWluKTsNCiAgIDwvc2NyaXB0Pg0KPC9zdmc+\">"
            + "</body></html>";
        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, "data:image/");

        content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head>"
            + "<body>"
            + "<img src=\"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4ge4SUNDX1BST0ZJTEUAAQEAAAeoYXBwb...inCBARRRTSsBKEwOtOAA6UUUwFooooAKKKKAP/Z\">"
            + "</body></html>";
        String sanitized = getHtmlService().sanitize(content, null, false, null, null);
        if (!Strings.isEmpty(sanitized)) {
            sanitized = sanitized.toLowerCase();
        }
        assertTrue("Sanitzed content does no more contain data URI for JPEG image:\n" + sanitized, sanitized.indexOf("data:image/") >= 0);
    }
}
