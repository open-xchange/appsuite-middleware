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

package com.openexchange.net.ssl.config.impl.jslob;

import static com.openexchange.java.Autoboxing.B;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobKeys;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.session.Session;

/**
 * {@link AcceptUntrustedCertificatesJSLobEntry}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class AcceptUntrustedCertificatesJSLobEntry implements JSlobEntry {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AcceptUntrustedCertificatesJSLobEntry.class);

    private static final String LOB_NAMESPACE = "security";

    private final ContextService contextService;
    private final UserAwareSSLConfigurationService userAwareSSLConfigurationService;

    /**
     * Default constructor.
     *
     * @param contextService
     */
    public AcceptUntrustedCertificatesJSLobEntry(ContextService contextService, UserAwareSSLConfigurationService userAwareSSLConfigurationService) {
        super();
        this.contextService = contextService;
        this.userAwareSSLConfigurationService = userAwareSSLConfigurationService;
    }

    @Override
    public String getKey() {
        return JSlobKeys.CORE;
    }

    @Override
    public String getPath() {
        return LOB_NAMESPACE + "/" + UserAwareSSLConfigurationService.USER_ATTRIBUTE_NAME;
    }

    @Override
    public boolean isWritable(Session session) throws OXException {
        return userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(session.getUserId(), session.getContextId());
    }

    @Override
    public Object getValue(Session sessiond) throws OXException {
        boolean allowedToDefineTrustLevel = userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(sessiond.getUserId(), sessiond.getContextId());
        if (!allowedToDefineTrustLevel) {
            return Boolean.FALSE;
        }

        return B(userAwareSSLConfigurationService.isTrustAll(sessiond.getUserId(), sessiond.getContextId()));
    }

    @Override
    public void setValue(Object value, Session sessiond) throws OXException {
        boolean allowedToDefineTrustLevel = userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(sessiond.getUserId(), sessiond.getContextId());

        if (!allowedToDefineTrustLevel) {
            LOG.debug("Setting {} has been disabled due to configuration ('{}'). The request will be ignored.", UserAwareSSLConfigurationService.USER_ATTRIBUTE_NAME, UserAwareSSLConfigurationService.USER_CONFIG_ENABLED_PROPERTY);
            return;
        }
        userAwareSSLConfigurationService.setTrustAll(sessiond.getUserId(), contextService.getContext(sessiond.getContextId()), Boolean.parseBoolean(value.toString()));
    }

    @Override
    public Map<String, Object> metadata(Session session) throws OXException {
        // nothing special to add
        return null;
    }
}
