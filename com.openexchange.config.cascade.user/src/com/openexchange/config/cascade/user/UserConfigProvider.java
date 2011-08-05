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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.UserService;

/**
 * {@link UserConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserConfigProvider implements ConfigProviderService {

    private static final String DYNAMIC_ATTR_PREFIX = "config/";

    private final UserService users;

    private final ContextService contexts;

    public UserConfigProvider(final UserService users, final ContextService contexts) {
        super();
        this.users = users;
        this.contexts = contexts;
    }

    @Override
    public BasicProperty get(final String property, final int context, final int userId) throws OXException {
        if(context == NO_CONTEXT && userId == NO_USER) {
            return NO_PROPERTY;
        }
        {
            final Context ctx = contexts.getContext(context);
            final User user = users.getUser(userId, ctx);


            return new BasicProperty() {

                @Override
                public String get() {
                    final Map<String, Set<String>> attributes = user.getAttributes();

                    final Set<String> set = attributes.get(DYNAMIC_ATTR_PREFIX + property);
                    if (set == null || set.isEmpty()) {
                        return null;
                    }
                    return set.iterator().next();
                }

                @Override
                public String get(final String metadataName) throws OXException {
                    return null;
                }

                @Override
                public boolean isDefined() throws OXException {
                    return get() != null;
                }

                @Override
                public void set(final String value) throws OXException {
                    try {
                        users.setAttribute(DYNAMIC_ATTR_PREFIX+property, value, userId, ctx);
                    } catch (final OXException e) {
                        throw new OXException(e);
                    }
                }
                @Override
                public void set(final String metadataName, final String value) throws OXException {
                    throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, "user");
                }

                @Override
                public List<String> getMetadataNames() throws OXException {
                    return Collections.emptyList();
                }

            };
        }

    }

    @Override
    public Collection<String> getAllPropertyNames(final int context, final int userId) throws OXException {
        if(context == NO_CONTEXT && userId == NO_CONTEXT) {
            return Collections.emptyList();
        }
        {
            final User user = users.getUser(userId, contexts.getContext(context));
            final Map<String, Set<String>> attributes = user.getAttributes();
            final Set<String> allNames = new HashSet<String>();
            final int snip = DYNAMIC_ATTR_PREFIX.length();
            for(final String name : attributes.keySet()) {
                if(name.startsWith(DYNAMIC_ATTR_PREFIX)) {
                    allNames.add(name.substring(snip));
                }
            }
            return allNames;
        }
    }

}
