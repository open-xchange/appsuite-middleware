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

package com.openexchange.ajax.quota;

import java.io.IOException;
import java.util.LinkedList;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;

/**
 * {@link GetQuotaRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GetQuotaRequest implements AJAXRequest<GetQuotaResponse> {

    private final String module;
    private final String account;
    private boolean failOnError;

    /**
     * Initializes a new {@link GetQuotaRequest}.
     *
     * @param module The module identifier, or <code>null</code> if not defined
     * @param account The account identifier, or <code>null</code> if not defined
     */
    public GetQuotaRequest(String module, String account) {
        super();
        this.module = module;
        this.account = account;
        this.failOnError = true;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/quota";
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        LinkedList<Parameter> parameters = new LinkedList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        if (null != module) {
            parameters.add(new Parameter("module", module));
        }
        if (null != account) {
            parameters.add(new Parameter("account", account));
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends GetQuotaResponse> getParser() {
        return new AbstractAJAXParser<GetQuotaResponse>(failOnError) {

            @Override
            protected GetQuotaResponse createResponse(Response response) throws JSONException {
                return new GetQuotaResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    /**
     * Gets the failOnError
     *
     * @return The failOnError
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Sets the failOnError
     *
     * @param failOnError The failOnError to set
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

}
