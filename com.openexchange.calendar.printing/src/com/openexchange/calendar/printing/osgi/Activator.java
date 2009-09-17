package com.openexchange.calendar.printing.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.calendar.printing.CalendarPrintingServlet;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;

public class Activator extends DeferredActivator implements BundleActivator {

    private static final String ALIAS = "/ajax/printCalendar";
    private static Class[] services = new Class[]{TemplateService.class, AppointmentSqlFactoryService.class, CalendarCollectionService.class};
    private HTTPServletRegistration registration;

    @Override
    protected Class<?>[] getNeededServices() {
        return services;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        register();
    }


    @Override
    protected void handleUnavailability(Class<?> clazz) {
        unregister();
    }
    
    @Override
    protected void startBundle() throws Exception {
        register();
    }
    
    @Override
    protected void stopBundle() throws Exception {
        unregister();
    }

    private void register() {
        TemplateService templates = getService(TemplateService.class);
        AppointmentSqlFactoryService appointmentSqlFactory = getService(AppointmentSqlFactoryService.class);
        CalendarCollectionService collectionService = getService(CalendarCollectionService.class);
        
        if(templates == null || appointmentSqlFactory == null || collectionService == null) {
            unregister();
            return;
        }
        
        CalendarPrintingServlet.setTemplateService(templates);
        CalendarPrintingServlet.setAppointmentSqlFactoryService(appointmentSqlFactory);
        CalendarPrintingServlet.setCalendarTools(collectionService);
        
        registration = new HTTPServletRegistration(context, ALIAS, new CalendarPrintingServlet());
        
    }

    private void unregister() {
        if(registration != null) {
            registration.unregister();
            registration = null;
        }
    }

}
