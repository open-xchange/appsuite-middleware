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

package com.openexchange.snippet.utils.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataSource;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.html.HtmlService;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.utils.SnippetImageDataSource;
import com.openexchange.snippet.utils.internal.Services;

/**
 * {@link SnippetUtilsActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SnippetUtilsActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SnippetUtilsActivator}.
     */
    public SnippetUtilsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HtmlService.class, ManagedFileManagement.class, ConfigViewFactory.class, CapabilityService.class, LeanConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        RankingAwareNearRegistryServiceTracker<SnippetService> snippetServiceRegistry = new RankingAwareNearRegistryServiceTracker<SnippetService>(context, SnippetService.class);
        rememberTracker(snippetServiceRegistry);
        trackService(ConversionService.class);
        openTrackers();

        {
            SnippetImageDataSource signImageDataSource = SnippetImageDataSource.getInstance();
            signImageDataSource.setServiceListing(snippetServiceRegistry);
            Dictionary<String, Object> signImageProps = new Hashtable<String, Object>(1);
            signImageProps.put("identifier", signImageDataSource.getRegistrationName());
            registerService(DataSource.class, signImageDataSource, signImageProps);
            ImageActionFactory.addMapping(signImageDataSource.getRegistrationName(), signImageDataSource.getAlias());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

}
