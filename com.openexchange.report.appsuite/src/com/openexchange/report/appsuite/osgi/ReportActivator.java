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

package com.openexchange.report.appsuite.osgi;

import javax.management.ObjectName;
import org.osgi.framework.ServiceReference;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.logging.LogLevelService;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.report.InfostoreInformationService;
import com.openexchange.report.LoginCounterService;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.ReportSystemHandler;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.report.appsuite.defaultHandlers.CapabilityHandler;
import com.openexchange.report.appsuite.defaultHandlers.ClientLoginCount;
import com.openexchange.report.appsuite.defaultHandlers.Total;
import com.openexchange.report.appsuite.internal.LocalReportService;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.management.ReportMXBeanImpl;
import com.openexchange.user.UserService;

/**
 * The {@link ReportActivator} is the interface to the OSGi world between the report framework and the runtime system.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReportActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ContextService.class, UserService.class, CapabilityService.class, ManagementService.class, LoginCounterService.class, ConfigurationService.class, DatabaseService.class, InfostoreInformationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServices(this);

        trackService(HazelcastInstance.class);

        // ContextReportCumulator
        track(ContextReportCumulator.class, new SimpleRegistryListener<ContextReportCumulator>() {

            @Override
            public void added(ServiceReference<ContextReportCumulator> ref, ContextReportCumulator service) {
                Services.add(service);
            }

            @Override
            public void removed(ServiceReference<ContextReportCumulator> ref, ContextReportCumulator service) {
                Services.remove(service);
            }
        });

        // ReportContextHandler
        track(ReportContextHandler.class, new SimpleRegistryListener<ReportContextHandler>() {

            @Override
            public void added(ServiceReference<ReportContextHandler> ref, ReportContextHandler service) {
                Services.add(service);
            }

            @Override
            public void removed(ServiceReference<ReportContextHandler> ref, ReportContextHandler service) {
                Services.remove(service);
            }

        });

        // ReportUserHandler
        track(ReportUserHandler.class, new SimpleRegistryListener<ReportUserHandler>() {

            @Override
            public void added(ServiceReference<ReportUserHandler> ref, ReportUserHandler service) {
                Services.add(service);
            }

            @Override
            public void removed(ServiceReference<ReportUserHandler> ref, ReportUserHandler service) {
                Services.remove(service);
            }

        });

        // UserReportCumulator

        track(UserReportCumulator.class, new SimpleRegistryListener<UserReportCumulator>() {

            @Override
            public void added(ServiceReference<UserReportCumulator> ref, UserReportCumulator service) {
                Services.add(service);
            }

            @Override
            public void removed(ServiceReference<UserReportCumulator> ref, UserReportCumulator service) {
                Services.remove(service);
            }

        });

        // ReportSystemHandler

        track(ReportSystemHandler.class, new SimpleRegistryListener<ReportSystemHandler>() {

            @Override
            public void added(ServiceReference<ReportSystemHandler> ref, ReportSystemHandler service) {
                Services.add(service);
            }

            @Override
            public void removed(ServiceReference<ReportSystemHandler> ref, ReportSystemHandler service) {
                Services.remove(service);
            }

        });

        // ReportFinishingTouches

        track(ReportFinishingTouches.class, new SimpleRegistryListener<ReportFinishingTouches>() {

            @Override
            public void added(ServiceReference<ReportFinishingTouches> ref, ReportFinishingTouches service) {
                Services.add(service);
            }

            @Override
            public void removed(ServiceReference<ReportFinishingTouches> ref, ReportFinishingTouches service) {
                Services.remove(service);
            }

        });

        // Register the implementations for the default report
        CapabilityHandler capabilityHandler = new CapabilityHandler();

        Services.add((ReportUserHandler) capabilityHandler);
        Services.add((ReportContextHandler) capabilityHandler);
        Services.add((UserReportCumulator) capabilityHandler);
        Services.add((ContextReportCumulator) capabilityHandler);
        Services.add((ReportFinishingTouches) capabilityHandler);

        Total total = new Total();
        Services.add(total);

        ClientLoginCount clc = new ClientLoginCount();
        Services.add(clc);

        registerService(ReportService.class, new LocalReportService());
        trackService(ReportService.class);

        trackService(LogLevelService.class);

        openTrackers();

        // Register the MBean
        ManagementService managementService = getService(ManagementService.class);
        managementService.registerMBean(new ObjectName("com.openexchange.reporting.appsuite", "name", "AppSuiteReporting"), new ReportMXBeanImpl());
    }

    protected void stopBundle() throws Exception {
        Services.setServices(null);

        super.stopBundle();
    }

}
