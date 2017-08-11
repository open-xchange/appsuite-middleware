
package com.openexchange.chronos.alarm.osgi;

import com.openexchange.chronos.alarm.AlarmTriggerService;
import com.openexchange.chronos.alarm.impl.AlarmTriggerServiceImpl;
import com.openexchange.chronos.alarm.storage.AlarmTriggerStorage;
import com.openexchange.chronos.alarm.storage.impl.AlarmTriggerStorageImpl;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.database.DatabaseService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class, RecurrenceService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        AlarmTriggerStorageImpl alarmTriggerStorageImpl = new AlarmTriggerStorageImpl(this);
        registerService(AlarmTriggerStorage.class, alarmTriggerStorageImpl);
        registerService(AlarmTriggerService.class, new AlarmTriggerServiceImpl(alarmTriggerStorageImpl, getService(RecurrenceService.class)));
    }

}
