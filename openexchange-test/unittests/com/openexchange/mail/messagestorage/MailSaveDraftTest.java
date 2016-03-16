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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.smtp.dataobjects.SMTPMailMessage;

/**
 * {@link MailSaveDraftTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSaveDraftTest extends AbstractMailTest {

    /**
	 *
	 */
    public MailSaveDraftTest() {
        super();
    }

    /**
     * @param name
     */
    public MailSaveDraftTest(final String name) {
        super(name);
    }

    public void testMailDraft() {
        try {
            final SessionObject session = getSession();
            final Context dummyContext = new ContextImpl(session.getContextId());

            ComposedMailMessage draftMail = new SMTPMailMessage("The first line", session, dummyContext);
            draftMail.addFrom(new InternetAddress("someone@somewhere.com", true));
            draftMail.addTo(new InternetAddress("someone.else@another.com", true));

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();

            final String draftFullname = mailAccess.getFolderStorage().getDraftsFolder();

            String prevUid = null;
            String uid = null;
            try {
                MailMessage mail = mailAccess.getMessageStorage().saveDraft(draftFullname, draftMail);
                uid = mail.getMailId();
                prevUid = uid;
                /*
                 * Check content
                 */
                String content = mail.getContent().toString();
                assertEquals("Content mismatch", "The first line", content);
                /*
                 * Edit draft
                 */
                draftMail = new SMTPMailMessage("The first line<br>And the second line", session, dummyContext);
                draftMail.addFrom(new InternetAddress("someone@somewhere.com", true));
                draftMail.addTo(new InternetAddress("someone.else@another.com", true));
                draftMail.addTo(new InternetAddress("Jane Doe <another.one@anywhere.org>", true));
                draftMail.setMsgref(new MailPath(mailAccess.getAccountId(), mail.getFolder(), mail.getMailId()));

                mail = mailAccess.getMessageStorage().saveDraft(draftFullname, draftMail);
                uid = mail.getMailId();
                /*
                 * Check existence of former draft version
                 */
                Exception exc = null;
                MailMessage tmp = null;
                try {
                    tmp = mailAccess.getMessageStorage().getMessage(draftFullname, prevUid, false);
                } catch (final OXException e) {
                    prevUid = null;
                    exc = e;
                }
                assertTrue("Former draft version still available", exc != null || tmp == null);
                /*
                 * Check content again
                 */
                final String expected = "The first line<br>And the second line";
                final Set<String> expectees = new HashSet<String>(Arrays.asList("The first line<br>And the second line", "The first line\nAnd the second line", "The first line\r\nAnd the second line"));
                content = mail.getContent().toString();
                if (!expectees.contains(content)) {
                    final StringBuilder sb = new StringBuilder(1024);
                    sb.append("Expected value:\n");
                    char[] chars = expected.toCharArray();
                    sb.append((int) chars[0]);
                    for (int i = 1; i < chars.length; i++) {
                        sb.append(' ').append((int) chars[i]);
                    }
                    sb.append("\nReturned value:\n");
                    chars = content.toCharArray();
                    sb.append((int) chars[0]);
                    for (int i = 1; i < chars.length; i++) {
                        sb.append(' ').append((int) chars[i]);
                    }
                    sb.append("\nIn words:");
                    sb.append('"').append(expected).append('"').append('\n');
                    sb.append('"').append(content).append('"');
                    fail(sb.toString());
                }
            } finally {
                if (prevUid != null) {
                    mailAccess.getMessageStorage().deleteMessages(draftFullname, new String[] { prevUid }, true);
                }
                if (uid != null) {
                    mailAccess.getMessageStorage().deleteMessages(draftFullname, new String[] { uid }, true);
                }
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
