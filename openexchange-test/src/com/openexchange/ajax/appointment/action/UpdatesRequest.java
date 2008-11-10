package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
public class UpdatesRequest extends AbstractAppointmentRequest<UpdatesResponse> {

    private int folderId;
    private int[] columns;
    private Date timestamp;
    private boolean recurrenceMaster;

    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster) {
        this.folderId = folderId;
        this.columns = columns;
        this.timestamp = timestamp;
        this.recurrenceMaster = recurrenceMaster;
    }
    
    public Object getBody() throws JSONException {
        return null;
    }

    public Method getMethod() {
        return Method.GET;
    }

    public Parameter[] getParameters() {
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(folderId)));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, timestamp));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_IGNORE, "deleted"));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_RECURRENCE_MASTER, recurrenceMaster));
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    public AbstractAJAXParser<UpdatesResponse> getParser() {
        return new UpdatesParser(columns);
    }

}
