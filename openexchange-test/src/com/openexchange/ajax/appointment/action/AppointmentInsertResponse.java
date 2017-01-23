
package com.openexchange.ajax.appointment.action;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;

public class AppointmentInsertResponse extends CommonInsertResponse {

    /**
     * @param response
     */
    AppointmentInsertResponse(final Response response) {
        super(response);
    }

    /**
     * Puts the data of this insert response into a task object. This are
     * especially the task identifier and the modified time stamp.
     */
    public void fillAppointment(final Appointment appointment) {
        fillObject(appointment);
    }
}
