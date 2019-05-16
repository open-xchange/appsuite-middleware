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

package com.openexchange.multifactor.provider.u2f.impl;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.DefaultRegistrationChallenge;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.yubico.u2f.data.messages.RegisterRequestData;

/**
 * {@link U2FRegistrationChallenge}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class U2FRegistrationChallenge extends DefaultRegistrationChallenge {

    private static final String REQUEST_ID_PARAMETER = "requestId";
    private static final String REGISTERED_KEYS_PARAMETER = "registeredKeys";
    private static final String REGISTER_REQUESTS_PARAMETER = "registerRequests";

    /**
     * Initializes a new {@link U2FRegistrationChallenge}.
     *
     * @param deviceId The device id
     * @param data The {@link RegisterRequestData} for the challenge
     * @throws OXException
     */
    public U2FRegistrationChallenge(String deviceId, RegisterRequestData data) throws OXException {
        super(deviceId, new HashMap<>(3));
        JSONObject json;
        try {
            json = new JSONObject(data.toJson());
            challenge.put(REQUEST_ID_PARAMETER, data.getRequestId());
            challenge.put(REGISTERED_KEYS_PARAMETER, json.getJSONArray(REGISTERED_KEYS_PARAMETER));
            challenge.put(REGISTER_REQUESTS_PARAMETER, json.getJSONArray(REGISTER_REQUESTS_PARAMETER));
        } catch (JSONException e) {
            throw MultifactorExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

}
