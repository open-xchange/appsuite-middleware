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

package com.openexchange.ajax.mail;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.mail.MailJSONField;

/**
 * {@link SendTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SendTest extends AbstractMailTest {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SendTest.class);

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            Name of this test.
	 */
	public SendTest(final String name) {
		super(name);
	}

	/**
	 * Tests the <code>action=new</code> request on INBOX folder
	 * 
	 * @throws Throwable
	 */
	public void testSend() throws Throwable {
		final JSONObject mailObject = new JSONObject();
		{
			mailObject.put(MailJSONField.FROM.getKey(), getSendAddress());
			mailObject.put(MailJSONField.RECIPIENT_TO.getKey(), getSendAddress());
			mailObject.put(MailJSONField.RECIPIENT_CC.getKey(), "");
			mailObject.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
			mailObject.put(MailJSONField.SUBJECT.getKey(), "The mail subject");
			mailObject.put(MailJSONField.PRIORITY.getKey(), "3");

			final JSONObject bodyObject = new JSONObject();
			bodyObject.put(MailJSONField.CONTENT_TYPE.getKey(), "ALTERNATIVE");
			bodyObject
					.put(
							MailJSONField.CONTENT.getKey(),
							"Mail text.<br /><br />People have been asking for support for the IMAP IDLE com"
									+ "mand for quite<br />a few years and I think I've finally figured out how to provide such<br />support safely.  The difficulty isn't in "
									+ "executing the command, which<br />is quite straightforward, the difficulty is in deciding how to expose<br />it to applications, and in"
									+ "handling the multithreading issues that<br />arise.<br /><br />After three attempts, I've got a version that seems to work.  It passes"
									+ "<br />all my tests, including a multithreading test I wrote just for this<br />purpose.  So now it's time for others to try it out as w"
									+ "ell.  Below is<br />my writeup on how to use the IDLE command.  You can find the test<br />version of JavaMail (essentially an early ve"
									+ "rsion of JavaMail 1.4.1)<br />in the java.net Maven repository (you want the 1.4.1ea version):<br /><br />https://maven-repository.dev."
									+ "java.net/nonav/repository/javax.mail/<br /><br />Note that this version is built with JDK 1.5 and thus requires JDK 1.5.<br /><br />Oh,"
									+ "and here's the entire list of what's fixed in this version so far:<br /><br />4107594 IMAP implementation should use the IDLE extensio"
									+ "n if available<br />6423701 Problem with using OrTerm when the protocol is IMAP<br />6431207 SMTP is adding extra CRLF to message conte"
									+ "nt<br />6447295 IMAPMessage fails to return Content-Language from bodystructure<br />6447799 encoded text not decoded even when mail.mi"
									+ "me.decodetext.strict is false<br />6447801 MimeBodyPart.writeTo reencodes data unnecessarily<br />6456422 NullPointerException in smtpt"
									+ "ransport when sending MimeMessages<br />        with no encoding<br />6456444 MimeMessages created from stream are not correctly handle"
									+ "d<br />        with allow8bitmime<br />&lt;no id&gt; fix performance bug in base64 encoder; now even faster!");

			final JSONArray attachments = new JSONArray();
			attachments.put(bodyObject);

			mailObject.put(MailJSONField.ATTACHMENTS.getKey(), attachments);
		}

		final SendResponse response = (SendResponse) Executor.execute(getSession(), new SendRequest(mailObject
				.toString()));
		assertTrue("ERROR OCCURED!", response.getException() == null);

	}
}
