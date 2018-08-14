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

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.optPropertyValue;
import static com.openexchange.osgi.Tools.requireService;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.UserConfigWrapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.AdministrativeCalendarProvider;
import com.openexchange.chronos.provider.AutoProvisioningCalendarProvider;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.BasicCalendarProvider;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.contact.ContactService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.osgi.Tools;
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
public class BirthdaysCalendarProvider implements BasicCalendarProvider, AutoProvisioningCalendarProvider, AdministrativeCalendarProvider {

    /** The identifier of the calendar provider */
    public static final String PROVIDER_ID = "birthdays";

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
    public int getDefaultMaxAccounts() {
        return 1;
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
    public boolean isAvailable(Session session) {
        try {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            return false == serverSession.getUser().isGuest() && serverSession.getUserPermissionBits().hasContact();
        } catch (OXException e) {
            LoggerFactory.getLogger(BirthdaysCalendarProvider.class).warn("Unexpected error while checking contacts calendar availability", e);
            return false;
        }
    }

    @Override
    public JSONObject autoConfigureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * initialize & check user configuration for new account & return default (empty) internal config
         */
        initializeUserConfig(ServerSessionAdapter.valueOf(session), userConfig);
        /*
         * prepare default internal config
         */
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe("name", BirthdaysCalendarStrings.CALENDAR_NAME);
        return internalConfig;
    }

    @Override
    public CalendarSettings probe(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        if (AutoProvisioningCalendarProvider.class.isAssignableFrom(getClass())) {
            /*
             * no probing allowed as accounts are provisioned automatically
             */
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(PROVIDER_ID);
        }
        /*
         * prepare & return default settings, taking over client-supplied values if applicable
         */
        CalendarSettings proposedSettings = new CalendarSettings();
        JSONObject userConfig = settings.containsConfig() ? settings.getConfig() : new JSONObject();
        initializeUserConfig(ServerSessionAdapter.valueOf(session), userConfig);
        proposedSettings.setConfig(userConfig);
        if (settings.containsName() && Strings.isNotEmpty(settings.getName())) {
            proposedSettings.setName(settings.getName());
        }
        if (settings.containsSubscribed()) {
            proposedSettings.setSubscribed(settings.isSubscribed());
        }
        ExtendedProperties proposedExtendedProperties = new ExtendedProperties();
        Object colorValue = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL);
        if (null != colorValue && String.class.isInstance(colorValue)) {
            proposedExtendedProperties.add(COLOR((String) colorValue, false));
        }
        return proposedSettings;
    }

    @Override
    public JSONObject configureAccount(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        if (AutoProvisioningCalendarProvider.class.isAssignableFrom(getClass())) {
            /*
             * no manual account creation allowed as accounts are provisioned automatically
             */
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(PROVIDER_ID);
        }
        /*
         * initialize & check user configuration for new account
         */
        initializeUserConfig(ServerSessionAdapter.valueOf(session), settings.getConfig());
        /*
         * prepare & return internal configuration for new account, taking over client-supplied values if set
         */
        JSONObject internalConfig = new JSONObject();
        Object colorValue = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL);
        if (null != colorValue && String.class.isInstance(colorValue)) {
            internalConfig.putSafe("color", colorValue);
        }
        if (Strings.isNotEmpty(settings.getName())) {
            internalConfig.putSafe("name", settings.getName());
        }
        internalConfig.putSafe("subscribed", settings.isSubscribed());
        return internalConfig;
    }

    @Override
    public JSONObject reconfigureAccount(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * initialize & check passed user config
         */
        if (settings.containsConfig()) {
            initializeUserConfig(ServerSessionAdapter.valueOf(session), settings.getConfig());
        }
        /*
         * check & apply changes to extended properties
         */
        boolean changed = false;
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        if (settings.containsExtendedProperties()) {
            Object colorValue = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL);
            if (null != colorValue && String.class.isInstance(colorValue) && false == colorValue.equals(internalConfig.opt("color"))) {
                internalConfig.putSafe("color", colorValue);
                changed = true;
            }
        }
        if (settings.containsName() && Strings.isNotEmpty(settings.getName()) && false == settings.getName().equals(internalConfig.opt("name"))) {
            internalConfig.putSafe("name", settings.getName());
            changed = true;
        }
        if (settings.containsSubscribed() && settings.isSubscribed() != internalConfig.optBoolean("subscribed", true)) {
            internalConfig.putSafe("subscribed", settings.isSubscribed());
            changed = true;
        }
        return changed ? internalConfig : null;
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
        onAccountDeleted(ServerSessionAdapter.valueOf(session).getContext(), account, parameters);
    }

    @Override
    public void onAccountDeleted(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        new AlarmHelper(services, context, account).deleteAllAlarms();
    }

    @Override
    public BasicCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
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
            List<Alarm> defaultAlarm = configWrapper.getDefaultAlarmDate();
            if (null != defaultAlarm) {
                Check.alarmsAreValid(defaultAlarm);
                Check.haveReleativeTriggers(defaultAlarm);
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
        return new BirthdaysCalendarAccess(services, serverSession, account, parameters);
    }

    @Override
    public Event getEventByAlarm(Context context, CalendarAccount account, String eventId) throws OXException {
        ServerSession session = ServerSessionAdapter.valueOf(account.getUserId(), context.getContextId());
        EventConverter eventConverter = new EventConverter(services, session.getUser().getLocale(), account.getUserId());
        int[] decodeEventId = eventConverter.decodeEventId(eventId);
        ContactService contactService = Tools.requireService(ContactService.class, services);
        Contact contact = contactService.getContact(session, String.valueOf(decodeEventId[0]), String.valueOf(decodeEventId[1]));
        Event seriesMaster = eventConverter.getSeriesMaster(contact);
        return new AlarmHelper(services, context, account).applyAlarms(seriesMaster);
    }

    @Override
    public void touchEvent(Context context, CalendarAccount account, String eventId) throws OXException {
        // nothing to do
    }

}
