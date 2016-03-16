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

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER_ID;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.framework.AbstractRedirectParser;

/**
 * Parses the redirect response of the formLogin action of the login servlet.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FormLoginParser extends AbstractRedirectParser<FormLoginResponse> {

    FormLoginParser(boolean cookiesNeeded) {
        super(cookiesNeeded);
    }

    @Override
    protected FormLoginResponse createResponse(String location) throws JSONException {
        int fragIndex = location.indexOf('#');
        if (-1 == fragIndex) {
            return new FormLoginResponse(location, null, null, -1, null, false);
        }
        String path = location.substring(0, fragIndex);
        String[] params = location.substring(fragIndex + 1).split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            int assignPos = param.indexOf('=');
            if (-1 == assignPos) {
                map.put(param, null);
            } else {
                map.put(param.substring(0, assignPos), param.substring(assignPos + 1));
            }
        }
        String userIdValue = map.get(PARAMETER_USER_ID);
        final int userId;
        if (null == userIdValue) {
            userId = -1;
        } else {
            try {
                userId = Integer.parseInt(userIdValue);
            } catch (NumberFormatException e) {
                throw new JSONException("Can not parse user_id value \"" + userIdValue + "\".", e);
            }
        }
        String booleanValue = map.get("store");
        final boolean store;
        if (null == booleanValue) {
            store = false;
        } else {
            store = Boolean.parseBoolean(booleanValue);
        }
        return new FormLoginResponse(path, map.get(PARAMETER_SESSION), map.get(PARAMETER_USER), userId, map.get("language"), store);
    }
}
