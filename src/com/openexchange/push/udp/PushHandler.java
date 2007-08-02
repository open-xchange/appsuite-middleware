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

import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.event.AppointmentEvent;
import com.openexchange.event.ContactEvent;
import com.openexchange.event.FolderEvent;
import com.openexchange.event.TaskEvent;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;

/**
 * PushHandler
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class PushHandler implements AppointmentEvent, ContactEvent, TaskEvent, FolderEvent {
	
	private GroupStorage groupStorage;
	
	private FolderSQLInterface folderSql;
	
	private static final Log LOG = LogFactory.getLog(PushHandler.class);
	
	public PushHandler() {

	}
	
	public static void event(final int userId, final int objectId, final int folderId, final int[] users, final int module, final SessionObject sessionObj) {
		if (users == null) {
			return ;
		}
		
		try {
			final PushObject pushObject = new PushObject(folderId, module, sessionObj.getContext().getContextId(), users, false);
			PushOutputQueue.add(pushObject);
		} catch (Exception exc) {
			LOG.error("event", exc);
		}
	}
	
	public void appointmentCreated(final AppointmentObject appointmentObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(appointmentObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), appointmentObj.getObjectID(), appointmentObj.getParentFolderID(), users, Types.APPOINTMENT, sessionObj);
	}
	
	public void appointmentModified(final AppointmentObject appointmentObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(appointmentObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), appointmentObj.getObjectID(), appointmentObj.getParentFolderID(), users, Types.APPOINTMENT, sessionObj);
	}
	
	public void appointmentDeleted(final AppointmentObject appointmentObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(appointmentObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), appointmentObj.getObjectID(), appointmentObj.getParentFolderID(), users, Types.APPOINTMENT, sessionObj);
	}
	
	public void contactCreated(final ContactObject contactObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(contactObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), contactObj.getObjectID(), contactObj.getParentFolderID(), users, Types.CONTACT, sessionObj);
	}
	
	public void contactModified(final ContactObject contactObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(contactObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), contactObj.getObjectID(), contactObj.getParentFolderID(), users, Types.CONTACT, sessionObj);
	}
	
	public void contactDeleted(final ContactObject contactObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(contactObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), contactObj.getObjectID(), contactObj.getParentFolderID(), users, Types.CONTACT, sessionObj);
	}
	
	public void taskCreated(final Task taskObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(taskObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), taskObj.getObjectID(), taskObj.getParentFolderID(), users, Types.TASK, sessionObj);
	}
	
	public void taskModified(final Task taskObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(taskObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), taskObj.getObjectID(), taskObj.getParentFolderID(), users, Types.TASK, sessionObj);
	}
	
	public void taskDeleted(final Task taskObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Object(taskObj.getParentFolderID(), sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), taskObj.getObjectID(), taskObj.getParentFolderID(), users, Types.TASK, sessionObj);
	}
	
	public void folderCreated(final FolderObject folderObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Folder(folderObj, sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), folderObj.getObjectID(), folderObj.getParentFolderID(), users, Types.TASK, sessionObj);
	}
	
	public void folderModified(final FolderObject folderObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Folder(folderObj, sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), folderObj.getObjectID(), folderObj.getParentFolderID(), users, Types.TASK, sessionObj);
	}
	
	public void folderDeleted(final FolderObject folderObj, final SessionObject sessionObj) {
		final int[] users = getAffectedUsers4Folder(folderObj, sessionObj, new HashSet<Integer>());
		event(sessionObj.getUserObject().getId(), folderObj.getObjectID(), folderObj.getParentFolderID(), users, Types.TASK, sessionObj);
	}
	
	protected int[] getAffectedUsers4Object(final int folderId, final SessionObject sessionObj, final Set<Integer> hs) {
		if (folderId == 0) {
			return null;
		}
		
		try {
			groupStorage = GroupStorage.getInstance(sessionObj.getContext(), true);
			
			folderSql = new RdbFolderSQLInterface(sessionObj);
			final FolderObject folderObj = folderSql.getFolderById(folderId); 
			
			final OCLPermission[] oclp = folderObj.getPermissionsAsArray();
			
			for (int a = 0; a < oclp.length; a++) {
				if (oclp[a].canReadOwnObjects() || oclp[a].canReadAllObjects()) {
					if (oclp[a].isGroupPermission()) {
						final Group g = groupStorage.getGroup(oclp[a].getEntity());
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
		
		return null;
	}
	
	protected int[] getAffectedUsers4Folder(final FolderObject folderObj, final SessionObject sessionObj, final Set<Integer> hs) {
		try {
			groupStorage = GroupStorage.getInstance(sessionObj.getContext(), true);
			
			final OCLPermission[] oclp = folderObj.getPermissionsAsArray();
			
			for (int a = 0; a < oclp.length; a++) {
				if (oclp[a].isFolderVisible()) {
					if (oclp[a].isGroupPermission()) {
						final Group g = groupStorage.getGroup(oclp[a].getEntity());
						addMembers(g, hs);
					} else {
						hs.add(Integer.valueOf(oclp[a].getEntity()));
					}
				}
			}
			
			return hashSet2Array(hs);
		} catch (LdapException exc) {
			LOG.error("getAffectedUsers4Folder", exc);
		}
		
		return null;
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
