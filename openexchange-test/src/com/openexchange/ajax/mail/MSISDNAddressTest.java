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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.mail;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailJSONField;

/**
 * {@link MSISDNFromTest} MSISDN is a number uniquely identifying a subscription in a GSM or a UMTS mobile network. Simply put, it is the
 * telephone number to the SIM card in a mobile/cellular phone. This abbreviation has several interpretations, the most common one being
 * "Mobile Subscriber Integrated Services Digital Network-Number" Some ISPs allow MSISDNS as email sender addresses. This test verifies that
 * our sender address handling in the backend is able to use MSISDNS specified in the users contact object within the phone number fields.
 * 
 * MSISDN numbers are allowed to consist of up to 15 digits and are formed by three pieces
 * Country Code + National Destination Code + Subscriber Number
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class MSISDNAddressTest extends AbstractMailTest {

    public MSISDNAddressTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testValidFromAddress() throws OXException, IOException, JSONException, SAXException {
        JSONObject createEMail = createEMail(
            getSendAddress(getClient()),
            "MSISDNSubject",
            MailContentType.PLAIN.name(),
            "Testing MSISDN as sender address");
        createEMail.put(MailJSONField.FROM.getKey(), "491701234567890");
        SendRequest request = new SendRequest(createEMail.toString());
        SendResponse response = client.execute(request);
        assertTrue("Send request failed", response.getFolderAndID() != null && response.getFolderAndID().length > 0);
    }
    
    public void testInvalidFromAddress() throws OXException, IOException, JSONException, SAXException {
        JSONObject createEMail = createEMail(
            getSendAddress(getClient()),
            "MSISDNSubject",
            MailContentType.PLAIN.name(),
            "Testing MSISDN as sender address");
        createEMail.put(MailJSONField.FROM.getKey(), "491711234567890");
        SendRequest request = new SendRequest(createEMail.toString(),false);
        SendResponse response = client.execute(request);
        assertTrue(response.getException() != null);
        assertEquals(OXException.CATEGORY_USER_INPUT, response.getException().getCategory());
        assertEquals("The specified E-Mail address 491711234567890 is not covered by allowed E-Mail address aliases.",response.getErrorMessage());
    }
}
