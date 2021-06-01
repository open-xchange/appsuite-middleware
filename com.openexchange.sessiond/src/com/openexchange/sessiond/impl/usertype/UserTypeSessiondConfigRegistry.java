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

package com.openexchange.sessiond.impl.usertype;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.UserAndContext;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link UserTypeSessiondConfigRegistry}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class UserTypeSessiondConfigRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserTypeSessiondConfigRegistry.class);

    public static enum UserType {
        USER, GUEST, ANONYMOUS;
    }

    // --------------------------------------------------------------------------------------------

    private final Cache<UserAndContext, UserType> cache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final Map<UserType, UserTypeSessiondConfigInterface> map;
    private final UserTypeSessiondConfigInterface fallbackConfig;

    /**
     * Initializes a new {@link UserTypeSessiondConfigRegistry}.
     *
     * @param conf The configuration service to use
     */
    public UserTypeSessiondConfigRegistry(ConfigurationService conf) {
        super();
        EnumMap<UserType, UserTypeSessiondConfigInterface> map = new EnumMap<UserType, UserTypeSessiondConfigInterface>(UserType.class);
        UserTypeSessiondConfigInterface userConfig = new SessiondUserConfigImpl(conf);
        this.fallbackConfig = userConfig;
        map.put(userConfig.getUserType(), userConfig);
        UserTypeSessiondConfigInterface linkConfig = new SessiondLinkConfigImpl(conf);
        map.put(linkConfig.getUserType(), linkConfig);
        UserTypeSessiondConfigInterface guestConfig = new SessiondGuestConfigImpl(conf);
        map.put(guestConfig.getUserType(), guestConfig);
        this.map = ImmutableMap.copyOf(map); // Creates an ImmutableEnumMap
    }

    /**
     * Clears this registry
     */
    public void clear() {
        cache.invalidateAll();
    }

    /**
     * Gets the fall-back configuration
     *
     * @return The fall-back configuration
     */
    public UserTypeSessiondConfigInterface getFallbackConfig() {
        return fallbackConfig;
    }

    /**
     * Gets the Sessiond configuration for specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The configuration for specified user
     * @throws OXException If configuration for specified user cannot be returned
     */
    public UserTypeSessiondConfigInterface getConfigFor(int userId, int contextId) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        UserType userType = cache.getIfPresent(key);
        if (null == userType) {
            UserService userService = Services.optService(UserService.class);
            if (userService == null) {
                LOG.warn("Unable to retrieve UserService. Can handle sessions only via user settings as evaluation of user type is not possible.");
                return fallbackConfig;
            }
            User user = userService.getUser(userId, contextId);
            if (user.isGuest()) {
                userType = Strings.isEmpty(user.getMail()) ? UserType.ANONYMOUS : UserType.GUEST;
            } else {
                userType = UserType.USER;
            }
            cache.put(key, userType);
        }

        UserTypeSessiondConfigInterface userConfig = this.map.get(userType);
        if (null == userConfig) {
            LOG.warn("No such Sessiond user config for {}. Can handle sessions only via user settings.", userType.name());
            return fallbackConfig;
        }
        return userConfig;
    }

    /**
     * Checks if specified user is an anonymous guest.
     *
     * @param user The user to check
     * @return <code>true</code> if user is an anonymous guest; otherwise <code>false</code>
     */
    public static boolean isAnonymousGuest(User user) {
        return user.isGuest() && Strings.isEmpty(user.getMail());
    }
}
