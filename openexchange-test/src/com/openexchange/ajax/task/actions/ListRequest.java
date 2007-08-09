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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.task.actions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.TaskFields;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ListRequest extends AbstractTaskRequest {

    private final int[][] folderAndTaskIds;

    private final int[] columns;

    /**
     * Default constructor.
     */
    public ListRequest(final int[][] folderAndTaskIds, final int[] columns) {
        super();
        this.folderAndTaskIds = folderAndTaskIds;
        this.columns = columns;
    }

    /**
     * {@inheritDoc}
     */
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (int[] folderAndTask : folderAndTaskIds) {
            final JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderAndTask[0]);
            json.put(TaskFields.ID, folderAndTask[1]);
            array.put(json);
        }
        return array;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
            .ACTION_LIST));
        final StringBuilder columnSB = new StringBuilder();
        for (int i : columns) {
            columnSB.append(i);
            columnSB.append(',');
        }
        columnSB.delete(columnSB.length() - 1, columnSB.length());
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columnSB
            .toString()));
        return params.toArray(new Parameter[params.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public ListParser getParser() {
        return new ListParser(true);
    }
}
