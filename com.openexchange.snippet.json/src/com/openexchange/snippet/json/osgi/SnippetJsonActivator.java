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

package com.openexchange.snippet.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetActionFactory;
import com.openexchange.snippet.json.converter.SnippetJSONResultConverter;

/**
 * {@link SnippetJsonActivator} - Activator for the snippet's JSON interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SnippetJsonActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link SnippetJsonActivator}.
     */
    public SnippetJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SnippetService.class, HtmlService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SnippetJsonActivator.class);

        final RankingAwareNearRegistryServiceTracker<SnippetService> snippetServiceRegistry = new RankingAwareNearRegistryServiceTracker<SnippetService>(context, SnippetService.class);
        rememberTracker(snippetServiceRegistry);
        trackService(CapabilityService.class);
        trackService(ManagedFileManagement.class);
        openTrackers();

        registerModule(new SnippetActionFactory(this, snippetServiceRegistry), "snippet");
        registerService(ResultConverter.class, new SnippetJSONResultConverter());
        log.info("Bundle successfully started: com.openexchange.snippet.json");
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        org.slf4j.LoggerFactory.getLogger(SnippetJsonActivator.class).info("Bundle stopped: com.openexchange.snippet.json");
    }
}
