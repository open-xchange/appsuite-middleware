package com.openexchange.chronos.alarm.osgi;

import com.openexchange.chronos.alarm.AlarmTriggerServiceInterceptor;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.storage.CalendarAccountStorageFactory;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserServiceInterceptor;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{CalendarAccountStorageFactory.class, CalendarStorageFactory.class, CalendarUtilities.class, DBProvider.class};
    }


    @Override
    protected void startBundle() throws Exception {
        registerService(UserServiceInterceptor.class, new AlarmTriggerServiceInterceptor(this));
    }


}
