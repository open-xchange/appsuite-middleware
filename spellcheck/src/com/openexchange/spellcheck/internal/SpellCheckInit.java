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

package com.openexchange.spellcheck.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.server.Initialization;
import com.openexchange.spellcheck.SpellCheckException;
import com.openexchange.spellcheck.serviceholder.SpellCheckHttpService;
import com.openexchange.spellcheck.servlet.SpellCheckServlet;

/**
 * {@link SpellCheckInit}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class SpellCheckInit implements Initialization {

	private static final String SC_SRVLT_ALIAS = "ajax/spellcheck";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(SpellCheckInit.class);

	private static final SpellCheckInit instance = new SpellCheckInit();

	/**
	 * Gets the singleton instance of {@link SpellCheckInit}
	 * 
	 * @return The singleton instance of {@link SpellCheckInit}
	 */
	public static SpellCheckInit getInstance() {
		return instance;
	}

	private final AtomicBoolean started;

	private ConfigurationServiceHolder csh;

	/**
	 * Initializes a new {@link SpellCheckInit}
	 */
	private SpellCheckInit() {
		super();
		started = new AtomicBoolean();
	}

	/**
	 * Gets the configuration service holder
	 * 
	 * @return The configuration service holder
	 */
	public ConfigurationServiceHolder getConfigurationServiceHolder() {
		return csh;
	}

	/**
	 * Sets the configuration service holder
	 * 
	 * @param csh
	 *            The configuration service holder to set
	 */
	public void setConfigurationServiceHolder(final ConfigurationServiceHolder csh) {
		this.csh = csh;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.Initialization#start()
	 */
	public void start() throws SpellCheckException {
		if (!started.compareAndSet(false, true)) {
			LOG.error("Spell check already started.");
			return;
		}
		DictonaryStorage.loadDictionaries();
		RdbUserSpellDictionary.start();
		final HttpService httpService = SpellCheckHttpService.getInstance().getService();
		if (httpService == null) {
			LOG.error("HTTP service is null. Spell check servlet cannot be registered");
			return;
		}
		try {
			/*
			 * Register spell check servlet
			 */
			httpService.registerServlet(SC_SRVLT_ALIAS, new SpellCheckServlet(), null, null);
		} catch (final ServletException e) {
			throw new SpellCheckException(SpellCheckException.Code.SERVLET_REGISTRATION_FAILED, e, e
					.getLocalizedMessage());
		} catch (final NamespaceException e) {
			throw new SpellCheckException(SpellCheckException.Code.SERVLET_REGISTRATION_FAILED, e, e
					.getLocalizedMessage());
		} finally {
			SpellCheckHttpService.getInstance().ungetService(httpService);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.Initialization#stop()
	 */
	public void stop() {
		if (!started.compareAndSet(true, false)) {
			LOG.error("Spell check has not been started.");
			return;
		}
		final HttpService httpService = SpellCheckHttpService.getInstance().getService();
		if (httpService == null) {
			LOG.error("HTTP service is null. Spell check servlet cannot be unregistered");
		} else {
			try {
				/*
				 * Unregister spell check servlet
				 */
				httpService.unregister(SC_SRVLT_ALIAS);
			} finally {
				SpellCheckHttpService.getInstance().ungetService(httpService);
			}
		}
		RdbUserSpellDictionary.stop();
		DictonaryStorage.clearDictionaries();
	}

}
