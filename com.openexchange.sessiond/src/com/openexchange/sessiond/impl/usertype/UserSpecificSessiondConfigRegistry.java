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

package com.openexchange.sessiond.impl.usertype;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.session.UserAndContext;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link UserSpecificSessiondConfigRegistry}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class UserSpecificSessiondConfigRegistry {

    public enum USER_TYPE {
        USER, GUEST, ANONYMOUS;
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserSpecificSessiondConfigRegistry.class);

    private static final Cache<UserAndContext, USER_TYPE> CACHE = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(60, TimeUnit.MINUTES).build();

    private final ConcurrentMap<USER_TYPE, UserTypeSessiondConfigInterface> map;

    private final ConfigurationService conf;

    public UserSpecificSessiondConfigRegistry(ConfigurationService conf) {
        this.conf = conf;
        this.map = new ConcurrentHashMap<USER_TYPE, UserTypeSessiondConfigInterface>();
    }

    public void init() {
        UserTypeSessiondConfigInterface userConfig = new SessiondUserConfigImpl(conf);
        map.put(userConfig.handles(), userConfig);
        UserTypeSessiondConfigInterface linkConfig = new SessiondLinkConfigImpl(conf);
        map.put(linkConfig.handles(), linkConfig);
        UserTypeSessiondConfigInterface guestConfig = new SessiondGuestConfigImpl(conf);
        map.put(guestConfig.handles(), guestConfig);
    }

    public List<UserTypeSessiondConfigInterface> getServices() {
        return (List<UserTypeSessiondConfigInterface>) map.values();
    }

    public void clear() {
        this.map.clear();
        CACHE.invalidateAll();
    }

    public UserTypeSessiondConfigInterface getService(int userId, int contextId) throws OXException {
        if (this.map.isEmpty()) {
            throw SessionExceptionCodes.NOT_INITIALIZED.create();
        }

        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        USER_TYPE result = CACHE.getIfPresent(key);

        if (null == result) {
            UserService userService = Services.optService(UserService.class);
            if (userService == null) {
                LOG.warn("Unable to retrieve UserService. Can handle sessions only via user settings as evaluation of user type is not possible.");
                return this.map.get(USER_TYPE.USER);
            }
            User user = userService.getUser(userId, contextId);
            if (isAnonymousGuest(user)) {
                result = USER_TYPE.ANONYMOUS;
            } else if (user.isGuest()) {
                result = USER_TYPE.GUEST;
            } else {
                result = USER_TYPE.USER;
            }
            CACHE.put(key, result);
        }
        return this.map.get(result);
    }

    private static boolean isAnonymousGuest(User user) {
        if (user.isGuest() && Strings.isEmpty(user.getMail())) {
            return true;
        }
        return false;
    }
}
