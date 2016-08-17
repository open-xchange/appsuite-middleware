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

package com.openexchange.ajax.conversion;

import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.conversion.actions.ConvertRequest;
import com.openexchange.ajax.conversion.actions.ConvertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.FolderAndID;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.ajax.mail.netsol.NetsolTestConstants;
import com.openexchange.ajax.mail.netsol.actions.NetsolDeleteRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolGetRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolGetResponse;
import com.openexchange.ajax.mail.netsol.actions.NetsolSendRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolSendResponse;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link VCardMailPartImportTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class VCardMailPartImportTest extends AbstractConversionTest {

	private static final byte[] VCARD_BYTES = String.valueOf(
			"" + "BEGIN:VCARD\n" + "VERSION:2.1\n" + "FN:Mustermann, Thomas\n"
					+ "N:Mustermann;Thomas;;Dipl.,Informatiker;\n" + "BDAY:19851213\n"
					+ "ADR;TYPE=work:;;Martinstr. 41;Olpe;NRW;57462;DE\n"
					+ "ADR;TYPE=home:;;Musterstr. 10;Olpe;NRW;57666;Deutschland\n"
					+ "TEL;TYPE=work;TYPE=voice:+49 (2761) 8385-16\n" + "TEL;TYPE=work;TYPE=fax:+49 (2761) 8385-30\n"
					+ "TEL;TYPE=home;TYPE=voice:+49 2761 / 843 157\n" + "TEL;TYPE=cell;TYPE=voice:0171 / 835 72 89\n"
					+ "EMAIL:thomas.mustermann@open-xchange.com\n" + "ORG:OX Software GmbH;Development\n"
					+ "REV:20080818T064153.771Z\n" + "UID:5@ox6-unstable.netline.de\n" + "END:VCARD").getBytes();

	/**
	 * Initializes a new {@link VCardMailPartImportTest}
	 *
	 * @param name
	 *            The name
	 */
	public VCardMailPartImportTest(final String name) {
		super(name);
	}

	/**
	 * Tests the <code>action=convert</code> request
	 *
	 * @throws Throwable
	 */
	public void testVCardImport() throws Throwable {
		final String[] mailFolderAndMailID;
		try {
			/*
			 * Create a mail with VCard attachment
			 */
			final JSONObject mailObject_25kb = new JSONObject();
			{
				mailObject_25kb.put(MailJSONField.FROM.getKey(), getClient().getValues().getSendAddress());
				mailObject_25kb.put(MailJSONField.RECIPIENT_TO.getKey(), getClient().getValues().getSendAddress());
				mailObject_25kb.put(MailJSONField.RECIPIENT_CC.getKey(), "");
				mailObject_25kb.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
				mailObject_25kb.put(MailJSONField.SUBJECT.getKey(), "The mail subject");
				mailObject_25kb.put(MailJSONField.PRIORITY.getKey(), "3");

				final JSONObject bodyObject = new JSONObject();
				bodyObject.put(MailJSONField.CONTENT_TYPE.getKey(), MailContentType.ALTERNATIVE.toString());
				bodyObject.put(MailJSONField.CONTENT.getKey(), NetsolTestConstants.MAIL_TEXT_BODY);

				final JSONArray attachments = new JSONArray();
				attachments.put(bodyObject);

				mailObject_25kb.put(MailJSONField.ATTACHMENTS.getKey(), attachments);
			}

			InputStream in = null;
			try {
				in = new UnsynchronizedByteArrayInputStream(VCARD_BYTES);
				/*
				 * Perform send
				 */
				final NetsolSendResponse response = Executor.execute(getSession(),
						new NetsolSendRequest(mailObject_25kb.toString(), in, "text/x-vcard; charset=US-ASCII",
								"vcard.vcf"));
				assertTrue("Send failed", response.getFolderAndID() != null);
				assertTrue("Duration corrupt", response.getRequestDuration() > 0);
				mailFolderAndMailID = response.getFolderAndID();
			} finally {
				if (null != in) {
					in.close();
				}
			}

			try {
				Long.parseLong(mailFolderAndMailID[1]);
			} catch (final NumberFormatException e) {
				int pos = mailFolderAndMailID[1].lastIndexOf('/');
				if (pos == -1) {
					pos = mailFolderAndMailID[1].lastIndexOf('.');
					if (pos == -1) {
						fail("UNKNOWN FORMAT FOR MAIL ID: " + mailFolderAndMailID[1]);
					}
				}
				final String substr = mailFolderAndMailID[1].substring(pos + 1);
				try {
					Long.parseLong(substr);
				} catch (final NumberFormatException e1) {
					fail("UNKNOWN FORMAT FOR MAIL ID: " + mailFolderAndMailID[1]);
				}
				mailFolderAndMailID[1] = substr;
			}

			try {
				/*
				 * Get previously sent mail
				 */
				final FolderAndID mailPath = new FolderAndID(mailFolderAndMailID[0], mailFolderAndMailID[1]);
				final NetsolGetResponse resp = Executor.execute(getSession(),
						new NetsolGetRequest(mailPath, true));
				final JSONObject mailObject = (JSONObject) resp.getData();
				final JSONArray attachments = mailObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
				final int len = attachments.length();
				String sequenceId = null;
				for (int i = 0; i < len && sequenceId == null; i++) {
					final JSONObject attachObj = attachments.getJSONObject(i);
					if (attachObj.getString(MailJSONField.CONTENT_TYPE.getKey()).startsWith("text/x-vcard")) {
						sequenceId = attachObj.getString(MailListField.ID.getKey());
					}
				}
				/*
				 * Trigger conversion
				 */
				final JSONObject jsonBody = new JSONObject();
				final JSONObject jsonSource = new JSONObject().put("identifier", "com.openexchange.mail.vcard");
				jsonSource.put("args", new JSONArray().put(
						new JSONObject().put("com.openexchange.mail.conversion.fullname", mailFolderAndMailID[0])).put(
						new JSONObject().put("com.openexchange.mail.conversion.mailid", mailFolderAndMailID[1])).put(
						new JSONObject().put("com.openexchange.mail.conversion.sequenceid", sequenceId)));
				jsonBody.put("datasource", jsonSource);
				final JSONObject jsonHandler = new JSONObject().put("identifier", "com.openexchange.contact");
				jsonHandler.put("args", new JSONArray().put(new JSONObject().put(
						"com.openexchange.groupware.contact.folder", getPrivateContactFolder())));
				jsonBody.put("datahandler", jsonHandler);
				final ConvertResponse convertResponse = (ConvertResponse) Executor.execute(getSession(),
						new ConvertRequest(jsonBody, true));
				final String[][] sa = convertResponse.getFoldersAndIDs();

				assertFalse("Missing response on action=convert", sa == null);
				assertTrue("Unexpected response length", sa.length == 1);
			} finally {
				if (mailFolderAndMailID != null) {
					final FolderAndID mailPath = new FolderAndID(mailFolderAndMailID[0], mailFolderAndMailID[1]);
					Executor.execute(getSession(), new NetsolDeleteRequest(new FolderAndID[] { mailPath }, true));
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

}
