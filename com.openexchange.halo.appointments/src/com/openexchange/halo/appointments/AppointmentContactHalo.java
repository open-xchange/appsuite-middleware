package com.openexchange.halo.appointments;

import java.util.Arrays;
import java.util.Date;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

public class AppointmentContactHalo implements HaloContactDataSource {

	private ServiceLookup services;

	public AppointmentContactHalo(
			ServiceLookup services) {
		this.services = services;
	}

	@Override
	public String getId() {
		return "calendar";
	}

	@Override
	public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req,
			ServerSession session) throws OXException {
		AppointmentSQLInterface appointmentService = getAppointmentService(session);
		//TODO: Construct a list of appointments with the given user and the session user in the near future
		CalendarDataObject cdo1 = new CalendarDataObject();
		cdo1.setTitle("An Appointment");
		cdo1.setStartDate(new Date());
		cdo1.setEndDate(new Date());
		
		CalendarDataObject cdo2 = new CalendarDataObject();
		cdo2.setTitle("Another Appointment");
		cdo2.setStartDate(new Date());
		cdo2.setEndDate(new Date());
		
		return new AJAXRequestResult(Arrays.asList(cdo1, cdo2), "appointment");
	}
	
	public AppointmentSQLInterface getAppointmentService(ServerSession session) {
		AppointmentSqlFactoryService factoryService = services.getService(AppointmentSqlFactoryService.class);
		return factoryService.createAppointmentSql(session);
	}

}
