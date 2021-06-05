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

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
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
    @Before
    public void setUp() throws Exception {
        super.setUp();
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

        folderAndID = getClient().execute(new SendRequest(mailObject_string)).getFolderAndID();

    }

    public Bug12409Test() {
        super();
    }

    @Test
    public void testSavedDispositionNotificationReturnedWhenEditing() throws IOException, JSONException, OXException {
        // load the email to edit it again
        GetResponse response;
        response = getClient().execute(new GetRequest(folderAndID[0], folderAndID[1]));
        // verify that the delivery receipt option is still set
        assertTrue("Disposition notification was not saved.", response.getMail(getTimeZone()).getDispositionNotification().toString().equals("testmail@example.invalid"));
    }

}
