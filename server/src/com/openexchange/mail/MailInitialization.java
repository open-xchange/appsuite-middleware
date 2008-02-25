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

package com.openexchange.mail;

import com.openexchange.config.services.ConfigurationServiceHolder;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mail.cache.MailCacheConfiguration;
import com.openexchange.mail.config.MailPropertiesInit;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.mail.mime.spam.SpamHandler;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtilityInit;
import com.openexchange.server.Initialization;

/**
 * {@link MailInitialization} - Initializes whole mail implementation and
 * therefore provides a central point for starting/stopping mail implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailInitialization implements Initialization {

	private static final MailInitialization instance = new MailInitialization();

	private ConfigurationServiceHolder configurationServiceHolder;

	/**
	 * No instantiation
	 */
	private MailInitialization() {
		super();
	}

	/**
	 * @return The singleton instance of {@link MailInitialization}
	 */
	public static MailInitialization getInstance() {
		return instance;
	}

	/**
	 * Gets the configuration service holder
	 * 
	 * @return The configuration service holder
	 */
	public ConfigurationServiceHolder getConfigurationServiceHolder() {
		return configurationServiceHolder;
	}

	/**
	 * Sets the configuration service holder
	 * 
	 * @param configurationServiceHolder
	 *            The configuration service holder
	 */
	public void setConfigurationServiceHolder(final ConfigurationServiceHolder configurationServiceHolder) {
		this.configurationServiceHolder = configurationServiceHolder;
	}

	/*
	 * @see com.openexchange.server.Initialization#start()
	 */
	public void start() throws AbstractOXException {
		/*
		 * Start global mail system
		 */
		MailPropertiesInit.getInstance().start();
		MailCacheConfiguration.getInstance().start();
		MailConnectionWatcher.init();
		MessageUtilityInit.getInstance().start();
		/*
		 * TODO: Remove Simulate bundle availability
		 */
		// MailProvider.initMailProvider();
	}

	/*
	 * @see com.openexchange.server.Initialization#stop()
	 */
	public void stop() throws AbstractOXException {
		/*
		 * TODO: Remove Simulate bundle disappearance
		 */
		// MailProvider.resetMailProvider();
		/*
		 * Stop global mail system
		 */
		MIMEType2ExtMap.reset();
		MessageUtilityInit.getInstance().stop();
		UserSettingMailStorage.releaseInstance();
		MailConnectionWatcher.stop();
		SpamHandler.releaseInstance();
		MailCacheConfiguration.getInstance().stop();
		MailPropertiesInit.getInstance().stop();
	}

}
