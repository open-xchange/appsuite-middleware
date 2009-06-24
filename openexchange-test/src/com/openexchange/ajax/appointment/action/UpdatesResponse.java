package com.openexchange.ajax.appointment.action;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.Appointment;

/**
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
public class UpdatesResponse extends AbstractAJAXResponse {

    private int[] columns;

    protected UpdatesResponse(Response response, int[] columns) {
        super(response);
        this.columns = columns;
    }
    
    public List<Appointment> getAppointments(final TimeZone timeZone) throws OXConflictException, JSONException {
        //TODO extract functionality from test...
        Appointment[] objects = AppointmentTest.jsonArray2AppointmentArray((JSONArray)getData(), columns, timeZone);
        return Arrays.asList(objects);
    }

}
