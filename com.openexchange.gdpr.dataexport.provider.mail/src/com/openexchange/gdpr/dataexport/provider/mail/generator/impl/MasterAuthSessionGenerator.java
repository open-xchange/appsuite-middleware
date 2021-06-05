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

import java.util.Collections;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.GeneratedSession;
import com.openexchange.gdpr.dataexport.provider.mail.MailDataExportPropertyNames;
import com.openexchange.gdpr.dataexport.provider.mail.generator.AbstractSessionGenerator;
import com.openexchange.mail.api.MailConfig.LoginSource;
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.session.Session;

/**
 * {@link MasterAuthSessionGenerator} - The session generator if master authentication is enabled through configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MasterAuthSessionGenerator extends AbstractSessionGenerator {

    /**
     * Initializes a new {@link MasterAuthSessionGenerator}.
     */
    public MasterAuthSessionGenerator() {
        super("b24e63d29b3948e79424d049951fae9f", null);
    }

    @Override
    protected boolean isApplicable(boolean isOAuthEnabled, Session session) throws OXException {
        return !isOAuthEnabled && (MailProperties.getInstance().getPasswordSource(session.getUserId(), session.getContextId()) == PasswordSource.GLOBAL);
    }

    @Override
    public Map<String, Object> craftExtendedProperties(Session session) throws OXException {
        // Pass login name to properties if needed
        LoginSource loginSource = MailProperties.getInstance().getLoginSource(session.getUserId(), session.getContextId());
        if (LoginSource.USER_NAME == loginSource || MailProperties.getInstance().getAuthProxyDelimiter() != null) {
            return Collections.singletonMap(MailDataExportPropertyNames.PROP_LOGIN_NAME, session.getLoginName());
        }
        return Collections.emptyMap();
    }

    @Override
    protected void enhanceExtendedProperties(Map<String, Object> extendedProperties, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    protected void enhanceGeneratedSession(GeneratedSession session, Map<String, Object> moduleProperties) throws OXException {
        // Nothing to do
    }

}
