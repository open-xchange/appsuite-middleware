package com.openexchange.chronos.provider.google.osgi;

import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.google.GoogleCalendarProvider;
import com.openexchange.chronos.provider.google.migration.GoogleSubscriptionsMigrationTask;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { OAuthService.class, CalendarAccountService.class, AdministrativeCalendarAccountService.class, LeanConfigurationService.class, RecurrenceService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { GenericConfigurationStorageService.class, CalendarStorageFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        openTrackers();
        Services.setServiceLookup(this);
        registerService(CalendarProvider.class, new GoogleCalendarProvider());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new GoogleSubscriptionsMigrationTask()));
    }

}
