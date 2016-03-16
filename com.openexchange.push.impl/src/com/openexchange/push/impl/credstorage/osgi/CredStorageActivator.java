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

package com.openexchange.push.impl.credstorage.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.credstorage.CredentialStorageProvider;
import com.openexchange.push.impl.credstorage.OSGiCredentialStorageProvider;
import com.openexchange.push.impl.credstorage.Obfuscator;
import com.openexchange.push.impl.credstorage.inmemory.HazelcastCredentialStorage;
import com.openexchange.push.impl.credstorage.inmemory.portable.PortableCredentialsFactory;
import com.openexchange.push.impl.credstorage.rdb.RdbCredentialStorage;
import com.openexchange.push.impl.credstorage.rdb.groupware.CreateCredStorageTable;
import com.openexchange.push.impl.credstorage.rdb.groupware.CredStorageCreateTableTask;
import com.openexchange.push.impl.credstorage.rdb.groupware.CredStorageDeleteListener;
import com.openexchange.push.impl.portable.HazelcastInstanceNotActiveExceptionHandler;
import com.openexchange.push.impl.portable.PortablePushUserFactory;


/**
 * {@link CredStorageActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CredStorageActivator extends HousekeepingActivator implements HazelcastInstanceNotActiveExceptionHandler {

    /**
     * Initializes a new {@link CredStorageActivator}.
     */
    public CredStorageActivator() {
        super();
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HazelcastConfigurationService.class, CryptoService.class, DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger log = org.slf4j.LoggerFactory.getLogger(CredStorageActivator.class);
        CredStorageServices.setServiceLookup(this);
        final BundleContext context = this.context;
        final HazelcastConfigurationService hazelcastConfig = getService(HazelcastConfigurationService.class);

        ConfigurationService configService = getService(ConfigurationService.class);
        boolean credStoreEnabled = configService.getBoolProperty("com.openexchange.push.credstorage.enabled", false);

        final HazelcastCredentialStorage hzCredStorage;
        final RdbCredentialStorage rdbCredStorage;
        if (credStoreEnabled) {
            String key = configService.getProperty("com.openexchange.push.credstorage.passcrypt");
            if (Strings.isEmpty(key)) {
                throw new BundleException("Property \"com.openexchange.push.credstorage.enabled\" set to \"true\", but missing value for \"com.openexchange.push.credstorage.passcrypt\" property.");
            }

            // Register portables
            registerService(CustomPortableFactory.class, new PortablePushUserFactory());
            registerService(CustomPortableFactory.class, new PortableCredentialsFactory());

            Obfuscator obfuscator = new Obfuscator(key.trim());

            hzCredStorage = new HazelcastCredentialStorage(obfuscator, this, this);
            rdbCredStorage = configService.getBoolProperty("com.openexchange.push.credstorage.rdb", false) ? new RdbCredentialStorage(obfuscator) : null;
            // Check Hazelcast stuff
            if (hazelcastConfig.isEnabled()) {
                // Track HazelcastInstance service
                ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> customizer = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    @Override
                    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                        HazelcastInstance hzInstance = context.getService(reference);
                        try {
                            String mapName = hazelcastConfig.discoverMapName("credentials");
                            if (null == mapName) {
                                context.ungetService(reference);
                                return null;
                            }
                            addService(HazelcastInstance.class, hzInstance);
                            hzCredStorage.setHzMapName(mapName);
                            hzCredStorage.changeBackingMapToHz();
                            return hzInstance;
                        } catch (OXException e) {
                            log.warn("Couldn't initialize remote credentials map.", e);
                        } catch (RuntimeException e) {
                            log.warn("Couldn't initialize remote credentials map.", e);
                        }
                        context.ungetService(reference);
                        return null;
                    }

                    @Override
                    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        // Ignore
                    }

                    @Override
                    public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        removeService(HazelcastInstance.class);
                        hzCredStorage.changeBackingMapToLocalMap();
                        context.ungetService(reference);
                    }
                };
                track(HazelcastInstance.class, customizer);
            }
        } else {
            hzCredStorage = null;
            rdbCredStorage = null;
        }

        OSGiCredentialStorageProvider storageProvider = new OSGiCredentialStorageProvider(context);
        rememberTracker(storageProvider);

        openTrackers();

        {
            CredStoragePasswordChangeHandler handler = new CredStoragePasswordChangeHandler();
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put(EventConstants.EVENT_TOPIC, handler.getTopic());
            registerService(EventHandler.class, handler, props);
        }

        registerService(CreateTableService.class, new CreateCredStorageTable(), null);
        registerService(DeleteListener.class, new CredStorageDeleteListener(), null);
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CredStorageCreateTableTask()));

        registerService(CredentialStorageProvider.class, storageProvider);
        addService(CredentialStorageProvider.class, storageProvider);
        if (null != hzCredStorage) {
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(Constants.SERVICE_RANKING, Integer.valueOf(0));
            registerService(CredentialStorage.class, hzCredStorage, serviceProperties);
        }
        if (null != rdbCredStorage) {
            // Higher ranked
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(Constants.SERVICE_RANKING, Integer.valueOf(10));
            registerService(CredentialStorage.class, rdbCredStorage, serviceProperties);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        CredStorageServices.setServiceLookup(null);
        super.stopBundle();
    }

}
