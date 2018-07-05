
package com.openexchange.ajax.importexport.actions;

import org.apache.http.HttpResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

public class CSVExportResponse extends AbstractAJAXResponse {

    private String csv;
    
    private final HttpResponse response; 

    protected CSVExportResponse(Response response) {
        super(response);
        this.response = null;
    }

    public CSVExportResponse(HttpResponse response) {
        super(null);
        this.response = response;
    }

    public void setCSV(String body) {
        this.csv = body;
    }

    @Override
    public Object getData() {
        return csv;
    }
    
    public HttpResponse getHttpResponse() {
        return response;
    }  
    
}
