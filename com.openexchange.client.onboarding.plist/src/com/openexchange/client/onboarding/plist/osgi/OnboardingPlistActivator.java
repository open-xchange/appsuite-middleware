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

package com.openexchange.client.onboarding.plist.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.plist.PListSigner;
import com.openexchange.client.onboarding.plist.download.PlistLinkProviderImpl;
import com.openexchange.client.onboarding.plist.internal.PListSignerImpl;
import com.openexchange.client.onboarding.plist.servlet.PListDownloadServlet;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.sms.SMSServiceSPI;
import com.openexchange.sms.tools.SMSBucketService;
import com.openexchange.user.UserService;


/**
 * {@link OnboardingPlistActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingPlistActivator extends HousekeepingActivator {

    private String downloadServletAlias;

    /**
     * Initializes a new {@link OnboardingPlistActivator}.
     */
    public OnboardingPlistActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { NotificationMailFactory.class, ConfigViewFactory.class, ConfigurationService.class,
            DispatcherPrefixService.class, HttpService.class, OnboardingService.class, UserService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.setServiceLookup(this);

        // Track services needed for SMS transport
        trackService(SMSServiceSPI.class);
        trackService(SMSBucketService.class);
        trackService(DownloadLinkProvider.class);
        trackService(HostnameService.class);
        openTrackers();

        PListSignerImpl signerImpl = new PListSignerImpl();
        addService(PListSigner.class, signerImpl);
        registerService(PListSigner.class, signerImpl);

        // Register PLIST link provider
        registerService(DownloadLinkProvider.class, new PlistLinkProviderImpl(this));

        // Register plist download servlet
        PListDownloadServlet downloadServlet = new PListDownloadServlet(this);
        String prefix = getService(DispatcherPrefixService.class).getPrefix();
        downloadServletAlias = prefix + PListDownloadServlet.SERVLET_PATH;
        getService(HttpService.class).registerServlet(downloadServletAlias, downloadServlet, null, null);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        String downloadServletAlias = this.downloadServletAlias;
        if (downloadServletAlias != null) {
            this.downloadServletAlias = null;
            HttpService httpService = getService(HttpService.class);
            if (httpService != null) {
                HttpServices.unregister(downloadServletAlias, httpService);
            }
        }
        removeService(PListSigner.class);
        super.stopBundle();
        Services.setServiceLookup(null);
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
