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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.config.Configuration;
import com.openexchange.config.services.ConfigurationServiceHolder;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;

/**
 * Initializes the event system.
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class PushInit implements Initialization {

	/**
	 * Singleton.
	 */
	private static final PushInit SINGLETON = new PushInit();

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(PushInit.class);

	private PushMulticastRequestTimer requestTimer;

	private PushMulticastSocket multicast;

	private PushOutputQueue output;

	private PushSocket input;

	private PushConfigInterface config;

	private final AtomicBoolean started = new AtomicBoolean();

	private ConfigurationServiceHolder csh;

	/**
	 * Prevent instantiation.
	 */
	private PushInit() {
		super();
	}

	/**
	 * @return the singleton instance.
	 */
	public static PushInit getInstance() {
		return SINGLETON;
	}

	/**
	 * Sets the configuration service holder
	 * 
	 * @param csh
	 *            The configuration service holder
	 */
	public void setConfigurationServiceHolder(final ConfigurationServiceHolder csh) {
		this.csh = csh;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() throws AbstractOXException {
		if (null != config) {
			LOG.error("Duplicate push initialization.");
			return;
		}

		final Configuration conf = csh.getService();
		try {
			if (conf != null) {
				config = new PushConfigInterfaceImpl(conf);
			}
		} finally {
			csh.ungetService(conf);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Starting Push UDP");
		}

		if (config != null) {
			input = new PushSocket(config);
			output = new PushOutputQueue(config);

			multicast = new PushMulticastSocket(config);
			requestTimer = new PushMulticastRequestTimer(config);

			started.set(true);
		} else {
			throw new PushUDPException(PushUDPException.Code.PUSH_UDP_EXCEPTION);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		if (null == requestTimer) {
			LOG.error("Duplicate push component shutdown.");
			return;
		}
		requestTimer.cancel();
		requestTimer = null;
		multicast.close();
		multicast = null;
		output.close();
		output = null;
		input.close();
		input = null;

		started.set(true);
	}

	public boolean isStarted() {
		return started.get();
	}
}
