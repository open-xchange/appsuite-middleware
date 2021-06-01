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
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobKeys;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.session.Session;

/**
 * {@link UserCanManageOwnCertificatesJSLobEntry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class UserCanManageOwnCertificatesJSLobEntry implements JSlobEntry {

    private static final String LOB_NAMESPACE = "security";
    private static final String LOB_PATH = LOB_NAMESPACE + "/manageCertificates";

    private final UserAwareSSLConfigurationService userAwareSSLConfigurationService;

    /**
     * Initialises a new {@link UserCanManageOwnCertificatesJSLobEntry}.
     */
    public UserCanManageOwnCertificatesJSLobEntry(UserAwareSSLConfigurationService userAwareSSLConfigurationService) {
        super();
        this.userAwareSSLConfigurationService = userAwareSSLConfigurationService;
    }

    @Override
    public String getKey() {
        return JSlobKeys.CORE;
    }

    @Override
    public String getPath() {
        return LOB_PATH;
    }

    @Override
    public boolean isWritable(Session session) throws OXException {
        return false;
    }

    @Override
    public Object getValue(Session session) throws OXException {
        return B(userAwareSSLConfigurationService.canManageCertificates(session.getUserId(), session.getContextId()));
    }

    @Override
    public void setValue(Object value, Session sessiond) throws OXException {
        // no-op, read-only
    }

    @Override
    public Map<String, Object> metadata(Session session) throws OXException {
        // no-op
        return null;
    }
}
