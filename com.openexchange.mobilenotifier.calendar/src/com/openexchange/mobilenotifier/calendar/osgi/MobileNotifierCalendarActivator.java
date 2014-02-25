package com.openexchange.mobilenotifier.calendar.osgi;

import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.mobilenotifier.MobileNotifierService;
import com.openexchange.mobilenotifier.calendar.MobileNotifierCalendarImpl;
import com.openexchange.osgi.HousekeepingActivator;

public class MobileNotifierCalendarActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { AppointmentSqlFactoryService.class, CalendarCollectionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(MobileNotifierService.class, new MobileNotifierCalendarImpl(this));
    }

}
