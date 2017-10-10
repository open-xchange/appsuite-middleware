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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider.birthdays;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link BirthdaysCalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BirthdaysCalendarProvider implements CalendarProvider {

    static final String PROVIDER_ID = "birthdays";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link BirthdaysCalendarProvider}.
     *
     * @param services A service lookup reference
     */
    public BirthdaysCalendarProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return StringHelper.valueOf(locale).getString(BirthdaysCalendarStrings.PROVIDER_NAME);
    }

    @Override
    public JSONObject configureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * check if user already has an account
         */
        List<CalendarAccount> existingAccounts = services.getService(AdministrativeCalendarAccountService.class).getAccounts(session.getContextId(), new int[] { session.getUserId() }, PROVIDER_ID);
        if (0 < existingAccounts.size()) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(PROVIDER_ID);
        }
        /*
         * initialize & check user config
         */
        initializeUserConfig(userConfig);
        /*
         * store 'firstRun' flag to perform initial storage preparation upon next usage
         */

        return new JSONObject();
    }

    @Override
    public JSONObject reconfigureAccount(Session session, JSONObject internalConfig, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * initialize & check user config
         */
        initializeUserConfig(userConfig);
        /*
         * no further internal config needed
         */
        return null;
    }

    @Override
    public void initializeAccount(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        getAccess(session, account, parameters).initialize();
    }

    @Override
    public CalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return getAccess(session, account, parameters);
    }

    private void initializeUserConfig(JSONObject userConfig) throws OXException {
        if (null == userConfig) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("null");
        }
        try {
            /*
             * check configured folder types
             */
            Set<String> allowedTypes = Collections.unmodifiableSet("private", "shared", "public");
            JSONArray typesJSONArray = userConfig.optJSONArray("folderTypes");
            if (null == typesJSONArray || typesJSONArray.isEmpty()) {
                userConfig.put("folderTypes", new JSONArray(allowedTypes));
            } else {
                for (int i = 0; i < typesJSONArray.length(); i++) {
                    if (false == allowedTypes.contains(typesJSONArray.getString(i))) {
                        throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(String.valueOf(userConfig));
                    }
                }
            }
            /*
             * check default alarm
             */
            JSONObject defaultAlarmJSONObject = userConfig.optJSONObject("defaultAlarmDate");
            if (null != defaultAlarmJSONObject) {

                //TODO: check alarm completeness, only allow alarm with relative trigger

            }
        } catch (JSONException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(e, String.valueOf(userConfig));
        }
    }


    private BirthdaysCalendarAccess getAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (false == serverSession.getUserConfiguration().hasContact()) {
            throw CalendarExceptionCodes.INSUFFICIENT_ACCOUNT_PERMISSIONS.create();
        }
        return new BirthdaysCalendarAccess(services, serverSession, account, parameters);
    }

}
