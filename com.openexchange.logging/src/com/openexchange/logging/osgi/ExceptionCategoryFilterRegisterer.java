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
@SuppressWarnings("unused")
public class ExceptionCategoryFilterRegisterer implements ServiceTrackerCustomizer<ConfigurationService, ConfigurationService>, Reloadable {

    private static final String PROP_SUPPRESSED_CATEGORIES = "com.openexchange.log.suppressedCategories";

    private final BundleContext context;
    private ExceptionCategoryFilter exceptionCategoryFilter = null; // Guarded by synchronized
    private final RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private final IncludeStackTraceService traceService;

    /**
     * Initializes a new {@link ExceptionCategoryFilterRegisterer}.
     *
     * @param context The bundle context
     * @param rankingAwareTurboFilterList The list of logback turbo filters
     * @param traceService The "include stack trace" service
     */
    public ExceptionCategoryFilterRegisterer(final BundleContext context, final RankingAwareTurboFilterList rankingAwareTurboFilterList, final IncludeStackTraceService traceService) {
        super();
        this.context = context;
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        this.traceService = traceService;
    }

    @Override
    public synchronized ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
        ConfigurationService service = context.getService(reference);
        String suppressedCategories = service.getProperty(PROP_SUPPRESSED_CATEGORIES, "USER_INPUT");
        ExceptionCategoryFilter.setCategories(suppressedCategories);

        ExceptionCategoryFilter exceptionCategoryFilter = this.exceptionCategoryFilter;
        if (exceptionCategoryFilter == null) {
            exceptionCategoryFilter = new ExceptionCategoryFilter(traceService);
            rankingAwareTurboFilterList.addTurboFilter(exceptionCategoryFilter);
            this.exceptionCategoryFilter = exceptionCategoryFilter;
        }

        return service;
    }

    @Override
    public void modifiedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        // Nothing to do
    }

    @Override
    public synchronized void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        ExceptionCategoryFilter exceptionCategoryFilter = this.exceptionCategoryFilter;
        if (exceptionCategoryFilter != null) {
            rankingAwareTurboFilterList.removeTurboFilter(exceptionCategoryFilter);
            this.exceptionCategoryFilter = null;
        }
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
