package com.openexchange.mobilenotifier.reminder.osgi;

import com.openexchange.mobilenotifier.MobileNotifierService;
import com.openexchange.mobilenotifier.reminder.MobileNotifierReminderImpl;
import com.openexchange.osgi.HousekeepingActivator;

public class MobileNotifierReminderActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(MobileNotifierService.class, new MobileNotifierReminderImpl(this));
    }

}
