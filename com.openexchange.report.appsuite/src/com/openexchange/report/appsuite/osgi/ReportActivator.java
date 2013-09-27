
package com.openexchange.report.appsuite.osgi;

import javax.management.ObjectName;
import org.osgi.framework.ServiceReference;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.context.ContextService;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.report.LoginCounterService;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.ReportSystemHandler;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.Services;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.report.appsuite.defaultHandlers.CapabilityHandler;
import com.openexchange.report.appsuite.defaultHandlers.ClientLoginCount;
import com.openexchange.report.appsuite.defaultHandlers.Total;
import com.openexchange.report.appsuite.jobs.Orchestration;
import com.openexchange.report.appsuite.management.ReportMXBeanImpl;
import com.openexchange.tools.strings.StringParser;
import com.openexchange.user.UserService;

/**
 * 
 * The {@link ReportActivator} is the interface to the OSGi world between the report framework and the runtime system. 
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReportActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            ContextService.class, UserService.class, HazelcastInstance.class, StringParser.class,
            CapabilityService.class, ManagementService.class, LoginCounterService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServices(this);

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
        
        openTrackers();

        registerService(ReportService.class, Orchestration.getInstance());
        
        // Register the MBean
        ManagementService managementService = getService(ManagementService.class);
        managementService.registerMBean(
            new ObjectName("com.openexchange.reporting.appsuite", "name", "AppSuiteReporting"),
            new ReportMXBeanImpl());

    }

}
