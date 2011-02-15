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
import com.openexchange.config.cascade.ConfigCascadeException;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.user.UserService;

/**
 * {@link UserConfigProvider}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserConfigProvider implements ConfigProviderService {

    private static final String DYNAMIC_ATTR_PREFIX = "config/";

    private UserService users;

    private ContextService contexts;
    
    public UserConfigProvider(UserService users, ContextService contexts) {
        super();
        this.users = users;
        this.contexts = contexts;
    }

    public BasicProperty get(final String property, final int context, final int userId) throws ConfigCascadeException {
        if(context == NO_CONTEXT && userId == NO_USER) {
            return NO_PROPERTY;
        }
        try {
            final Context ctx = contexts.getContext(context);
            final User user = users.getUser(userId, ctx);


            return new BasicProperty() {

                public String get() {
                    Map<String, Set<String>> attributes = user.getAttributes();
                    
                    Set<String> set = attributes.get(DYNAMIC_ATTR_PREFIX + property);
                    if (set == null || set.isEmpty()) {
                        return null;
                    }
                    return set.iterator().next();
                }

                public String get(String metadataName) throws ConfigCascadeException {
                    return null;
                }

                public boolean isDefined() throws ConfigCascadeException {
                    return get() != null;
                }

                public void set(String value) throws ConfigCascadeException {
                    try {
                        users.setAttribute(DYNAMIC_ATTR_PREFIX+property, value, userId, ctx);
                    } catch (UserException e) {
                        throw new ConfigCascadeException(e);
                    }
                }
                public void set(String metadataName, String value) throws ConfigCascadeException {
                    throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, "user");
                }

                public List<String> getMetadataNames() throws ConfigCascadeException {
                    return Collections.emptyList();
                }
                
            };
        } catch (AbstractOXException e) {
            throw new ConfigCascadeException(e);
        }

    }

    public Collection<String> getAllPropertyNames(int context, int userId) throws ConfigCascadeException {
        if(context == NO_CONTEXT && userId == NO_CONTEXT) {
            return Collections.emptyList();
        }
        try {
            final User user = users.getUser(userId, contexts.getContext(context));
            Map<String, Set<String>> attributes = user.getAttributes();
            Set<String> allNames = new HashSet<String>();
            int snip = DYNAMIC_ATTR_PREFIX.length();
            for(String name : attributes.keySet()) {
                if(name.startsWith(DYNAMIC_ATTR_PREFIX)) {
                    allNames.add(name.substring(snip));
                }
            }
            return allNames;
        } catch (AbstractOXException e) {
            throw new ConfigCascadeException(e);
        }   
    }

}
