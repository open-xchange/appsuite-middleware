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

package com.openexchange.mail.compose.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.DataSource;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DatabaseServiceDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.html.HtmlService;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.CompositionSpaceServiceImpl;
import com.openexchange.mail.compose.impl.CryptoCompositionSpaceService;
import com.openexchange.mail.compose.impl.attachment.AttachmentImageDataSource;
import com.openexchange.mail.compose.impl.attachment.AttachmentStorageServiceImpl;
import com.openexchange.mail.compose.impl.attachment.FileStorageAttachmentStorage;
import com.openexchange.mail.compose.impl.attachment.FileStrorageAttachmentFileLocationHandler;
import com.openexchange.mail.compose.impl.attachment.RdbAttachmentStorage;
import com.openexchange.mail.compose.impl.groupware.CompositionSpaceAddContentEncryptedFlag;
import com.openexchange.mail.compose.impl.groupware.CompositionSpaceCreateTableService;
import com.openexchange.mail.compose.impl.groupware.CompositionSpaceCreateTableTask;
import com.openexchange.mail.compose.impl.groupware.CompositionSpaceDeleteListener;
import com.openexchange.mail.compose.impl.security.CompositionSpaceKeyStorageServiceImpl;
import com.openexchange.mail.compose.impl.security.FileStorageCompositionSpaceKeyStorage;
import com.openexchange.mail.compose.impl.security.HazelcastCompositionSpaceKeyStorage;
import com.openexchange.mail.compose.impl.storage.db.RdbCompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.storage.inmemory.InMemoryCompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.storage.security.CryptoCompositionSpaceStorageService;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorage;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.mail.json.compose.ComposeHandlerRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link CompositionSpaceActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CompositionSpaceActivator extends HousekeepingActivator {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceActivator.class);
    }

    private InMemoryCompositionSpaceStorageService inmemoryStorage;

    /**
     * Initializes a new {@link CompositionSpaceActivator}.
     */
    public CompositionSpaceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, QuotaFileStorageService.class, CapabilityService.class, HtmlService.class,
            ConfigurationService.class, ContextService.class, UserService.class, ComposeHandlerRegistry.class, ObfuscatorService.class,
            ConfigViewFactory.class, CryptoService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final BundleContext context = this.context;

        final HazelcastCompositionSpaceKeyStorage hzCompositionSpaceKeyStorage = new HazelcastCompositionSpaceKeyStorage(this);

        ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

            @Override
            public synchronized HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                HazelcastInstance hazelcastInstance = context.getService(reference);
                String mapName = discoverMapName(hazelcastInstance.getConfig());
                if (null == mapName) {
                    LoggerHolder.LOG.warn("No distributed composition space key map found in Hazelcast configuration. Hazelcast will not be used to store AES keys!");
                    context.ungetService(reference);
                    return null;
                }

                CompositionSpaceActivator.this.addService(HazelcastInstance.class, hazelcastInstance);
                hzCompositionSpaceKeyStorage.setHazelcastResources(hazelcastInstance, mapName);
                return hazelcastInstance;
            }

            @Override
            public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hazelcastInstance) {
                // Ignore
            }

            @Override
            public synchronized void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hazelcastInstance) {
                hzCompositionSpaceKeyStorage.unsetHazelcastResources(true);
                CompositionSpaceActivator.this.removeService(HazelcastInstance.class);
                context.ungetService(reference);
            }

            /**
             * Discovers the map name from the supplied Hazelcast configuration.
             *
             * @param config The config object
             * @return The sessions map name
             * @throws IllegalStateException If no such map is available
             */
            private String discoverMapName(Config config) throws IllegalStateException {
                Map<String, MapConfig> mapConfigs = config.getMapConfigs();
                if (null != mapConfigs) {
                    for (String mapName : mapConfigs.keySet()) {
                        if (mapName.startsWith("cskeys-")) {
                            return mapName;
                        }
                    }
                }
                return null;
            }

        });
        rememberTracker(hzTracker);

        CompositionSpaceKeyStorageServiceImpl keyStorageService = new CompositionSpaceKeyStorageServiceImpl(this, context);
        rememberTracker(keyStorageService);

        AttachmentStorageServiceImpl attachmentStorageService = new AttachmentStorageServiceImpl(keyStorageService, this, context);
        rememberTracker(attachmentStorageService);

        openTrackers();

        registerService(CompositionSpaceKeyStorageService.class, keyStorageService);
        {
            Dictionary<String, Object> properties = new Hashtable<>(2);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(0));
            registerService(CompositionSpaceKeyStorage.class, hzCompositionSpaceKeyStorage, properties);
        }
        {
            Dictionary<String, Object> properties = new Hashtable<>(2);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(1));
            registerService(CompositionSpaceKeyStorage.class, FileStorageCompositionSpaceKeyStorage.initInstance(this), properties);
        }

        registerService(AttachmentStorageService.class, attachmentStorageService);

        registerService(AttachmentStorage.class, new FileStorageAttachmentStorage(this));
        registerService(FileLocationHandler.class, new FileStrorageAttachmentFileLocationHandler());
        registerService(AttachmentStorage.class, new RdbAttachmentStorage(this));

        {
            AttachmentImageDataSource attachmentImageDataSource = AttachmentImageDataSource.getInstance();
            attachmentImageDataSource.setService(attachmentStorageService);

            Dictionary<String, Object> attachmentImageProps = new Hashtable<String, Object>(1);
            attachmentImageProps.put("identifier", attachmentImageDataSource.getRegistrationName());
            registerService(DataSource.class, attachmentImageDataSource, attachmentImageProps);
            ImageActionFactory.addMapping(attachmentImageDataSource.getRegistrationName(), attachmentImageDataSource.getAlias());
        }

        ConfigurationService configurationService = getService(ConfigurationService.class);

        CompositionSpaceStorageService storageService;
        {
            DatabaseServiceDBProvider dbProvider = new DatabaseServiceDBProvider(getService(DatabaseService.class));
            RdbCompositionSpaceStorageService rdbStorage = new RdbCompositionSpaceStorageService(dbProvider, attachmentStorageService, this);
            boolean useInMemoryStorage = configurationService.getBoolProperty("com.openexchange.mail.compose.useInMemoryStorage", false);
            if (useInMemoryStorage) {
                long delayDuration = configurationService.getIntProperty("com.openexchange.mail.compose.delayDuration", 60000);
                long maxDelayDuration = configurationService.getIntProperty("com.openexchange.mail.compose.maxDelayDuration", 300000);
                InMemoryCompositionSpaceStorageService inmemoryStorage = new InMemoryCompositionSpaceStorageService(delayDuration, maxDelayDuration, rdbStorage);
                inmemoryStorage.start();
                this.inmemoryStorage = inmemoryStorage;
                storageService = inmemoryStorage;
            } else {
                storageService = rdbStorage;
            }

            // Set non-crypto composition space storage
            attachmentStorageService.setCompositionSpaceStorageService(storageService);

            storageService = new CryptoCompositionSpaceStorageService(storageService, keyStorageService, this);
        }
        registerService(CompositionSpaceStorageService.class, storageService);

        CompositionSpaceServiceImpl serviceImpl = new CompositionSpaceServiceImpl(storageService, attachmentStorageService, this);
        final CryptoCompositionSpaceService cryptoServiceImpl = new CryptoCompositionSpaceService(serviceImpl, keyStorageService, this);
        registerService(CompositionSpaceService.class, cryptoServiceImpl);

        {
            LoginHandlerService loginHandler = new LoginHandlerService() {

                private long getMaxIdleTimeMillis(Session session) throws OXException {
                    String defaultValue = "1W";

                    ConfigViewFactory viewFactory = CompositionSpaceActivator.this.getOptionalService(ConfigViewFactory.class);
                    if (null == viewFactory) {
                        return ConfigTools.parseTimespan(defaultValue);
                    }

                    ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
                    return ConfigTools.parseTimespan(ConfigViews.getDefinedStringPropertyFrom("com.openexchange.mail.compose.maxIdleTimeMillis", defaultValue, view));
                }

                @Override
                public void handleLogout(LoginResult logout) throws OXException {
                    // Ignore
                }

                @Override
                public void handleLogin(LoginResult login) throws OXException {
                    Session session = login.getSession();
                    if (null != session) {
                        long maxIdleTimeMillis = getMaxIdleTimeMillis(session);
                        if (maxIdleTimeMillis > 0) {
                            cryptoServiceImpl.closeExpiredCompositionSpaces(maxIdleTimeMillis, session);
                        }
                    }
                }
            };
            registerService(LoginHandlerService.class, loginHandler);
        }

        // Register Groupware stuff.
        registerService(CreateTableService.class, new CompositionSpaceCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(
            new CompositionSpaceCreateTableTask(),
            new CompositionSpaceAddContentEncryptedFlag()
        ));
        registerService(DeleteListener.class, new CompositionSpaceDeleteListener());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        InMemoryCompositionSpaceStorageService inmemoryStorage = this.inmemoryStorage;
        if (null != inmemoryStorage) {
            this.inmemoryStorage = null;
            inmemoryStorage.close();
        }
        FileStorageCompositionSpaceKeyStorage.unsetInstance();
        super.stopBundle();
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
