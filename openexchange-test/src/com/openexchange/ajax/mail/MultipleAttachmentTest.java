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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.MultipleAttachmentRequest;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link MultipleAttachmentTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MultipleAttachmentTest extends AbstractMailTest {

    /**
     * Default constructor.
     *
     * @param name
     *            Name of this test.
     */
    public MultipleAttachmentTest() {
        super();
    }

    /**
     * Tests the <code>action=zip_attachments</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testGet() throws Throwable {
        String[] folderAndID = null;
        try {
            {
                final String eml = ("Date: Mon, 19 Nov 2012 21:36:51 +0100 (CET)\n" + "From: #ADDR#\n" + "To: #ADDR#\n" + "Message-ID: <1508703313.17483.1353357411049>\n" + "Subject: MultipleAttachmentTest\n" + "MIME-Version: 1.0\n" + "Content-Type: multipart/alternative; \n" + "    boundary=\"----=_Part_17482_1388684087.1353357411002\"\n" + "\n" + "------=_Part_17482_1388684087.1353357411002\n" + "MIME-Version: 1.0\n" + "Content-Type: text/plain; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n" + "MultipleAttachmentTest\n" + "------=_Part_17482_1388684087.1353357411002\n" + "MIME-Version: 1.0\n" + "Content-Type: text/html; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n" + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">" + " <head>\n" + "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n" + " </head><body style=\"font-family: verdana,geneva; font-size: 10pt; \">\n" + " \n" + "  <div>\n" + "   MultipleAttachmentTest\n" + "  </div>\n" + " \n" + "</body></html>\n" + "------=_Part_17482_1388684087.1353357411002--\n").replaceAll("#ADDR#", getSendAddress());
                NewMailResponse newMailResponse = getClient().execute(new NewMailRequest(getClient().getValues().getInboxFolder(), eml, -1, true));
                String folder = newMailResponse.getFolder();
                String id = newMailResponse.getId();
                folderAndID = new String[] { folder, id };
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
            final MultipleAttachmentRequest attachmentRequest = new MultipleAttachmentRequest(folderAndID[0], folderAndID[1], new String[] { sequenceId });
            final WebResponse webResponse = Executor.execute4Download(getSession(), attachmentRequest, AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL), AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
            /*
             * Some assertions
             */
            assertFalse("Web response does indicate HTML content", webResponse.isHTML());
            assertEquals("No ZIP content", "application/zip", webResponse.getContentType());
            final String disp = webResponse.getHeaderField("Content-disposition");
            assertNotNull("No Content-disposition header", disp);
            // Behavior changed with bug 26879
            //assertTrue("Disposition is not set to 'attachment'", disp.startsWith("attachment"));
            assertTrue("'filename' parameter not found in Content-disposition", disp.indexOf("filename=") >= 0);

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
