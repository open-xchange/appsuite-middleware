package com.openexchange.calendar.itip;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.api.CalendarFeature;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

public class ITipFeature implements CalendarFeature {

	private static final String ITIP = "itip";

	private final ServiceLookup services;
	
	
	public ITipFeature(ServiceLookup services) {
		super();
		this.services = services;
	}



	@Override
    public String getId() {
		return ITIP;
	}

	
	
	@Override
    public AppointmentSQLInterface wrap(AppointmentSQLInterface delegate,
			Session session) throws OXException {
		
		return new ITipConsistencyCalendar(delegate, session, services);
	}

}
