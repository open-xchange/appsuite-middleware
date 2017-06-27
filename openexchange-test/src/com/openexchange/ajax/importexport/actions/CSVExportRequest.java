
package com.openexchange.ajax.importexport.actions;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.importexport.actions.AbstractImportRequest.Action;

public class CSVExportRequest extends AbstractExportRequest<CSVExportResponse> {

    private boolean exportDlists = true;
    private boolean failOnError = true;
    private String objectId = "";

    public CSVExportRequest(int folderId) {
        this(folderId, true);
    }

    public CSVExportRequest(int folderId, boolean exportDlists) {
        super(Action.CSV, folderId);
        this.exportDlists = exportDlists;
    }    
    
    public CSVExportRequest(int folderId, String objectId) {
        this(folderId, true);
        this.objectId = objectId;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        Parameter[] parameters = super.getParameters();
        if (!exportDlists) {
            parameters = parametersToAdd(new Parameter("export_dlists", false), parameters);
        }
        if(null != objectId || !objectId.equals("")) {
            parameters = parametersToAdd(new Parameter(AJAXServlet.PARAMETER_ID, this.objectId), parameters);
        }
        return parameters;
    }

    @Override
    public AbstractAJAXParser<? extends CSVExportResponse> getParser() {
        return new CSVExportParser(failOnError);
    }
    
    private com.openexchange.ajax.framework.AJAXRequest.Parameter[] parametersToAdd(Parameter parameter, Parameter[] parameters) {
        Parameter[] newParameters = new Parameter[parameters.length + 1];
        System.arraycopy(parameters, 0, newParameters, 0, parameters.length);
        newParameters[newParameters.length - 1] = parameter;
        return newParameters;
    }

}
