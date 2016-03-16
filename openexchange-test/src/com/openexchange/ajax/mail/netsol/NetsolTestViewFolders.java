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

package com.openexchange.ajax.mail.netsol;

import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.netsol.actions.NetsolFolderRequest;
import com.openexchange.ajax.mail.netsol.actions.NetsolFolderResponse;

/**
 * {@link NetsolTestViewFolders}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class NetsolTestViewFolders extends AbstractNetsolTest {

	/**
	 * Initializes a new {@link NetsolTestViewFolders}
	 *
	 * @param name
	 */
	public NetsolTestViewFolders(final String name) {
		super(name);
	}

	public void testViewFolders() throws Throwable {
		netsolClearFolder(getInboxFolder());
		netsolClearFolder(getSentFolder());
		netsolClearFolder(getTrashFolder());

		final int runs = NetsolTestConstants.RUNS;
		final DurationTracker requestTracker = new DurationTracker(runs * 3);
		final DurationTracker parseTracker = new DurationTracker(runs * 3);
		for (int i = 0; i < runs; i++) {
			/*
			 * Get private folders
			 */
			final NetsolFolderResponse response = (NetsolFolderResponse) Executor.execute(getSession(), new NetsolFolderRequest("1"));
			assertTrue("Folder List failed", response.getArray() != null && response.getArray().length > 0);
			assertTrue("Duration corrupt", response.getRequestDuration() > 0);
			requestTracker.addDuration(response.getRequestDuration());
			parseTracker.addDuration(response.getParseDuration());
		}

		for (int i = 0; i < runs; i++) {
			/*
			 * Get first level folders
			 */
			final NetsolFolderResponse response = (NetsolFolderResponse) Executor.execute(getSession(), new NetsolFolderRequest("default"));
			assertTrue("Folder List failed", response.getArray() != null && response.getArray().length > 0);
			assertTrue("Duration corrupt", response.getRequestDuration() > 0);
			requestTracker.addDuration(response.getRequestDuration());
			parseTracker.addDuration(response.getParseDuration());
		}

		for (int i = 0; i < runs; i++) {
			/*
			 * Get INBOX subfolders
			 */
			final NetsolFolderResponse response = (NetsolFolderResponse) Executor.execute(getSession(), new NetsolFolderRequest("default.INBOX"));
			assertTrue("Folder List failed", response.getArray() != null && response.getArray().length > 0);
			assertTrue("Duration corrupt", response.getRequestDuration() > 0);
			requestTracker.addDuration(response.getRequestDuration());
			parseTracker.addDuration(response.getParseDuration());
		}


		/*
		 * Clean everything
		 */
		netsolClearFolder(getInboxFolder());
		netsolClearFolder(getSentFolder());
		netsolClearFolder(getTrashFolder());
	}

}
