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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;

/**
 * {@link MailAppendTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAppendTest extends MessageStorageTest {

    private static final String INBOX = "INBOX";

    /**
	 * 
	 */
    public MailAppendTest() {
        super();
    }

    /**
     * @param name
     */
    public MailAppendTest(final String name) {
        super(name);
    }

    public void testMailAppend() {

        // At first we should test the append - get - delete operation with one mail only so that we see, that the basic functions
        // are working
        try {
            final MailField[][] test = generateVariations();
            final MailAccess<?, ?> mailAccess = getMailAccess();
            try {
                final MailMessage[] testmessages = getMessages(getTestMailDir(), -1);
                final long[] uids = mailAccess.getMessageStorage().appendMessages(INBOX, testmessages);
                try {
                    final MailField[] fieldsfull = { MailField.FULL };
                    final MailField[] fieldWithoutUidFolderAndFlags = {
                        MailField.CONTENT_TYPE, MailField.FROM, MailField.TO, MailField.CC, MailField.BCC, MailField.SUBJECT,
                        MailField.SIZE, MailField.SENT_DATE, MailField.THREAD_LEVEL, MailField.DISPOSITION_NOTIFICATION_TO,
                        MailField.PRIORITY, MailField.COLOR_LABEL, MailField.HEADERS, MailField.BODY };

                    for (int i = 0; i < uids.length; i++) {
                        final MailMessage m = mailAccess.getMessageStorage().getMessage(INBOX, uids[i], true);
                        m.removeHeader(MessageHeaders.HDR_X_OX_MARKER);
                        System.out.println("Mail #" + m.getMailId() + ": " + m.getSubject());
                        compareMailMessages(testmessages[i], m, fieldWithoutUidFolderAndFlags, fieldsfull, "test message", "fetched message", true);
                    }
                    // Check only for the one values here (that is the size of the enum out of all combinations):
                    for (int o = 0; o < MailField.values().length; o++) {
                        MailField[] fields = test[o];
                        final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages(INBOX, uids, fields);
                        assertTrue("The size of the uids is not equal with the size of the fetched mails which is returned from the getMessages method", fetchedMails.length == uids.length);
                        for (int i = 0; i < fetchedMails.length; i++) {
                            System.out.println("Fetched: " + fetchedMails[i].getMailId());
                            fetchedMails[i].removeHeader(MessageHeaders.HDR_X_OX_MARKER);
                            compareMailMessages(testmessages[i], fetchedMails[i], fieldWithoutUidFolderAndFlags, fields, "test messages", "fetched messages " + i, true);
                        }
                    }
                    // TODO: Random test out of all combinations:
                } finally {
                    mailAccess.getMessageStorage().deleteMessages(INBOX, uids, true);
                }
            } finally {
                /*
                 * close
                 */
                mailAccess.close(false);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
