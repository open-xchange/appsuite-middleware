
package com.openexchange.ajax.group.actions;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;
import com.openexchange.java.Strings;

public class AllRequest extends AbstractGroupRequest<AllResponse> {

    private final boolean failOnError;
    private final int[] columns;

    public AllRequest(int[] columns, boolean failOnError) {
        this.failOnError = failOnError;
        this.columns = columns;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL, AJAXServlet.PARAMETER_COLUMNS, Strings.join(columns, ",")).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends AllResponse> getParser() {
        return new AllParser(this.failOnError);
    }

}
