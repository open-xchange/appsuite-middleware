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

package com.openexchange.ajax.onboarding.tests;

import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.springframework.util.xml.StaxUtils;
import org.xml.sax.XMLReader;
import com.openexchange.testing.httpclient.modules.ClientonboardingApi;

/**
 * {@link DownloadTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class DownloadTest extends AbstractOnboardingTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testDownloadCalDAVProfile() throws Exception {
        ClientonboardingApi api = getApi();
        byte[] response = api.downloadClientOnboardingProfile(getSessionId(), "caldav");
        XMLReader profile = parseProfile(response);
        String payloadId = (String) profile.getProperty("PayloadIdentifier");
        String payloadType = (String) profile.getProperty("PayloadType");
        String payloadVersion = (String) profile.getProperty("PayloadVersion");
        String payloadDisplayName = (String) profile.getProperty("PayloadDisplayName");
        assertEquals("com.open-xchange.caldav", payloadId);
        assertEquals("Configuration", payloadType);
        assertEquals("1", payloadVersion);
        assertEquals("caldav", payloadDisplayName);
    }

    @Test
    public void testDownloadCardDAVProfile() throws Exception {
        ClientonboardingApi api = getApi();
        byte[] response = api.downloadClientOnboardingProfile(getSessionId(), "carddav");
        XMLReader profile = parseProfile(response);
        String payloadId = (String) profile.getProperty("PayloadIdentifier");
        String payloadType = (String) profile.getProperty("PayloadType");
        String payloadVersion = (String) profile.getProperty("PayloadVersion");
        String payloadDisplayName = (String) profile.getProperty("PayloadDisplayName");
        assertEquals("com.open-xchange.carddav", payloadId);
        assertEquals("Configuration", payloadType);
        assertEquals("1", payloadVersion);
        assertEquals("carddav", payloadDisplayName);
    }

    private XMLReader parseProfile(byte[] response) throws XMLStreamException, UnsupportedEncodingException {
        String xmlString = new String(response, "UTF-8");
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(xmlString));
        return StaxUtils.createXMLReader(reader);
    }

}
