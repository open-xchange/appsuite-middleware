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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.appsuite.client.common.calls.login;

import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.CLIENT;
import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.GUEST;
import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.LOGIN;
import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.PASSWORD;
import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.SHARE;
import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.STAY_SIGNED_IN;
import static com.openexchange.appsuite.client.common.AppsuiteApiConstants.TARGET;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.Nullable;
import com.openexchange.appsuite.client.AppsuiteClientExceptions;
import com.openexchange.appsuite.client.Credentials;
import com.openexchange.appsuite.client.common.AppsuiteApiConstants;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link GuestLoginCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class GuestLoginCall extends AbstractLoginCall {

    private final String share;
    private final String target;
    private final String optLoginName;

    /**
     * Initializes a new {@link GuestLoginCall}.
     * 
     * @param credentials The credentials to login with
     * @param optLoginName The optional login name the user has on the remote server as received by it
     * @param share The token of the share to access
     * @param target The path to a specific share target
     * @throws OXException In case parameter is missing
     */
    public GuestLoginCall(Credentials credentials, String optLoginName, String share, String target) throws OXException {
        super(credentials);
        checkParameters(share, target);

        this.optLoginName = optLoginName;
        this.share = share;
        this.target = target;
    }

    @Override
    protected String getAction() {
        return GUEST;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put(SHARE, share);
        parameters.put(TARGET, target);
        parameters.put(CLIENT, AppsuiteApiConstants.CLIENT_VALUE);
        parameters.put(STAY_SIGNED_IN, Boolean.TRUE.toString());
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException {
        try {
            JSONObject json = new JSONObject();
            String login;
            if (Strings.isNotEmpty(credentials.getLogin())) {
                login = credentials.getLogin();
            } else {
                login = null == optLoginName ? "" : optLoginName;
            }

            json.put(LOGIN, login);
            json.put(PASSWORD, null == credentials.getPassword() ? "" : credentials.getPassword());
            return toHttpEntity(json);
        } catch (JSONException e) {
            throw AppsuiteClientExceptions.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
