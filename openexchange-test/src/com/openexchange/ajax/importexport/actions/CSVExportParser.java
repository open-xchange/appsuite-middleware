
package com.openexchange.ajax.importexport.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

public class CSVExportParser extends AbstractAJAXParser<CSVExportResponse> {

    protected CSVExportParser(boolean failOnError) {
        super(failOnError);
        // TODO Auto-generated constructor stub
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
        final CSVExportResponse retval = new CSVExportResponse();
        retval.setCSV(body);
        return retval;
    }

    @Override
    protected CSVExportResponse createResponse(final Response response) throws JSONException {
        throw new UnsupportedOperationException();
    }

}
