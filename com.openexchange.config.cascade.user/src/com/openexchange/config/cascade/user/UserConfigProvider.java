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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

    private static final String REGION_NAME = "User";

    /** The attribute prefix */
    static final String DYNAMIC_ATTR_PREFIX = "config/";

    /** The service look-up */
    final ServiceLookup services;

    /**
     * Initializes a new {@link UserConfigProvider}.
     *
     * @param services The service look-up
     */
    public UserConfigProvider(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the associated user.
     *
     * @param userId The user identifier
     * @param ctx The context
     * @return The user
     * @throws OXException If obtaining user fails
     */
    private User getUser(final int userId, final Context ctx) throws OXException {
    	// Most often we will talk about the current user, so let's try to quickly retrieve that one.
        /*
         * No more needed due to property cache
    	ThreadLocalSessionHolder sessionHolder = ThreadLocalSessionHolder.getInstance();
    	if (sessionHolder != null && sessionHolder.getContext() != null && sessionHolder.getContext().getContextId() == ctx.getContextId() && sessionHolder.getUser() != null && sessionHolder.getUser().getId() == userId) {
    		return sessionHolder.getUser();
    	}
    	*/

        final CacheService cacheService = services.getService(CacheService.class);
        if (cacheService == null) {
            return services.getService(UserService.class).getUser(userId, ctx);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        if (cache == null) {
            return services.getService(UserService.class).getUser(userId, ctx);
        }
        final Object obj = cache.get(cacheService.newCacheKey(ctx.getContextId(), userId));
        if (obj instanceof User) {
            return (User) obj;
        }
        return services.getService(UserService.class).getUser(userId, ctx);
    }

    @Override
    public BasicProperty get(final String property, final int contextId, final int userId) throws OXException {
        if (contextId == NO_CONTEXT && userId == NO_USER) {
            return NO_PROPERTY;
        }
        final PropertyMap propertyMap = PropertyMapManagement.getInstance().getFor(userId, contextId);
        BasicProperty basicProperty = propertyMap.get(property);
        if (null == basicProperty) {
            final BasicProperty loaded = new BasicPropertyImpl(property, userId, contextId, services);

            // System.out.println("UserConfigProvider.get() invoked: " + property + " -- " + userId + " -- " + contextId + " ==> " + loaded.get());
            // new Throwable().printStackTrace(System.out);

            basicProperty = propertyMap.putIfAbsent(property, loaded);
            if (null == basicProperty) {
                basicProperty = loaded;
            }
        }
        return basicProperty;
    }

    @Override
    public Collection<String> getAllPropertyNames(final int contextId, final int userId) throws OXException {
        if (contextId == NO_CONTEXT && userId == NO_CONTEXT) {
            return Collections.emptyList();
        }
        final User user = getUser(userId, services.getService(ContextService.class).getContext(contextId));
        final Map<String, Set<String>> attributes = user.getAttributes();
        final Set<String> allNames = new HashSet<String>();
        final String dynamicAttrPrefix = DYNAMIC_ATTR_PREFIX;
        final int snip = dynamicAttrPrefix.length();
        for (final String name : attributes.keySet()) {
            if (name.startsWith(dynamicAttrPrefix)) {
                allNames.add(name.substring(snip));
            }
        }
        return allNames;
    }

}
