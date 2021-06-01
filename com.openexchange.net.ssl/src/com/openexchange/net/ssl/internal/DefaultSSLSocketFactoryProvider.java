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

package com.openexchange.net.ssl.internal;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import com.openexchange.java.util.Tools;
import com.openexchange.log.LogProperties;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.TrustedSSLSocketFactory;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.TrustLevel;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.net.ssl.osgi.Services;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * The provider of the {@link SSLSocketFactory} based on the configuration made by the administrator.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class DefaultSSLSocketFactoryProvider implements SSLSocketFactoryProvider {

    private final SSLConfigurationService sslConfigService;

    /**
     * Initializes a new {@link DefaultSSLSocketFactoryProvider}.
     */
    public DefaultSSLSocketFactoryProvider(SSLConfigurationService sslConfigService) {
        super();
        this.sslConfigService = sslConfigService;
    }

    private TrustLevel getEffectiveTrustLevel() {
        if (sslConfigService.getTrustLevel().equals(TrustLevel.TRUST_ALL)) {
            // Globally configured to use trust-all socket factory
            return TrustLevel.TRUST_ALL;
        }

        UserAwareSSLConfigurationService userSSLConfig = Services.getService(UserAwareSSLConfigurationService.class);
        if (null == userSSLConfig) {
            // Absent user-aware SSL config service. This happens for setups w/o a user/context service.
            return TrustLevel.TRUST_ALL;
        }

        // Try to determine by user
        int user = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_USER_ID));
        int context = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID));
        if ((user < 0) || (context < 0)) {
            // No user-specific socket factory selectable
            return TrustLevel.TRUST_RESTRICTED;
        }

        return userSSLConfig.isTrustAll(user, context) ? TrustLevel.TRUST_ALL : TrustLevel.TRUST_RESTRICTED;
    }

    @Override
    public SSLSocketFactory getDefault() {
        TrustLevel effectiveTrustLevel = getEffectiveTrustLevel();
        return TrustLevel.TRUST_ALL == effectiveTrustLevel ? TrustAllSSLSocketFactory.getDefault() : TrustedSSLSocketFactory.getDefault();
    }

    @Override
    public SSLContext getOriginatingDefaultContext() {
        TrustLevel effectiveTrustLevel = getEffectiveTrustLevel();
        return TrustLevel.TRUST_ALL == effectiveTrustLevel ? TrustAllSSLSocketFactory.getCreatingDefaultContext() : TrustedSSLSocketFactory.getCreatingDefaultContext();
    }
}
