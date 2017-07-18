
package com.openexchange.ajax.importexport.actions;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.importexport.actions.AbstractImportRequest.Action;
import edu.emory.mathcs.backport.java.util.Arrays;

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
            parameters = parametersToAdd(new Parameter("export_dlists", false), parameters);
        }
        if (this.getFolderId() < 0) {
            parameters = parametersToRemove(AJAXServlet.PARAMETER_FOLDERID, parameters);
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
    
    private com.openexchange.ajax.framework.AJAXRequest.Parameter[] parametersToRemove(String parameter, Parameter[] parameters) {
        List<Parameter> list = Arrays.asList(parameters);
        List<Parameter> newList = new ArrayList<Parameter>();
        for(Parameter param : list){
            if(!param.getName().equals(parameter)){
                newList.add(param);
            }
        }
        return newList.toArray(new Parameter[newList.size()]);
    }

}
