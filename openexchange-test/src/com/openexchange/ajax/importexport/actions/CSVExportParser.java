
package com.openexchange.ajax.importexport.actions;

import java.io.IOException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

public class CSVExportParser extends AbstractAJAXParser<CSVExportResponse> {

    private HttpResponse httpResponse;
    
    protected CSVExportParser(boolean failOnError) {
        super(failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response getResponse(final String body) throws JSONException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CSVExportResponse parse(final String body) throws JSONException {
        final CSVExportResponse retval = new CSVExportResponse(httpResponse);
        retval.setCSV(body);
        return retval;
    }

    @Override
    protected CSVExportResponse createResponse(final Response response) throws JSONException {
        return new CSVExportResponse(httpResponse);
    }
    
    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        this.httpResponse = resp;
        return super.checkResponse(resp, request);
    }

}
