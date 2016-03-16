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

package com.openexchange.messaging.generic;

import java.util.Date;
import java.util.Locale;
import javax.mail.internet.MailDateFormat;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.SimpleMessagingMessage;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeContentType;


/**
 * {@link MessagingComparatorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingComparatorTest extends TestCase {

    SimpleMessagingMessage msg1 = new SimpleMessagingMessage();
    SimpleMessagingMessage msg2 = new SimpleMessagingMessage();

    public void testByID() throws OXException {
        msg1.setId("a");
        msg2.setId("b");

        assertBigger(msg2, msg1, MessagingField.ID);
    }

    public void testByFolderId() throws OXException {
        msg1.setFolder("a");
        msg2.setFolder("b");

        assertBigger(msg2, msg1, MessagingField.FOLDER_ID);
    }

    public void testByContentType() throws OXException {
        msg1.putHeader(new MimeContentType("text/a"));
        msg2.putHeader(new MimeContentType("text/b"));

        assertBigger(msg2, msg1, MessagingField.CONTENT_TYPE);

    }

    public void testByFrom() throws OXException {
        msg1.putHeader(new StringMessageHeader("From", "a"));
        msg2.putHeader(new StringMessageHeader("From", "b"));

        assertBigger(msg2, msg1, MessagingField.FROM);
    }

    public void testByTo() throws OXException {
        msg1.putHeader(new StringMessageHeader("To", "a"));
        msg2.putHeader(new StringMessageHeader("To", "b"));

        assertBigger(msg2, msg1, MessagingField.TO);
    }

    public void testByBcc() throws OXException {
        msg1.putHeader(new StringMessageHeader("Bcc", "a"));
        msg2.putHeader(new StringMessageHeader("Bcc", "b"));

        assertBigger(msg2, msg1, MessagingField.BCC);
    }

    public void testByCc() throws OXException {
        msg1.putHeader(new StringMessageHeader("Cc", "a"));
        msg2.putHeader(new StringMessageHeader("Cc", "b"));

        assertBigger(msg2, msg1, MessagingField.CC);
    }

    public void testBySubject() throws OXException {
        msg1.putHeader(new StringMessageHeader("Subject", "a"));
        msg2.putHeader(new StringMessageHeader("Subject", "b"));

        assertBigger(msg2, msg1, MessagingField.SUBJECT);
    }

    public void testBySize() throws OXException {
        msg1.setSize(1);
        msg2.setSize(3);

        assertBigger(msg2, msg1, MessagingField.SIZE);
    }

    public void testSentDate() throws OXException {
        final MailDateFormat dateFormat = new MailDateFormat();
        msg1.putHeader(new StringMessageHeader("Date",dateFormat.format(new Date(0))));
        msg2.putHeader(new StringMessageHeader("Date",dateFormat.format(new Date())));

        assertBigger(msg2, msg1, MessagingField.SENT_DATE);
    }

    public void testReceivedDate() throws OXException {
        msg1.setReceivedDate(1);
        msg2.setReceivedDate(3);

        assertBigger(msg2, msg1, MessagingField.RECEIVED_DATE);
    }

    public void testFlags() throws OXException {
        msg1.setFlags(1);
        msg2.setFlags(3);

        assertBigger(msg2, msg1, MessagingField.FLAGS);
    }

    public void testThreadLevel() throws OXException {
        msg1.setThreadLevel(1);
        msg2.setThreadLevel(3);

        assertBigger(msg2, msg1, MessagingField.THREAD_LEVEL);
    }

    public void testDispositionNotificationTo() throws OXException {
        msg1.putHeader(new StringMessageHeader("Disposition-Notification-To", "a"));
        msg2.putHeader(new StringMessageHeader("Disposition-Notification-To", "b"));

        assertBigger(msg2, msg1, MessagingField.DISPOSITION_NOTIFICATION_TO);
    }

    public void testPriority() throws OXException {
        msg1.putHeader(new StringMessageHeader("X-Priority", "2"));
        msg2.putHeader(new StringMessageHeader("X-Priority", "100"));

        assertBigger(msg2, msg1, MessagingField.PRIORITY);
    }

    public void testColorLabel() throws OXException {
        msg1.setColorLabel(1);
        msg2.setColorLabel(3);

        assertBigger(msg2, msg1, MessagingField.COLOR_LABEL);
    }




    private void assertBigger(final SimpleMessagingMessage bigger, final SimpleMessagingMessage smaller, final MessagingField field) throws OXException {
        final MessagingComparator comparator = new MessagingComparator(field, Locale.ENGLISH);
        assertTrue("Comparison Failure in field "+field, comparator.compare(bigger, smaller) > 0);
    }

}
