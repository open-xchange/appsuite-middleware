/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.publish.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Strings;

/**
 * {@link AllPublicationsRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AllPublicationsRequest extends AbstractPublicationRequest<AllPublicationsResponse> {

    private int id;

    private String Folder;

    private String entityModule; // the module of the entity.

    private List<String> columns; // a list of column names to load (id, entityId, entityModule, url, target)

    private Map<String, List<String>> dynamicColumns;   // add a parameter for every plugin whose columns you want to load listing the column
                                                        // names that need to be loaded in the response. If a plugin is not used by a certain
                                                        // publication all values will be null.

    public AllPublicationsRequest() {
        super();
    }

    public AllPublicationsRequest(String folder, int id, String entityModule, List<String> columns) {
        this();
        setId(id);
        setFolder(folder);
        setEntityModule(entityModule);
        setColumns(columns);
    }

    public AllPublicationsRequest(String folder, int id, String entityModule, List<String> columns, Map<String, List<String>> dynamicColumns) {
        this(folder, id, entityModule, columns);
        setDynamicColumns(dynamicColumns);
    }

    public AllPublicationsRequest(List<String> columns) {
    	this(null, -1, null, columns);
    }

    public AllPublicationsRequest(int id, String entityModule, List<String> columns) {
    	this(null, id, entityModule, columns);
    }

    public void setDynamicColumns(Map<String, List<String>> dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }

    public Map<String, List<String>> getDynamicColumns() {
        return dynamicColumns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setEntityModule(String entityModule) {
        this.entityModule = entityModule;
    }

    public String getEntityModule() {
        return entityModule;
    }

    public void setFolder(String folder) {
        Folder = folder;
    }

    public String getFolder() {
        return Folder;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        LinkedList<Parameter> params = new LinkedList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL));

        if( getId() > -1) {
            params.add(new Parameter("id", getId()));
        }

        if( getFolder() != null) {
            params.add(new Parameter("folder", getFolder()));
        }

        if( getEntityModule() != null) {
            params.add(new Parameter("entityModule", getEntityModule()));
        }

        if(getColumns() != null){
            params.add(new Parameter("columns", Strings.join(getColumns(), ",")));
        }

        if(getDynamicColumns() != null){
            for (String plugin : getDynamicColumns().keySet()) {
                params.add(new Parameter(plugin, Strings.join(getDynamicColumns().get(plugin), ",")));
            }
        }

        return params.toArray(new Parameter[0]);
    }

    @Override
    public AbstractAJAXParser<? extends AllPublicationsResponse> getParser() {
        return new AbstractAJAXParser<AllPublicationsResponse>(isFailOnError()) {

            @Override
            protected AllPublicationsResponse createResponse(final Response response) throws JSONException {
                return new AllPublicationsResponse(response);
            }
        };
    }

}
