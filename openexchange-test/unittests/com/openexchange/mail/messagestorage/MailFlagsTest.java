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

import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailFlagsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public final class MailFlagsTest extends MessageStorageTest {

    /**
	 *
	 */
    public MailFlagsTest() {
        super();
    }

    private static final MailField[] FIELDS_ID_AND_FLAGS = { MailField.ID, MailField.FLAGS };

    public void testMailFlagsNonExistingIds() throws OXException {
        try {
            final long currentTimeMillis = System.currentTimeMillis();
            final String[] nonexistingids = new String[] { String.valueOf(currentTimeMillis), String.valueOf(currentTimeMillis + 1) };
            mailAccess.getMessageStorage().updateMessageFlags("INBOX", nonexistingids, MailMessage.FLAG_SEEN, true);
        } catch (final Exception e) {
            fail("No Exception should be thrown here but was " + e.getMessage());
        }
    }

    public void testMailFlagsNonExistingIdsMixed() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            final long currentTimeMillis = System.currentTimeMillis();
            final String[] fetchIds = new String[]{uids[0]};
            final String[] mixednonexistingids = new String[] { String.valueOf(currentTimeMillis), uids[0] };
            mailAccess.getMessageStorage().updateMessageFlags("INBOX", mixednonexistingids, MailMessage.FLAG_SEEN, true);
            MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", fetchIds, FIELDS_ID_AND_FLAGS);
            assertTrue("Mail is not marked as \\Seen", fetchedMails[0].isSeen());

            mailAccess.getMessageStorage().updateMessageFlags("INBOX", mixednonexistingids, MailMessage.FLAG_ANSWERED, true);
            fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", fetchIds, FIELDS_ID_AND_FLAGS);
            assertTrue("Mail is not marked as \\Answered", fetchedMails[0].isAnswered());
        } catch (final Exception e) {
            fail("No Exception should be thrown here but was " + e.getMessage());
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailFlagsNotExistingFolder() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            try {
                mailAccess.getMessageStorage().updateMessageFlags("MichGibtEsNicht1337", uids, MailMessage.FLAG_SEEN, true);
            } catch (final OXException e) {
                assertTrue("Wrong Exception is thrown.", e.getErrorCode().endsWith("-1002"));
            }

            try {
                mailAccess.getMessageStorage().updateMessageFlags("MichGibtEsNicht1337", uids, MailMessage.FLAG_ANSWERED, true);
            } catch (final OXException e) {
                assertTrue("Wrong Exception is thrown.", e.getErrorCode().endsWith("-1002"));
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailFlags() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            mailAccess.getMessageStorage().updateMessageFlags("INBOX", uids, MailMessage.FLAG_SEEN, true);
            MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_FLAGS);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertTrue("Mail is not marked as \\Seen", fetchedMails[i].isSeen());
            }

            mailAccess.getMessageStorage().updateMessageFlags("INBOX", uids, MailMessage.FLAG_ANSWERED, true);
            fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_FLAGS);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertTrue("Mail is not marked as \\Answered", fetchedMails[i].isAnswered());
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailFlagsUserFlags() throws OXException {
        if (!mailAccess.getFolderStorage().getFolder("INBOX").isSupportsUserFlags()) {
            System.err.println("User flags not supported. Skipping test for user flag $Forwarded...");
            return;
        }

        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            mailAccess.getMessageStorage().updateMessageFlags("INBOX", uids, MailMessage.FLAG_FORWARDED, true);
            MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_FLAGS);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertTrue("Mail is not marked as $Forwarded", fetchedMails[i].isForwarded());
            }

            mailAccess.getMessageStorage().updateMessageFlags("INBOX", uids, MailMessage.FLAG_READ_ACK, true);
            fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_FLAGS);
            for (int i = 0; i < fetchedMails.length; i++) {
                assertTrue("Mail is not marked as $MDNSent", fetchedMails[i].isReadAcknowledgment());
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailFlagsWith268() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            mailAccess.getMessageStorage().updateMessageFlags("INBOX", uids, 268, true);
            final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_FLAGS);

            final boolean checkForwarded = mailAccess.getFolderStorage().getFolder("INBOX").isSupportsUserFlags();
            for (int i = 0; i < fetchedMails.length; i++) {
                if (checkForwarded) {
                    assertTrue("Mail is not marked as $Forwarded", fetchedMails[i].isForwarded());
                }
                assertTrue("Mail is not marked as \\Draft", fetchedMails[i].isDraft());
                assertTrue("Mail is not marked as \\Flagged", fetchedMails[i].isFlagged());
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

}
