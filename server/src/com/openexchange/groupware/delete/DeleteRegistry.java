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

import com.openexchange.ajax.spellcheck.AJAXUserDictionary;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.attach.impl.AttachmentContextDelete;
import com.openexchange.groupware.attach.impl.AttachmentDelDelete;
import com.openexchange.groupware.calendar.CalendarAdministration;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.filestore.FileStorageRemover;
import com.openexchange.groupware.infostore.InfostoreDelete;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.tasks.TasksDelete;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.DBPoolingException;
import com.openexchange.tools.file.QuotaUsageDelete;
import com.openexchange.tools.oxfolder.OXFolderDeleteListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DeleteRegistry
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class DeleteRegistry {

	private static DeleteRegistry instance;

	private final Set<Class> classes;

	private final List<DeleteListener> listeners;

	private final Lock registryLock = new ReentrantLock();

	private static final Lock INIT_LOCK = new ReentrantLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private DeleteRegistry() {
		super();
		listeners = new ArrayList<DeleteListener>(10);
		classes = new HashSet<Class>();
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
        registerDeleteListener(new AttachmentContextDelete());
        registerDeleteListener(new AttachmentDelDelete());
		/*
		 * At last insert folder delete listener
		 */
		registerDeleteListener(new OXFolderDeleteListener());
		// Remove FileStorage if context is deleted.
		registerDeleteListener(new FileStorageRemover());
	}

	/**
	 * Factory method
	 * 
	 * @return the singleton instance of <code>{@link DeleteRegistry}</code>
	 */
	public static DeleteRegistry getInstance() {
		if (!initialized.get()) {
			INIT_LOCK.lock();
			try {
				if (instance == null) {
					instance = new DeleteRegistry();
					try {
						instance.init();
						initialized.set(true);
					} catch (Exception e) {
						instance = null;
					}
				}
			} finally {
				INIT_LOCK.unlock();
			}
		}
		return instance;
	}

	/**
	 * Registers an instance of <code>{@link DeleteListener}</code>. <b>Note</b>:
	 * Only one instance of a certain <code>{@link DeleteListener}</code>
	 * implementation is added, meaning if you try to register a certain
	 * implementation twice, the latter one is going to be discarded
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void registerDeleteListener(final DeleteListener listener) {
		registryLock.lock();
		try {
			if (classes.contains(listener.getClass())) {
				return;
			}
			listeners.add(listener);
			classes.add(listener.getClass());
		} finally {
			registryLock.unlock();
		}
	}

	/**
	 * Removes given instance of <code>{@link DeleteListener}</code> from this
	 * registry's known listeners.
	 * 
	 * @param listener -
	 *            the listener to remove
	 */
	public void unregisterDeleteListener(final DeleteListener listener) {
		registryLock.lock();
		try {
			listeners.remove(listener);
			classes.remove(listener.getClass());
		} finally {
			registryLock.unlock();
		}
	}

	/**
	 * Fires the delete event
	 * 
	 * @param deleteEvent
	 *            the delete event
	 * @param readCon
	 *            a readable connection
	 * @param writeCon
	 *            a writeable connection
	 * @throws DeleteFailedException
	 *             if delete event could not be performed
	 * @throws LdapException
	 *             if any user/group data could not be determined
	 * @throws SQLException
	 *             if an SQL error occured
	 * @throws DBPoolingException
	 *             if no connection could be fetched from pool
	 */
	public void fireDeleteEvent(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon)
			throws DeleteFailedException {
		registryLock.lock();
		try {
			final int size = listeners.size();
			final Iterator<DeleteListener> iter = listeners.iterator();
			for (int i = 0; i < size; i++) {
				final DeleteListener listener = iter.next();
				listener.deletePerformed(deleteEvent, readCon, writeCon);
			}
		} finally {
			registryLock.unlock();
		}
	}

}
