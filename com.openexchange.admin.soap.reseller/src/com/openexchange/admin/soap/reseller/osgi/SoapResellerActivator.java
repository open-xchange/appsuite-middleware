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

package com.openexchange.admin.soap.reseller.osgi;

import java.rmi.Remote;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.soap.reseller.context.reseller.soap.OXResellerContextServicePortType;
import com.openexchange.admin.soap.reseller.context.reseller.soap.OXResellerContextServicePortTypeImpl;
import com.openexchange.admin.soap.reseller.group.reseller.soap.OXResellerGroupServicePortType;
import com.openexchange.admin.soap.reseller.group.reseller.soap.OXResellerGroupServicePortTypeImpl;
import com.openexchange.admin.soap.reseller.resource.reseller.soap.OXResellerResourceServicePortType;
import com.openexchange.admin.soap.reseller.resource.reseller.soap.OXResellerResourceServicePortTypeImpl;
import com.openexchange.admin.soap.reseller.service.reseller.soap.OXResellerServicePortType;
import com.openexchange.admin.soap.reseller.service.reseller.soap.OXResellerServicePortTypeImpl;
import com.openexchange.admin.soap.reseller.user.reseller.soap.OXResellerUserServicePortType;
import com.openexchange.admin.soap.reseller.user.reseller.soap.OXResellerUserServicePortTypeImpl;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link SoapResellerActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SoapResellerActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SoapResellerActivator}.
     */
    public SoapResellerActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        final ServiceTrackerCustomizer<Remote, Remote> trackerCustomizer = new ServiceTrackerCustomizer<Remote, Remote>() {

            @Override
            public void removedService(final ServiceReference<Remote> reference, final Remote service) {
                if (service instanceof OXResellerInterface) {
                    OXResellerServicePortTypeImpl.RMI_REFERENCE.set(null);
                    context.ungetService(reference);
                }
                if (service instanceof OXUserInterface) {
                    OXResellerUserServicePortTypeImpl.RMI_REFERENCE.set(null);
                    context.ungetService(reference);
                }
                if (service instanceof OXContextInterface) {
                    OXResellerContextServicePortTypeImpl.RMI_REFERENCE.set(null);
                    context.ungetService(reference);
                }
                if (service instanceof OXGroupInterface) {
                    OXResellerGroupServicePortTypeImpl.RMI_REFERENCE.set(null);
                    context.ungetService(reference);
                }
                if (service instanceof OXResourceInterface) {
                    OXResellerResourceServicePortTypeImpl.RMI_REFERENCE.set(null);
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
                if (service instanceof OXResellerInterface) {
                    OXResellerServicePortTypeImpl.RMI_REFERENCE.set((OXResellerInterface) service);
                    return service;
                }
                if (service instanceof OXUserInterface) {
                    OXResellerUserServicePortTypeImpl.RMI_REFERENCE.set((OXUserInterface) service);
                    return service;
                }
                if (service instanceof OXContextInterface) {
                    OXResellerContextServicePortTypeImpl.RMI_REFERENCE.set((OXContextInterface) service);
                    return service;
                }
                if (service instanceof OXGroupInterface) {
                    OXResellerGroupServicePortTypeImpl.RMI_REFERENCE.set((OXGroupInterface) service);
                    return service;
                }
                if (service instanceof OXResourceInterface) {
                    OXResellerResourceServicePortTypeImpl.RMI_REFERENCE.set((OXResourceInterface) service);
                    return service;
                }
                context.ungetService(reference);
                return null;
            }
        };
        track(Remote.class, trackerCustomizer);
        openTrackers();

        {
            final OXResellerServicePortTypeImpl soapService = new OXResellerServicePortTypeImpl();
            registerService(OXResellerServicePortType.class, soapService);
        }
        {
            final OXResellerUserServicePortTypeImpl soapService = new OXResellerUserServicePortTypeImpl();
            registerService(OXResellerUserServicePortType.class, soapService);
        }
        {
            final OXResellerResourceServicePortTypeImpl soapService = new OXResellerResourceServicePortTypeImpl();
            registerService(OXResellerResourceServicePortType.class, soapService);
        }
        {
            final OXResellerGroupServicePortTypeImpl soapService = new OXResellerGroupServicePortTypeImpl();
            registerService(OXResellerGroupServicePortType.class, soapService);
        }
        {
            final OXResellerContextServicePortTypeImpl soapService = new OXResellerContextServicePortTypeImpl();
            registerService(OXResellerContextServicePortType.class, soapService);
        }
    }

}
