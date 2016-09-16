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

package com.openexchange.net.ssl;

import javax.net.ssl.SSLSocketFactory;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Tools;
import com.openexchange.log.LogProperties;
import com.openexchange.net.ssl.config.SSLProperties;
import com.openexchange.net.ssl.config.TrustLevel;
import com.openexchange.net.ssl.osgi.Services;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * The provider of the {@link SSLSocketFactory} based on the configuration made by the administrator.
 *
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class SSLSocketFactoryProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SSLSocketFactoryProvider.class);

    /**
     * Returns the configured {@link SSLSocketFactory}. This method is invoked by by reflection.
     * <p>
     * Do not use the underlying {@link SSLSocketFactory} directly as this will bypass the server configuration.
     * 
     * @return {@link TrustedSSLSocketFactory} or {@link TrustAllSSLSocketFactory} based on the configuration
     */
    public static SSLSocketFactory getDefault() {
        if (SSLProperties.trustLevel().equals(TrustLevel.TRUST_ALL)) {
            return TrustAllSSLSocketFactory.getDefault();
        }
        try {
            int user = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_USER_ID));
            int context = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID));

            Boolean isUserAllowedToConfigureTrustlevel = Services.getService(ConfigViewFactory.class).getView(user, context).get("com.openexchange.net.ssl.user.configuration.enabled", Boolean.class);
            if (isUserAllowedToConfigureTrustlevel.booleanValue()) {
                if ((user == -1) || (context == -1)) {
                    return TrustedSSLSocketFactory.getDefault();
                }
                UserTrustConfiguration userTrustConfiguration = Services.getService(UserTrustConfiguration.class);
                if ((userTrustConfiguration != null) && (userTrustConfiguration.isTrustAll(user, context))) {
                    return TrustAllSSLSocketFactory.getDefault();
                }
            }
        } catch (OXException e) {
            LOG.error("Unable to retrieve SSL configuration made by the user. Fall back to use truststore associated settings.", e);
        }

        return TrustedSSLSocketFactory.getDefault();
    }
}
