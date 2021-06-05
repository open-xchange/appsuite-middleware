/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.compose.mailstorage.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.DataSource;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.html.HtmlService;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.lock.LockService;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceConfig;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceImageDataSource;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceServiceFactory;
import com.openexchange.mail.compose.mailstorage.association.AssociationStorageManager;
import com.openexchange.mail.compose.mailstorage.cache.CacheManagerFactory;
import com.openexchange.mail.compose.mailstorage.cache.file.FileCacheManagerFactory;
import com.openexchange.mail.compose.mailstorage.cleanup.CompositionSpaceCleanUpRegistry;
import com.openexchange.mail.compose.mailstorage.storage.MailStorage;
import com.openexchange.mail.json.compose.ComposeHandlerRegistry;
import com.openexchange.mail.json.compose.share.AttachmentStorageRegistry;
import com.openexchange.mail.mime.crypto.PGPMailRecognizer;
import com.openexchange.mail.service.EncryptedMailService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.Tools;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.uploaddir.UploadDirService;
import com.openexchange.user.UserService;

/**
 * {@link MailStorageCompositionSpaceActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.5
 */
public class MailStorageCompositionSpaceActivator extends HousekeepingActivator {

    private FileCacheManagerFactory cacheManagerFactory;
    private AssociationStorageManager associationStorageManager;

    /**
     * Initializes a new {@link MailStorageCompositionSpaceActivator}.
     */
    public MailStorageCompositionSpaceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, QuotaFileStorageService.class, CapabilityService.class, HtmlService.class,
            ConfigurationService.class, ContextService.class, UserService.class, ComposeHandlerRegistry.class, ObfuscatorService.class,
            ConfigViewFactory.class, CryptoService.class, MailAccountStorageService.class, ThreadPoolService.class, TimerService.class,
            SessiondService.class, MailService.class, UploadDirService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        trackService(UnifiedInboxManagement.class);
        trackService(AttachmentStorageRegistry.class);
        trackService(PGPMailRecognizer.class);
        trackService(CryptographicAwareMailAccessFactory.class);
        trackService(CryptographicServiceAuthenticationFactory.class);
        trackService(EncryptedMailService.class);
        trackService(LockService.class);
        openTrackers();

        MailStorageCompositionSpaceConfig config = MailStorageCompositionSpaceConfig.initInstance(this);
        registerService(ForcedReloadable.class, config);

        FileCacheManagerFactory cacheManagerFactory = new FileCacheManagerFactory(this);
        this.cacheManagerFactory = cacheManagerFactory;
        addService(CacheManagerFactory.class, cacheManagerFactory);
        registerService(Reloadable.class, cacheManagerFactory);

        MailStorage mailStorage = new MailStorage(this);
        AssociationStorageManager associationStorageManager = new AssociationStorageManager();
        this.associationStorageManager = associationStorageManager;

        MailStorageCompositionSpaceServiceFactory compositionSpaceServiceFactory = new MailStorageCompositionSpaceServiceFactory(mailStorage, associationStorageManager, this);
        {
            MailStorageCompositionSpaceImageDataSource mailStorageCompositionSpaceImageDataSource = MailStorageCompositionSpaceImageDataSource.getInstance();
            mailStorageCompositionSpaceImageDataSource.setServiceFactory(compositionSpaceServiceFactory);

            Dictionary<String, Object> attachmentImageProps = new Hashtable<String, Object>(1);
            attachmentImageProps.put("identifier", mailStorageCompositionSpaceImageDataSource.getRegistrationName());
            registerService(DataSource.class, mailStorageCompositionSpaceImageDataSource, attachmentImageProps);
            ImageActionFactory.addMapping(mailStorageCompositionSpaceImageDataSource.getRegistrationName(), mailStorageCompositionSpaceImageDataSource.getAlias());
        }

        registerService(CompositionSpaceServiceFactory.class, compositionSpaceServiceFactory, Tools.withRanking(compositionSpaceServiceFactory.getRanking()));

        {
            Optional<CompositionSpaceCleanUpRegistry> optionalCleanUpRegistry = CompositionSpaceCleanUpRegistry.initInstance(compositionSpaceServiceFactory, this);
            if (optionalCleanUpRegistry.isPresent()) {
                CompositionSpaceCleanUpRegistry cleanUpRegistry = optionalCleanUpRegistry.get();
                Dictionary<String, Object> serviceProperties = new Hashtable<>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
                registerService(EventHandler.class, cleanUpRegistry, serviceProperties);
            }
        }

        /*-
         *
        {
            LoginHandlerService loginHandler = new LoginHandlerService() {

                @Override
                public void handleLogout(LoginResult logout) throws OXException {
                    // Ignore
                }

                @Override
                public void handleLogin(LoginResult login) throws OXException {
                    Session session = login.getSession();
                    if (null != session) {
                        CompositionSpaceCleanUpRegistry cleanUpRegistry = CompositionSpaceCleanUpRegistry.getInstance();
                        if (cleanUpRegistry != null) {
                            cleanUpRegistry.scheduleCleanUpFor(session);
                        }
                    }
                }
            };
            registerService(LoginHandlerService.class, loginHandler);
        }
        */
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        CompositionSpaceCleanUpRegistry.releaseInstance();
        MailStorageCompositionSpaceConfig.dropInstance();
        FileCacheManagerFactory cacheManagerFactory = this.cacheManagerFactory;
        if (cacheManagerFactory != null) {
            this.cacheManagerFactory = null;
            cacheManagerFactory.shutDown();
        }
        removeService(CacheManagerFactory.class);
        AssociationStorageManager associationStorageManager = this.associationStorageManager;
        if (associationStorageManager != null) {
            this.associationStorageManager = null;
            associationStorageManager.shutDown();
        }
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
