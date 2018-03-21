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

package com.openexchange.chronos.provider.caching.basic;

import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.BasicCalendarProvider;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link BasicCachingCalendarProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class BasicCachingCalendarProvider implements BasicCalendarProvider {

    @Override
    public final JSONObject configureAccount(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException {
        //Nothing caching specific to do
        return configureAccountOpt(session, settings, parameters);
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
        //Nothing caching specific to do
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
                    accountCaching.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, 0);
                }
            }
        }

        JSONObject reconfigureAccountOpt = reconfigureAccountOpt(session, account, settings, parameters);
        if (reconfigureAccountOpt == null) { // make sure changes from caching will be used
            return internalConfiguration;
        }
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
        //Nothing caching specific to do; a possible cleanup already happened here: CachingCalendarProvider.reconfigureAccount(Session, CalendarAccount, JSONObject, CalendarParameters)
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
        delete(session.getContextId(), account, parameters);

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
        delete(context.getContextId(), account, parameters);

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

    private void delete(int contextId, CalendarAccount account, CalendarParameters parameters) throws OXException {

        new OSGiCalendarStorageOperation<Void>(Services.getServiceLookup(), contextId, account.getAccountId()) {

            @Override
            protected Void call(CalendarStorage storage) throws OXException {
                storage.getUtilities().deleteAllData();
                return null;
            }
        }.executeUpdate();
    }
}
