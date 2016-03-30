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

import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailBugfixTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailBugfixTest extends AbstractMailTest {

	private static final byte[] MAIL_BYTES = String.valueOf(
			"Delivery-date: Tue, 21 Oct 2008 16:04:37 +0200\r\n" + "Message-ID: <48FDE207.60801@hostpoint.ch>\r\n"
					+ "Date: Tue, 21 Oct 2008 16:07:03 +0200\r\n" + "From: Foo Bar <foo.bar@somewhere.com>\r\n"
					+ "User-Agent: Thunderbird 2.0.0.0 (X11/20070326)\r\n" + "MIME-Version: 1.0\r\n"
					+ "To: Bar Foo <bar.foo@nowhere.org>\r\n" + "Subject: Test Bildanhang\r\n"
					+ "Content-Type: multipart/mixed;\r\n" + " boundary=\"------------080501040409070402000300\"\r\n"
					+ "\r\n" + "This is a multi-part message in MIME format.\r\n"
					+ "--------------080501040409070402000300\r\n"
					+ "Content-Type: text/plain; charset=ISO-8859-15; format=flowed\r\n"
					+ "Content-Transfer-Encoding: 7bit\r\n" + "\r\n" + "More phun!\r\n" + "\r\n"
					+ "--------------080501040409070402000300\r\n" + "Content-Type: image/png;\r\n"
					+ "name=\"hostpoint.png\"\r\n" + "Content-Transfer-Encoding: base64\r\n"
					+ "Content-Disposition: inline;\r\n" + " filename=\"hostpoint.png\"\r\n" + "\r\n"
					+ "iVBORw0KGgoAAAANSUhEUgAAAFAAAAAPCAIAAAD8q9/YAAAA7klEQVRIieVXOw7DIAw1ETfK\r\n"
					+ "ncycJQMHyMDCTO/UKzUdLFHCP6hJ2vAm2zLmvRgZwhARegIHAGPM1TROghCCk8Uez3zqiqPS\r\n"
					+ "8nhKB2KeFgAYrqZxNrjrrDiGGcXm/xf8Dg9bFNfTOXENsl3XRtzkjBstktooUyEKX9Jri+L6\r\n"
					+ "EPO0KC2Vli4bitgpQIY7FFJLohrCYFgwBe75XldrNBe/q+WdIVRMCAu2DVFfcENXaeO8bJuT\r\n"
					+ "YrmXfar5RXyhwykqVoNlVq+q5lC0aWaIaIyhUZyZ0ve4hz8PD8LNbqAoen14RA+zh7Yh8Wtg\r\n"
					+ "vf0tvQHWlp1rhK+ZbQAAAABJRU5ErkJggg==\r\n" + "--------------080501040409070402000300--\r\n")
			.getBytes();

	private static final String INBOX = "INBOX";

	private static final MailField[] FIELDS_ID = { MailField.ID };

	/**
	 * Initializes a new {@link MailBugfixTest}
	 */
	public MailBugfixTest() {
		super();
	}

	/**
	 * Initializes a new {@link MailBugfixTest}
	 *
	 * @param name
	 *            The name
	 */
	public MailBugfixTest(final String name) {
		super(name);
	}

	/**
	 * Test for <a href=
	 * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12357">bug
	 * #12357</a>:<br>
	 *
	 * <pre>
	 * Attachments won't be forwarded when email will be forwarded as inline
	 * </pre>
	 */
	public void testBug12357() {
		try {
			final MailMessage mail = MimeMessageConverter.convertMessage(MAIL_BYTES);

			final SessionObject session = getSession();
			final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			try {
				final String[] uids = mailAccess.getMessageStorage().appendMessages(INBOX, new MailMessage[] { mail });
				try {
					/*
					 * Obtain forward mail with "inlined" original mail.
					 */
					final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(
							session.getUserId(), session.getContextId());
					usm.setNoSave(true);
					usm.setForwardAsAttachment(false);
					final MailMessage forwardMail = MimeForward.getFowardMail(new MailMessage[] { mail }, session, mailAccess.getAccountId(), usm, false);
					/*
					 * Check for image attachment
					 */
					final int count = forwardMail.getEnclosedCount();
					assertFalse("Forward message has no enclosed parts", count == MailMessage.NO_ENCLOSED_PARTS);
					assertEquals("Forward message has no enclosed parts", 2, count);
					final MailPart imgPart = forwardMail.getEnclosedMailPart(1);
					assertFalse("Forward message's image part is null", imgPart == null);
					assertTrue("Unexpected image part content-type", imgPart.getContentType().getBaseType()
							.equalsIgnoreCase("image/png"));
				} finally {
					/*
					 * Hard-delete test mail
					 */
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
