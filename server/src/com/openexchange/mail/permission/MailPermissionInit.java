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

package com.openexchange.mail.permission;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mail.MailConnection;
import com.openexchange.mail.MailException;
import com.openexchange.server.impl.Initialization;

/**
 * {@link MailPermissionInit}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailPermissionInit implements Initialization {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailPermissionInit.class);

	private static final MailPermissionInit instance = new MailPermissionInit();

	private final AtomicBoolean started = new AtomicBoolean();

	private final AtomicBoolean initialized = new AtomicBoolean();

	private final Lock initLock = new ReentrantLock();

	/**
	 * No instantiation
	 */
	private MailPermissionInit() {
		super();
	}

	/**
	 * @return The singleton instance of {@link MailPermissionInit}
	 */
	public static MailPermissionInit getInstance() {
		return instance;
	}

	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error(this.getClass().getName() + " already started");
			return;
		}
		init();
		started.set(true);
	}

	public void stop() throws AbstractOXException {
		if (!started.get()) {
			LOG.error(this.getClass().getName() + " cannot be stopped since it has not been started before");
			return;
		}
		initialized.set(false);
		started.set(false);
	}

	/**
	 * Initializes the mail permission
	 * 
	 * @throws MailException
	 *             If implementing class cannot be found
	 */
	private void init() throws MailException {
		if (!initialized.get()) {
			initLock.lock();
			try {
				if (!initialized.get()) {
					final String className = MailConnection.getMailPermissionClass();
					try {
						if (className == null) {
							/*
							 * Fallback
							 */
							if (LOG.isWarnEnabled()) {
								LOG.warn(new StringBuilder("Using fallback \"").append(
										DefaultMailPermission.class.getName()).append('"').toString());
							}
							final Class<? extends MailPermission> clazz = DefaultMailPermission.class;
							MailPermission.initialzeMailPermission(clazz);
							initialized.set(true);
							return;
						}
						final Class<? extends MailPermission> clazz = Class.forName(className).asSubclass(
								MailPermission.class);
						MailPermission.initialzeMailPermission(clazz);
						initialized.set(true);
					} catch (final ClassNotFoundException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					}
				}
			} finally {
				initLock.unlock();
			}
		}
	}

}
