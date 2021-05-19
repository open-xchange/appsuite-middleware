
package com.openexchange.ajax.contact.action;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
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
    private List<String> folders;
    private boolean failOnError;

    public void setFilter(JSONObject filter) {
        this.filter = filter;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
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

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject bodyObject = new JSONObject(filter);
        if (null != folders) {
            JSONArray foldersArray = new JSONArray(folders.size());
            for (int i = 0; i < folders.size(); i++) {
                foldersArray.put(i, folders.get(i));
            }
            bodyObject.put("folders", foldersArray);
        }
        return bodyObject;
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
        return new SearchParser(failOnError, columns);
    }

    public AdvancedSearchRequest() {
        super();
        this.failOnError = true;
    }

    public AdvancedSearchRequest(JSONObject filter, int[] columns, int orderBy, String orderDir) {
        this();
        setFilter(filter);
        setColumns(columns);
        setOrderBy(orderBy);
        setOrderDir(orderDir);
    }

    public AdvancedSearchRequest(JSONObject filter, List<String> folders, int[] columns, int orderBy, String orderDir) {
        this(filter, columns, orderBy, orderDir);
        setFolders(folders);
    }

    public AdvancedSearchRequest(JSONObject filter, List<String> folders, int[] columns, int orderBy, String orderDir, String collation) {
        this(filter, folders, columns, orderBy, orderDir);
        setCollation(collation);
    }

}
