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

package com.openexchange.chronos.provider.ical;

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.optPropertyValue;
import static com.openexchange.chronos.provider.ical.ICalCalendarConstants.NAME;
import static com.openexchange.chronos.provider.ical.ICalCalendarConstants.PROVIDER_ID;
import static com.openexchange.chronos.provider.ical.ICalCalendarConstants.REFRESH_INTERVAL;
import static com.openexchange.chronos.provider.ical.ICalCalendarConstants.URI;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.Duration;
import org.json.JSONObject;
import com.openexchange.auth.info.AuthType;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarAccountAttribute;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.CachingCalendarUtils;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarProvider;
import com.openexchange.chronos.provider.ical.auth.AdvancedAuthInfo;
import com.openexchange.chronos.provider.ical.auth.ICalAuthParser;
import com.openexchange.chronos.provider.ical.conn.ICalFeedClient;
import com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionCodes;
import com.openexchange.chronos.provider.ical.result.GetResponse;
import com.openexchange.chronos.provider.ical.result.GetResponseState;
import com.openexchange.chronos.provider.ical.utils.ICalProviderUtils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 *
 * {@link BasicICalCalendarProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class BasicICalCalendarProvider extends BasicCachingCalendarProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicICalCalendarProvider.class);

    @Override
    public String getId() {
        return ICalCalendarConstants.PROVIDER_ID;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return StringHelper.valueOf(locale).getString(ICalCalendarStrings.PROVIDER_NAME);
    }

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(BasicICalCalendarAccess.class);
    }

    @Override
    public BasicCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return new BasicICalCalendarAccess(session, account, parameters);
    }

    @Override
    public void onAccountCreatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    public void onAccountUpdatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    public void onAccountDeletedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    public void onAccountDeletedOpt(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    public CalendarSettings probe(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * check feed uri
         */
        JSONObject config = settings.getConfig();
        if (null == config || false == config.hasAndNotNull(URI)) {
            throw ICalProviderExceptionCodes.MISSING_FEED_URI.create();
        }
        String uri = config.optString(URI, null);
        /*
         * attempt to read and parse feed & take over extracted metadata as needed
         */
        ICalCalendarFeedConfig feedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, new JSONObject(settings.getConfig()), new JSONObject()).build();
        ICalFeedClient feedClient = new ICalFeedClient(session, feedConfig);
        GetResponse feedResponse = feedClient.executeRequest();
        if (feedResponse.getState() == GetResponseState.REMOVED) {
            throw ICalProviderExceptionCodes.NO_FEED.create(feedConfig.getFeedUrl());
        }

        Long refreshInterval = null;
        if (Strings.isNotEmpty(feedResponse.getRefreshInterval())) {
            try {
                Duration duration = org.dmfs.rfc5545.Duration.parse(feedResponse.getRefreshInterval());
                refreshInterval = L(TimeUnit.MILLISECONDS.toMinutes(duration.toMillis()));
            } catch (IllegalArgumentException e) {
                LOG.warn("Ignoring unparsable refresh interval \"{}\" from calendar feed \"{}\".", feedResponse.getRefreshInterval(), uri, e);
            }
        }
        //TODO: check against some minimum refresh interval?
        String color = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL, String.class);
        if (Strings.isEmpty(color)) {
            color = feedResponse.getFeedColor();
        }
        String description = optPropertyValue(settings.getExtendedProperties(), DESCRIPTION_LITERAL, String.class);
        if (Strings.isEmpty(description)) {
            description = feedResponse.getFeedDescription();
        }
        Boolean usedForSync = optPropertyValue(settings.getExtendedProperties(), USED_FOR_SYNC_LITERAL, Boolean.class);
        if (null == usedForSync || false == CachingCalendarUtils.canBeUsedForSync(PROVIDER_ID, session)) {
            usedForSync = Boolean.FALSE;
        }
        String name = settings.getName();
        if (Strings.isEmpty(name)) {
            name = Strings.isNotEmpty(feedResponse.getFeedName()) ? feedResponse.getFeedName() : "Calendar";
        }
        /*
         * prepare & return proposed settings, taking over client-supplied values if applicable
         */
        CalendarSettings proposedSettings = new CalendarSettings();
        JSONObject proposedConfig = new JSONObject();
        ExtendedProperties proposedExtendedProperties = new ExtendedProperties();
        proposedConfig.putSafe(URI, uri);
        if (null != refreshInterval) {
            proposedConfig.putSafe(REFRESH_INTERVAL, refreshInterval);
        }
        AdvancedAuthInfo authInfo = feedConfig.getAuthInfo();
        addAuthInfo(proposedConfig, authInfo);

        if (null != color) {
            proposedExtendedProperties.add(COLOR(color, false));
        }
        if (null != description) {
            proposedExtendedProperties.add(DESCRIPTION(description, false));
        }
        if (null != usedForSync) {
            proposedExtendedProperties.add(USED_FOR_SYNC(usedForSync, false == CachingCalendarUtils.canBeUsedForSync(PROVIDER_ID, session)));
        }
        proposedSettings.setConfig(proposedConfig);
        proposedSettings.setExtendedProperties(proposedExtendedProperties);
        proposedSettings.setName(name);
        proposedSettings.setSubscribed(true);
        return proposedSettings;
    }

    private void addAuthInfo(JSONObject proposedConfig, AdvancedAuthInfo authInfo) {
        if (authInfo != null && authInfo.getAuthType() == AuthType.BASIC) {
            String login = authInfo.getLogin();
            if (Strings.isNotEmpty(login)) {
                proposedConfig.putSafe(CalendarAccountAttribute.LOGIN_LITERAL.getName(), login);
            }
            String password = authInfo.getPassword();
            if (Strings.isNotEmpty(password)) {
                proposedConfig.putSafe(CalendarAccountAttribute.PASSWORD_LITERAL.getName(), password);
            }
        }
    }

    @Override
    public JSONObject configureAccountOpt(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * check & adjust passed user config as needed
         */
        JSONObject config = settings.getConfig();
        if (null == config || false == config.hasAndNotNull(URI)) {
            throw ICalProviderExceptionCodes.MISSING_FEED_URI.create();
        }
        String uri = config.optString(URI, null);
        ICalProviderUtils.verifyURI(uri);
        ICalCalendarFeedConfig iCalFeedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, new JSONObject(config), new JSONObject()).build();
        if (AuthType.BASIC.equals(iCalFeedConfig.getAuthInfo().getAuthType())) {
            ICalAuthParser.encrypt(config, session.getPassword());
        }
        if (config.hasAndNotNull(ICalCalendarConstants.REFRESH_INTERVAL)) {
            Object opt = config.opt(ICalCalendarConstants.REFRESH_INTERVAL);

            if(opt!=null && !(opt instanceof Number)){
                throw ICalProviderExceptionCodes.BAD_PARAMETER.create(ICalCalendarConstants.REFRESH_INTERVAL, opt);
            }
        }
        /*
         * prepare & return internal config, taking over client-supplied values if applicable
         */
        JSONObject internalConfig = new JSONObject();
        internalConfig.putSafe(NAME, Strings.isNotEmpty(settings.getName()) ? settings.getName() : "Calendar");
        internalConfig.putSafe("subscribed", settings.isSubscribed());
        String color = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL, String.class);
        if (Strings.isNotEmpty(color)) {
            internalConfig.putSafe("color", color);
        }
        String description = optPropertyValue(settings.getExtendedProperties(), DESCRIPTION_LITERAL, String.class);
        if (Strings.isNotEmpty(description)) {
            internalConfig.putSafe("description", description);
        }
        Boolean usedForSyncValue = optPropertyValue(settings.getExtendedProperties(), USED_FOR_SYNC_LITERAL, Boolean.class);
        if (null != usedForSyncValue) {
            boolean usedForSync = usedForSyncValue.booleanValue();
            if (false == internalConfig.has("usedForSync") || usedForSync != internalConfig.optBoolean("usedForSync", false)) {
                if (usedForSync && false == CachingCalendarUtils.canBeUsedForSync(PROVIDER_ID, session)) {
                    throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(USED_FOR_SYNC_LITERAL);
                }
                internalConfig.putSafe("usedForSync", usedForSync);
            }
        }
        return internalConfig;
    }

    @Override
    public JSONObject reconfigureAccountOpt(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        /*
         * check & adjust passed user config as needed
         */
        if (settings.containsConfig()) {
            JSONObject config = settings.getConfig();
            if (null == config || false == config.hasAndNotNull(URI)) {
                throw ICalProviderExceptionCodes.MISSING_FEED_URI.create();
            }
            String uri = config.optString(URI, null);
            ICalProviderUtils.verifyURI(uri);
            if (!uri.equals(account.getUserConfiguration().optString(URI))) {
                throw ICalProviderExceptionCodes.NOT_ALLOWED_CHANGE.create("uri");
            }
            ICalCalendarFeedConfig iCalFeedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, new JSONObject(config), new JSONObject()).build();
            if (AuthType.BASIC.equals(iCalFeedConfig.getAuthInfo().getAuthType())) {
                ICalAuthParser.encrypt(config, session.getPassword());
            }
        }
        /*
         * check & apply changes to internal config
         */
        boolean changed = false;
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        if (settings.containsExtendedProperties()) {
            String color = optPropertyValue(settings.getExtendedProperties(), COLOR_LITERAL, String.class);
            if (false == Objects.equals(color, internalConfig.optString("color", null))) {
                internalConfig.putSafe("color", color);
                changed = true;
            }
            String description = optPropertyValue(settings.getExtendedProperties(), DESCRIPTION_LITERAL, String.class);
            if (false == Objects.equals(description, internalConfig.optString("description", null))) {
                internalConfig.putSafe("description", description);
                changed = true;
            }
            Boolean usedForSync = Boolean.valueOf(optPropertyValue(settings.getExtendedProperties(), USED_FOR_SYNC_LITERAL, String.class));
            if (false == Objects.equals(usedForSync, internalConfig.has("usedForSync") ? B(internalConfig.optBoolean("usedForSync")) : null)) {
                if (false == CachingCalendarUtils.canBeUsedForSync(PROVIDER_ID, session)) {
                    throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(USED_FOR_SYNC_LITERAL);
                }
                internalConfig.putSafe("usedForSync", usedForSync);
                changed = true;
            }
        }
        if (settings.containsName() && false == Objects.equals(settings.getName(), internalConfig.optString("name", null))) {
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
    public boolean triggerCacheInvalidation(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException {
        ICalCalendarFeedConfig oldFeedConfig = new ICalCalendarFeedConfig.DecryptedBuilder(session, new JSONObject(originUserConfiguration), new JSONObject()).build();
        JSONObject newUserConfigurationCopy = new JSONObject(newUserConfiguration);
        ICalCalendarFeedConfig newFeedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, newUserConfigurationCopy, new JSONObject()).build();
        return oldFeedConfig.mandatoryChanges(newFeedConfig);
    }

    @Override
    public void checkAllowedUpdate(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException {
        ICalCalendarFeedConfig oldFeedConfig = new ICalCalendarFeedConfig.DecryptedBuilder(session, new JSONObject(originUserConfiguration), new JSONObject()).build();
        JSONObject newUserConfigurationCopy = new JSONObject(newUserConfiguration);
        ICalCalendarFeedConfig newFeedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, newUserConfigurationCopy, new JSONObject()).build();

        if (!newFeedConfig.getFeedUrl().equalsIgnoreCase(oldFeedConfig.getFeedUrl())) {
            throw ICalProviderExceptionCodes.NOT_ALLOWED_CHANGE.create("URI");
        }
    }

    @Override
    public int getDefaultMaxAccounts() {
        return 50;
    }
}
