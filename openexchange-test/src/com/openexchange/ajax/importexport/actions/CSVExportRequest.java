
package com.openexchange.ajax.importexport.actions;

import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.importexport.actions.AbstractImportRequest.Action;

public class CSVExportRequest extends AbstractExportRequest<CSVExportResponse> {

    private boolean exportDlists = true;

    private boolean failOnError = true;

    public CSVExportRequest(int folderId) {
        this(folderId, true);
    }

    public CSVExportRequest(int folderId, boolean exportDlists) {
        super(Action.CSV, folderId);
        this.exportDlists = exportDlists;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        Parameter[] parameters = super.getParameters();
        if (!exportDlists) {
            Parameter[] newParameters = new Parameter[parameters.length + 1];
            System.arraycopy(parameters, 0, newParameters, 0, parameters.length);
            newParameters[newParameters.length - 1] = new Parameter("export_dlists", false);
            return newParameters;
        }

        return parameters;
    }

    @Override
    public AbstractAJAXParser<? extends CSVExportResponse> getParser() {
        return new CSVExportParser(failOnError);
    }

}
