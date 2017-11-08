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

import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.osgi.Tools.requireService;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.common.UserConfigWrapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.AutoProvisioningCalendarProvider;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.SingleAccountCalendarProvider;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link BirthdaysCalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BirthdaysCalendarProvider implements AutoProvisioningCalendarProvider, SingleAccountCalendarProvider {

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
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(BirthdaysCalendarAccess.class);
    }

    @Override
    public JSONObject autoConfigureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * check capabilities and if user already has an account
         */
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (false == serverSession.getUserPermissionBits().hasContact()) {
            throw CalendarExceptionCodes.MISSING_CAPABILITY.create(com.openexchange.groupware.userconfiguration.Permission.CONTACTS.getCapabilityName());
        }
        /*
         * initialize & check user config
         */
        initializeUserConfig(serverSession, userConfig);
        /*
         * apply default properties for internal config
         */
        StringHelper stringHelper = StringHelper.valueOf(serverSession.getUser().getLocale());
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, true));
        extendedProperties.add(DESCRIPTION(stringHelper.getString(BirthdaysCalendarStrings.CALENDAR_DESCRIPTION), true));
        extendedProperties.add(USED_FOR_SYNC(Boolean.FALSE, true));
        JSONObject internalConfig = new JSONObject();
        try {
            DataHandler dataHandler = requireService(ConversionService.class, services).getDataHandler(DataHandlers.XPROPERTIES2JSON);
            ConversionResult result = dataHandler.processData(new SimpleData<ExtendedProperties>(extendedProperties), new DataArguments(), null);
            internalConfig.put("extendedProperties", result.getData());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return internalConfig;
    }

    @Override
    public JSONObject configureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * no manual account creation allowed as accounts are provisioned automatically
         */
        throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(PROVIDER_ID);
    }

    @Override
    public JSONObject reconfigureAccount(Session session, CalendarAccount calendarAccount, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * initialize & check user config
         */
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        initializeUserConfig(serverSession, userConfig);
        /*
         * no further internal config needed
         */
        return null;
    }

    @Override
    public void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        getAccess(session, account, parameters).onAccountCreated();
    }

    @Override
    public void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        getAccess(session, account, parameters).onAccountUpdated();
    }

    @Override
    public void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        getAccess(session, account, parameters).onAccountDeleted();
    }

    @Override
    public CalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return getAccess(session, account, parameters);
    }

    private void initializeUserConfig(ServerSession session, JSONObject userConfig) throws OXException {
        if (null == userConfig) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("null");
        }
        try {
            /*
             * check configured folder types
             */
            Set<String> allowedTypes = getAllowedFolderTypes(session.getUserPermissionBits());
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
        } catch (JSONException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(e, String.valueOf(userConfig));
        }
        /*
         * check default alarm
         */
        try {
            UserConfigWrapper configWrapper = new UserConfigWrapper(requireService(ConversionService.class, services), userConfig);
            Alarm defaultAlarm = configWrapper.getDefaultAlarmDate();
            if (null != defaultAlarm) {
                Check.alarmIsValid(defaultAlarm);
            }
        } catch (OXException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(e, String.valueOf(userConfig));
        }
    }

    private static Set<String> getAllowedFolderTypes(UserPermissionBits permissionBits) {
        Set<String> allowedFolderTypes = new HashSet<String>();
        if (permissionBits.hasContact()) {
            allowedFolderTypes.add("private");
            if (permissionBits.hasFullSharedFolderAccess()) {
                allowedFolderTypes.add("shared");
            }
            if (permissionBits.hasFullPublicFolderAccess()) {
                allowedFolderTypes.add("public");
            }
        }
        return allowedFolderTypes;
    }

    private BirthdaysCalendarAccess getAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (false == serverSession.getUserPermissionBits().hasContact()) {
            throw CalendarExceptionCodes.MISSING_CAPABILITY.create(com.openexchange.groupware.userconfiguration.Permission.CONTACTS.getCapabilityName());
        }
        return new BirthdaysCalendarAccess(services, serverSession, account, parameters);
    }

}
