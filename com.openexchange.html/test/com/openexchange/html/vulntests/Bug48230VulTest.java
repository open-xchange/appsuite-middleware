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
     public void testDataUriSanitizing() throws Exception {
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
        if (Strings.isNotEmpty(sanitized)) {
            sanitized = sanitized.toLowerCase();
        }
        assertTrue("Sanitzed content does no more contain data URI for JPEG image:\n" + sanitized, sanitized.indexOf("data:image/") >= 0);
    }
}
