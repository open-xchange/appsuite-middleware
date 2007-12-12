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

package com.openexchange.ajax.framework;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CommonListRequest implements AJAXRequest {

    private final String servletPath;

    private final String[][] folderAndObjectIds;

    private final int[] columns;

    private final boolean failOnError;

    /**
     * Default constructor.
     */
    public CommonListRequest(final String servletPath,
        final int[][] folderAndObjectIds, final int[] columns) {
        this(servletPath, folderAndObjectIds, columns, true);
    }

    /**
     * Constructor with all parameters.
     */
    public CommonListRequest(final String servletPath,
        final int[][] folderAndObjectIds, final int[] columns,
        final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderAndObjectIds = new String[folderAndObjectIds.length][];
        for (int i = 0; i < folderAndObjectIds.length; i++) {
        	this.folderAndObjectIds[i] = new String[folderAndObjectIds[i].length];
        	for (int j = 0; j < folderAndObjectIds[i].length; j++) {
        		this.folderAndObjectIds[i][j] = String.valueOf(folderAndObjectIds[i][j]);
			}
		}
        this.columns = columns;
        this.failOnError = failOnError;
    }

    /**
     * Default constructor.
     */
    public CommonListRequest(final String servletPath,
        final String[][] folderAndObjectIds, final int[] columns) {
        this(servletPath, folderAndObjectIds, columns, true);
    }

    /**
     * Constructor with all parameters.
     */
    public CommonListRequest(final String servletPath,
        final String[][] folderAndObjectIds, final int[] columns,
        final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderAndObjectIds = folderAndObjectIds;
        this.columns = columns;
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {
        return servletPath;
    }

    /**
     * {@inheritDoc}
     */
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (String[] folderAndObject : folderAndObjectIds) {
            final JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderAndObject[0]);
            json.put(DataFields.ID, folderAndObject[1]);
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
        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST),
            new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns)
        };
    }

    /**
     * {@inheritDoc}
     */
    public CommonListParser getParser() {
        return new CommonListParser(failOnError);
    }
}
