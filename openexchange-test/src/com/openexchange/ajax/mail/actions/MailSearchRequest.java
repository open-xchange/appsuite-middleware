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

package com.openexchange.ajax.mail.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.Header;

/**
 * {@link MailSearchRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MailSearchRequest implements AJAXRequest<MailSearchResponse> {

    private final List<Parameter> params;

    private JSONArray body;

    private final MailSearchParser searchParser;

    /**
     * This constructor allows to set the parameters of the request.
     * For it to work properly, you also have to set the body of the
     * PUT request afterwards.
     *
     * @see #setBody(JSONArray)
     */
    public MailSearchRequest(final String folder, final int[] columns, final int orderBy, final String orderDir, final boolean failOnError) {
        params = new LinkedList<Parameter>();

        searchParser = new MailSearchParser(failOnError, columns);

        param(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);

        param(AJAXServlet.PARAMETER_INFOLDER, folder);

        param(AJAXServlet.PARAMETER_COLUMNS, join(columns));

        if (orderBy != -1) {
            param(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy));
            param(AJAXServlet.PARAMETER_ORDER, orderDir);
        }
    }

    /**
     * Convenience constructor that allows the body to be passed on a
     * argument.
     *
     */
    public MailSearchRequest(final JSONArray body, final String folder, final int[] columns, final int orderBy, final String orderDir, final boolean failOnError) {
        this(folder, columns, orderBy, orderDir, failOnError);
        setBody(body);
    }

    @Override
    public Object getBody() {
        return body;
    }

    public void setBody(JSONArray body){
        this.body = body;
        param(AJAXServlet.PARAMETER_DATA, body.toString());
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public MailSearchParser getParser() {
        return searchParser;
    }

    @Override
    public String getServletPath() {
        return "/ajax/mail";
    }

    // TODO refactor: move to super class or helper
    private void param(final String key, final String value) {
        if (value != null) {
            params.add(new Parameter(key, value));
        }
    }

    // TODO refactor: move to super class or helper
    private String join(final int[] values) {
        final StringBuilder b = new StringBuilder();
        for (final int v : values) {
            b.append(v).append(", ");
        }
        b.setLength(b.length() - 2);
        return b.toString();
    }
}
