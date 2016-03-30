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
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.ajax.mail.actions.UpdateMailRequest;
import com.openexchange.ajax.mail.actions.UpdateMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link UpdateMailTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdateMailTest extends AbstractMailTest {

    private UserValues values;

    public UpdateMailTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
        clearFolder(getSentFolder());
        clearFolder(getInboxFolder());
    }

    @Override
    protected void tearDown() throws Exception {
//        clearFolder( values.getSentFolder() );
//        clearFolder( values.getInboxFolder() );
        super.tearDown();
    }

    public void testShouldBeAbleToAddFlags() throws OXException, IOException, SAXException, JSONException{
        final String eml =
            "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.";
        NewMailRequest newMailRequest = new NewMailRequest(values.getInboxFolder(), eml, -1, true);
        NewMailResponse newMailResponse = getClient().execute(newMailRequest);
        String folder = newMailResponse.getFolder();
        String id = newMailResponse.getId();

        UpdateMailRequest updateRequest = new UpdateMailRequest( folder, id );
        final int additionalFlag = MailMessage.FLAG_ANSWERED; //note: doesn't work for 16 (recent) and 64 (user)
        updateRequest.setFlags( additionalFlag );
        updateRequest.updateFlags();
        UpdateMailResponse updateResponse = getClient().execute(updateRequest);

        TestMail updatedMail = getMail(folder, id);
        assertTrue("Flag should have been changed, but are: " + Integer.toBinaryString(updatedMail.getFlags()), (updatedMail.getFlags() & additionalFlag) == additionalFlag);

        updateRequest = new UpdateMailRequest( folder, id );
        updateRequest.setFlags( additionalFlag );
        updateRequest.removeFlags();
        updateResponse = getClient().execute(updateRequest);

        updatedMail = getMail(folder, id);
        assertTrue("Flag should have been changed back again, but are: " + Integer.toBinaryString(updatedMail.getFlags()), (updatedMail.getFlags() & additionalFlag) == 0);
    }

    public void testShouldBeAbleToAddFlags2AllMessages() throws OXException, IOException, SAXException, JSONException {
        String newId = null;
        try {
            /*
             * Create new mail folder
             */
            {
                final FolderObject fo = new FolderObject();
                {
                    final String inboxFolder = values.getInboxFolder();
                    final String name = "TestFolder" + System.currentTimeMillis();
                    final String fullName = inboxFolder + "/" + name;
                    fo.setFullName(fullName);
                    fo.setFolderName(name);
                }
                fo.setModule(FolderObject.MAIL);

                final OCLPermission oclP = new OCLPermission();
                oclP.setEntity(client.getValues().getUserId());
                oclP.setGroupPermission(false);
                oclP.setFolderAdmin(true);
                oclP.setAllPermission(
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION);
                fo.setPermissionsAsArray(new OCLPermission[] { oclP });

                final InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, fo);
                final InsertResponse response = client.execute(request);

                newId = (String) response.getResponse().getData();
            }
            /*
             * Append mails to new folder
             */
            {
                final String eml =
                    "Message-Id: <4A002517.4650.0059.1@deployfast.com>\n" +
                    "X-Mailer: Novell GroupWise Internet Agent 8.0.0 \n" +
                    "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
                    "From: " + getSendAddress() + "\n" +
                    "To: " + getSendAddress() + "\n" +
                    "Subject: Re: Your order for East Texas Lighthouse\n" +
                    "Mime-Version: 1.0\n" +
                    "Content-Type: text/plain; charset=\"UTF-8\"\n" +
                    "Content-Transfer-Encoding: 8bit\n" +
                    "\n" +
                    "This is a MIME message. If you are reading this text, you may want to \n" +
                    "consider changing to a mail reader or gateway that understands how to \n" +
                    "properly handle MIME multipart messages.";

                for (int i = 0; i < 10; i++) {
                    final NewMailRequest newMailRequest = new NewMailRequest(newId, eml, -1, true);
                    final NewMailResponse newMailResponse = getClient().execute(newMailRequest);
                    final String folder = newMailResponse.getFolder();
                    assertNotNull("Missing folder in response.", folder);
                    assertNotNull("Missing ID in response.", newMailResponse.getId());
                    assertEquals("Folder ID mismatch in newly appended message.", newId, folder);
                }
            }
            /*
             * Perform batch update call
             */
            final int flag = MailMessage.FLAG_ANSWERED;
            {
                final UpdateMailRequest updateRequest = new UpdateMailRequest(newId);
                final int additionalFlag = flag; // note: doesn't work for 16 (recent) and 64 (user)
                updateRequest.setFlags(additionalFlag);
                updateRequest.updateFlags();
                final UpdateMailResponse updateResponse = getClient().execute(updateRequest);
                assertEquals("Folder ID mismatch.", newId, updateResponse.getFolder());
            }
            /*
             * Check
             */
            {
                final AllRequest allRequest =
                    new AllRequest(
                        newId,
                        new int[] { MailListField.ID.getField(), MailListField.FLAGS.getField() },
                        MailSortField.RECEIVED_DATE.getField(),
                        Order.ASCENDING,
                        true);
                final AllResponse allResponse = getClient().execute(allRequest);
                final Object[][] array = allResponse.getArray();
                for (final Object[] arr : array) {
                    final Integer flags = (Integer) arr[1];
                    assertTrue("\\Seen flag not set for message " + arr[0] + " in folder " + newId, (flags.intValue() & flag) > 0);
                }
            }

        } finally {
            if (null != newId) {
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, newId, new Date());
                    client.execute(deleteRequest);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void testShouldBeAbleToAddColorLabel2AllMessages() throws OXException, IOException, SAXException, JSONException {
        String newId = null;
        try {
            /*
             * Create new mail folder
             */
            {
                final FolderObject fo = new FolderObject();
                {
                    final String inboxFolder = values.getInboxFolder();
                    final String name = "TestFolder" + System.currentTimeMillis();
                    final String fullName = inboxFolder + "/" + name;
                    fo.setFullName(fullName);
                    fo.setFolderName(name);
                }
                fo.setModule(FolderObject.MAIL);

                final OCLPermission oclP = new OCLPermission();
                oclP.setEntity(client.getValues().getUserId());
                oclP.setGroupPermission(false);
                oclP.setFolderAdmin(true);
                oclP.setAllPermission(
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION);
                fo.setPermissionsAsArray(new OCLPermission[] { oclP });

                final InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, fo);
                final InsertResponse response = client.execute(request);

                newId = (String) response.getResponse().getData();
            }
            /*
             * Append mails to new folder
             */
            {
                final String eml =
                    "Message-Id: <4A002517.4650.0059.1@deployfast.com>\n" +
                    "X-Mailer: Novell GroupWise Internet Agent 8.0.0 \n" +
                    "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
                    "From: " + getSendAddress() + "\n" +
                    "To: " + getSendAddress() + "\n" +
                    "Subject: Re: Your order for East Texas Lighthouse\n" +
                    "Mime-Version: 1.0\n" +
                    "Content-Type: text/plain; charset=\"UTF-8\"\n" +
                    "Content-Transfer-Encoding: 8bit\n" +
                    "\n" +
                    "This is a MIME message. If you are reading this text, you may want to \n" +
                    "consider changing to a mail reader or gateway that understands how to \n" +
                    "properly handle MIME multipart messages.";

                for (int i = 0; i < 10; i++) {
                    final NewMailRequest newMailRequest = new NewMailRequest(newId, eml, -1, true);
                    final NewMailResponse newMailResponse = getClient().execute(newMailRequest);
                    final String folder = newMailResponse.getFolder();
                    assertNotNull("Missing folder in response.", folder);
                    assertNotNull("Missing ID in response.", newMailResponse.getId());
                    assertEquals("Folder ID mismatch in newly appended message.", newId, folder);
                }
            }
            /*
             * Perform batch update call
             */
            final int colorLable = 5;
            {
                final UpdateMailRequest updateRequest = new UpdateMailRequest(newId);
                updateRequest.setColor(colorLable);
                final UpdateMailResponse updateResponse = getClient().execute(updateRequest);
                assertEquals("Folder ID mismatch.", newId, updateResponse.getFolder());
            }
            /*
             * Check
             */
            {
                final AllRequest allRequest =
                    new AllRequest(
                        newId,
                        new int[] { MailListField.ID.getField(), MailListField.COLOR_LABEL.getField() },
                        MailSortField.RECEIVED_DATE.getField(),
                        Order.ASCENDING,
                        true);
                final AllResponse allResponse = getClient().execute(allRequest);
                final Object[][] array = allResponse.getArray();
                for (final Object[] arr : array) {
                    final Integer label = (Integer) arr[1];
                    assertEquals("Color label not set for message " + arr[0] + " in folder " + newId, colorLable, label.intValue());
                }
            }

        } finally {
            if (null != newId) {
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, newId, new Date());
                    client.execute(deleteRequest);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notestShouldBeAbleToAddFlagsByMessageId() throws OXException, IOException, SAXException, JSONException{
        final String mail = values.getSendAddress();
        sendMail( createEMail(mail, "Update test for adding and removing a flag by message id", "ALTERNATE", "Just a little bit").toString() );
        final TestMail myMail = new TestMail( getFirstMailInFolder(values.getInboxFolder() ) );

        final String messageId;
        {
            final Object obj = myMail.getHeader("Message-ID");
            if (null == obj) {
                messageId = null;
            } else {
                messageId = obj.toString();
            }
        }
        assertNotNull("Message-ID header not found.", messageId);


        final UpdateMailRequest updateRequest = new UpdateMailRequest( myMail.getFolder(), messageId ).setMessageId(true);
        final int additionalFlag = MailMessage.FLAG_ANSWERED; //note: doesn't work for 16 (recent) and 64 (user)
        updateRequest.setFlags( additionalFlag );
        updateRequest.updateFlags();
        UpdateMailResponse updateResponse = getClient().execute(updateRequest);

        TestMail updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertTrue("Flag should have been changed", (updatedMail.getFlags() & additionalFlag) == additionalFlag);

        updateRequest.removeFlags();
        updateResponse = getClient().execute(updateRequest);

        updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertTrue("Flag should have been changed back again", (updatedMail.getFlags() & additionalFlag) == 0);
    }


    public void notestShouldBeAbleToSetColors() throws OXException, IOException, SAXException, JSONException{
        final String mail = values.getSendAddress();
        sendMail( createEMail(mail, "Update test for changing colors", "ALTERNATE", "Just a little bit").toString() );
        final TestMail myMail = new TestMail( getFirstMailInFolder(values.getInboxFolder() ) );

        final UpdateMailRequest updateRequest = new UpdateMailRequest( myMail.getFolder(), myMail.getId() );
        int myColor = 8;
        updateRequest.setColor(myColor );
        UpdateMailResponse updateResponse = getClient().execute(updateRequest);

        TestMail updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertEquals("Color should have been changed", myColor, updatedMail.getColor());

        myColor = 4;
        updateRequest.setColor(myColor );
        updateResponse = getClient().execute(updateRequest);

        updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertEquals("Color should have been changed again", myColor, updatedMail.getColor());
    }
}
