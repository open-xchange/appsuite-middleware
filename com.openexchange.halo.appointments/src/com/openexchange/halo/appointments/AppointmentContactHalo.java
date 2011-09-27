package com.openexchange.halo.appointments;

import java.util.Date;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.search.Order;
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
		return "com.openexchange.halo.appointments";
	}

	@Override
	public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req,
			ServerSession session) throws OXException {
		AppointmentSQLInterface appointmentService = getAppointmentService(session);
		
		int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
		String parameterStart = req.checkParameter(AJAXServlet.PARAMETER_START);
		Date start = new Date(Long.parseLong(parameterStart));
        String parameterEnd = req.checkParameter(AJAXServlet.PARAMETER_END);
        Date end = new Date(Long.parseLong(parameterEnd));
        String parameterSort = req.getParameter(AJAXServlet.PARAMETER_SORT);
        int orderBy = parameterSort == null ? 0 : Integer.parseInt(parameterSort);
        String parameterOrder = req.getParameter(AJAXServlet.PARAMETER_ORDER);
        Order order = OrderFields.parse(parameterOrder);
        
		List<Appointment> appointments = null;
		if (query.getUser() != null) {
		    appointments = appointmentService.getAppointmentsWithUserBetween(query.getUser(), columns, start, end, orderBy, order);
		} else {
		    appointments = appointmentService.getAppointmentsWithExternalParticipantBetween(query.getContact().getEmail1(), columns, start, end, orderBy, order);
		}
		
//		//TODO: Construct a list of appointments with the given user and the session user in the near future
//		CalendarDataObject cdo1 = new CalendarDataObject();
//		cdo1.setTitle("An Appointment");
//		cdo1.setStartDate(new Date());
//		cdo1.setEndDate(new Date());
//		
//		CalendarDataObject cdo2 = new CalendarDataObject();
//		cdo2.setTitle("Another Appointment");
//		cdo2.setStartDate(new Date());
//		cdo2.setEndDate(new Date());
		
		return new AJAXRequestResult(appointments, "appointment");
	}
	
	public AppointmentSQLInterface getAppointmentService(ServerSession session) {
		AppointmentSqlFactoryService factoryService = services.getService(AppointmentSqlFactoryService.class);
		return factoryService.createAppointmentSql(session);
	}

	@Override
	public boolean isAvailable(ServerSession session) {
		return session.getUserConfiguration().hasCalendar();
	}

}
