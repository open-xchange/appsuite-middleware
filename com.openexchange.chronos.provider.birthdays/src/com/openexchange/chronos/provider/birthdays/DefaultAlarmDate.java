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
