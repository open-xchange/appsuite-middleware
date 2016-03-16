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
package com.openexchange.halo.xing.osgi;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactImageSource;
import com.openexchange.halo.xing.XingInvestigationResultConverter;
import com.openexchange.halo.xing.XingUserDataSource;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.xing.access.XingOAuthAccessProvider;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class XingHaloActivator extends HousekeepingActivator {

    private volatile ServiceRegistration<HaloContactDataSource> contactRegistration = null;

    private volatile ServiceRegistration<HaloContactImageSource> imageRegistration = null;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[0];
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(ResultConverter.class, new XingInvestigationResultConverter());
        track(XingOAuthAccessProvider.class, new SimpleRegistryListener<XingOAuthAccessProvider>() {
            @Override
            public void added(ServiceReference<XingOAuthAccessProvider> ref, XingOAuthAccessProvider service) {
                XingUserDataSource xingDataSource = new XingUserDataSource(service);
                contactRegistration = context.registerService(HaloContactDataSource.class, xingDataSource, null);
                imageRegistration = context.registerService(HaloContactImageSource.class, xingDataSource, null);
            }

            @Override
            public void removed(ServiceReference<XingOAuthAccessProvider> ref, XingOAuthAccessProvider service) {
                if (contactRegistration != null) {
                    contactRegistration.unregister();
                    contactRegistration = null;
                }

                if (imageRegistration != null) {
                    imageRegistration.unregister();
                    imageRegistration = null;
                }
            }
        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        if (contactRegistration != null) {
            contactRegistration.unregister();
            contactRegistration = null;
        }

        if (imageRegistration != null) {
            imageRegistration.unregister();
            imageRegistration = null;
        }
        super.stopBundle();
    }

}
