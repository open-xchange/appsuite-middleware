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

package com.openexchange.gdpr.dataexport.provider.mail.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.gdpr.dataexport.DataExportProvider;
import com.openexchange.gdpr.dataexport.DataExportStatusChecker;
import com.openexchange.gdpr.dataexport.provider.mail.generator.SessionGenerator;
import com.openexchange.gdpr.dataexport.provider.mail.generator.impl.LoginAuthSessionGenerator;
import com.openexchange.gdpr.dataexport.provider.mail.generator.impl.MasterAuthSessionGenerator;
import com.openexchange.gdpr.dataexport.provider.mail.generator.impl.OAuthSessionGenerator;
import com.openexchange.gdpr.dataexport.provider.mail.internal.MailDataExportProvider;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link MailDataExportProviderActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MailDataExportProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailDataExportProviderActivator}.
     */
    public MailDataExportProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, ConfigurationService.class, UserService.class, MailService.class,
            MailAccountStorageService.class, SessiondService.class, DataExportStatusChecker.class, ObfuscatorService.class,
            UserConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Initialize session generator registry
        SessionGeneratorRegistryImpl registry = new SessionGeneratorRegistryImpl(context);
        rememberTracker(registry);
        openTrackers();

        // Register basic session generators
        registerService(SessionGenerator.class, new MasterAuthSessionGenerator());
        registerService(SessionGenerator.class, new LoginAuthSessionGenerator(this));
        registerService(SessionGenerator.class, new OAuthSessionGenerator(this));

        // Register data export provider for mails
        registerService(DataExportProvider.class, new MailDataExportProvider(registry, this));
    }

}
