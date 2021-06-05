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

package com.openexchange;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * {@link TestServletTest}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class TestServletTest {

    private final String URL = "http://localhost/servlet/TestServlet";

    private final String PUTSTRING = "A P\u00dcT String with Umlaut";

    @Test
    public void testGetMethod() throws Exception {
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(URL);
        WebResponse response = conversation.getResponse(request);
        response.getElementsByTagName("p");
    }

    @Test
    public void testPutMethod() throws IOException, SAXException {
        WebConversation conversation = new WebConversation();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(PUTSTRING.getBytes("UTF-8"));
        WebRequest request = new PutMethodWebRequest("http://localhost/servlet/TestServlet", byteArrayInputStream, "text/xml");
        WebResponse response = conversation.getResponse(request);
        HTMLElement[] paragraphs = response.getElementsByTagName("p");
        assertEquals("The transfered content differs", "The content: " + PUTSTRING, paragraphs[4].getText());
    }

    /**
     * Test if we can send a get request with more than the allowed max amount of parameters (30 by default).
     * Expects an HttpInternalErrorException when the requests receives a 500 because of too many parameters.
     * 
     * @throws Exception
     */
    @Test
    public void testMaxParam() throws Exception {
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(URL);
        for (int i = 0; i < 31; i++) {
            request.setParameter("param" + i, "value" + i);
        }
        conversation.getResponse(request);
    }

}
