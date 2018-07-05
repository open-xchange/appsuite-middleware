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

package com.openexchange.chronos.provider.composition.impl;

import static com.openexchange.chronos.provider.CalendarProviders.getCapabilityName;
import static com.openexchange.chronos.provider.CalendarProviders.getEnabledPropertyName;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CalendarProviderTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarProviderTracker extends RankingAwareNearRegistryServiceTracker<CalendarProvider> {

    private final ConcurrentMap<String, ServiceRegistration<CapabilityChecker>> checkerRegistrations;
    final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarProviderTracker}.
     *
     * @param calendarProviders The bundle context
     * @param services A service lookup reference
     */
    public CalendarProviderTracker(BundleContext context, ServiceLookup services) {
        super(context, CalendarProvider.class);
        this.checkerRegistrations = new ConcurrentHashMap<String, ServiceRegistration<CapabilityChecker>>();
        this.services = services;
    }

    @Override
    protected void onServiceAdded(CalendarProvider provider) {
        /*
         * declare capability for calendar provider
         */
        String capabilityName = getCapabilityName(provider);
        services.getService(CapabilityService.class).declareCapability(capabilityName);
        /*
         * register an appropriate capability checker
         */
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(CapabilityChecker.PROPERTY_CAPABILITIES, capabilityName);
        ServiceRegistration<CapabilityChecker> checkerRegistration = context.registerService(CapabilityChecker.class, new CapabilityChecker() {

            @Override
            public boolean isEnabled(String capability, Session session) throws OXException {
                ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                if (serverSession.isAnonymous()) {
                    return false;
                }
                if (false == serverSession.getUserPermissionBits().hasCalendar()) {
                    return false;
                }
                ConfigView configView = requireService(ConfigViewFactory.class, services).getView(session.getUserId(), session.getContextId());
                return ConfigViews.getDefinedBoolPropertyFrom(getEnabledPropertyName(provider), true, configView) && provider.isAvailable(session);
            }
        }, serviceProperties);
        if (null != checkerRegistrations.putIfAbsent(provider.getId(), checkerRegistration)) {
            checkerRegistration.unregister();
        }
    }

    @Override
    protected void onServiceRemoved(CalendarProvider provider) {
        /*
         * unregister capability checker for calendar provider
         */
        ServiceRegistration<CapabilityChecker> checkerRegistration = checkerRegistrations.remove(provider.getId());
        if (null != checkerRegistration) {
            checkerRegistration.unregister();
        }
        /*
         * undeclare capability for calendar provider
         */
        services.getService(CapabilityService.class).undeclareCapability(getCapabilityName(provider));
    }

}
