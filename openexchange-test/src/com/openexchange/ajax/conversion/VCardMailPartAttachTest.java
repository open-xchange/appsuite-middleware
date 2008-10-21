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

package com.openexchange.ajax.conversion;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.AllResponse;
import com.openexchange.ajax.conversion.actions.ConvertRequest;
import com.openexchange.ajax.conversion.actions.ConvertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.DataObject;

/**
 * {@link VCardMailPartAttachTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class VCardMailPartAttachTest extends AbstractConversionTest {

	/**
	 * Initializes a new {@link VCardMailPartAttachTest}
	 * 
	 * @param name
	 *            The name
	 */
	public VCardMailPartAttachTest(final String name) {
		super(name);
	}

	/**
	 * Tests the <code>action=convert</code> request
	 * 
	 * @throws Throwable
	 */
	public void testVCardAttach() throws Throwable {
		try {
			/*
			 * Find a valid contact
			 */
			final int objectId;
			final int folderId = getPrivateContactFolder();
			final Date listStart = new Date(TimeTools.getHour(-1));
			final Date listEnd = new Date(TimeTools.getHour(2));
			final AllResponse allR = (AllResponse) Executor.execute(getSession(), new AllRequest(folderId,
					new int[] { DataObject.OBJECT_ID }, listStart, listEnd));
			final ListIDs listIDs = allR.getListIDs();
			if (listIDs.size() == 0) {
				/*
				 * TODO: Create a contact and remember its object-id
				 */
				objectId = -1;

			} else {
				objectId = Integer.parseInt(listIDs.get(0).getObject());
			}

			try {
				/*
				 * Trigger conversion
				 */
				final JSONObject jsonBody = new JSONObject();
				final JSONObject jsonSource = new JSONObject().put("identifier", "com.openexchange.contact");
				jsonSource.put("args", new JSONArray().put(
						new JSONObject().put("com.openexchange.groupware.contact.folder", folderId)).put(
						new JSONObject().put("com.openexchange.groupware.contact.id", objectId)));
				jsonBody.put("datasource", jsonSource);
				final JSONObject jsonHandler = new JSONObject().put("identifier", "com.openexchange.mail.vcard");
				jsonHandler.put("args", new JSONArray());
				jsonBody.put("datahandler", jsonHandler);
				final ConvertResponse convertResponse = (ConvertResponse) Executor.execute(getSession(),
						new ConvertRequest(jsonBody, true));

				final JSONObject mailObject = (JSONObject) convertResponse.getData();

				assertFalse("Missing response on action=convert", mailObject == null);

				System.out.println("NEW MAIL:\n" + mailObject.toString());

				// TODO: Send mail
			} finally {
				// Nothing to do
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
}
