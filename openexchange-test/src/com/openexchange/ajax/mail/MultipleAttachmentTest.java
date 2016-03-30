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
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.MultipleAttachmentRequest;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;

/**
 * {@link MultipleAttachmentTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MultipleAttachmentTest extends AbstractMailTest {

	/**
	 * Default constructor.
	 *
	 * @param name
	 *            Name of this test.
	 */
	public MultipleAttachmentTest(final String name) {
		super(name);
	}

	/**
	 * Tests the <code>action=zip_attachments</code> request on INBOX folder
	 *
	 * @throws Throwable
	 */
	public void testGet() throws Throwable {
		String[] folderAndID = null;
		try {
			{
			    final String eml =
		            ("Date: Mon, 19 Nov 2012 21:36:51 +0100 (CET)\n" +
		            "From: #ADDR#\n" +
		            "To: #ADDR#\n" +
		            "Message-ID: <1508703313.17483.1353357411049>\n" +
		            "Subject: MultipleAttachmentTest\n" +
		            "MIME-Version: 1.0\n" +
		            "Content-Type: multipart/alternative; \n" +
		            "    boundary=\"----=_Part_17482_1388684087.1353357411002\"\n" +
		            "\n" +
		            "------=_Part_17482_1388684087.1353357411002\n" +
		            "MIME-Version: 1.0\n" +
		            "Content-Type: text/plain; charset=UTF-8\n" +
		            "Content-Transfer-Encoding: 7bit\n" +
		            "\n" +
		            "MultipleAttachmentTest\n" +
		            "------=_Part_17482_1388684087.1353357411002\n" +
		            "MIME-Version: 1.0\n" +
		            "Content-Type: text/html; charset=UTF-8\n" +
		            "Content-Transfer-Encoding: 7bit\n" +
		            "\n" +
		            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">" +
		            " <head>\n" +
		            "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n" +
		            " </head><body style=\"font-family: verdana,geneva; font-size: 10pt; \">\n" +
		            " \n" +
		            "  <div>\n" +
		            "   MultipleAttachmentTest\n" +
		            "  </div>\n" +
		            " \n" +
		            "</body></html>\n" +
		            "------=_Part_17482_1388684087.1353357411002--\n").replaceAll("#ADDR#", getSendAddress());
		        NewMailResponse newMailResponse = getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
		        String folder = newMailResponse.getFolder();
		        String id = newMailResponse.getId();
		        folderAndID = new String [] { folder, id };
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
			final MultipleAttachmentRequest attachmentRequest = new MultipleAttachmentRequest(folderAndID[0], folderAndID[1], new String[] { sequenceId });
			final WebResponse webResponse = Executor.execute4Download(getSession(), attachmentRequest, AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL), AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
			/*
			 * Some assertions
			 */
			assertFalse("Web response does indicate HTML content", webResponse.isHTML());
			assertEquals("No ZIP content", "application/zip", webResponse.getContentType());
			final String disp = webResponse.getHeaderField("Content-disposition");
			assertNotNull("No Content-disposition header", disp);
			// Behavior changed with bug 26879
			//assertTrue("Disposition is not set to 'attachment'", disp.startsWith("attachment"));
			assertTrue("'filename' parameter not found in Content-disposition", disp.indexOf("filename=") >= 0);

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
