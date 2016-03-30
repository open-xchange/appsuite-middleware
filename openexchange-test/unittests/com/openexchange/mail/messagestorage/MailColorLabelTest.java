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
 * {@link MailColorLabelTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public final class MailColorLabelTest extends MessageStorageTest {

    /**
	 *
	 */
    public MailColorLabelTest() {
        super();
    }

    private static final MailField[] FIELDS_ID_AND_COLORLABEL = { MailField.ID, MailField.COLOR_LABEL };

    public void testMailColorLabelNonExistingIds() throws OXException {
        if (!mailAccess.getFolderStorage().getFolder("INBOX").isSupportsUserFlags()) {
            System.err.println("User flags not supported. Skipping test for non-exsiting ids");
            return;
        }

        final long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < 11; i++) {
            mailAccess.getMessageStorage().updateMessageColorLabel("INBOX", new String[]{String.valueOf(currentTimeMillis), String.valueOf(currentTimeMillis + 1)}, i);
        }
    }

    public void testMailColorLabelNonExistingIdsMixed() throws OXException {
        if (!mailAccess.getFolderStorage().getFolder("INBOX").isSupportsUserFlags()) {
            System.err.println("User flags not supported. Skipping test for non-exsiting mixed ids");
            return;
        }

        final long currentTimeMillis = System.currentTimeMillis();
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            for (int i = 0; i < 11; i++) {
                mailAccess.getMessageStorage().updateMessageColorLabel("INBOX", new String[]{String.valueOf(currentTimeMillis), uids[0]}, i);
                final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", new String[]{uids[0]}, FIELDS_ID_AND_COLORLABEL);
                    assertTrue("Missing color label", fetchedMails[0].containsColorLabel());
                    assertTrue("Mail's color flag does not carry expected value", fetchedMails[0].getColorLabel() == i);
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailColorLabelNotExistingFolder() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            for (int i = 0; i < 11; i++) {
                try {
                    mailAccess.getMessageStorage().updateMessageColorLabel("MichGibtEsNicht1337", uids, i);
                } catch (final OXException e) {
                    assertTrue("Wrong Exception is thrown.", e.getErrorCode().endsWith("-1002"));
                }
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailColorLabel() throws OXException {
        if (!mailAccess.getFolderStorage().getFolder("INBOX").isSupportsUserFlags()) {
            System.err.println("User flags not supported. Skipping test for color labels");
            return;
        }

        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            for (int i = 0; i < 11; i++) {
                mailAccess.getMessageStorage().updateMessageColorLabel("INBOX", uids, i);
                final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID_AND_COLORLABEL);
                for (int o = 0; o < fetchedMails.length; o++) {
                    assertTrue("Missing color label", fetchedMails[o].containsColorLabel());
                    assertTrue("Mail's color flag does not carry expected value", fetchedMails[o].getColorLabel() == i);
                }
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

}
