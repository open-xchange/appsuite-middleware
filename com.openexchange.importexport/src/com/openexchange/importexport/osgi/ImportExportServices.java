package com.openexchange.importexport.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.server.ServiceLookup;

public class ImportExportServices {
    public static ServiceLookup LOOKUP = null;
    
    public static ContactInterfaceDiscoveryService getContactInterfaceDiscoveryService(){
    	return LOOKUP.getService(ContactInterfaceDiscoveryService.class);
    }

	public static FolderUpdaterRegistry getUpdaterRegistry() {
		return LOOKUP.getService(FolderUpdaterRegistry.class);
	}

	public static ICalParser getIcalParser() {
		return LOOKUP.getService(ICalParser.class);
	}

	public static AppointmentSqlFactoryService getAppointmentFactoryService() {
		return LOOKUP.getService(AppointmentSqlFactoryService.class);
	}

	public static CalendarCollectionService getCalendarCollectionService() {
		return LOOKUP.getService(CalendarCollectionService.class);
	}

	public static ConfigurationService getConfigurationService() {
		return LOOKUP.getService(ConfigurationService.class);
	}

	public static ICalEmitter getICalEmitter() {
		return LOOKUP.getService(ICalEmitter.class);
	}
}
