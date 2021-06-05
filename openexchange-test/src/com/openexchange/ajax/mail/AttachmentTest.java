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
import static org.junit.Assert.fail;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AttachmentRequest;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link AttachmentTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AttachmentTest extends AbstractMailTest {

    /**
     * Default constructor.
     *
     * @param name
     *            Name of this test.
     */
    public AttachmentTest() {
        super();
    }

    /**
     * Tests the <code>action=attachment</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testGet() throws Throwable {
        String[] folderAndID = null;
        try {
            {
                /*
                 * Create JSON mail object
                 */
                final String mailObject_25kb = createSelfAddressed25KBMailObject().toString();
                /*
                 * Insert mail through a send request
                 */
                folderAndID = Executor.execute(getSession(), new SendRequest(mailObject_25kb)).getFolderAndID();
            }
            /*
             * Perform action=get
             */
            final GetResponse response = Executor.execute(getSession(), new GetRequest(folderAndID[0], folderAndID[1]));
            /*
             * Get mail's JSON representation
             */
            final JSONObject mailObject = (JSONObject) response.getResponse().getData();
            /*
             * Some assertions
             */
            assertTrue("Missing field " + MailJSONField.ATTACHMENTS.getKey(), mailObject.has(MailJSONField.ATTACHMENTS.getKey()) && !mailObject.isNull(MailJSONField.ATTACHMENTS.getKey()));
            final JSONArray attachmentArray = mailObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
            final int len = attachmentArray.length();
            assertTrue("Missing attachments", len > 0);
            /*
             * Iterate over attachments
             */
            String sequenceId = null;
            for (int i = 0; i < len && sequenceId == null; i++) {
                final JSONObject attachmentObject = attachmentArray.getJSONObject(i);
                final String contentType = attachmentObject.getString(MailJSONField.CONTENT_TYPE.getKey());
                if (contentType.regionMatches(true, 0, "text/htm", 0, 8)) {
                    sequenceId = attachmentObject.getString(MailListField.ID.getKey());
                }
            }
            assertTrue("No HTML part found", sequenceId != null);
            /*
             * Perform action=attachment
             */
            final AttachmentRequest attachmentRequest = new AttachmentRequest(folderAndID[0], folderAndID[1], sequenceId);
            attachmentRequest.setSaveToDisk(false);
            attachmentRequest.setFilter(true);
            final WebResponse webResponse = Executor.execute4Download(getSession(), attachmentRequest, AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL), AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
            /*
             * Some assertions
             */
            assertTrue("Web response does not indicate HTML content", webResponse.isHTML());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (folderAndID != null) {
                final String[][] foo = new String[1][];
                foo[0] = folderAndID;
                Executor.execute(getSession(), new DeleteRequest(foo, true));
            }
        }
    }
}
