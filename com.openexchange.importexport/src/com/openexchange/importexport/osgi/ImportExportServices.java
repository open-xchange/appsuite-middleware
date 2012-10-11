package com.openexchange.importexport.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.server.ServiceLookup;

public class ImportExportServices {

    public static final AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<ServiceLookup>();

    public static ContactService getContactService() {
        return LOOKUP.get().getService(ContactService.class);
    }

    public static FolderUpdaterRegistry getUpdaterRegistry() {
        return LOOKUP.get().getService(FolderUpdaterRegistry.class);
    }

	public static ICalParser getIcalParser() {
		return LOOKUP.get().getService(ICalParser.class);
	}

	public static AppointmentSqlFactoryService getAppointmentFactoryService() {
		return LOOKUP.get().getService(AppointmentSqlFactoryService.class);
	}

	public static CalendarCollectionService getCalendarCollectionService() {
		return LOOKUP.get().getService(CalendarCollectionService.class);
	}

	public static ConfigurationService getConfigurationService() {
		return LOOKUP.get().getService(ConfigurationService.class);
	}

	public static ICalEmitter getICalEmitter() {
		return LOOKUP.get().getService(ICalEmitter.class);
	}
}
