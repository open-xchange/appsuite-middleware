/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.importexport.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.importexport.actions.AbstractImportRequest.Action;
import com.openexchange.java.Strings;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ICalExportRequest extends AbstractExportRequest<ICalExportResponse> {

    private final int type;
    private final String body;

    public ICalExportRequest(final int folderId) {
        this(folderId, -1);
    }
    
    public ICalExportRequest(final int folderId, final int type) {
        super(Action.ICal, folderId);
        this.type = type;     
        this.body = "";
    }            
    
    public ICalExportRequest(final int folderId, final int type, String body) {
        super(Action.ICal, folderId);
        this.type = type;   
        this.body = body;
    }
    
    @Override
    public Parameter[] getParameters() {
        com.openexchange.ajax.framework.AJAXRequest.Parameter[] parameters = super.getParameters();
        if (type != -1) {
            parameters = parametersToAdd(new Parameter(AJAXServlet.PARAMETER_TYPE, this.type), parameters);
        }
        if (this.getFolderId() < 0) {
            parameters = parametersToRemove(AJAXServlet.PARAMETER_FOLDERID, parameters);
        }
        if (Strings.isNotEmpty(body)) {
            parameters = parametersToAdd(new Parameter("body", body), parameters);
        }
        return parameters;
    }

    @Override
    public ICalExportParser getParser() {
        return new ICalExportParser(true);
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
            if (!param.getName().equals(parameter)){
                newList.add(param);
            }
        }
        return newList.toArray(new Parameter[newList.size()]);
    }
}
