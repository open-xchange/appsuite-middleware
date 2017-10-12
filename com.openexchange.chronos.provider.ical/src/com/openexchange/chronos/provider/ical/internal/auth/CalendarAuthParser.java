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

package com.openexchange.chronos.provider.ical.internal.auth;

import java.io.IOException;
import java.util.Map;
import org.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openexchange.auth.info.AuthInfo;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccountAttribute;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link CalendarAuthParser}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CalendarAuthParser {

    private static final CalendarAuthParser INSTANCE = new CalendarAuthParser();

    private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static CalendarAuthParser getInstance() {
        return INSTANCE;
    }

    private CalendarAuthParser() {
        super();
    }

    /**
     * Updates the configuration map to hold the authentication information in a structured way based on the given {@link AuthInfo} object. All previously contained auth information will be removed from configuration map and replaced by those from
     * {@link AuthInfo}.
     * 
     * @param authInfo {@link AuthInfo} that will be added to the configuration
     * @param configuration {@link Map} that will be enhanced (and cleaned up)
     * @throws OXException
     */
    public void updateConfiguration(AuthInfo authInfo, Map<String, Object> configuration) throws OXException {
        configuration.remove(CalendarAccountAttribute.OAUTH_ACCOUNT_ID_LITERAL.getName());
        configuration.remove(CalendarAccountAttribute.TOKEN_LITERAL.getName());
        configuration.remove(CalendarAccountAttribute.LOGIN_LITERAL.getName());
        configuration.remove(CalendarAccountAttribute.PASSWORD_LITERAL.getName());

        try {
            String auth = mapper.writeValueAsString(authInfo);
            configuration.put("auth", auth);
        } catch (JsonProcessingException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
    }

    /**
     * Returns the {@link AuthInfo} based on {@link JSONObject} configuration
     * 
     * @param configuration The {@link JSONObject}
     * @return {@link AuthInfo} if "auth" is available in configuration; otherwise <code>com.openexchange.auth.info.AuthInfo.NONE</code>
     * @throws OXException
     */
    public AuthInfo getAuthInfo(JSONObject configuration) throws OXException {
        Object auth = configuration.optString("auth", null);
        if (auth == null) {
            return AuthInfo.NONE;
        }
        try {
            return mapper.readValue((String) auth, AuthInfo.class);
        } catch (IOException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
    }

}
