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

package com.openexchange.importexport.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * Encapsulates a request for exporting data.
 */
public class ExportRequest {

    private static final String PARAMETER_FOLDER_ID = "folder_id";

	private ServerSession session;
	private AJAXRequestData request;
	private String folder;
	private List<Integer> columns;
	private Map<String, List<String>> batchIds;

    /**
     * Initializes a new {@link ExportRequest}.
     *
     * @param request The AJAX request
     * @param session The associated session
     * @throws OXException If initialization fails
     */
    public ExportRequest(AJAXRequestData request, ServerSession session) throws OXException {
        super();
        this.setSession(session);
        this.setRequest(request);

        Object data = request.getData();
        if (data instanceof JSONArray) {
            try {
                batchIds = extractBatchArrayFromRequest((JSONArray) data);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e);
            }
        } else {
            String value = request.getParameter("body");
            if (Strings.isNotEmpty(value)) {
                String ids = value;
                try{
                    batchIds = extractBatchArrayFromRequest(new JSONArray(ids));
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e);
                }
            } else {
                batchIds = null;
                if (request.getParameter(AJAXServlet.PARAMETER_FOLDERID) == null) {
                    throw ImportExportExceptionCodes.NEED_FOLDER.create();
                }
            }
        }

        String colStr = request.getParameter(AJAXServlet.PARAMETER_COLUMNS);
        if (colStr != null) {
            String[] split = Strings.splitByComma(colStr);
            setColumns(new LinkedList<Integer>());
            for (String s : split) {
                try {
                    getColumns().add(Integer.valueOf(s));
                } catch (NumberFormatException e) {
                    throw ImportExportExceptionCodes.IRREGULAR_COLUMN_ID.create(e, s);
                }
            }
        }
        this.setFolder(request.getParameter(AJAXServlet.PARAMETER_FOLDERID));
    }

    private Map<String, List<String>> extractBatchArrayFromRequest(JSONArray jPairs) throws JSONException {
        int length = jPairs.length();
        if (length <= 0) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> batchIds = new LinkedHashMap<String, List<String>>(length);
        for (int i = 0; i < length; i++) {
            JSONObject tuple = jPairs.getJSONObject(i);
            String folderId = tuple.getString(PARAMETER_FOLDER_ID);
            String objectId = tuple.getString(AJAXServlet.PARAMETER_ID);
            List<String> valueList = batchIds.get(folderId);
            if (null == valueList) {
                valueList = new ArrayList<String>();
                batchIds.put(folderId, valueList);
            }
            valueList.add(objectId);
        }
        return batchIds;
    }

    public Map<String, List<String>> getBatchIds() {
        return batchIds;
    }

    public void setBatchIds(Map<String, List<String>> batchIds) {
        this.batchIds = batchIds;
    }

    public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public AJAXRequestData getRequest() {
		return request;
	}

	public void setRequest(AJAXRequestData request) {
		this.request = request;
	}

	public ServerSession getSession() {
		return session;
	}

	public void setSession(ServerSession session) {
		this.session = session;
	}

	public List<Integer> getColumns() {
		return columns;
	}

	public void setColumns(List<Integer> columns) {
		this.columns = columns;
	}

	public String getObjectId() {
	    return request.getParameter(AJAXServlet.PARAMETER_ID);
	}
}
