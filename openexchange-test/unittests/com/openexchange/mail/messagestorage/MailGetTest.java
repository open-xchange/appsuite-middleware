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

package com.openexchange.mail.messagestorage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.activation.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;

/**
 * {@link MailGetTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public final class MailGetTest extends MessageStorageTest {

    private class TestMailMessage extends MailMessage {

        /**
         * For serialization
         */
        private static final long serialVersionUID = 4645951099640670488L;


        @Override
        public String getMailId() {
            // Nothing to do
            return null;
        }

        @Override
        public int getUnreadMessages() {
            // Nothing to do
            return 0;
        }

        @Override
        public void setMailId(final String id) {
            // Nothing to do

        }

        @Override
        public void setUnreadMessages(final int unreadMessages) {
            // Nothing to do

        }

        @Override
        public Object getContent() throws OXException {
            // Nothing to do
            return null;
        }

        @Override
        public DataHandler getDataHandler() throws OXException {
            // Nothing to do
            return null;
        }

        @Override
        public int getEnclosedCount() throws OXException {
            // Nothing to do
            return 0;
        }

        @Override
        public MailPart getEnclosedMailPart(final int index) throws OXException {
            // Nothing to do
            return null;
        }

        @Override
        public InputStream getInputStream() throws OXException {
            return new ByteArrayInputStream(brokenContentTypeMail.getBytes());
        }

        @Override
        public void loadContent() throws OXException {
            // Nothing to do

        }

        @Override
        public void prepareForCaching() {
            // Nothing to do

        }

    }

    private static char linebreak = '\n';
    private final String brokenContentTypeMail = "Return-Path: <schweigi@open-xchange.com>" + linebreak +
        "Date: Thu, 20 Sep 2007 11:01:25 +0200" + linebreak +
        "From: Thomas Schweiger <schweigi@open-xchange.com>" + linebreak +
        "To: Thomas Schweiger <schweigi@open-xchange.com>" + linebreak +
        "Subject: test PGP signed mail" + linebreak +
        "Message-ID: <20070920090125.GA12567@open-xchange.com>" + linebreak +
        "Mime-Version: 1.0" + linebreak +
        "Content-Type: multipart/signed; micalg=pgp-sha1; protocol=\"application/pgp-signature\" boundary=\"mP3DRpeJDSE+ciuQ\"" + linebreak +
        "Content-Disposition: inline" + linebreak +
        "X-Operating-System: SUSE LINUX" + linebreak +
        "X-Mailer: Open-Xchange v6.0 Console Mailer" + linebreak +
        "X-PGP-Key: 1024D/D532F2E8" + linebreak +
        "X-PGP-Fingerprint: 815B 2A54 E23A FEF9 1AED 6CF9 2603 813F D532 F2E8" + linebreak +
        "X-Message-Flag: \"Es ist Wahnsinn, das Leben so zu sehen wie es ist, anstatt es zu sehen wie es sein sollte.\"" + linebreak +
        "X-Private-URL1: http://www.schweigisito.de" + linebreak +
        "X-Private-URL2: http://www.straight-live.de" + linebreak +
        linebreak +
        linebreak +
        "--mP3DRpeJDSE+ciuQ" + linebreak +
        "Content-Type: text/plain; charset=us-ascii" + linebreak +
        "Content-Disposition: inline" + linebreak +
        + linebreak +
        "This mail contains a PGP signature." + linebreak +
        linebreak +
        linebreak +
        "--mP3DRpeJDSE+ciuQ" + linebreak +
        "Content-Type: application/pgp-signature" + linebreak +
        "Content-Disposition: inline" + linebreak +
        linebreak +
        "-----BEGIN PGP SIGNATURE-----" + linebreak +
        "Version: GnuPG v1.4.2 (GNU/Linux)" + linebreak +
        linebreak +
        "iD8DBQFG8jblJgOBP9Uy8ugRAp4+AJ9iAZcBh6ke0zrqkrtLMWH+QKfTGgCffF+5" + linebreak +
        "F2P9TrHERgiiyRTA6x6BR2U=" + linebreak +
        "=Wk8C" + linebreak +
        "-----END PGP SIGNATURE-----" + linebreak +
        linebreak +
        "--mP3DRpeJDSE+ciuQ--" + linebreak;

    /**
     *
     */
    public MailGetTest() {
        super();
    }

    private static final MailField[] FIELDS_ID = { MailField.ID };

    private static final MailField[] FIELDS_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS,
            MailField.BODY };

    private static final MailField[] FIELDS_EVEN_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS,
            MailField.FROM, MailField.TO, MailField.DISPOSITION_NOTIFICATION_TO, MailField.COLOR_LABEL,
            MailField.HEADERS, MailField.SUBJECT, MailField.THREAD_LEVEL, MailField.SIZE, MailField.PRIORITY,
            MailField.SENT_DATE, MailField.RECEIVED_DATE, MailField.CC, MailField.BCC, MailField.FOLDER_ID };

    private static final MailField[] FIELDS_FULL = { MailField.FULL };

    public void testMailGetNotExistingMails() throws OXException {
        try {
            final MailMessage message = mailAccess.getMessageStorage().getMessage("INBOX", String.valueOf(System.currentTimeMillis()), true);
            assertTrue("The message for an invalid id must be null", null == message);
        } catch (final Exception e) {
            fail("getMessage throws an exception: " + e.getMessage());
        }
    }

    public void testMailGetNotExistingFolder() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            assertNull("No mail should be returned on a invalid folder", mailAccess.getMessageStorage().getMessage("Ichbinnichda1337", String.valueOf(System.currentTimeMillis()), true));
        } catch (final OXException e) {
            assertTrue("Wrong Exception is thrown.", e.getErrorCode().endsWith("-1002"));
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailGet() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertFalse("Mail ID is -1", fetchedMails[i].getMailId() == null);
            }

            fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_MORE);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
                assertTrue("Missing content type", fetchedMails[i].containsContentType());
                assertTrue("Missing flags", fetchedMails[i].containsFlags());
                if (fetchedMails[i].getContentType().isMimeType("multipart/*")) {
                    assertFalse("Enclosed count returned -1", fetchedMails[i].getEnclosedCount() == -1);
                } else {
                    assertFalse("Content is null", fetchedMails[i].getContent() == null);
                }
            }

            fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_EVEN_MORE);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
                assertTrue("Missing content type", fetchedMails[i].containsContentType());
                assertTrue("Missing flags", fetchedMails[i].containsFlags());
                assertTrue("Missing From", fetchedMails[i].containsFrom());
                assertTrue("Missing To", fetchedMails[i].containsTo());
                assertTrue("Missing Disposition-Notification-To", fetchedMails[i].containsDispositionNotification());
                assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
                assertTrue("Missing headers", fetchedMails[i].containsHeaders());
                assertTrue("Missing subject", fetchedMails[i].containsSubject());
                assertTrue("Missing thread level", fetchedMails[i].containsThreadLevel());
                assertTrue("Missing size", fetchedMails[i].containsSize());
                assertTrue("Missing priority", fetchedMails[i].containsPriority());
                assertTrue("Missing sent date", fetchedMails[i].containsSentDate());
                assertTrue("Missing received date", fetchedMails[i].containsReceivedDate());
                assertTrue("Missing Cc", fetchedMails[i].containsCc());
                assertTrue("Missing Bcc", fetchedMails[i].containsBcc());
                assertTrue("Missing folder fullname", fetchedMails[i].containsFolder());
            }

            fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_FULL);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
                assertTrue("Missing content type", fetchedMails[i].containsContentType());
                assertTrue("Missing flags", fetchedMails[i].containsFlags());
                assertTrue("Missing From", fetchedMails[i].containsFrom());
                assertTrue("Missing To", fetchedMails[i].containsTo());
                assertTrue("Missing Disposition-Notification-To", fetchedMails[i].containsDispositionNotification());
                assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
                assertTrue("Missing headers", fetchedMails[i].containsHeaders());
                assertTrue("Missing subject", fetchedMails[i].containsSubject());
                assertTrue("Missing thread level", fetchedMails[i].containsThreadLevel());
                assertTrue("Missing size", fetchedMails[i].containsSize());
                assertTrue("Missing priority", fetchedMails[i].containsPriority());
                assertTrue("Missing sent date", fetchedMails[i].containsSentDate());
                assertTrue("Missing received date", fetchedMails[i].containsReceivedDate());
                assertTrue("Missing Cc", fetchedMails[i].containsCc());
                assertTrue("Missing Bcc", fetchedMails[i].containsBcc());
                assertTrue("Missing folder fullname", fetchedMails[i].containsFolder());
                assertTrue("Missing account name", fetchedMails[i].containsAccountName());
                if (fetchedMails[i].getContentType().isMimeType("multipart/*")) {
                    assertFalse("Enclosed count returned -1", fetchedMails[i].getEnclosedCount() == -1);
                } else {
                    assertFalse("Content is null", fetchedMails[i].getContent() == null);
                }
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    // Added for bug 14276
    public void testMailGetBrokenContentTypeList() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", new MailMessage[]{new TestMailMessage()});
        try {
            final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_MORE);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
                assertTrue("Missing content type", fetchedMails[i].containsContentType());
                assertTrue("Missing flags", fetchedMails[i].containsFlags());
                assertTrue("Message must contain attachment information", fetchedMails[i].containsHasAttachment());
                if (fetchedMails[i].getContentType().startsWith("multipart/")) {
                    assertTrue("Message is of type '"+fetchedMails[i].getContentType()+"' but signals no attachment", fetchedMails[i].hasAttachment());
                } else {
                    assertTrue("Message is not of type multipart/*, but signals to hold attachments", !fetchedMails[i].hasAttachment());
                }
            }

        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailGetBrokenContentTypeGet() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", new MailMessage[]{new TestMailMessage()});
        try {
            final MailMessage fetchedMail = mailAccess.getMessageStorage().getMessage("INBOX", uids[0], true);
            assertFalse("Missing mail ID", fetchedMail.getMailId() == null);
            assertTrue("Missing content type", fetchedMail.containsContentType());
            assertTrue("Missing flags", fetchedMail.containsFlags());
            assertTrue("Message must contain attachment information", fetchedMail.containsHasAttachment());
            if (fetchedMail.getContentType().startsWith("multipart/")) {
                assertTrue("Message is of type '"+fetchedMail.getContentType()+"' but signals no attachment", fetchedMail.hasAttachment());
            } else {
                assertTrue("Message is not of type multipart/*, but signals to hold attachments", !fetchedMail.hasAttachment());
            }

        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

}
