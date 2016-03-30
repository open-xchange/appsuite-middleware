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

package com.openexchange.tools.oxfolder;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.server.Initialization;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import com.openexchange.tools.oxfolder.permissionLoader.PermissionLoaderService;

/**
 * <tt>OXFolderProperties</tt> contains both folder properties and folder cache properties
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderProperties implements Initialization, CacheAvailabilityListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderProperties.class);

    private static OXFolderProperties instance = new OXFolderProperties();

    /**
     * @return The singleton instance of {@link OXFolderProperties}
     */
    public static OXFolderProperties getInstance() {
        return instance;
    }

    /*
     * Fields
     */
    private final AtomicBoolean started = new AtomicBoolean();

    private boolean enableDBGrouping = true;

    private boolean enableFolderCache = true;

    private boolean ignoreSharedAddressbook = false;

    volatile boolean enableInternalUsersEdit = true;

    private boolean enableSharedFolderCaching = true;

    private OXFolderProperties() {
        super();
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("Folder properties have already been started", new Throwable());
            return;
        }
        init();
        PermissionLoaderService.getInstance().startUp();
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (reg != null) {
            reg.registerListener(this);
        }
        if (enableFolderCache) {
            FolderCacheManager.initInstance();
        }
        FolderQueryCacheManager.initInstance();
        ConditionTreeMapManagement.startInstance();
    }

    @Override
    public void stop() throws OXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error("Folder properties cannot be stopped since they have not been started before", new Throwable());
            return;
        }
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (reg != null) {
            reg.unregisterListener(this);
        }
        ConditionTreeMapManagement.stopInstance();
        FolderCacheManager.releaseInstance();
        FolderQueryCacheManager.releaseInstance();
        PermissionLoaderService.dropInstance();
        reset();
    }

    @Override
    public void handleAvailability() throws OXException {
        final FolderCacheManager fcm = FolderCacheManager.getInstance();
        if (null != fcm) {
            fcm.initCache();
        }
        final FolderQueryCacheManager fqcm = FolderQueryCacheManager.getInstance();
        if (null != fqcm) {
            fqcm.initCache();
        }
    }

    @Override
    public void handleAbsence() throws OXException {
        final FolderCacheManager fcm = FolderCacheManager.getInstance();
        if (null != fcm) {
            fcm.releaseCache();
        }
        final FolderQueryCacheManager fqcm = FolderQueryCacheManager.getInstance();
        if (null != fqcm) {
            fqcm.releaseCache();
        }
    }

    private void reset() {
        enableSharedFolderCaching = true;
        enableDBGrouping = true;
        enableFolderCache = true;
        ignoreSharedAddressbook = false;
        enableInternalUsersEdit = true;
    }

    private void init() {
        final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configurationService == null) {
            LOG.error("Cannot look-up configuration service");
            return;
        }
        /*
         * ENABLE_SHARED_FOLDER_CACHING
         */
        String value = configurationService.getProperty("ENABLE_SHARED_FOLDER_CACHING");
        if (null != value) {
            enableSharedFolderCaching = Boolean.parseBoolean(value.trim());
        }
        /*
         * ENABLE_DB_GROUPING
         */
        value = configurationService.getProperty("ENABLE_DB_GROUPING");
        if (null == value) {
            LOG.warn("Missing property ENABLE_DB_GROUPING.");
        } else {
            enableDBGrouping = Boolean.parseBoolean(value.trim());
        }
        /*
         * ENABLE_FOLDER_CACHE
         */
        value = configurationService.getProperty("ENABLE_FOLDER_CACHE");
        if (null == value) {
            LOG.warn("Missing property ENABLE_FOLDER_CACHE");
        } else {
            enableFolderCache = Boolean.parseBoolean(value.trim());
        }
        /*
         * IGNORE_SHARED_ADDRESSBOOK
         */
        value = configurationService.getProperty("IGNORE_SHARED_ADDRESSBOOK");
        if (null == value) {
            LOG.warn("Missing property IGNORE_SHARED_ADDRESSBOOK");
        } else {
            ignoreSharedAddressbook = Boolean.parseBoolean(value.trim());
        }
        /*
         * ENABLE_INTERNAL_USER_EDIT
         */
        value = configurationService.getProperty("ENABLE_INTERNAL_USER_EDIT");
        if (null == value) {
            LOG.warn("Missing property ENABLE_INTERNAL_USER_EDIT");
        } else {
            final boolean enableInternalUsersEdit = Boolean.parseBoolean(value.trim());
            this.enableInternalUsersEdit = enableInternalUsersEdit;
        }
        /*
         * Log info
         */
        logInfo();
    }

    private void logInfo() {
        final StringBuilder sb = new StringBuilder(512);
        sb.append("\nFolder Properties & Folder Cache Properties:\n");
        sb.append("\tENABLE_SHARED_FOLDER_CACHING=").append(enableSharedFolderCaching).append('\n');
        sb.append("\tENABLE_DB_GROUPING=").append(enableDBGrouping).append('\n');
        sb.append("\tENABLE_FOLDER_CACHE=").append(enableFolderCache).append('\n');
        sb.append("\tENABLE_INTERNAL_USER_EDIT=").append(enableInternalUsersEdit).append('\n');
        sb.append("\tIGNORE_SHARED_ADDRESSBOOK=").append(ignoreSharedAddressbook);
        LOG.info(sb.toString());
    }

    private static final String WARN_FOLDER_PROPERTIES_INIT = "Folder properties have not been started.";

    /**
     * @return <code>true</code> if database grouping is enabled ( <code>GROUB BY</code>); otherwise <code>false</code>
     */
    public static boolean isEnableDBGrouping() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableDBGrouping;
    }

    /**
     * @return <code>true</code> if folder cache is enabled; otherwise <code>false</code>
     */
    public static boolean isEnableFolderCache() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableFolderCache;
    }

    /**
     * @return <code>true</code> if shared address book should be omitted in folder tree display; otherwise <code>false</code>
     */
    public static boolean isIgnoreSharedAddressbook() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.ignoreSharedAddressbook;
    }

    /**
     * Context's system folder "<code>Global address book</code>" is created with write permission set to
     * {@link OCLPermission#WRITE_OWN_OBJECTS} if this property is set to <code>true</code>
     *
     * @return <code>true</code> if contacts located in global address book may be edited; otherwise <code>false</code>
     */
    public static boolean isEnableInternalUsersEdit() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableInternalUsersEdit;
    }

    /**
     * Checks whether caching for shared folders is enabled or not.
     *
     * @return <code>true</code> if caching for shared folder is enabled; otherwise <code>false</code>
     */
    public static boolean isEnableSharedFolderCaching() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableSharedFolderCaching;
    }

    /*-
     * ############################# MBEAN STUFF #############################
     */

    /**
     * Registers the global address book restorer MBean.
     *
     * @param managementService The management service
     * @return The object name of registered MBean
     */
    public static ObjectName registerRestorerMBean(final ManagementService managementService) {
        try {
            final ObjectName objectName = getObjectName(GABRestorerMBeanImpl.class.getName(), GABRestorerMBean.GAB_DOMAIN);
            managementService.registerMBean(objectName, new GABRestorerMBeanImpl());
            return objectName;
        } catch (final MalformedObjectNameException e) {
            LOG.error("", e);
        } catch (final NotCompliantMBeanException e) {
            LOG.error("", e);
        } catch (final OXException e) {
            LOG.error("", e);
        }
        return null;
    }

    /**
     * Unregisters the global address book restorer MBean.
     *
     * @param objectName The object name of registered MBean
     * @param managementService The management service
     */
    public static void unregisterRestorerMBean(final ObjectName objectName, final ManagementService managementService) {
        if (objectName != null) {
            try {
                managementService.unregisterMBean(objectName);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name and domain name.
     *
     * @param className The class name to use as object name
     * @param domain The domain name
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     */
    public static ObjectName getObjectName(final String className, final String domain) throws MalformedObjectNameException {
        final int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", pos == -1 ? className : className.substring(pos + 1));
    }
}
