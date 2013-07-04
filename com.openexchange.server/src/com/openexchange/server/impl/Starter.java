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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.text.NumberFormat;
import java.util.Properties;
import java.util.Stack;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.server.Initialization;
import com.openexchange.version.Version;

/**
 * {@link Starter} - Starter for <a href="www.open-xchange.com">Open-Xchange</a> server.
 * <p>
 * All necessary initializations for a proper system start-up take place.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Starter implements Initialization {

    /**
     * This contains the components to be started if a normal groupware startup is done.
     */
    private final Initialization[] inits = new Initialization[] {
    /**
     * Reads system.properties.
     */
    com.openexchange.configuration.SystemConfig.getInstance(),
    /**
     * Cache availability registry start-up
     */
    com.openexchange.cache.registry.CacheAvailabilityRegistryInit.getInstance(),
    /**
     * Reads the calendar.properties.
     */
    com.openexchange.groupware.calendar.CalendarConfig.getInstance(),
    /**
     * Initialization for custom charset provider
     */
    new com.openexchange.charset.CustomCharsetProviderInit(),
    /**
     * Setup of ContextStorage and LoginInfo.
     */
    com.openexchange.groupware.contexts.impl.ContextInit.getInstance(),
    /**
     * The Servlet initialization
     */
    new com.openexchange.tools.servlet.ServletInitialization(),
    /**
     * Folder initialization
     */
    com.openexchange.tools.oxfolder.OXFolderProperties.getInstance(),
    new com.openexchange.folder.internal.FolderInitialization(),
    /**
     * Mail initialization
     */
    com.openexchange.mail.MailInitialization.getInstance(),
    /**
     * Webdav Whitelist
     */
    new com.openexchange.webdav.tools.WebdavWhitelistInit(),
    /**
     * Transport initialization
     */
    com.openexchange.mail.transport.TransportInitialization.getInstance(),
    /**
     * Infostore Configuration
     */
    com.openexchange.groupware.infostore.InfostoreConfig.getInstance(),
    /**
     * Attachment Configuration
     */
    com.openexchange.groupware.attach.AttachmentConfig.getInstance(),
    /**
     * User storage init
     */
    com.openexchange.groupware.ldap.UserStorageInit.getInstance(),
    /**
     * Group storage init
     */
    com.openexchange.group.internal.GroupInit.getInstance(),
    /**
     * Resource storage init
     */
    com.openexchange.resource.internal.ResourceStorageInit.getInstance(),
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
    com.openexchange.groupware.settings.impl.ConfigTreeInit.getInstance(),
    /**
     * Responsible for starting and stopping the EventQueue
     */
    new com.openexchange.event.impl.EventInit(),
    /**
     * Responsible for registering all instances for deleting users and groups.
     */
    new com.openexchange.groupware.delete.DeleteRegistryInitialization(),
    /**
     * Downgrade registry start-up
     */
    com.openexchange.groupware.downgrade.DowngradeRegistryInit.getInstance(),
    /**
     * Initializes the Attachment Calendar Listener
     */
    new com.openexchange.groupware.attach.AttachmentInit(),
    /**
     * Initializes the Link Attachment Listener
     */
    new com.openexchange.groupware.links.LinkInit(),
    /**
     * Managed file initialization
     */
    new com.openexchange.filemanagement.internal.ManagedFileInitialization(),
    new com.openexchange.mailaccount.internal.MailAccountStorageInit(),
    new com.openexchange.multiple.internal.MultipleHandlerInit(),
    new com.openexchange.groupware.impl.id.IDGeneratorInit() };

    /**
     * This contains the components that must be started if the admin uses APIs of the server.
     */
    private final Initialization[] adminInits = new Initialization[] {
    /**
     * Reads system.properties.
     */
    com.openexchange.configuration.SystemConfig.getInstance(),
    /**
     * Cache availability registry start-up
     */
    com.openexchange.cache.registry.CacheAvailabilityRegistryInit.getInstance(),
    /**
     * Initialization for alias charset provider
     */
    new com.openexchange.charset.CustomCharsetProviderInit(),
    /**
     * Setup of ContextStorage and LoginInfo.
     */
    com.openexchange.groupware.contexts.impl.ContextInit.getInstance(),
    /**
     * The Servlet initialization
     */
    new com.openexchange.tools.servlet.ServletInitialization(),
    /**
     * Folder initialization
     */
    com.openexchange.tools.oxfolder.OXFolderProperties.getInstance(),
    new com.openexchange.folder.internal.FolderInitialization(),
    /**
     * User storage init
     */
    com.openexchange.groupware.ldap.UserStorageInit.getInstance(),
    /**
     * Group storage init
     */
    com.openexchange.group.internal.GroupInit.getInstance(),
    /**
     * Resource storage init
     */
    com.openexchange.resource.internal.ResourceStorageInit.getInstance(),
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
    com.openexchange.groupware.settings.impl.ConfigTreeInit.getInstance(),
    /**
     * Responsible for starting and stopping the EventQueue
     */
    new com.openexchange.event.impl.EventInit(),
    /**
     * Responsible for registering all instances for deleting users and groups.
     */
    new com.openexchange.groupware.delete.DeleteRegistryInitialization(),
    /**
     * Downgrade registry start-up
     */
    com.openexchange.groupware.downgrade.DowngradeRegistryInit.getInstance(),
    new com.openexchange.mailaccount.internal.MailAccountStorageInit(),
    new com.openexchange.groupware.impl.id.IDGeneratorInit() };

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Starter.class));

    private final Stack<Initialization> started;

    /**
     * Default constructor.
     */
    public Starter() {
        super();
        started = new Stack<Initialization>();
    }

    @Override
    public void start() {

        dumpServerInfos();

        for (final Initialization init : inits) {
            try {
                init.start();
                started.push(init);
            } catch (Throwable t) {
                LOG.error("initialization of " + init.getClass().getName() + " failed", t);
            }
        }

        if (LOG.isInfoEnabled()) {
            if (started.size() == inits.length) {
                LOG.info("Groupware server successfully initialized.");
            } else {
                LOG.info("Groupware server initialized with errors.");
            }
        }

        /*
         * TODO: Check property ENABLE_INTERNAL_USER_EDIT OXFolderSQL.updateCtxAddrBookPermission
         * (FolderCacheProperties.isEnableInternalUsersEdit())
         */

        if (LOG.isInfoEnabled()) {
            if (started.size() == inits.length) {
                LOG.info("SYSTEM IS UP & RUNNING...");
            } else {
                LOG.info("SYSTEM IS UP & RUNNING WITH ERRORS...");
            }
        }

    }

    /**
     * Start in admin mode
     */
    public void adminStart() {
        dumpServerInfos();
        for (final Initialization init : adminInits) {
            try {
                init.start();
                started.push(init);
            } catch (final OXException e) {
                LOG.error("Initialization of " + init.getClass().getName() + " failed", e);
            }
        }
        if (LOG.isInfoEnabled()) {
            if (started.size() == adminInits.length) {
                LOG.info("Admin successfully initialized.");
            } else {
                LOG.info("Admin initialized with errors.");
            }
        }
        if (LOG.isInfoEnabled()) {
            if (started.size() == adminInits.length) {
                LOG.info("SYSTEM IS UP & RUNNING IN ADMIN MODE...");
            } else {
                LOG.info("SYSTEM IS UP & RUNNING WITH ERRORS IN ADMIN MODE...");
            }
        }
    }

    /**
     * Dump server information.
     */
    private static final void dumpServerInfos() {
        try {
            final Properties p = System.getProperties();
            if (LOG.isInfoEnabled()) {
                LOG.info(p.getProperty("os.name") + ' ' + p.getProperty("os.arch") + ' ' + p.getProperty("os.version"));
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(p.getProperty("java.runtime.version"));
            }
            final long totalMemory = Runtime.getRuntime().totalMemory() >> 10;
            if (LOG.isInfoEnabled()) {
                LOG.info("VM Total Memory       : " + NumberFormat.getNumberInstance().format(totalMemory) + " KB");
            }
            final long freeMemory = Runtime.getRuntime().freeMemory() >> 10;
            if (LOG.isInfoEnabled()) {
                LOG.info("VM Free Memory        : " + NumberFormat.getNumberInstance().format(freeMemory) + " KB");
            }
            final long usedMemory = totalMemory - freeMemory;
            if (LOG.isInfoEnabled()) {
                LOG.info("VM Used Memory        : " + NumberFormat.getNumberInstance().format(usedMemory) + " KB");
            }
        } catch (final Exception gee) {
            LOG.error(gee.getMessage(), gee);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("System version : " + Version.NAME + " Server [" + Version.getInstance().getVersionString() + "] initializing ...");
            LOG.info("Server Footprint : " + OXException.getServerId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        while (!started.isEmpty()) {
            try {
                started.pop().stop();
            } catch (final OXException e) {
                LOG.error("Component shutdown failed.", e);
            }
        }
    }
}
