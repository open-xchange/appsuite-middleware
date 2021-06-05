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

package com.openexchange.net.ssl.config.impl.internal;

import static com.openexchange.java.Autoboxing.I;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.net.ssl.config.TrustLevel;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.user.UserService;

/**
 * The {@link UserAwareSSLConfigurationImpl} provides user specific configuration with regards to SSL
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class UserAwareSSLConfigurationImpl implements UserAwareSSLConfigurationService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserAwareSSLConfigurationImpl.class);

    private final UserService userService;
    private final ContextService contextService;
    private final ConfigViewFactory configViewFactory;
    private final ConfigurationService configService;

    /**
     * Initializes a new {@link UserAwareSSLConfigurationImpl}.
     *
     * @param userService The suer service
     * @param contextService The context service
     * @param configViewFactory The config-cascade service
     */
    public UserAwareSSLConfigurationImpl(UserService userService, ContextService contextService, ConfigurationService configService, ConfigViewFactory configViewFactory) {
        this.userService = userService;
        this.contextService = contextService;
        this.configService = configService;
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
            return userTrustsAll == null ? false : userTrustsAll.equalsIgnoreCase("true");
        } catch (OXException e) {
            LOG.error("Unable to retrieve trust level based on user attribute {} for user {} in context {}", e, USER_ATTRIBUTE_NAME, I(userId), I(contextId));
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
            return isUserAllowedToDefineTrustlevel == null ? false : isUserAllowedToDefineTrustlevel.booleanValue();
        } catch (OXException e) {
            LOG.error("Unable to retrieve trust level based on user attribute {} for user {} in context {}", e, USER_ATTRIBUTE_NAME, I(userId), I(contextId));
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
            LOG.error("Unable to set trust level for user {} in context {}", e, USER_ATTRIBUTE_NAME, I(userId), I(context.getContextId()));
        }
    }

    @Override
    public boolean canManageCertificates(int userId, int contextId) {
        boolean allowedToDefineTrustLevel = isAllowedToDefineTrustLevel(userId, contextId);
        if (!allowedToDefineTrustLevel) {
            return false;
        }
        return TrustLevel.TRUST_RESTRICTED.equals(SSLProperties.trustLevel(configService));
    }
}
