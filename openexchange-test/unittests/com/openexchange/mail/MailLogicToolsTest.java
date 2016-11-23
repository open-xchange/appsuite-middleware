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

package com.openexchange.mail;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.DumperMessageHandler;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailLogicToolsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailLogicToolsTest extends AbstractMailTest {


    private static final MailField[] COMMON_LIST_FIELDS = { MailField.ID, MailField.FOLDER_ID, MailField.FROM, MailField.TO, MailField.RECEIVED_DATE, MailField.SENT_DATE, MailField.SUBJECT, MailField.CONTENT_TYPE, MailField.FLAGS, MailField.PRIORITY, MailField.COLOR_LABEL };

    @Test
    public void testForward() {
        try {
            final SessionObject session = getSession();
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect(/* mailConfig */);
            try {
                // ByteArrayInputStream in = new
                // ByteArrayInputStream(TEST_MAIL.getBytes(com.openexchange.java.Charsets.US_ASCII));

                final MailMessage[] mails = mailAccess.getMessageStorage().searchMessages("INBOX", null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, COMMON_LIST_FIELDS);
                int count = 0;
                for (int i = 0; i < mails.length; i++) {
                    if (mails[i].getContentType().isMimeType("multipart/mixed")) {
                        final DumperMessageHandler msgHandler1 = new DumperMessageHandler(false);
                        new MailMessageParser().parseMailMessage(mailAccess.getMessageStorage().getMessage("INBOX", mails[i].getMailId(), true), msgHandler1);

                        final MailMessage[] ms = new MailMessage[] { mailAccess.getMessageStorage().getMessage("INBOX", mails[i].getMailId(), false) };
                        final MailMessage forwardMail = mailAccess.getLogicTools().getFowardMessage(ms, false);
                        final DumperMessageHandler msgHandler = new DumperMessageHandler(false);
                        new MailMessageParser().parseMailMessage(forwardMail, msgHandler);
                        if (++count == 50) {
                            break;
                        }
                    }
                }

            } finally {
                mailAccess.close(true);
            }

        } catch (final OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testReply() {
        try {
            final SessionObject session = getSession();
            final MailAccess<?, ?> mailConnection = MailAccess.getInstance(session);
            mailConnection.connect(/* mailConfig */);
            try {
                // ByteArrayInputStream in = new
                // ByteArrayInputStream(TEST_MAIL.getBytes(com.openexchange.java.Charsets.US_ASCII));

                final MailMessage[] mails = mailConnection.getMessageStorage().searchMessages("INBOX", null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, COMMON_LIST_FIELDS);
                int count = 0;
                for (int i = 0; i < mails.length; i++) {
                    if (!"11611".equals(mails[i].getMailId())) {
                        continue;
                    }
                    final DumperMessageHandler msgHandler1 = new DumperMessageHandler(true);
                    new MailMessageParser().parseMailMessage(mailConnection.getMessageStorage().getMessage("default/INBOX", mails[i].getMailId(), true), msgHandler1);

                    final MailMessage originalMail = mailConnection.getMessageStorage().getMessage("INBOX", mails[i].getMailId(), false);
                    final MailMessage replyMail = mailConnection.getLogicTools().getReplyMessage(originalMail, true);
                    final DumperMessageHandler msgHandler = new DumperMessageHandler(true);
                    new MailMessageParser().parseMailMessage(replyMail, msgHandler);
                    if (++count == 50) {
                        break;
                    }
                }

            } finally {
                mailConnection.close(true);
            }

        } catch (final OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
