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

package com.openexchange.http.grizzly.addon;

import java.util.ArrayList;
import java.util.List;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.OXHttpServerFilter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.http.grizzly.filter.FilterChainUtils;
import com.openexchange.http.grizzly.filter.backendroute.AppendBackendRouteFilter;
import com.openexchange.http.grizzly.osgi.Services;

/**
 * {@link GrizzlOXAddOn}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlOXAddOn implements AddOn {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GrizzlOXAddOn.class);

    private final List<Filter> filters = new ArrayList<Filter>();

    public GrizzlOXAddOn() {
        //1. BackendRouteFilter
        ConfigurationService configurationService = Services.optService(ConfigurationService.class);
        final String defaultRoute = "";
        final String backendRoute = null == configurationService ? defaultRoute : configurationService.getProperty("com.openexchange.http.grizzly.backendRoute", defaultRoute);
        AppendBackendRouteFilter appendBackendRouteFilter = new AppendBackendRouteFilter(backendRoute);
        filters.add(appendBackendRouteFilter);
    }

    @Override
    public void setup(NetworkListener networkListener, FilterChainBuilder builder) {
        AddOn[] addOns = networkListener.getAddOns();
        for (AddOn addOn : addOns) {
            LOG.info("Current Addon is: {}", addOn.getClass());
        }
        int httpServerFilterIdx = builder.indexOfType(OXHttpServerFilter.class);
        if (httpServerFilterIdx > 0) {
            builder.addAll(httpServerFilterIdx -1 , filters);
        }
        LOG.debug("FilterChain after adding Watchers:\n{}", FilterChainUtils.formatFilterChainString(builder.build()));
    }

}
