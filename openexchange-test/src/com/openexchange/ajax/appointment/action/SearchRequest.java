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

package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.request.AppointmentRequest;
import com.openexchange.java.Strings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class SearchRequest extends AbstractAppointmentRequest<SearchResponse> {

    private final JSONObject body = new JSONObject();

    private final SearchParser searchParser;

    private final List<Parameter> params = new ArrayList<Parameter>();

    public SearchRequest(String pattern, int folderId, int[] columns) {
        this(pattern, folderId, columns, true);
    }

    public SearchRequest(final String pattern, final int inFolder, final int[] columns, final boolean failOnError) {
        this(pattern, inFolder, null, null, columns, -1, null, false, failOnError);
    }

    public SearchRequest(final String pattern, final int inFolder, final Date startDate, final Date endDate, final int[] columns, final int orderBy, final String orderDir, final boolean recurrenceMaster, final boolean failOnError) {
        super();
        searchParser = new SearchParser(failOnError, columns);

        param(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
        param(AJAXServlet.PARAMETER_COLUMNS, Strings.join(columns, ", "));
        if (orderBy != -1) {
            param(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy));
            param(AJAXServlet.PARAMETER_ORDER, orderDir);
        }
        param(AJAXServlet.PARAMETER_START, startDate);
        param(AJAXServlet.PARAMETER_END, endDate);
        if (recurrenceMaster) {
            param(AppointmentRequest.RECURRENCE_MASTER, String.valueOf(recurrenceMaster));
        }

        try {
            if (inFolder != -1) {
                body.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
            }
            if (pattern != null) {
                body.put(SearchFields.PATTERN, pattern);
            }
        } catch (final JSONException e) {
            throw new IllegalStateException(e); // Shouldn't happen
        }

    }

    private void param(final String key, final String value) {
        if (value != null) {
            params.add(new Parameter(key, value));
        }
    }

    private void param(final String key, final Date value) {
        if (value != null) {
            params.add(new Parameter(key, value));
        }
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public SearchParser getParser() {
        return searchParser;
    }

    @Override
    public Object getBody() {
        return body;
    }
}
