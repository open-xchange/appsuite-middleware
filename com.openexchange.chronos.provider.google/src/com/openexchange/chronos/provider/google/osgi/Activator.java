package com.openexchange.chronos.provider.google.osgi;

import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.google.GoogleCalendarProvider;
import com.openexchange.oauth.OAuthService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { OAuthService.class, CalendarAccountService.class, AdministrativeCalendarAccountService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        registerService(CalendarProvider.class, new GoogleCalendarProvider());
    }

}
