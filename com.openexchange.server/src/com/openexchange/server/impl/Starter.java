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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.Initialization;
import com.openexchange.tools.exceptions.ExceptionUtils;
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
     * Initialization for custom charset provider
     */
    new com.openexchange.charset.CustomCharsetProviderInit(),
    /**
     * Setup of ContextStorage and LoginInfo.
     */
    com.openexchange.groupware.contexts.impl.ContextInit.getInstance(),
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
    new com.openexchange.mailaccount.internal.MailAccountStorageInit(),
    new com.openexchange.multiple.internal.MultipleHandlerInit(),
    new com.openexchange.groupware.impl.id.IDGeneratorInit() };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Starter.class);

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
                ExceptionUtils.handleThrowable(t);
                LOG.error("initialization of {} failed", init.getClass().getName(), t);
            }
        }

        if (started.size() == inits.length) {
            LOG.info("Groupware server successfully initialized.");
        } else {
            LOG.info("Groupware server initialized with errors.");
        }
    }

    /**
     * Dump server information.
     */
    private static final void dumpServerInfos() {
        StringBuilder message = new StringBuilder(64);
        List<Object> args = new ArrayList<>();
        String sep = Strings.getLineSeparator();

        try {
            Properties p = System.getProperties();

            message.append("Server info{}");
            args.add(sep);

            message.append("Operating system : {} {} {}{}");
            args.add(p.getProperty("os.name"));
            args.add(p.getProperty("os.arch"));
            args.add(p.getProperty("os.version"));
            args.add(sep);


            message.append("Java             : {}{}");
            args.add(p.getProperty("java.runtime.version"));
            args.add(sep);

            message.append("VM Total Memory  : {} KB{}");
            long totalMemory = Runtime.getRuntime().totalMemory() >> 10;
            args.add(NumberFormat.getNumberInstance().format(totalMemory));
            args.add(sep);

            message.append("VM Free Memory   : {} KB{}");
            long freeMemory = Runtime.getRuntime().freeMemory() >> 10;
            args.add(NumberFormat.getNumberInstance().format(freeMemory));
            args.add(sep);

            message.append("VM Used Memory   : {} KB{}");
            long usedMemory = totalMemory - freeMemory;
            args.add(NumberFormat.getNumberInstance().format(usedMemory));
            args.add(sep);
        } catch (final Exception gee) {
            LOG.error("", gee);
        }

        message.append("System version   : {} Server [{}] initializing ...{}");
        args.add(Version.NAME);
        args.add(Version.getInstance().getVersionString());
        args.add(sep);

        message.append("Server Footprint : {}{}");
        args.add(OXException.getServerId());
        args.add(sep);

        LOG.info(message.toString(), args.toArray(new Object[args.size()]));
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
