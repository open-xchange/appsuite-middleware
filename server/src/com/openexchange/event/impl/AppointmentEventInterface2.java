package com.openexchange.event.impl;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.session.Session;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface AppointmentEventInterface2 extends AppointmentEventInterface {

    public void appointmentModified(AppointmentObject oldAppointment, AppointmentObject newAppointment, Session sessionObj);

}
