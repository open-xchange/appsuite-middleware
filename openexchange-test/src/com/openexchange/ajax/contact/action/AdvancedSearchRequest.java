
package com.openexchange.ajax.contact.action;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;
import com.openexchange.java.Strings;

public class AdvancedSearchRequest extends AbstractContactRequest<SearchResponse> {

    private JSONObject filter;
    private int orderBy;
    private String orderDir;
    private int[] columns;
    private String collation;

    public void setFilter(JSONObject filter) {
        this.filter = filter;
    }

    public void setOrderBy(int orderBy) {
        this.orderBy = orderBy;
    }

    public void setOrderDir(String orderDir) {
        this.orderDir = orderDir;
    }

    public void setColumns(int[] columns) {
        this.columns = columns;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return filter;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        Params params = new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_TERMSEARCH);
        if (columns != null) {
            params.add(AJAXServlet.PARAMETER_COLUMNS, Strings.join(columns, ","));
        }
        if (orderBy != -1) {
            params.add(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy));
        }
        if (orderDir != null) {
            params.add(AJAXServlet.PARAMETER_ORDER, orderDir);
        }
        if (collation != null) {
            params.add(AJAXServlet.PARAMETER_COLLATION, collation);
        }

        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<SearchResponse> getParser() {
        return new SearchParser(true, columns);
    }

    public AdvancedSearchRequest() {
        super();
    }

    public AdvancedSearchRequest(JSONObject filter, int[] columns, int orderBy, String orderDir) {
        this();
        setFilter(filter);
        setColumns(columns);
        setOrderBy(orderBy);
        setOrderDir(orderDir);
    }

    public AdvancedSearchRequest(JSONObject filter, int[] columns, int orderBy, String orderDir, String collation) {
        this(filter, columns, orderBy, orderDir);
        setCollation(collation);
    }
}
