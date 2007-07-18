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



package com.openexchange.groupware.delete;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.openexchange.ajax.spellcheck.AJAXUserDictionary;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.attach.impl.AttachmentDelDelete;
import com.openexchange.groupware.calendar.CalendarAdministration;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.infostore.InfostoreDelete;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.tasks.TasksDelete;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.server.DBPoolingException;
import com.openexchange.tools.file.QuotaUsageDelete;
import com.openexchange.tools.oxfolder.OXFolderDeleteListener;

/**
 * DeleteRegistry
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class DeleteRegistry {
	
	private static DeleteRegistry instance;
	
	private final Set<Class> classes;
	
	private final List<DeleteListener> listeners;
	
	//private final Map<Class,DeleteListener> listeners;

	private DeleteRegistry() {
		super();
		listeners = new ArrayList<DeleteListener>(10);
		classes =  new HashSet<Class>();
	}
	
	private void init() throws Exception {
        SystemConfig.init();
		/*
		 * Insert module delete listener
		 */
		registerDeleteListener(new TasksDelete());
		registerDeleteListener(new InfostoreDelete());
		registerDeleteListener(new Contacts());
		registerDeleteListener(new CalendarAdministration());
		/*
		 * Delete user configuration & settings
		 */
		registerDeleteListener(new AJAXUserDictionary());
		registerDeleteListener(new UserConfiguration());
		registerDeleteListener(new UserSettingMail());
		registerDeleteListener(new QuotaUsageDelete());
		registerDeleteListener(new AttachmentDelDelete());
		
		/*
		 * At last insert folder delete listener
		 */
		registerDeleteListener(new OXFolderDeleteListener());
		//registerDeleteListener(new OXFolderAction());
	}
	
	public static DeleteRegistry getInstance() {
		synchronized (DeleteRegistry.class) {
			if (instance == null) {
				instance = new DeleteRegistry();
				try {
					instance.init();
				} catch (Exception e) {
					instance = null;
				}
			}
		}
		return instance;
	}
	
	public void registerDeleteListener(final DeleteListener listener) {
		synchronized (this) {
			if (classes.contains(listener.getClass())) {
				return;
			}
			listeners.add(listener);
			classes.add(listener.getClass());
		}
	}
	
	public void unregisterDeleteListener(final DeleteListener listener) {
		synchronized (this) {
			listeners.remove(listener);
			classes.remove(listener.getClass());
		}
	}
	
	public void fireDeleteEvent(final DeleteEvent sqlDelEvent, final Connection readCon, final Connection writeCon) throws DeleteFailedException, LdapException, SQLException, DBPoolingException {
		synchronized (this) {
			final int size = listeners.size();
			final Iterator<DeleteListener> iter = listeners.iterator();
			for (int i = 0; i < size; i++) {
				final DeleteListener listener = iter.next();
				listener.deletePerformed(sqlDelEvent, readCon, writeCon);
			}
		}
	}

}
