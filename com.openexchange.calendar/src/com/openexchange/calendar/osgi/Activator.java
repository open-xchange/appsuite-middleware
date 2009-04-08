package com.openexchange.calendar.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.calendar.CalendarAdministration;
import com.openexchange.calendar.CalendarReminderDelete;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarAdministrationService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.reminder.ReminderDeleteInterface;

public class Activator implements BundleActivator {
    
    private ServiceRegistration appointmentSqlFactoryRegistration;
    private ServiceRegistration calendarCollectionRegistration;
    private ServiceRegistration calendarAdministrationRegistration;
    private ServiceRegistration calendarReminderDeleteRegistration;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
	    appointmentSqlFactoryRegistration = context.registerService(AppointmentSqlFactoryService.class.getName(), new AppointmentSqlFactory(), null);
	    calendarCollectionRegistration = context.registerService(CalendarCollectionService.class.getName(), new CalendarCollection(), null);
	    calendarAdministrationRegistration = context.registerService(CalendarAdministrationService.class.getName(), new CalendarAdministration(), null);
	    calendarReminderDeleteRegistration = context.registerService(ReminderDeleteInterface.class.getName(), new CalendarReminderDelete(), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	    appointmentSqlFactoryRegistration.unregister();
	    calendarCollectionRegistration.unregister();
	    calendarAdministrationRegistration.unregister();
	    calendarReminderDeleteRegistration.unregister();
	}

}
