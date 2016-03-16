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

package com.openexchange.ajax.conversion;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.mail.FolderAndID;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.ajax.mail.netsol.actions.NetsolDeleteRequest;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.mail.MailJSONField;

/**
 * {@link VCardMailPartAttachTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class VCardMailPartAttachTest extends AbstractConversionTest {

    /**
     * Initializes a new {@link VCardMailPartAttachTest}
     *
     * @param name
     *            The name
     */
    public VCardMailPartAttachTest(final String name) {
        super(name);
    }

    /**
     * Tests the <code>action=convert</code> request
     *
     * @throws Throwable
     */
    public void testVCardAttach() throws Throwable {
        try {
            /*
             * Find a valid contact
             */
            final int objectId;
            final int folderId = getPrivateContactFolder();
            final CommonAllResponse allR = Executor.execute(getSession(), new AllRequest(folderId,
                    new int[] { DataObject.OBJECT_ID }));
            final ListIDs listIDs = allR.getListIDs();
            if (listIDs.size() == 0) {
                /*
                 * TODO: Create a contact and remember its object-id
                 */
                objectId = -1;

            } else {
                objectId = Integer.parseInt(listIDs.get(0).getObject());
            }

            String[] mailFolderAndMailID = null;
            try {

                /*
                 * Create a mail
                 */
                final JSONObject mailObject_25kb = new JSONObject();
                {
                    mailObject_25kb.put(MailJSONField.FROM.getKey(), getClient().getValues().getSendAddress());
                    mailObject_25kb.put(MailJSONField.RECIPIENT_TO.getKey(), getClient().getValues().getSendAddress());
                    mailObject_25kb.put(MailJSONField.RECIPIENT_CC.getKey(), "");
                    mailObject_25kb.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
                    mailObject_25kb.put(MailJSONField.SUBJECT.getKey(), "The mail subject");
                    mailObject_25kb.put(MailJSONField.PRIORITY.getKey(), "3");

                    final JSONObject bodyObject = new JSONObject();
                    bodyObject.put(MailJSONField.CONTENT_TYPE.getKey(), "text/html");
                    bodyObject.put(MailJSONField.CONTENT.getKey(), "Mail body text for test: "
                            + VCardMailPartAttachTest.class.getName() + "<br>ENJOY!");

                    final JSONArray attachments = new JSONArray();
                    attachments.put(bodyObject);

                    mailObject_25kb.put(MailJSONField.ATTACHMENTS.getKey(), attachments);
                }
                /*
                 * Add data source
                 */
                {
                    final JSONObject jsonSource = new JSONObject().put("identifier", "com.openexchange.contact");
                    jsonSource.put("args", new JSONArray().put(new JSONObject().put(
                            "com.openexchange.groupware.contact.pairs", new JSONArray().put(
                                    new JSONObject().put(AJAXServlet.PARAMETER_FOLDERID, folderId).put(
                                            AJAXServlet.PARAMETER_ID, objectId)).toString())));
                    mailObject_25kb.put(MailJSONField.DATASOURCES.getKey(), new JSONArray().put(jsonSource));
                }
                /*
                 * Perform transport
                 */
                final SendResponse response = Executor.execute(getSession(),
                    new SendRequest(mailObject_25kb.toString()));
                assertTrue("Send failed", response.getFolderAndID() != null);
                assertTrue("Duration corrupt", response.getRequestDuration() > 0);
                mailFolderAndMailID = response.getFolderAndID();
                try {
                    Long.parseLong(mailFolderAndMailID[1]);
                } catch (final NumberFormatException e) {
                    int pos = mailFolderAndMailID[1].lastIndexOf('/');
                    if (pos == -1) {
                        pos = mailFolderAndMailID[1].lastIndexOf('.');
                        if (pos == -1) {
                            fail("UNKNOWN FORMAT FOR MAIL ID: " + mailFolderAndMailID[1]);
                        }
                    }
                    final String substr = mailFolderAndMailID[1].substring(pos + 1);
                    try {
                        Long.parseLong(substr);
                    } catch (final NumberFormatException e1) {
                        fail("UNKNOWN FORMAT FOR MAIL ID: " + mailFolderAndMailID[1]);
                    }
                    mailFolderAndMailID[1] = substr;
                }

                /*
                 * Get previously sent mail
                 */
                final GetResponse resp = Executor.execute(getSession(),
                    new GetRequest(mailFolderAndMailID[0], mailFolderAndMailID[1], true));
                final JSONObject fetchedMailObject = (JSONObject) resp.getData();

                assertNotNull("Missing JSON mail object", fetchedMailObject);
                assertTrue("JSON mail object misses field: " + MailJSONField.ATTACHMENTS.getKey(), fetchedMailObject
                        .has(MailJSONField.ATTACHMENTS.getKey())
                        && !fetchedMailObject.isNull(MailJSONField.ATTACHMENTS.getKey()));
                final JSONArray attachmentArray = fetchedMailObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
                assertTrue("Unexpected number of attachments in JSON mail object: " + attachmentArray.length(), attachmentArray.length() >= 2);
                StringBuilder sb = new StringBuilder();
                boolean foundAttachment = false;
                for (int i = 0; i < attachmentArray.length() && !foundAttachment; i++) {
                    final JSONObject vcardAttachmentObject = attachmentArray.getJSONObject(i);
                    String contentType = vcardAttachmentObject.getString(MailJSONField.CONTENT_TYPE.getKey());
                    foundAttachment = contentType.startsWith("text/vcard");
                    if (!foundAttachment) {
                        sb.append(contentType);
                        sb.append(' ');
                    }
                }
                assertTrue("Can't find vCard attachment. Found content-types: " + sb, foundAttachment);
            } finally {
                if (mailFolderAndMailID != null) {
                    final FolderAndID mailPath = new FolderAndID(mailFolderAndMailID[0], mailFolderAndMailID[1]);
                    Executor.execute(getSession(), new NetsolDeleteRequest(new FolderAndID[] { mailPath }, true));
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
}
