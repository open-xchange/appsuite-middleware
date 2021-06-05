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

package com.openexchange.folder.json.osgi;

import static com.openexchange.folder.json.services.ServiceRegistry.getInstance;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.file.storage.limit.FileLimitService;
import com.openexchange.folder.json.Constants;
import com.openexchange.folder.json.FolderConverter;
import com.openexchange.folder.json.FolderFieldRegistry;
import com.openexchange.folder.json.actions.FolderActionFactory;
import com.openexchange.folder.json.preferences.Tree;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.user.UserService;

/**
 * {@link FolderJSONActivator} - Activator for JSON folder interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderJSONActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderJSONActivator.class);

    private String module;
    private String servletPath;
    private FolderFieldRegistry fieldRegistry;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DispatcherPrefixService.class };
        //TODO handle LimitService optional?
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (ConfigurationService.class.equals(clazz)) {
            apply((ConfigurationService) getService(clazz));
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        restore();
    }

    @Override
    public synchronized void startBundle() throws Exception {
        try {
            /*
             * Configure
             */
            getInstance().addService(DispatcherPrefixService.class, getService(DispatcherPrefixService.class));
            apply(getService(ConfigurationService.class));
            /*
             * Service trackers
             */
            track(FileLimitService.class, new RegistryServiceTrackerCustomizer<FileLimitService>(context, getInstance(), FileLimitService.class));
            track(FolderService.class, new RegistryServiceTrackerCustomizer<FolderService>(context, getInstance(), FolderService.class));
            track(UserService.class, new RegistryServiceTrackerCustomizer<UserService>(context, getInstance(), UserService.class));
            track(ContentTypeDiscoveryService.class, new RegistryServiceTrackerCustomizer<ContentTypeDiscoveryService>(
                context,
                getInstance(),
                ContentTypeDiscoveryService.class));
            track(AdditionalFolderField.class, new FolderFieldCollector(context, Constants.ADDITIONAL_FOLDER_FIELD_LIST));
            track(ShareNotificationService.class, new RegistryServiceTrackerCustomizer<ShareNotificationService>(context, getInstance(), ShareNotificationService.class));
            track(SubscriptionSourceDiscoveryService.class, new RegistryServiceTrackerCustomizer<SubscriptionSourceDiscoveryService>(context, getInstance(), SubscriptionSourceDiscoveryService.class));
            track(I18nServiceRegistry.class, new RegistryServiceTrackerCustomizer<I18nServiceRegistry>(context, getInstance(), I18nServiceRegistry.class));
            /*
             * Open trackers
             */
            openTrackers();
            fieldRegistry = FolderFieldRegistry.getInstance();
            fieldRegistry.startUp(context);
            /*
             * Register module
             */
            registerModule(FolderActionFactory.getInstance(), Constants.getModule());
            /*
             * Preference item
             */
            registerService(PreferencesItemService.class, new Tree());
            registerService(LoginHandlerService.class, new FolderConsistencyLoginHandler());
            /*
             * Result converter
             */
            registerService(ResultConverter.class, new FolderConverter());
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        try {
            if (null != fieldRegistry) {
                fieldRegistry.shutDown();
                fieldRegistry = null;
                FolderFieldRegistry.releaseInstance();
            }
            super.stopBundle();
            /*
             * Restore
             */
            restore();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    private void apply(final ConfigurationService configurationService) {
        ServiceRegistry.getInstance().addService(ConfigurationService.class, configurationService);
        final String tmpModule = configurationService.getProperty("com.openexchange.folder.json.module");
        if (null != tmpModule) {
            final String cmod = Constants.getModule();
            if (!cmod.equals(tmpModule)) {
                /*
                 * Remember old module and apply new one
                 */
                module = cmod;
                Constants.setModule(tmpModule);
            } else {
                Constants.setModule(null);
            }
        } else {
            Constants.setModule(null);
        }
        final String tmpServletPath = configurationService.getProperty("com.openexchange.folder.json.servletPath");
        if (null != tmpServletPath) {
            final String cpath = Constants.getServletPath();
            if (!cpath.equals(tmpServletPath)) {
                /*
                 * Remember old path and apply new one
                 */
                servletPath = cpath;
                Constants.setServletPath(tmpServletPath);
            }
        }
    }

    private void restore() {
        if (null != module) {
            Constants.setModule(module);
            module = null;
        }
        if (null != servletPath) {
            Constants.setServletPath(servletPath);
            servletPath = null;
        }
        ServiceRegistry.getInstance().removeService(ConfigurationService.class);
    }

    @Override
    public <S> boolean addService(final Class<S> clazz, final S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(final Class<? extends S> clazz) {
        return super.removeService(clazz);
    }
}
