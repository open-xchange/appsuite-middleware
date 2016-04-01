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

package com.openexchange.mail.replyforward;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.mail.internet.InternetAddress;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MailForwardTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailForwardTest extends AbstractMailTest {

    /**
	 *
	 */
    public MailForwardTest() {
        super();
    }

    /**
     * @param name
     */
    public MailForwardTest(final String name) {
        super(name);
    }

    private static final String RFC822_SRC = "Date: Wed, 2 Apr 2008 07:41:24 +0200 (CEST)\n" +
                                            "From: \"Kraft, Manuel\" <manuel.kraft@open-xchange.com>\n" +
                                            "To: \"Betten, Thorben\" <thorben.betten@open-xchange.com>\n" +
                                            "Message-ID: <32481287.4641207114884399.JavaMail.open-xchange@oxee>\n" +
                                            "Subject: Hello\n" +
                                            "MIME-Version: 1.0\n" +
                                            "Content-Type: multipart/mixed; \n" +
                                            "	boundary=\"----=_Part_298_27959028.1207114884271\"\n" +
                                            "X-Priority: 3\n" +
                                            "X-Mailer: OX Software GmbH;Development\n" +
                                            "\n" +
                                            "------=_Part_298_27959028.1207114884271\n" +
                                            "MIME-Version: 1.0\n" +
                                            "Content-Type: text/plain; charset=UTF-8\n" +
                                            "Content-Transfer-Encoding: 7bit\n" +
                                            "\n" +
                                            "Hello... This is the first message\n" +
                                            "------=_Part_298_27959028.1207114884271\n" +
                                            "MIME-Version: 1.0\n" +
                                            "Content-Type: text/vcard; charset=UTF-8; name=my.vcf\n" +
                                            "Content-Transfer-Encoding: quoted-printable\n" +
                                            "Content-Disposition: attachment; filename=my.vcf\n" +
                                            "\n" +
                                            "BEGIN:VCARD\n" +
                                            "VERSION:3.0\n" +
                                            "EMAIL;TYPE=3Dwork,pref:thorben.betten@open-xchange.com\n" +
                                            "FN:Thorben Betten\n" +
                                            "IMPP;TYPE=3Dwork,pref:x-apple:th0rb3nb\n" +
                                            "ORG:Open-Xchange GmbH;Engineering\n" +
                                            "PRODID:-//Open-Xchange//7.8.1-Rev5//EN\n" +
                                            "X-OX-ROOM-NUMBER:Development 3\n" +
                                            "REV:20160307T151919Z\n" +
                                            "ROLE:Software-Entwickler\n" +
                                            "N:Betten;Thorben;;;\n" +
                                            "TITLE:Leader Engineering\n" +
                                            "END:VCARD\n"+
                                            "\n" +
                                            "------=_Part_298_27959028.1207114884271--\n" +
                                            "\n";

    private static final String RFC822_FORWARD = "Date: Mon, 31 Mar 2008 22:39:25 +0200\n" +
                                            "To: \"dream-team@open-xchange.com\" <dream-team@open-xchange.com>\n" +
                                            "From: jane.doe@open-xchange.com\n" +
                                            "Organization: http://open-xchange.com/\n" +
                                            "Content-Type: text/plain; charset=utf-8\n" +
                                            "MIME-Version: 1.0\n" +
                                            "Message-ID: <op.t8webzmnraenw4@edna>\n" +
                                            "User-Agent: Opera Mail/9.26 (Linux)\n" +
                                            "Subject: [dream-team] Good bye und macht's gut\n" +
                                            "Content-Transfer-Encoding: 7bit\n" +
                                            "\n" +
                                            "Hallo Dream-Team,\n" +
                                            "\n" +
                                            "This is the second message\n";

    public void testMailForward() {
        try {
            final MailMessage sourceMail = MimeMessageConverter.convertMessage(RFC822_SRC.getBytes(com.openexchange.java.Charsets.US_ASCII));

            final Context ctx = new ContextImpl(getCid());
            final SessionObject session = getSession();

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();
            try {

                final MailMessage forwardMail = mailAccess.getLogicTools().getFowardMessage(new MailMessage[] { sourceMail }, false);

                {
                    /*
                     * Check the from header of the forward message which should contain the sender address of the user
                     */
                    final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(getUser(), ctx.getContextId());
                    assertTrue("Header 'From' does not carry expected value", forwardMail.getFrom()[0].equals(new InternetAddress(
                        usm.getSendAddr(),
                        true)));
                }

                /*
                 * Check if the right prefix was added to the subject
                 */
                final User user = UserStorage.getStorageUser(session.getUserId(), ctx);
                final Locale locale = user.getLocale();
                final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());
                final StringHelper stringHelper = StringHelper.valueOf(locale);
                {
                    final String subjectPrefix = "Fwd: ";
                    final String subject = new StringBuilder(32).append(subjectPrefix).append(sourceMail.getSubject()).toString();
                    assertTrue("Header 'Subject' does not carry expected value", subject.equals(forwardMail.getSubject()));
                }

                /*
                 * Check if the content type "multipart/mixed"
                 */
                assertTrue("Header 'Content-Type' does not carry expected value", forwardMail.getContentType().isMimeType(
                    MimeTypes.MIME_MULTIPART_MIXED));

                /*
                 * Check if the number of the enclosed parts is 2
                 */
                final int count = forwardMail.getEnclosedCount();
                assertTrue("Unexpected number of enclosed parts", count == 2);

                /*
                 * Check for each enclosed part if the mime type is "text/*"
                 */
                for (int i = 0; i < count; i++) {
                    final MailPart part = forwardMail.getEnclosedMailPart(i);
                    if (i == 0) {
                        assertTrue("Unexpected content type in body", part.getContentType().isMimeType(MimeTypes.MIME_TEXT_ALL));
                        final Object content = part.getContent();
                        assertTrue("Missing content", content != null);

                        if (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isForwardAsAttachment()) {

                            String forwardPrefix = stringHelper.getString(MailStrings.FORWARD_PREFIX);
                            {
                                final InternetAddress[] from = sourceMail.getFrom();
                                forwardPrefix = forwardPrefix.replaceFirst(
                                    "#FROM#",
                                    from == null || from.length == 0 ? "" : from[0].toUnicodeString());
                            }
                            {
                                final InternetAddress[] to = sourceMail.getTo();
                                forwardPrefix = forwardPrefix.replaceFirst("#TO#", to == null || to.length == 0 ? "" : addrs2String(to));
                            }
                            {
                                final InternetAddress[] cc = sourceMail.getCc();
                                forwardPrefix = forwardPrefix.replaceFirst(
                                    "#CC_LINE#",
                                    cc == null || cc.length == 0 ? "" : new StringBuilder(64).append("\nCc: ").append(addrs2String(cc)).toString());
                            }
                            {
                                final Date date = sourceMail.getSentDate();
                                if (date == null) {
                                    forwardPrefix = forwardPrefix.replaceFirst("#DATE#", "");
                                    forwardPrefix = forwardPrefix.replaceFirst("#TIME#", "");
                                } else {
                                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG,locale);
                                    dateFormat.setTimeZone(tz);
                                    forwardPrefix = forwardPrefix.replaceFirst("#DATE#", dateFormat.format(date));

                                    dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT,locale);
                                    dateFormat.setTimeZone(tz);
                                    forwardPrefix = forwardPrefix.replaceFirst("#TIME#", dateFormat.format(date));
                                }
                            }
                            forwardPrefix = forwardPrefix.replaceFirst("#SUBJECT#", sourceMail.getSubject());

                            final String text = content.toString().replaceAll("(\r?\n)> ", "$1");
                            assertTrue("Missing forward prefix:\n" + forwardPrefix, text.indexOf(forwardPrefix) > 0);
                        } else {
                            assertTrue("Unexpected forward prefix", content.toString().trim().length() == 0);
                        }

                    } else {
                        if (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isForwardAsAttachment()) {
                            assertTrue("Unexpected content type in file attachment", part.getContentType().isMimeType(
                                MimeTypes.MIME_TEXT_ALL_CARD));
                        } else {
                            assertTrue("Unexpected content type in file attachment", part.getContentType().isMimeType(
                                MimeTypes.MIME_MESSAGE_RFC822));
                        }
                    }
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

    public void testMailForwardAnother() {
        try {
            final MailMessage sourceMail = MimeMessageConverter.convertMessage(RFC822_FORWARD.getBytes(com.openexchange.java.Charsets.US_ASCII));

            final Context ctx = new ContextImpl(getCid());
            final SessionObject session = getSession();

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();
            try {

                final MailMessage forwardMail = mailAccess.getLogicTools().getFowardMessage(new MailMessage[] { sourceMail }, false);

                {
                    final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(getUser(), ctx.getContextId());
                    assertTrue("Header 'From' does not carry expected value", forwardMail.getFrom()[0].equals(new InternetAddress(
                        usm.getSendAddr(),
                        true)));
                }

                final User user = UserStorage.getStorageUser(session.getUserId(), ctx);
                final Locale locale = user.getLocale();
                final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());
                final StringHelper stringHelper = StringHelper.valueOf(locale);
                {
                    final String subjectPrefix = "Fwd: ";
                    final String subject = new StringBuilder(32).append(subjectPrefix).append(sourceMail.getSubject()).toString();
                    assertTrue("Header 'Subject' does not carry expected value", subject.equals(forwardMail.getSubject()));
                }

                final boolean isInlineForward = (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isForwardAsAttachment());

                if (isInlineForward) {
                    assertTrue("Header 'Content-Type' does not carry expected value", forwardMail.getContentType().isMimeType(
                        MimeTypes.MIME_TEXT_PLAIN));
                } else {
                    assertTrue("Header 'Content-Type' does not carry expected value", forwardMail.getContentType().isMimeType(
                        MimeTypes.MIME_MULTIPART_ALL));
                }

                final int count = forwardMail.getEnclosedCount();
                if (isInlineForward) {
                    assertTrue("Unexpected number of enclosed parts: " + count, count == MailPart.NO_ENCLOSED_PARTS);
                } else {
                    assertTrue("Unexpected number of enclosed parts: " + count, count == 2);
                }

                final Object content = forwardMail.getContent();
                if (isInlineForward) {
                    assertTrue("Missing content", content != null);
                }

                if (isInlineForward) {
                    String forwardPrefix = stringHelper.getString(MailStrings.FORWARD_PREFIX);
                    {
                        final InternetAddress[] from = sourceMail.getFrom();
                        forwardPrefix = forwardPrefix.replaceFirst(
                            "#FROM#",
                            from == null || from.length == 0 ? "" : from[0].toUnicodeString());
                    }
                    {
                        final InternetAddress[] to = sourceMail.getTo();
                        forwardPrefix = forwardPrefix.replaceFirst("#TO#", to == null || to.length == 0 ? "" : addrs2String(to));
                    }
                    {
                        final InternetAddress[] cc = sourceMail.getCc();
                        forwardPrefix = forwardPrefix.replaceFirst(
                            "#CC_LINE#",
                            cc == null || cc.length == 0 ? "" : new StringBuilder(64).append("\nCc: ").append(addrs2String(cc)).toString());
                    }
                    {
                        final Date date = sourceMail.getSentDate();
                        if (date == null) {
                            forwardPrefix = forwardPrefix.replaceFirst("#DATE#", "");
                            forwardPrefix = forwardPrefix.replaceFirst("#TIME#", "");
                        } else {
                            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG,locale);
                            dateFormat.setTimeZone(tz);
                            forwardPrefix = forwardPrefix.replaceFirst("#DATE#", dateFormat.format(date));

                            dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT,locale);
                            dateFormat.setTimeZone(tz);
                            forwardPrefix = forwardPrefix.replaceFirst("#TIME#", dateFormat.format(date));
                        }
                    }
                    forwardPrefix = forwardPrefix.replaceFirst("#SUBJECT#", sourceMail.getSubject());

                    final String text = content.toString().replaceAll("(\r?\n)> ", "$1");
                    assertTrue("Missing forward prefix:\n" + forwardPrefix, text.indexOf(forwardPrefix) > 0);
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

    public void testForwardMultiple() {
        try {

            new ContextImpl(getCid());
            final SessionObject session = getSession();

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();
            String[] uids = null;
            try {
                {
                    final MailMessage[] mails = new MailMessage[2];
                    mails[0] = MimeMessageConverter.convertMessage(RFC822_FORWARD.getBytes(com.openexchange.java.Charsets.US_ASCII));
                    mails[1] = MimeMessageConverter.convertMessage(RFC822_SRC.getBytes(com.openexchange.java.Charsets.US_ASCII));
                    uids = mailAccess.getMessageStorage().appendMessages("INBOX", mails);
                }

                final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages(
                    "INBOX",
                    uids,
                    new MailField[] { MailField.FULL });

                final MailMessage forwardMail = mailAccess.getLogicTools().getFowardMessage(fetchedMails, false);

                assertTrue("Unexpected content type: " + forwardMail.getContentType().toString(), forwardMail.getContentType().isMimeType(
                    MimeTypes.MIME_MULTIPART_MIXED));

                final int count = forwardMail.getEnclosedCount();
                assertTrue("Unexpected number of attachments: " + count, count == 3);

                boolean partOfFirstMailFound = false;
                boolean partOfSecondMailFound = false;

                for (int i = 0; i < count; i++) {
                    final MailPart part = forwardMail.getEnclosedMailPart(i);
                    if (i == 0) {
                        assertTrue("Unexpected enclosed part's content type: " + part.getContentType(), part.getContentType().isMimeType(
                            MimeTypes.MIME_TEXT_ALL));
                    } else {
                        assertTrue("Unexpected enclosed part's content type: " + part.getContentType(), part.getContentType().isMimeType(
                            MimeTypes.MIME_MESSAGE_RFC822));
                    }
                    /*
                     * additional checks for bug 12420, where there is an amount of forwarded mails, yet their content is always that of the
                     * main mail.
                     */
                    if (i == 1 || i == 2) {
                        final MailMessage myMail = (MailMessage) part.getContent();
                        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();
                        myMail.writeTo(out);
                        final String mailtext = new String(out.toByteArray(), com.openexchange.java.Charsets.US_ASCII);
                        if (mailtext.contains("This is the first message")) {
                            partOfFirstMailFound = true;
                        }
                        if (mailtext.contains("This is the second message")) {
                            partOfSecondMailFound = true;
                        }
                    }
                }
                assertTrue("Part of first mail missing", partOfFirstMailFound);
                assertTrue("Part of second mail missing", partOfSecondMailFound);

            } finally {

                if (uids != null) {
                    mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
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

    /**
     * Creates a {@link String} from given array of {@link InternetAddress} instances through invoking
     * {@link InternetAddress#toUnicodeString()}
     *
     * @param addrs The array of {@link InternetAddress} instances
     * @return A comma-separated list of addresses as a {@link String}
     */
    private static String addrs2String(final InternetAddress[] addrs) {
        final StringBuilder tmp = new StringBuilder(addrs.length * 16);
        tmp.append(addrs[0].toUnicodeString());
        for (int i = 1; i < addrs.length; i++) {
            tmp.append(", ").append(addrs[i].toUnicodeString());
        }
        return tmp.toString();
    }
}
