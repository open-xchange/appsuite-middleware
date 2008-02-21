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

package com.openexchange.ajax.mail.netsol;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.netsol.actions.NetsolGetRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolGetResponse;
import com.openexchange.ajax.mail.netsol.actions.NetsolSendRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolSendResponse;
import com.openexchange.mail.MailJSONField;

/**
 * {@link NetsolTestMailMessageOpen}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class NetsolTestMailMessageOpen extends AbstractNetsolTest {

	/**
	 * Initializes a new {@link NetsolTestMailMessageOpen}
	 * 
	 * @param name
	 */
	public NetsolTestMailMessageOpen(final String name) {
		super(name);
	}

	public void testMailOpen() throws Throwable {
		netsolClearFolder(getInboxFolder());
		netsolClearFolder(getSentFolder());
		netsolClearFolder(getTrashFolder());
		System.out.println("INBOX, Sent & Trash cleared");

		/*
		 * Create 25kb mail object
		 */
		final JSONObject mailObject_25kb = new JSONObject();
		{
			mailObject_25kb.put(MailJSONField.FROM.getKey(), getSendAddress());
			mailObject_25kb.put(MailJSONField.RECIPIENT_TO.getKey(), getSendAddress());
			mailObject_25kb.put(MailJSONField.RECIPIENT_CC.getKey(), "");
			mailObject_25kb.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
			mailObject_25kb.put(MailJSONField.SUBJECT.getKey(), "The mail subject");
			mailObject_25kb.put(MailJSONField.PRIORITY.getKey(), "3");

			final JSONObject bodyObject = new JSONObject();
			bodyObject.put(MailJSONField.CONTENT_TYPE.getKey(), "ALTERNATIVE");
			bodyObject.put(MailJSONField.CONTENT.getKey(), NetsolTestConstants.MAIL_TEXT_BODY + "<br />"
					+ NetsolTestConstants.MAIL_TEXT_BODY + "<br />" + NetsolTestConstants.MAIL_TEXT_BODY + "<br />"
					+ NetsolTestConstants.MAIL_TEXT_BODY + "<br />" + NetsolTestConstants.MAIL_TEXT_BODY + "<br />"
					+ NetsolTestConstants.MAIL_TEXT_BODY + "<br />" + NetsolTestConstants.MAIL_TEXT_BODY + "<br />");

			final JSONArray attachments = new JSONArray();
			attachments.put(bodyObject);

			mailObject_25kb.put(MailJSONField.ATTACHMENTS.getKey(), attachments);
		}
		/*
		 * Put single mail into "Sent" folder
		 */
		final NetsolSendResponse response = (NetsolSendResponse) Executor.execute(getSession(), new NetsolSendRequest(
				mailObject_25kb.toString()));
		final FolderAndID mailPath = new FolderAndID(response.getFolderAndID()[0], response.getFolderAndID()[1]);

		final int runs = 10;
		final DurationTracker durationTracker = new DurationTracker(runs);
		System.out.println("Starting test runs...");
		for (int i = 0; i < runs; i++) {
			/*
			 * "Open" 25kb mail
			 */
			NetsolGetResponse resp = ((NetsolGetResponse) Executor.execute(getSession(),
					new NetsolGetRequest(mailPath), true));
			assertTrue("Get failed", resp.getMail(getTimeZone()) != null);
			assertTrue("Duration corrupt", resp.getDuration() > 0);
			durationTracker.addDuration(resp.getDuration());
		}
		System.out.println("Mail Open: Test runs finished");
		System.out.println(durationTracker.toString());
		/*
		 * Clean everything
		 */
		netsolClearFolder(getInboxFolder());
		netsolClearFolder(getSentFolder());
		netsolClearFolder(getTrashFolder());
		System.out.println("INBOX, Sent & Trash cleared");
	}

}
