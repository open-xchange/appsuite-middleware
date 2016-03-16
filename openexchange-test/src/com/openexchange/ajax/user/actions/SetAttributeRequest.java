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

package com.openexchange.ajax.user.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.user.json.actions.SetAttributeAction;

/**
 * {@link SetAttributeRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class SetAttributeRequest extends AbstractUserRequest<SetAttributeResponse> {

    private final int userId;
    private final String name;
    private final Object value;
    private final boolean setIfAbsent;
    private final boolean failOnError;

    public SetAttributeRequest(int userId, String name, Object value, boolean setIfAbsent) {
        this(userId, name, value, setIfAbsent, true);
    }

    public SetAttributeRequest(int userId, String name, Object value, boolean setIfAbsent, boolean failOnError) {
        super();
        this.userId = userId;
        this.name = name;
        this.value = value;
        this.setIfAbsent = setIfAbsent;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_ACTION, SetAttributeAction.ACTION),
            new URLParameter(AJAXServlet.PARAMETER_ID, userId),
            new URLParameter("setIfAbsent", setIfAbsent)
        };
    }

    @Override
    public SetAttributeParser getParser() {
        return new SetAttributeParser(failOnError);
    }

    @Override
    public Object getBody() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("name", name);
        if (null != value) {
            body.put("value", value);
        }
        return body;
    }
}
