package com.openexchange.event.impl;

import com.openexchange.groupware.container.Appointment;
import com.openexchange.session.Session;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface AppointmentEventInterface2 extends AppointmentEventInterface {

    public void appointmentModified(Appointment oldAppointment, Appointment newAppointment, Session sessionObj);

}
