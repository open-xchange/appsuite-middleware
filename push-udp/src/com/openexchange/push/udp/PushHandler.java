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

package com.openexchange.push.udp;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;

/**
 * PushHandler
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class PushHandler implements EventHandler {

	private GroupStorage groupStorage;

	private FolderSQLInterface folderSql;

	private static final Log LOG = LogFactory.getLog(PushHandler.class);

	public PushHandler() {

	}

	public void handleEvent(Event event) {
		if (event.getProperty(CommonEvent.EVENT_KEY) == null) {
			return ;
		}

		final CommonEvent genericEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);

		final int userId = genericEvent.getUserId();
		final int contextId = genericEvent.getContextId();

		final Context ctx;
		try {
			ctx = ContextStorage.getInstance().getContext(contextId);
		} catch (ContextException exc) {
			LOG.error("cannot resolve context id: " + contextId, exc);
			return;
		}

		final int action = genericEvent.getAction();
		final int module = genericEvent.getModule();

		FolderObject parentFolder = (FolderObject) genericEvent.getSourceFolder();

		final Set<Integer> usersSet = new HashSet<Integer>();

		final int[] users;
		
		switch (module) {
		case Types.APPOINTMENT:
			users = getAffectedUsers4Object(parentFolder, usersSet, ctx);
			final AppointmentObject appointmentObj = (AppointmentObject) genericEvent
					.getActionObj();
			event(userId, appointmentObj.getObjectID(), appointmentObj.getParentFolderID(), users,
					module, ctx);
			break;
		case Types.TASK:
			users = getAffectedUsers4Object(parentFolder, usersSet, ctx);
			final Task taskObj = (Task) genericEvent
					.getActionObj();
			event(userId, taskObj.getObjectID(), taskObj.getParentFolderID(), users,
					module, ctx);
			break;
		case Types.CONTACT:
			users = getAffectedUsers4Object(parentFolder, usersSet, ctx);
			final ContactObject contactObj = (ContactObject) genericEvent
					.getActionObj();
			event(userId, contactObj.getObjectID(), contactObj.getParentFolderID(), users,
					module, ctx);
			break;
		case Types.FOLDER:
			users = getAffectedUsers4Folder(parentFolder, usersSet, ctx);
			final FolderObject folderObj = (FolderObject) genericEvent
					.getActionObj();
			event(userId, folderObj.getObjectID(), folderObj.getParentFolderID(), users,
					module, ctx);
			break;
		}
	}

	public static void event(final int userId, final int objectId, final int folderId,
			final int[] users, final int module, final Context ctx) {
		if (users == null) {
			return;
		}

		try {
			final PushObject pushObject = new PushObject(folderId, module, ctx.getContextId(),
					users, false);
			PushOutputQueue.add(pushObject);
		} catch (Exception exc) {
			LOG.error("event", exc);
		}
	}

	protected int[] getAffectedUsers4Object(final FolderObject folderObj, final Set<Integer> hs,
			final Context ctx) {
		try {
			groupStorage = GroupStorage.getInstance(true);

			final OCLPermission[] oclp = folderObj.getPermissionsAsArray();

			for (int a = 0; a < oclp.length; a++) {
				if (oclp[a].canReadOwnObjects() || oclp[a].canReadAllObjects()) {
					if (oclp[a].isGroupPermission()) {
						final Group g = groupStorage.getGroup(oclp[a].getEntity(), ctx);
						addMembers(g, hs);
					} else {
						hs.add(Integer.valueOf(oclp[a].getEntity()));
					}
				}
			}

			return hashSet2Array(hs);
		} catch (Exception exc) {
			LOG.error("getAffectedUser4Object", exc);
		}

		return new int[] { };
	}

	protected int[] getAffectedUsers4Folder(final FolderObject folderObj, final Set<Integer> hs,
			final Context ctx) {
		try {
			groupStorage = GroupStorage.getInstance(true);

			final OCLPermission[] oclp = folderObj.getPermissionsAsArray();

			for (int a = 0; a < oclp.length; a++) {
				if (oclp[a].isFolderVisible()) {
					if (oclp[a].isGroupPermission()) {
						final Group g = groupStorage.getGroup(oclp[a].getEntity(), ctx);
						addMembers(g, hs);
					} else {
						hs.add(Integer.valueOf(oclp[a].getEntity()));
					}
				}
			}

			return hashSet2Array(hs);
		} catch (Exception exc) {
			LOG.error("getAffectedUsers4Folder", exc);
		}

		return new int[] { };
	}

	protected void addMembers(final Group g, final Set<Integer> hs) {
		final int members[] = g.getMember();
		for (int a = 0; a < members.length; a++) {
			hs.add(Integer.valueOf(members[a]));
		}
	}

	protected int[] hashSet2Array(final Set<Integer> hs) {
		int i[] = new int[hs.size()];

		int counter = 0;
		for (Integer integer : hs) {
			i[counter++] = integer.intValue();
		}

		return i;
	}
}
