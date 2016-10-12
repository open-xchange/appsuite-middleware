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

package com.openexchange.net.ssl.config.impl.internal;

import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.user.UserService;

/**
 * The {@link UserAwareSSLConfigurationImpl} provides user specific configuration with regards to SSL
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class UserAwareSSLConfigurationImpl implements UserAwareSSLConfigurationService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserAwareSSLConfigurationImpl.class);

    private UserService userService;

    private ContextService contextService;

    private ConfigViewFactory configViewFactory;

    public UserAwareSSLConfigurationImpl(UserService userService, ContextService contextService, ConfigViewFactory configViewFactory) {
        this.userService = userService;
        this.contextService = contextService;
        this.configViewFactory = configViewFactory;
    }

    @Override
    public boolean isTrustAll(int userId, int contextId) {
        boolean allowedToDefineTrustLevel = isAllowedToDefineTrustLevel(userId, contextId);

        if (!allowedToDefineTrustLevel) {
            return false;
        }

        try {
            String userTrustsAll = this.userService.getUserAttribute(USER_ATTRIBUTE_NAME, userId, this.contextService.getContext(contextId));
            if (userTrustsAll == null) {
                return false;
            }
            return Boolean.parseBoolean(userTrustsAll);
        } catch (OXException e) {
            LOG.error("Unable to retrieve trust level based on user attribute {} for user {} in context {}", e, USER_ATTRIBUTE_NAME, userId, contextId);
        }
        return false;
    }

    @Override
    public boolean isAllowedToDefineTrustLevel(int userId, int contextId) {
        if ((userId <= 0) || (contextId <= 0)) {
            return false;
        }

        try {
            ConfigView view = this.configViewFactory.getView(userId, contextId);
            Boolean isUserAllowedToDefineTrustlevel = view.property(USER_CONFIG_ENABLED_PROPERTY, Boolean.class).get();
            if (isUserAllowedToDefineTrustlevel == null) {
                return false;
            }
            return isUserAllowedToDefineTrustlevel.booleanValue();
        } catch (OXException e) {
            LOG.error("Unable to retrieve trust level based on user attribute {} for user {} in context {}", e, USER_ATTRIBUTE_NAME, userId, contextId);
        }
        return false;
    }

    @Override
    public void setTrustAll(int userId, Context context, boolean trustAll) {
        if (context == null) {
            return;
        }
        boolean allowedToDefineTrustLevel = isAllowedToDefineTrustLevel(userId, context.getContextId());

        if (!allowedToDefineTrustLevel) {
            return;
        }

        try {
            userService.setUserAttribute(USER_ATTRIBUTE_NAME, Boolean.toString(trustAll), userId, context);
        } catch (OXException e) {
            LOG.error("Unable to set trust level for user {} in context {}", e, USER_ATTRIBUTE_NAME, userId, context);
        }
    }

}
