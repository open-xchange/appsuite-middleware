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

package com.openexchange.chronos.provider.google;

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.optPropertyValue;
import static com.openexchange.chronos.provider.google.GoogleCalendarConfigField.OAUTH_ID;
import static com.openexchange.osgi.Tools.requireService;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarProvider;
import com.openexchange.chronos.provider.google.access.GoogleCalendarAccess;
import com.openexchange.chronos.provider.google.access.GoogleOAuthAccess;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link GoogleCalendarProvider}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarProvider extends BasicCachingCalendarProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarProvider.class);

    public static final String PROVIDER_ID = "google";
    private static final String DISPLAY_NAME = "Google";

    private final ServiceLookup services;

    /**
     * Initialises a new {@link GoogleCalendarProvider}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public GoogleCalendarProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return DISPLAY_NAME;
    }

    private int checkConfig(JSONObject userConfig) throws OXException {
        if (!userConfig.hasAndNotNull(GoogleCalendarConfigField.OAUTH_ID)) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(userConfig);
        }
        try {
            return userConfig.getInt(GoogleCalendarConfigField.OAUTH_ID);
        } catch (JSONException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(userConfig);
        }
    }

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(GoogleCalendarAccess.class);
    }

    @Override
    public CalendarSettings probe(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * check existence of referenced google oauth account
         */
        JSONObject userConfig = settings.getConfig();
        if (null == userConfig || false == userConfig.hasAndNotNull(OAUTH_ID)) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(userConfig);
        }
        int oauthId;
        try {
            oauthId = userConfig.getInt(OAUTH_ID);
        } catch (JSONException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(e, userConfig);
        }
        OAuthService oAuthService = requireService(OAuthService.class, services);
        oAuthService.getAccount(session, oauthId);
        /*
         * prepare & return checked proposed settings based on client-supplied settings
         */
        CalendarSettings proposedSettings = new CalendarSettings();
        ExtendedProperties proposedExtendedProperties = new ExtendedProperties();

        GoogleOAuthAccess googleOAuthAccess = new GoogleOAuthAccess(oauthId, session);
        googleOAuthAccess.initialize();

        Calendar googleCal = (Calendar) googleOAuthAccess.getClient().getClient();

        try {
            CalendarList list = googleCal.calendarList().list().execute();
            CalendarListEntry primary = null;
            for (CalendarListEntry entry : list.getItems()) {
                if (entry.isPrimary()) {
                    primary = entry;
                    break;
                }
            }
            if (primary != null) {
                userConfig.put(GoogleCalendarConfigField.FOLDER, primary.getId());
                settings.setName(primary.getSummary());
                proposedExtendedProperties.add(COLOR(primary.getColorId(), false));
                proposedExtendedProperties.add(DESCRIPTION(primary.getDescription(), false));
            }
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        proposedSettings.setConfig(userConfig);
        if (settings.containsName()) {
            proposedSettings.setName(settings.getName());
        }

        Object colorValue = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL);
        if (null != colorValue && String.class.isInstance(colorValue)) {
            proposedExtendedProperties.replace(COLOR((String) colorValue, false));
        }
        Object descriptionValue = optPropertyValue(settings.getExtendedProperties(), DESCRIPTION_LITERAL);
        if (null != descriptionValue && String.class.isInstance(descriptionValue)) {
            proposedExtendedProperties.replace(DESCRIPTION((String) descriptionValue, false));
        }
        proposedSettings.setExtendedProperties(proposedExtendedProperties);
        return proposedSettings;
    }

    @Override
    public BasicCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return new GoogleCalendarAccess(session, account, parameters, true);
    }

    @Override
    public boolean isAvailable(Session session) {
        OAuthServiceMetaDataRegistry service = Services.getService(OAuthServiceMetaDataRegistry.class);
        if (service == null) {
            return false;
        }
        try {
            OAuthServiceMetaData metaData = service.getService(KnownApi.GOOGLE.getServiceId(), session.getUserId(), session.getContextId());
            return metaData == null ? false : metaData.isEnabled(session.getUserId(), session.getContextId());
        } catch (OXException e) {
            return false;
        }
    }

    @Override
    protected JSONObject configureAccountOpt(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        JSONObject userConfig = settings.getConfig();
        int accountId = checkConfig(userConfig);

        final OAuthService oAuthService = services.getOptionalService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        // Check existing google account
        oAuthService.getAccount(session, accountId);

        JSONObject internalConfig = new JSONObject();
        try {
            internalConfig.put(GoogleCalendarConfigField.OAUTH_ID, accountId);
            if (userConfig.hasAndNotNull(GoogleCalendarConfigField.FOLDER)) {
                internalConfig.put(GoogleCalendarConfigField.FOLDER, userConfig.getString(GoogleCalendarConfigField.FOLDER));
            }
            internalConfig.put("name", settings.getName());

            // store extended properties
            ExtendedProperties extendedProperties = settings.getExtendedProperties();
            Object colorValue = CalendarFolderProperty.optPropertyValue(extendedProperties, CalendarFolderProperty.COLOR_LITERAL);
            if (colorValue != null) {
                internalConfig.put(GoogleCalendarConfigField.COLOR, colorValue);
            }

            Object description = CalendarFolderProperty.optPropertyValue(extendedProperties, CalendarFolderProperty.DESCRIPTION_LITERAL);
            if (description != null) {
                internalConfig.put(GoogleCalendarConfigField.DESCRIPTION, description);
            }
        } catch (JSONException e) {
            // never happens
            LOG.debug("{}", e.getMessage(), e);
        }
        return internalConfig;
    }

    @Override
    protected JSONObject reconfigureAccountOpt(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        JSONObject config = account.getInternalConfiguration();
        JSONObject updated = new JSONObject(config);
        int accountId = checkConfig(config);

        final OAuthService oAuthService = services.getOptionalService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        // Check existing google account
        oAuthService.getAccount(session, accountId);

        try {
            updated.put(GoogleCalendarConfigField.OAUTH_ID, accountId);
            ExtendedProperties extendedProperties = settings.getExtendedProperties();
            String folder = optPropertyValue(extendedProperties, GoogleCalendarConfigField.FOLDER, String.class);
            if (Strings.isNotEmpty(folder)) {
                updated.put(GoogleCalendarConfigField.FOLDER, folder);
            }
            if (Strings.isNotEmpty(settings.getName())) {
                updated.put(GoogleCalendarConfigField.NAME, settings.getName());
            }

            Object colorValue = optPropertyValue(extendedProperties, COLOR_LITERAL);
            if (colorValue != null) {
                updated.put(GoogleCalendarConfigField.COLOR, colorValue);
            }

            String desc = optPropertyValue(extendedProperties, GoogleCalendarConfigField.DESCRIPTION, String.class);
            if (Strings.isNotEmpty(desc)) {
                updated.put(GoogleCalendarConfigField.DESCRIPTION, desc);
            }
        } catch (JSONException e) {
            // never happens
            LOG.debug("{}", e.getMessage(), e);
        }
        return updated.isEqualTo(config) ? null : updated;
    }

    @Override
    protected void onAccountCreatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) {
        // Nothing to do
    }

    @Override
    protected void onAccountUpdatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) {
        // Nothing to do
    }

    @Override
    protected void onAccountDeletedOpt(Session session, CalendarAccount account, CalendarParameters parameters) {
        // Nothing to do
    }

    @Override
    protected void onAccountDeletedOpt(Context context, CalendarAccount account, CalendarParameters parameters) {
        // Nothing to do
    }

    @Override
    public boolean triggerCacheInvalidation(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException {
        boolean result = checkConfig(newUserConfiguration) != checkConfig(originUserConfiguration);
        if (result) {
            return result;
        }
        try {
            if (newUserConfiguration.hasAndNotNull(GoogleCalendarConfigField.FOLDER) && originUserConfiguration.hasAndNotNull(GoogleCalendarConfigField.FOLDER)) {
                return !newUserConfiguration.get(GoogleCalendarConfigField.FOLDER).equals(originUserConfiguration.get(GoogleCalendarConfigField.FOLDER));
            }
            return newUserConfiguration.get(GoogleCalendarConfigField.FOLDER) != null;
        } catch (JSONException e) {
            LOG.debug("Error while comparing configured folder names: {}", e.getMessage());
            return false;
        }
    }

}
