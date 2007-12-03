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

package com.openexchange.server.impl;

import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;

/**
 * {@link Starter}
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Starter implements Initialization {

	/**
	 * This contains the components to be started if a normal groupware startup
	 * is done.
	 */
	private final Initialization[] inits = new Initialization[] {
	/**
	 * Reads system.properties.
	 */
	com.openexchange.configuration.SystemConfig.getInstance(),
	/**
	 * Reads configdb.properties.
	 */
	com.openexchange.configuration.ConfigDB.getInstance(),
	/**
	 * Read in update tasks
	 */
	com.openexchange.groupware.update.UpdateTaskCollectionInit.getInstance(),
	/**
	 * Reads the calendar.properties.
	 */
	com.openexchange.groupware.calendar.CalendarConfig.getInstance(),
	/**
	 * Reads the participant.properties.
	 */
	com.openexchange.groupware.configuration.ParticipantConfig.getInstance(),
	/**
	 * Sets the caching system JCS up.
	 */
	com.openexchange.cache.impl.Configuration.getInstance(),
	/**
	 * Connection pools for ConfigDB and database assignments for contexts.
	 */
	com.openexchange.database.DatabaseInit.getInstance(),
	/**
	 * Starts AJP server
	 */
	com.openexchange.groupware.impl.BackendServicesInit.getInstance(),
	/**
	 * Starts HTTP serlvet manager
	 */
	com.openexchange.tools.servlet.http.HttpServletManagerInit.getInstance(),
	/**
	 * Setup of ContextStorage and LoginInfo.
	 */
	com.openexchange.groupware.contexts.impl.ContextInit.getInstance(),
	/**
	 * Folder initialization
	 */
	com.openexchange.tools.oxfolder.OXFolderProperties.getInstance(),
	/**
	 * Mail initialization
	 */
	com.openexchange.mail.MailInitialization.getInstance(),
	/**
	 * Infostore Configuration
	 */
	com.openexchange.groupware.infostore.InfostoreConfig.getInstance(),
	/**
	 * Contact Configuration
	 */
	com.openexchange.groupware.contact.ContactConfig.getInstance(),
	/**
	 * Attachment Configuration
	 */
	com.openexchange.groupware.attach.AttachmentConfig.getInstance(),
	/**
	 * User configuration init
	 */
	com.openexchange.groupware.userconfiguration.UserConfigurationStorageInit.getInstance(),
	/**
	 * Notification Configuration
	 */
	com.openexchange.groupware.notify.NotificationConfig.getInstance(),
	/**
	 * Sets up the configuration tree.
	 */
	com.openexchange.groupware.settings.ConfigTreeInit.getInstance(),
	/**
	 * Responsible for starting and stopping the EventQueue
	 */
	new com.openexchange.event.EventInit(),
	/**
	 * Initializes the push component.
	 */
	com.openexchange.push.PushInit.getInstance() };

	/**
	 * This contains the components that must be started if the admin uses APIs
	 * of the server.
	 */
	private final Initialization[] adminInits = new Initialization[] {
	/**
	 * Reads system.properties.
	 */
	com.openexchange.configuration.SystemConfig.getInstance(),
	/**
	 * Reads configdb.properties.
	 */
	com.openexchange.configuration.ConfigDB.getInstance(),
        /**
         * Read in update tasks
         */
        com.openexchange.groupware.update.UpdateTaskCollectionInit.getInstance(),
	/**
	 * Sets the caching system JCS up.
	 */
	com.openexchange.cache.impl.Configuration.getInstance(),
	/**
	 * Connection pools for ConfigDB and database assignments for contexts.
	 */
	com.openexchange.database.DatabaseInit.getInstance(),
	/**
	 * Setup of ContextStorage and LoginInfo.
	 */
	com.openexchange.groupware.contexts.impl.ContextInit.getInstance(),
	/**
	 * Notification Configuration
	 */
	com.openexchange.groupware.notify.NotificationConfig.getInstance() };

	private static final Log LOG = LogFactory.getLog(Starter.class);

	private final Stack<Initialization> started = new Stack<Initialization>();

	/**
	 * Default constructor.
	 */
	public Starter() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() throws AbstractOXException {

		dumbServerInfos();

		for (Initialization init : inits) {
			init.start();
			started.push(init);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Groupware server successfully initialized.");
		}

		/*
		 * TODO: Check property ENABLE_INTERNAL_USER_EDIT
		 * OXFolderSQL.updateCtxAddrBookPermission(FolderCacheProperties.isEnableInternalUsersEdit())
		 */

		if (LOG.isInfoEnabled()) {
			LOG.info("SYSTEM IS UP & RUNNING...");
		}

	}

	/**
	 * Start in admin mode
	 */
	public void adminStart() throws AbstractOXException {

		dumbServerInfos();

		for (Initialization init : adminInits) {
			init.start();
			started.push(init);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("SYSTEM IS UP & RUNNING IN ADMIN MODE...");
		}

	}

	/**
	 * Dumb server informations
	 */
	private static final void dumbServerInfos() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Open-Xchange 6.0");
			LOG.info("(c) Open-Xchange Inc. , Open-Xchange GmbH");
		}

		try {
			final Properties p = System.getProperties();
			if (LOG.isInfoEnabled()) {
				LOG.info(p.getProperty("os.name") + ' ' + p.getProperty("os.arch") + ' ' + p.getProperty("os.version"));
			}
			if (LOG.isInfoEnabled()) {
				LOG.info(p.getProperty("java.runtime.version"));
			}
			final long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
			if (LOG.isInfoEnabled()) {
				LOG.info("VM Total Memory       : " + DecimalFormat.getNumberInstance().format(totalMemory) + " KB");
			}
			final long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
			if (LOG.isInfoEnabled()) {
				LOG.info("VM Free Memory        : " + DecimalFormat.getNumberInstance().format(freeMemory) + " KB");
			}
			final long usedMemory = totalMemory - freeMemory;
			if (LOG.isInfoEnabled()) {
				LOG.info("VM Used Memory        : " + DecimalFormat.getNumberInstance().format(usedMemory) + " KB");
			}
		} catch (final Exception gee) {
			LOG.error(gee.getMessage(), gee);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("System version : Open-Xchange Server [" + Version.BUILDNUMBER + "] initializing ...");
			LOG.info("Server Footprint : " + AbstractOXException.SERVER_ID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		while (!started.isEmpty()) {
			try {
				started.pop().stop();
			} catch (AbstractOXException e) {
				LOG.error("Component shutdown failed.", e);
			}
		}
	}
}
