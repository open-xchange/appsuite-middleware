/*
 *
 *    OPEN-XCHANGE legal informationcom.openexchange.security.manager
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.security.manager.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.security.AccessControlException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.security.manager.OXSecurityManager;
import com.openexchange.security.manager.SecurityManagerPropertyProvider;
import com.openexchange.security.manager.configurationReader.ConfigurationReader;
import com.openexchange.security.manager.configurationReader.FileStorageReader;
import com.openexchange.security.manager.impl.FolderPermission;
import com.openexchange.security.manager.impl.OXSecurityManagerImpl;

/**
 * {@link SecurityManagerActivator}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class SecurityManagerActivator extends HousekeepingActivator {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SecurityManagerActivator.class);
    private static final String TEST_FILE = "openexchange.security.policy.deny.testfile";
    final ConcurrentHashMap<Integer, SecurityManagerPropertyProvider> providers = new ConcurrentHashMap<>();;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConditionalPermissionAdmin.class };
    }


    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        final org.slf4j.Logger LOG = SecurityManagerActivator.LOG;

        if (System.getSecurityManager() == null) {
            LOG.info("No java security manager enabled.  OXSecurityManager will not be loaded");
            return;
        }
        // Check OSGI security manager loaded
        if (!System.getSecurityManager().getClass().getCanonicalName().contains("EquinoxSecurityManager")) {
            LOG.error("Wrong security manager type.  Please set -Dorg.osgi.framework.security=osgi .  OXSecurityManager will not be loaded");
            return;
        }

        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());
        OXSecurityManager securityManager = new OXSecurityManagerImpl(this);
        securityManager.loadFromPolicyFile();

        registerService(OXSecurityManager.class, securityManager);
        registerService(Reloadable.class, securityManager);

        // Track when configuration service is loaded.  Once loaded, send to configuration reader to add security whitelists
        ServiceTrackerCustomizer<ConfigurationService, ConfigurationService> confServiceTracker = new ServiceTrackerCustomizer<ConfigurationService, ConfigurationService>() {

            /**
             * Perform a simple test to see if security manager is functioning.  See if can read from root directory.
             * Log results
             */
             private boolean testSecurityManager() {
                 try {
                     File testFile = new File(System.getProperty(TEST_FILE) == null ? "/test" : System.getProperty(TEST_FILE));
                     testFile.exists();
                     return false;
                 } catch (AccessControlException ex) {
                     return true;
                 }
             }

            @Override
            public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
                ConfigurationService configService = context.getService(reference);

                ConfigurationReader secConfig = new ConfigurationReader(new AtomicReference<ConcurrentHashMap<Integer, SecurityManagerPropertyProvider>>(providers), configService);
                try {
                    List<FolderPermission> folderPermissions = secConfig.readConfigFolders();
                    if (folderPermissions != null) {
                        securityManager.insertFolderPolicy(folderPermissions);
                    }
                } catch (OXException e) {
                    LOG.error("!!!!!!!  Problem loading allowed file paths from configuration files.  Server will likely not function properly.", e);
                }
                addService(ConfigurationService.class, configService);
                addService(ConfigurationReader.class, secConfig);
                if (testSecurityManager()) {
                    LOG.info("Security manager started successfully and functioning");
                } else {
                    LOG.error("Security manager not functioning properly.  Please check configuration");
                }
                return configService;
            }

            @Override
            public void modifiedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
                // ignore

            }

            @Override
            public void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
                removeService(ConfigurationService.class);
                context.ungetService(reference);
            }

        };

        track(SecurityManagerPropertyProvider.class, new SimpleRegistryListener<SecurityManagerPropertyProvider>() {

            @Override
            public void added(ServiceReference<SecurityManagerPropertyProvider> ref, SecurityManagerPropertyProvider service) {
                providers.put(I(service.hashCode()), service);
                ConfigurationReader opt = getOptionalService(ConfigurationReader.class);
                if(opt != null) {
                    Optional<Object> props = Optional.ofNullable(ref.getProperty(SecurityManagerPropertyProvider.PROPS_SERVICE_KEY));
                    if(props.isPresent()) {
                        Optional<List<FolderPermission>> newPerms = opt.checkProvider(Strings.splitByComma(props.get().toString()), service);
                        if(newPerms.isPresent()) {
                            try {
                                securityManager.insertFolderPolicy(newPerms.get());
                            } catch (OXException e) {
                                LOG.error("!!!!!!!  Problem loading allowed file paths from registered provider.  Server will likely not function properly.", e);
                            }
                        }
                    } else {
                        LOG.warn("Missing properties in the SecurityManagerPropertyProvider service dictionary of bundle {}", ref.getBundle().getSymbolicName());
                    }
                }
            }

            @Override
            public void removed(ServiceReference<SecurityManagerPropertyProvider> ref, SecurityManagerPropertyProvider service) {
                providers.remove(I(service.hashCode()));
            }});

        // Wait for database service to be loaded.  Once loaded, add filestores from configdb
        ServiceTrackerCustomizer<DatabaseService, DatabaseService> DatabaseServiceTracker = new ServiceTrackerCustomizer<DatabaseService, DatabaseService>() {

            @Override
            public DatabaseService addingService(ServiceReference<DatabaseService> reference) {
                DatabaseService db = context.getService(reference);
                try {
                    new FileStorageReader(db, securityManager).updateSecurityForFileStore();
                } catch (OXException e) {
                    LOG.error("Problem updating security permissions based on filestores ", e);
                }
                addService(DatabaseService.class, db);
                return db;
            }

            @Override
            public void modifiedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
                // ignore

            }

            @Override
            public void removedService(ServiceReference<DatabaseService> reference, DatabaseService service) {
                removeService(FileStorageInfoService.class);
                context.ungetService(reference);
            }

        };
        track(ConfigurationService.class, confServiceTracker);
        track(DatabaseService.class, DatabaseServiceTracker);
        openTrackers();

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        this.providers.clear();
        super.stopBundle();
    }

    @Override
    protected <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }
}
