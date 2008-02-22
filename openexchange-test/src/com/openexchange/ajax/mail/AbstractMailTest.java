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

import java.io.IOException;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link AbstractMailTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class AbstractMailTest extends AbstractAJAXSession {

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            name of the test.
	 */
	protected AbstractMailTest(final String name) {
		super(name);
	}

	/**
	 * Converts specified object array into a corresponding <i>folder-and-ID</i>-array
	 * 
	 * @param array
	 * @return
	 */
	protected static final String[][] toFolderAndIDs(final Object[][] array) {
		final String[][] retval = new String[array.length][];
		for (int i = 0; i < retval.length; i++) {
			if (array[i] == null || array[i].length != 2) {
				return null;
			}
			retval[i] = new String[array[i].length];
			retval[i][0] = array[i][0] == null ? null : array[i][0].toString();
			retval[i][1] = array[i][1] == null ? null : array[i][1].toString();
		}
		return retval;
	}

	/**
	 * Random mail text body
	 */
	protected static final String MAIL_TEXT_BODY = "Mail text.<br /><br />People have been asking for support for the IMAP IDLE com"
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
			+ "d<br />        with allow8bitmime<br />&lt;no id&gt; fix performance bug in base64 encoder; now even faster!";

	protected final JSONObject createSelfAddressed25KBMailObject() throws AjaxException, JSONException, IOException,
			SAXException {
		/*
		 * Create JSON mail object
		 */
		final JSONObject mailObject_25kb = new JSONObject();
		mailObject_25kb.put(MailJSONField.FROM.getKey(), getSendAddress());
		mailObject_25kb.put(MailJSONField.RECIPIENT_TO.getKey(), getSendAddress());
		mailObject_25kb.put(MailJSONField.RECIPIENT_CC.getKey(), "");
		mailObject_25kb.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
		mailObject_25kb.put(MailJSONField.SUBJECT.getKey(), "The mail subject");
		mailObject_25kb.put(MailJSONField.PRIORITY.getKey(), "3");

		final JSONObject bodyObject = new JSONObject();
		bodyObject.put(MailJSONField.CONTENT_TYPE.getKey(), "ALTERNATIVE");
		bodyObject.put(MailJSONField.CONTENT.getKey(), MAIL_TEXT_BODY + "<br />" + MAIL_TEXT_BODY + "<br />"
				+ MAIL_TEXT_BODY + "<br />" + MAIL_TEXT_BODY + "<br />" + MAIL_TEXT_BODY + "<br />" + MAIL_TEXT_BODY
				+ "<br />" + MAIL_TEXT_BODY + "<br />");

		final JSONArray attachments = new JSONArray();
		attachments.put(bodyObject);
		mailObject_25kb.put(MailJSONField.ATTACHMENTS.getKey(), attachments);
		return mailObject_25kb;
	}

	/**
	 * @return <code>true</code> if SP3 is enabled via 'ajax.properties' file;
	 *         otherwise <code>false</code>
	 */
	protected static final boolean isSP3() {
		return Boolean.parseBoolean(AJAXConfig.getProperty(AJAXConfig.Property.IS_SP3));
	}

	protected static final int[] COLUMNS_DEFAULT_LIST = { 600, 601, 612, 602, 603, 607, 610, 608, 611, 614, 102, 604,
			609 };

	protected static final int[] COLUMNS_FOLDER_ID = new int[] { MailListField.FOLDER_ID.getField(),
			MailListField.ID.getField() };

	/**
	 * Performs a hard delete on specified folder
	 * 
	 * @param folder
	 *            The folder
	 */
	protected final void clearFolder(final String folder) throws AjaxException, IOException, SAXException,
			JSONException {
		Executor.execute(getSession(), new DeleteRequest(getFolderAndIDs(folder), true));
	}

	/**
	 * Gets all folder and IDs from specified folder
	 * 
	 * @param folder
	 *            The folder
	 * @return All folder and IDs as a two-dimensional array whereby the second
	 *         dimension's array is always of length <code>2</code>.
	 */
	protected final String[][] getFolderAndIDs(final String folder) throws AjaxException, IOException, SAXException,
			JSONException {
		final CommonAllResponse allR = (CommonAllResponse) Executor.execute(getSession(), new AllRequest(folder,
				COLUMNS_FOLDER_ID, 0, null));
		final Object[][] array = allR.getArray();
		final String[][] folderAndIDs = new String[array.length][];
		for (int i = 0; i < array.length; i++) {
			folderAndIDs[i] = new String[] { array[i][0].toString(), array[i][1].toString() };
		}
		return folderAndIDs;
	}

	protected String getInboxFolder() throws AjaxException, IOException, SAXException, JSONException {
		return getClient().getValues().getInboxFolder();
	}

	protected String getSentFolder() throws AjaxException, IOException, SAXException, JSONException {
		return getClient().getValues().getSentFolder();
	}

	protected String getTrashFolder() throws AjaxException, IOException, SAXException, JSONException {
		return getClient().getValues().getTrashFolder();
	}

	/**
	 * @return User's default send address
	 */
	protected String getSendAddress() throws AjaxException, IOException, SAXException, JSONException {
		return getClient().getValues().getSendAddress();
	}

	/**
	 * @return the private task folder of the user.
	 */
	protected int getPrivateFolder() throws AjaxException, IOException, SAXException, JSONException {
		return getClient().getValues().getPrivateTaskFolder();
	}

	protected TimeZone getTimeZone() throws AjaxException, IOException, SAXException, JSONException {
		return getClient().getValues().getTimeZone();
	}
}
