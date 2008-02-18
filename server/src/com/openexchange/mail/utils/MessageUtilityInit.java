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

package com.openexchange.mail.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.MailException;
import com.openexchange.server.Initialization;

/**
 * {@link MessageUtilityInit} - Initialization implementation for
 * {@link MessageUtility} class
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MessageUtilityInit implements Initialization {

	private static final MessageUtilityInit instance = new MessageUtilityInit();

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageUtilityInit.class);

	public static MessageUtilityInit getInstance() {
		return instance;
	}

	private final AtomicBoolean started = new AtomicBoolean();

	/**
	 * No instantiation
	 */
	private MessageUtilityInit() {
		super();
	}

	private void initMaps() throws MailException {
		final Map<Character, String> htmlCharMap = new HashMap<Character, String>();
		final Map<String, Character> htmlEntityMap = new HashMap<String, Character>();
		final Properties htmlEntities = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(SystemConfig.getProperty(SystemConfig.Property.HTMLEntities));
			htmlEntities.load(in);
		} catch (final IOException e) {
			throw new MailException(MailException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
				in = null;
			}
		}
		/*
		 * Build up map
		 */
		final Iterator<Map.Entry<Object, Object>> iter = htmlEntities.entrySet().iterator();
		final int size = htmlEntities.size();
		for (int i = 0; i < size; i++) {
			final Map.Entry<Object, Object> entry = iter.next();
			final Character c = Character.valueOf((char) Integer.parseInt((String) entry.getValue()));
			htmlEntityMap.put((String) entry.getKey(), c);
			htmlCharMap.put(c, (String) entry.getKey());
		}
		MessageUtility.setMaps(htmlCharMap, htmlEntityMap);
	}

	private void resetMaps() {
		MessageUtility.setMaps(null, null);
	}

	public void start() throws MailException {
		if (started.get()) {
			LOG.error("MessageUtility has already been started", new Throwable());
		}
		initMaps();
		started.set(true);
		if (LOG.isInfoEnabled()) {
			LOG.info("MessageUtility successfully started");
		}
	}

	public void stop() throws MailException {
		if (!started.get()) {
			LOG.error("MessageUtility cannot be stopped since it has not been started before", new Throwable());
		}
		resetMaps();
		started.set(false);
		if (LOG.isInfoEnabled()) {
			LOG.info("MessageUtility successfully stopped");
		}
	}
}
