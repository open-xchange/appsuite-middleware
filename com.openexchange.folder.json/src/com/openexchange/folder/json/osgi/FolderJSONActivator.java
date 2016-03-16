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

package com.openexchange.folder.json.osgi;

import static com.openexchange.folder.json.services.ServiceRegistry.getInstance;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.folder.json.Constants;
import com.openexchange.folder.json.FolderFieldRegistry;
import com.openexchange.folder.json.actions.FolderActionFactory;
import com.openexchange.folder.json.preferences.Tree;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;

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
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        apply((ConfigurationService) getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        restore();
    }

    @Override
    public void startBundle() throws Exception {
        try {
            /*
             * Configure
             */
            getInstance().addService(DispatcherPrefixService.class, getService(DispatcherPrefixService.class));
            apply(getService(ConfigurationService.class));
            /*
             * Service trackers
             */
            track(FolderService.class, new RegistryServiceTrackerCustomizer<FolderService>(context, getInstance(), FolderService.class));
            track(ContentTypeDiscoveryService.class, new RegistryServiceTrackerCustomizer<ContentTypeDiscoveryService>(
                context,
                getInstance(),
                ContentTypeDiscoveryService.class));
            track(AdditionalFolderField.class, new FolderFieldCollector(context, Constants.ADDITIONAL_FOLDER_FIELD_LIST));
            track(ShareNotificationService.class, new RegistryServiceTrackerCustomizer<ShareNotificationService>(context, getInstance(), ShareNotificationService.class));
            track(SubscriptionSourceDiscoveryService.class, new RegistryServiceTrackerCustomizer<SubscriptionSourceDiscoveryService>(context, getInstance(), SubscriptionSourceDiscoveryService.class));
            track(PublicationTargetDiscoveryService.class, new RegistryServiceTrackerCustomizer<PublicationTargetDiscoveryService>(context, getInstance(), PublicationTargetDiscoveryService.class));
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
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            if (null != fieldRegistry) {
                fieldRegistry.shutDown();
                FolderFieldRegistry.releaseInstance();
            }
            cleanUp();
            /*
             * Restore
             */
            restore();
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(FolderJSONActivator.class).error("", e);
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
