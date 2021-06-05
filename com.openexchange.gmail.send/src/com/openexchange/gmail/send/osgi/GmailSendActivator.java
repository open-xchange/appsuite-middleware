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

package com.openexchange.gmail.send.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.gmail.send.GmailSendProvider;
import com.openexchange.gmail.send.ListenerChain;
import com.openexchange.gmail.send.groupware.ReplaceSMTPTransportAccountsWithGmailSend;
import com.openexchange.gmail.send.oauth.GmailSendOAuthAccountAssociationProvider;
import com.openexchange.gmail.send.services.Services;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.mail.api.AuthenticationFailedHandlerService;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.listener.MailTransportListener;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;

/**
 * {@link GmailSendActivator} - The {@link BundleActivator activator} for Gmail Send bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GmailSendActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GmailSendActivator.class);

    /**
     * Initializes a new {@link GmailSendActivator}
     */
    public GmailSendActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, MailAccountStorageService.class, ConfigViewFactory.class, ThreadPoolService.class, ContextService.class, UserService.class,
            SSLSocketFactoryProvider.class, SSLConfigurationService.class, UserAwareSSLConfigurationService.class, RegionalSettingsService.class };
    }

    @Override
    public void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            RankingAwareNearRegistryServiceTracker<MailTransportListener> listing = new RankingAwareNearRegistryServiceTracker<MailTransportListener>(context, MailTransportListener.class);
            ListenerChain.initInstance(listing);
            rememberTracker(listing);

            trackService(HostnameService.class);
            trackService(AuditLogService.class);
            trackService(OAuthService.class);
            trackService(AuthenticationFailedHandlerService.class);
            trackService(MailOAuthService.class);
            openTrackers();

            registerService(OAuthAccountAssociationProvider.class, new GmailSendOAuthAccountAssociationProvider());

            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(
                new ReplaceSMTPTransportAccountsWithGmailSend(this)
                ));

            Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("protocol", GmailSendProvider.PROTOCOL_GMAIL_SEND.toString());
            registerService(TransportProvider.class, GmailSendProvider.getInstance(), dictionary);
        } catch (Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            ListenerChain.releaseInstance();
            super.stopBundle();
            Services.setServiceLookup(null);
        } catch (Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

}
