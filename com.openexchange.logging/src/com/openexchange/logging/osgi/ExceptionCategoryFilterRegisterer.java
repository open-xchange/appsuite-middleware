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

package com.openexchange.logging.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.logging.filter.ExceptionCategoryFilter;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;

/**
 * {@link ExceptionCategoryFilterRegisterer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ExceptionCategoryFilterRegisterer implements ServiceTrackerCustomizer<ConfigurationService, ConfigurationService>, Reloadable {

    private static final String PROP_SUPPRESSED_CATEGORIES = "com.openexchange.log.suppressedCategories";

    private final BundleContext context;
    private final RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private final IncludeStackTraceService traceService;

    /**
     * Initializes a new {@link ExceptionCategoryFilterRegisterer}.
     *
     * @param context The bundle context
     * @param rankingAwareTurboFilterList The list of logback turbo filters
     * @param traceService The "include stack trace" service
     */
    public ExceptionCategoryFilterRegisterer(BundleContext context, RankingAwareTurboFilterList rankingAwareTurboFilterList, IncludeStackTraceService traceService) {
        super();
        this.context = context;
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        this.traceService = traceService;
    }

    @Override
    public synchronized ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
        ConfigurationService service = context.getService(reference);
        String suppressedCategories = service.getProperty(PROP_SUPPRESSED_CATEGORIES, "USER_INPUT");
        ExceptionCategoryFilter.createInstance(suppressedCategories, traceService, rankingAwareTurboFilterList);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        // Nothing to do
    }

    @Override
    public synchronized void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        ExceptionCategoryFilter.dropInstance(rankingAwareTurboFilterList);
    }

    // ------------------------------------------------ Reloadable stuff -------------------------------------------------------------------

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(PROP_SUPPRESSED_CATEGORIES);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        ExceptionCategoryFilter.setCategories(configService.getProperty(PROP_SUPPRESSED_CATEGORIES, "USER_INPUT"));
    }

}
