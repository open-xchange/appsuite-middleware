
package com.openexchange.ajax.importexport.actions;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

public class CSVExportResponse extends AbstractAJAXResponse {

    private String csv;

    protected CSVExportResponse(Response response) {
        super(response);
    }

    public CSVExportResponse() {
        super(null);
    }

    public void setCSV(String body) {
        this.csv = body;
    }

    @Override
    public Object getData() {
        return csv;
    }
}
