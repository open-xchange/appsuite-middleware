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

package com.openexchange.config.cascade.user;

import static com.openexchange.user.UserExceptionCode.USER_NOT_FOUND;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.config.cascade.user.cache.PropertyMap;
import com.openexchange.config.cascade.user.cache.PropertyMapManagement;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link UserConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserConfigProvider implements ConfigProviderService {

    /** The cache region name */
    private static final String REGION_NAME = "User";

    /** The attribute prefix */
    static final String DYNAMIC_ATTR_PREFIX = "config/";

    // ------------------------------------------------------------------------------------------------------

    /** The service look-up */
    private final ServiceLookup services;

    /**
     * Initializes a new {@link UserConfigProvider}.
     *
     * @param services The service look-up
     */
    public UserConfigProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getScope() {
    	return ConfigViewScope.USER.getScopeName();
    }

    /**
     * Gets the associated user.
     *
     * @param userId The user identifier
     * @param ctx The context
     * @return The user
     * @throws OXException If obtaining user fails
     */
    private User getUser(int userId, Context ctx) throws OXException {
    	// Most often we will talk about the current user, so let's try to quickly retrieve that one.
        CacheService cacheService = services.getService(CacheService.class);
        if (cacheService == null) {
            return services.getService(UserService.class).getUser(userId, ctx);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        if (cache == null) {
            return services.getService(UserService.class).getUser(userId, ctx);
        }

        Object obj = cache.get(cacheService.newCacheKey(ctx.getContextId(), userId));
        if (obj instanceof User) {
            return (User) obj;
        }

        return services.getService(UserService.class).getUser(userId, ctx);
    }

    @Override
    public BasicProperty get(String propertyName, int contextId, int userId) throws OXException {
        if (userId == NO_USER) {
            return NO_PROPERTY;
        }

        PropertyMap propertyMap = PropertyMapManagement.getInstance().getFor(userId, contextId);
        BasicProperty basicProperty = propertyMap.get(propertyName);
        if (null == basicProperty) {
            BasicProperty loaded;
            try {
                loaded = new BasicPropertyImpl(propertyName, userId, contextId, services);
            } catch (OXException e) {
                if (e.equalsCode(2, "CTX")) {
                    // "CTX-0002" --> No such context
                    return NO_PROPERTY;
                } else if (USER_NOT_FOUND.equals(e)) {
                    // "USR-0010" --> No such user
                    return NO_PROPERTY;
                } else {
                    throw e;
                }
            }

            basicProperty = propertyMap.putIfAbsent(propertyName, loaded);
            if (null == basicProperty) {
                basicProperty = loaded;
            }
        }
        return basicProperty;
    }

    @Override
    public Collection<String> getAllPropertyNames(int contextId, int userId) throws OXException {
        if (userId == NO_USER) {
            return Collections.emptyList();
        }

        try {
            Map<String, String> attributes = getUser(userId, services.getService(ContextService.class).getContext(contextId, UpdateBehavior.DENY_UPDATE)).getAttributes();
            if (attributes.isEmpty()) {
                return Collections.emptyList();
            }

            String dynamicAttrPrefix = DYNAMIC_ATTR_PREFIX;
            int snip = dynamicAttrPrefix.length();
            Set<String> allNames = new HashSet<String>(attributes.size());
            for (String name : attributes.keySet()) {
                if (name.startsWith(dynamicAttrPrefix)) {
                    allNames.add(name.substring(snip));
                }
            }
            return allNames;
        } catch (OXException e) {
            if (false == USER_NOT_FOUND.equals(e)) {
                throw e;
            }

            // "USR-0010" --> No such user
            return Collections.emptyList();
        }
    }

}
