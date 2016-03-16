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

package com.openexchange.ajax.contact.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.CommonSearchResponse;

/**
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class SearchByBirthdayRequest extends AbstractContactRequest<CommonSearchResponse> {

    private final List<AJAXRequest.Parameter> params = new ArrayList<AJAXRequest.Parameter>();
    private final SearchParser searchParser;

    public SearchByBirthdayRequest(Date start, Date end, String inFolder, int[] columns, boolean failOnError) {
        this(start, end, inFolder, columns, -1, null, null, failOnError);
    }
    
    public SearchByBirthdayRequest(Date start, Date end, String inFolder, int[] columns, int orderBy, String orderDir, String collation, boolean failOnError) {
        super();
        this.searchParser = new SearchParser(failOnError, columns);
        params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_ACTION, "birthdays"));
        params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_START, String.valueOf(start.getTime())));
        params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_END, String.valueOf(end.getTime())));
        if (null != columns) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_COLUMNS, getColumns(columns)));
        }
        if (-1 != orderBy) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy)));
        }
        if (null != orderDir) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_ORDER, orderDir));
        }
        if (null != collation) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_COLLATION, collation));
        }
        if (null !=  inFolder) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_INFOLDER, inFolder));
        }
    }

    @Override
    public AJAXRequest.Method getMethod() {
        return AJAXRequest.Method.GET;
    }

    @Override
    public AJAXRequest.Parameter[] getParameters() {
        return params.toArray(new AJAXRequest.Parameter[params.size()]);
    }

    @Override
    public AbstractAJAXParser<SearchResponse> getParser() {
        return searchParser;
    }

    @Override
    public Object getBody() {
        return null;
    }

    private static String getColumns(int[] values) {
        StringBuilder b = new StringBuilder();
        for (int v : values) {
            b.append(v).append(",");
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

}
