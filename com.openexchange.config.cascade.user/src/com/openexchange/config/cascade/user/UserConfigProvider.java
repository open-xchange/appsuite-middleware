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

package com.openexchange.config.cascade.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.user.cache.PropertyMap;
import com.openexchange.config.cascade.user.cache.PropertyMapManagement;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
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
    	return "user";
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
    public BasicProperty get(String property, int contextId, int userId) throws OXException {
        if (userId == NO_USER) {
            return NO_PROPERTY;
        }

        PropertyMap propertyMap = PropertyMapManagement.getInstance().getFor(userId, contextId);
        BasicProperty basicProperty = propertyMap.get(property);
        if (null == basicProperty) {
            BasicProperty loaded = new BasicPropertyImpl(property, userId, contextId, services);
            basicProperty = propertyMap.putIfAbsent(property, loaded);
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
        Map<String, Set<String>> attributes = getUser(userId, services.getService(ContextService.class).getContext(contextId)).getAttributes();
        Set<String> allNames = new HashSet<String>(attributes.size());

        String dynamicAttrPrefix = DYNAMIC_ATTR_PREFIX;
        int snip = dynamicAttrPrefix.length();

        for (String name : attributes.keySet()) {
            if (name.startsWith(dynamicAttrPrefix)) {
                allNames.add(name.substring(snip));
            }
        }

        return allNames;
    }

}
