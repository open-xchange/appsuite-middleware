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
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;

/**
 * {@link LoginAuthSessionGenerator} - The session generator for common login/password access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class LoginAuthSessionGenerator extends AbstractSessionGenerator {

    /**
     * Initializes a new {@link LoginAuthSessionGenerator}.
     */
    public LoginAuthSessionGenerator(ServiceLookup services) {
        super("28ed502e78fa4a2c9aaae5c4fa6f72bf", services);
    }

    @Override
    protected boolean isApplicable(boolean isOAuthEnabled, Session session) throws OXException {
        return !isOAuthEnabled && (MailProperties.getInstance().getPasswordSource(session.getUserId(), session.getContextId()) != PasswordSource.GLOBAL);
    }

    @Override
    protected void enhanceExtendedProperties(Map<String, Object> extendedProperties, Session session) throws OXException {
        String password = session.getPassword();
        if (password != null) {
            ObfuscatorService obfuscatorService = services.getServiceSafe(ObfuscatorService.class);
            extendedProperties.put(MailDataExportPropertyNames.PROP_PASSWORD, obfuscatorService.obfuscate(password));
        }
    }

    @Override
    protected void enhanceGeneratedSession(GeneratedSession session, Map<String, Object> moduleProperties) throws OXException {
        String password = (String) moduleProperties.get(MailDataExportPropertyNames.PROP_PASSWORD);
        if (password != null) {
            ObfuscatorService obfuscatorService = services.getServiceSafe(ObfuscatorService.class);
            session.setPassword(obfuscatorService.unobfuscate(password));
        }
    }

}
