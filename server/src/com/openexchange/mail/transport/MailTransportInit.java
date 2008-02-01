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

package com.openexchange.mail.transport;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mail.MailException;
import com.openexchange.server.Initialization;

/**
 * {@link MailTransportInit} - Initializes the mail transport implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailTransportInit implements Initialization {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailTransportInit.class);

	private static final MailTransportInit instance = new MailTransportInit();

	private final AtomicBoolean started = new AtomicBoolean();

	private final AtomicBoolean initialized = new AtomicBoolean();

	private final Lock initLock = new ReentrantLock();

	/**
	 * No instantiation
	 */
	private MailTransportInit() {
		super();
	}

	public static MailTransportInit getInstance() {
		return instance;
	}

	/**
	 * Initializes the mail transport class
	 * 
	 * @throws MailException
	 *             If implementing class cannot be found
	 */
	private void initMailTransportClass() throws MailException {
		if (!initialized.get()) {
			initLock.lock();
			try {
				if (!initialized.get()) {
					final String className = MailTransportProvider.getInstance().getMailTransportClass();
					try {
						if (className == null) {
							/*
							 * Fallback
							 */
							if (LOG.isWarnEnabled()) {
								LOG.warn("Using fallback \"com.openexchange.smtp.SMTPTransport\"");
							}
							final Class<? extends MailTransport> clazz = Class.forName(
									"com.openexchange.smtp.SMTPTransport").asSubclass(MailTransport.class);
							MailTransport.setImplementingClass(clazz);
							initialized.set(true);
							return;
						}
						final Class<? extends MailTransport> clazz = Class.forName(className).asSubclass(
								MailTransport.class);
						MailTransport.setImplementingClass(clazz);
					} catch (final ClassNotFoundException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					}
					initialized.set(true);
				}
			} finally {
				initLock.unlock();
			}
		}
	}

	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error(this.getClass().getName() + " already started");
			return;
		}
		initMailTransportClass();
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
}
