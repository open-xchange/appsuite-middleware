
package com.openexchange.ajax.reminder.actions;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.ReminderFields;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.reminder.ReminderObject;

public class DeleteRequest extends AbstractReminderRequest<CommonDeleteResponse> {

    private final ReminderObject reminder;
    private final ReminderObject[] reminders;
    private final boolean failOnError;

    public DeleteRequest(ReminderObject reminder, boolean failOnError) {
        super();
        this.reminder = reminder;
        this.reminders = null;
        this.failOnError = failOnError;
    }

    public DeleteRequest(ReminderObject[] reminders, boolean failOnError) {
        super();
        this.reminder = null;
        this.reminders = reminders;
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        if (reminders == null) {
            JSONObject json = new JSONObject();
            json.put(CalendarFields.RECURRENCE_POSITION, reminder.getRecurrencePosition());
            json.put(ReminderFields.ID, reminder.getObjectId());
            return json;
        } else {
            JSONArray jsonArray = new JSONArray();
            for (ReminderObject reminder : reminders) {
                JSONObject json = new JSONObject();
                json.put(CalendarFields.RECURRENCE_POSITION, reminder.getRecurrencePosition());
                json.put(ReminderFields.ID, reminder.getObjectId());
                jsonArray.put(json);
            }
            return jsonArray;
        }
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        if (reminders == null) {
            return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(reminder.getLastModified().getTime()))
            };
        } else {
            return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(reminders[0].getLastModified().getTime()))
            };
        }
    }

    @Override
    public DeleteParser getParser() {
        return new DeleteParser(failOnError);
    }

}
