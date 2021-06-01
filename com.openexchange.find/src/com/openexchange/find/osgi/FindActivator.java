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

package com.openexchange.find.osgi;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.find.SearchService;
import com.openexchange.find.internal.AvailableModules;
import com.openexchange.find.internal.MandatoryAccounts;
import com.openexchange.find.internal.MandatoryFolders;
import com.openexchange.find.internal.SearchDriverManager;
import com.openexchange.find.internal.SearchServiceImpl;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * The activator for find bundle.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Exception handling + logging
 * @since 7.6.0
 */
public class FindActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link FindActivator}.
     */
    public FindActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, LeanConfigurationService.class, I18nServiceRegistry.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FindActivator.class);
        logger.info("Starting bundle: com.openexchange.find");
        Services.setServiceLookup(this);
        try {
            final SearchDriverManager driverManager = new SearchDriverManager(context, getService(ConfigViewFactory.class));
            track(ModuleSearchDriver.class, driverManager);
            openTrackers();

            final SearchServiceImpl searchService = new SearchServiceImpl(driverManager);
            registerService(SearchService.class, searchService);

            final MandatoryFolders mandatoryFolders = new MandatoryFolders(driverManager);
            registerService(PreferencesItemService.class, mandatoryFolders);
            registerService(ConfigTreeEquivalent.class, mandatoryFolders);

            final MandatoryAccounts mandatoryAccounts = new MandatoryAccounts(driverManager);
            registerService(PreferencesItemService.class, mandatoryAccounts);
            registerService(ConfigTreeEquivalent.class, mandatoryAccounts);

            final AvailableModules availableModules = new AvailableModules(driverManager);
            registerService(PreferencesItemService.class, availableModules);
            registerService(ConfigTreeEquivalent.class, availableModules);

            logger.info("Bundle successfully started: com.openexchange.find");
        } catch (Exception e) {
            logger.error("Error while starting bundle: com.openexchange.find", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(FindActivator.class).info("Stopping bundle: com.openexchange.find");
        super.stopBundle();
    }

}
