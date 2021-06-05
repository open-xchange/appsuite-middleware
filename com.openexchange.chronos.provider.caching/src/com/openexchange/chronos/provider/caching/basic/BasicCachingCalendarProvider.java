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

package com.openexchange.chronos.provider.caching.basic;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.UserConfigWrapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.FallbackAwareCalendarProvider;
import com.openexchange.chronos.provider.basic.BasicCalendarProvider;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.conversion.ConversionService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link BasicCachingCalendarProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class BasicCachingCalendarProvider implements BasicCalendarProvider, FallbackAwareCalendarProvider {

    @Override
    public final JSONObject configureAccount(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        //Nothing caching specific to do
        JSONObject result = configureAccountOpt(session, settings, parameters);
        checkAlarms(result);
        return result;
    }

    /**
     * Checks if the alarms are valid
     *
     * @param json A json object containing the user config
     * @throws OXException
     */
    private void checkAlarms(JSONObject json) throws OXException {
        try {
            UserConfigWrapper configWrapper = new UserConfigWrapper(requireService(ConversionService.class, Services.getServiceLookup()), json);
            List<Alarm> defaultAlarm = configWrapper.getDefaultAlarmDate();
            if (null != defaultAlarm) {
                Check.alarmsAreValid(defaultAlarm);
                Check.haveReleativeTriggers(defaultAlarm);
            }
            defaultAlarm = configWrapper.getDefaultAlarmDateTime();
            if (null != defaultAlarm) {
                Check.alarmsAreValid(defaultAlarm);
                Check.haveReleativeTriggers(defaultAlarm);
            }
        } catch (OXException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(e, String.valueOf(json));
        }
    }

    /**
     * Initializes the configuration prior creating a new calendar account after the {@link BasicCachingCalendarProvider} has executed desired preparations/cleanups.
     * <p/>
     * Any permission- or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any <i>internal</i> configuration data is returned for
     * persisting along with the newly created calendar account.
     *
     * @param session The user's session
     * @param settings Calendar settings for the new account as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return A JSON object holding the <i>internal</i> configuration to store along with the new account
     */
    protected abstract JSONObject configureAccountOpt(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException;

    @Override
    public final void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // Nothing caching specific to do
        onAccountCreatedOpt(session, account, parameters);
    }

    /**
     * Callback routine that is invoked after a new account for the calendar provider has been created and after the {@link BasicCachingCalendarProvider} has executed desired preparations/cleanups.
     *
     * @param session The user's session
     * @param account The calendar account that was created
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    protected abstract void onAccountCreatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    @Override
    public final JSONObject reconfigureAccount(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        JSONObject internalConfiguration = account.getInternalConfiguration();
        if (settings.containsConfig() && triggerCacheInvalidation(session, account.getUserConfiguration(), settings.getConfig())) {
            if (internalConfiguration.hasAndNotNull(CachingCalendarAccessConstants.CACHING)) {
                JSONObject accountCaching = internalConfiguration.optJSONObject(CachingCalendarAccessConstants.CACHING);
                if (accountCaching != null) {
                    accountCaching.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, I(0));
                }
            }
        }

        JSONObject reconfigureAccountOpt = reconfigureAccountOpt(session, account, settings, parameters);
        if (reconfigureAccountOpt == null) { // make sure changes from caching will be used
            return internalConfiguration;
        }
        checkAlarms(reconfigureAccountOpt);
        return reconfigureAccountOpt;
    }

    /**
     * Checks if the desired update contains fields that shouldn't be allowed to change.
     *
     * @param session The user's session
     * @param originUserConfiguration Previously stored user configuration
     * @param newUserConfiguration New user configuration
     * @throws OXException should be thrown when unchangeable fields might be changed
     */
    @SuppressWarnings("unused")
    public void checkAllowedUpdate(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException {
        // overwrite if desired
    }

    /**
     * Returns if a reconfiguration of the account should trigger a cache invalidation (for the account!) to ensure all associated calendar data will be updated with the next request.
     *
     * @param session The user's session
     * @param originUserConfiguration Previously stored user configuration
     * @param newUserConfiguration New user configuration
     * @return <code>true</code> if the cached data should be recreated; otherwise <code>false</code>
     * @see BasicCachingCalendarProvider#reconfigureAccount(Session, CalendarAccount, JSONObject, CalendarParameters)
     */
    public abstract boolean triggerCacheInvalidation(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException;

    /**
     * Re-initializes the configuration prior updating an existing calendar account and after the {@link BasicCachingCalendarProvider} has executed desired preparations/cleanups.
     * <p/>
     * Any permission- or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any updated <i>internal</i> configuration data is
     * returned for persisting along with the updated calendar account.
     *
     * @param session The user's session
     * @param calendarAccount The currently stored calendar account holding the obsolete user and current <i>internal</i> configuration
     * @param settings The updated settings for the updated account as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return A JSON object holding the updated <i>internal</i> configuration to store along with update, or <code>null</code> if unchanged
     */
    protected abstract JSONObject reconfigureAccountOpt(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException;

    @Override
    public final void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // Nothing caching specific to do; a possible cleanup already happened here: CachingCalendarProvider.reconfigureAccount(Session, CalendarAccount, JSONObject, CalendarParameters)
        onAccountUpdatedOpt(session, account, parameters);
    }

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been updated and after the {@link BasicCachingCalendarProvider} has executed desired preparations/cleanups.
     *
     * @param session The user's session
     * @param account The calendar account that was updated
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    protected abstract void onAccountUpdatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    @Override
    public final void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        deleteAllData(session.getContextId(), account.getAccountId());
        onAccountDeletedOpt(session, account, parameters);
    }

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been deleted and after the {@link BasicCachingCalendarProvider} has executed desired preparations/cleanups.
     *
     * @param session The user's session
     * @param account The calendar account that was deleted
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    protected abstract void onAccountDeletedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    @Override
    public final void onAccountDeleted(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        deleteAllData(context.getContextId(), account.getAccountId());
        onAccountDeletedOpt(context, account, parameters);
    }

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been deleted and after the {@link BasicCachingCalendarProvider} has executed desired preparations/cleanups.
     *
     * @param context The context
     * @param account The calendar account that was deleted
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    protected abstract void onAccountDeletedOpt(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException;

    private static void deleteAllData(int contextId, int accountId) throws OXException {
        new OSGiCalendarStorageOperation<Void>(Services.getServiceLookup(), contextId, accountId) {

            @Override
            protected Void call(CalendarStorage storage) throws OXException {
                storage.getUtilities().deleteAllData();
                return null;
            }
        }.executeUpdate();
    }

    @Override
    public CalendarAccess connectFallback(Session session, CalendarAccount account, CalendarParameters parameters, OXException error) {
        return new FallbackBasicCachingCalendarAccess(session, account, error);
    }

}
