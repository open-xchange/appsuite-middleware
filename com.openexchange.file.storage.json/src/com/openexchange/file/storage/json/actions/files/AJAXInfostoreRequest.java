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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.file.storage.json.actions.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction.Param;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;

import static com.openexchange.file.storage.FileStorageFileAccess.SortDirection;

/**
 * {@link AJAXInfostoreRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AJAXInfostoreRequest implements InfostoreRequest{

    protected AJAXRequestData data;
    private List<Field> columns;
    private Field sortingField;
    private ServerSession session;
    private Map<String, String> folderMapping = new HashMap<String, String>();
    private List<String> ids = null;
    
    
    public AJAXInfostoreRequest(AJAXRequestData requestData, ServerSession session) {
        this.data = requestData;
        this.session = session;
    }

    public void require(Param... params) throws AjaxException {
        String[] names = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            names[i] = params[i].getName();
        }
        List<String> missingParameters = data.getMissingParameters(names);
        if(!missingParameters.isEmpty()) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, missingParameters.toString());
        }
    }
    
    public void requireBody() throws AjaxException {
        if(data.getData() == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, "data");
        }
    }
    
    public String getFolderId() throws AbstractOXException {
        return data.getParameter(Param.FOLDER_ID.getName());
    }

    public List<Field> getColumns() throws AbstractOXException {
        if(columns != null) {
            return columns;
        }
        
        String parameter = data.getParameter(Param.COLUMNS.getName());
        if(parameter == null || parameter.equals("")) {
            return columns = Arrays.asList(File.Field.values());
        }
        String[] columnStrings = parameter.split("\\s*,\\s*");
        List<Field> fields = new ArrayList<Field>(columnStrings.length);
        List<String> unknownColumns = new ArrayList<String>(columnStrings.length);
        
        for (String columnNumberOrName : columnStrings) {
            Field field = Field.get(columnNumberOrName);
            if(field == null) {
                unknownColumns.add(columnNumberOrName);
            } else {
                fields.add( field );
            }
        }
        
        if(!unknownColumns.isEmpty()) {
            throw new AjaxException(AjaxException.Code.InvalidParameterValue, Param.COLUMNS.getName(), unknownColumns.toString());
        }
        
        return columns = fields;
    }
    
    public Field getSortingField() throws AbstractOXException {
        if(sortingField != null) {
            return sortingField;
        }
        String sort = data.getParameter(Param.SORT.getName());
        if(sort == null) {
            return null;
        }
        Field field = sortingField = Field.get(sort);
        if(field == null) {
            throw new AjaxException(AjaxException.Code.InvalidParameterValue, Param.SORT.getName(), sort);
        }
        return field;
    }

    public SortDirection getSortingOrder() throws AbstractOXException{
        SortDirection sortDirection = SortDirection.get(data.getParameter(Param.ORDER.getName()));
        if(sortDirection == null) {
            throw new AjaxException(AjaxException.Code.InvalidParameterValue, Param.ORDER.getName(), sortDirection);
        }
        return sortDirection;
    }
    
    public TimeZone getTimezone() throws AbstractOXException {
        String parameter = data.getParameter(Param.TIMEZONE.getName());
        if(parameter == null) {
            parameter = getSession().getUser().getTimeZone();
        }
        return TimeZone.getTimeZone(parameter);
    }

    public FileStorageFileAccess getFileAccess() {
        return null;
    }

    public ServerSession getSession() {
        return session;
    }

    public String getId() {
        return data.getParameter(Param.ID.getName());
    }

    public int getVersion() {
        String parameter = data.getParameter(Param.VERSION.getName());
        if(parameter == null) {
            return FileStorageFileAccess.CURRENT_VERSION;
        }
        return Integer.parseInt(parameter);
    }

    public Set<String> getIgnore() {
        String parameter = data.getParameter(Param.IGNORE.getName());
        if(parameter == null) {
            return Collections.emptySet();
        }
        
        return new HashSet<String>(Arrays.asList(parameter.split("\\s*,\\s*")));
    }

    public long getTimestamp() {
        String parameter = data.getParameter(Param.TIMESTAMP.getName());
        if(parameter == null) {
            return FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER;
        }
        
        return Long.parseLong(parameter);
    }
    
    public List<String> getIds() throws AjaxException {
        parseIDList();
        return ids;
    }

    private void parseIDList() throws AjaxException {
        try {
            if(ids != null) {
                return;
            }
            JSONArray array = (JSONArray) data.getData();
            ids = new ArrayList<String>(array.length());
            for(int i = 0, size = array.length(); i < size; i++) {
                JSONObject tuple = array.getJSONObject(i);
                String id = tuple.getString(Param.ID.getName());
                ids.add(id);
                
                folderMapping.put(id, tuple.optString(Param.FOLDER_ID.getName()));
            }
        } catch (JSONException x) {
            throw new AjaxException(AjaxException.Code.JSONError, x.getMessage());
        }
        
    }


}
