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

package com.openexchange.authentication.application.storage.rdb;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING;
import java.util.UUID;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.NamePart;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import com.openexchange.mailmapping.MailResolverService;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Abstract password storage. Provides basic functionality for getting new password, login
 * {@link AbstractAppPasswordStorage}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public abstract class AbstractAppPasswordStorage {

    protected final ServiceLookup lookup;
    protected final AppPasswordGenerator passwordGenerator;

    /**
     * Initializes a new {@link AbstractAppPasswordStorage}.
     *
     * @param lookup The service lookup
     */
    public AbstractAppPasswordStorage(ServiceLookup lookup) {
        super();
        this.lookup = lookup;
        this.passwordGenerator = new AppPasswordGenerator();
    }

    /**
     * Retrieve PasswordMechRegistry
     *
     * @return the PasswordMechRegistry
     * @throws OXException if the service is absent
     */
    private PasswordMechRegistry getMechRegistry() throws OXException {
        return lookup.getServiceSafe(PasswordMechRegistry.class);
    }

    /**
     * Retrieve ContextService
     *
     * @return the ContextService
     * @throws OXException if the service is absent
     */
    private ContextService getContextService() throws OXException {
        return lookup.getServiceSafe(ContextService.class);
    }

    /**
     * Retrieve LeanConfigurationService
     *
     * @return the LeanConfigurationService
     * @throws OXException if the service is absent
     */
    protected LeanConfigurationService getConfigService() throws OXException {
        return lookup.getServiceSafe(LeanConfigurationService.class);
    }

    /**
     * Get the registered DatabaseService
     *
     * @return the DatabaseService
     * @throws OXException if the service is absent
     */
    protected DatabaseService getDatabase() throws OXException {
        return lookup.getServiceSafe(DatabaseService.class);
    }

    /**
     * Create new password hash from new password
     *
     * @param password The password
     * @return PasswordDetails The password details with the encoded password
     * @throws OXException if an error is occurred
     */
    protected PasswordDetails getPasswordHash(String password) throws OXException {
        PasswordMech mech = getMechRegistry().getDefault();
        return mech.encode(password);
    }

    /**
     * Gets the registered CryptoService
     *
     * @return the CryptoService
     * @throws OXException if the service is absent
     */
    protected CryptoService getCryptoService() throws OXException {
        return lookup.getServiceSafe(CryptoService.class);
    }

    /**
     * Decrypt the full password using the app specific password
     *
     * @param encrPassword The encrypted stored password
     * @param password The given password
     * @return The users full password after decryption
     * @throws OXException if an error is occurred
     */
    protected String decryptPassword(String encrPassword, String password) throws OXException {
        return encrPassword != null && password != null ? getCryptoService().decrypt(encrPassword, password) : encrPassword;
    }

    /**
     * Encrypt the users full password prior to database storage
     *
     * @param password Users full password
     * @param appPassword The application specific password to be used as the key
     * @return Encrypted string safe for database storage
     * @throws OXException if an error is occurred
     */
    protected String encryptPassword(String password, String appPassword) throws OXException {
        return password != null && appPassword != null ? getCryptoService().encrypt(password, appPassword) : null;
    }

    /**
     * Compare password to stored hash/salt
     *
     * @param password The password
     * @param type The hash type
     * @param hash The hash
     * @param salt The salt
     * @return True if the password matches
     * @throws OXException if an error is occurred
     */
    protected boolean isMatch(String password, String type, String hash, byte[] salt) throws OXException {
        PasswordMech mech = getMechRegistry().get(type);
        if (mech == null) {
            return false;
        }
        return mech.check(password, hash, salt);
    }

    /**
     * Checks if configured to store the user password. Returns if true. Null if not
     *
     * @param session The session
     * @return The session's password or <code>null</code> if the password is not stored (by configuration setting)
     * @throws OXException if an error is occurred
     */
    private String getPasswordIfConfigured(Session session) throws OXException {
        if (getConfigService().getBooleanProperty(session.getUserId(), session.getContextId(), AppPasswordStorageProperty.STORE_USER_PASSWORD)) {
            return session.getPassword();
        }
        return null;
    }

    /**
     * Create an application password from session with defined type and name
     *
     * @param session Active user session
     * @param appName User defined name
     * @param appType Application type for the password
     * @return new Application Password
     * @throws OXException if an error is occurred
     */
    protected ApplicationPassword createAppPass(Session session, String appName, String appType) throws OXException {
        return ApplicationPassword.builder()
            .setLogin(getLoginName(session))
            .setAppPassword(passwordGenerator.generateRandomPassword())
            .setFullPassword(getPasswordIfConfigured(session))
            .setAppType(appType == null ? "" : appType)
            .setName(appName == null ? "" : appName)
            .setUUID(UUID.randomUUID().toString())
        .build();
    }

    /**
     * Parse out the context from login string and return context Id
     *
     * @param userInfo The user identifier
     * @return Context Id using contextService lookup
     * @throws OXException if an error is occurred
     */
    protected int getContextId(String userInfo) throws OXException {
        NamePart namePart = NamePart.of(getConfigService().getProperty(AppPasswordStorageProperty.CONTEXT_LOOKUP_NAME_PART));
        if (null == namePart) {
            throw AppPasswordExceptionCodes.MISSING_CONFIGURATION.create(AppPasswordStorageProperty.CONTEXT_LOOKUP_NAME_PART.getFQPropertyName());
        }
        String loginContextInfo;
        try {
            loginContextInfo = namePart.getFrom(userInfo, Authenticated.DEFAULT_CONTEXT_INFO);
        } catch (IllegalArgumentException e) {
            throw INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(userInfo);
        }
        int contextId = getContextService().getContextId(loginContextInfo);
        if (-1 == contextId) {
            if (NamePart.FULL.equals(namePart) && "mail".equals(getConfigService().getProperty(AppPasswordStorageProperty.LOGIN_NAME_SOURCE))) {
                MailResolverService mailResolver = lookup.getOptionalService(MailResolverService.class);
                if (null != mailResolver) {
                    contextId = mailResolver.resolve(loginContextInfo).getContextID();
                }
            }
            if (-1 == contextId) {
                throw INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(loginContextInfo);
            }
        }
        return contextId;
    }

    private String getLoginName(Session session) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        String loginNameSource = getConfigService().getProperty(session.getUserId(), session.getContextId(), AppPasswordStorageProperty.LOGIN_NAME_SOURCE);
        switch (Enums.parse(LoginNameSource.class, loginNameSource, LoginNameSource.SESSION)) {
            case MAIL:
                return serverSession.getUser().getMail();
            case USERNAME:
                return serverSession.getUser().getLoginInfo();
            case SYNTHETIC:
                return serverSession.getUser().getLoginInfo() + '@' + serverSession.getContext().getLoginInfo()[0];
            case SESSION:
                String login = session.getLogin();
                if (Strings.isEmpty(login)) {
                    throw AppPasswordExceptionCodes.MISSING_CONFIGURATION.create(AppPasswordStorageProperty.LOGIN_NAME_SOURCE.getFQPropertyName());
                }
                return session.getLogin();
            default:
                throw AppPasswordExceptionCodes.MISSING_CONFIGURATION.create(AppPasswordStorageProperty.LOGIN_NAME_SOURCE.getFQPropertyName());
        }
    }

}
