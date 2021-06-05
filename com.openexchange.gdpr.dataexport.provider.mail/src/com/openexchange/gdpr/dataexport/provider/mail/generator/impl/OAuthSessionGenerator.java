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

package com.openexchange.gdpr.dataexport.provider.mail.generator.impl;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.GeneratedSession;
import com.openexchange.gdpr.dataexport.provider.mail.MailDataExportPropertyNames;
import com.openexchange.gdpr.dataexport.provider.mail.generator.AbstractSessionGenerator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;

/**
 * {@link OAuthSessionGenerator} - The session generator in case either <code>XOAUTH2</code> or <code>OAUTHBEARER</code> is configured as
 * authentication type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class OAuthSessionGenerator extends AbstractSessionGenerator {

    /**
     * Initializes a new {@link OAuthSessionGenerator}.
     */
    public OAuthSessionGenerator(ServiceLookup services) {
        super("f4d04539077e422d8c60eaea539f4511", services);
    }

    @Override
    protected boolean isApplicable(boolean isOAuthEnabled, Session session) throws OXException {
        return isOAuthEnabled;
    }

    @Override
    protected void enhanceExtendedProperties(Map<String, Object> extendedProperties, Session session) throws OXException {
        ObfuscatorService obfuscatorService = services.getServiceSafe(ObfuscatorService.class);

        // Access token
        String accessToken = (String) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN);
        if (accessToken != null) {
            extendedProperties.put(MailDataExportPropertyNames.PROP_ACCESS_TOKEN, obfuscatorService.obfuscate(accessToken));
        }

        // Refresh token
        String refreshToken = (String) session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN);
        if (refreshToken != null) {
            extendedProperties.put(MailDataExportPropertyNames.PROP_REFRESH_TOKEN, obfuscatorService.obfuscate(refreshToken));
        }
    }

    @Override
    protected void enhanceGeneratedSession(GeneratedSession session, Map<String, Object> moduleProperties) throws OXException {
        ObfuscatorService obfuscatorService = services.getServiceSafe(ObfuscatorService.class);

        String accessToken = (String) moduleProperties.get(MailDataExportPropertyNames.PROP_ACCESS_TOKEN);
        if (accessToken != null) {
            session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN, obfuscatorService.unobfuscate(accessToken));
        }

        String refreshToken = (String) moduleProperties.get(MailDataExportPropertyNames.PROP_REFRESH_TOKEN);
        if (refreshToken != null) {
            session.setParameter(Session.PARAM_OAUTH_REFRESH_TOKEN, obfuscatorService.unobfuscate(refreshToken));
        }
    }

}
