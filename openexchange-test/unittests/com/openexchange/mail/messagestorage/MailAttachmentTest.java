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

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.JsonMessageHandler;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailAttachmentTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailAttachmentTest extends MessageStorageTest {

	private static final String RFC822_WITH_ATTACH = "Return-Path: <thorben.betten@open-xchange.com>\n"
			+ "Received: from ox.netline-is.de ([unix socket])\n"
			+ "	by ox (Cyrus v2.2.3) with LMTP; Fri, 04 Apr 2008 00:12:36 +0200\n" + "X-Sieve: CMU Sieve 2.2\n"
			+ "Received: by ox.netline-is.de (Postfix, from userid 65534)\n"
			+ "	id B0F553DBA6F; Fri,  4 Apr 2008 00:12:36 +0200 (CEST)\n"
			+ "Received: from oxee (unknown [192.168.32.9])\n"
			+ "	by ox.netline-is.de (Postfix) with ESMTP id 3C1C33DBA58\n"
			+ "	for <thorben.betten@open-xchange.com>; Fri,  4 Apr 2008 00:12:36 +0200 (CEST)\n"
			+ "Date: Fri, 4 Apr 2008 00:11:46 +0200 (CEST)\n"
			+ "From: \"Betten, Thorben\" <thorben.betten@open-xchange.com>\n"
			+ "To: \"Betten, Thorben\" <thorben.betten@open-xchange.com>\n"
			+ "Message-ID: <22037327.8941207260706888.JavaMail.open-xchange@oxee>\n"
			+ "Subject: DowngradeRegistry-Patch\n" + "MIME-Version: 1.0\n" + "Content-Type: multipart/mixed; \n"
			+ "	boundary=\"----=_Part_582_17246096.1207260706746\"\n" + "X-Priority: 3\n"
			+ "X-Mailer: Open-Xchange Mailer v6.5.0-6342\n"
			+ "X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\n" + "X-Spam-Level: \n"
			+ "X-Spam-Status: No, hits=-4.6 required=5.0 tests=AWL,BAYES_00,HTML_MESSAGE \n"
			+ "	autolearn=no version=2.64\n" + "\n" + "------=_Part_582_17246096.1207260706746\n"
			+ "Content-Type: multipart/alternative; \n" + "	boundary=\"----=_Part_583_11954091.1207260706746\"\n"
			+ "\n" + "------=_Part_583_11954091.1207260706746\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: text/plain; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n" + "\n"
			+ "(see attached file)\n" + "\n" + "\n" + "--\n" + "with best regards,\n" + "Thorben Betten\n" + "\n"
			+ "_______________________________________________\n" + "Thorben Betten, Software Developer\n"
			+ "Phone +49 2761 8385 16\n" + "Fax +49 2761 8385 30\n"
			+ "Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe\n" + "_______________________________________________\n"
			+ "------=_Part_583_11954091.1207260706746\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: text/html; charset=UTF-8\n" + "Content-Transfer-Encoding: 7bit\n" + "\n"
			+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
			+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + "\n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "  <head>\n"
			+ "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">\n"
			+ "    <meta name=\"generator\"\n"
			+ "    content=\"HTML Tidy for Java (vers. 26 Sep 2004), see www.w3.org\" />\n" + "\n"
			+ "    <title></title>\n" + "  </head>\n" + "\n" + "  <body>\n" + "    <br />\n"
			+ "    (see attached file)<br />\n" + "\n" + "    <div>\n" + "      <br />\n" + "      <br />\n"
			+ "      --<br />\n" + "      with best regards,<br />\n" + "      Thorben Betten<br />\n"
			+ "      <br />\n" + "      _______________________________________________<br />\n"
			+ "      Thorben Betten, Software Developer<br />\n" + "      Phone +49 2761 8385 16<br />\n"
			+ "      Fax +49 2761 8385 30<br />\n" + "      Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe<br />\n"
			+ "      _______________________________________________\n" + "    </div>\n" + "  </body>\n" + "\n"
			+ "</html>\n" + "\n" + "------=_Part_583_11954091.1207260706746--\n" + "\n"
			+ "------=_Part_582_17246096.1207260706746\n" + "Content-Type: text/x-patch; charset=US-ASCII; \n"
			+ "	name=downgrade_registry_patch.diff\n" + "Content-Transfer-Encoding: base64\n"
			+ "Content-Disposition: attachment; filename=downgrade_registry_patch.diff\n" + "\n"
			+ "SW5kZXg6IHNyYy9jb20vb3BlbmV4Y2hhbmdlL2dyb3Vwd2FyZS9kb3duZ3JhZGUvRG93bmdyYWRl\n"
			+ "UmVnaXN0cnkuamF2YQo9PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09\n"
			+ "PT09PT09PT09PT09PT09PT09PT09PT09ClJDUyBmaWxlOiAvY3Zzcm9vdC9vcGVuLXhjaGFuZ2Uv\n"
			+ "c2VydmVyL3NyYy9jb20vb3BlbmV4Y2hhbmdlL2dyb3Vwd2FyZS9kb3duZ3JhZGUvRG93bmdyYWRl\n"
			+ "UmVnaXN0cnkuamF2YSx2CnJldHJpZXZpbmcgcmV2aXNpb24gMS40CmRpZmYgLXUgLXIxLjQgRG93\n"
			+ "bmdyYWRlUmVnaXN0cnkuamF2YQotLS0gc3JjL2NvbS9vcGVuZXhjaGFuZ2UvZ3JvdXB3YXJlL2Rv\n"
			+ "d25ncmFkZS9Eb3duZ3JhZGVSZWdpc3RyeS5qYXZhCTEgQXByIDIwMDggMDk6MzQ6NDMgLTAwMDAJ\n"
			+ "MS40CisrKyBzcmMvY29tL29wZW5leGNoYW5nZS9ncm91cHdhcmUvZG93bmdyYWRlL0Rvd25ncmFk\n"
			+ "ZVJlZ2lzdHJ5LmphdmEJMyBBcHIgMjAwOCAyMTozNDowNCAtMDAwMApAQCAtMjg4LDggKzI4OCwy\n"
			+ "NCBAQAogCXB1YmxpYyB2b2lkIHVucmVnaXN0ZXJEb3duZ3JhZGVMaXN0ZW5lcihmaW5hbCBEb3du\n"
			+ "Z3JhZGVMaXN0ZW5lciBsaXN0ZW5lcikgewogCQlyZWdpc3RyeUxvY2subG9jaygpOwogCQl0cnkg\n"
			+ "ewotCQkJbGlzdGVuZXJzLnJlbW92ZShsaXN0ZW5lcik7Ci0JCQljbGFzc2VzLnJlbW92ZShsaXN0\n"
			+ "ZW5lci5nZXRDbGFzcygpKTsKKwkJCWZpbmFsIENsYXNzPD8gZXh0ZW5kcyBEb3duZ3JhZGVMaXN0\n"
			+ "ZW5lcj4gY2xhenogPSBsaXN0ZW5lci5nZXRDbGFzcygpOworCQkJaWYgKCFjbGFzc2VzLmNvbnRh\n"
			+ "aW5zKGNsYXp6KSkgeworCQkJCXJldHVybjsKKwkJCX0KKwkJCWlmICghbGlzdGVuZXJzLnJlbW92\n"
			+ "ZShsaXN0ZW5lcikpIHsKKwkJCQkvKgorCQkJCSAqIFJlbW92ZSBieSByZWZlcmVuY2UgZGlkIG5v\n"
			+ "dCB3b3JrCisJCQkJICovCisJCQkJaW50IHNpemUgPSBsaXN0ZW5lcnMuc2l6ZSgpOworCQkJCWZv\n"
			+ "ciAoaW50IGkgPSAwOyBpIDwgc2l6ZTsgaSsrKSB7CisJCQkJCWlmIChjbGF6ei5lcXVhbHMobGlz\n"
			+ "dGVuZXJzLmdldChpKS5nZXRDbGFzcygpKSkgeworCQkJCQkJbGlzdGVuZXJzLnJlbW92ZShpKTsK\n"
			+ "KwkJCQkJCS8vIFJlc2V0IHNpemUgdG8gbGVhdmUgbG9vcAorCQkJCQkJc2l6ZSA9IDA7CisJCQkJ\n"
			+ "CX0KKwkJCQl9CisJCQl9CisJCQljbGFzc2VzLnJlbW92ZShjbGF6eik7CiAJCX0gZmluYWxseSB7\n"
			+ "CiAJCQlyZWdpc3RyeUxvY2sudW5sb2NrKCk7CiAJCX0K\n" + "------=_Part_582_17246096.1207260706746--\n";

	private static final String RFC822_WO_ATTACH = "Return-Path: <markus.strotkemper@open-xchange.com>\n"
			+ "Received: from ox.netline-is.de ([unix socket])\n"
			+ "	by ox (Cyrus v2.2.3) with LMTP; Fri, 04 Apr 2008 11:37:57 +0200\n"
			+ "X-Sieve: CMU Sieve 2.2\n"
			+ "Received: by ox.netline-is.de (Postfix, from userid 65534)\n"
			+ "	id E44443DBC7F; Fri,  4 Apr 2008 11:37:56 +0200 (CEST)\n"
			+ "Received: from netline.de (comfire.netline.de [192.168.32.1])\n"
			+ "	by ox.netline-is.de (Postfix) with ESMTP id 250743DBC7D\n"
			+ "	for <thorben@open-xchange.com>; Fri,  4 Apr 2008 11:37:56 +0200 (CEST)\n"
			+ "Received: from [10.20.30.11] (helo=www.open-xchange.org ident=mail)\n"
			+ "	by netline.de with esmtp (Exim)\n"
			+ "	id 1JhiCw-000368-00\n"
			+ "	for thorben@open-xchange.com; Fri, 04 Apr 2008 11:27:38 +0200\n"
			+ "Received: from mail.open-xchange.com ([10.20.30.22] helo=ox.open-xchange.com)\n"
			+ "	by www.open-xchange.org with esmtp (Exim 3.36 #1 (Debian))\n"
			+ "	id 1JhiM6-0006Dp-00\n"
			+ "	for <thorben@open-xchange.org>; Fri, 04 Apr 2008 11:37:06 +0200\n"
			+ "Received: by ox.open-xchange.com (Postfix, from userid 76)\n"
			+ "	id C232532CDFA; Fri,  4 Apr 2008 11:37:05 +0200 (CEST)\n"
			+ "Received: from ox.open-xchange.com ([unix socket])\n"
			+ "	 by ox.open-xchange.com (Cyrus v2.2.12-Invoca-RPM-2.2.12-8.1.RHEL4) with LMTPA;\n"
			+ "	 Fri, 04 Apr 2008 11:37:05 +0200\n"
			+ "X-Sieve: CMU Sieve 2.2\n"
			+ "Received: by ox.open-xchange.com (Postfix, from userid 99)\n"
			+ "	id 7FEAA32CE1A; Fri,  4 Apr 2008 11:37:05 +0200 (CEST)\n"
			+ "Received: from oxee (unknown [192.168.32.9])\n"
			+ "	by ox.open-xchange.com (Postfix) with ESMTP id 4BF3832CD76\n"
			+ "	for <thorben.betten@open-xchange.com>; Fri,  4 Apr 2008 11:37:04 +0200 (CEST)\n"
			+ "Date: Fri, 4 Apr 2008 11:37:04 +0200 (CEST)\n"
			+ "From: \"Strotkemper, Markus\" <markus.strotkemper@open-xchange.com>\n"
			+ "To: \"Betten, Thorben\" <thorben.betten@open-xchange.com>\n"
			+ "Message-ID: <3231881.9631207301824285.JavaMail.open-xchange@oxee>\n"
			+ "In-Reply-To: <30694952.9571207301412448.JavaMail.open-xchange@oxee>\n"
			+ "References: <23142826.9271207296337478.JavaMail.open-xchange@oxee> <30694952.9571207301412448.JavaMail.open-xchange@oxee>\n"
			+ "Subject: Re: port 2144\n" + "MIME-Version: 1.0\n" + "Content-Type: text/plain; charset=UTF-8\n"
			+ "Content-Transfer-Encoding: quoted-printable\n" + "X-Priority: 3\n"
			+ "X-Mailer: Open-Xchange Mailer v6.5.0-6342\n"
			+ "X-Scanner: exiscan *1JhiCw-000368-00*GURR6OLwjOM* http://duncanthrax.net/exiscan/\n"
			+ "X-Spam-Checker-Version: SpamAssassin 2.64 (2004-01-11) on ox.netline-is.de\n" + "X-Spam-Level: \n"
			+ "X-Spam-Status: No, hits=-4.8 required=5.0 tests=AWL,BAYES_00 autolearn=ham \n" + "	version=2.64\n"
			+ "\n" + "Du, das geht bei mir auch net. Die scheinen den wieder deaktiviert zu haben=\n" + "....\n" + "\n"
			+ "\n" + "\n" + "\n" + "\"Betten, Thorben\" <thorben.betten@open-xchange.com> hat am 4. April 2008 um=\n"
			+ " 11:30 geschrieben:\n" + "\n" + "> Hmm...\n" + ">=20\n" + "> funzt leider immer noch nicht:\n"
			+ ">=20\n" + "> thorben@thorben:~$ telnet ns0.ovh.net 2144\n" + "> Trying 213.186.33.20...\n" + ">=20\n"
			+ "> Kommt leider kein Connect zustande...\n" + ">=20\n"
			+ "> \"Strotkemper, Markus\" <markus.strotkemper@open-xchange.com> hat am 4. Apr=\n" + "il\n"
			+ "> 2008 um 10:05 geschrieben:\n" + ">=20\n" + "> > Hi,\n" + "> >=20\n"
			+ "> > hab Dir Port TCP2144 nach au=C3=9Fen freigegeben. Falls Daniel fragt: w=\n" + "ozu ist\n"
			+ "> > der? Und kannst Du mir bitte bescheit geben wenn Du ihn nicht mehr\n" + "> > ben=C3=B6tigst?\n"
			+ "> >=20\n" + "> > Besten Dank,\n" + "> > Markus\n" + ">=20\n" + ">=20\n" + ">=20\n" + ">=20\n" + "> --\n"
			+ "> with best regards,\n" + "> Thorben Betten\n" + ">=20\n"
			+ "> _______________________________________________\n" + "> Thorben Betten, Software Developer\n"
			+ "> Phone +49 2761 8385 16\n" + "> Fax +49 2761 8385 30\n"
			+ "> Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe\n"
			+ "> _______________________________________________\n";

	private static final String RFC2231 = "From: Marcus Klein <m.klein@open-xchange.com>\n"
			+ "Organization: Netline Internet Service GmbH\n"
			+ "X-KMail-Fcc: sent-mail\n"
			+ "To: marcus@1337\n"
			+ "Date: Wed, 9 Jan 2008 11:01:10 +0100\n"
			+ "User-Agent: KMail/1.9.7\n"
			+ "MIME-Version: 1.0\n"
			+ "Content-Type: Multipart/Mixed;\n"
			+ "  boundary=\"Boundary-00=_mtJhHd7H54sG6XG\"\n"
			+ "X-KMail-Recipients: marcus@1337\n"
			+ "Status: R\n"
			+ "Subject: RFC2231-Test\n"
			+ "X-Status: N\n"
			+ "X-KMail-EncryptionState:  \n"
			+ "X-KMail-SignatureState:  \n"
			+ "X-KMail-MDN-Sent:  \n"
			+ "X-Length: 1307\n"
			+ "X-UID: 6\n"
			+ "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG\n"
			+ "Content-Type: text/plain;\n"
			+ "  charset=\"utf-8\"\n"
			+ "Content-Transfer-Encoding: 7bit\n"
			+ "Content-Disposition: inline\n"
			+ "\n"
			+ "\n"
			+ "-- \n"
			+ "Marcus Klein\n"
			+ "--\n"
			+ "Netline Internet Service GmbH\n"
			+ "\n"
			+ "There are 10 kinds of humans - those, who understand the\n"
			+ "binary system, and those, who do not understand it.\n"
			+ "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG\n"
			+ "Content-Type: text/plain;\n"
			+ "  charset=\"utf-8\";\n"
			+ "  name*=utf-8''test%20%C3%A4%C3%B6%C3%BC%2Etxt\n"
			+ "Content-Transfer-Encoding: base64\n"
			+ "Content-Disposition: attachment;\n"
			+ "	filename*=utf-8''test%20%C3%A4%C3%B6%C3%BC%2Etxt\n"
			+ "\n"
			+ "dGVzdMOkw7bDvAo=\n"
			+ "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG\n"
			+ "Content-Type: text/plain; charset=\"utf-8\"; name*1*=utf-8''%EC%84%9C%EC%98%81%EC%A7%84; name*2*=funny%2Etxt\n"
			+ "Content-Transfer-Encoding: base64\n" + "Content-Disposition: attachment;\n"
			+ "	filename*=utf-8''%EC%84%9C%EC%98%81%EC%A7%84%2Etxt\n" + "\n" + "7ISc7JiB7KeE\n" + "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG--\n";

	/**
	 *
	 */
	public MailAttachmentTest() {
		super();
	}

	public void testMailAttachment() {
		try {
			final MailAccess<?, ?> mailAccess = getMailAccess();

			final MailMessage[] mails = getMessages(getTestMailDir(), -1);
			final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", mails);
			try {

				MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages("INBOX", uids, FIELDS_ID);
				for (int i = 0; i < fetchedMails.length; i++) {
					assertFalse("Mail ID is null", fetchedMails[i].getMailId() == null);
				}

				final Set<String> hasAttachmentSet = new HashSet<String>(uids.length);
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
					if (fetchedMails[i].hasAttachment()) {
						hasAttachmentSet.add(fetchedMails[i].getMailId());
					}
				}

				for (final String id : hasAttachmentSet) {
					final MailMessage mail = mailAccess.getMessageStorage().getMessage("INBOX", id, true);
					final MailPath mailPath = new MailPath(mailAccess.getAccountId(), mail.getFolder(), mail.getMailId());

					final SessionObject session = getSession();
					final JsonMessageHandler messageHandler = new JsonMessageHandler(MailAccount.DEFAULT_ID, mailPath, mail,
							DisplayMode.DISPLAY, false, session, UserSettingMailStorage.getInstance().getUserSettingMail(
									session.getUserId(), session.getContextId()), false, -1);
					new MailMessageParser().parseMailMessage(mail, messageHandler);
					final JSONObject jObject = messageHandler.getJSONObject();
					if (jObject.has(MailJSONField.ATTACHMENTS.getKey())) {
						final JSONArray jArray = jObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
						final int len = jArray.length();
						assertTrue("Missing attachments although existence indicated through 'hasAttachments()'",
								len > 0);
						for (int i = 0; i < len; i++) {
							final String sequenceId = jArray.getJSONObject(i).getString(MailListField.ID.getKey());
							final MailPart part = mailAccess.getMessageStorage().getAttachment("INBOX", id,
									sequenceId);
							assertFalse("No mail part found for sequence ID: " + sequenceId, null == part);
						}
					} else {
						fail("Missing attachments although existence indicated through 'hasAttachments()'");
					}
				}

			} finally {

				mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);

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

	    // TODO: Should not be part of "real" MAL tests
    public void testNoAttachmentMIMEMessageConverter() throws OXException, UnsupportedEncodingException {
        final MailMessage testMail = MimeMessageConverter.convertMessage(RFC822_WO_ATTACH.getBytes(com.openexchange.java.Charsets.US_ASCII));
        assertTrue("Missing hasAttachment", testMail.containsHasAttachment());
        assertFalse("A message w/o attachments is marked to hold attachments", testMail.hasAttachment());
	}

    // TODO: Should not be part of "real" MAL tests
	public void testHasAttachmentMIMEMessageConverter() throws OXException, UnsupportedEncodingException {
	    final MailMessage testMail = MimeMessageConverter.convertMessage(RFC822_WITH_ATTACH.getBytes(com.openexchange.java.Charsets.US_ASCII));
        assertTrue("Missing hasAttachment", testMail.containsHasAttachment());
        assertTrue("A message with attachments is marked to NOT hold attachments", testMail.hasAttachment());
	}

	public void testRFC2231() {
		try {
			final SessionObject session = SessionObjectWrapper.createSessionObject(getUser(),
					new ContextImpl(getCid()), "mail-test-session");
			session.setPassword(getPassword());

			final MailMessage rfc2231Mail = MimeMessageConverter.convertMessage(RFC2231.getBytes(com.openexchange.java.Charsets.US_ASCII));
			final JsonMessageHandler messageHandler = new JsonMessageHandler(MailAccount.DEFAULT_ID, null, rfc2231Mail, DisplayMode.DISPLAY, false,
					session, UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(),
							session.getContextId()), false, -1);
			new MailMessageParser().parseMailMessage(rfc2231Mail, messageHandler);
			final JSONObject jObject = messageHandler.getJSONObject();
			if (jObject.has(MailJSONField.ATTACHMENTS.getKey())) {
				final JSONArray jArray = jObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
				final int len = jArray.length();
				assertTrue("Missing attachments although existence indicated through 'hasAttachments()'", len > 0);
				for (int i = 0; i < len; i++) {
					final JSONObject attachObj = jArray.getJSONObject(i);
					if (attachObj.has(MailJSONField.ATTACHMENT_FILE_NAME.getKey())) {
						final String filename = attachObj.getString(MailJSONField.ATTACHMENT_FILE_NAME.getKey());
						assertTrue("Unexpected filename: " + filename, "\uc11c\uc601\uc9c4.txt".equals(filename) || "test \u00e4\u00f6\u00fc.txt".equals(filename));
					}
				}
			} else {
				fail("Missing attachments although existence indicated through 'hasAttachments()'");
			}

		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
