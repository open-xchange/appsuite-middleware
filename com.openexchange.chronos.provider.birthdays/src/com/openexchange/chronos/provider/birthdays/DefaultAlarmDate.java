/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.birthdays;

import static com.openexchange.chronos.provider.birthdays.BirthdaysCalendarProvider.PROVIDER_ID;
import static com.openexchange.osgi.Tools.requireService;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.UserConfigWrapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.conversion.ConversionService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobKeys;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link DefaultAlarmDate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultAlarmDate implements JSlobEntry {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultAlarmDate}.
     *
     * @param services A service lookup reference
     */
    public DefaultAlarmDate(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getKey() {
        return JSlobKeys.CALENDAR;
    }

    @Override
    public String getPath() {
        return PROVIDER_ID + '/' + "defaultAlarmDate";
    }

    @Override
    public boolean isWritable(Session session) throws OXException {
        return true;
    }

    @Override
    public Map<String, Object> metadata(Session session) throws OXException {
        return null;
    }

    @Override
    public Object getValue(Session session) throws OXException {
        List<CalendarAccount> accounts = requireService(CalendarAccountService.class, services).getAccounts(session, PROVIDER_ID, null);
        if (null == accounts || accounts.isEmpty()) {
            return JSONObject.NULL;
        }
        JSONObject userConfig = accounts.get(0).getUserConfiguration();
        return null == userConfig ? JSONObject.NULL : userConfig.optJSONArray("defaultAlarmDate");
    }

    @Override
    public void setValue(Object value, Session session) throws OXException {
        /*
         * get account & current user config
         */
        CalendarAccountService accountService = requireService(CalendarAccountService.class, services);
        List<CalendarAccount> accounts = accountService.getAccounts(session, PROVIDER_ID, null);
        if (null == accounts || accounts.isEmpty()) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(PROVIDER_ID);
        }
        CalendarAccount account = accounts.get(0);
        JSONObject userConfig = account.getUserConfiguration();
        if (null == userConfig) {
            userConfig = new JSONObject();
        }
        /*
         * set & check default alarm
         */
        userConfig.putSafe("defaultAlarmDate", value);
        try {
            UserConfigWrapper configWrapper = new UserConfigWrapper(requireService(ConversionService.class, services), userConfig);
            List<Alarm> defaultAlarm = configWrapper.getDefaultAlarmDate();
            if (null != defaultAlarm) {
                Check.alarmsAreValid(defaultAlarm);
                Check.haveReleativeTriggers(defaultAlarm);
            }
        } catch (OXException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(e, String.valueOf(userConfig));
        }
        /*
         * update account settings
         */
        CalendarSettings settings = new CalendarSettings();
        settings.setConfig(userConfig);
        accountService.updateAccount(session, account.getAccountId(), settings, account.getLastModified().getTime(), null);

    }

}
