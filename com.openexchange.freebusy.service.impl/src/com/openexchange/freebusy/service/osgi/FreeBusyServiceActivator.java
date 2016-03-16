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

package com.openexchange.freebusy.service.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.context.ContextService;
import com.openexchange.freebusy.provider.FreeBusyProvider;
import com.openexchange.freebusy.provider.InternalFreeBusyProvider;
import com.openexchange.freebusy.service.FreeBusyService;
import com.openexchange.freebusy.service.impl.FreeBusyProviderRegistry;
import com.openexchange.freebusy.service.impl.FreeBusyServiceImpl;
import com.openexchange.freebusy.service.impl.FreeBusyServiceLookup;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link FreeBusyServiceActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyServiceActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FreeBusyServiceActivator.class);

    /**
     * Initializes a new {@link FreeBusyServiceActivator}.
     */
    public FreeBusyServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class, UserConfigurationService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: com.openexchange.freebusy");
            FreeBusyServiceLookup.set(this);
            /*
             * track providers
             */
            final FreeBusyProviderRegistry registry = new FreeBusyProviderRegistry();
            super.track(InternalFreeBusyProvider.class, new SimpleRegistryListener<InternalFreeBusyProvider>() {

                @Override
                public void added(ServiceReference<InternalFreeBusyProvider> ref, InternalFreeBusyProvider service) {
                    registry.add(service);
                }

                @Override
                public void removed(ServiceReference<InternalFreeBusyProvider> ref, InternalFreeBusyProvider service) {
                    registry.remove(service);
                }
            });
            super.track(FreeBusyProvider.class, new SimpleRegistryListener<FreeBusyProvider>() {

                @Override
                public void added(ServiceReference<FreeBusyProvider> ref, FreeBusyProvider service) {
                    registry.add(service);
                }

                @Override
                public void removed(ServiceReference<FreeBusyProvider> ref, FreeBusyProvider service) {
                    registry.remove(service);
                }
            });
            super.openTrackers();
            /*
             * register services
             */
            FreeBusyService freeBusyService = new FreeBusyServiceImpl(registry);
            super.registerService(FreeBusyService.class, freeBusyService);
        } catch (Exception e) {
            LOG.error("error starting com.openexchange.freebusy", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.freebusy");
        FreeBusyServiceLookup.set(null);
        super.stopBundle();
    }

}
