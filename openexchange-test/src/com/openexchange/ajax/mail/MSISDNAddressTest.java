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

package com.openexchange.ajax.mail;

import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.ajax.user.actions.UpdateRequest;
import com.openexchange.ajax.user.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.mail.MailJSONField;

/**
 * {@link MSISDNFromTest} MSISDN is a number uniquely identifying a subscription in a GSM or a UMTS mobile network. Simply put, it is the
 * telephone number to the SIM card in a mobile/cellular phone. This abbreviation has several interpretations, the most common one being
 * "Mobile Subscriber Integrated Services Digital Network-Number" Some ISPs allow MSISDNS as email sender addresses. This test verifies that
 * our sender address handling in the backend is able to use MSISDNS specified in the users contact object within the phone number fields.
 * MSISDN numbers are allowed to consist of up to 15 digits and are formed by three pieces Country Code + National Destination Code +
 * Subscriber Number
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class MSISDNAddressTest extends AbstractMailTest {

    private UserValues userValues = null;

    private Contact contactData;

    private String originalCellPhoneNumber;

    private final String validTestCellPhoneNumber = "491401234567890";

    private final String invalidTestCellPhoneNumber = "491501234567890";

    public MSISDNAddressTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userValues = client.getValues();
        // get the current contactData and
        GetResponse response = client.execute(new GetRequest(userValues.getUserId(), userValues.getTimeZone()));
        contactData = response.getContact();
        originalCellPhoneNumber = contactData.getCellularTelephone1();
        setCellularNumberOfContact(validTestCellPhoneNumber);
    }

    @Override
    protected void tearDown() throws Exception {
        // reset to original Number if tests expect this
        setCellularNumberOfContact(originalCellPhoneNumber);
        super.tearDown();
    }

    private void setCellularNumberOfContact(String cellPhoneNumber) throws OXException, IOException, JSONException {
        Contact changedContactData = new Contact();
        changedContactData.setObjectID(contactData.getObjectID());
        changedContactData.setInternalUserId(contactData.getInternalUserId());
        changedContactData.setCellularTelephone1(validTestCellPhoneNumber);
        changedContactData.setLastModified(new Date());
        UpdateRequest updateRequest = new UpdateRequest(changedContactData, null);
        UpdateResponse updateResponse = client.execute(updateRequest);
        // successful update returns only a timestamp
        Date timestamp = updateResponse.getTimestamp();
        assertNotNull(timestamp);
    }

    /*
     * Send an e-mail with the msisdn we just set in the contact
     */
    public void testValidFromAddress() throws OXException, IOException, JSONException, SAXException {
        JSONObject createEMail = createEMail(
            getSendAddress(client),
            "MSISDNSubject",
            MailContentType.PLAIN.name(),
            "Testing MSISDN as sender address");
        createEMail.put(MailJSONField.FROM.getKey(), validTestCellPhoneNumber);
        SendRequest request = new SendRequest(createEMail.toString());
        SendResponse response = client.execute(request);
        assertTrue("Send request failed", response.getFolderAndID() != null && response.getFolderAndID().length > 0);
    }

    public void testInvalidFromAddress() throws OXException, IOException, JSONException, SAXException {
        System.out.println("Testing invalid");
        JSONObject createEMail = createEMail(
            getSendAddress(getClient()),
            "MSISDNSubject",
            MailContentType.PLAIN.name(),
            "Testing MSISDN as sender address");
        createEMail.put(MailJSONField.FROM.getKey(), invalidTestCellPhoneNumber);
        SendRequest request = new SendRequest(createEMail.toString(), false);
        SendResponse response = client.execute(request);
        assertTrue(response.getException() != null);
        assertEquals(OXException.CATEGORY_USER_INPUT, response.getException().getCategory());
        assertEquals(response.getErrorMessage(), "MSG-0056", response.getException().getErrorCode());
    }
}
