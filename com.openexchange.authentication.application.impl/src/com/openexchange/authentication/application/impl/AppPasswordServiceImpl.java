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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.authentication.application.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.authentication.application.AppAuthenticatorService;
import com.openexchange.authentication.application.AppLoginRequest;
import com.openexchange.authentication.application.AppPasswordApplication;
import com.openexchange.authentication.application.AppPasswordService;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.RestrictedAuthentication;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.authentication.application.impl.notification.AppPasswordNotifierRegistry;
import com.openexchange.authentication.application.storage.AppPasswordStorage;
import com.openexchange.authentication.application.storage.AuthenticatedApplicationPassword;
import com.openexchange.authentication.application.storage.history.AppPasswordLogin;
import com.openexchange.authentication.application.storage.history.AppPasswordLoginHistoryStorage;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlSanitizeOptions;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AppPasswordServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class AppPasswordServiceImpl implements AppPasswordService, AppAuthenticatorService, Reloadable {

    private static final Logger LOG = LoggerFactory.getLogger(AppPasswordServiceImpl.class);
    private static final String CONFIGFILE = "app-password-apps.yml";

    private final ServiceLookup services;
    private final AppPasswordNotifierRegistry notifierRegistry;
    private final AtomicReference<Map<String, AppPasswordApplication>> appPasswordApps;
    private final ServiceSet<AppPasswordStorage> storages;

    /**
     * Initializes a new {@link AppPasswordServiceImpl}.
     *
     * @param services A service lookup reference
     * @param notifierRegistry The notifier registry to use
     * @param storages The available storage services
     */
    public AppPasswordServiceImpl(ServiceLookup services, AppPasswordNotifierRegistry notifierRegistry, ServiceSet<AppPasswordStorage> storages) throws OXException {
        super();
        this.services = services;
        this.notifierRegistry = notifierRegistry;
        this.storages = storages;
        this.appPasswordApps = new AtomicReference<Map<String, AppPasswordApplication>>(parseApps(services.getServiceSafe(ConfigurationService.class)));
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames(CONFIGFILE).build();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            appPasswordApps.set(parseApps(configService));
        } catch (OXException e) {
            LOG.error("Failed to reload configured app-password apps", e);
        }
    }

    @Override
    public boolean applies(AppLoginRequest loginRequest) {
        /*
         * applies, if client not blacklisted and applicable for storage
         */
        return null != loginRequest && false == isBlacklistedClient(loginRequest.getClient()) && null != optStorage(loginRequest);
    }

    @Override
    public RestrictedAuthentication doAuth(AppLoginRequest loginRequest) throws OXException {
        /*
         * authenticate against responsible storage
         */
        if (isBlacklistedClient(loginRequest.getClient())) {
            throw AppPasswordExceptionCodes.UNSUPPORTED_CLIENT.create(loginRequest.getClient());
        }
        AppPasswordStorage storage = optStorage(loginRequest);
        if (null == storage) {
            throw ServiceExceptionCode.absentService(AppPasswordStorage.class);
        }
        AuthenticatedApplicationPassword appPassword = storage.doAuth(loginRequest.getLogin(), loginRequest.getPassword());
        if (null == appPassword) {
            return null;
        }
        /*
         * get configured application (implicitly checking the app is available for the user)
         */
        String appType = appPassword.getApplicationPassword().getAppType();
        AppPasswordApplication application = getApplicationsById(appPassword.getContextId(), appPassword.getUserId()).get(appType);
        if (null == application) {
            throw AppPasswordExceptionCodes.UNKNOWN_APPLICATION_TYPE.create(appType);
        }
        /*
         * track login, build & return restricted authentication object
         */
        trackLogin(storage, appPassword, loginRequest);
        return new RestrictedAuthenticationImpl(services, appPassword, application.getScopes(), loginRequest.getLogin(), loginRequest.getPassword());
    }

    @Override
    public List<ApplicationPassword> getList(Session session) throws OXException {
        ArrayList<ApplicationPassword> passwords = new ArrayList<ApplicationPassword>();
        for (AppPasswordStorage storage : getStorages()) {
            List<ApplicationPassword> storagePasswords = storage.getList(session);
            if (null != storagePasswords) {
                passwords.addAll(storagePasswords);
            }
        }
        return passwords;
    }

    @Override
    public Map<String, AppPasswordLogin> getLastLogins(Session session) throws OXException {
        Map<String, AppPasswordLogin> loginsPerPassword = new HashMap<String, AppPasswordLogin>();
        for (AppPasswordStorage storage : getStorages()) {
            AppPasswordLoginHistoryStorage historyStorage = storage.getLoginHistoryStorage();
            if (null != historyStorage) {
                Map<String, AppPasswordLogin> history = historyStorage.getHistory(session);
                if (null != history) {
                    loginsPerPassword.putAll(history);
                }
            }
        }
        return loginsPerPassword;
    }

    @Override
    public List<AppPasswordApplication> getApplications(Session session) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (serverSession.getUser().isGuest()) {
            return Collections.emptyList();
        }
        Collection<AppPasswordApplication> applications = getApplicationsById(session.getContextId(), session.getUserId()).values();
        Locale locale = serverSession.getUser().getLocale();
        return getLocalizedApplications(locale, applications);
    }

    @Override
    public void removePassword(Session session, String passwordId) throws OXException {
        for (AppPasswordStorage storage : getStorages()) {
            if (storage.removePassword(session, passwordId)) {
                deleteLastLogin(storage, session, passwordId);
                notifierRegistry.notifyRemovePassword(passwordId);
                removeSessions(session.getContextId(), session.getUserId(), passwordId);
            }
        }
    }

    @Override
    public ApplicationPassword addPassword(Session session, String appName, String appType) throws OXException {
        String checkedType = checkApplicationType(session, appType);
        for (AppPasswordStorage storage : getStorages()) {
            if (storage.handles(session, checkedType)) {
                String sanitizedAppName = sanitize(appName);
                ApplicationPassword password = storage.addPassword(session, sanitizedAppName, checkedType);
                notifierRegistry.notifyAddPassword(password);
                return password;
            }
        }
        throw AppPasswordExceptionCodes.NO_APPLICATION_PASSWORD_STORAGE.create(checkedType);
    }

    /**
     * Sanitize the application name, as this is user entered
     *
     * @param appName The name of the application
     * @return the sanitized name or <code>null</code> if the supplied name is <code>null</code>
     * @throws OXException if an error is occurred
     */
    private String sanitize(String appName) throws OXException {
        if (Strings.isEmpty()) {
            return appName;
        }
        return services.getServiceSafe(HtmlService.class).sanitize(appName, HtmlSanitizeOptions.builder().setSanitize(true).build()).getContent().trim();
    }

    private String checkApplicationType(Session session, String appType) throws OXException {
        if (false == getApplicationsById(session.getContextId(), session.getUserId()).containsKey(appType)) {
            throw AppPasswordExceptionCodes.UNKNOWN_APPLICATION_TYPE.create(appType);
        }
        return appType;
    }


    /**
     * Gets the app-password applications configured and enabled for a specific user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The available applications mapped by their application type id, or an empty map if there are none
     */
    private Map<String, AppPasswordApplication> getApplicationsById(int contextId, int userId) throws OXException {
        Map<String, AppPasswordApplication> availableApps = appPasswordApps.get();
        if (null == availableApps || availableApps.isEmpty()) {
            return Collections.emptyMap();
        }
        LeanConfigurationService configService = services.getServiceSafe(LeanConfigurationService.class);
        String value = configService.getProperty(userId, contextId, AppPasswordProperty.APP_TYPES);
        if (Strings.isEmpty(value)) {
            return Collections.emptyMap();
        }
        String[] appTypes = Strings.splitByComma(value);
        Map<String, AppPasswordApplication> applicationsById = new HashMap<String, AppPasswordApplication>(appTypes.length);
        for (String appType : appTypes) {
            AppPasswordApplication application = availableApps.get(appType);
            if (null == application) {
                LOG.info("Unknwon application type {} configured for user {} in context {}, skipping.", appType, I(userId), I(contextId));
                continue;
            }
            applicationsById.put(appType, application);
        }
        return applicationsById;
    }

    private Collection<String> removeSessions(int contextId, int userId, String passwordId) {
        SessiondService sessiondService = services.getOptionalService(SessiondService.class);
        if (null == sessiondService) {
            LOG.info("Unable to access SessionD service, unable to remove sessions for app-password {}.", passwordId);
            return Collections.emptyList();
        }
        String filterString = new StringBuilder("(&")
            .append('(').append(SessionFilter.CONTEXT_ID).append('=').append(contextId).append(')')
            .append('(').append(SessionFilter.USER_ID).append('=').append(userId).append(')')
            .append('(').append(AppPasswordSessionStorageParameterNamesProvider.PARAM_APP_PASSWORD_ID).append('=').append(passwordId).append(')')
            .append(')')
        .toString();
        try {
            return sessiondService.removeSessionsGlobally(SessionFilter.create(filterString));
        } catch (OXException e) {
            LOG.warn("Error removing sessions for app-password {}", passwordId, e);
            return Collections.emptyList();
        }
    }

    private boolean deleteLastLogin(AppPasswordStorage storage, Session session, String passwordId) {
        AppPasswordLoginHistoryStorage loginHistoryStorage = storage.getLoginHistoryStorage();
        if (null != loginHistoryStorage) {
            try {
                loginHistoryStorage.deleteHistory(session, passwordId);
                return true;
            } catch (OXException e) {
                LOG.warn("Unexpected error deleting login history for {}", passwordId, e);
            }
        }
        return false;
    }

    private boolean trackLogin(AppPasswordStorage storage, AuthenticatedApplicationPassword authenticatedPassword, AppLoginRequest loginRequest) {
        AppPasswordLoginHistoryStorage loginHistoryStorage = storage.getLoginHistoryStorage();
        if (null != loginHistoryStorage) {
            try {
                AppPasswordLogin passwordLogin = AppPasswordLogin.builder().setUserAgent(loginRequest.getUserAgent()).setClient(loginRequest.getClient()).setIpAddress(loginRequest.getClientIP()).setTimestamp(System.currentTimeMillis()).build();
                loginHistoryStorage.trackLogin(authenticatedPassword, passwordLogin);
                return true;
            } catch (OXException e) {
                LOG.warn("Unexpected error updating login history for {}", authenticatedPassword.getApplicationPassword(), e);
            }
        }
        return false;
    }

    private boolean isBlacklistedClient(String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }
        try {
            LeanConfigurationService configurationService = services.getServiceSafe(LeanConfigurationService.class);
            String clients = configurationService.getProperty(AppPasswordProperty.BLACKLISTED_CLIENTS);
            if (Strings.isNotEmpty(clients)) {
                for (String value : Strings.splitBy(clients, ',', true)) {
                    if (clientId.equals(value)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Unexpected error checking if client is blacklisted for app-specific password aithentication", e);
        }
        return false;
    }

    private Collection<AppPasswordStorage> getStorages() {
        return storages;
    }

    private AppPasswordStorage optStorage(AppLoginRequest loginRequest) {
        for (AppPasswordStorage storage : getStorages()) {
            if (handles(storage, loginRequest)) {
                return storage;
            }
        }
        return null;
    }

    private static boolean handles(AppPasswordStorage storage, AppLoginRequest loginRequest) {
        try {
            return null != storage && storage.handles(loginRequest);
        } catch (Exception e) {
            LOG.warn("Unexpected error checking if login request can be handled by app-specific password storage", e);
            return false;
        }
    }

    /**
     * Parses configuration settings for application-password enabled apps from the configuration file <code>app-password-apps.yml</code>.
     *
     * @param configService A reference to the configuration service
     * @return The app-password applications, mapped by their assigned app type, or an empty map if none are defined
     */
    private static Map<String, AppPasswordApplication> parseApps(ConfigurationService configService) throws OXException {
        Map<String, Object> configs = null;
        try {
            Object yaml = configService.getYaml("app-password-apps.yml");
            if (null != yaml) {
                configs = (Map<String, Object>) yaml;
            }
        } catch (Exception e) {
            throw AppPasswordExceptionCodes.MISSING_CONFIGURATION.create(e, CONFIGFILE);
        }
        if (null == configs || 0 == configs.size()) {
            LOG.info("No applications found to use with application-specific passwords in file {}.", CONFIGFILE);
            return Collections.emptyMap();
        }
        Map<String, AppPasswordApplication> applicationsById = new HashMap<String, AppPasswordApplication>(configs.size());
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            try {
                String appType = entry.getKey();
                Map<String, Object> values = (Map<String, Object>) entry.getValue();
                List<String> scopes = (List<String>) values.get("restrictedScopes");
                applicationsById.put(appType, AppPasswordApplication.builder()
                    .setType(appType)
                    .setDisplayName(String.valueOf(values.get("displayName_t10e")))
                    .setSort(values.containsKey("sortOrder") ? Integer.parseInt(String.valueOf(values.get("sortOrder"))) : -1)
                    .setScopes(null == scopes ? new String[0] : scopes.toArray(new String[scopes.size()]))
                .build());
            } catch (Exception e) {
                throw AppPasswordExceptionCodes.MISSING_CONFIGURATION.create(e, entry.getKey());
            }
        }
        return Collections.unmodifiableMap(applicationsById);
    }

    private static List<AppPasswordApplication> getLocalizedApplications(Locale locale, Collection<AppPasswordApplication> applications) {
        if (null == applications || applications.isEmpty()) {
            return Collections.emptyList();
        }
        StringHelper stringHelper = StringHelper.valueOf(locale);
        List<AppPasswordApplication> localizedApplications = new LinkedList<AppPasswordApplication>();
        for (AppPasswordApplication application : applications) {
            localizedApplications.add(AppPasswordApplication.builder()
                .setType(application.getType())
                .setScopes(application.getScopes())
                .setSort(null == application.getSortOrder() ? -1 : application.getSortOrder().intValue())
                .setDisplayName(stringHelper.getString(application.getDisplayName()))
            .build());
        }
        return localizedApplications;
    }

}
