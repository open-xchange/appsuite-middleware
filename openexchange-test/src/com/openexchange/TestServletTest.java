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

    private final String QUERYSTRING = "?";

    @Test
    public void testGetMethod() throws Exception {
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(URL);
        WebResponse response = conversation.getResponse(request);
        HTMLElement[] paragraphs = response.getElementsByTagName("p");
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
     * @throws Exception
     */
    @Test(expected = com.meterware.httpunit.HttpInternalErrorException.class)
    public void testMaxParam() throws Exception {
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(URL);
        for (int i = 0; i <31; i++) {
            request.setParameter("param" + i, "value" + i);
        }
        WebResponse response = conversation.getResponse(request);
        int responseCode = response.getResponseCode();
        // System.out.println(responseCode);
    }

}
