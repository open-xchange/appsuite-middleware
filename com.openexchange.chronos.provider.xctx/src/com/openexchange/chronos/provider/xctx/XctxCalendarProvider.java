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

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_UPDATE_CACHE;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.l;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.FallbackAwareCalendarProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.folder.FolderCalendarProvider;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.subscription.XctxSessionManager;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link XctxCalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxCalendarProvider implements FolderCalendarProvider, FallbackAwareCalendarProvider {

    private static final Logger LOG = LoggerFactory.getLogger(XctxCalendarProvider.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XctxCalendarProvider}.
     *
     * @param services A service lookup reference
     */
    public XctxCalendarProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getId() {
        return Constants.PROVIDER_ID;
    }

    @Override
    public int getDefaultMaxAccounts() {
        return 20; // will use com.openexchange.calendar.xctx2.maxAccounts automatically during checks if set
    }

    @Override
    public boolean getDefaultEnabled() {
        return false; // will use com.openexchange.calendar.xctx2.enabled automatically during checks if set
    }

    @Override
    public boolean isAvailable(Session session) {
        try {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            return false == serverSession.getUser().isGuest() && serverSession.getUserPermissionBits().hasGroupware();
        } catch (OXException e) {
            LOG.warn("Unexpected error while checking xctx2 calendar availability", e);
            return false;
        }
    }

    @Override
    public String getDisplayName(Locale locale) {
        return StringHelper.valueOf(locale).getString(XctxCalendarStrings.PROVIDER_NAME);
    }

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(XctxCalendarAccess.class);
    }

    @Override
    public XctxCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        /*
         * check & handle a known error persisted in the account prior connecting
         */
        handleStoredAccountError(session, account, parameters);
        /*
         * connect and initialize calendar access
         */
        try {
            Session guestSession = doGuestLogin(session, account.getUserConfiguration());
            return new XctxCalendarAccess(services, account, session, guestSession, parameters);
        } catch (OXException e) {
            /*
             * remove calendar account if guest account no longer exists in foreign context, otherwise remember error to fail-fast during repeated attempts
             */
            if (ShareExceptionCodes.UNKNOWN_SHARE.equals(e) && isAutoRemoveUnknownShares(session)) {
                boolean deleted = false;
                LOG.info("Guest account for cross-context share subscription no longer exists, removing calendar account {}.", getAccountId(account), e);
                try {
                    services.getServiceSafe(CalendarAccountService.class).deleteAccount(session, account.getAccountId(), account.getLastModified().getTime(), parameters);
                    deleted = true;
                } catch (OXException x) {
                    LOG.error("Unexpected error removing calendar account {}.", getAccountId(account), x);
                }
                if (deleted) {
                    throw e;
                }
            }
            LOG.info("Error connecting calendar account {}, remembering error in account config before trying again later.", getAccountId(account), e);
            storeAccountError(session, account, e);
            throw e;
        }
    }

    @Override
    public JSONObject configureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        return reconfigureAccount(session, null, userConfig, parameters);
    }

    @Override
    public JSONObject reconfigureAccount(Session session, CalendarAccount calendarAccount, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * get & check necessary configuration properties
         */
        if (null == userConfig || false == userConfig.hasAndNotNull("url")) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("url");
        }
        String url = userConfig.optString("url", null);
        String password = userConfig.optString("password", null);
        /*
         * compare with old config & re-check access as needed by logging in as guest user
         */
        JSONObject oldUserConfig = null != calendarAccount ? calendarAccount.getUserConfiguration() : null;
        if (null == oldUserConfig) {
            doGuestLogin(session, url, password);
        } else {
            if (false == Objects.equals(oldUserConfig.optString("url"), url)) {
                throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("url");
            }
            if (false == Objects.equals(oldUserConfig.optString("password", null), password)) {
                doGuestLogin(session, url, password);
                /*
                 * clear persisted error if login was successful after configuration changes
                 */
                Pair<OXException, Date> lastError = optAccountError(calendarAccount);
                if (null != lastError) {
                    LOG.info("Clearing previously remembered error \"{}\" in calendar account {} to try again after reconfiguration.",
                        lastError.getFirst().getErrorCode(), getAccountId(calendarAccount));
                    setAccountError(calendarAccount, null);
                }
            }
        }
        /*
         * extract & return slipstreamed internal config if set to be saved along with the account, otherwise use empty default
         */
        Object internalConfig = userConfig.remove("internalConfig");
        if (null != internalConfig && JSONObject.class.isInstance(internalConfig)) {
            return (JSONObject) internalConfig;
        }
        return null != calendarAccount ? calendarAccount.getInternalConfiguration() : new JSONObject();
    }

    @Override
    public void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountDeleted(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public CalendarAccess connectFallback(Session session, CalendarAccount account, CalendarParameters parameters, OXException error) {
        return new FallbackXctxCalendarAccess(services, account, session, parameters, error);
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.singleton("password");
    }

    private Session doGuestLogin(Session localSession, JSONObject userConfig) throws OXException {
        if (null == userConfig || false == userConfig.hasAndNotNull("url")) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("url");
        }
        String shareUrl = userConfig.optString("url", null);
        String password = userConfig.optString("password", null);
        return doGuestLogin(localSession, shareUrl, password);
    }

    private Session doGuestLogin(Session localSession, String shareUrl, String password) throws OXException {
        String baseToken = ShareLinks.extractBaseToken(shareUrl);
        return services.getServiceSafe(XctxSessionManager.class).getGuestSession(localSession, baseToken, password);
    }

    /**
     * Handles an error that is persisted in the account's configuration by either re-throwing it, or resetting it so that a new
     * connection attempt can be retried after temporary errors.
     * <p/>
     * Does nothing if no error is persisted in the account.
     *
     * @param session The session
     * @param account The calendar account
     * @param parameters Additional calendar parameters, or <code>null</code> if not specified
     * @throws OXException The persisted account error if applicable
     */
    private void handleStoredAccountError(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        Pair<OXException, Date> lastError = optAccountError(account);
        if (null == lastError) {
            return;
        }
        long timeSinceError = System.currentTimeMillis() - lastError.getSecond().getTime();
        if (timeSinceError < TimeUnit.MINUTES.toMillis(1L)) {
            /*
             * error just happened within last 60 seconds, re-throw without further checks
             */
            throw lastError.getFirst();
        }
        if (null != parameters && b(parameters.get(PARAMETER_UPDATE_CACHE, Boolean.class, Boolean.FALSE)) || timeSinceError >= getRetryAfterErrorInterval(session)) {
            /*
             * client-initiated "retry now", or regular retry-after interval elapsed, clear persisted error & continue
             */
            LOG.info("Clearing previously remembered error \"{}\" in calendar account {} to try again.", lastError.getFirst().getErrorCode(), getAccountId(account));
            storeAccountError(session, account, null);
            return;
        }
        /*
         * re-throw persisted error, otherwise
         */
        throw lastError.getFirst();
    }

    /**
     * Gets the interval in milliseconds when a retry attempt should be made after an error occurred when initializing the calendar access.
     *
     * @param session The session to check the configuration for
     * @return The retry interval in milliseconds
     */
    private long getRetryAfterErrorInterval(Session session) {
        DefaultProperty property = DefaultProperty.valueOf("com.openexchange.calendar.xctx2.retryAfterErrorInterval", L(TimeUnit.HOURS.toMillis(1L)));
        try {
            long value = services.getServiceSafe(LeanConfigurationService.class).getLongProperty(session.getUserId(), session.getContextId(), property);
            if (TimeUnit.MINUTES.toMillis(1L) > value) {
                throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(new Exception("Invalid value " + value + " (less than one minute)"), property);
            }
            return value;
        } catch (OXException e) {
            LOG.error("Error getting {}, falling back to defaults.", property, e);
            return l(property.getDefaultValue(Long.class));
        }
    }

    /**
     * Gets a value indicating whether the automatic removal of accounts in the <i>cross-context</i> calendar provider that refer to a no
     * longer existing guest user in the remote context is enabled or not.
     *
     * @param session The session to check the configuration for
     * @return <code>true</code> if unknown shares should be removed automatically, <code>false</code>, otherwise
     */
    private boolean isAutoRemoveUnknownShares(Session session) {
        DefaultProperty property = DefaultProperty.valueOf("com.openexchange.calendar.xctx2.autoRemoveUnknownShares", Boolean.TRUE);
        try {
            return services.getServiceSafe(LeanConfigurationService.class).getBooleanProperty(session.getUserId(), session.getContextId(), property);
        } catch (OXException e) {
            LOG.error("Error getting {}, falling back to defaults.", property, e);
            return b(property.getDefaultValue(Boolean.class));
        }
    }

    /**
     * Stores an exception as last error within the account's configuration data, then updates the account in the storage.
     *
     * @param session The session
     * @param account The calendar account
     * @param error The error to persist, or <code>null</code> to clear a previously set last error
     * @return The updated calendar account, or the passed account as-is if nothing was changes
     */
    private CalendarAccount storeAccountError(Session session, CalendarAccount account, OXException error) {
        /*
         * serialize & persist error, then update account if needed
         */
        JSONObject updatedInternalConfig = setAccountError(account, error);
        if (null != updatedInternalConfig) {
            try {
                JSONObject userConfig = null != account.getUserConfiguration() ? account.getUserConfiguration() : new JSONObject();
                userConfig.putSafe("internalConfig", updatedInternalConfig);
                return services.getService(CalendarAccountService.class).updateAccount(session, account.getAccountId(), userConfig, account.getLastModified().getTime(), null);
            } catch (OXException e) {
                LOG.warn("Error persisting error in account config", e);
            }
        }
        return account;
    }

    /**
     * Stores an exception as last error within the account's internal configuration data.
     *
     * @param account The calendar account
     * @param error The error to set, or <code>null</code> to clear a previously set last error
     * @return The updated internal configuration of the calendar account, or <code>null</code> if nothing was changed
     */
    private JSONObject setAccountError(CalendarAccount account, OXException error) {
        JSONObject internalConfig = null != account.getInternalConfiguration() ? account.getInternalConfiguration() : new JSONObject();
        /*
         * set or remove error in account config
         */
        if (null == error) {
            return null == internalConfig.remove("lastError") ? null : internalConfig;
        }
        try {
            DataHandler dataHandler = services.getServiceSafe(ConversionService.class).getDataHandler(DataHandlers.OXEXCEPTION2JSON);
            ConversionResult result = dataHandler.processData(new SimpleData<OXException>(error), new DataArguments(), null);
            if (null != result && null != result.getData() && JSONObject.class.isInstance(result.getData())) {
                JSONObject errorJson = (JSONObject) result.getData();
                errorJson.remove("error_stack");
                errorJson.put("timestamp", System.currentTimeMillis());
                return internalConfig.putSafe("lastError", errorJson);
            }
        } catch (OXException | JSONException e) {
            LOG.error("Unable to process data.", e);
        }
        return null;
    }

    /**
     * Optionally gets a persisted account error that occurred during previous operations from the underlying account configuration.
     *
     * @param account The calendar account to get the persisted account error from
     * @return The account error, paired with the time it occurred, or <code>null</code> if there is none
     */
    private Pair<OXException, Date> optAccountError(CalendarAccount account) {
        if (null == account.getInternalConfiguration()) {
            return null;
        }
        JSONObject jsonObject = account.getInternalConfiguration().optJSONObject("lastError");
        if (null == jsonObject) {
            return null;
        }
        long timestamp = jsonObject.optLong("timestamp", 0L);
        try {
            DataHandler dataHandler = services.getServiceSafe(ConversionService.class).getDataHandler(DataHandlers.JSON2OXEXCEPTION);
            ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(jsonObject), new DataArguments(), null);
            if (null != result && null != result.getData() && OXException.class.isInstance(result.getData())) {
                return new Pair<OXException, Date>((OXException) result.getData(), new Date(timestamp));
            }
        } catch (OXException e) {
            LOG.error("Unable to process data.", e);
        }
        return null;
    }
    
    /**
     * Returns the account identifier of the specified account
     *
     * @param calendarAccount The calendar account
     * @return The account identifier
     */
    private Integer getAccountId(CalendarAccount calendarAccount) {
        return I(calendarAccount.getAccountId());
    }
}
