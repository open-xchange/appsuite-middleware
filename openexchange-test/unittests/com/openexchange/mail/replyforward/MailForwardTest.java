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

package com.openexchange.mail.replyforward;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.mail.internet.InternetAddress;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailForwardTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
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
	
	private static final String RFC822_SRC = "Return-Path: <manuel.kraft@open-xchange.com>\n" + 
			"Received: from ox.netline-is.de ([unix socket])\n" + 
			"	by ox (Cyrus v2.2.3) with LMTP; Wed, 02 Apr 2008 07:42:19 +0200\n" + 
			"X-Sieve: CMU Sieve 2.2\n" + 
			"Received: by ox.netline-is.de (Postfix, from userid 65534)\n" + 
			"	id 49AB33DB402; Wed,  2 Apr 2008 07:42:19 +0200 (CEST)\n" + 
			"Received: from netline.de (comfire.netline.de [192.168.32.1])\n" + 
			"	by ox.netline-is.de (Postfix) with ESMTP id 744013DB3F7\n" + 
			"	for <thorben@netline-is.de>; Wed,  2 Apr 2008 07:42:17 +0200 (CEST)\n" + 
			"Received: from [10.20.30.11] (helo=www.open-xchange.org ident=mail)\n" + 
			"	by netline.de with esmtp (Exim)\n" + 
			"	id 1JgvaA-00032q-00\n" + 
			"	for thorben@netline-is.de; Wed, 02 Apr 2008 07:32:22 +0200\n" + 
			"Received: from mail.open-xchange.com ([10.20.30.22] helo=ox.open-xchange.com)\n" + 
			"	by www.open-xchange.org with esmtp (Exim 3.36 #1 (Debian))\n" + 
			"	id 1Jgvix-0004np-00\n" + 
			"	for <thorben@open-xchange.org>; Wed, 02 Apr 2008 07:41:27 +0200\n" + 
			"Received: by ox.open-xchange.com (Postfix, from userid 76)\n" + 
			"	id 890B032C833; Wed,  2 Apr 2008 07:41:26 +0200 (CEST)\n" + 
			"Received: from ox.open-xchange.com ([unix socket])\n" + 
			"	 by ox.open-xchange.com (Cyrus v2.2.12-Invoca-RPM-2.2.12-8.1.RHEL4) with LMTPA;\n" + 
			"	 Wed, 02 Apr 2008 07:41:26 +0200\n" + 
			"X-Sieve: CMU Sieve 2.2\n" + 
			"Received: by ox.open-xchange.com (Postfix, from userid 99)\n" + 
			"	id 4C6F232C887; Wed,  2 Apr 2008 07:41:26 +0200 (CEST)\n" + 
			"Received: from oxee (unknown [192.168.32.9])\n" + 
			"	by ox.open-xchange.com (Postfix) with ESMTP id 62D8332C7BB\n" + 
			"	for <thorben.betten@open-xchange.com>; Wed,  2 Apr 2008 07:41:24 +0200 (CEST)\n" + 
			"Date: Wed, 2 Apr 2008 07:41:24 +0200 (CEST)\n" + 
			"From: \"Kraft, Manuel\" <manuel.kraft@open-xchange.com>\n" + 
			"To: \"Betten, Thorben\" <thorben.betten@open-xchange.com>\n" + 
			"Message-ID: <32481287.4641207114884399.JavaMail.open-xchange@oxee>\n" + 
			"Subject: imap server\n" + 
			"MIME-Version: 1.0\n" + 
			"Content-Type: multipart/mixed; \n" + 
			"	boundary=\"----=_Part_298_27959028.1207114884271\"\n" + 
			"X-Priority: 3\n" + 
			"X-Mailer: Open-Xchange Mailer v6.5.0-6342\n" + 
			"X-Scanner: exiscan *1JgvaA-00032q-00*NAZPyw7vBYw* http://duncanthrax.net/exiscan/\n" + 
			"X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\n" + 
			"X-Spam-Level: \n" + 
			"X-Spam-Status: No, hits=-4.9 required=5.0 tests=AWL,BAYES_00,UPPERCASE_25_50 \n" + 
			"	autolearn=no version=2.64\n" + 
			"\n" + 
			"------=_Part_298_27959028.1207114884271\n" + 
			"MIME-Version: 1.0\n" + 
			"Content-Type: text/plain; charset=UTF-8\n" + 
			"Content-Transfer-Encoding: quoted-printable\n" + 
			"\n" + 
			"http://www.open-xchange.com/forum/showthread.php?t=3D1237\n" + 
			"\n" + 
			"\n" + 
			"weisst du was ich den noch fragen k=C3=B6nnten warum das bei dem son fehler=\n" + 
			" gibt obwohl er nen courier hat den wir ja eigentlich supporten?!=20\n" + 
			"\n" + 
			"manuel\n" + 
			"------=_Part_298_27959028.1207114884271\n" + 
			"MIME-Version: 1.0\n" + 
			"Content-Type: text/x-vcard; charset=UTF-8; name=\"Kraft,Manuel.vcf\"\n" + 
			"Content-Transfer-Encoding: quoted-printable\n" + 
			"Content-Disposition: attachment; filename=\"Kraft,Manuel.vcf\"\n" + 
			"\n" + 
			"BEGIN:VCARD\n" + 
			"VERSION:2.1\n" + 
			"FN:Kraft, Manuel\n" + 
			"N:Kraft;Manuel;;Herr;\n" + 
			"PHOTO;JPEG;BASE64:\n" + 
			"PNGIHDRFFLupHYsdtIMEu0IDATxilumsPXIA47hS4EZNuBF1+YRu87oE//ss1c0Q1AB0JiXFRDq=\n" + 
			"GfUb4Lzll277gYmLRm9ZxTDi8h4AkxxY1lxWs//Hn3GUGJCUllDICB+1Ub2QONa9tcT73yMUWx1=\n" + 
			"BNy1M+r9QyI186dizplPJIufKOSMw11nnaccLwEOCEZEfZsAAAWck/ccD3RDc1VrTedY++aKA05=\n" + 
			"30AlkZeRv5gN+yo78tAqy5sAT+RLl/x0f3sSSSJULO7jUUW8849UXbEBAOKqxORXHcZdr59mVXf=\n" + 
			"lf02ZgAAZxtCH/bd3zKK+tjA8wAAwttn//kOAwFmFMzyPQDzgO74vNSwjVQPv0txOYKwfADqew0=\n" + 
			"XEWOkMhPXTkvSR6EqeUmI0tGlmt6s1EkykW2adf368vJyvm7CLYBz+O6Eltx6jtstv10F6N//gS=\n" + 
			"xpSCo9Aekej8Gv7D3QPAPQmTcKicyiRICNP1IicWxQeFlewulhOZuajr1INtr+SJXJNaxEfeifG=\n" + 
			"eHE59IIKxzO27Jz96duyWyyJyreSSopwGBQFTu0IbicGk5lrSpH/neeUysF//vTu1LbCXUZO/GT=\n" + 
			"6Z1W00Ffdvh+9Zr5acgfoXw8g6osmc1PYsrr1kEpVryGlnqnUxoWClMM/X+VU6XKddCa29pSW9e=\n" + 
			"LcXpxCykiU9QZRFmltSmp4+Vksqzy5dU+9nrSRMNF2qesAQfRYrod7xW/4e/XGpvE4xZSqguU9C=\n" + 
			"A2EUO57MVxPetKqyTEzHF8TTX1Q9c81uWLisTj/yJY00JbxItsN+xliiQTsftB//DywyPgzxqqo=\n" + 
			"iaMqjBNud8SSCHA+Zx0X999vRabll7x7WWG0qzaR2YgdLkpAioNbGxSTHzjCs/2ktJgz2UWig/O=\n" + 
			"fo/uOr4AuHf2SzIqK9H+q959JZ3/2s9p6mHl0nn7sfrFb6114BNyim7NYRQpb5If633AdTCrwLN=\n" + 
			"hLL8DwwSSFTrdsx0HtH33/vTIpj2SZ+Zkz8jMmwqZ1l1EWNY8cRqt7XPM8RB6ONG/mQj9ShCGYu=\n" + 
			"JrDqyIVxit4t2ZoLkNTlhBH2nXMBe8XyE9li1fHSOwlHs9JSXI3El8eSegLetw0MXuEEs1UIENA=\n" + 
			"AA=3D\n" + 
			" BDAY:19810907\n" + 
			"ADR;TYPE=3Dwork:;;;;;;DE\n" + 
			"TEL;TYPE=3Dwork;TYPE=3Dvoice:+49 (2761) 8385-29\n" + 
			"TEL;TYPE=3Dwork;TYPE=3Dvoice:+49 162 2393954\n" + 
			"TEL;TYPE=3Dwork;TYPE=3Dfax:+49 (2761) 8385-30\n" + 
			"TEL;TYPE=3Dcell;TYPE=3Dvoice:+49 171 691 1712\n" + 
			"EMAIL:manuel.kraft@open-xchange.com\n" + 
			"ROLE:Developer\n" + 
			"ORG:Open-Xchange Inc.;Development\n" + 
			"REV:20080310T100717.953Z\n" + 
			"UID:22@oxee.netline.de\n" + 
			"END:VCARD\n" + 
			"\n" + 
			"------=_Part_298_27959028.1207114884271--\n" + 
			"\n";

	private static final String RFC822_FORWARD = "Return-Path: <dream-team-bounces@open-xchange.com>\n" + 
			"Received: from ox.netline-is.de ([unix socket])\n" + 
			"	by ox (Cyrus v2.2.3) with LMTP; Mon, 31 Mar 2008 22:39:13 +0200\n" + 
			"X-Sieve: CMU Sieve 2.2\n" + 
			"Received: by ox.netline-is.de (Postfix, from userid 65534)\n" + 
			"	id 1D3EB39A15A; Mon, 31 Mar 2008 22:39:11 +0200 (CEST)\n" + 
			"Received: from netline.de (comfire.netline.de [192.168.32.1])\n" + 
			"	by ox.netline-is.de (Postfix) with ESMTP id 0272639A155\n" + 
			"	for <thorben@netline-is.de>; Mon, 31 Mar 2008 22:39:00 +0200 (CEST)\n" + 
			"Received: from [10.20.30.11] (helo=www.open-xchange.org ident=mail)\n" + 
			"	by netline.de with esmtp (Exim)\n" + 
			"	id 1JgQd5-0005ya-00\n" + 
			"	for thorben@netline-is.de; Mon, 31 Mar 2008 22:29:19 +0200\n" + 
			"Received: from mail.open-xchange.com ([10.20.30.22] helo=ox.open-xchange.com)\n" + 
			"	by www.open-xchange.org with esmtp (Exim 3.36 #1 (Debian))\n" + 
			"	id 1JgQlf-00070z-00\n" + 
			"	for <thorben@open-xchange.org>; Mon, 31 Mar 2008 22:38:11 +0200\n" + 
			"Received: by ox.open-xchange.com (Postfix, from userid 76)\n" + 
			"	id 6EE3F32C677; Mon, 31 Mar 2008 22:38:08 +0200 (CEST)\n" + 
			"Received: from ox.open-xchange.com ([unix socket])\n" + 
			"	 by ox.open-xchange.com (Cyrus v2.2.12-Invoca-RPM-2.2.12-8.1.RHEL4) with LMTPA;\n" + 
			"	 Mon, 31 Mar 2008 22:38:07 +0200\n" + 
			"X-Sieve: CMU Sieve 2.2\n" + 
			"Received: by ox.open-xchange.com (Postfix, from userid 99)\n" + 
			"	id 46C8F32C6F2; Mon, 31 Mar 2008 22:38:00 +0200 (CEST)\n" + 
			"Received: from ox.open-xchange.com (localhost.localdomain [127.0.0.1])\n" + 
			"	by ox.open-xchange.com (Postfix) with ESMTP id 3CD7732C620;\n" + 
			"	Mon, 31 Mar 2008 22:37:58 +0200 (CEST)\n" + 
			"X-Original-To: dream-team@ox.open-xchange.com\n" + 
			"Delivered-To: dream-team@ox.open-xchange.com\n" + 
			"Received: by ox.open-xchange.com (Postfix, from userid 99)\n" + 
			"	id 6258D32C677; Mon, 31 Mar 2008 22:37:56 +0200 (CEST)\n" + 
			"Received: from edna (nrbg-4dbf9375.pool.einsundeins.de [77.191.147.117])\n" + 
			"	by ox.open-xchange.com (Postfix) with ESMTP id 82C6532C50F\n" + 
			"	for <dream-team@open-xchange.com>;\n" + 
			"	Mon, 31 Mar 2008 22:37:54 +0200 (CEST)\n" + 
			"Date: Mon, 31 Mar 2008 22:39:25 +0200\n" + 
			"To: \"dream-team@open-xchange.com\" <dream-team@open-xchange.com>\n" + 
			"From: =?utf-8?Q?Ren=C3=A9_Stach?= <rene.stach@open-xchange.com>\n" + 
			"Organization: http://open-xchange.com/\n" + 
			"Content-Type: text/plain; format=flowed; delsp=yes; charset=utf-8\n" + 
			"MIME-Version: 1.0\n" + 
			"Message-ID: <op.t8webzmnraenw4@edna>\n" + 
			"User-Agent: Opera Mail/9.26 (Linux)\n" + 
			"Subject: [dream-team] Good bye und macht's gut\n" + 
			"X-BeenThere: dream-team@open-xchange.com\n" + 
			"X-Mailman-Version: 2.1.5\n" + 
			"Precedence: list\n" + 
			"List-Id: Mailinglist for whole the dream-team of Open-Xchange - all members of\n" + 
			"	Open-Change <dream-team.open-xchange.com>\n" + 
			"List-Unsubscribe: <https://ox.open-xchange.com/mailman/listinfo/dream-team>,\n" + 
			"	<mailto:dream-team-request@open-xchange.com?subject=unsubscribe>\n" + 
			"List-Archive: <https://ox.open-xchange.com/pipermail/dream-team>\n" + 
			"List-Post: <mailto:dream-team@open-xchange.com>\n" + 
			"List-Help: <mailto:dream-team-request@open-xchange.com?subject=help>\n" + 
			"List-Subscribe: <https://ox.open-xchange.com/mailman/listinfo/dream-team>,\n" + 
			"	<mailto:dream-team-request@open-xchange.com?subject=subscribe>\n" + 
			"Sender: dream-team-bounces@open-xchange.com\n" + 
			"Errors-To: dream-team-bounces@open-xchange.com\n" + 
			"Content-Transfer-Encoding: quoted-printable\n" + 
			"X-Scanner: exiscan *1JgQd5-0005ya-00*nthOXtQxc7Q* http://duncanthrax.net/exiscan/\n" + 
			"X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\n" + 
			"X-Spam-Level: \n" + 
			"X-Spam-Status: No, hits=-4.5 required=5.0 tests=AWL,BAYES_00,\n" + 
			"	TO_ADDRESS_EQ_REAL autolearn=no version=2.64\n" + 
			"\n" + 
			"Hallo Dream-Team,\n" + 
			"\n" + 
			"auch ich m=C3=B6chte mich =C3=BCber diese Mailingliste von euch allen ver=\n" + 
			"abschieden. =20\n" + 
			"Es war eine tolle Zeit, die mir viel Spa=C3=9F gemacht hat. Wir haben zus=\n" + 
			"ammen =20\n" + 
			"tolle Produkte auf die Beine gestellt, die mittlerweile vom Kleinkindalte=\n" + 
			"r =20\n" + 
			"zum Jugendlichen herangewachsen sind.\n" + 
			"\n" + 
			"Ich w=C3=BCnsche euch allen das Geschick und Gl=C3=BCck, die beiden junge=\n" + 
			"n Produkte =20\n" + 
			"ins Erwachsenenalter zu =C3=BCberf=C3=BChren, denn das ist die Zeit, in d=\n" + 
			"er sie Geld =20\n" + 
			"nach Hause bringen und somit helfen, das Unternehmen erfolgreich zu mache=\n" + 
			"n.\n" + 
			"\n" + 
			"Wohin meine Reise gehen wird, wei=C3=9F ich momentan noch nicht. Es gibt =\n" + 
			"=20\n" + 
			"mehrere interessante Angebote, aber noch ist nichts entschieden. Da die =20\n" + 
			"meisten mit mir =C3=BCber Xing verbunden sind, werdet ihr sicherlich =20\n" + 
			"mitbekommen was ich in Zukunft machen werde.\n" + 
			"\n" + 
			"Wie Rafael so sch=C3=B6n sagte: \"Man sieht sich immer zweimal im Leben.\" =\n" + 
			"W=C3=BCrde =20\n" + 
			"mich sehr freuen.\n" + 
			"\n" + 
			"Tsch=C3=BCss, servus und good bye!\n" + 
			"--=20\n" + 
			"Ren=C3=A9 Stach\n" + 
			"Address: Open-Xchange GmbH, Maxfeldstr. 9, 90409 N=C3=BCrnberg\n" + 
			"Phone: +49 (0)911 180 1413     Fax: +49 (0)911 180 1419\n" + 
			"Web: http://open-xchange.com/\n";

	public void testMailForward() {
		try {
			final MailMessage sourceMail = MIMEMessageConverter.convertMessage(RFC822_SRC.getBytes("US-ASCII"));

			final Context ctx = new ContextImpl(getCid());
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(), ctx, "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {

				final MailMessage forwardMail = mailAccess.getLogicTools().getFowardMessage(
						new MailMessage[] { sourceMail });

				{
					final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(getUser(),
							ctx.getContextId());
					assertTrue("Header 'From' does not carry expected value", forwardMail.getFrom()[0]
							.equals(new InternetAddress(usm.getSendAddr(), true)));
				}

				final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
				final StringHelper stringHelper = new StringHelper(locale);
				{
					final String subjectPrefix = stringHelper.getString(MailStrings.FORWARD_SUBJECT_PREFIX);
					final String subject = new StringBuilder(32).append(subjectPrefix).append(sourceMail.getSubject())
							.toString();
					assertTrue("Header 'Subject' does not carry expected value", subject.equals(forwardMail
							.getSubject()));
				}

				assertTrue("Header 'Content-Type' does not carry expected value", forwardMail.getContentType()
						.isMimeType(MIMETypes.MIME_MULTIPART_MIXED));

				final int count = forwardMail.getEnclosedCount();
				assertTrue("Unexpected number of enclosed parts", count == 2);

				for (int i = 0; i < count; i++) {
					final MailPart part = forwardMail.getEnclosedMailPart(i);
					if (i == 0) {
						assertTrue("Unexpected content type in body", part.getContentType().isMimeType(
								MIMETypes.MIME_TEXT_ALL));
						final Object content = part.getContent();
						assertTrue("Missing content", content != null);

						String forwardPrefix = stringHelper.getString(MailStrings.FORWARD_PREFIX);
						{
							final InternetAddress[] from = sourceMail.getFrom();
							forwardPrefix = forwardPrefix.replaceFirst("#FROM#", from == null || from.length == 0 ? ""
									: from[0].toUnicodeString());
						}
						{
							final InternetAddress[] to = sourceMail.getTo();
							forwardPrefix = forwardPrefix.replaceFirst("#TO#", to == null || to.length == 0 ? ""
									: addrs2String(to));
						}
						{
							final Date date = sourceMail.getSentDate();
							try {
								forwardPrefix = forwardPrefix.replaceFirst("#DATE#", date == null ? "" : DateFormat
										.getDateInstance(DateFormat.LONG, locale).format(date));
							} catch (final Throwable t) {
								t.printStackTrace();
								forwardPrefix = forwardPrefix.replaceFirst("#DATE#", "");
							}
							try {
								forwardPrefix = forwardPrefix.replaceFirst("#TIME#", date == null ? "" : DateFormat
										.getTimeInstance(DateFormat.SHORT, locale).format(date));
							} catch (final Throwable t) {
								t.printStackTrace();
								forwardPrefix = forwardPrefix.replaceFirst("#TIME#", "");
							}

						}
						forwardPrefix = forwardPrefix.replaceFirst("#SUBJECT#", sourceMail.getSubject());
						forwardPrefix = new StringBuilder(forwardPrefix.length() + 4).append("\r\n\r\n").append(
								forwardPrefix).toString();
						assertTrue("Missing forward prefix", content.toString().startsWith(forwardPrefix));

					} else {
						assertTrue("Unexpected content type in file attachment", part.getContentType().isMimeType(
								MIMETypes.MIME_TEXT_ALL_CARD));
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
			fail(e.getLocalizedMessage());
		}
	}

	public void testMailForwardAnother() {
		try {
			final MailMessage sourceMail = MIMEMessageConverter.convertMessage(RFC822_FORWARD.getBytes("US-ASCII"));

			final Context ctx = new ContextImpl(getCid());
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(), ctx, "mail-test-session");
			session.setPassword(getPassword());

			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {

				final MailMessage forwardMail = mailAccess.getLogicTools().getFowardMessage(
						new MailMessage[] { sourceMail });

				forwardMail.writeTo(System.out);

				{
					final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(getUser(),
							ctx.getContextId());
					assertTrue("Header 'From' does not carry expected value", forwardMail.getFrom()[0]
							.equals(new InternetAddress(usm.getSendAddr(), true)));
				}

				final Locale locale = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale();
				final StringHelper stringHelper = new StringHelper(locale);
				{
					final String subjectPrefix = stringHelper.getString(MailStrings.FORWARD_SUBJECT_PREFIX);
					final String subject = new StringBuilder(32).append(subjectPrefix).append(sourceMail.getSubject())
							.toString();
					assertTrue("Header 'Subject' does not carry expected value", subject.equals(forwardMail
							.getSubject()));
				}

				assertTrue("Header 'Content-Type' does not carry expected value", forwardMail.getContentType()
						.isMimeType(MIMETypes.MIME_TEXT_PLAIN));

				final int count = forwardMail.getEnclosedCount();
				assertTrue("Unexpected number of enclosed parts: " + count, count == MailPart.NO_ENCLOSED_PARTS);

				final Object content = forwardMail.getContent();
				assertTrue("Missing content", content != null);

				String forwardPrefix = stringHelper.getString(MailStrings.FORWARD_PREFIX);
				{
					final InternetAddress[] from = sourceMail.getFrom();
					forwardPrefix = forwardPrefix.replaceFirst("#FROM#", from == null || from.length == 0 ? ""
							: from[0].toUnicodeString());
				}
				{
					final InternetAddress[] to = sourceMail.getTo();
					forwardPrefix = forwardPrefix.replaceFirst("#TO#", to == null || to.length == 0 ? ""
							: addrs2String(to));
				}
				{
					final Date date = sourceMail.getSentDate();
					try {
						forwardPrefix = forwardPrefix.replaceFirst("#DATE#", date == null ? "" : DateFormat
								.getDateInstance(DateFormat.LONG, locale).format(date));
					} catch (final Throwable t) {
						t.printStackTrace();
						forwardPrefix = forwardPrefix.replaceFirst("#DATE#", "");
					}
					try {
						forwardPrefix = forwardPrefix.replaceFirst("#TIME#", date == null ? "" : DateFormat
								.getTimeInstance(DateFormat.SHORT, locale).format(date));
					} catch (final Throwable t) {
						t.printStackTrace();
						forwardPrefix = forwardPrefix.replaceFirst("#TIME#", "");
					}

				}
				forwardPrefix = forwardPrefix.replaceFirst("#SUBJECT#", sourceMail.getSubject());
				forwardPrefix = new StringBuilder(forwardPrefix.length() + 4).append("\r\n\r\n").append(
						forwardPrefix).toString();
				assertTrue("Missing forward prefix", content.toString().startsWith(forwardPrefix));

			} finally {
				/*
				 * close
				 */
				mailAccess.close(false);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Creates a {@link String} from given array of {@link InternetAddress}
	 * instances through invoking {@link InternetAddress#toUnicodeString()}
	 * 
	 * @param addrs
	 *            The rray of {@link InternetAddress} instances
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
