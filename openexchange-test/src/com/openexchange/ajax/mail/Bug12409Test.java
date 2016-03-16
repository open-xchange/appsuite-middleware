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
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * Title of Bug: The option "Delivery receipt" is no longer checked when editing a draft
 *
 * Description of Bug: When loading a draft with this option enabled the field
 * "Disposition-Notification-To" is missing in the server response. But it's
 * needed to re-select this field again.
 *
 * Steps to Reproduce:
 * 1. Create a new e-mail and check "Delivery receipt"
 * 2. Save the message as draft
 * 3. Open the saved draft
 * 4. Check the field "Delivery receipt"
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 *
 */
public class Bug12409Test extends AbstractMailTest {

	private String[] folderAndID;

	@Override
    public void setUp() throws Exception {
		super.setUp();
		final AJAXClient client = getClient();
		// clean the drafts folder
		clearFolder(getDraftsFolder());
		// create an email
		JSONObject mailObject = createSelfAddressed25KBMailObject();
		// set the delivery-receipt / disposition-notification option
		mailObject.put(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey(), "testmail@example.invalid");
		// mark it as a draft
		mailObject.put(MailJSONField.FLAGS.getKey(), Integer.toString(MailMessage.FLAG_DRAFT));
		// convert it to a string for the SaveRequest
		final String mailObject_string = mailObject.toString();

		folderAndID = client.execute(new SendRequest(mailObject_string)).getFolderAndID();


	}

	@Override
	public void tearDown() throws Exception {
		// clean the drafts folder
		clearFolder(getDraftsFolder());
		super.tearDown();
	}
	public Bug12409Test(final String name) {
		super(name);
	}

	public void testSavedDispositionNotificationReturnedWhenEditing() throws IOException, SAXException, JSONException, OXException {
		final AJAXClient client = getClient();
		// load the email to edit it again
		GetResponse response;
		response = client.execute(new GetRequest(folderAndID[0], folderAndID[1]));
		// verify that the delivery receipt option is still set
		assertTrue("Disposition notification was not saved.", response.getMail(getTimeZone()).getDispositionNotification().toString().equals("testmail@example.invalid"));
	}

}
