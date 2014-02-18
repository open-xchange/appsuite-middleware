package com.openexchange.mobilenotifier.mail.osgi;

import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mobilenotifier.MobileNotifierService;
import com.openexchange.mobilenotifier.mail.MobileNotifierMailImpl;
import com.openexchange.osgi.HousekeepingActivator;

public class MobileNotifierMailActivator extends HousekeepingActivator {
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { MailService.class, MailAccountStorageService.class, UnifiedInboxManagement.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(MobileNotifierService.class, new MobileNotifierMailImpl(this));
    }
}
