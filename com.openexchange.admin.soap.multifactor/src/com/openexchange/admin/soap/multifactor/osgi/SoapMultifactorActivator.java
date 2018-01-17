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

package com.openexchange.admin.soap.multifactor.osgi;

import java.rmi.Remote;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.admin.soap.multifactor.soap.OXMultifactorServicePortType;
import com.openexchange.admin.soap.multifactor.soap.OXMultifactorServicePortTypeImpl;
import com.openexchange.multifactor.rmi.MultifactorManagementRemoteService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link SoapMultifactorActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class SoapMultifactorActivator extends HousekeepingActivator {

    private static final Logger  logger = org.slf4j.LoggerFactory.getLogger(SoapMultifactorActivator.class);

    /* (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#getNeededServices()
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    /* (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#startBundle()
     */
    @Override
    protected void startBundle() throws Exception {
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());

        final BundleContext context = this.context;
        final ServiceTrackerCustomizer<Remote, Remote> trackerCustomizer = new ServiceTrackerCustomizer<Remote, Remote>() {

            @Override
            public void removedService(final ServiceReference<Remote> reference, final Remote service) {
                if (service instanceof MultifactorManagementRemoteService) {
                    OXMultifactorServicePortTypeImpl.RMI_REFERENCE.set(null);
                    context.ungetService(reference);
                }
            }

            @Override
            public void modifiedService(final ServiceReference<Remote> reference, final Remote service) {
                // Ignore
            }

            @Override
            public Remote addingService(final ServiceReference<Remote> reference) {
                final Remote service = context.getService(reference);
                if (service instanceof MultifactorManagementRemoteService) {
                    OXMultifactorServicePortTypeImpl.RMI_REFERENCE.set((MultifactorManagementRemoteService) service);
                    return service;
                }
                context.ungetService(reference);
                return null;
            }
        };
        track(Remote.class, trackerCustomizer);
        openTrackers();

        OXMultifactorServicePortTypeImpl soapService = new OXMultifactorServicePortTypeImpl();
        registerService(OXMultifactorServicePortType.class, soapService);

    }

    /* (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        unregisterService(OXMultifactorServicePortType.class);
        super.stop(context);
    }
}
