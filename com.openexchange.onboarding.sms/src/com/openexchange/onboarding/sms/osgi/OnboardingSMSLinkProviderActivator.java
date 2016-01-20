package com.openexchange.onboarding.sms.osgi;

import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.onboarding.sms.SMSLinkProvider;
import com.openexchange.onboarding.sms.internal.SMSLinkProviderImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

public class OnboardingSMSLinkProviderActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DispatcherPrefixService.class, UserService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(SMSLinkProvider.class, new SMSLinkProviderImpl(this));
    }


}
