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

package com.openexchange.ajax.session.actions;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_ACTION;
import static com.openexchange.ajax.LoginServlet.ACTION_TOKENLOGIN;
import static com.openexchange.ajax.fields.LoginFields.AUTHID_PARAM;
import static com.openexchange.ajax.fields.LoginFields.AUTOLOGIN_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.LOGIN_PARAM;
import static com.openexchange.ajax.fields.LoginFields.PASSWORD_PARAM;
import static com.openexchange.ajax.fields.LoginFields.VERSION_PARAM;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.java.util.UUIDs;

/**
 * {@link TokenLoginJSONRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public final class TokenLoginJSONRequest extends AbstractRequest<TokenLoginJSONResponse> {

    public TokenLoginJSONRequest(String login, String password, String authId, String client, String version, boolean autologin, String clientToken, boolean json) {
        super(createParameter(login, password, authId, client, version, autologin, clientToken, json, false));
    }

    public TokenLoginJSONRequest(String login, String password, boolean jsonResponse) {
        this(login, password, LoginTools.generateAuthId(), AJAXClient.class.getName(), AJAXClient.VERSION, true, UUIDs.getUnformattedString(UUID.randomUUID()), jsonResponse);
    }

    public TokenLoginJSONRequest(String login, String password, boolean jsonResponse, boolean passwordInURL) {
        super(createParameter(login, password, LoginTools.generateAuthId(), AJAXClient.class.getName(), AJAXClient.VERSION, true, UUIDs.getUnformattedString(UUID.randomUUID()), jsonResponse, passwordInURL));
    }

    private static Parameter[] createParameter(String login, String password, String authId, String client, String version, boolean autologin, String clientToken, boolean json, boolean passwordInURL) {
        List<Parameter> retval = new ArrayList<Parameter>();
        if (passwordInURL) {
            retval.add(new URLParameter(PASSWORD_PARAM, password));
        }
        retval.add(new URLParameter(PARAMETER_ACTION, ACTION_TOKENLOGIN));
        retval.add(new URLParameter(AUTHID_PARAM, authId));
        retval.add(new FieldParameter(LOGIN_PARAM, login));
        retval.add(new FieldParameter(PASSWORD_PARAM, password));
        retval.add(new FieldParameter(CLIENT_PARAM, client));
        retval.add(new FieldParameter(VERSION_PARAM, version));
        retval.add(new FieldParameter(AUTOLOGIN_PARAM, Boolean.toString(autologin)));
        retval.add(new FieldParameter(CLIENT_TOKEN, clientToken));
        if (json) {
            retval.add(new URLParameter("jsonResponse", true));
        }
        return retval.toArray(new Parameter[retval.size()]);
    }

    @Override
    public TokenLoginJSONParser getParser() {
        return new TokenLoginJSONParser(false);
    }
}
