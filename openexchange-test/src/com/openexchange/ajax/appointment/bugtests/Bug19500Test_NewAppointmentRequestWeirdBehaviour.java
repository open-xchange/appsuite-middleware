package com.openexchange.ajax.appointment.bugtests;

import java.util.Date;
import java.util.List;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.groupware.container.Appointment;

public class Bug19500Test_NewAppointmentRequestWeirdBehaviour extends ManagedAppointmentTest{

	public Bug19500Test_NewAppointmentRequestWeirdBehaviour(String name) {
		super(name);
	}

	public void testNewappointmentsRequestConsistency(){
		Date start = D("yesterday");
		Date end = D("tomorrow");
		int numOccurences = 3;

		Appointment series = generateDailyAppointment();
		series.setOccurrence(numOccurences);
		series.setTitle("Bug 19500 Series");
		series.setStartDate(start);
		calendarManager.insert(series);



		List<Appointment> list1 = calendarManager.newappointments(start, end, 5, Appointment.ALL_COLUMNS);
		List<Appointment> list2 = calendarManager.newappointments(start, end, 5, Appointment.ALL_COLUMNS);

		assertEquals("Expected correct length", numOccurences, list2.size());
		assertEquals("Expected same length", list1.size(), list2.size());
		for(int i = 0, limit = list1.size(); i < limit; i++){
			assertEquals("Different element #"+i, list1.get(i), list2.get(i));
		}
	}

}
