
package com.openexchange.chronos.alarm.osgi;

import com.openexchange.chronos.alarm.AlarmCalendarHandler;
import com.openexchange.chronos.alarm.storage.AlarmTriggerStorage;
import com.openexchange.chronos.alarm.storage.impl.AlarmTriggerStorageImpl;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.database.DatabaseService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        AlarmTriggerStorageImpl alarmTriggerStorageImpl = new AlarmTriggerStorageImpl(this);
        registerService(AlarmTriggerStorage.class, alarmTriggerStorageImpl);
        registerService(CalendarHandler.class, new AlarmCalendarHandler(alarmTriggerStorageImpl));
    }

}
