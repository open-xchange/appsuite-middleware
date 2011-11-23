package com.openexchange.ajax.reminder.actions;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.ReminderFields;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.reminder.ReminderObject;


public class DeleteRequest extends AbstractReminderRequest<CommonDeleteResponse> {
    private final ReminderObject reminder;
    private final boolean failOnError;

    public DeleteRequest(ReminderObject reminder, boolean failOnError) {
        super();
        this.reminder = reminder;
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject json = new JSONObject();
        json.put(CalendarFields.RECURRENCE_POSITION, reminder.getRecurrencePosition());
        json.put(ReminderFields.ID, reminder.getObjectId());

        return json;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
                .ACTION_DELETE),
            new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(reminder.getLastModified().getTime()))
        };
    }

    @Override
    public DeleteParser getParser() {
        return new DeleteParser(failOnError);
    }

}
