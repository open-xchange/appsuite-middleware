package com.openexchange.mobilenotifier.mail.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mobilenotifier.MobileNotifierService;
import com.openexchange.mobilenotifier.mail.MobileNotifierMailImpl;
import com.openexchange.osgi.HousekeepingActivator;

public class MobileNotifierMailActivator extends HousekeepingActivator {
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { MailService.class, MailAccountStorageService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(MobileNotifierService.class, new MobileNotifierMailImpl(this));
    }
}
