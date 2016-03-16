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

package com.openexchange.ajax.mail;

import org.json.JSONArray;
import org.json.JSONObject;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AttachmentRequest;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;

/**
 * {@link AttachmentTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AttachmentTest extends AbstractMailTest {

	/**
	 * Default constructor.
	 *
	 * @param name
	 *            Name of this test.
	 */
	public AttachmentTest(final String name) {
		super(name);
	}

	/**
	 * Tests the <code>action=attachment</code> request on INBOX folder
	 *
	 * @throws Throwable
	 */
	public void testGet() throws Throwable {
		String[] folderAndID = null;
		try {
			{
				/*
				 * Create JSON mail object
				 */
				final String mailObject_25kb = createSelfAddressed25KBMailObject().toString();
				/*
				 * Insert mail through a send request
				 */
				folderAndID = Executor.execute(getSession(), new SendRequest(mailObject_25kb))
						.getFolderAndID();
			}
			/*
			 * Perform action=get
			 */
			final GetResponse response = Executor.execute(getSession(), new GetRequest(folderAndID[0],
					folderAndID[1]));
			/*
			 * Get mail's JSON representation
			 */
			final JSONObject mailObject = (JSONObject) response.getResponse().getData();
			/*
			 * Some assertions
			 */
			assertTrue("Missing field " + MailJSONField.ATTACHMENTS.getKey(), mailObject.has(MailJSONField.ATTACHMENTS
					.getKey())
					&& !mailObject.isNull(MailJSONField.ATTACHMENTS.getKey()));
			final JSONArray attachmentArray = mailObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
			final int len = attachmentArray.length();
			assertTrue("Missing attachments", len > 0);
			/*
			 * Iterate over attachments
			 */
			String sequenceId = null;
			for (int i = 0; i < len && sequenceId == null; i++) {
				final JSONObject attachmentObject = attachmentArray.getJSONObject(i);
				final String contentType = attachmentObject.getString(MailJSONField.CONTENT_TYPE.getKey());
				if (contentType.regionMatches(true, 0, "text/htm", 0, 8)) {
					sequenceId = attachmentObject.getString(MailListField.ID.getKey());
				}
			}
			assertTrue("No HTML part found", sequenceId != null);
			/*
			 * Perform action=attachment
			 */
			final AttachmentRequest attachmentRequest = new AttachmentRequest(folderAndID[0], folderAndID[1], sequenceId);
			attachmentRequest.setSaveToDisk(false);
			attachmentRequest.setFilter(true);
			final WebResponse webResponse = Executor.execute4Download(getSession(), attachmentRequest, AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL), AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
			/*
			 * Some assertions
			 */
			assertTrue("Web response does not indicate HTML content", webResponse.isHTML());


		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			if (folderAndID != null) {
				final String[][] foo = new String[1][];
				foo[0] = folderAndID;
				Executor.execute(getSession(), new DeleteRequest(foo, true));
			}
		}
	}
}
