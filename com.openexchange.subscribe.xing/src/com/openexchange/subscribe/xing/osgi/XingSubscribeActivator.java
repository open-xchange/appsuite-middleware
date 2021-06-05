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

package com.openexchange.subscribe.xing.osgi;

import org.osgi.framework.ServiceRegistration;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.xing.Services;
import com.openexchange.subscribe.xing.XingSubscribeService;
import com.openexchange.subscribe.xing.groupware.XingSubscriptionsOAuthAccountDeleteListener;
import com.openexchange.subscribe.xing.oauth.XingContactsOAuthAccountAssociationProvider;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.xing.access.XingOAuthAccessProvider;

/**
 * {@link XingSubscribeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XingSubscribeActivator extends HousekeepingActivator {

    private ServiceRegistration<SubscribeService> serviceRegistration;
    private ServiceRegistration<OAuthAccountAssociationProvider> associationProviderRegistration;
    private ServiceRegistration<OAuthAccountDeleteListener> deleteListenerRegistration;

    /**
     * Initializes a new {@link XingSubscribeActivator}.
     */
    public XingSubscribeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { OAuthService.class, ContextService.class, SessiondService.class, DatabaseService.class, XingOAuthAccessProvider.class, ThreadPoolService.class, FolderService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServices(this);
        track(OAuthServiceMetaData.class, new OAuthServiceMetaDataRegisterer(context, this));
        trackService(SubscriptionExecutionService.class);
        trackService(FolderUpdaterRegistry.class);
        openTrackers();
        /*
         * Register update task
         */
        DefaultUpdateTaskProviderService providerService = new DefaultUpdateTaskProviderService(new com.openexchange.subscribe.xing.groupware.XingCrawlerSubscriptionsRemoverTask());
        registerService(UpdateTaskProviderService.class.getName(), providerService);
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterSubscribeService();
        super.stopBundle();
        Services.setServices(null);
    }

    /**
     * Registers the subscribe service.
     *
     * @throws OXException
     */
    public synchronized void registerSubscribeService() throws OXException {
        if (null == serviceRegistration) {
            XingSubscribeService xingSubscribeService = new XingSubscribeService(getService(OAuthServiceMetaData.class), this);
            serviceRegistration = context.registerService(SubscribeService.class, xingSubscribeService, null);
            org.slf4j.LoggerFactory.getLogger(XingSubscribeActivator.class).info("XingSubscribeService was started");

            if (deleteListenerRegistration == null) {
                // Register the delete listener
                ContextService contextService = Services.getService(ContextService.class);
                XingSubscriptionsOAuthAccountDeleteListener deleteListener = new XingSubscriptionsOAuthAccountDeleteListener(xingSubscribeService, contextService);
                deleteListenerRegistration = context.registerService(OAuthAccountDeleteListener.class, deleteListener, null);
            }
        }
        if (associationProviderRegistration == null) {
            associationProviderRegistration = context.registerService(OAuthAccountAssociationProvider.class, new XingContactsOAuthAccountAssociationProvider(this), null);
        }
    }

    /**
     * Un-registers the subscribe service.
     */
    public synchronized void unregisterSubscribeService() {
        ServiceRegistration<SubscribeService> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            this.serviceRegistration = null;
        }
        {
            ServiceRegistration<OAuthAccountDeleteListener> deleteRegistration = this.deleteListenerRegistration;
            if (null != deleteRegistration) {
                deleteRegistration.unregister();
                this.deleteListenerRegistration = null;
            }
        }
        {
            ServiceRegistration<OAuthAccountAssociationProvider> registration = this.associationProviderRegistration;
            if (null != registration) {
                registration.unregister();
                this.associationProviderRegistration = null;
            }
        }
        org.slf4j.LoggerFactory.getLogger(XingSubscribeActivator.class).info("XingSubscribeService was stopped");
    }

    /**
     * Adds given service.
     *
     * @param authServiceMetaData The service to add
     */
    public void setOAuthServiceMetaData(final OAuthServiceMetaData oAuthServiceMetaData) {
        addService(OAuthServiceMetaData.class, oAuthServiceMetaData);
    }
}
