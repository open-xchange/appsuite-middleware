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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.ListRequest;
import com.openexchange.mail.MailListField;

/**
 * {@link ListTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ListTest extends AbstractMailTest {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ListTest.class);

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            Name of this test.
	 */
	public ListTest(final String name) {
		super(name);
	}

	private static final int[] listAttributes = new int[] { MailListField.ID.getField(),
			MailListField.FOLDER_ID.getField(), MailListField.THREAD_LEVEL.getField(),
			MailListField.ATTACHMENT.getField(), MailListField.FROM.getField(), MailListField.SUBJECT.getField(),
			MailListField.RECEIVED_DATE.getField(), MailListField.SIZE.getField(), MailListField.FLAGS.getField(),
			MailListField.PRIORITY.getField(), MailListField.COLOR_LABEL.getField() };

	/**
	 * Tests the <code>action=list</code> request on INBOX folder
	 * 
	 * @throws Throwable
	 */
	public void testList() throws Throwable {
		/*
		 * TODO: Insert mails
		 */
		int[] columns = new int[] { MailListField.FOLDER_ID.getField(), MailListField.ID.getField() };
		final CommonAllResponse allR = (CommonAllResponse) Executor.execute(getSession(), new AllRequest(
				getInboxFolder(), columns, 0, null));
		final Object[][] allArray = allR.getArray();
		final String[][] folderAndMailIds = new String[allArray.length][];
		for (int i = 0; i < folderAndMailIds.length; i++) {
			folderAndMailIds[i] = new String[allArray[i].length];
			for (int j = 0; j < folderAndMailIds[i].length; j++) {
				folderAndMailIds[i][j] = allArray[i][j].toString();
			}
		}
		columns = listAttributes;
		final CommonListResponse listR = (CommonListResponse) Executor.execute(getSession(), new ListRequest(
				folderAndMailIds, columns));
		final Object[][] listArray = listR.getArray();
		final StringBuilder strBuilder = new StringBuilder(256);
		final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH);
		final Date date = new Date();
		for (int i = 0; i < listArray.length; i++) {
			strBuilder.setLength(0);
			strBuilder.append('\n');
			for (int j = 0; j < listArray[i].length; j++) {
				strBuilder.append(String.valueOf(j + 1)).append(".\t");
				if (j == 6) {
					date.setTime(Long.parseLong(listArray[i][j].toString()));
					strBuilder.append(dateFormat.format(date));
				} else {
					strBuilder.append(listArray[i][j].toString());
				}
				strBuilder.append('\n');
			}
			LOG.trace(strBuilder.toString());
		}
		/*
		 * TODO: Delete previously inserted mails
		 */
	}
}
