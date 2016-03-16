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

package com.openexchange.ajax.folder.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
import org.json.JSONValue;

/**
 * {@link GenJSONRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class GenJSONRequest extends AbstractFolderRequest<GenJSONResponse> {

    private final boolean failOnError;

    private JSONValue jsonValue;

    private Method method;

    private final Map<String, String> parameters;

    public GenJSONRequest(final API api) {
        this(api, true);
    }

    /**
     * Initializes a new {@link GenJSONRequest} with method set to <code>PUT</code>.
     *
     * @param failOnError Whether to fail on error
     */
    public GenJSONRequest(final API api, final boolean failOnError) {
        super(api);
        this.failOnError = failOnError;
        method = Method.PUT;
        parameters = new HashMap<String, String>();
    }

    public void setJSONValue(final JSONValue jsonValue) {
        this.jsonValue = jsonValue;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    public void setParameter(final String name, final String value) {
        parameters.put(name, value);
    }

    @Override
    public Object getBody() {
        return null == jsonValue ? JSONObject.NULL : jsonValue;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        for (final Entry<String, String> entry : parameters.entrySet()) {
            params.add(new Parameter(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public GenJSONParser getParser() {
        return new GenJSONParser(failOnError);
    }
}
